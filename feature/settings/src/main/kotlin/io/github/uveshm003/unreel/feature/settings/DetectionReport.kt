package io.github.uveshm003.unreel.feature.settings

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

internal enum class ReportApp(val formValue: String, val packageName: String?) {
    Instagram("Instagram", "com.instagram.android"),
    YouTube("YouTube", "com.google.android.youtube"),
    Other("Other", null),
}

internal data class DetectionReport(
    val app: ReportApp,
    val appVersion: String?,
    val unreelVersion: String,
    val device: String,
) {
    fun issueUrl(): Uri = Uri.parse("https://github.com/uveshm003/unreel-android/issues/new").buildUpon()
        .appendQueryParameter("template", "detection.yml")
        .appendQueryParameter("app", app.formValue)
        .apply { if (!appVersion.isNullOrBlank()) appendQueryParameter("app_version", appVersion) }
        .appendQueryParameter("unreel_version", unreelVersion)
        .appendQueryParameter("device", device)
        .build()
}

internal fun ReportApp.installedVersion(packages: PackageManager): String? {
    val name = packageName ?: return null
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packages.getPackageInfo(name, PackageManager.PackageInfoFlags.of(0)).versionName
        } else {
            packages.getPackageInfo(name, 0).versionName
        }
    } catch (_: PackageManager.NameNotFoundException) {
        null
    } catch (_: SecurityException) {
        null
    }
}
