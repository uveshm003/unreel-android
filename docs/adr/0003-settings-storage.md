# 3. Store protection policy in Room and app preferences in DataStore

**Status** · Proposed

**Date** · 2026-11-24

## Context

A schedule, budget, pause or reset can lower protection. A commitment lock must apply to all these
writes, including requests from the widget or Quick Settings. Appearance and setup progress change
independently and only need small, durable values.

## Decision

Use Room for per-app protection, schedules and their app membership, the lock expiry instant,
temporary allowances, and usage history. Put every protection-changing operation behind
`:core:data`. In one Room transaction, read the current lock using the injected clock, validate the
proposed change against current policy, and either apply it or return a typed rejection. This keeps a
concurrent lock and settings edit ordered. The UI, tile and widget call the same repository.

Use Preferences DataStore for small independent values such as theme, setup progress and disclosure
version. An accepted disclosure version and its timestamp are one edit. Store stable keys and values
rather than enum ordinals. Keep one DataStore instance per file per process. Onboarding completion,
consent, and the service's actual enabled state have different lifetimes and are checked separately.

### Alternatives

- Preferences DataStore can update a group of keys atomically. Schedule membership, time windows and
  history would need custom serialization and queries.
- A typed DataStore with kotlinx.serialization would give the policy a schema and one atomic update.
  Each schedule edit would rewrite the full document, and linking it to Room-backed usage and resets
  would cross two transaction boundaries.
- Room alone would also work for preferences, at the cost of schema migrations for independent
  display settings. Keeping lock-governed fields together provides the transaction boundary we need.

## Consequences

Export each Room schema and test migrations from supported versions with populated policy, schedules
and lock records. Preserve stable IDs, existing budgets and absolute lock expiries. Give new
preference keys explicit defaults. Rename or transform existing keys with an idempotent DataStore
migration before consumers see the data. An unknown disclosure version requires fresh consent.

Tests exercise concurrent edits, rejected weakening changes and restart behavior against the actual
stores. A preference read failure must not report onboarding or consent as complete. Surface a
policy database failure and preserve its data for recovery.

The accessibility service runs in the app process and uses the same single-process DataStore instance.
If the preferences file is corrupt, a `ReplaceFileCorruptionHandler` can replace it with empty
preferences, returning setup to Welcome and clearing consent. Handle ordinary I/O failures as
retryable errors. Revisit the store implementation before placing a consumer in another process.
Checked on 2026-11-24 against the [Android DataStore guide](https://developer.android.com/topic/libraries/architecture/datastore).
