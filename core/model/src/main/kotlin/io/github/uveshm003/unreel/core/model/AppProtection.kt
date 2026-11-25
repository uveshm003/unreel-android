package io.github.uveshm003.unreel.core.model

enum class ProtectedApp(val packageName: String) {
    Instagram("com.instagram.android"),
    YouTube("com.google.android.youtube"),
    ;

    companion object {
        fun fromPackage(name: String): ProtectedApp? = entries.find { it.packageName == name }
    }
}

enum class ProtectionLevel(val storageKey: String) {
    Off("off"),
    Pause("pause"),
    Budget("budget"),
    HardBlock("hard_block"),
    ;

    companion object {
        fun fromStorage(key: String): ProtectionLevel = checkNotNull(entries.find { it.storageKey == key }) {
            "The stored protection level cannot be read"
        }
    }
}

data class AppProtection(
    val packageName: String,
    val level: ProtectionLevel = ProtectionLevel.Pause,
    val dailyBudgetMinutes: Int = 15,
    val allowOneBlockScroll: Boolean = true,
) {
    companion object {
        val budgetRange = 1..240
    }
}
