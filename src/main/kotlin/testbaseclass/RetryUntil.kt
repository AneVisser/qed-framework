package qed.testbaseclass

import com.winterbe.expekt.expect
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RetryUntil {
    internal var action: () -> Unit = {}
    internal var evaluateCondition: () -> Boolean = {false}
    internal var actOnConditionNotMet: (Exception) -> Unit = {}

    fun action(block: () -> Unit) { action = block }
    fun until(block: () -> Boolean) { evaluateCondition = block }
    fun onConditionNotMet(block: (Exception) -> Unit) { actOnConditionNotMet = block }
}

fun retryUntil(maxWaitTime: Duration = 30.seconds, pollInterval:Duration = 5.milliseconds, block: RetryUntil.() -> Unit) {
    val runner = RetryUntil().apply(block)
    try {
        val actualPollInterval = minOf(pollInterval, maxWaitTime)
        // check excess of maximum wait time of 5 minutes
        expect(maxWaitTime).below(5.minutes)
        // check excess of actual poll interval
        expect(actualPollInterval).below(10.seconds)
        val startTime = LocalDateTime.now()
        val maxEndTime = startTime.plusSeconds(maxWaitTime.inWholeSeconds)
        var conditionMet = false
        while (!conditionMet && LocalDateTime.now() < maxEndTime) {
            runner.action()
            Thread.sleep(actualPollInterval.inWholeMilliseconds)
            conditionMet = runner.evaluateCondition()
        }
        if (!conditionMet) {
            throw ConditionNotMetException()
        }
    } catch (ex: ConditionNotMetException) {
        runner.actOnConditionNotMet(ex)
    }
}

class ConditionNotMetException : Exception()
