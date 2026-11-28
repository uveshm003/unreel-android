# 6. Open the system battery optimisation list from onboarding

- **Status** Proposed
- **Date** 2026-11-24

## Context

The optional device guide helps users inspect restrictions on background work. Device manufacturers also provide controls for autostart and sleeping apps. The Android exemption flag describes only the system battery optimisation setting.

## Decision

Use `PowerManager.isIgnoringBatteryOptimizations(packageName)` to display Unreel’s current status. Refresh it whenever the guide resumes. The settings button sends `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`, which opens the system list and lets the user choose an app. The guide remains optional in either state.

Android also offers a direct exemption request for qualifying apps. The system list meets the onboarding requirement without adding a permission or requiring an exemption for setup. [Android guidance on Doze and App Standby, checked 2026-11-24](https://developer.android.com/training/monitoring-device-state/doze-standby#support_for_other_use_cases)

## Consequences

Selecting Unreel in the list takes an extra step. Manufacturer-specific advice describes where to look for additional settings. A browser opens the corresponding guide only when requested. If either action is unavailable, the user can still finish onboarding.
