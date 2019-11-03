package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.utils.TimeRangeCalculateUtils.Companion.hourInRange
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by SymeonChen on 2019-11-03.
 */
class TimeRangeCalculateUtilsTest {
    @Test
    fun hourInRangeTest() {
        assertEquals(hourInRange(23, 22, 4), true)
        assertEquals(hourInRange(0, 22, 4), true)
        assertEquals(hourInRange(22, 22, 4), true)
        assertEquals(hourInRange(4, 22, 4), true)

        assertEquals(hourInRange(21, 22, 4), false)
        assertEquals(hourInRange(5, 22, 4), false)

        assertEquals(hourInRange(2, 1, 4), true)
        assertEquals(hourInRange(3, 1, 4), true)
        assertEquals(hourInRange(1, 1, 4), true)
        assertEquals(hourInRange(4, 1, 4), true)

        assertEquals(hourInRange(21, 1, 4), false)
        assertEquals(hourInRange(5, 1, 4), false)


        assertEquals(hourInRange(4, 1, 1), true)
        assertEquals(hourInRange(1, 1, 1), true)

    }

}