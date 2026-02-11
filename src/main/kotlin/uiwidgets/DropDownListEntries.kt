package qed.uiwidgets

import kotlin.enums.EnumEntries

/**
 * See https://www.baeldung.com/kotlin/function-enum-classes#:~:text=The%20challenge%20lies%20in%20the,each%20enum%20class%20is%20independent.
 * Create a Function That Works for All Enum Classes in Kotlin
 */

interface IPickValue {
    val PickValue : String
}

fun <E : Enum<E>> Array<E>.joinTheirNames(): String {
    return joinToString { (it as IPickValue).PickValue }
}

// this is an extension function to Enum class
fun EnumEntries<*>.joinTheirNames(): String {
    return joinToString { (it as IPickValue).PickValue }
}

fun EnumEntries<*>.find(value : String):Any? {
    return find {(it as IPickValue).PickValue==value}
}

abstract class Enumer<E : Enum<E>>(name: String, ordinal: Int): Comparable<IPickValue> {
    companion object {
        inline fun <reified E : Enum<E>> joinTheirNames(): String {
            return enumValues<E>().joinToString {
                println(it)
                (it as IPickValue).PickValue
            }
        }

    }
}

class DD<T>(private val value: EnumEntries<T>) where T : Enum<T>, T : IPickValue {
    fun get() = value
}


