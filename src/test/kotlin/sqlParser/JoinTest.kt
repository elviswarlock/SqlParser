package sqlParser

import kotlin.test.assertEquals
import org.junit.Test
import sqlParser.classes.*
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JoinTest {
    @Test
    fun noOperatorEntry() {
        val testQuery = "SELECT 1"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.joins)
    }

    @Test
    fun onlySimpleJoins() {
        val testQuery = "SELECT 1 FROM author, test"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.joins)
    }

    @Test
    fun tableJoin() {
        val testQuery = "SELECT 1 FROM author \n" +
                "JOIN book AS b ON author.id = book.author_id"
        val result = Parser().parseQuery(testQuery)
        assertEquals(result.joins, listOf(
                Join(
                        type = JoinType.JOIN,
                        tableName = "book",
                        alias = "b",
                        joinClause = SimpleExpression(
                                expression = "author.id = book.author_id"
                        )
                ))
        )
    }

    @Test
    fun joinWithSubQuery() {
        val testQuery = "SELECT 1 FROM author \n" +
                "JOIN (SELECT 1 FROM book) AS b ON author.id = book.author_id"
        val result = Parser().parseQuery(testQuery)
        assertEquals(result.joins?.get(0), JoinWithSubQuery(
                type = JoinType.JOIN,
                query = Query(
                        columns = listOf(
                                SimpleColumn(
                                        columnName = "1"
                                )),
                        fromSources = listOf(
                                Table(
                                        name = "book"
                                )
                        ),
                        queryText = "SELECT 1 FROM book"
                ),
                alias = "b",
                joinClause = SimpleExpression(
                        expression = "author.id = book.author_id"
                )
        )
        )
    }

    @Test
    fun joinTypes() {
        assertEquals(JoinType.JOIN, getJoinType("SELECT 1 FROM author JOIN book ON 1 = 1"))
        assertEquals(JoinType.LEFT, getJoinType("SELECT 1 FROM author LEFT JOIN book ON 1 = 1"))
        assertEquals(JoinType.RIGHT, getJoinType("SELECT 1 FROM author RIGHT JOIN book ON 1 = 1"))
        assertEquals(JoinType.INNER, getJoinType("SELECT 1 FROM author INNER JOIN book ON 1 = 1"))
        assertEquals(JoinType.FULL, getJoinType("SELECT 1 FROM author FULL JOIN book ON 1 = 1"))
    }

    private fun getJoinType(query: String): JoinType? {
        val result = Parser().parseQuery(query)
        return (result.joins?.get(0) as Join).type
    }
}
