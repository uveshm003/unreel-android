package io.github.uveshm003.unreel

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.uveshm003.unreel.core.data.ProtectionActivity
import io.github.uveshm003.unreel.core.data.ProtectionSettingsRepository
import io.github.uveshm003.unreel.core.designsystem.component.PrimaryButton
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.ui.OverviewPage
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Only a user-selected, single YouTube Shorts URL is accepted. Its identifier stays in memory. */
@AndroidEntryPoint
class SharedShortActivity : ComponentActivity() {
    @Inject lateinit var policies: ProtectionSettingsRepository

    @Inject lateinit var activity: ProtectionActivity
    private var error by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            sharedShortId(intent.getStringExtra(Intent.EXTRA_TEXT))
        } else {
            null
        }
        setContent {
            UnreelTheme {
                val description = if (id == null) R.string.shared_invalid else R.string.shared_body
                OverviewPage(getString(R.string.shared_title), getString(description)) {
                    if (id != null) {
                        PrimaryButton(getString(R.string.shared_play), { open(id) })
                    }
                    if (error) Text(getString(R.string.shared_error), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    private fun open(id: String) {
        lifecycleScope.launch {
            try {
                val policy = policies.settings.first().getValue(ProtectedApp.YouTube.packageName)
                val single = policy.allowOneBlockScroll && !activity.window.first().contains(activity.now())
                val path = if (single) "watch?v=$id" else "shorts/$id"
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/$path"))
                        .setPackage(ProtectedApp.YouTube.packageName),
                )
                finish()
            } catch (_: ActivityNotFoundException) {
                error = true
            } catch (_: IOException) {
                error = true
            } catch (_: SecurityException) {
                error = true
            }
        }
    }
}

private val shortUrl =
    Regex("""https://(?:(?:www|m)\.)?youtube\.com/shorts/([A-Za-z0-9_-]{11})/?(?:\?[^\s#]*)?(?:#[^\s]*)?""")
internal fun sharedShortId(value: String?): String? =
    value?.trim()?.let { shortUrl.matchEntire(it)?.groupValues?.get(1) }
