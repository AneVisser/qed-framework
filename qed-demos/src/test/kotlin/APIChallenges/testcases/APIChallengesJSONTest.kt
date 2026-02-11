package APIChallenges.testcases

// SearchTest
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.testng.annotations.Test
import qed.json.DoubleJsonAdapter
import qed.json.IntJsonAdapter
import qed.testbaseclass.BaseTest
import qed.testbaseclass.HasRest
import qed.testbaseclass.TestContext
import qed.json.QEDJson

private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")

private class APIChallengesJSONTest :  TestContext(baseTest, null, hasRest) {

    data class ZZZ(
        val zzz : Int = 3
    )

    data class WithinJSONObj(
        val withinJSONObj: String = "next level down"
    )

    data class Length(
        val a : Int = 3,
        val b : Int = 4,
        val c : Int = 5,
        val d : WithinJSONObj = WithinJSONObj()
    )

    data class Body(
        val height1 : Int = 173,
        val height2: Long = 1000000000,
        val weight: Double = 80.1,
        val width: Int? = null,
        val width2: Int = 555,
        val isBig: Boolean = true,
        val length : Length
    )

    data class Nested (
        val nested: String = "next level down"
    )

    val rimslst :List<Any> = listOf(
        1,
        2,
        3,
        Nested()
    )

    data class TyresAndRims(
        val tyres: Int? = null,
        val rims: List<Any>
    )

    data class Fuel(
        val fuel : String = "petrol"
    )

    val randomList : List<Any> = listOf(
        "abc",
        "def",
        "ghi"
    )

    val carslst : List<Any?> = listOf (
        "Tesla",
        "Porsche",
        "BMW",
        "Ferrari",
        100.5,
        30,
        1000000000,
        null,
        true,
        TyresAndRims(
            rims = rimslst
        ),
        null,
        Fuel(),
        randomList
    )

    data class Something(
        val test1: ZZZ = ZZZ(),
        val test2: List<Int>,
        val name: String = "Roy",
        val city : String = "Auckland",
        val body : Body,
        val cars : List<Any?>
    )


    @OptIn(ExperimentalStdlibApi::class)
    @Test(priority = 0, description = "first test of API testing playground, JSON", groups = ["All"])
    fun JSONTest() {
        val something = Something(test2 = listOf(3), body = Body(length = Length()), cars = carslst)

        val moshi = Moshi.Builder()
            .add(DoubleJsonAdapter())
            .add(IntJsonAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter<Something>()
        val deptJson = adapter.toJson(something)
        logger.info { deptJson }
        println(deptJson)
        val testJson = "{\"test1\":{\"zzz\":3},\"test2\":[3],\"name\":\"Roy\",\"city\":\"Auckland\",\"body\":{\"height1\":173,\"height2\":1000000000,\"weight\":80.1,\"width\":null,\"width2\":555,\"isBig\":true,\"length\":{\"a\":3,\"b\":4,\"c\":5,\"d\":{\"withinJSONObj\":\"next level down\"}}},\"cars\":[\"Tesla\",\"Porsche\",\"BMW\",\"Ferrari\",100.5,30,1000000000,null,true,{\"tyres\":null,\"rims\":[1,2,3,{\"nested\":\"next level down\"}]},null,{\"fuel\":\"petrol\"},[\"abc\",\"def\",\"ghi\"]]}"
        rest.jsonVerify(deptJson, testJson)
        logger.info{ QEDJson.mapFromJson(testJson).toString() }

    }


}