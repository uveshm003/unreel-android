package io.github.uveshm003.unreel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedShortTest {
    @Test
    fun acceptsOnlyOneShortFromYouTube() {
        val id = "k31_U7Y1-H8"
        assertEquals(id, sharedShortId("https://www.youtube.com/shorts/$id?si=example"))
        assertEquals(id, sharedShortId(" https://m.youtube.com/shorts/$id "))
        for (input in listOf(
            null,
            "https://other.test/shorts/$id",
            "https://youtube.com.evil.test/shorts/$id",
            "https://youtube.com/watch?v=$id",
            "https://youtube.com/shorts/$id extra",
        )) {
            assertNull(sharedShortId(input))
        }
    }
}
