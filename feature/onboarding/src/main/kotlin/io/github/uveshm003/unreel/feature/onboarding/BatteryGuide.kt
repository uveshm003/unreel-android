package io.github.uveshm003.unreel.feature.onboarding

internal enum class BatteryGuide(val instructions: Int, page: String) {
    Xiaomi(R.string.battery_xiaomi, "xiaomi"),
    Oppo(R.string.battery_oppo_realme, "oppo"),
    Realme(R.string.battery_oppo_realme, "realme"),
    Vivo(R.string.battery_vivo, "vivo"),
    OnePlus(R.string.battery_oneplus, "oneplus"),
    Samsung(R.string.battery_samsung, "samsung"),
    Generic(R.string.battery_generic, ""),
    ;

    val url = "https://dontkillmyapp.com/$page"

    companion object {
        fun forDevice(manufacturer: String, brand: String): BatteryGuide =
            lookup(brand) ?: lookup(manufacturer) ?: Generic

        private fun lookup(name: String): BatteryGuide? = when (name.trim().lowercase()) {
            "xiaomi", "redmi", "poco" -> Xiaomi
            "oppo" -> Oppo
            "realme" -> Realme
            "vivo" -> Vivo
            "oneplus" -> OnePlus
            "samsung" -> Samsung
            else -> null
        }
    }
}
