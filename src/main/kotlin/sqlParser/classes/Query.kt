package sqlParser.classes

data class Query (
    val columns: List<IColumn>? = null, // Перечисление полей выборки явно (с алиасами) или *
    val fromSources: List<ITable>? = null, // Неявное объединение нескольких таблиц (select * from A,B,C)
    val joins: List<IJoin>? = null, // Явное объединение таблиц (inner, left, right, full join) и подзапросов
    val whereClauses: IExpression? = null, // Фильтрующие условия (where a = 1 and b > 100)
    val groupByColumns: List<String>? = null, // Группировка по одному или нескольким полям (group by)
    val sortColumns: List<Sort>? = null, // Сортировка по одному или нескольким полям (order by)
    val limit: Limit? = null,// Усечение выборки
    val offset: Offset? = null, // Усечение выборки
    val queryText: String? = null // Текст запроса
)