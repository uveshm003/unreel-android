package io.github.uveshm003.unreel.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import org.junit.Test
import org.robolectric.annotation.Config

abstract class ComponentScreenshotSuite(name: String) : ScreenshotSuite(name) {
    @Composable
    final override fun Screen() {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.CenterStart) { Example() }
        }
    }

    @Composable
    protected abstract fun Example()

    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun largeFont() = capture("phone_light_large_font", darkTheme = false, fontScale = 2f)
}

class PrimaryButtonScreenshotTest : ComponentScreenshotSuite("primary_button") {
    @Composable
    override fun Example() {
        PrimaryButton("Enable protection", onClick = {})
    }
}

class SecondaryButtonScreenshotTest : ComponentScreenshotSuite("secondary_button") {
    @Composable
    override fun Example() {
        SecondaryButton("Change schedule", onClick = {})
    }
}

class UnreelCardScreenshotTest : ComponentScreenshotSuite("unreel_card") {
    @Composable
    override fun Example() {
        UnreelCard { Text("Daily budget", modifier = Modifier.padding(16.dp)) }
    }
}

class SettingsRowScreenshotTest : ComponentScreenshotSuite("settings_row") {
    @Composable
    override fun Example() {
        Column {
            SettingsRow("Pause before scrolling", true, {}, subtitle = "Take a breath before opening the feed")
            SettingsRow("Daily budget", "15 minutes", {})
        }
    }
}

class SectionHeaderScreenshotTest : ComponentScreenshotSuite("section_header") {
    @Composable
    override fun Example() {
        SectionHeader("Protection settings")
    }
}

class EmptyStateScreenshotTest : ComponentScreenshotSuite("empty_state") {
    @Composable
    override fun Example() {
        EmptyState("No schedules yet", "Add a time window to increase protection.")
    }
}

class StatusBannerScreenshotTest : ComponentScreenshotSuite("status_banner") {
    @Composable
    override fun Example() {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBanner(StatusKind.Ok, "Protection is on")
            StatusBanner(StatusKind.Warning, "Protection is paused")
            StatusBanner(StatusKind.Error, "Accessibility service is off")
        }
    }
}
