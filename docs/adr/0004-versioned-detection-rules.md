# 4. Version the feed detection rules as data

- **Status** Proposed
- **Date** 2026-11-25

## Context

Instagram and YouTube can change their UI independently of Unreel. A rule matching a message thread or an ordinary video could interrupt someone outside a short-form feed.

## Decision

Package one JSON rule file per supported app. Keep the format version, the file’s rules revision and the supported app version-code bounds separate. Parse and validate the complete file before evaluating any rule. Rule authors should use resource IDs, class names, flags and relationships justified by sanitised fixtures. A content description is an exact, locale-dependent condition and needs evidence for the locale it handles.

## Consequences

A syntax error, unknown matcher or conflicting identifier rejects the file as a unit. Introducing a new incompatible format requires a new schema version. Production identifiers belong in rule assets with positive and negative fixtures. The Kotlin engine works with structural snapshots that contain no node text. [Rule format](../detection-rules.md)
