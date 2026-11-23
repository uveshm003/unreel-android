package io.github.uveshm003.unreel.core.designsystem.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class ThemeScreenshotTest : ScreenshotSuite("theme") {
    @Composable
    override fun Screen() {
        val colors = MaterialTheme.colorScheme
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Unreel", style = MaterialTheme.typography.displaySmall)
                Text("Protection", style = MaterialTheme.typography.headlineMedium)
                Text("Time spent in short-form feeds", style = MaterialTheme.typography.bodyLarge)
                for ((label, background, foreground) in listOf(
                    Triple("Primary", colors.primary, colors.onPrimary),
                    Triple("Secondary", colors.secondary, colors.onSecondary),
                    Triple("Tertiary", colors.tertiary, colors.onTertiary),
                    Triple("Error", colors.error, colors.onError),
                    Triple("Surface container", colors.surfaceContainer, colors.onSurface),
                )) {
                    Surface(color = background, contentColor = foreground, shape = MaterialTheme.shapes.medium) {
                        Text(
                            label,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
