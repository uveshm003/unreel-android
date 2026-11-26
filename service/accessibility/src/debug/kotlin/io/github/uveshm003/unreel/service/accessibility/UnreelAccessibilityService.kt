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

    @Inject lateinit var guard: FeedGuard

    @Inject
    @Dispatcher(UnreelDispatchers.IO)
    lateinit var io: CoroutineDispatcher
    private var incoming: Channel<WindowEvent>? = null
    private var worker: Job? = null
    private val generation = AtomicLong()

    override fun onServiceConnected() {
        super.onServiceConnected()
        disconnect()
        val events = Channel<WindowEvent>(Channel.CONFLATED)
        incoming = events
        preview.connectionChanged(true)
        worker = CoroutineScope(io).launch {
            var canActAt = 0L
            for (event in events) {
                try {
                    if (SystemClock.elapsedRealtime() >= canActAt && tryBlock(event)) {
                        canActAt = SystemClock.elapsedRealtime() + COOLDOWN_MS
                    }
                } catch (_: SecurityException) {
                    // Android can revoke access while a window is being examined.
                } catch (_: IllegalStateException) {
                    // A window may disappear during a traversal.
                } catch (_: IOException) {
                    // An unreadable consent record leaves this window alone.
                }
            }
        }
    }

    private suspend fun tryBlock(event: WindowEvent): Boolean {
        val detected = preview.enabled.first() && guard.shouldBlock(event) { rootInActiveWindow }
        if (!detected || !preview.enabled.first()) return false
        val current = kotlinx.coroutines.currentCoroutineContext().isActive && event.generation == generation.get()
        return current && stillActive(event) && performGlobalAction(GLOBAL_ACTION_BACK)
    }

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
        incoming?.trySend(WindowEvent(ProtectedApp.YouTube, event.windowId, kind, generation.incrementAndGet()))
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
        const val COOLDOWN_MS = 1_200L
    }
}
