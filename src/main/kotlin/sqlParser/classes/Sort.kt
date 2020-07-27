package sqlParser.classes

data class Sort(
        val indexNumber: Int? = null,
        val expression: String? = null,
        val isAsc: Boolean
)