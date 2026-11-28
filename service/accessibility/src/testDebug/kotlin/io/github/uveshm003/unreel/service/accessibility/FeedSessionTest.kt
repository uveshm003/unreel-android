package io.github.uveshm003.unreel.service.accessibility

import io.github.uveshm003.unreel.core.model.AppProtection
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.model.ProtectionLevel
import kotlin.test.assertEquals
import org.junit.Test

class FeedSessionTest {
    @Test
    fun modesRespectTheBudgetBoundaryAndFiveMinutePass() {
        val policy = AppProtection(ProtectedApp.YouTube.packageName, dailyBudgetMinutes = 1)
        fun choose(level: ProtectionLevel, used: Long, remaining: Long = 0) =
            FeedSession.choose(policy.copy(level = level), used, remaining)
        assertEquals(FeedAction.Allow(Long.MAX_VALUE), choose(ProtectionLevel.Off, ONE_MINUTE))
        assertEquals(FeedAction.Pause, choose(ProtectionLevel.Pause, 0))
        assertEquals(FeedAction.Allow(PASS_REMAINING), choose(ProtectionLevel.Pause, 0, PASS_REMAINING))
        assertEquals(FeedAction.Allow(1), choose(ProtectionLevel.Budget, ONE_MINUTE - 1))
        assertEquals(FeedAction.Exit, choose(ProtectionLevel.Budget, ONE_MINUTE, PASS_REMAINING))
        assertEquals(FeedAction.Exit, choose(ProtectionLevel.HardBlock, 0, PASS_REMAINING))
    }
}

private const val ONE_MINUTE = 60_000L

private const val PASS_REMAINING = 10L
