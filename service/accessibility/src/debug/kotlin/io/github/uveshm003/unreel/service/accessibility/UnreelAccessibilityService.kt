package io.github.uveshm003.unreel.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import io.github.uveshm003.unreel.core.common.dispatchers.Dispatcher
import io.github.uveshm003.unreel.core.common.dispatchers.UnreelDispatchers
import io.github.uveshm003.unreel.core.data.FeedPreviewRepository
import io.github.uveshm003.unreel.core.detection.DetectionEvent
import io.github.uveshm003.unreel.core.model.ProtectedApp
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class UnreelAccessibilityService : AccessibilityService() {
    @Inject lateinit var preview: FeedPreviewRepository

    @Inject lateinit var session: FeedSession

    @Inject lateinit var guard: FeedGuard

    @Inject
    @Dispatcher(UnreelDispatchers.IO)
    lateinit var io: CoroutineDispatcher
    private var incoming: Channel<WindowEvent>? = null
    private var worker: Job? = null
    private var windowId: Int? = null
    private val generation = AtomicLong()

    override fun onServiceConnected() {
        super.onServiceConnected()
        disconnect()
        val events = Channel<WindowEvent>(Channel.CONFLATED)
        incoming = events
        preview.connectionChanged(true)
        worker = CoroutineScope(io).launch {
            var canActAt = 0L
            var deadline: Job? = null
            for (event in events) {
                try {
                    deadline?.cancel()
                    val now = SystemClock.elapsedRealtime()
                    val action = if (now < canActAt) FeedAction.Allow(canActAt - now) else handle(event, now)
                    when (action) {
                        is FeedAction.Allow -> if (action.checkAfterMillis < Long.MAX_VALUE) {
                            deadline = launch {
                                kotlinx.coroutines.delay(action.checkAfterMillis)
                                if (event.generation == generation.get()) {
                                    events.trySend(event.copy(kind = DetectionEvent.ContentChanged))
                                }
                            }
                        }

                        FeedAction.Exit, FeedAction.Pause -> canActAt = SystemClock.elapsedRealtime() + COOLDOWN_MS

                        null -> Unit
                    }
                } catch (_: SecurityException) {
                    // Android can revoke accessibility access during an event.
                } catch (_: IllegalStateException) {
                    // A window can disappear while its nodes are being visited.
                } catch (_: IOException) {
                    // No action without a readable consent and policy.
                }
            }
        }
    }

    private suspend fun handle(event: WindowEvent, now: Long): FeedAction? {
        val interactive = getSystemService(android.os.PowerManager::class.java)?.isInteractive == true
        if (!interactive || !preview.enabled.first() || !guard.shouldBlock(event) { rootInActiveWindow }) {
            session.leave()
            return null
        }
        val action = session.visit(event.generation, now, preview.remainingPass())
        return when {
            action is FeedAction.Allow -> action

            exitViewer(event) -> {
                session.exited()
                if (action == FeedAction.Pause) {
                    startActivity(Intent(PAUSE_ACTION).setPackage(packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                action
            }

            else -> null
        }
    }

    private suspend fun exitViewer(event: WindowEvent): Boolean =
        kotlinx.coroutines.currentCoroutineContext().isActive && event.generation == generation.get() &&
            preview.enabled.first() && guard.shouldBlock(event) { rootInActiveWindow } &&
            stillActive(event) && performGlobalAction(GLOBAL_ACTION_BACK)

    private fun stillActive(event: WindowEvent): Boolean {
        val root = rootInActiveWindow ?: return false
        return try {
            root.windowId == event.window && root.packageName?.toString() == event.app.packageName
        } finally {
            root.recycle()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName?.toString() != ProtectedApp.YouTube.packageName) return
        val kind = when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> DetectionEvent.WindowStateChanged
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> DetectionEvent.ContentChanged
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> DetectionEvent.ViewScrolled
            else -> return
        }
        if (event.windowId != windowId || kind == DetectionEvent.WindowStateChanged) {
            windowId = event.windowId
            generation.incrementAndGet()
        }
        incoming?.trySend(WindowEvent(ProtectedApp.YouTube, event.windowId, kind, generation.get()))
    }

    override fun onInterrupt() {
        generation.incrementAndGet()
    }
    override fun onUnbind(intent: Intent?): Boolean {
        disconnect()
        return super.onUnbind(intent)
    }
    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    private fun disconnect() {
        generation.incrementAndGet()
        worker?.cancel()
        incoming?.close()
        incoming = null
        preview.connectionChanged(false)
    }

    private companion object {
        const val PAUSE_ACTION = "io.github.uveshm003.unreel.PAUSE_SHORTS"
        const val COOLDOWN_MS = 1_200L
    }
}
