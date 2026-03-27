# QED Mailpit Integration Guide

## Overview

Mailpit is a lightweight SMTP capture server with a web UI and REST API. Instead of
delivering emails to real inboxes, it holds them so they can be inspected manually or
queried by automated tests.

QED uses Mailpit to verify outbound email flows end-to-end — asserting on subject,
recipient, and link content rather than just checking that an API returns HTTP 200.

**Mailpit is not part of the application under test.** It is a QED infrastructure
component. For VM setup and installation, see
`qed-mailpit-infrastructure.md`.

---

## Configuration

The Mailpit API base URL is a QED configuration value. Add it to the QED configuration
file alongside other infrastructure URLs:

```
mailpitBaseUrl = http://192.168.56.13:8025
```

`MailpitHelper` reads this value at startup. If the VM is ever moved or the port
changes, only the config needs updating.

**Note on addresses:** The Mailpit VM has two network addresses — a host-only address
(`192.168.56.13`) and a LAN/bridged address (`192.168.50.104`). QED tests running on
Windows should always use the host-only address. The LAN address is used by the
application backend when running locally on Windows (since the Windows host cannot
reach the host-only network from a JVM process in the same way). See
`qed-mailpit-infrastructure.md` for the full address reference table.

---

## API Reference

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api/v1/messages` | List all captured messages |
| `GET` | `/api/v1/message/{id}` | Get full message body by ID |
| `DELETE` | `/api/v1/message/{id}` | Delete a single message by ID |
| `DELETE` | `/api/v1/messages` | Delete all messages |

Full Mailpit API docs: https://mailpit.axllent.org/docs/api-v1/

---

## Test Patterns

### Setup — clear inbox before each email test

Add `MailpitHelper.clearInbox()` to `@BeforeMethod` for any test class that sends
email. This handles leftover messages from a previous interrupted run:

```kotlin
@BeforeMethod
fun setup() {
    TestHelpers.clearRateLimits(this)
    MailpitHelper.clearInbox()
}
```

### Pattern: verify email arrival and extract a link

The general pattern for any email-triggered flow:

1. Trigger the action that sends the email (registration, password reset, etc.)
2. Call `waitForEmail()` — it polls until the email arrives or times out
3. Assert on subject, recipient, or body content if needed
4. Extract the token from the link using `extractLink()`
5. Call the backend API endpoint directly with the token — do not follow the link URL
   directly, as it typically points to the frontend (e.g.
   `http://localhost:5173/verify-email?token=xxx`), not the backend API

```kotlin
// 1. Trigger action
rest.send(kTorURLPath.API_AUTH_REGISTER, registrationReq, statusCodeLst = listOf(200, 201))

// 2. Wait for email
val email = MailpitHelper.waitForEmail(
    toAddress = "newuser@example.com",
    subjectContains = "Verify"
)

// 3. Assert on content (optional but recommended)
verify("check verification email subject") {
    expect(email.getString("Subject")).to.contain("Verify")
}

// 4. Extract token from link
val link = MailpitHelper.extractLink(email.getString("Text"), "/verify-email?token=")
val token = URI.create(link).query
    .split("&")
    .first { it.startsWith("token=") }
    .substringAfter("token=")

// 5. Call backend API directly with token
val confirmation = rest.sendUntyped(
    kTorURLPath.AUTH_VERIFY_EMAIL,
    parameterPairs = listOf(URLParameter("token", token)),
    statusCodeLst = listOf(200)
)
verify("check email verification succeeded") {
    expect(confirmation.get("success")).to.equal(true)
}
```

**Why call the API directly rather than following the link?**

Email links point to the frontend URL. The frontend parses the token and calls the
backend API. The test bypasses the frontend and calls the backend directly — this is
correct because the test is verifying backend behaviour, not frontend routing.

### Pattern: password reset via email

```kotlin
// Trigger reset
rest.send<PasswordResetResponse>(
    kTorURLPath.API_AUTH_PASSWORD_RESET_REQUEST,
    PasswordResetRequest(email),
    statusCodeLst = listOf(200)
)

// Retrieve email from Mailpit
val resetEmail = MailpitHelper.waitForEmail(
    toAddress = email,
    subjectContains = "Password Reset"
)

// Optionally assert on content
verify("check password reset email subject") {
    expect(resetEmail.getString("Subject")).to.contain("Password Reset")
}

// Extract token from reset link
val resetLink = MailpitHelper.extractLink(resetEmail.getString("Text"), "/reset-password?token=")
val resetToken = URI.create(resetLink).query
    .split("&")
    .first { it.startsWith("token=") }
    .substringAfter("token=")

// Confirm the reset
val newPassword = randomChars(10, CharSet.ALPHANUMERIC)
val resetConfirmation = rest.send<PasswordResetResponse>(
    kTorURLPath.API_AUTH_PASSWORD_RESET_CONFIRM,
    PasswordResetConfirm(resetToken, newPassword)
)
verify("check password reset succeeded") {
    expect(resetConfirmation.success).to.equal(true)
}
```

---

## Shared inbox considerations

All environments (staging, preprod, local dev) share one Mailpit inbox. This is by
design — `vm-mailpit` is a single shared infrastructure component.

`waitForEmail()` handles this correctly — it filters by recipient address and deletes
by message ID after retrieval. Two concurrent test runs using different email addresses
will not interfere with each other.

The only edge case is two concurrent runs using the **same** email address. This should
not happen in practice if tests generate unique email addresses (e.g. using a random
suffix).

---

## Replacing obsolete test routes

Before Mailpit was available, test-only backend routes existed solely to retrieve email
tokens directly from the database. These are now redundant and should be removed once
Mailpit-based tests are confirmed green:

| Old approach | Replacement |
|---|---|
| `DEV_CONFIRM_EMAIL` test route — retrieves verification token from DB | `MailpitHelper.waitForEmail()` + extract token from email |
| `DEV_PWRESET_EMAIL` test route — retrieves reset token from DB | `MailpitHelper.waitForEmail()` + extract token from email |

Remove the corresponding entries from `kTorURLPath` and the handler methods from the
backend testing routes file after the replacement tests pass.

---

## Troubleshooting

**`waitForEmail` times out:**
- Check the Mailpit web UI manually at `http://192.168.56.13:8025` — did the email
  arrive at all?
- If the inbox is empty, the problem is in the application SMTP configuration, not QED.
  Check the application logs on the relevant environment.
- If the email arrived but with the wrong subject or recipient: inspect `Subject` and
  `To` in the Mailpit UI and update the `waitForEmail` parameters.
- If the email arrived but was deleted by a concurrent test: check for duplicate
  recipient addresses across parallel test runs.

**`extractLink` throws AssertionError:**
- The plain-text body may format the link differently. Inspect `email.getString("Text")`
  and adjust the `pathFragment` or splitting logic in `extractLink` accordingly.
- Try `email.getString("HTML")` if the plain-text body is minimal.

**Mailpit web UI unreachable (`192.168.56.13:8025`):**
- Verify vm-mailpit is running: SSH to `192.168.56.13` and run
  `sudo systemctl status mailpit`
- Confirm ports are listening: `ss -tlnp | grep -E '1025|8025'`
- Do NOT use `192.168.50.104` from the Windows host — use the host-only IP

**`clearInbox()` has no visible effect:**
- Confirm Mailpit is reachable: `Test-NetConnection -ComputerName 192.168.56.13 -Port 8025`
- The DELETE request returns silently even if Mailpit is unreachable — check
  connectivity before assuming the inbox was cleared