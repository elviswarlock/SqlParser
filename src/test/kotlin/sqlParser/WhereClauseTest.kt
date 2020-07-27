package sqlParser

import kotlin.test.assertEquals
import org.junit.Test
import sqlParser.classes.*
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WhereClauseTest {
    @Test
    fun noOperatorEntry() {
        val testQuery = "SELECT 1"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.whereClauses)
    }

    @Test
    fun simpleExpression() {
        val testQuery = "SELECT * FROM author AS a WHERE a = 100"
        val result = Parser().parseQuery(testQuery)
        assertEquals("a = 100", (result.whereClauses as SimpleExpression).expression)
    }

    @Test
    fun orExpression() {
        val testQuery = "SELECT * FROM author AS a WHERE a < 2 OR a > 5"
        val result = Parser().parseQuery(testQuery)
        assertEquals("a < 2", ((result.whereClauses as OrExpression).leftExpression as SimpleExpression).expression)
        assertEquals("a > 5", ((result.whereClauses as OrExpression).rightExpression as SimpleExpression).expression)
    }

    @Test
    fun andExpression() {
        val testQuery = "SELECT * FROM author AS a WHERE a > 1 AND a < 6"
        val result = Parser().parseQuery(testQuery)
        assertEquals("a > 1", ((result.whereClauses as AndExpression).leftExpression as SimpleExpression).expression)
        assertEquals("a < 6", ((result.whereClauses as AndExpression).rightExpression as SimpleExpression).expression)
    }

    @Test
    fun combinationOfDifferentTypesExpressions() {
        val testQuery = "SELECT * FROM author AS a WHERE a > 1 AND a < 6 OR a > 10"
        val result = Parser().parseQuery(testQuery)
        assertEquals("a > 1", (((result.whereClauses as OrExpression).leftExpression as AndExpression).leftExpression as SimpleExpression).expression)
        assertEquals("a < 6", (((result.whereClauses as OrExpression).leftExpression as AndExpression).rightExpression as SimpleExpression).expression)
        assertEquals("a > 10", ((result.whereClauses as OrExpression).rightExpression as SimpleExpression).expression)
    }
}