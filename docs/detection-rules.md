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

The engine tests each eligible rule against the root and then its descendants in child order. A structural matcher starts from its current candidate node. A detection can visit up to 512 nodes across all candidate rules and explore 32 edges below the root. Nested searches and revisits count toward the same budget. If evaluation needs to go further, the result is `None`. An incomplete search cannot establish a negative condition or justify selecting a lower-priority rule.

### Recognising a move to the next item

A `ReelsViewer` or `ShortsPlayer` rule can declare `"swipeSignal": { "source": { "type": "className", "value": "fixture.Pager" } }`. Match the actual scroll event source, using a pager identity verified by fixtures. Supply the event’s monotonic timestamp and its first visible item index when available. A pixel scroll distance, an inner comments list or a change in visible text cannot establish a new item. Use this signal only for pagers with a reliable indexed position.

Within one viewer the first known position establishes a baseline. A newer `ViewScrolled` event from a matching source emits one swipe if its index advances. The engine ignores duplicate positions, stale events and backwards motion. A new window, package, app version or viewer starts a new baseline. Missing timestamps clear the baseline. Tracking retains only the app, version, rule ID, timestamp and numeric position in memory.
