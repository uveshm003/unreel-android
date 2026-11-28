package io.github.uveshm003.unreel.feature.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryGuideTest {
    @Test
    fun mapsShippingBrandsAndPrefersTheRetailBrandOnSharedHardware() {
        val cases = mapOf(
            " Xiaomi " to "xiaomi",
            "Redmi" to "xiaomi",
            "POCO" to "xiaomi",
            "OPPO" to "oppo",
            "realme" to "realme",
            "vivo" to "vivo",
            "OnePlus" to "oneplus",
            "SAMSUNG" to "samsung",
        )
        for ((brand, page) in cases) {
            assertEquals("https://dontkillmyapp.com/$page", BatteryGuide.forDevice("unknown", brand).url)
        }
        assertEquals(BatteryGuide.Realme, BatteryGuide.forDevice("OPPO", "realme"))
        assertEquals(BatteryGuide.Samsung, BatteryGuide.forDevice("samsung", "unknown"))
    }

    @Test
    fun unknownOrMissingManufacturerGetsGeneralGuidance() {
        assertEquals(BatteryGuide.Generic, BatteryGuide.forDevice("unknown", ""))
        assertEquals("https://dontkillmyapp.com/", BatteryGuide.forDevice("", "").url)
    }
}
