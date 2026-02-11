package qed.uiwidgets

import WidgetType
import qed.testbaseclass.TestContext
import qed.uiwidgets.widgetlibs.base.BaseDropDownListDelegate
import qed.uiwidgets.widgetlibs.base.Button
import qed.uiwidgets.widgetlibs.base.Checkbox
import qed.uiwidgets.widgetlibs.base.DropDownList
import qed.uiwidgets.widgetlibs.base.DropDownListDelegate
import qed.uiwidgets.widgetlibs.base.DynamicDropDownList
import qed.uiwidgets.widgetlibs.base.DynamicDropDownListDelegate
import qed.uiwidgets.widgetlibs.base.HoverArea
import qed.uiwidgets.widgetlibs.base.IDropDownListDelegate
import qed.uiwidgets.widgetlibs.base.IDynamicDropDownListDelegate
import qed.uiwidgets.widgetlibs.base.ITable
import qed.uiwidgets.widgetlibs.base.InputField
import qed.uiwidgets.widgetlibs.base.InputFieldDelegate
import qed.uiwidgets.widgetlibs.base.Link
import qed.uiwidgets.widgetlibs.base.LinkDelegate
import qed.uiwidgets.widgetlibs.base.RadioButton
import qed.uiwidgets.widgetlibs.base.Table
import qed.uiwidgets.widgetlibs.base.TextField
import qed.uiwidgets.widgetlibs.base.TextFieldDelegate
import qed.uiwidgets.widgetlibs.jqWidgets.jqWidgetsDropDownList
import qed.uiwidgets.widgetlibs.jqWidgets.jqWidgetsDropDownListDelegate
import qed.uiwidgets.widgetlibs.jqWidgets.jqWidgetsDynamicDropDownListDelegate
import qed.uiwidgets.widgetlibs.jqWidgets.jqWidgetsDynamicDropdownList
import qed.uiwidgets.widgetlibs.jqWidgets.jqWidgetsTable
import kotlin.enums.EnumEntries

/**
 * A factory picks the right adapter at runtime, based on configuration of widgetType (which is set in the config file).
 * In many cases, the default widget type is used (as defined in package widgetlibs.base). However, if a javascript library
 * has a specific behaviour, the widget is implemented by its specific class
 */

object ComponentFactory {

    fun link(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> Link(context, selector)
            WidgetType.REACT -> Link(context, selector)
            WidgetType.JQUERY -> Link(context, selector)
            WidgetType.JQWIDGETS -> Link(context, selector)
        }

    fun linkDelegate(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> LinkDelegate(context, selector)
            WidgetType.REACT -> LinkDelegate(context, selector)
            WidgetType.JQUERY -> LinkDelegate(context, selector)
            WidgetType.JQWIDGETS -> LinkDelegate(context, selector)
        }

    fun button(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> Button(context, selector)
            WidgetType.REACT -> Button(context, selector)
            WidgetType.JQUERY -> Button(context, selector)
            WidgetType.JQWIDGETS -> Button(context, selector)
        }

    fun radiobutton(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> RadioButton(context, selector)
            WidgetType.REACT -> RadioButton(context, selector)
            WidgetType.JQUERY -> RadioButton(context, selector)
            WidgetType.JQWIDGETS -> RadioButton(context, selector)
        }

    fun checkbox(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> Checkbox(context, selector)
            WidgetType.REACT -> Checkbox(context, selector)
            WidgetType.JQUERY -> Checkbox(context, selector)
            WidgetType.JQWIDGETS -> Checkbox(context, selector)
        }

    fun table(context : TestContext, selector : String) : ITable =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> Table(context, selector)
            WidgetType.REACT -> Table(context, selector)
            WidgetType.JQUERY -> Table(context, selector)
            WidgetType.JQWIDGETS -> jqWidgetsTable(context, selector)
        }

    fun inputfield(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> InputField(context, selector)
            WidgetType.REACT -> InputField(context, selector)
            WidgetType.JQUERY -> InputField(context, selector)
            WidgetType.JQWIDGETS -> InputField(context, selector)
        }

    fun inputfieldDelegate(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> InputFieldDelegate(context, selector)
            WidgetType.REACT -> InputFieldDelegate(context, selector)
            WidgetType.JQUERY -> InputFieldDelegate(context, selector)
            WidgetType.JQWIDGETS -> InputFieldDelegate(context, selector)
        }

    fun textfield(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> TextField(context, selector)
            WidgetType.REACT -> TextField(context, selector)
            WidgetType.JQUERY -> TextField(context, selector)
            WidgetType.JQWIDGETS -> TextField(context, selector)
        }

    fun textfieldDelegate(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> TextFieldDelegate(context, selector)
            WidgetType.REACT -> TextFieldDelegate(context, selector)
            WidgetType.JQUERY -> TextFieldDelegate(context, selector)
            WidgetType.JQWIDGETS -> TextFieldDelegate(context, selector)
        }

    fun hoverarea(context : TestContext, selector : String) =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> HoverArea(context, selector)
            WidgetType.REACT -> HoverArea(context, selector)
            WidgetType.JQUERY -> HoverArea(context, selector)
            WidgetType.JQWIDGETS -> HoverArea(context, selector)
        }

    fun <T> dropdownlist(context : TestContext, selector : String, entriesList: EnumEntries<T>) : AtomicWidget where T: Enum<T>, T: IPickValue =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> DropDownList(context, selector, entriesList)
            WidgetType.REACT -> DropDownList(context, selector, entriesList)
            WidgetType.JQUERY -> DropDownList(context, selector, entriesList)
            WidgetType.JQWIDGETS -> jqWidgetsDropDownList(context, selector, entriesList)
        }

    fun <T> dropdownlistDelegate(context : TestContext, selector : String, entriesList: EnumEntries<T>) : IDropDownListDelegate<T> where T : Enum<T>, T : IPickValue =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> DropDownListDelegate(context, selector, entriesList)
            WidgetType.REACT -> DropDownListDelegate(context, selector, entriesList)
            WidgetType.JQUERY -> DropDownListDelegate(context, selector, entriesList)
            WidgetType.JQWIDGETS -> jqWidgetsDropDownListDelegate(context, selector, entriesList)
        }

    fun <T> dynamicdropdownlist(context : TestContext, selector : String) : AtomicWidget =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> DynamicDropDownList(context, selector)
            WidgetType.REACT -> DynamicDropDownList(context, selector)
            WidgetType.JQUERY -> DynamicDropDownList(context, selector)
            WidgetType.JQWIDGETS -> jqWidgetsDynamicDropdownList(context, selector)
        }

    fun dynamicdropdownlistDelegate(context : TestContext, selector : String) : IDynamicDropDownListDelegate =
        when (context.hasBrowser!!.widgetType) {
            WidgetType.BASE -> DynamicDropDownListDelegate(context, selector)
            WidgetType.REACT -> DynamicDropDownListDelegate(context, selector)
            WidgetType.JQUERY -> DynamicDropDownListDelegate(context, selector)
            WidgetType.JQWIDGETS -> jqWidgetsDynamicDropDownListDelegate(context, selector)
        }


}
