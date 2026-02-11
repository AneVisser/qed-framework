package qed.testbaseclass

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

class Utility {
    // get method name of current method, used for reporting
    companion object {
        fun methodName(prefix : String? = "") : String {
            val stackTrace = Thread.currentThread().stackTrace
            val i = stackTrace.indexOf(stackTrace.find { it.methodName == "methodName" })
            return "current method:$prefix.${Thread.currentThread().stackTrace[i+1].methodName.firstWord('$')}"
        }
     }
}

// get the last word from a string separated by a given character
fun String.lastWord(separator: Char = '.'): String? {
    val names = this.trim().split(separator)
    return names.lastOrNull()
}

// get the first word from a string separated by a given character
fun String.firstWord(separator: Char = '.'): String? {
    val names = this.trim().split(separator)
    return names.firstOrNull()
}

// get the nth word from a string separated by a given character
fun String.nthWord(N: Int, separator: Char = '.'): String? {
    val names = this.trim().split(separator)
    return names[N-1]
}

enum class CharSet(val list: List<Char>) {
    ALPHANUMERIC(('a'..'z') + ('A'..'Z') + ('0'..'9')),
    ALPHABETIC(('a'..'z') + ('A'..'Z')),
    ALPHABETIC_LOWER(('a'..'z').toList()),
    ALPHABETIC_UPPER(('A'..'Z').toList()),
    NUMERIC(('0'..'9').toList())
}

fun randomChars(length: Int, chars : CharSet) =
    (1..length)
        .map { chars.list.random() }
        .joinToString("").lowercase().replaceFirstChar { it.uppercase() }


/**
 * Common DateTimeFormatter Patterns:
 * | Pattern                    | Example Output              | Meaning                        |
 * | -------------------------- | --------------------------- | ------------------------------ |
 * | `yyyy-MM-dd`               | `2025-08-22`                | Year-Month-Day                 |
 * | `dd/MM/yyyy`               | `22/08/2025`                | Day/Month/Year                 |
 * | `MM-dd-yyyy`               | `08-22-2025`                | Month-Day-Year                 |
 * | `yyyyMMdd`                 | `20250822`                  | Compact date (no separators)   |
 * | `yyyy-MM-dd HH:mm`         | `2025-08-22 14:45`          | Date + Hours\:Minutes          |
 * | `yyyy-MM-dd HH:mm:ss`      | `2025-08-22 14:45:30`       | Date + Hours\:Minutes\:Seconds |
 * | `HH:mm:ss`                 | `14:45:30`                  | Time (24-hour)                 |
 * | `hh:mm a`                  | `02:45 PM`                  | Time (12-hour with AM/PM)      |
 * | `yyyy-MM-dd'T'HH:mm:ss`    | `2025-08-22T14:45:30`       | ISO 8601 LocalDateTime         |
 * | `yyyy-MM-dd'T'HH:mm:ssXXX` | `2025-08-22T14:45:30+12:00` | ISO 8601 with timezone offset  |
 * | `EEEE, MMMM d, yyyy`       | `Friday, August 22, 2025`   | Full day + month name          |
 * | `EEE, MMM d`               | `Fri, Aug 22`               | Abbreviated day + month        |
 * | `MMM dd yyyy`              | `Aug 22 2025`               | Month name + day + year        |
 *
 * This class can be constructed as follows:
 * QEDDate() -> gets todays date, default pattern is dd/MM/yyy
 * QEDDate(LocalDate.of(2000, 3, 25)) -> 25/03/2000
 * val parsed = QEDDate("dd-MM-yyyy").parse("25-03-2025)
 * parsed.yearsAgo(5)) -> 25/03/2020
 */
class QEDDate(
    private var date: LocalDateTime = LocalDateTime.now(),
    private var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
) {
    constructor(pattern: String) : this(LocalDateTime.now(), DateTimeFormatter.ofPattern(pattern))
    constructor(date: LocalDateTime, pattern: String = "dd/MM/yyyy") : this(date, DateTimeFormatter.ofPattern(pattern))
    fun setFormat(pattern: String): QEDDate {
        formatter = DateTimeFormatter.ofPattern(pattern)
        return this
    }
    val today: QEDDate
        get() = QEDDate(date, formatter)
    val yesterday: QEDDate
        get() = QEDDate(date.minusDays(1), formatter)
    val tomorrow: QEDDate
        get() = QEDDate(date.plusDays(1), formatter)
    fun daysAgo(days: Long): QEDDate =
        QEDDate(date.minusDays(days), formatter)
    fun daysFromNow(days: Long): QEDDate =
        QEDDate(date.plusDays(days), formatter)
    fun monthsAgo(months: Long): QEDDate =
        QEDDate(date.minusMonths(months), formatter)
    fun monthsFromNow(months: Long): QEDDate =
        QEDDate(date.plusMonths(months), formatter)
    fun yearsAgo(years: Long): QEDDate =
        QEDDate(date.minusYears(years), formatter)
    fun yearsFromNow(years: Long): QEDDate =
        QEDDate(date.plusYears(years), formatter)
    fun format(): String = date.format(formatter)
    fun parse(dateStr: String): QEDDate =
        QEDDate(LocalDateTime.parse(dateStr, formatter), formatter)
    val month : Int = date.month.value
    val year : Int = date.year
    val dow : Int = date.dayOfWeek.value
    val doy : Int = date.dayOfYear
    override fun toString(): String = format()
}

fun List<Double>.standardDeviation(): Double {
    val mean = average()
    return kotlin.math.sqrt(map { (it - mean).pow(2) }.average())
}

fun List<Double>.coefVar(): Double {
    return this.standardDeviation().div(this.average())
}