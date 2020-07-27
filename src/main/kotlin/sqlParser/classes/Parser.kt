package sqlParser.classes

import net.sf.jsqlparser.expression.Expression
import net.sf.jsqlparser.expression.LongValue
import net.sf.jsqlparser.expression.operators.conditional.OrExpression as JOrExpression
import net.sf.jsqlparser.expression.operators.conditional.AndExpression as JAndExpression
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.schema.Table as JTable
import net.sf.jsqlparser.statement.select.*
import net.sf.jsqlparser.statement.select.AllColumns as JAllColumns
import net.sf.jsqlparser.statement.select.Join as JJoin
import net.sf.jsqlparser.statement.select.Limit as JLimit
import net.sf.jsqlparser.statement.select.Offset as JOffset

class Parser {

    fun parseQuery(example: String?): Query {
        val selectStatement = CCJSqlParserUtil.parse(example) as Select
        val plainSelect = selectStatement.selectBody as PlainSelect
        return Query(
                columns = getColumns(plainSelect.selectItems),
                fromSources = getFromSources(plainSelect.fromItem, plainSelect.joins),
                joins = getJoins(plainSelect.joins),
                whereClauses = getExpressions(plainSelect.where),
                groupByColumns = getGroupByColumns(plainSelect.groupBy),
                sortColumns = getSortColumns(plainSelect.orderByElements, plainSelect.selectItems),
                limit = getLimit(plainSelect.limit, plainSelect.fetch),
                offset = getOffset(plainSelect.limit, plainSelect.offset),
                queryText = example
        )
    }

    private fun getColumns(selectItems: List<SelectItem>?): List<IColumn>? {
        if (selectItems == null) {
            throw InvalidQueryException()
        }

        val result = mutableListOf<IColumn>()

        selectItems.forEach { selectItem ->
            when (selectItem) {
                is JAllColumns -> {
                    result.add(AllColumns(
                            expression = selectItem.toString()
                    ))
                }
                is AllTableColumns -> {
                    result.add(AllColumnsFromTable(
                            tableName = selectItem.table.toString(),
                            expression = selectItem.toString()
                    ))
                }
                is SelectExpressionItem -> {
                    when (selectItem.expression) {
                        is Column, is LongValue -> {
                            result.add(SimpleColumn(
                                    columnName = selectItem.expression.toString(),
                                    alias = if (selectItem.alias != null) {
                                        getAliasWithoutName(selectItem.alias.toString())
                                    } else {
                                        null
                                    }
                            ))
                        }
                        is SubSelect -> {
                            result.add(ColumnWithSubQuery(
                                    query = parseQuery(selectItem.expression.toString()),
                                    alias = if (selectItem.alias != null) {
                                        getAliasWithoutName(selectItem.alias.toString())
                                    } else {
                                        null
                                    }
                            ))
                        }
                    }
                }
            }
        }

        return result
    }

    private fun getFromSources(fromItem: FromItem?, joins: List<JJoin>?): List<ITable>? {
        if (fromItem == null) {
            return null
        }

        val result = mutableListOf<ITable>()
        when (fromItem) {
            is JTable -> {
                result.add(Table(
                        name = getNameWithoutAlias(fromItem.toString()),
                        alias = if (fromItem.alias != null) {
                            fromItem.alias.name
                        } else {
                            null
                        }
                ))
            }
            is SubSelect -> {
                result.add(TableWithSubQuery(
                        query = parseQuery(fromItem.selectBody.toString()),
                        alias = if (fromItem.alias != null) {
                            fromItem.alias.name
                        } else {
                            null
                        }
                ))
            }
        }

        if (joins != null) {
            val simpleJoins = joins.filter { join -> join.isSimple }
            simpleJoins.forEach { simpleJoin ->
                when (simpleJoin.rightItem) {
                    is JTable -> {
                        result.add(Table(
                                name = getNameWithoutAlias(simpleJoin.toString()),
                                alias = getAliasWithoutName(simpleJoin.toString())
                        ))
                    }
                    is SubSelect -> {
                        result.add(TableWithSubQuery(
                                query = parseQuery((simpleJoin.rightItem as SubSelect).selectBody.toString()),
                                alias = if (simpleJoin.rightItem.alias != null) {
                                    simpleJoin.rightItem.alias.name
                                } else {
                                    null
                                }
                        ))
                    }
                }
            }
        }

        return result
    }

    private fun getGroupByColumns(groupBy: GroupByElement?): List<String>? {
        if (groupBy == null) {
            return null
        }

        return groupBy.groupByExpressions.map { expression -> expression.toString() }
    }

    private fun getJoins(joins: List<net.sf.jsqlparser.statement.select.Join>?): List<IJoin>? {
        if (joins == null) {
            return null
        }

        val notSimpleJoins = joins.filter { join -> !join.isSimple }
        if (notSimpleJoins.isEmpty()) {
            return null
        }

        val result = mutableListOf<IJoin>()

        notSimpleJoins.forEach { notSimpleJoin ->
            when (notSimpleJoin.rightItem) {
                is JTable -> {
                    result.add(Join(
                            tableName = getNameWithoutAlias(notSimpleJoin.rightItem.toString()),
                            alias = if (notSimpleJoin.rightItem.alias != null) {
                                getAliasWithoutName(notSimpleJoin.rightItem.alias.toString())
                            } else {
                                null
                            },
                            type = getJoinType(notSimpleJoin),
                            joinClause = getExpressions(notSimpleJoin.onExpression)
                    ))
                }
                is SubSelect -> {
                    result.add(JoinWithSubQuery(
                            query = parseQuery((notSimpleJoin.rightItem as SubSelect).selectBody.toString()),
                            alias = getAliasWithoutName(notSimpleJoin.rightItem.alias.toString()),
                            type = getJoinType(notSimpleJoin),
                            joinClause = getExpressions(notSimpleJoin.onExpression)
                    ))
                }
            }
        }

        return result
    }

    private fun getLimit(limit: JLimit?, fetch: Fetch?): Limit? {
        if (limit == null && fetch == null) {
            return null
        }
        if (limit != null && fetch != null) {
            throw DoubleLimitSelectionException()
        }

        return if (limit != null) {
            val rowCount = limit.rowCount.toString()
            (Limit(
                    query = limit.toString().trimStart(),
                    rowCount = rowCount.toIntOrNull()
            ))
        } else {
            val rowCount = fetch?.rowCount.toString()
            (Limit(
                    query = fetch.toString().trimStart(),
                    rowCount = rowCount.toIntOrNull()
            ))
        }
    }

    private fun getOffset(limit: JLimit?, offset: JOffset?): Offset? {
        if (limit == null && offset == null) {
            return null
        }

        if (offset != null) {
            val result = Offset(
                    query = offset.toString().trimStart(),
                    rowCount = offset.offset.toString().toIntOrNull()
            )
            if (limit != null) {
                return getOffsetFromLimit(limit, result)
            }

            return result
        } else {
            return limit?.let { getOffsetFromLimit(it) }
        }

    }

    private fun getSortColumns(orderBy: List<OrderByElement>?, selectItems: MutableList<SelectItem>): List<Sort>? {
        if (orderBy == null) {
            return null
        }

        val result = mutableListOf<Sort>()
        var index = 1
        orderBy.forEach { orderByElement ->
            val columnNumber = orderByElement.expression.toString().toIntOrNull()
            val element = Sort(
                    indexNumber = index,
                    expression = if (columnNumber == null) {
                        orderByElement.expression.toString()
                    } else {
                        getNameWithoutAlias(selectItems[columnNumber - 1].toString())
                    },
                    isAsc = orderByElement.isAsc
            )
            result.add(element)

            index++
        }

        return result
    }

    private fun getAliasWithoutName(nameWithAlias: String): String? {
        return when {
            nameWithAlias.contains(" AS ") -> nameWithAlias.split(" AS ")[1]
            nameWithAlias.contains(" ") -> nameWithAlias.split(" ")[1]
            else -> null
        }
    }

    private fun getExpressions(expression: Expression?): IExpression? {
        if (expression == null) {
            return null
        }

        when (expression) {
            is JOrExpression -> {
                return OrExpression(
                        leftExpression = getExpressions(expression.leftExpression),
                        rightExpression = getExpressions(expression.rightExpression)
                )
            }
            is JAndExpression -> {
                return AndExpression(
                        leftExpression = getExpressions(expression.leftExpression),
                        rightExpression = getExpressions(expression.rightExpression)
                )
            }
            else -> {
                return SimpleExpression(
                        expression = expression.toString()
                )
            }
        }
    }

    private fun getJoinType(join: JJoin): JoinType? {
        return when {
            join.isLeft -> JoinType.LEFT
            join.isRight -> JoinType.RIGHT
            join.isInner -> JoinType.INNER
            join.isFull -> JoinType.FULL
            else -> JoinType.JOIN
        }
    }

    private fun getOffsetFromLimit(limit: JLimit, offset: Offset? = null): Offset? {
        val limitOffset = limit.offset
        var rowCount: Int? = null
        if (limitOffset != null)
            rowCount = limitOffset.toString().toIntOrNull()

        if (rowCount != null) {
            if (offset?.query != null) {
                throw DoubleOffsetSelectionException()
            } else {
                return Offset(
                        query = limit.toString().trimStart(),
                        rowCount = rowCount
                )
            }
        }

        return offset
    }

    private fun getNameWithoutAlias(nameWithAlias: String): String {
        return when {
            nameWithAlias.contains(" AS ") -> nameWithAlias.split(" AS ")[0]
            nameWithAlias.contains(" ") -> nameWithAlias.split(" ")[0]
            else -> nameWithAlias
        }
    }
}