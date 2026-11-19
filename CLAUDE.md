# Unreel: Claude Code guide

Android app (Kotlin + Jetpack Compose, minSdk 29, target 37). Blocks only the short-form feed surface
(Instagram Reels, YouTube Shorts) via an AccessibilityService, leaving the rest of those apps working.
On-device only. **Source of truth: `docs/PROJECT_BRIEF.md`.** Architecture: `docs/architecture.md`.
Decisions: `docs/adr/`.

## Start / resume a session
1. Read this file and `docs/PROJECT_BRIEF.md`.
2. `gh pr list --state open` and `gh issue list --milestone "<current>" --state open`.
3. Finish open PRs first, then continue the loop. GitHub issues track progress.

## Commands
- `./gradlew spotlessApply ciCheck`: local gate, identical to CI (format, detekt, lint, tests, screenshots,
  Kover, module graph, no-INTERNET check)
- `./gradlew assembleDebug` / `installDebug`
- `./gradlew recordRoborazziDebug`: re-record screenshot goldens after an intended UI change
- `./gradlew :core:detection:test`, `./gradlew :<module>:testDebugUnitTest`
- `python3 scripts/module-graph.py`: regenerate the module graph after changing module dependencies
- `./gradlew connectedDebugAndroidTest`: on device

## Architecture rules
- UI (Compose + ViewModel) → domain (use cases only where useful) → data (repositories) → sources. UDF: one
  `StateFlow<UiState>` per ViewModel, actions are functions, one-off effects via `Channel`.
- `:core:detection` is pure Kotlin: `NodeSnapshot` + rules → `DetectedSurface` (+ swipe-to-next). Rules are
  JSON data; **never hardcode or guess view IDs**. Every rule has sanitised positive *and* negative fixtures.
- The accessibility service is thin: filter + debounce events, build a capped snapshot off the main thread,
  call injected classes, run actions. No business logic in services, receivers or composables.
- False positives (blocking DMs or normal videos) are the worst bug. When unsure, don't match.
- Inject dispatchers and `Clock`; prefer fakes over mocks. Handle midnight, time-zone changes, process death.
- Features never depend on each other. Build config lives in `build-logic/` convention plugins.
- Verify platform APIs on developer.android.com before use; record decisions as ADRs.

## The loop (brief §5)
Pick issue → assign + comment plan → `gh issue develop <n> --checkout --name <type>/<n>-<slug>` → tests
first, small Conventional Commits referencing `#<n>` → `./gradlew spotlessApply ciCheck` → `gh pr create`
(title `<type>(<scope>): <summary> (#<n>)`, template, same labels + milestone) → review:
`/code-review high --comment <pr>`, then the `android-reviewer` subagent (size:S docs/chore: subagent only)
→ fix BLOCKER/MAJOR, reply to each finding with the SHA → re-review if a BLOCKER (max 2 rounds) →
`gh pr checks <pr> --watch` → `gh pr merge <pr> --squash --delete-branch`.
End of milestone: tag `v0.<m>.0`, update the README roadmap, report to the user with device tests.

## Definition of done
Acceptance criteria met with tests; `ciCheck` green; screenshots for UI (light/dark, phone/tablet); strings
in English + Hindi; accessible (content descriptions, 48 dp, 200 % font); reviewed; merged.

## Don'ts
- No `INTERNET` permission, analytics, crash SDKs, accounts or backend. Never strip a dependency's INTERNET
  with `tools:node="remove"`; stop and ask.
- Never log or store on-screen text, usernames, captions or messages. Fixtures must be sanitised.
- No `SYSTEM_ALERT_WINDOW` (use `TYPE_ACCESSIBILITY_OVERLAY`), no polling, no foreground service without an ADR.
- Never claim the commitment lock is unbreakable.
- Never get green by deleting/skipping tests, lowering thresholds, lint/detekt baselines or `@Suppress`.
- No direct pushes to `main`, no force-pushes, never create or commit a keystore.
- No `Co-Authored-By` trailers or "Generated with Claude Code" lines in commits or PRs.
- Ask the user before: any Play-policy or privacy trade-off, a dependency that adds INTERNET, release
  signing, or after 3 identical CI failures.
