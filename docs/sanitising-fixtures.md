# Sanitising a local snapshot

From the repository root, run `./gradlew :core:detection:sanitiseFixture --args='example.fixture /tmp/dump.xml /tmp/fixture.json'`. The input can also be a debug JSON snapshot with a single root node. The XML input can contain one `node` or a `hierarchy` with one root `node`. Android UI Automator dumps omit invisible children, so XML nodes default to visible when the flag is absent. Make the output directory before running the command.

The tool writes deterministic, pretty-printed JSON. It copies structural identifiers, flags, numeric bounds and child order. Every `text` field and unknown property is discarded. To preserve a verified static description, add the exact label to a JSON string array in `core/detection/src/main/rules/labels/<package>.json`. Without an allowlist, every nonempty description becomes `[redacted]`. Keep raw inputs outside the repository.
