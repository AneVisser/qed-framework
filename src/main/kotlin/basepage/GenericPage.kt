package qed.basepage


/**
 * Generic page class. The only function of this class is to return the type of page passed, so that any page can be handled the same
 * way, regardless the page type. This way, the methods and properties of a particular page become available in a block call,
 * without the need to define onPage functions for each and every page.
 * See also https://www.baeldung.com/kotlin/generics
 */
class GenericPage<out T>(private val value: T) {

    fun get(): T = value

}

