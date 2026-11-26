package io.github.uveshm003.unreel.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AppProtectionTest {
    @Test
    fun supportedPackagesHaveIndependentConservativeDefaults() {
        assertEquals(
            listOf("com.instagram.android", "com.google.android.youtube"),
            ProtectedApp.entries.map {
                it.packageName
            },
        )
        for (app in ProtectedApp.entries) {
            assertEquals(app, ProtectedApp.fromPackage(app.packageName))
            assertEquals(
                AppProtection(app.packageName, ProtectionLevel.Pause, 15, true),
                AppProtection(app.packageName),
            )
        }
        assertNull(ProtectedApp.fromPackage("unknown"))
        assertEquals(1..240, AppProtection.budgetRange)
    }

    @Test
    fun readsStableKeysAndSurfacesAnUnrecognisedStoredPolicy() {
        val keys = listOf("off", "pause", "budget", "hard_block")
        assertEquals(keys, ProtectionLevel.entries.map { it.storageKey })
        assertEquals(ProtectionLevel.entries, keys.map(ProtectionLevel::fromStorage))
        assertThrows(IllegalStateException::class.java) { ProtectionLevel.fromStorage("unknown") }
    }
}
