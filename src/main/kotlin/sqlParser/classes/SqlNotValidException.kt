package sqlParser.classes

open class SqlNotValidException : Exception()

class InvalidQueryException : SqlNotValidException() {
    override val message: String?
        get() = "Запрос некорректный, в нём отсутствует SELECT"
}

class DoubleLimitSelectionException : SqlNotValidException() {
    override val message: String?
        get() = "Запрос некорректный, в нём одновременно есть LIMIT и FETCH"
}

class DoubleOffsetSelectionException : SqlNotValidException() {
    override val message: String?
        get() = "Запрос некорректный, в нём одновременно есть усечение выборки и в LIMIT и в OFFSET"
}