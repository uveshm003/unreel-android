package io.github.uveshm003.unreel

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.github.uveshm003.unreel.core.data.FeedPreviewRepository
import io.github.uveshm003.unreel.core.designsystem.component.PrimaryButton
import io.github.uveshm003.unreel.core.designsystem.component.SecondaryButton
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import javax.inject.Inject
import kotlinx.coroutines.delay

@AndroidEntryPoint
class PauseActivity : ComponentActivity() {
    @Inject lateinit var preview: FeedPreviewRepository
    private var readyAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readyAt = savedInstanceState?.getLong("readyAt") ?: (SystemClock.elapsedRealtime() + PAUSE)
        setContent {
            var remaining by remember { mutableLongStateOf((readyAt - SystemClock.elapsedRealtime()).coerceAtLeast(0)) }
            LaunchedEffect(readyAt) {
                while (remaining > 0) {
                    delay(minOf(remaining, SECOND))
                    remaining = (readyAt - SystemClock.elapsedRealtime()).coerceAtLeast(0)
                }
            }
            UnreelTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(stringResource(R.string.pause_title), style = MaterialTheme.typography.headlineLarge)
                        Text(stringResource(R.string.pause_body))
                        PrimaryButton(stringResource(R.string.pause_leave), ::finish)
                        val label = if (remaining >
                            0
                        ) {
                            stringResource(R.string.pause_wait, (remaining + SECOND - 1) / SECOND)
                        } else {
                            stringResource(R.string.pause_allow)
                        }
                        SecondaryButton(label, {
                            preview.allowFiveMinutes()
                            finish()
                        }, enabled = remaining == 0L)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("readyAt", readyAt)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val SECOND = 1_000L
        const val PAUSE = 10 * SECOND
    }
}
