package sqlParser.classes

interface IColumn

data class AllColumns(
        val expression: String? = null
) : IColumn

data class AllColumnsFromTable(
        val tableName: String? = null,
        val expression: String? = null
) : IColumn

data class SimpleColumn(
        val columnName: String? = null,
        val alias: String? = null
) : IColumn

data class ColumnWithSubQuery(
        val query: Query? = null,
        val alias: String? = null
) : IColumn
