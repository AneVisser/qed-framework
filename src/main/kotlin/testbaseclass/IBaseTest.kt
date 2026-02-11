package qed.testbaseclass

import qed.reports.Logger

interface IBaseTest {
    val logger : Logger
    fun verify(objective: String, verificationBlock: () -> Unit)
}