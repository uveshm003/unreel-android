# 2. Screen state and user actions

**Status** · Proposed

**Date** · 2026-11-24

## Context

The same protection setting can change from the app or a system surface. Screens need to display
the committed value after recreation and surface rejected edits to the user.

## Decision

Each feature ViewModel exposes one immutable `StateFlow<UiState>`. Map or combine repository flows
and use `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState)`. The initial
state represents loading when the stored value controls what the user may do. The screen collects
with `collectAsStateWithLifecycle()` and passes state and callbacks to its content composable.

Expose actions as named functions such as `setBudget(minutes)`. The repository validates and commits
the request. Its flow updates the screen. Put durable outcomes in state. For a transient effect with
one consumer, such as a snackbar, send to a buffered `Channel<Effect>` and expose `receiveAsFlow()`.
The screen collects effects once while started. A pending consent decision or setup step belongs in
storage so it survives process death.

## Consequences

Tests run the ViewModel with a test dispatcher, a fake repository and Turbine. Start collecting
before dispatching an action, assert the loading and committed states, then recreate the collector
to check the latest value. For a rejected change, assert the repository value stays intact and the
active consumer receives the effect once. Composables can also be tested with a supplied state.

The lifecycle collection pattern follows the [Android guidance on state in Compose](https://developer.android.com/develop/ui/compose/state),
checked on 2026-11-24.
