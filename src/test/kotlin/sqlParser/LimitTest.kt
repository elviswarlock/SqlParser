package sqlParser

import kotlin.test.assertEquals
import org.junit.Test
import sqlParser.classes.DoubleLimitSelectionException
import sqlParser.classes.Parser
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LimitTest {
    @Test
    fun noOperatorEntry() {
        val testQuery = "SELECT 1"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.limit)
    }

    @Test
    fun limitEquals() {
        val testQuery = "SELECT 1 FROM author LIMIT 15"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.limit)
        assertEquals("LIMIT 15", result.limit?.query)
        assertEquals(15, result.limit?.rowCount)
    }

    @Test
    fun fetchEquals() {
        val testQuery = "SELECT 1 FROM author FETCH NEXT 15 ROWS ONLY"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.limit)
        assertEquals("FETCH NEXT 15 ROWS ONLY", result.limit?.query)
        assertEquals(15, result.limit?.rowCount)
    }

    @Test(expected = DoubleLimitSelectionException::class)
    fun limitAndFetchConflict() {
        val testQuery = "SELECT 1 FROM author LIMIT 15 FETCH NEXT 15 ROWS ONLY"
        Parser().parseQuery(testQuery)
    }
}
