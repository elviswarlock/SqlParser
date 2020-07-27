package sqlParser

import kotlin.test.assertEquals
import org.junit.Test
import sqlParser.classes.DoubleOffsetSelectionException
import sqlParser.classes.Parser
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OffsetTest {
    @Test
    fun noOperatorEntry() {
        val testQuery = "SELECT 1"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.offset)
    }

    @Test
    fun offsetEquals() {
        val testQuery = "SELECT 1 FROM author OFFSET 10 ROWS"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.offset)
        assertEquals("OFFSET 10 ROWS", result.offset?.query)
        assertEquals(10, result.offset?.rowCount)
    }

    @Test
    fun offsetInLimitEquals() {
        val testQuery = "SELECT 1 FROM author LIMIT 10, 15"
        val result = Parser().parseQuery(testQuery)
        assertNotNull(result.offset)
        assertEquals("LIMIT 10, 15", result.offset?.query)
        assertEquals(10, result.offset?.rowCount)
    }

    @Test(expected = DoubleOffsetSelectionException::class)
    fun limitAndOffsetConflict() {
        val testQuery = "SELECT 1 FROM author LIMIT 10, 15 OFFSET 10 ROWS"
        Parser().parseQuery(testQuery)
    }
}
