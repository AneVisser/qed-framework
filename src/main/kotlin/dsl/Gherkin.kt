package qed.testbaseclass

val startFont = "<font color=\"blue\"><b><u>"
val endFont = "</u></b></font>"

fun Given(owner : TestContext, description : String, block: () -> Unit ) {
    owner.logger.info { "${startFont}Given $description${endFont}" }
    block()
}

fun When(owner : TestContext, description : String, block: () -> Unit ) {
    owner.logger.info { "${startFont}When $description${endFont}" }
    block()
}

fun And(owner : TestContext, description : String, block: () -> Unit ) {
    owner.logger.info { "${startFont}And $description${endFont}" }
    block()
}

fun Then(owner : TestContext, description : String, block: () -> Unit ) {
    owner.logger.info { "${startFont}Then $description${endFont}" }
    block()
}

