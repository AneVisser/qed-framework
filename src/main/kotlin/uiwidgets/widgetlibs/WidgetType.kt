/**
 * This is where widget types are defined. Their names can be used in the config json file for tests that use a browser.
 */

enum class WidgetType(val dirName : String) {
    BASE("base"),
    JQUERY("jquery"),
    JQWIDGETS("jqWidgets"),
    REACT("react");

    companion object {
        fun fromString(value: String) =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.dirName.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown widget type: $value")
    }
}
