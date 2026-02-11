package APIChallenges.testcases

// SearchTest
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.winterbe.expekt.expect
import qed.json.BigDecimalJsonAdapter
import qed.json.UuidAdapter
import qed.testbaseclass.BaseTest
import qed.testbaseclass.HasRest
import qed.testbaseclass.TestContext
import org.testng.annotations.Test
import java.math.BigDecimal
import java.util.*

private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")

private class APIChallengesMOSHITest : TestContext(baseTest, null, hasRest) {

    data class Department(
        val name: String,
        val code: UUID,
        val employees: List<Employee>
    )

    data class Employee(
        val firstName: String,
        val lastName: String,
        val title: String,
        val age: Int,
        val salary: BigDecimal
    )

    data class SalaryRecord(
        val employeeFirstName: String,
        val employeeLastName: String,
        val departmentCode: UUID,
        val departmentName: String,
        val sum: BigDecimal,
        val taxPercentage: BigDecimal
    )



    @OptIn(ExperimentalStdlibApi::class)
    @Test(priority = 0, description = "first test of API testing playground, JSON", groups = ["All"])
    fun MoshiTest() {
        val moshi = Moshi.Builder()
            .add(UuidAdapter())
            .add(BigDecimalJsonAdapter()) // And all other adapters
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val adapter = moshi.adapter<Department>()
        val uuid = UUID.randomUUID().toString()
        val dept = "{\"name\":\"Ane\",\"code\":\"cf20bb69-1ed0-49d8-a2b3-cd5a3db99149\",\"employees\":[{\"firstName\":\"Bert\",\"lastName\":\"van der Zaal\",\"title\":\"dr\",\"age\":65,\"salary\":100000.0}]}"
        val department = adapter.fromJson(dept)
        val deptJson = adapter.toJson(department)
        verify("assert that conversion and conversion back give the same json strings") {
            expect(deptJson).to.equal(dept)
        }


        val salaryRecordJsonAdapter = moshi.adapter<SalaryRecord>()
        val record = SalaryRecord("Ane", "Visser", UUID.randomUUID(), "tax", 100000.0.toBigDecimal(),200000.0.toBigDecimal())
        val serialized: String = salaryRecordJsonAdapter.toJson(record)
        println(serialized)
        val json = salaryRecordJsonAdapter.fromJson(serialized)
        println(json)
        verify("assert that conversion and conversion back give the same salary records") {
            expect(json).to.equal(record)
        }

    }


}