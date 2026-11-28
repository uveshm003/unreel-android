package io.github.uveshm003.unreel.core.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.uveshm003.unreel.core.common.result.Outcome
import io.github.uveshm003.unreel.core.database.ProtectionDatabase
import io.github.uveshm003.unreel.core.model.AppProtection
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.model.ProtectionLevel
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.SQLiteMode

@RunWith(RobolectricTestRunner::class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class RoomProtectionSettingsRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "test-protection-${UUID.randomUUID()}.db"
    private var database = ProtectionDatabase.open(context, name)
    private var repo = RoomProtectionSettingsRepository(database.protection())
    private val instagram = ProtectedApp.Instagram.packageName
    private val youtube = ProtectedApp.YouTube.packageName

    @After
    fun close() {
        database.close()
        context.deleteDatabase(name)
    }

    @Test
    fun defaultsAndIndependentEditsSurviveClosingAndReopening() = runBlocking {
        assertEquals(
            mapOf(instagram to AppProtection(instagram), youtube to AppProtection(youtube)),
            repo.settings.first(),
        )
        val edits = listOf(
            async { repo.setLevel(instagram, ProtectionLevel.Budget) },
            async { repo.setBudget(instagram, 240) },
            async { repo.setAllowOne(instagram, false) },
            async { repo.setBudget(youtube, 1) },
        ).awaitAll()
        assertTrue(edits.all { it.isSuccess })
        reopen()
        assertEquals(
            mapOf(
                instagram to AppProtection(instagram, ProtectionLevel.Budget, 240, false),
                youtube to AppProtection(youtube, dailyBudgetMinutes = 1),
            ),
            repo.settings.first(),
        )
    }

    @Test
    fun invalidEditsReturnTypedErrorsWithoutCreatingRows() = runBlocking {
        for (minutes in listOf(-1, 0, 241, Int.MAX_VALUE)) {
            assertEquals(Outcome.Failure(ProtectionChangeError.InvalidBudget), repo.setBudget(instagram, minutes))
        }
        assertEquals(Outcome.Failure(ProtectionChangeError.UnknownPackage), repo.setAllowOne("unknown", false))
        assertEquals(emptyList<Any>(), database.protection().observe().first())
    }

    @Test
    fun anUnreadableFileIsPreservedAndARestoredBackupRecoversTheLastPolicy() = runBlocking {
        repo.setLevel(youtube, ProtectionLevel.HardBlock)
        database.close()
        val file = context.getDatabasePath(name)
        val backup = file.readBytes()
        val damaged = backup.clone().apply { "damaged database".toByteArray().copyInto(this) }
        file.writeBytes(damaged)
        reopen()
        val error = runCatching { repo.settings.first() }.exceptionOrNull()
        assertTrue("An unreadable file must fail the read", error != null)
        database.close()
        assertArrayEquals(damaged, file.readBytes())
        file.writeBytes(backup)
        reopen()
        assertEquals(ProtectionLevel.HardBlock, repo.settings.first().getValue(youtube).level)
    }

    private fun reopen() {
        database.close()
        database = ProtectionDatabase.open(context, name)
        repo = RoomProtectionSettingsRepository(database.protection())
    }
}
