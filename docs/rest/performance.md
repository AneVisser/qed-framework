**QED’s performance testing framework** leverages Kotlin coroutines for realistic, concurrent load simulation. With built-in telemetry, stress-level control, and markdown-friendly reporting, it enables longitudinal performance tracking with minimal ceremony.

The core of a performance test is the PerformanceTestContext, an extension of TestContext. A performance test is structured in two parts:

 - a coroutine block that executes the test logic, and
 - a control block that manages the concurrent execution of those coroutines.

The following code segment shows how to do that:

```kotlin
private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")

private class APIChallengesPerformanceTest : PerformanceTestContext(baseTest, null, 
    hasRest, stressLevel = Stresslevel.BASELINE) {

    @Test(priority = 0, description = "Concurrent POST test with jitter", groups = ["All"])
    fun Post1Test() = runConcurrentTest("Post1Test") {
        logger.info{ "StressLevel: $stressLevel"}

        logger.info { "START TEST" }
        coroutineScope {
            val jobs = (0..9).map {
                launch(ctx) {
                    // delay adds some jitter to make simulation more realistic
                    delay(Random.nextLong(10, 100))
                    doPost(it)
                }
            }
            jobs.joinAll() // Wait for all coroutines to complete
        }
        logger.info { "END TEST" }
    }
}
```

## Setup Guide

 - **Structured Concurrency**. Use **coroutineScope** for structured concurrency.
 - The use of **PerformanceTestContext** instead of TestContext is important. The constructor parameters are the same 
 (e.g., HasBrowser?, HasRest?)
 - Annotate the test function with @Test and wrap it in **=runConcurrentTest(name)**
 - Launch of the coroutines using **launch(Dispatchers.IO + coroutineContext){...}**. In the above example this is done through the block **val jobs =...**. In this case, **doPost** is the coroutine that executes the test.
 - **Stress Level Control**. The parameter **stressLevel = StressLevel.BASELINE** or **StressLevel.STRESS** determines what the value of the ctx CoroutineContext parameter is that is used in the launch function. With STRESS, the IO pools are flooded, and this option gives an unrealistic load. The value defaults to StressLevel.BASELINE, which is a better representation of reality. 
 - Call **jobs.joinAll()** to wait for all coroutine instances to finish
 - It is good practice to add a logging line to the report about the current stress level. 


The next step will be to setup the coroutine. An example is below:
```kotlin
    suspend fun doPost(index : Int) = withIOContext  {
        // jitter in ms
        delay(Random.nextLong(10, 100))
        val json = generatePayload(index)
        val result = rest.sendUntyped(RequestType.POST, APIChalURLPath.SIM_ENTITIES, json, 201, trackPerformance = true)

        val logger = currentCoroutineContext()[LoggerContext]?.logger ?: error("LoggerContext missing")

        logger.info { "POST #$index executed in coroutine" }

        verify("check response body for name=bob and id=11") {
            expect(result.get("name")).to.equal("bob")
            expect(result.get("id")).to.equal(11)
        }
    }
```
Important elements here are:

 - **suspend**: marks the function as suspendable, so it can be paused and resumed without blocking the thread.
 - **= withIOContext {...}**: ensures execution runs on the IO dispatcher. Here the function is defined as a lambda with receiver.
 - **trackPerformance = true**: every rest.send request that has this parameter set to true will be tracked for performance.


## Performance test execution report

If in a test suite at least one Rest send request is executed with **trackPerformance = true**, then
QED will automatically generate a performance report. The performance summary shows at bottom of the left hand side
list test reports in Extent Reports ("Performance Summary").
The performance summary report has a top bar with the pooled average duration of all send requests in the suite, 
and a breakdown of individual requests by endpoint and request type.

![img.png](../dsl/img.png)

## Performance history report
You can configure for how long you want to retain the history (in terms of number of commits of 
the system under test and the number of test runs per commit). These configuration settings can be added 
to the config file in your project root directory. In order to obtain the last commit number of the repo, you will need
to set the 'repository' property of the 'testrunmetadata' property, as well as the name of the system under test ("sut").
The latter will be used to create a subdirectory where the json files containing the run-history will be kept. 
This is a subdirectory of your SED directory "perf-history". So for the case of the json snippet below, the data would be kept in 
\<QED\>/perf-history/apichallenges.  
History retention is based on commit lineage and capped by maxRunsPerCommit and maxCommitsToKeep. This ensures 
meaningful trend analysis without bloating the report directory.
```json
"testrunmetadata": {
    "repository": "C:\\Projects\\qed",
    "sut":  "apichallenges",
    "maxRunsPerCommit" : 5,
    "maxCommitsToKeep" : 20
}
```

The history chart shows the variation in average response time per test run for each endpoint/request type.
![img_1.png](img_1.png)