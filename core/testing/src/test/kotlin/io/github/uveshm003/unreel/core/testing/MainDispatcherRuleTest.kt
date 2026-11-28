package io.github.uveshm003.unreel.core.testing

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MainDispatcherRuleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun mainDispatcher_isTheTestDispatcher() = runTest(mainDispatcherRule.testDispatcher) {
        var ran = false
        launch(Dispatchers.Main) { ran = true }

        assertFalse("StandardTestDispatcher queues work until the scheduler runs", ran)
        testScheduler.advanceUntilIdle()
        assertTrue(ran)
    }

    @Test
    fun rule_setsMainForTheTestAndResetsItAfterwards() {
        val description = Description.createTestDescription(MainDispatcherRuleTest::class.java, "nested")
        var mainUsableInside = false
        val body = object : Statement() {
            override fun evaluate() {
                mainUsableInside = runCatching { Dispatchers.Main.isDispatchNeeded(EmptyCoroutineContext) }.isSuccess
            }
        }

        MainDispatcherRule(StandardTestDispatcher()).apply(body, description).evaluate()

        assertTrue(mainUsableInside)
        // resetMain() removes the test Main; plain JVM tests have no real Main dispatcher to fall back to.
        assertTrue(runCatching { Dispatchers.Main.isDispatchNeeded(EmptyCoroutineContext) }.isFailure)
    }
}
