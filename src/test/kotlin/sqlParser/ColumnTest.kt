package sqlParser

import kotlin.test.assertEquals
import org.junit.Test
import sqlParser.classes.*
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ColumnTest {
    @Test
    fun allColumns() {
        val testQuery = "SELECT * FROM author"
        val result = Parser().parseQuery(testQuery)

        assertEquals("*", (result.columns?.get(0) as AllColumns).expression)
    }

    @Test
    fun allTableColumns() {
        val testQuery = "SELECT a.* FROM author AS a"
        val result = Parser().parseQuery(testQuery)

        assertEquals(result.columns?.get(0), AllColumnsFromTable(
                tableName = "a",
                expression = "a.*"
        ))
    }

    @Test
    fun simpleColumnsWithoutAlias() {
        val testQuery = "SELECT a.Id, a.Name FROM author AS a"
        val result = Parser().parseQuery(testQuery)

        assertNull((result.columns?.get(0) as SimpleColumn).alias)
    }

    @Test
    fun simpleColumnsWithAlias() {
        val testQuery = "SELECT a.Id AS authorId, a.Name FROM author AS a"
        val result = Parser().parseQuery(testQuery)

        assertNotNull((result.columns?.get(0) as SimpleColumn).alias)
    }

    @Test
    fun columnWithSubQuery() {
        val testQuery = "SELECT (SELECT 1 FROM book) AS b FROM author AS a"
        val result = Parser().parseQuery(testQuery)

        assertEquals(result.columns?.get(0), ColumnWithSubQuery(
                query = Query(
                        columns = listOf(
                                SimpleColumn(
                                        columnName = "1"
                                )),
                        fromSources = listOf(
                                Table(
                                        name = "book"
                                )),
                        queryText = "(SELECT 1 FROM book)"
                ),
                alias = "b"
        ))
    }

    @Test
    fun combinationOfDifferentTypesColumns() {
        val testQuery = "SELECT *, a.*, a.Id AS authorId FROM author AS a"
        val result = Parser().parseQuery(testQuery)

        assertEquals(result.columns, listOf(
                AllColumns(
                        expression = "*"
                ),
                AllColumnsFromTable(
                        tableName = "a",
                        expression = "a.*"
                ),
                SimpleColumn(
                        columnName = "a.Id",
                        alias = "authorId"
                )))
    }
}
