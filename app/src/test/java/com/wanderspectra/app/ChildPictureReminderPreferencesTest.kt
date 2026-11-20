package com.wanderspectra.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ChildPictureReminderPreferencesTest {

    @Test
    fun testOneMonthMsValue() {
        // 30 days * 24 hours * 60 minutes * 60 seconds * 1000 ms
        val expected = 30L * 24L * 60L * 60L * 1000L
        assertEquals(expected, ChildPictureReminderPreferences.ONE_MONTH_MS)
        assertEquals(2_592_000_000L, ChildPictureReminderPreferences.ONE_MONTH_MS)
    }
}
