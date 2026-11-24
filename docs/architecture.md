# Architecture

Unreel is a single-activity Compose app plus an AccessibilityService. Everything runs on the device; there
is no network access (see the privacy check in `build-logic`). This page is the map. Decisions and their
reasons live in [ADRs](adr/).

## Layers

```
UI (Compose screens + ViewModels, :feature:*)            one StateFlow<UiState> per ViewModel
   │  user actions are functions, one-off effects via Channel
   ▼
Domain (use cases only where they add value)
   ▼
Data (:core:data repositories)                           the single source of truth for each kind of data
   ▼
Sources (:core:database Room, :core:datastore DataStore, platform managers)
```

## Detection pipeline

The following sequence describes the planned service. Feature screens and the service share the same
repository contracts. A change to the Apps screen therefore reaches the next policy decision without
restarting the service.

```mermaid
sequenceDiagram
    participant UI as Settings screen
    participant Repo as Protection repository
    participant OS as Android
    participant S as Accessibility service
    participant D as Detection engine
    participant P as Intervention policy
    UI->>Repo: Request a protection change
    Repo->>Repo: Validate lock and write in one transaction
    Repo-->>S: Emit updated policy settings
    OS->>S: Accessibility event
    S->>S: Filter supported package and event type, coalesce updates
    S->>S: Take a bounded snapshot of the current window
    S->>D: Match immutable snapshot against package rules
    D-->>S: Surface and swipe classification
    S->>P: Evaluate classification against current settings and budget
    P-->>S: Requested intervention
    S->>S: Confirm window and decision are still current
    S->>OS: Perform action when an intervention is required
```

### Threading and lifetime

The main-thread callback filters events and schedules a snapshot. Read live accessibility nodes on
the service thread and copy only the fields detection needs before handing the snapshot to the
injected computation dispatcher. The engine consumes ordinary Kotlin values. Apply platform actions
on the main thread. If the window or package changes while matching is in flight, discard the stale
result. Cancel outstanding jobs and settings collection when the service disconnects.

Repositories expose flows backed by Room and DataStore. The service collects protection settings once
per connection and holds the latest snapshot in memory. Wait for the initial settings before acting.
An event callback can then evaluate policy without a disk read. A rejected settings edit leaves the
previous policy in effect. Session recording and aggregation use asynchronous database operations.

## Modules

| Module | Responsibility |
|---|---|
| `:app` | Activity, navigation host, Hilt application, debug tooling (StrictMode, LeakCanary) |
| `:core:common` | Dispatchers, result types, `Clock`; pure Kotlin |
| `:core:model` | Plain data types shared across layers; pure Kotlin |
| `:core:detection` | Feed detection engine: `NodeSnapshot` + rules (JSON) → `DetectedSurface`; pure Kotlin, ≥ 80 % coverage |
| `:core:database` | Room database (protection policy, feed sessions, aggregates); schemas exported to `core/database/schemas` |
| `:core:datastore` | DataStore for preferences and setup progress |
| `:core:data` | Repositories over the sources above |
| `:core:designsystem` | Material 3 theme, typography, core components |
| `:core:ui` | Shared composables that know about the model |
| `:core:testing` | Test support: dispatcher rule, fakes, screenshot helpers |
| `:service:accessibility` | The AccessibilityService, snapshot builder, overlays and actions |
| `:feature:*` | Screens + ViewModels: onboarding, home, apps, schedule, insights, settings |
| `:widget` | Glance widget and Quick Settings tile |
| `:baselineprofile`, `:benchmark` | Baseline Profile generator and macro/microbenchmarks (device only) |

Rules: features never depend on other features. `:core:detection`, `:core:common` and `:core:model` have no
Android dependencies. Build configuration lives in `build-logic/` convention plugins, never copied between
modules.

<!-- module-graph:start -->
```mermaid
graph TD
    app[app]
    baselineprofile[baselineprofile]
    benchmark[benchmark]
    widget[widget]
    subgraph core
        core_common[common]
        core_data[data]
        core_database[database]
        core_datastore[datastore]
        core_designsystem[designsystem]
        core_detection[detection]
        core_model[model]
        core_testing[testing]
        core_ui[ui]
    end
    subgraph feature
        feature_apps[apps]
        feature_home[home]
        feature_insights[insights]
        feature_onboarding[onboarding]
        feature_schedule[schedule]
        feature_settings[settings]
    end
    subgraph service
        service_accessibility[accessibility]
    end
    app --> core_data
    app --> core_designsystem
    app --> core_ui
    app --> feature_apps
    app --> feature_home
    app --> feature_insights
    app --> feature_onboarding
    app --> feature_schedule
    app --> feature_settings
    app --> service_accessibility
    app --> widget
    core_data --> core_common
    core_data --> core_database
    core_data --> core_datastore
    core_data --> core_model
    core_database --> core_model
    core_datastore --> core_common
    core_datastore --> core_model
    core_detection --> core_model
    core_testing --> core_common
    core_testing --> core_model
    core_ui --> core_designsystem
    core_ui --> core_model
    feature_apps --> core_data
    feature_apps --> core_designsystem
    feature_apps --> core_ui
    feature_home --> core_data
    feature_home --> core_designsystem
    feature_home --> core_ui
    feature_insights --> core_data
    feature_insights --> core_designsystem
    feature_insights --> core_ui
    feature_onboarding --> core_data
    feature_onboarding --> core_designsystem
    feature_onboarding --> core_ui
    feature_schedule --> core_data
    feature_schedule --> core_designsystem
    feature_schedule --> core_ui
    feature_settings --> core_data
    feature_settings --> core_designsystem
    feature_settings --> core_ui
    service_accessibility --> core_common
    service_accessibility --> core_data
    service_accessibility --> core_detection
    widget --> core_data
    widget --> core_designsystem
```
<!-- module-graph:end -->

Regenerate the graph with `python3 scripts/module-graph.py` after changing module dependencies (`ciCheck`
fails if it is out of date).

## Quality gates

`./gradlew ciCheck` is exactly what CI runs: Spotless (ktlint + Compose rules), Detekt, Android Lint
(errors fail), unit and Robolectric tests, Roborazzi screenshot verification, Kover (≥ 80 % for pure
Kotlin modules, ≥ 60 % overall), the module-graph check and the no-`INTERNET` check on the merged release
manifest.
