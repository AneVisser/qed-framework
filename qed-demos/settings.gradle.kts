rootProject.name = "qed-demos"

// ── Composite build ─────────────────────────────────────────────────
// The demos live inside the qed-framework repo, so the framework
// is one directory up. SUT repos use the same pattern but with:
//   includeBuild("../qed-framework")
//
includeBuild("..")
// QED-Shared is deprecated as of 2.0.0 and replaced by QED-Api-Contract:
// includeBuild("../QED-Shared")
includeBuild("../QED-Api-Contract")