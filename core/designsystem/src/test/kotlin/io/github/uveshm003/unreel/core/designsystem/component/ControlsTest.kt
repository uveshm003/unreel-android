package io.github.uveshm003.unreel.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ControlsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun tappingTheRowTogglesItsSingleSwitch() {
        compose.setContent {
            var enabled by remember { mutableStateOf(false) }
            SettingsRow("Protection", enabled, { enabled = it })
        }
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).assertCountEquals(1)
        val row = compose.onNodeWithText("Protection").assertHeightIsAtLeast(48.dp).assertIsOff()
        row.performTouchInput { click(Offset(2f, centerY)) }
        row.assertIsOn().performClick().assertIsOff()
    }

    @Test
    fun buttonsInvokeTheirOwnCallbacks() {
        val clicks = mutableListOf<String>()
        compose.setContent {
            Column {
                PrimaryButton("Enable", { clicks += "primary" })
                SecondaryButton("Cancel", { clicks += "secondary" })
                SettingsRow("Budget", "15 minutes", { clicks += "value" })
            }
        }
        for (label in listOf("Enable", "Cancel", "Budget")) {
            compose.onNodeWithText(label).assertHeightIsAtLeast(48.dp).performClick()
        }
        assertEquals(listOf("primary", "secondary", "value"), clicks)
    }
}
