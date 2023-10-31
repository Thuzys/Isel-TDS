package date_Structure

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class DateTest{
    @Test
    fun `Test of a Date`(){
        val date = Date(31, 5, 2009)
        assertEquals(31, date.day)
        assertEquals(5, date.month)
        assertEquals(2009, date.year)
    }
    @Test
    fun `Test with just the day`(){
        val date = Date(year = 1578)
        assertEquals(1, date.day)
        assertEquals(1, date.month)
        assertEquals(1578, date.year)
    }
    @Test
    fun `Out of range test`(){
        assertFailsWith<IllegalArgumentException> { Date(66, 5, 2001) }
        assertFailsWith<IllegalArgumentException> { Date(20, 90, 2023) }
        assertFailsWith<IllegalArgumentException> { Date(10, 5, 0) }
    }
    @Test
    fun `Is leap year`(){
        var date = Date(year = 2024)
        assertTrue(date.isLeapYear)
        date = Date(year = 2023)
        assertFalse(date.isLeapYear)
    }
    @Test
    fun `Add days test`(){
        val date = Date(30, 6, 2003)
        assertEquals(2, date.addDays(2).day)
        assertEquals(1, date.addDays(32).day)
        assertEquals(8, date.addDays(32).month)
    }
}