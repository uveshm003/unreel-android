package io.github.uveshm003.unreel.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.uveshm003.unreel.core.designsystem.component.EmptyState

@Composable
fun DestinationPlaceholder(title: String, description: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            EmptyState(title, description, modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth())
        }
    }
}
