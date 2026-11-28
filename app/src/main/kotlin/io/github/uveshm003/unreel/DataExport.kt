package io.github.uveshm003.unreel

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.uveshm003.unreel.core.common.dispatchers.Dispatcher
import io.github.uveshm003.unreel.core.common.dispatchers.UnreelDispatchers
import io.github.uveshm003.unreel.core.data.AppearanceRepository
import io.github.uveshm003.unreel.core.data.ProtectionActivity
import io.github.uveshm003.unreel.core.data.ProtectionSettingsRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal class DataExport @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activity: ProtectionActivity,
    private val policies: ProtectionSettingsRepository,
    private val appearance: AppearanceRepository,
    @Dispatcher(UnreelDispatchers.IO) private val io: CoroutineDispatcher,
) {
    suspend fun write(uri: Uri) = withContext(io) {
        val summary = activity.snapshot()
        val display = appearance.appearance.first()
        val apps = JSONArray()
        policies.settings.first().values.forEach {
            apps.put(
                JSONObject().put("package", it.packageName).put("level", it.level.storageKey)
                    .put("dailyBudgetMinutes", it.dailyBudgetMinutes).put("allowOne", it.allowOneBlockScroll),
            )
        }
        val days = JSONArray()
        summary.days.forEach {
            days.put(
                JSONObject().put("date", it.date.toString())
                    .put("exits", it.exits).put("viewingMillis", it.viewingMillis),
            )
        }
        val document = JSONObject().put("schemaVersion", 1).put("apps", apps).put("days", days)
            .put("appearance", JSONObject().put("theme", display.theme.key).put("dynamicColor", display.dynamicColor))
            .put(
                "schedule",
                JSONObject().put("enabled", summary.window.enabled)
                    .put("start", summary.window.start.toString()).put("end", summary.window.end.toString()),
            )
        val stream = context.contentResolver.openOutputStream(uri, "wt") ?: throw IOException("No output stream")
        stream.bufferedWriter().use { it.write(document.toString(2)) }
    }
}
