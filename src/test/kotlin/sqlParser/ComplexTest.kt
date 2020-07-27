package sqlParser

import net.sf.jsqlparser.schema.Column
import org.junit.Test
import sqlParser.classes.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class ComplexTest {
    @Test
    fun checkStructure() {
        val testQuery = "SELECT author.name AS test1, author.*, *\n" +
                "FROM author AS a, test AS t\n" +
                "LEFT JOIN book AS b ON (author.id = book.author_id)\n" +
                "                       AND (author.name = book.author_name)\n" +
                "                       OR author.name = book.author_name\n" +
                "                       AND (author.name = book.author_name)\n" +
                "WHERE t.Id = 1\n" +
                "AND a.Id > 100\n" +
                "OR b.Id <= 1000\n" +
                "GROUP BY author.id, author.name\n" +
                "ORDER BY count(book.id), 1 DESC\n" +
                "LIMIT 15\n" +
                "OFFSET 10 ROWS\n"
        val result = Parser().parseQuery(testQuery)
        // Select
        assertEquals(result.columns, listOf(
                SimpleColumn(columnName = "author.name", alias = "test1"),
                AllColumnsFromTable(tableName = "author", expression = "author.*"),
                AllColumns(expression = "*")
        ))
        // From
        assertEquals(result.fromSources, listOf(
                Table(name = "author", alias = "a"),
                Table(name = "test", alias = "t")
        ))
        // Join
        assertEquals(result.joins, listOf(
                Join(
                        type = JoinType.LEFT,
                        tableName = "book",
                        alias = "b",
                        joinClause = OrExpression(
                                leftExpression = AndExpression(
                                        leftExpression = SimpleExpression(expression = "(author.id = book.author_id)"),
                                        rightExpression = SimpleExpression(expression = "(author.name = book.author_name)")
                                ),
                                rightExpression = AndExpression(
                                        leftExpression = SimpleExpression(expression = "author.name = book.author_name"),
                                        rightExpression = SimpleExpression(expression = "(author.name = book.author_name)")
                                )
                        ))
        ))
        // Where
        assertEquals(result.whereClauses, OrExpression(
                leftExpression = AndExpression(
                        leftExpression = SimpleExpression(expression = "t.Id = 1"),
                        rightExpression = SimpleExpression(expression = "a.Id > 100")
                ),
                rightExpression = SimpleExpression(expression = "b.Id <= 1000")
        ))
        // Group by
        assertEquals(result.groupByColumns, listOf(
                "author.id",
                "author.name"
        ))
        // Order by
        assertEquals(result.sortColumns, listOf(
                Sort(indexNumber = 1, isAsc = true, expression = "count(book.id)"),
                Sort(indexNumber = 2, isAsc = false, expression = "author.name")
        ))
        // Limit
        assertEquals(result.limit, Limit(query = "LIMIT 15", rowCount = 15))
        // Offset
        assertEquals(result.offset, Offset(query = "OFFSET 10 ROWS", rowCount = 10))
    }
}
