package qed.testbaseclass

import qed.json.QEDJson
import qed.reports.Logger

/**
 * recursively traverse through the node and verify if each element exists in the test node
 *  singular values are represented by JsonNodeType.BOOLEAN, JsonNodeType.NULL, JsonNodeType.BINARY, JsonNodeType.MISSING,
 *  JsonNodeType.STRING, JsonNodeType.POJO, JsonNodeType.NUMBER
 *  when an element is an object or array, it needs to be reprocessed until singular values are encountered
 *  An alterative method to to compare json files is on https://semanticdiff.com/online-diff/json/
 *  This site has two panes where you can compare json files to spot differences.
 */

class JsonVerify(expected: String, testValue: String, val logger: Logger) {

    private fun traverse(expected: Any?, actual: Any?) {
        when {
            expected is Map<*, *> && actual is Map<*, *> -> {
                expected.forEach { (key, expValue) ->
                    logger.info { "$key : (object)" }
                    val actValue = actual[key]
                    traverse(expValue, actValue)
                }
            }
            expected is List<*> && actual is List<*> -> {
                expected.forEach { expElem ->
                    logger.info { expElem }
                    assert(actual.contains(expElem)) // could be enhanced with deep comparison
                }
            }
            else -> {
                logger.info { "$expected" }
                assert(expected == actual)
            }
        }
    }

    init {
        val expJson = QEDJson.mapFromJson(expected)
        val testJson = QEDJson.mapFromJson(testValue)
        traverse(expJson, testJson)
    }
}
