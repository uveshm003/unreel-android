package io.github.uveshm003.unreel.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SetupStepTest {
    @Test
    fun savedKeysRemainReadableInSetupOrder() {
        val keys = listOf("welcome", "disclosure", "service", "guides", "complete")
        assertEquals(keys, SetupStep.entries.map { it.key })
        assertEquals(SetupStep.entries, keys.map { SetupStep.fromKey(it) })
    }

    @Test
    fun missingOrUnrecognizedProgressStartsAtWelcome() {
        assertEquals(SetupStep.Welcome, SetupStep.fromKey(null))
        assertEquals(SetupStep.Welcome, SetupStep.fromKey("unrecognized"))
    }
}
