package sqlParser

import sqlParser.classes.*
import sqlParser.classes.Parser

const val QUERY = "SELECT *, author.*, author.name AS test1\n" +
        "FROM author AS a, test AS t\n" +
        "LEFT JOIN book AS b ON (author.id = book.author_id)\n" +
        "                       AND (author.name = book.author_name)\n" +
        "                       OR author.name = book.author_name\n" +
        "                       AND (author.name = book.author_name)\n" +
        "JOIN (SELECT TOP 100 * FROM author) AS c ON c.id = book.author_id\n" +
        "WHERE t.Id = 1\n" +
        "AND a.Id > 100\n" +
        "OR b.Id <= 1000\n" +
        "GROUP BY author.id, author.name\n" +
        "ORDER BY count(book.id), 1\n" +
        "LIMIT 15\n" +
        "OFFSET 10 ROWS\n"

fun main() {
    val result = Parser().parseQuery(QUERY)
    printAllPartsQuery(result)
}

private fun printAllPartsQuery(query: Query) {
    println()
    if (query.columns != null) {
        printHeader("COLUMNS")
        query.columns.forEach {
            println(it)
        }
    }
    println()
    if (query.fromSources != null) {
        println()
        printHeader("FROM")
        query.fromSources.forEach {
            println(it)
        }
    }
    println()
    if (query.joins != null) {
        printHeader("JOINS")
        var index = 1
        query.joins.forEach {
            println(printJoins(it, index))
            index++
        }
    }
    println()
    if (query.whereClauses != null) {
        printHeader("WHERE")
        println(printExpression(query.whereClauses))
    }
    println()
    if (query.groupByColumns != null) {
        printHeader("GROUP_BY")
        query.groupByColumns.forEach() {
            println(it)
        }
    }
    println()
    if (query.sortColumns != null) {
        printHeader("ORDER_BY")
        query.sortColumns.forEach() {
            println(it)
        }
    }
    println()
    if (query.limit != null) {
        printHeader("LIMIT")
        println(query.limit)
    }
    println()
    if (query.offset != null) {
        printHeader("OFFSET")
        println(query.offset)
    }
}

private fun printExpression(expression: IExpression): String? {
    return when (expression) {
        is OrExpression -> {
            expression.leftExpression?.let { printExpression(it) } + "\n\t OR " +
                    expression.rightExpression?.let { printExpression(it) }
        }
        is AndExpression -> {
            "(" + expression.leftExpression?.let { printExpression(it) } + "\n\t AND " +
                    expression.rightExpression?.let { printExpression(it) } + ")"

        }
        else -> {
            val simpleExpression = expression as SimpleExpression
            simpleExpression.expression
        }
    }
}

private fun printJoins(join: IJoin, index: Int): String? {
    return when (join) {
        is Join -> {
            index.toString() + ". " +
                    join.type.toString() + " " +
                    join.tableName + " " +
                    join.alias + " ON\n" +
                    join.joinClause?.let { printExpression(it) }
        }
        else -> {
            index.toString() + ". " +
                    join.type.toString() + " (" +
                    (join as JoinWithSubQuery).query?.queryText.toString() + ") " +
                    join.alias + " ON " +
                    join.joinClause?.let { printExpression(it) }
        }
    }
}

private fun printHeader(header: String){
    println("# $header")
}