Closes #

## What & why

<!-- What changed and why. Link ADRs if a decision was made. -->

## How tested

<!-- Unit / Robolectric / screenshot / instrumented tests added or changed, and what you ran. -->

## Screenshots

<!-- UI changes: Roborazzi images or `adb exec-out screencap -p`. Delete if not applicable. -->

## Device test steps

<!-- Required if labelled `needs-device-test`: exact steps someone can follow on a phone. -->

## Risks

<!-- Detection false positives, behaviour, policy, battery or migration risks, and how they are mitigated. -->

## Checklist

- [ ] `./gradlew spotlessApply ciCheck` passes locally
- [ ] Tests cover the acceptance criteria (no tests skipped, no thresholds lowered, no baselines or `@Suppress`)
- [ ] Detection rules (if any) are JSON data with sanitised positive **and** negative fixtures; no view IDs in Kotlin
- [ ] Nothing read from the screen is logged or stored beyond what detection needs
- [ ] User-facing strings are in resources, in English and Hindi
- [ ] Screenshot tests added/updated for UI changes (light/dark, phone/tablet)
- [ ] Accessibility: content descriptions, 48 dp targets, TalkBack order, 200 % font scale
- [ ] No `INTERNET` permission; no new exported components or mutable `PendingIntent`s without reason
- [ ] Docs/ADR updated if a decision was made
