package io.github.uveshm003.unreel.core.detection

/**
 * Matches the highest-priority eligible rule anywhere in the snapshot. Ties retain file order.
 * One detection visits at most 512 nodes (including revisits) and descends at most 32 edges.
 * A search that reaches either limit without a decision returns [DetectionResult.None]. This also
 * applies inside negation, where a partial search cannot establish that a descendant is absent.
 * Traversal uses bounded call frames and indexed child access, without per-node heap allocations.
 */
class DetectionEngine(files: List<DetectionRules>) {
    private val byPackage = buildMap {
        for (file in files) {
            require(file.packageName !in this) { "Duplicate rules file for ${file.packageName}" }
            put(file.packageName, file.rules.sortedByDescending { it.priority })
        }
    }

    fun detect(input: DetectionInput): DetectionResult {
        val root = input.root
        val rules = byPackage[input.packageName].orEmpty()
        if (root != null && rules.isNotEmpty()) {
            val search = Search()
            for (rule in rules) {
                val eligible = rule.versionCode == null || input.versionCode in rule.versionCode
                if (eligible && search.find(root, rule.match, 0)) return DetectionResult(rule.surface, false, rule.id)
                if (search.incomplete) break
            }
        }
        return DetectionResult.None
    }
}

private const val MAX_DEPTH = 32
private const val NODE_BUDGET = 512

private class Search {
    private var remaining = NODE_BUDGET
    var incomplete = false
        private set

    fun find(node: NodeSnapshot, matcher: Matcher, depth: Int): Boolean =
        visit(node, matcher, depth, includeDescendants = true)

    private fun visit(node: NodeSnapshot, matcher: Matcher, depth: Int, includeDescendants: Boolean): Boolean {
        if (remaining == 0 || depth > MAX_DEPTH) {
            incomplete = true
            return false
        }
        remaining--
        return matches(node, matcher, depth) ||
            (includeDescendants && !incomplete && childrenMatch(node, matcher, depth, true))
    }

    private fun matches(node: NodeSnapshot, matcher: Matcher, depth: Int): Boolean = when (matcher) {
        is Matcher.ViewId -> matchesViewId(node.viewIdResourceName, matcher.value)
        is Matcher.ClassName -> node.className == matcher.value
        is Matcher.ContentDescription -> node.contentDescription == matcher.value
        is Matcher.Flags -> matcher.matchesFlags(node)
        is Matcher.HasChild -> childrenMatch(node, matcher.matcher, depth, false)
        is Matcher.HasDescendant -> childrenMatch(node, matcher.matcher, depth, true)
        is Matcher.All -> groupMatches(node, matcher.matchers, depth, requireAll = true)
        is Matcher.AnyOf -> groupMatches(node, matcher.matchers, depth, requireAll = false)
        is Matcher.Not -> !matches(node, matcher.matcher, depth) && !incomplete
    }

    private fun childrenMatch(node: NodeSnapshot, matcher: Matcher, depth: Int, recurse: Boolean): Boolean {
        for (index in node.children.indices) {
            if (visit(node.children[index], matcher, depth + 1, recurse)) return true
            if (incomplete) break
        }
        return false
    }

    private fun groupMatches(node: NodeSnapshot, matchers: List<Matcher>, depth: Int, requireAll: Boolean): Boolean {
        for (index in matchers.indices) {
            val matched = matches(node, matchers[index], depth)
            if (incomplete || matched != requireAll) return matched && !incomplete
        }
        return requireAll
    }
}

private fun matchesViewId(actual: String?, expected: String): Boolean {
    if (actual == null || actual == expected) return actual == expected
    val separator = ":id/"
    val offset = actual.indexOf(separator)
    return offset > 0 && actual.length == offset + separator.length + expected.length &&
        actual.regionMatches(offset + separator.length, expected, 0, expected.length)
}

private fun Matcher.Flags.matchesFlags(node: NodeSnapshot) = (visible == null || visible == node.isVisibleToUser) &&
    (scrollable == null || scrollable == node.isScrollable) &&
    (selected == null || selected == node.isSelected)
