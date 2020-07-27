package sqlParser.classes

interface ITable {
    val alias: String?
}

data class Table(
        override val alias: String? = null,
        val name: String? = null
) : ITable

data class TableWithSubQuery(
        override val alias: String? = null,
        val query: Query? = null
) : ITable