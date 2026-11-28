package io.github.uveshm003.unreel.service.accessibility

import io.github.uveshm003.unreel.core.data.ProtectionActivity
import io.github.uveshm003.unreel.core.data.ProtectionSettingsRepository
import io.github.uveshm003.unreel.core.model.AppProtection
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.model.ProtectionLevel
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

internal sealed interface FeedAction {
    data object Exit : FeedAction
    data object Pause : FeedAction
    data class Allow(val checkAfterMillis: Long) : FeedAction
}

/** Accessed only by the service's ordered event consumer. A reading never spans two window sessions. */
internal class FeedSession @Inject constructor(
    private val activity: ProtectionActivity,
    private val policies: ProtectionSettingsRepository,
) {
    private var lastSeen: Long? = null
    private var day: LocalDate? = null
    private var pending = 0L
    private var window: Long? = null

    suspend fun leave() {
        persist()
        lastSeen = null
        window = null
    }

    suspend fun visit(id: Long, elapsed: Long, passRemaining: Long): FeedAction {
        val today = activity.today()
        if (today != day || id != window) leave()
        day = today
        window = id
        lastSeen?.let { pending += (elapsed - it).coerceAtLeast(0) }
        lastSeen = elapsed
        if (pending >= CHECKPOINT) persist()
        val state = activity.snapshot()
        val policy = policies.settings.first().getValue(ProtectedApp.YouTube.packageName)
        val now = activity.now()
        val untilStart = Duration.between(now, state.window.start).toMillis().let {
            if (it <= 0) it + DAY else it
        }
        val action = if (state.window.contains(now)) {
            FeedAction.Exit
        } else {
            choose(policy, state.days.first().viewingMillis + pending, passRemaining)
        }
        return if (state.window.enabled && action is FeedAction.Allow) {
            FeedAction.Allow(minOf(untilStart, action.checkAfterMillis))
        } else {
            action
        }
    }

    suspend fun exited() {
        leave()
        activity.record(activity.today(), exits = 1)
    }

    private suspend fun persist() {
        if (pending > 0) day?.let { activity.record(it, millis = pending) }
        pending = 0L
    }

    companion object {
        private const val CHECKPOINT = 15_000L
        private const val DAY = 86_400_000L
        fun choose(policy: AppProtection, usedMillis: Long, passRemaining: Long): FeedAction = when (policy.level) {
            ProtectionLevel.Off -> FeedAction.Allow(Long.MAX_VALUE)

            ProtectionLevel.HardBlock -> FeedAction.Exit

            ProtectionLevel.Pause -> if (passRemaining > 0) FeedAction.Allow(passRemaining) else FeedAction.Pause

            ProtectionLevel.Budget -> {
                val remaining = Duration.ofMinutes(policy.dailyBudgetMinutes.toLong()).toMillis() - usedMillis
                if (remaining > 0) FeedAction.Allow(remaining) else FeedAction.Exit
            }
        }
    }
}
