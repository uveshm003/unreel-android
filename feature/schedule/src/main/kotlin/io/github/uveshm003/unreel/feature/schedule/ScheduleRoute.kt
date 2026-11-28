package io.github.uveshm003.unreel.feature.schedule

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.data.FocusWindow
import io.github.uveshm003.unreel.core.designsystem.component.SettingsRow
import io.github.uveshm003.unreel.core.designsystem.component.UnreelCard
import io.github.uveshm003.unreel.core.ui.OverviewPage
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Serializable

@Serializable
data object ScheduleRoute : TopLevelDestination {
    override val label: Int get() = R.string.schedule_label
    override val icon: Int get() = R.drawable.ic_schedule
}

@Serializable
internal data object ScheduleOverview

fun NavGraphBuilder.scheduleScreen(
    window: () -> FocusWindow? = {
        FocusWindow()
    },
    enable: (Boolean) -> Unit = {},
    setTime: (Boolean, LocalTime) -> Unit = { _, _ -> },
) {
    navigation<ScheduleRoute>(startDestination = ScheduleOverview) {
        composable<ScheduleOverview> { ScheduleScreen(window(), enable, setTime) }
    }
}

@Composable
internal fun ScheduleScreen(window: FocusWindow?, enable: (Boolean) -> Unit, setTime: (Boolean, LocalTime) -> Unit) {
    OverviewPage(stringResource(ScheduleRoute.label), stringResource(R.string.schedule_description)) {
        if (window != null) {
            UnreelCard {
                SettingsRow(stringResource(R.string.schedule_daily), window.enabled, enable)
                TimeRow(true, window.start, setTime)
                TimeRow(false, window.end, setTime)
            }
            val detail = when {
                !window.enabled -> R.string.schedule_off
                window.start == window.end -> R.string.schedule_all_day
                window.start > window.end -> R.string.schedule_overnight
                else -> R.string.schedule_same_day
            }
            Text(stringResource(detail))
        }
    }
}

@Composable
private fun TimeRow(start: Boolean, time: LocalTime, setTime: (Boolean, LocalTime) -> Unit) {
    val context = LocalContext.current
    val use24 = DateFormat.is24HourFormat(context)
    val locale = LocalConfiguration.current.locales[0]
    val pattern = DateFormat.getBestDateTimePattern(locale, if (use24) "Hm" else "hm")
    val label = if (start) R.string.schedule_start else R.string.schedule_end
    SettingsRow(stringResource(label), time.format(DateTimeFormatter.ofPattern(pattern, locale)), {
        TimePickerDialog(
            context,
            { _, hour, minute -> setTime(start, LocalTime.of(hour, minute)) },
            time.hour,
            time.minute,
            use24,
        ).show()
    })
}
