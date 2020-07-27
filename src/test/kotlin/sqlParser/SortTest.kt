package sqlParser

import org.junit.Test
import sqlParser.classes.Parser
import kotlin.test.*

class SortTest {
    @Test
    fun noOperatorEntry() {
        val testQuery = "SELECT 1"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.sortColumns)
    }

    @Test
    fun defaultSort() {
        val testQuery = "SELECT 1 FROM author ORDER BY count(author.id)"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.sortColumns)
        val firstElement = result.sortColumns?.get(0)
        assertTrue(firstElement?.isAsc == true)
        assertEquals("count(author.id)", firstElement?.expression)
    }

    @Test
    fun ascSort() {
        val testQuery = "SELECT 1 FROM author ORDER BY count(author.id) ASC"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.sortColumns)
        val firstElement = result.sortColumns?.get(0)
        assertTrue(firstElement?.isAsc == true)
        assertEquals("count(author.id)", firstElement?.expression)
    }

    @Test
    fun descSort() {
        val testQuery = "SELECT 1 FROM author ORDER BY count(author.id) DESC"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.sortColumns)
        val firstElement = result.sortColumns?.get(0)
        assertTrue(firstElement?.isAsc == false)
        assertEquals("count(author.id)", firstElement?.expression)
    }

    @Test
    fun sortByNumber() {
        val testQuery = "SELECT author.id FROM author ORDER BY 1"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.sortColumns)
        assertEquals("author.id", result.sortColumns?.get(0)?.expression)
    }

    @Test
    fun indexNumber() {
        val testQuery = "SELECT author.id FROM author ORDER BY author.id, author.name"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.sortColumns)
        assertEquals(1, result.sortColumns?.get(0)?.indexNumber)
        assertEquals(2, result.sortColumns?.get(1)?.indexNumber)
    }
}
