package io.github.uveshm003.unreel.core.detection

import kotlin.test.assertEquals
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Test

class YouTubeFixtureTest {
    private val pkg = "com.google.android.youtube"
    private val version = 1561170057L
    private val engine = DetectionEngine(listOf(RulesParser.parse(resource("/$pkg.json"))))

    @Test
    fun shorts_matches_only_the_captured_version_and_viewer() {
        val viewer = input("shorts")
        assertEquals(DetectedSurface.ShortsPlayer, engine.detect(viewer).surface)
        assertEquals(DetectionResult.None, engine.detect(viewer.copy(versionCode = version + 1)))
        assertEquals(DetectionResult.None, engine.detect(viewer.copy(packageName = "other.package")))
        for (screen in listOf(
            "home",
            "regular",
        )) {
            assertEquals(DetectionResult.None, engine.detect(input(screen)), screen)
        }
    }

    @Test
    fun service_can_see_the_pager_when_android_omits_its_outer_wrappers() {
        val capture = Json.decodeFromString<Fixture>(resource("/fixtures/$pkg/$version/shorts.json"))
        val pager = capture.subtree("$pkg:id/reel_recycler")!!
        val page = pager.children.single()
        val input = DetectionInput(DetectionEvent.ContentChanged, pkg, version, pager.snapshot())
        assertEquals(DetectedSurface.ShortsPlayer, engine.detect(input).surface)
        val nearMisses = listOf(
            pager.copy(children = emptyList()),
            pager.copy(isVisibleToUser = false),
            pager.copy(children = listOf(page.copy(isVisibleToUser = false))),
        )
        for (nearMiss in nearMisses) {
            assertEquals(
                DetectionResult.None,
                engine.detect(input.copy(root = nearMiss.snapshot())),
            )
        }
    }

    private fun input(screen: String): DetectionInput {
        val fixture = Json.decodeFromString<Fixture>(resource("/fixtures/$pkg/$version/$screen.json"))
        return DetectionInput(DetectionEvent.WindowStateChanged, pkg, version, fixture.snapshot())
    }

    private fun resource(name: String) = checkNotNull(javaClass.getResource(name)).readText()

    @Serializable
    private data class Fixture(
        val viewIdResourceName: String?,
        val className: String?,
        val contentDescription: String?,
        val isVisibleToUser: Boolean,
        val isScrollable: Boolean,
        val isSelected: Boolean,
        val bounds: NodeBounds,
        val children: List<Fixture>,
    ) {
        fun subtree(id: String): Fixture? = if (viewIdResourceName ==
            id
        ) {
            this
        } else {
            children.firstNotNullOfOrNull { it.subtree(id) }
        }

        fun snapshot(): NodeSnapshot {
            check(contentDescription == null)
            return NodeSnapshot(
                viewIdResourceName,
                className,
                null,
                isVisibleToUser,
                isScrollable,
                isSelected,
                bounds,
                children.map {
                    it.snapshot()
                },
            )
        }
    }
}
