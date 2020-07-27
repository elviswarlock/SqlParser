package sqlParser.classes

interface IExpression

data class OrExpression(
        val leftExpression: IExpression? = null,
        val rightExpression: IExpression? = null
) : IExpression

data class AndExpression(
        val leftExpression: IExpression? = null,
        val rightExpression: IExpression? = null
) : IExpression

data class SimpleExpression(
        val expression: String? = null
) : IExpression