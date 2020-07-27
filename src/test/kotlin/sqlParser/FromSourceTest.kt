package sqlParser

import org.junit.Test
import sqlParser.classes.*
import kotlin.test.*

class FromSourceTest {
    @Test
    fun noOperatorEntry() {
        val testQuery = "SELECT 1"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.fromSources)
    }

    @Test
    fun emptyAlias() {
        val testQuery = "SELECT 1 FROM author"
        val result = Parser().parseQuery(testQuery)
        assertNull(result.fromSources?.get(0)?.alias)
    }

    @Test
    fun notEmptyAlias() {
        var testQuery = "SELECT 1 FROM author AS a, book b"
        var result = Parser().parseQuery(testQuery)
        assertEquals("a", result.fromSources?.get(0)?.alias)
        assertEquals("b", result.fromSources?.get(1)?.alias)
    }

    @Test
    fun tableName() {
        val testQuery = "SELECT 1 FROM author"
        val result = Parser().parseQuery(testQuery)
        assertEquals("author", (result.fromSources?.get(0) as Table).name)
    }

    @Test
    fun joinMultipleTables() {
        val testQuery = "SELECT 1 FROM author, book"
        val result = Parser().parseQuery(testQuery)
        assertEquals(2, result.fromSources?.size)
        assertEquals("author", (result.fromSources?.get(0) as Table).name)
        assertEquals("book", (result.fromSources?.get(1) as Table).name)
    }

    @Test
    fun joinMultipleTablesWithAlias() {
        val testQuery = "SELECT 1 FROM author AS a, book AS b"
        val result = Parser().parseQuery(testQuery)
        assertEquals("a", result.fromSources?.get(0)?.alias)
        assertEquals("b", result.fromSources?.get(1)?.alias)
    }

    @Test
    fun joinMultipleSubQueryWithTable() {
        val testQuery = "SELECT 1 FROM (SELECT 1 FROM book) AS b, author"
        val result = Parser().parseQuery(testQuery)

        assertEquals(result.fromSources, listOf(
                TableWithSubQuery(
                        alias = "b",
                        query = Query(
                                columns = listOf(
                                        SimpleColumn(
                                                columnName = "1"
                                        )),
                                fromSources = listOf(
                                        Table(
                                                name = "book"
                                        )),
                                queryText = "SELECT 1 FROM book"
                        )
                ),
                Table(
                        name = "author"
                ))
        )
    }

    @Test
    fun joinMultipleTableWithSubQuery() {
        val testQuery = "SELECT 1 FROM author, (SELECT 1 FROM book) AS b"
        val result = Parser().parseQuery(testQuery)

        assertEquals(result.fromSources, listOf(
                Table(
                        name = "author"
                ),
                TableWithSubQuery(
                        alias = "b",
                        query = Query(
                                columns = listOf(
                                        SimpleColumn(
                                                columnName = "1"
                                        )),
                                fromSources = listOf(
                                        Table(
                                                name = "book"
                                        )),
                                queryText = "SELECT 1 FROM book"
                        )
                ))
        )
    }
}
