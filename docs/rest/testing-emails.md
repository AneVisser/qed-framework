# QED Mailpit Integration Guide

## Overview

Mailpit is a lightweight SMTP capture server with a web UI and REST API. Instead of
delivering emails to real inboxes, it holds them so they can be inspected manually or
queried by automated tests.

QED uses Mailpit to verify outbound email flows end-to-end — asserting on subject,
recipient, and link content rather than just checking that an API returns HTTP 200.

**Mailpit is not part of the application under test.** It is a QED infrastructure
component. For VM setup and installation, see `qed-mailpit-infrastructure.md`.

---

## Configuration

### Framework config file

Mailpit connection details live in the QED framework's own config file, not in any
SUT config. This means any SUT tested with QED automatically has access to Mailpit
without any per-project configuration.

**Location:** `src/main/resources/qed-framework.json` (in the QED framework project)

```json
{
  "environments": {
    "dev": {
      "mailpitBaseUrl": "http://<mailpit-lan-ip>:8025"
    },
    "stag": {
      "mailpitBaseUrl": "http://<mailpit-host-only-ip>>:8025"
    },
    "preprod": {
      "mailpitBaseUrl": "http://<mailpit-host-only-ip>>:8025"
    }
  }
}
```

This file is committed to the QED framework repository. Because the Mailpit VM has
fixed IP addresses that are the same for everyone on the team, there is no per-machine
setup needed.

The environment is selected using the same `env.name` system property used by SUT
configs (`dev` by default). If no entry exists for the current environment, or if
the config file is missing, `MailpitHelper` falls back to `http://<mailpit-host-only-ip>>:8025`
with a warning.

**Why two different addresses for dev vs staging/preprod?**

The Mailpit VM has a host-only adapter (`<mailpit-host-only-ip>>`) and a LAN/bridged adapter
(`<mailpit-lan-ip>`). VirtualBox bridged VMs cannot communicate with their own host
machine through the bridged adapter, so:
- QED tests running on Windows use the host-only address for staging and preprod
- The local dev backend (a JVM process on Windows) uses the LAN address for SMTP
- QED tests in `dev` environment also use the LAN address for consistency with
  how the dev backend connects

See `qed-mailpit-infrastructure.md` for the full address reference.

### QedFrameworkSettings

`QedFrameworkSettings` is a singleton in the QED framework that loads
`qed-framework.json` from the classpath and exposes the values:

```kotlin
// Usage in MailpitHelper or any other framework code:
val baseUrl = QedFrameworkSettings.mailpitBaseUrl
```

It is loaded lazily on first access and shared across all tests in the suite.

---

## API Reference

| Method   | Endpoint               | Purpose                       |
|----------|------------------------|-------------------------------|
| `GET`    | `/api/v1/messages`     | List all captured messages    |
| `GET`    | `/api/v1/message/{id}` | Get full message body by ID   |
| `DELETE` | `/api/v1/message/{id}` | Delete a single message by ID |
| `DELETE` | `/api/v1/messages`     | Delete all messages           |

Full Mailpit API docs: https://mailpit.axllent.org/docs/api-v1/

---

## Test Patterns

### Setup — clear inbox once at suite start

Add `MailpitHelper.clearInbox()` to `@BeforeSuite` — not `@BeforeMethod`. Clearing
before every method is too aggressive: if two email-related tests run in sequence,
the `@BeforeMethod` clear would wipe the inbox before the first test's email has been
consumed, which could mask failures.

The per-message delete inside `waitForEmail()` keeps the inbox clean during the suite.
The `@BeforeSuite` clear only handles leftover messages from a previous interrupted run.

```kotlin
@BeforeSuite
fun clearMailpitInbox() {
    MailpitHelper.clearInbox()
}

@BeforeMethod
fun setup() {
    TestHelpers.clearRateLimits(this)   // rate limits cleared per method as before
    // no inbox clear here
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

## Pending: CI runner configuration

When QED is wired into the build pipeline, the current framework config approach needs
updating. The `env.name` property selects which SUT environment is being tested, not
where QED is running — but the Mailpit URL depends on where QED is running, not which
SUT environment is targeted.

**The correct model:**

| QED running on          | Mailpit URL                                                          |
|-------------------------|----------------------------------------------------------------------|
| Windows dev machine     | `http://<mailpit-lan-ip>:8025` (LAN — always, regardless of SUT env) |
| CI runner on vm-staging | `http://<mailpit-host-only-ip>>:8025` (host-only — always)           |

**Required changes when implementing CI pipeline:**

1. **Move config file** from `src/main/resources/qed-framework.json` (classpath,
   committed to repo) to `~/.qed/qed-framework.json` (user home, per-machine)

2. **Flatten `QedFrameworkConfig.kt`** — remove the `environments` map, replace with
   a single top-level value:
   ```kotlin
   data class QedFrameworkConfig(
       val mailpitBaseUrl: String? = null
   )
   ```

3. **Update `QedFrameworkSettings.kt`** — load from user home instead of classpath,
   remove the `env.name` environment selection:
   ```kotlin
   private val CONFIG_PATH =
       "${System.getProperty("user.home")}/.qed/qed-framework.json"
   
   private fun load(): String? {
       val file = File(CONFIG_PATH)
       if (!file.exists()) {
           println("⚠️  QED framework config not found at $CONFIG_PATH — using defaults")
           return null
       }
       return try {
           QEDJson.fromJson<QedFrameworkConfig>(file.readText())?.mailpitBaseUrl
       } catch (e: Exception) {
           println("⚠️  Failed to load QED framework config: ${e.message} — using defaults")
           null
       }
   }
   ```

4. **Create the config file on vm-staging** (as part of runner setup):
   ```bash
   mkdir -p /home/runner/.qed
   cat > /home/runner/.qed/qed-framework.json << 'EOF'
   {
     "mailpitBaseUrl": "http://<mailpit-host-only-ip>>:8025"
   }
   EOF
   ```

5. **Windows dev machine config** (`~/.qed/qed-framework.json`):
   ```json
   {
     "mailpitBaseUrl": "http://<mailpit-lan-ip>:8025"
   }
   ```

Until this is implemented, the URL in `src/main/resources/qed-framework.json` should
be set to `http://<mailpit-lan-ip>:8025` for running tests from the dev machine.

---

## Replacing obsolete test routes

Before Mailpit was available, test-only backend routes existed solely to retrieve email
tokens directly from the database. These are now redundant and should be removed once
Mailpit-based tests are confirmed green:

| Old approach                                                          | Replacement                                               |
|-----------------------------------------------------------------------|-----------------------------------------------------------|
| `DEV_CONFIRM_EMAIL` test route — retrieves verification token from DB | `MailpitHelper.waitForEmail()` + extract token from email |
| `DEV_PWRESET_EMAIL` test route — retrieves reset token from DB        | `MailpitHelper.waitForEmail()` + extract token from email |

Remove the corresponding entries from `kTorURLPath` and the handler methods from the
backend testing routes file after the replacement tests pass.

---

## Troubleshooting

**`waitForEmail` times out:**
- Check the Mailpit web UI manually — did the email arrive at all?
    - From dev: `http://<mailpit-lan-ip>:8025`
    - From Windows host (staging/preprod): `http://<mailpit-host-only-ip>>:8025`
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

**Mailpit web UI unreachable:**
- Verify vm-mailpit is running: SSH to `<mailpit-host-only-ip>>` and run
  `sudo systemctl status mailpit`
- Confirm ports are listening: `ss -tlnp | grep -E '1025|8025'`
- Do NOT use `<mailpit-lan-ip>` from the Windows host — bridged VMs are unreachable
  from their own host machine. Use `<mailpit-host-only-ip>>` instead.

**Wrong Mailpit URL being used:**
- Check which environment `env.name` is set to: `System.getProperty("env.name")`
- Verify `qed-framework.json` has the correct entry for that environment
- Check `QedFrameworkSettings` logs at startup for any warning about missing config

**`clearInbox()` has no visible effect:**
- Confirm Mailpit is reachable using `Test-NetConnection` before assuming the inbox
  was cleared — the DELETE request returns silently even if unreachable