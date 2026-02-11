package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.TestContext
import kotlin.reflect.KProperty

/**
 * The dynamic dropdown list adds support for valuse that cannot be represented by enumerated types as in the normal dropdown list.
 * This happens if values are entered in an application, which are then used to build the entries for a dropdown list.
 * In other words, these entries are added dynamically during the use of the applciation
 *
 * val nameDropdown = DropDownList(context, "#name-selector")
 *
 * // Use existing enum values
 * nameDropdown.value = "Wilson"
 *
 */

interface IDynamicDropdownList {
    var value : String
}

interface IDynamicDropDownListDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String)
}

abstract class BaseDynamicDropDownListDelegate<D>(
    private val owner: TestContext,
    private val selector: String
) : IDynamicDropDownListDelegate where D : IDynamicDropdownList {

    protected abstract fun createDynamicDropDownList(
        owner: TestContext,
        selector: String
    ): D

    private val dropDownList by lazy { createDynamicDropDownList(owner, selector) }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return dropDownList.value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        dropDownList.value = value
    }

}