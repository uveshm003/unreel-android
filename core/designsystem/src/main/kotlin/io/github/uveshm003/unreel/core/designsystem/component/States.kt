package io.github.uveshm003.unreel.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.uveshm003.unreel.core.designsystem.R

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(title, modifier = modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
}

@Composable
fun EmptyState(title: String, description: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title)
        Text(
            description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

enum class StatusKind { Ok, Warning, Error }

@Composable
fun StatusBanner(status: StatusKind, message: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (icon, container, foreground) = when (status) {
        StatusKind.Ok -> Triple(R.drawable.status_ok, colors.primaryContainer, colors.onPrimaryContainer)
        StatusKind.Warning -> Triple(R.drawable.status_warning, colors.tertiaryContainer, colors.onTertiaryContainer)
        StatusKind.Error -> Triple(R.drawable.status_error, colors.errorContainer, colors.onErrorContainer)
    }
    Surface(modifier = modifier, color = container, contentColor = foreground, shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(painterResource(icon), contentDescription = null)
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
