package E2E_UITestingPlayground.testcases

// SearchTest
import org.testng.Assert
import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.uitestingplayground.pages.UILandingPage
import qed.testbaseclass.*

private val baseTest = BaseTest()
private val hasBrowser = HasBrowser(baseTest, urlKey = "url")

private class E2E_UITestingPlayground2Test : TestContext(baseTest, hasBrowser) {

    @Test(priority=0, description = "second test of UI testing playground and lambda expressions", groups = ["All"])
    fun secondTest() {
        val landingPage = GenericPage(UILandingPage(this))

        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage){
                ScrollBars.click()
            }
        }

        // lambda expression (or anonymopuidf function):
        val company = {logger.info { "abc"}}

        fun testExpression() {
            company()
            company.invoke()
        }


        //generic: val lambda_name : Data_type = { argument_List -> code_body }
        val sum1  = { a: Int, b: Int -> a + b }
        // or more complete:
        val sum2:(Int, Int) -> Int = { a: Int, b: Int -> a + b }
        //of:
        val sum3:(Int, Int) -> Int = { a, b -> a + b }

        fun testExpressions() {
            logger.info{"sum1:" + sum1(2, 3)}
            logger.info{"sum2:" + sum2(2, 3)}
            logger.info{"sum3:" + sum3(2, 3)}
        }


        logger.info { "see also: https://www.geeksforgeeks.org/kotlin-lambdas-expressions-and-anonymous-functions/" }
        testExpression()
        testExpressions()

        Assert.assertEquals(sum2(2,4), sum3(2,4))  // dummy assertion



        // type of lambda must be declared, unless it is implicit:
        val lambda1: (Int) -> Int = {a -> a * a}
        val lambda2: (String,String) -> String = { a , b -> a + b }
        val lambda3: (Int)-> Unit = {logger.info { Int }}
        val lambda4: String.(Int) -> String = {"$this $it"}

        fun showlambdas() {
            logger.info{"lambda1(4):" +lambda1(4)}
            logger.info{"lambda2(\"4\", \"4\"):" +lambda2("4", "4")}
            logger.info{"lambda3(4):" +lambda3(4)}
            logger.info{"\"Geeks\".lambda4(3):" +"Geeks".lambda4(3)}
        }

        showlambdas()

        //return String value by lambda function
        val find = fun(num: Int): String{
            if(num % 2==0 && num < 0) {
                return "Number is even and negative"
            }
            else if (num %2 ==0 && num >0){
                return "Number is even and positive"
            }
            else if(num %2 !=0 && num < 0){
                return "Number is odd and negative"
            }
            else {
                return "Number is odd and positive"
            }
        }

        fun logFinds() {
            logger.info{"find(-12) " + find(-12) }
            logger.info{"find(-41) " + find(-41) }
            logger.info{"find(12) " + find(12) }
            logger.info{"find(41) " + find(41) }
        }
        logFinds()

    }

}