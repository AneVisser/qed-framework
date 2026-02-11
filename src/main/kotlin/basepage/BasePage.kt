package qed.basepage

import qed.testbaseclass.TestContext
import qed.testbaseclass.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * The annotation DSLMarker is used to limit the scope of each onPage function call to only have access to the relevant page functions,
 * so that methods in the outer scope of nested lambdas are not accessible, and yield an error when attempting to use them.
 * The error will read something like this: 'fun ...(): Unit' can't be called in this context by implicit receiver. Use explicit one if necessary.
 * These functions from other pages (i.e. outer lambda's) will not show in the IntelliJ prompt (code completion).
 *
 * see also https://medium.com/kotlin-and-kotlin-for-android/kotlin-dsl-coding-a-dsl-6-ee355be81106,
 * Kotlin DSL | Coding a DSL: 6— The @DslMarker annotation
 */


@Target(AnnotationTarget.CLASS)
@DslMarker
annotation class ShouldNotAccessHigherContainer

@ShouldNotAccessHigherContainer
open class BasePage(val context : TestContext) : IBaseTest by context{

    // the default function relies on the load-state of the page. It is better for applications to implement an
    // own function for each page that verifies for the presence of a given (unique) page element, or at least one that
    // the page you are coming from doesn't have. But uniqueness throughout the app is better.
    open fun hasLandedOnPage() : Boolean {
        Thread.sleep(300)
        var result = true
        // give the page some time to start loading
        retryUntil(200.milliseconds) {
            until {
                context.hasBrowser?.loadState != LoadStateSnapshot.COMPLETE
            }
        }
        // then wait for max 5 seconds until the page is loaded
        retryUntil(4.seconds) {
            until {
                context.hasBrowser?.loadState == LoadStateSnapshot.COMPLETE
            }
            onConditionNotMet {
                result = false
            }
        }
        return result
    }

    // pages you can navigate to from here
    fun <T> onPage(landingPage : GenericPage<T>, block: T.() -> Unit) {
        logger.indent()
        val pageName = landingPage.get()!!::class.toString().lastWord()
        logger.info { "landing on $pageName" }
        context.hasBrowser?.waitUntilLandedOnPage(landingPage.get() as BasePage)
        landingPage.get().block()
        logger.info { "exit lambda for $pageName" }
        logger.outdent()
    }

}