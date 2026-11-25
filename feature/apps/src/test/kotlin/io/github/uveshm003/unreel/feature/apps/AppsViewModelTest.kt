package io.github.uveshm003.unreel.feature.apps

import android.content.Context
import android.content.pm.PackageInfo
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.github.uveshm003.unreel.core.common.result.Outcome
import io.github.uveshm003.unreel.core.data.ProtectionChangeError
import io.github.uveshm003.unreel.core.data.ProtectionSettingsRepository
import io.github.uveshm003.unreel.core.model.AppProtection
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.model.ProtectionLevel
import io.github.uveshm003.unreel.core.testing.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AppsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repo = FakePolicies()
    private val instagram = ProtectedApp.Instagram

    @Test
    fun updatesTheSelectedAppAndReflectsInstalledPackagesOnResume() = runTest {
        val vm = AppsViewModel(repo, InstalledApps(context, main.testDispatcher))
        try {
            vm.uiState.test {
                assertEquals(AppProtection(instagram.packageName), awaitItem().policies[instagram.packageName])
                shadowOf(context.packageManager).installPackage(
                    PackageInfo().apply {
                        packageName =
                            instagram.packageName
                    },
                )
                vm.refreshInstalled()
                assertEquals(setOf(instagram), awaitItem().installed)
                vm.setLevel(instagram, ProtectionLevel.Budget)
                assertEquals(ProtectionLevel.Budget, awaitItem().policies.getValue(instagram.packageName).level)
                vm.setBudget(instagram, 35)
                assertEquals(35, awaitItem().policies.getValue(instagram.packageName).dailyBudgetMinutes)
                vm.setAllowOne(instagram, false)
                val last = awaitItem()
                assertFalse(last.policies.getValue(instagram.packageName).allowOneBlockScroll)
                assertEquals(
                    AppProtection(ProtectedApp.YouTube.packageName),
                    last.policies[ProtectedApp.YouTube.packageName],
                )
            }
        } finally {
            vm.viewModelScope.cancel()
        }
    }

    @Test
    fun readCanBeRetriedAndRejectionsAreOneOff() = runTest {
        repo.readFails = true
        val vm = AppsViewModel(repo, InstalledApps(context, main.testDispatcher))
        try {
            assertTrue(vm.uiState.value.readFailed)
            repo.readFails = false
            vm.retry()
            assertFalse(vm.uiState.value.readFailed)
            vm.errors.test {
                vm.setBudget(instagram, 241)
                assertEquals(R.string.apps_invalid_budget, awaitItem())
                expectNoEvents()
            }
            assertEquals(15, vm.uiState.value.policies.getValue(instagram.packageName).dailyBudgetMinutes)
        } finally {
            vm.viewModelScope.cancel()
        }
    }

    private class FakePolicies : ProtectionSettingsRepository {
        private val values = MutableStateFlow(
            ProtectedApp.entries.associate {
                it.packageName to
                    AppProtection(it.packageName)
            },
        )
        var readFails = false
        override val settings = flow {
            if (readFails) throw IOException()
            emitAll(values)
        }
        override suspend fun setBudget(packageName: String, minutes: Int): Outcome<Unit, ProtectionChangeError> {
            if (minutes !in AppProtection.budgetRange) return Outcome.Failure(ProtectionChangeError.InvalidBudget)
            return update(packageName) { it.copy(dailyBudgetMinutes = minutes) }
        }
        override suspend fun setLevel(packageName: String, level: ProtectionLevel) =
            update(packageName) { it.copy(level = level) }
        override suspend fun setAllowOne(packageName: String, enabled: Boolean) =
            update(packageName) { it.copy(allowOneBlockScroll = enabled) }

        private fun update(
            name: String,
            change: (AppProtection) -> AppProtection,
        ): Outcome<Unit, ProtectionChangeError> {
            values.value = values.value + (name to change(values.value.getValue(name)))
            return Outcome.Success(Unit)
        }
    }
}
