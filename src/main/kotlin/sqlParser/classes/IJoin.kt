package sqlParser.classes

interface IJoin {
    val alias: String?
    val type: JoinType?
    val joinClause: IExpression?
}

data class Join(
        override val alias: String? = null,
        override val type: JoinType? = null,
        override val joinClause: IExpression? = null,
        val tableName: String? = null
) : IJoin

data class JoinWithSubQuery(
        override val alias: String? = null,
        override val type: JoinType? = null,
        override val joinClause: IExpression? = null,
        val query: Query? = null
) : IJoin

enum class JoinType {
    JOIN, LEFT, RIGHT, INNER, FULL
}