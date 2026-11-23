package io.github.uveshm003.unreel.core.designsystem.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class BrandColorsTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    @Config(sdk = [30])
    fun requestingDynamicColorBeforeAndroid12UsesBrandColors() {
        assertSame(LightBrandColors, unreelColors(context, darkTheme = false, dynamicColor = true))
        assertSame(DarkBrandColors, unreelColors(context, darkTheme = true, dynamicColor = true))
    }

    @Test
    fun disablingDynamicColorUsesTheMatchingBrandScheme() {
        assertSame(LightBrandColors, unreelColors(context, darkTheme = false, dynamicColor = false))
        assertSame(DarkBrandColors, unreelColors(context, darkTheme = true, dynamicColor = false))
    }

    @Test
    fun brandTextMeetsBodyTextContrast() {
        listOf(LightBrandColors, DarkBrandColors).forEach { scheme ->
            scheme.textPairs().forEach { (background, foreground) ->
                val a = background.luminance()
                val b = foreground.luminance()
                val ratio = (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
                assertTrue("Expected at least 4.5:1 for $background and $foreground, got $ratio", ratio >= 4.5f)
            }
        }
    }

    private fun ColorScheme.textPairs() = listOf(
        primary to onPrimary, primaryContainer to onPrimaryContainer,
        secondary to onSecondary, secondaryContainer to onSecondaryContainer,
        tertiary to onTertiary, tertiaryContainer to onTertiaryContainer,
        error to onError, errorContainer to onErrorContainer,
        surfaceDim to onSurface, surfaceBright to onSurface,
        surface to onSurface, surfaceVariant to onSurfaceVariant, inverseSurface to inverseOnSurface,
        surfaceContainerLowest to onSurface, surfaceContainerLow to onSurface,
        surfaceContainer to onSurface, surfaceContainerHigh to onSurface, surfaceContainerHighest to onSurface,
        primaryFixed to onPrimaryFixed, primaryFixedDim to onPrimaryFixedVariant,
        secondaryFixed to onSecondaryFixed, secondaryFixedDim to onSecondaryFixedVariant,
        tertiaryFixed to onTertiaryFixed, tertiaryFixedDim to onTertiaryFixedVariant,
    )
}
