package io.github.uveshm003.unreel.core.detection

/**
 * Matches the highest-priority eligible rule anywhere in the snapshot. Ties retain file order.
 * One detection visits at most 512 nodes (including revisits) and descends at most 32 edges.
 * A search that reaches either limit without a decision returns [DetectionResult.None]. This also
 * applies inside negation, where a partial search cannot establish that a descendant is absent.
 * Traversal uses bounded call frames and indexed child access, without per-node heap allocations.
 */
class DetectionEngine(files: List<DetectionRules>) {
    private val tracker = SwipeTracker()
    private val byPackage = buildMap {
        for (file in files) {
            require(file.packageName !in this) { "Duplicate rules file for ${file.packageName}" }
            put(file.packageName, file.rules.sortedByDescending { it.priority })
        }
    }

    /** Use one engine per ordered event stream. All position state stays in memory. */
    @Synchronized
    fun detect(input: DetectionInput): DetectionResult {
        val root = input.root
        val rules = byPackage[input.packageName].orEmpty()
        val search = Search()
        val rule = root?.let { search.winningRule(it, rules, input.versionCode) }
        val position = input.scrollPosition
        val source = rule?.swipeSignal?.source
        val index = if (position != null && source != null && search.matchesSource(position.source, source)) {
            position.firstVisibleItem.takeIf { it >= 0 }
        } else {
            null
        }
        val swiped = tracker.observe(input, rule, index)
        return if (rule == null) DetectionResult.None else DetectionResult(rule.surface, swiped, rule.id)
    }
}

private const val MAX_DEPTH = 32
private const val NODE_BUDGET = 512

private class Search {
    private var remaining = NODE_BUDGET
    private var incomplete = false

    fun winningRule(root: NodeSnapshot, rules: List<DetectionRule>, versionCode: Long): DetectionRule? {
        for (rule in rules) {
            val eligible = rule.versionCode == null || versionCode in rule.versionCode
            if (eligible && visit(root, rule.match, 0, includeDescendants = true)) return rule
            if (incomplete) break
        }
        return null
    }

    fun matchesSource(node: NodeSnapshot, matcher: Matcher): Boolean =
        visit(node, matcher, 0, includeDescendants = false)

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
