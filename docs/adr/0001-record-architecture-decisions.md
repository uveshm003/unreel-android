# 1. Record architecture decisions

- **Status:** Accepted
- **Date:** 2026-11-20

## Context

Unreel is built by one engineer across many sessions, with every change going through an issue and a
reviewed pull request. Decisions about platform APIs (AccessibilityService configuration, overlay window
types, global actions, WorkManager scheduling, battery-optimisation exemptions) carry Play-policy, battery
and privacy consequences that are easy to forget and expensive to rediscover. Reviewers, human or agent,
need to know *why* the code looks the way it does.

## Decision

We record every significant decision as an Architecture Decision Record in `docs/adr/`, using the
lightweight format by Michael Nygard:

- File name: `NNNN-kebab-case-title.md`, numbered sequentially, never renumbered.
- Sections: **Status** (Proposed / Accepted / Superseded by ADR-NNNN / Deprecated), **Date**, **Context**,
  **Decision**, **Consequences**, and **Alternatives considered** where relevant.
- An ADR is added in the same pull request as the code it describes and is linked from that PR.
- Accepted ADRs are not edited to change the decision; a new ADR supersedes the old one.
- Any decision that relies on a platform API cites the developer.android.com page it was checked against
  and the date it was checked.

A decision needs an ADR when it is hard to reverse, has policy/privacy/battery impact, chooses between
credible alternatives, or deviates from `docs/PROJECT_BRIEF.md` (for example a non-stable library or a
foreground service).

## Consequences

- Reviewers can check code against recorded intent; the `android-reviewer` agent reads `docs/adr/`.
- Slightly more writing per significant PR. Small, reversible choices stay in code comments and PR
  descriptions instead.
