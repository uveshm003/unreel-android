# 5. Store the app language through AppCompat

- **Status** Proposed
- **Date** 2026-11-24

## Context

Unreel supports Android 10 onward. Users can select English, Hindi, or the system language. Android 13 added a system per-app language picker.

## Decision

Use an AppCompat activity and the application locale API. AppCompat delegates to the platform locale manager on Android 13 and later. On Android 10–12L its automatic locale storage restores the choice during activity creation. An empty locale list selects the system default. Keep theme and wallpaper colour preferences in DataStore.

## Consequences

A language change recreates the activity and reloads Android and Compose resources together. The system picker and the in-app control share a single language setting. AppCompat’s compatibility storage can perform a small synchronous disk operation on older releases. [Android per-app language documentation, checked 2026-11-24](https://developer.android.com/guide/topics/resources/app-languages)
