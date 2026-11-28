package io.github.uveshm003.unreel.core.common.result

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test

class OutcomeTest {
    private val success: Outcome<Int, String> = Outcome.Success(2)
    private val failure: Outcome<Int, String> = Outcome.Failure("locked")

    @Test
    fun accessors_reflect_the_variant() {
        assertTrue(success.isSuccess)
        assertEquals(2, success.getOrNull())
        assertNull(success.errorOrNull())

        assertFalse(failure.isSuccess)
        assertNull(failure.getOrNull())
        assertEquals("locked", failure.errorOrNull())
    }

    @Test
    fun map_transforms_only_success() {
        assertEquals<Outcome<Int, String>>(Outcome.Success(4), success.map { it * 2 })
        assertEquals(failure, failure.map { it * 2 })
    }

    @Test
    fun mapError_transforms_only_failure() {
        assertEquals<Outcome<Int, Int>>(Outcome.Success(2), success.mapError { it.length })
        assertEquals<Outcome<Int, Int>>(Outcome.Failure(6), failure.mapError { it.length })
    }

    @Test
    fun fold_picks_the_matching_branch() {
        assertEquals("ok 2", success.fold({ "ok $it" }, { "err $it" }))
        assertEquals("err locked", failure.fold({ "ok $it" }, { "err $it" }))
    }

    @Test
    fun onSuccess_and_onFailure_run_only_for_their_variant_and_return_the_same_outcome() {
        val seen = mutableListOf<String>()

        val s = success.onSuccess { seen += "success $it" }.onFailure { seen += "failure $it" }
        val f = failure.onSuccess { seen += "success $it" }.onFailure { seen += "failure $it" }

        assertEquals(listOf("success 2", "failure locked"), seen)
        assertEquals(success, s)
        assertEquals(failure, f)
    }
}
