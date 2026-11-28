package io.github.uveshm003.unreel.core.common.dispatchers

import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

class DispatchersModuleTest {
    @Test
    fun providesTheRealDispatchers() {
        assertSame(Dispatchers.IO, DispatchersModule.providesIoDispatcher())
        assertSame(Dispatchers.Default, DispatchersModule.providesDefaultDispatcher())
    }

    @Test
    fun applicationScope_usesTheGivenDispatcher() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = DispatchersModule.providesApplicationScope(dispatcher)
        var ran = false

        scope.launch { ran = true }
        testScheduler.advanceUntilIdle()

        assertTrue(ran)
        scope.cancel()
    }

    @Test
    fun applicationScope_survivesAFailingChild() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = DispatchersModule.providesApplicationScope(dispatcher)
        val second = CompletableDeferred<Unit>()

        scope.launch(CoroutineExceptionHandler { _, _ -> }) { error("boom") }
        testScheduler.advanceUntilIdle()
        scope.launch { second.complete(Unit) }
        testScheduler.advanceUntilIdle()

        assertTrue(second.isCompleted, "SupervisorJob: a failed child must not cancel the scope")
        assertTrue(scope.coroutineContext[Job]!!.isActive)
        scope.cancel()
    }
}
