# Security policy

## Reporting a vulnerability

Please **do not open a public issue** for security problems. Use GitHub's private reporting instead:
**Security → Report a vulnerability** on this repository. You should get a reply within 7 days.

Useful things to include: the Unreel version (Settings → About), Android version and device, steps to
reproduce, and the impact you expect.

## Scope

In scope:

- Anything that makes Unreel send data off the device. Unreel has no `INTERNET` permission by design.
- Anything that makes Unreel store or log on-screen content beyond what detection needs (which surface is
  showing and for how long).
- Ways for *another app* to change Unreel's settings, end a commitment lock, or read Unreel's data
  (exported components, intents, `PendingIntent`s, content providers).

Out of scope, by design:

- The user turning off Unreel's accessibility service, force-stopping or uninstalling Unreel. Unreel adds
  honest friction; it does not pretend Android cannot be escaped.
- A target app changing its UI so that detection stops working. Report that with the **Detection problem**
  issue form instead.

## Supported versions

Only the latest release receives fixes.
