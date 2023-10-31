package date_Structure

private val daysOfMonth = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
class Date(val day: Int = 1, val month: Int = 1, val year:Int) {
    init {
        require(month in 1..12){"Invalid month."}
        require(day in 1..lastDayOfTheMonth){"Invalid day."}
        require(year in 1..4000){"Invalid year."}
    }
    private fun isleapYear() = year%400 ==0 || year%4 == 0 && year%100 != 0
    val isLeapYear get() = isleapYear()
}

tailrec fun Date.addDays(days: Int):Date =when{
    day+days <= lastDayOfTheMonth ->
        Date(day+days, month, year)
    month < 12 ->
        Date(1, month+1, year).addDays(days - (lastDayOfTheMonth - day + 1))
    else ->
        Date(year = year+1).addDays(days - (lastDayOfTheMonth - day + 1))
}
private val Date.lastDayOfTheMonth get() = if (month == 2 && isLeapYear) 29 else daysOfMonth[month-1]