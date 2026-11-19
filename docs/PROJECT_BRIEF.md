# Build "Unreel": a native Android app that blocks Reels & Shorts, not whole apps

You are the only engineer on this project, working on your own in Claude Code. You will:

1. Create the Android project from scratch.
2. Put it on GitHub and set up everything a professional repo needs (CI, protection, templates, labels, milestones, backlog).
3. Build the app through a strict loop: **Open Issue → Raise PR → Review Code → Merge PR → Repeat**, milestone by milestone, until `v1.0.0`.

Read this whole brief before doing anything. In the first commit, save it unchanged as `docs/PROJECT_BRIEF.md`. It is the source of truth for every future session.

---

## 0. Project settings (I may override these)

| Setting | Value |
|---|---|
| App name | **Unreel** (working name) |
| Repo | `unreel-android`, **public**, under the account `gh` is logged in as (`gh api user -q .login`) |
| applicationId / namespace root | `io.github.<login>.unreel`, with the login lowercased and any character that isn't valid in a package name replaced by `_` |
| License | Apache-2.0 |
| minSdk | 29 |
| compileSdk / targetSdk | Latest stable API level. Check this at setup time (it was Android 17 / API 37 when this brief was written) |
| UI languages | English + Hindi |

---

## 1. Product

### Problem
Short-form feeds (Instagram Reels, YouTube Shorts) are built for endless scrolling. App-level blockers fail because people need the rest of those apps (DMs, search, normal videos, uploads).

### Solution
Unreel blocks **only the short-form feed surface** inside apps and leaves everything else working. It uses an AccessibilityService to recognise when the user is in a Reels/Shorts feed and steps in.

### Principles
- **Block the feed, not the app.** False positives (blocking DMs or normal videos) are the worst bug.
- **On-device only.** The merged manifest has **no `INTERNET` permission**. No analytics, crash SDKs, accounts or backend. Nothing read from the screen is ever stored or logged except the minimum needed for detection.
- **Honest friction, not a jail.** Android can't stop a determined user from turning off an accessibility service, so never pretend otherwise.
- **Battery-friendly.** Handle accessibility events cheaply. No polling, and no foreground service unless an ADR justifies it.

### v1 scope
Supported surfaces: **Instagram Reels** and **YouTube Shorts**. (Facebook Reels and Snapchat Spotlight are stretch goals, see M5.)

### Features
1. **Feed detection engine.** Detects Reels/Shorts surfaces (tab, viewer, a reel opened from feed/DM/link) from accessibility events. Rules are data, not code (see §2).
2. **Per-app protection level.**
   - **Off**
   - **Pause:** a full-screen overlay with a 10-second breathing countdown, then "Go back" (primary) or "Allow 5 min" (secondary)
   - **Budget:** N minutes per day; when used up, behaves like Hard block
   - **Hard block:** immediately bounce out of the feed
3. **"Allow one, block the scroll"** (per app, default ON). A single reel opened from a DM or link plays, but swiping to the next reel triggers the protection level.
4. **Schedules.** Raise the protection level during time windows (e.g. Hard block 22:00–07:00 on all days).
5. **Commitment lock.** Lock settings for 1 / 4 / 12 / 24 h. While locked, protection can be raised but not lowered. If the service gets disabled during a lock, record it in insights and post a notification. Never claim it is unbreakable.
6. **Insights.** Time spent in short-form feeds (today / week), interventions, "scrolls avoided", streak of days under budget, and an estimate of time reclaimed (clearly labelled as an estimate). Charts.
7. **Surfaces.** A Glance home-screen widget (today's minutes + streak). A Quick Settings tile ("Pause protection 15 min", behind a friction confirm).
8. **Onboarding & permissions.**
   - Prominent disclosure + explicit consent screen **before** sending the user to Accessibility settings (Play policy)
   - Deep link to settings, and detect when the service is enabled
   - Android 13+ **restricted settings** guide for sideloaded APKs (App info → ⋮ → Allow restricted settings)
   - Battery-optimisation exemption request + OEM autostart guidance (Xiaomi/Redmi/POCO, Oppo/Realme, Vivo, OnePlus, Samsung), linking to dontkillmyapp.com
9. **Service health.** Checked when the app opens and by a daily WorkManager job. Post a notification if the service is off or was killed.
10. **Settings.** Theme (dynamic colour, light/dark), language, JSON data export via the Storage Access Framework, reset all data, "Report a detection problem". The report button opens a pre-filled GitHub new-issue URL with `ACTION_VIEW` (this does not need `INTERNET`).

### Non-goals
No root, no VPN/DNS blocking, no device admin, no iOS, no backend, no accounts, no ads.

---

## 2. Technical design (Unreel-specific)

### Detection engine: `:core:detection` (pure Kotlin/JVM, no Android deps)
- Input: a lightweight `NodeSnapshot` tree (viewIdResourceName, className, contentDescription, a few booleans, bounds, children), plus the event type, package name and app versionCode. Avoid reading on-screen `text` unless a rule truly needs it.
- Output: `DetectedSurface` (e.g. `ReelsTab`, `ReelsViewer`, `ShortsPlayer`, `None`) plus whether this event was a **swipe to the next item**.
- **Rules are data.** Versioned JSON in `assets/rules/<package>.json`, with optional versionCode ranges and matchers on id / class / content-description / structure. Never hardcode view IDs in Kotlin.
- **Never guess view IDs.** Get them from real UI dumps (`adb shell uiautomator dump`, plus a debug-only accessibility event logger) taken on a real device with the real apps installed.
- **Fixtures.** Save **sanitised** snapshots (strip usernames, captions, comments, any personal text) to `core/detection/src/test/resources/fixtures/<app>/<versionCode>/`. Every rule has positive fixtures and **negative** fixtures (home feed, stories, DMs, explore/search grid, a normal YouTube video, YouTube home). Add a script `scripts/capture-fixture.sh` that dumps from a connected device and runs the sanitiser.
- Fallback heuristics for unknown app versions, kept conservative (prefer missing a feed to blocking a DM).

### Accessibility service: `:service:accessibility`
- `isAccessibilityTool="false"`, `canRetrieveWindowContent="true"`, `packageNames` limited to supported apps, and the minimum event types needed (window state changed, content changed, view scrolled).
- Debounce content-changed events (around 150–300 ms, tuned by measurement). Cap traversal depth and node count. Never do heavy work on the main thread. In debug builds, log p50/p95 handling time.
- The service is thin: it builds snapshots, calls injected classes and runs actions. No business logic lives in the service.
- **Actions.** Hard block uses `GLOBAL_ACTION_BACK` (verify per app that it actually leaves the viewer; otherwise go home) plus a brief overlay message. Pause uses an overlay window of type `TYPE_ACCESSIBILITY_OVERLAY` (no `SYSTEM_ALERT_WINDOW`) rendered with Compose. Give the ComposeView a proper Lifecycle/SavedState owner inside the service.
- **Time tracking.** Feed sessions (start/end) go to Room. A WorkManager job aggregates them daily. Handle local midnight, time zone changes and process death.

### Module layout
```
app
build-logic/            (convention plugins)
core/common  core/model  core/data  core/database  core/datastore
core/designsystem  core/ui  core/testing  core/detection
service/accessibility
feature/onboarding  feature/home  feature/apps  feature/schedule
feature/insights  feature/settings
widget                  (Glance + QS tile)
baselineprofile  benchmark
```

---

## 3. Engineering standards (apply to every PR)

- **Kotlin + Jetpack Compose only.** No XML layouts except where the platform requires XML (manifest, accessibility config, etc.).
- **Material 3**: dynamic colour, light/dark, edge-to-edge, predictive back. **Adaptive layouts** using window size classes (Android 17 removes the opt-out from resizability on large screens), so check phone, foldable and tablet.
- **Architecture**: UI (Compose + ViewModel) → domain (use cases only where they add value) → data (repositories) → sources. **Unidirectional data flow**: each ViewModel exposes one `StateFlow<UiState>`, user actions are functions, and one-off effects go through a `Channel`. No business logic in composables, services or receivers.
- **Stack**: Hilt, Coroutines/Flow (inject dispatchers), Room (exported schemas + migration tests), DataStore, WorkManager, type-safe Navigation Compose, kotlinx.serialization, Vico for charts.
- **Versions**: Gradle version catalog + convention plugins in `build-logic`. **Look up the latest stable versions at setup time** (AGP, Kotlin, Compose BOM, Hilt, Room, etc.) and don't rely on memory. No alpha/beta libraries unless an ADR justifies them.
- **Verify every platform API against current developer.android.com docs before using it.** Record non-obvious decisions as ADRs in `docs/adr/NNNN-title.md`.
- **Quality gates**: Spotless (ktlint), Detekt, Android Lint (errors fail the build), unit tests (JUnit4, Turbine, coroutines-test, fakes preferred over mocks), Robolectric + **Roborazzi** screenshot tests for every screen (light/dark, phone/tablet), Compose UI tests for key flows, **Kover** gate (≥ 80% for pure-Kotlin modules like `core:detection`, ≥ 60% overall).
- One aggregate Gradle task, **`./gradlew ciCheck`**, runs exactly what CI runs, so local and CI never drift.
- **Privacy check in CI**: fail the build if the merged release manifest contains `android.permission.INTERNET`.
- **Accessibility of our own UI**: content descriptions, 48 dp touch targets, sensible TalkBack order, works at 200% font scale.
- All user-facing strings in resources (English + Hindi).
- Debug builds: StrictMode + LeakCanary.
- **Security**: no secrets in the repo. Release signing reads environment variables / GitHub secrets. Use `PendingIntent` immutability flags, and export no components unless they need to be exported.

---

## 4. First session: preflight, bootstrap, GitHub setup, backlog

### 4.1 Preflight
Check `git`, `gh auth status` (scopes: `repo`, `workflow`; `project` is optional), JDK (whatever the current AGP needs), the Android SDK (`ANDROID_HOME`, `sdkmanager`), and `adb devices`. **If anything is missing, stop and tell me exactly what to install or run. Don't work around it.** A physical phone connected over adb is strongly preferred. If one is connected, use it for verification yourself.

### 4.2 Bootstrap commit (the ONLY direct push to `main`)
- Gradle wrapper, `build-logic` convention plugins, version catalog, every module from §2 as a compiling skeleton, and an app shell that builds and launches.
- Spotless, Detekt, Lint, Kover, Roborazzi wired up, plus the `ciCheck` task.
- Files: `README.md` (pitch, features, roadmap, build steps, privacy statement, Mermaid module graph), `LICENSE`, `CONTRIBUTING.md` (describes the loop below), `SECURITY.md`, `.editorconfig`, `.gitignore` (keystores, `local.properties`, build outputs), `CODEOWNERS` (`* @<login>`), `docs/PROJECT_BRIEF.md`, `docs/architecture.md`, `docs/adr/0001-record-architecture-decisions.md`.
- `.github/`:
  - `ISSUE_TEMPLATE/feature.yml`, `bug.yml`, `chore.yml`, `config.yml` (YAML issue forms)
  - `pull_request_template.md` (Closes #, What & why, How tested, Screenshots, Device test steps, Risks, checklist)
  - `workflows/ci.yml`: on PRs and pushes to `main`. Job name **`ci`**. Uses `gradle/actions/setup-gradle` caching and concurrency with cancel-in-progress. Runs `./gradlew ciCheck assembleDebug` and the INTERNET-permission check, and uploads the debug APK + test/lint reports.
  - `workflows/instrumented.yml`: nightly + manual. Runs connected tests on an emulator (not a required check).
  - `workflows/release.yml`: on `v*` tags. Builds a release APK/AAB. Signs it only if the secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` exist; otherwise attaches the debug APK. Creates a GitHub Release with generated notes.
  - `dependabot.yml`: Gradle + GitHub Actions, weekly, grouped.
- Claude Code setup:
  - `CLAUDE.md`, **kept short**: what the app is, key commands, architecture rules, the loop, definition of done, don'ts. It points to `docs/` for detail.
  - `.claude/agents/android-reviewer.md`, the review subagent (spec in §6).
  - `.claude/settings.json` with a permissions allowlist for `./gradlew`, `git`, `gh` and `adb` commands. Check the current Claude Code settings docs for the exact syntax.

### 4.3 GitHub setup
1. `gh repo create unreel-android --public --source . --push`, with a description and topics (`android`, `kotlin`, `jetpack-compose`, `accessibility-service`, `digital-wellbeing`, `material3`).
2. Repo settings via `gh repo edit`: squash merge only, delete branch on merge, auto-merge on, wiki off.
3. **Once `ci` has run on `main` at least once**, protect `main` via `gh api` (ruleset or classic branch protection): require a PR, **0 required approvals** (GitHub doesn't allow approving your own PR, so the review step in §5 takes the place of approval), require the `ci` status check to pass and be up to date, linear history, block force-pushes and deletion.
4. Labels: replace the GitHub defaults with `type:feature`, `type:bug`, `type:chore`, `type:docs`, `type:test`, `type:perf`, `area:detection`, `area:service`, `area:ui`, `area:data`, `area:infra`, `priority:p0`/`p1`/`p2`, `size:S`/`M`/`L`, `needs-device-test`, `tech-debt`, `blocked`.
5. Milestones from §7, each with a one-paragraph goal.
6. Optional: a GitHub Project board (Backlog / In progress / In review / Done) if `gh` has the `project` scope. Otherwise skip it and tell me how to enable it.
7. **Create the full backlog as issues** from §7. Each issue has context, testable acceptance criteria as checkboxes, technical notes, a test plan, labels, a milestone and dependencies ("Blocked by #x"). Every issue must fit in **one PR of ≤ ~400 changed lines** (excluding generated files and fixtures). Split anything bigger.

---

## 5. The loop (every change after the bootstrap commit)

1. **Pick.** `git switch main && git pull`. If you have an open PR (`gh pr list --author @me`), finish it first. Otherwise take the next open issue in the current milestone whose blockers are closed. Assign it to yourself and comment a 3–5 line plan on the issue.
2. **Branch.** `gh issue develop <n> --checkout --name <type>/<n>-<slug>` (type: `feat|fix|chore|docs|test|perf|refactor`).
3. **Build.** Write tests first for pure logic (detection rules, repositories, ViewModels). Make small Conventional Commits, each referencing `#<n>`.
4. **Local gate.** `./gradlew spotlessApply ciCheck` must be green before you push. **Never get green by deleting or skipping tests, lowering thresholds, adding lint baselines or `@Suppress`.** Fix the cause, or explain in the PR and open a `tech-debt` issue.
5. **Raise PR.** `gh pr create`:
   - Title: `<type>(<scope>): <summary> (#<n>)`
   - Body from the template: `Closes #<n>`, what & why, how it was tested, screenshots for UI changes (Roborazzi images, or `adb exec-out screencap -p` from a device), manual device steps if labelled `needs-device-test`, risks.
   - Same labels and milestone as the issue.
6. **Review.** Never skipped, and never done only by the context that wrote the code:
   - a. Run the built-in **`/code-review`** on the PR with `--comment` so findings land as inline PR comments (e.g. `/code-review high --comment <pr>`). Wait for it to finish.
   - b. Delegate to the **`android-reviewer` subagent**. It reviews `gh pr diff <pr>` against the issue's acceptance criteria and its checklist, then posts one review with `gh pr review <pr> --comment --body-file <file>`. Findings are tagged **BLOCKER / MAJOR / NIT**, with a met/not-met line for each acceptance criterion.
   - c. Fix every BLOCKER and MAJOR in new commits. Reply to each finding with the fixing commit SHA or a reasoned rejection. For NITs, fix them if cheap; otherwise open a `tech-debt` issue and link it.
   - d. If anything was a BLOCKER, run (b) again on the new diff. **Max 2 rounds.** If it's still blocked, stop and ask me.
   - For `size:S` docs/chore PRs, step (b) alone is enough.
7. **Merge.** `gh pr checks <pr> --watch`. When green, `gh pr merge <pr> --squash --delete-branch` with a concise squash message. Confirm the issue auto-closed.
8. **Repeat.**

**At the end of each milestone:** tag `v0.<m>.0` (the final milestone is `v1.0.0`) so `release.yml` publishes an APK. Update the README roadmap. Send me a short report: what shipped, screenshots, known issues, and **the exact device tests I should run**. Any bugs I report become issues and go through the same loop.

### Starting or resuming a session
Read `CLAUDE.md` and `docs/PROJECT_BRIEF.md`. Run `gh pr list --state open` and `gh issue list --milestone "<current>" --state open`. Finish open PRs first, then continue the loop. GitHub issues are the source of truth for progress.

---

## 6. `android-reviewer` subagent (create at `.claude/agents/android-reviewer.md`)

Frontmatter: `name: android-reviewer`, a short `description` ("Reviews a PR diff for an Android Kotlin/Compose app against its issue's acceptance criteria and the project checklist"), `tools: Read, Grep, Glob, Bash`, `model: inherit`.

Its system prompt makes it a strict senior Android reviewer. It reads the issue (`gh issue view`), the diff (`gh pr diff`) and the surrounding code, then checks:
- **Acceptance criteria**: each one met or not met, with evidence (file:line or test name).
- **Correctness**: lifecycle, coroutine scopes/cancellation, main-thread work, leaks (Context/Service refs in singletons), process death, config changes, edge cases, time zones.
- **Architecture**: layering, UDF, no logic in composables/services/receivers, DI, module boundaries.
- **Platform & policy**: permissions (runtime/special), API-level guards, background limits, exported components, PendingIntent flags, the Play accessibility disclosure, **no INTERNET**, and no on-screen content in logs or storage.
- **Detection-specific**: new rules have sanitised positive **and negative** fixtures, there are no hardcoded IDs in Kotlin, and false-positive risk is assessed.
- **Tests**: meaningful assertions, edge cases, screenshot tests for UI changes.
- **UI**: accessibility, string resources, dark mode, adaptive layout.
- **Performance**: cost of accessibility event handling, allocations in hot paths, recomposition.

Output: a summary verdict (APPROVE-equivalent / CHANGES NEEDED), the acceptance-criteria table, then the findings list tagged BLOCKER / MAJOR / NIT with file:line references.

---

## 7. Backlog (create these as issues; split further where needed)

**M0 – Foundation** (no tag)
- Design system: M3 theme, dynamic colour, typography, core components, screenshot tests
- App shell: type-safe navigation, adaptive scaffold (bottom bar ↔ nav rail), placeholder screens
- `core:common` (dispatchers, result types) + `core:testing` (MainDispatcherRule, fakes, Roborazzi helpers)
- `docs/architecture.md` with Mermaid module graph; ADRs for state management/UDF and settings storage

**M1 – Detection engine** → `v0.1.0`
- Detection model + rules JSON schema + parser + matcher (pure Kotlin, fully unit-tested)
- Debug-only event logger/inspector + `scripts/capture-fixture.sh` + sanitiser (with tests proving personal text is stripped)
- YouTube Shorts rules + positive/negative fixtures
- Instagram Reels rules + fixtures (reels tab, reel from feed, reel from DM; negatives: home, stories, DMs, explore grid)
- Accessibility service skeleton: config, snapshot builder with debounce/caps, engine wiring, debug timing logs
- Hard block end-to-end (behind a debug toggle), verified on device

**M2 – Interventions & control** → `v0.2.0`
- Per-app protection level: data + UI
- Pause overlay (`TYPE_ACCESSIBILITY_OVERLAY`, Compose in service, breathing countdown)
- "Allow one, block the scroll"
- Daily budgets (reset at local midnight; handle time zone changes)
- Schedules
- Commitment lock
- Onboarding: disclosure & consent, enable accessibility, restricted-settings guide, battery/OEM guide
- Service health monitor + notification

**M3 – Insights & surfaces** → `v0.3.0`
- Feed-session tracking in Room + daily aggregation worker
- Insights screen (day/week charts, streaks, reclaimed estimate)
- Home dashboard (protection status + service health, today summary, quick actions)
- Glance widget
- Quick Settings tile with friction confirm
- JSON export via SAF + reset data
- Hindi localisation

**M4 – Hardening & release** → `v1.0.0`
- Performance & battery audit: tune event filters, microbenchmark snapshot+match, write `docs/performance.md`
- Baseline Profile + Macrobenchmark (startup)
- Resilience: process death, service restart, reboot, target-app updates (rule versioning + fallback heuristics), "Report a detection problem"
- Play readiness: review the disclosure flow, `docs/play/accessibility-declaration.md`, data-safety answers, privacy policy on GitHub Pages, store listing copy + screenshots
- README final: demo GIF, architecture, key decisions, benchmark numbers
- Release `v1.0.0`

**M5 – Stretch** (create the issues, but don't start them until I say so)
- Facebook Reels, Snapchat Spotlight support

---

## 8. Stop and ask me ONLY when
- Preflight fails, or a tool/auth/scope is missing.
- An action is irreversible or reaches beyond this repo (deleting/renaming the repo, force-push, anything in Play Console).
- Release signing: ask me to create the keystore and add the secrets. Never generate or commit one yourself.
- Device-only verification is needed and no phone is connected. Batch these at milestone end, with exact steps.
- There is a Play-policy or privacy trade-off.
- The same CI failure happens 3 times, or a review is still blocked after 2 rounds.

Otherwise keep working through the loop without asking.

## 9. Done means
`v1.0.0` is tagged with an installable APK on GitHub Releases. All M0–M4 issues are closed through reviewed PRs. CI is green on `main`. The README shows a demo GIF, architecture and decisions. There is no `INTERNET` permission. Everything is documented well enough for a reviewer to understand the project in 5 minutes.

**Start now with §4.1 Preflight.**
