package io.github.uveshm003.unreel.feature.onboarding

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.uveshm003.unreel.core.designsystem.component.SecondaryButton

@Composable
internal fun BatteryGuidance() {
    val context = LocalContext.current
    val guide = remember { BatteryGuide.forDevice(Build.MANUFACTURER, Build.BRAND) }
    val power = remember(context) { checkNotNull(context.getSystemService(PowerManager::class.java)) }
    var exempt by remember(power) { mutableStateOf(power.isIgnoringBatteryOptimizations(context.packageName)) }
    LifecycleResumeEffect(context) {
        exempt = power.isIgnoringBatteryOptimizations(context.packageName)
        onPauseOrDispose { }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(if (exempt) R.string.battery_exempt else R.string.battery_optimised),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
        )
        Text(stringResource(guide.instructions))
        SecondaryButton(stringResource(R.string.battery_open_settings), {
            context.openGuideIntent(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                R.string.battery_no_settings,
            )
        })
        SecondaryButton(stringResource(R.string.battery_learn_more), {
            context.openGuideIntent(Intent(Intent.ACTION_VIEW, guide.url.toUri()), R.string.battery_no_browser)
        })
    }
}

private fun Context.openGuideIntent(intent: Intent, fallback: Int) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, fallback, Toast.LENGTH_LONG).show()
    } catch (_: SecurityException) {
        Toast.makeText(this, fallback, Toast.LENGTH_LONG).show()
    }
}
