# QED Mailpit Infrastructure Setup

## Overview

This guide covers setting up the dedicated `vm-mailpit` VM that runs the Mailpit SMTP
capture server for use with QED test suites. It is infrastructure — not
application-specific — and applies to any SUT that QED tests against email flows.

For how to use Mailpit in QED tests, see `qed-mailpit-guide.md`.
For application-specific SMTP configuration, see the relevant SUT's CI/CD guide.

---

## What is vm-mailpit?

A lightweight Ubuntu VM running a single Mailpit binary. It receives outbound email
from all test environments (staging, preprod, local dev) over SMTP, holds the messages,
and exposes them via a web UI and REST API that QED tests query.

**Specs — Mailpit is a single Go binary, very little is needed:**

| Setting | Value |
|---------|-------|
| Name | vm-mailpit |
| RAM | 512 MB |
| CPU | 1 |
| Disk | 10 GB |
| OS | Ubuntu 24.04 LTS Server (minimized) |

---

## Network design

vm-mailpit has three network adapters:

| Adapter        | Type      | IP                     | Used by                                                   |
|----------------|-----------|------------------------|-----------------------------------------------------------|
| NIC 1 (enp0s3) | NAT       | DHCP                   | Internet access (binary download, updates)                |
| NIC 2 (enp0s8) | Host-Only | <mailpit-host-only-ip> | SMTP from other VMs, Windows host browser, QED tests      |
| NIC 3 (enp0s9) | Bridged   | <mailpit-lan-ip>         | SMTP from local dev backend on Windows, other LAN devices |

**Address reference by caller:**

| Caller                              | Address to use                | Reason                                             |
|-------------------------------------|-------------------------------|----------------------------------------------------|
| Other VMs (SMTP)                    | `<mailpit-host-only-ip>:1025` | Host-only inter-VM network                         |
| Windows host browser / QED tests    | `<mailpit-host-only-ip>:8025` | Host-only (bridged VMs unreachable from own host)  |
| Local dev backend on Windows (SMTP) | `<mailpit-lan-ip>:1025`       | Bridged LAN — Windows cannot reach host-only to VM |
| Other LAN devices                   | `<mailpit-lan-ip>:8025`       | Bridged LAN                                        |

**Important:** VirtualBox bridged VMs cannot communicate with their own host machine
through the bridged adapter. Always use the host-only IP (`<mailpit-host-only-ip>`) from the
Windows host machine. `ping <mailpit-lan-ip>` from the Windows host will not respond —
this is expected and does not indicate a problem.

---

## Step 1: Create the VM

Create a new VM in VirtualBox with the specs above. Install Ubuntu 24.04 LTS Server
(minimized) and enable OpenSSH during install.

If you see a mirror configuration error during install, reboot and select
**"Skip mirror check"** when it appears, or install without network and run
`sudo apt update && sudo apt upgrade` after install.

---

## Step 2: Configure network adapters

**vm-mailpit must be powered off before changing adapters.**

In VirtualBox Settings → Network:
- **Adapter 1:** NAT
- **Adapter 2:** Host-Only Adapter
- **Adapter 3:** Bridged Adapter

Add the bridged adapter and set promiscuous mode via VBoxManage (PowerShell):

```powershell
& "C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" modifyvm vm-mailpit --nic3 bridged --bridgeadapter3 "Realtek Gaming 2.5GbE Family Controller"
& "C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" modifyvm vm-mailpit --nicpromisc3 allow-all
```

Replace the adapter name with the Ethernet adapter connected to your LAN.
Use `VBoxManage list bridgedifs` to see available adapters.

Verify promiscuous mode was set (VM must be running):

```powershell
& "C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" showvminfo vm-mailpit | findstr "NIC 3"
```

Should show `Promisc Policy: allow-all`. If it shows `deny`, power off and rerun the
`modifyvm` command.

---

## Step 3: Configure static IPs

Boot vm-mailpit and SSH in. Check adapter names and edit the netplan file:

```bash
ip link show
sudo nano /etc/netplan/50-cloud-init.yaml
```

```yaml
network:
  version: 2
  ethernets:
    enp0s3:
      dhcp4: true
    enp0s8:
      addresses:
        - <mailpit-host-only-ip>/24
    enp0s9:
      addresses:
        - <mailpit-lan-ip>/24
```

**Important:** Do NOT add a default gateway to `enp0s9` — the NAT adapter (`enp0s3`)
handles the default route. Adding a second gateway causes routing conflicts.

Fix permissions and apply:

```bash
sudo chmod 600 /etc/netplan/50-cloud-init.yaml
sudo netplan apply
```

Verify both IPs are assigned:

```bash
ip addr show enp0s8    # should show <mailpit-host-only-ip>
ip addr show enp0s9    # should show <mailpit-lan-ip>
```

Verify from Windows (host-only only — bridged will not respond from own host):

```powershell
ping <mailpit-host-only-ip>
```

---

## Step 4: Install Mailpit

```bash
curl -sL https://github.com/axllent/mailpit/releases/latest/download/mailpit-linux-amd64.tar.gz \
  | sudo tar -xz -C /usr/local/bin mailpit

mailpit --version
```

If the URL has changed, check https://github.com/axllent/mailpit/releases for the
current `linux-amd64` asset name.

---

## Step 5: Create the systemd service

```bash
sudo tee /etc/systemd/system/mailpit.service > /dev/null << 'EOF'
[Unit]
Description=Mailpit SMTP capture server
After=network.target

[Service]
ExecStart=/usr/local/bin/mailpit \
  --smtp 0.0.0.0:1025 \
  --listen 0.0.0.0:8025
Restart=on-failure
User=nobody
Group=nogroup

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable mailpit
sudo systemctl start mailpit
sudo systemctl status mailpit
```

You should see `Active: active (running)`.

Verify both ports are listening:

```bash
ss -tlnp | grep -E '1025|8025'
```

---

## Step 6: Open firewall ports

```bash
sudo apt install -y ufw
sudo ufw allow 22/tcp comment "SSH"
sudo ufw allow 1025/tcp comment "Mailpit SMTP"
sudo ufw allow 8025/tcp comment "Mailpit Web UI"
sudo ufw enable
```

---

## Step 7: Verify the web UI

From the Windows host, open a browser:

```
http://<mailpit-host-only-ip>:8025
```

You should see the Mailpit inbox (empty at this point).

---

## VM prerequisites verification script

Run on vm-mailpit after any provisioning or migration:

```bash
echo "=== Network ==="
ip addr show enp0s8 | grep "inet " || echo "enp0s8: NO IP"
ip addr show enp0s9 | grep "inet " || echo "enp0s9: NO IP"

echo "=== Mailpit ==="
systemctl is-active mailpit && echo "mailpit: running" || echo "mailpit: NOT RUNNING"
ss -tlnp | grep 1025 && echo "SMTP port 1025: open" || echo "SMTP port 1025: NOT LISTENING"
ss -tlnp | grep 8025 && echo "Web UI port 8025: open" || echo "Web UI port 8025: NOT LISTENING"

echo "=== Firewall ==="
sudo ufw status | grep -E '1025|8025' || echo "ports may not be open in ufw"
```

---

## Migration notes

When moving vm-mailpit to a new host machine, use direct VM folder copy rather than
OVA export — OVA export may lose installed packages and configuration.

After any migration:
- Verify the Mailpit binary still exists: `which mailpit`
- Verify the systemd service is still present: `sudo systemctl status mailpit`
- Re-verify network IPs (Step 3)
- Re-run the verification script above

Mailpit holds no persistent state that needs migrating — the inbox is in-memory and
intentionally ephemeral.

---

## Troubleshooting

**Mailpit service not starting:**
```bash
sudo journalctl -u mailpit -n 30 --no-pager
```
Common cause: port 1025 or 8025 already in use.
Check: `ss -tlnp | grep -E '1025|8025'`

**Web UI unreachable from Windows (`<mailpit-host-only-ip>:8025`):**
- Confirm Mailpit is running and listening: `ss -tlnp | grep 8025`
- Check firewall: `sudo ufw status`
- Try SSH connectivity first: `ssh admin@<mailpit-host-only-ip>`
- Do NOT troubleshoot using `<mailpit-lan-ip>` from the Windows host — it will never
  respond from the host machine (VirtualBox limitation, not a problem)

**`ping <mailpit-lan-ip>` from Windows doesn't respond:**
- Expected — VirtualBox bridged VMs cannot communicate with their own host through
  the bridged adapter. Use `<mailpit-host-only-ip>` from Windows.
- To confirm the bridged adapter is working, ping from another VM or another LAN device.

**NIC 3 promiscuous mode shows `deny` after reboot:**
- Power off vm-mailpit and rerun the `modifyvm --nicpromisc3 allow-all` command.
  VirtualBox does not always persist this setting correctly.

---

## Setup checklist

- [ ] VM created with 512 MB RAM, 1 CPU, 10 GB disk
- [ ] Ubuntu 24.04 LTS Server installed, OpenSSH enabled
- [ ] Three network adapters configured: NAT, host-only, bridged
- [ ] NIC 3 promiscuous mode set to `allow-all`
- [ ] Static IPs assigned: `<mailpit-host-only-ip>` (host-only), `<mailpit-lan-ip>` (LAN)
- [ ] Mailpit binary installed at `/usr/local/bin/mailpit`
- [ ] Mailpit systemd service enabled and running
- [ ] Ports 22, 1025, and 8025 open in ufw
- [ ] Web UI accessible at `http://<mailpit-host-only-ip>:8025` from Windows host
- [ ] Application SMTP configured to point at Mailpit (see SUT CI/CD guide)