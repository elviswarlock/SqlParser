package sqlParser

import org.junit.Test
import sqlParser.classes.Parser
import kotlin.test.*

class GroupTest {
    @Test
    fun noOperatorEntry() {
        val testQuery = "SELECT 1"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.groupByColumns)
    }

    @Test
    fun checkOrder() {
        val testQuery = "SELECT 1 FROM author GROUP BY author.id, author.name"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.groupByColumns)
        assertEquals("author.id", result.groupByColumns?.get(0))
        assertEquals("author.name", result.groupByColumns?.get(1))
    }
}
