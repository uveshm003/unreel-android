package io.github.uveshm003.unreel.service.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.uveshm003.unreel.core.data.PreviewCompatibility
import io.github.uveshm003.unreel.core.data.PreviewDevice
import io.github.uveshm003.unreel.core.detection.RulesParser
import io.github.uveshm003.unreel.core.model.ProtectedApp
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AndroidPreviewDevice @Inject constructor(@ApplicationContext private val context: Context) :
    PreviewDevice {
    private val pkg = ProtectedApp.YouTube.packageName
    val rules by lazy {
        try {
            context.assets.open("$pkg.json").bufferedReader().use { RulesParser.parse(it.readText()) }
                .takeIf { it.packageName == pkg }
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override fun compatibility(): PreviewCompatibility {
        val file = rules ?: return PreviewCompatibility.Unavailable
        return try {
            val info = context.packageManager.getPackageInfo(pkg, 0)
            val version = info.longVersionCode
            if (file.rules.any { it.versionCode?.contains(version) == true }) {
                PreviewCompatibility.Supported(version)
            } else {
                PreviewCompatibility.Unsupported(info.versionName, version)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            PreviewCompatibility.Missing
        } catch (_: SecurityException) {
            PreviewCompatibility.Unavailable
        }
    }

    override fun serviceEnabled(): Boolean? = try {
        val component = ComponentName(context, UnreelAccessibilityService::class.java)
        context.getSystemService(AccessibilityManager::class.java)
            ?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            ?.any { ComponentName.unflattenFromString(it.id) == component }
    } catch (_: SecurityException) {
        null
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal interface PreviewDeviceModule {
    @Binds fun device(implementation: AndroidPreviewDevice): PreviewDevice
}
