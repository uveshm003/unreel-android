# Detection rule format

Each file in `core/detection/src/main/rules/<package>.json` describes one Android package. The example below uses a fictional fixture package and identifiers. [Prepare a sanitised fixture](sanitising-fixtures.md).

```json
{
  "schemaVersion": 1,
  "package": "example.fixture",
  "rulesVersion": 1,
  "rules": [
    {
      "id": "fixture-viewer",
      "surface": "ShortsPlayer",
      "versionCode": { "min": 10, "max": 25 },
      "priority": 10,
      "match": {
        "type": "all",
        "matchers": [
          { "type": "viewId", "value": "example.fixture:id/sample_viewer" },
          { "type": "flags", "visible": true },
          { "type": "hasChild", "matcher": { "type": "className", "value": "fixture.Player" } }
        ]
      }
    }
  ]
}
```

The parser accepts schema version 1. `rulesVersion` is a positive integer for the whole file. Each rule needs a unique nonblank ID, an integer priority, a matcher and one of `ReelsTab`, `ReelsViewer` or `ShortsPlayer`. `None` is the engine result when nothing matches. A rule’s optional range uses inclusive, non-negative Android version codes. Either endpoint may be omitted. Higher priorities take precedence, with file order breaking a tie.

| Matcher type | Fields and meaning |
| --- | --- |
| `viewId` | `value` is a full resource ID or its entry name after `:id/`. |
| `className`, `contentDescription` | `value` is a nonblank exact string. Descriptions vary by language. |
| `flags` | Specify at least one boolean from `visible`, `scrollable` and `selected`. All supplied flags must agree. |
| `hasChild`, `hasDescendant` | `matcher` must hold on a direct child or a strict descendant, respectively. |
| `all`, `any` | `matchers` is a nonempty array. Evaluate every condition or at least one condition on the same node. |
| `not` | Negate the nested `matcher` on the same node. |

Use exact structural evidence from sanitised fixtures. A snapshot supplies bounds, children, identifiers, an optional description and boolean flags. Text from the screen is excluded. Reject malformed files before use.
