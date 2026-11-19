package io.github.uveshm003.unreel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import io.github.uveshm003.unreel.ui.UnreelApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnreelTheme {
                UnreelApp()
            }
        }
    }
}
