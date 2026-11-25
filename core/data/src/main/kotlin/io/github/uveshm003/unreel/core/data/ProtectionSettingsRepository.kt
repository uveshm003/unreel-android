package io.github.uveshm003.unreel.core.data

import io.github.uveshm003.unreel.core.common.result.Outcome
import io.github.uveshm003.unreel.core.database.AppProtectionRow
import io.github.uveshm003.unreel.core.database.ProtectionDao
import io.github.uveshm003.unreel.core.model.AppProtection
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.model.ProtectionLevel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

enum class ProtectionChangeError { UnknownPackage, InvalidBudget }

interface ProtectionSettingsRepository {
    val settings: Flow<Map<String, AppProtection>>
    suspend fun setLevel(packageName: String, level: ProtectionLevel): Outcome<Unit, ProtectionChangeError>
    suspend fun setBudget(packageName: String, minutes: Int): Outcome<Unit, ProtectionChangeError>
    suspend fun setAllowOne(packageName: String, enabled: Boolean): Outcome<Unit, ProtectionChangeError>
}

internal class RoomProtectionSettingsRepository @Inject constructor(private val dao: ProtectionDao) :
    ProtectionSettingsRepository {
    override val settings = dao.observe().map { rows ->
        val byPackage = rows.associateBy { it.packageName }
        ProtectedApp.entries.associate { app ->
            app.packageName to (byPackage[app.packageName]?.toModel() ?: AppProtection(app.packageName))
        }
    }.distinctUntilChanged()

    override suspend fun setLevel(packageName: String, level: ProtectionLevel) =
        edit(packageName) { it.copy(level = level) }

    override suspend fun setBudget(packageName: String, minutes: Int): Outcome<Unit, ProtectionChangeError> {
        if (minutes !in AppProtection.budgetRange) return Outcome.Failure(ProtectionChangeError.InvalidBudget)
        return edit(packageName) { it.copy(dailyBudgetMinutes = minutes) }
    }

    override suspend fun setAllowOne(packageName: String, enabled: Boolean) =
        edit(packageName) { it.copy(allowOneBlockScroll = enabled) }

    private suspend fun edit(
        packageName: String,
        change: (AppProtection) -> AppProtection,
    ): Outcome<Unit, ProtectionChangeError> {
        if (ProtectedApp.fromPackage(packageName) == null) return Outcome.Failure(ProtectionChangeError.UnknownPackage)
        dao.edit(packageName) { row -> change(row?.toModel() ?: AppProtection(packageName)).toRow() }
        return Outcome.Success(Unit)
    }

    private fun AppProtectionRow.toModel(): AppProtection {
        check(dailyBudgetMinutes in AppProtection.budgetRange) { "The stored budget is outside the supported range" }
        return AppProtection(packageName, ProtectionLevel.fromStorage(level), dailyBudgetMinutes, allowOneBlockScroll)
    }

    private fun AppProtection.toRow() =
        AppProtectionRow(packageName, level.storageKey, dailyBudgetMinutes, allowOneBlockScroll)
}
