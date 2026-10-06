package qed.testbaseclass

// RequestType moved to QED-Api-Contract (qed.contract.RequestType) so multiplatform code can use it.
// This alias keeps existing code compiling unchanged; new code should import qed.contract.RequestType.
@Deprecated(
    "Moved to QED-Api-Contract. Use qed.contract.RequestType instead.",
    ReplaceWith("qed.contract.RequestType")
)
typealias RequestType = qed.contract.RequestType