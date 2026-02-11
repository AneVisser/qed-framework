package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.TestContext
import qed.uiwidgets.AtomicWidget
import qed.uiwidgets.IPickValue
import kotlin.enums.EnumEntries
import kotlin.reflect.KProperty

/**
 * The dropdown list adds support for static dd enumtypes. They are used as static representations of what
 * the value of a dropdownlist can be.
 * The usage in a breed dropdown could be as follows:
 *
 * val breedDropdown = DropDownList(context, "#breed-selector", ddBreed.entries)
 *
 * // Use existing enum values
 * breedDropdown.value = ddBreed.SHEPHERD
 *
 */

interface IDropDownList<T> where T : Enum<T>, T : IPickValue  {
    var value : T
}

interface IDropDownListDelegate<T> where T : Enum<T>, T : IPickValue {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}

abstract class BaseDropDownListDelegate<T, U>(
    private val owner: TestContext,
    private val selector: String,
    private val entriesList: EnumEntries<T>
) : IDropDownListDelegate<T> where T : Enum<T>, T : IPickValue, U : IDropDownList<T> {

    protected abstract fun createDropDownList(
        owner: TestContext,
        selector: String,
        entriesList: EnumEntries<T>
    ): U

    private val dropDownList by lazy { createDropDownList(owner, selector, entriesList) }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return dropDownList.value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        dropDownList.value = value
    }

}