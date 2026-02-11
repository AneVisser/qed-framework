rootProject.name = "qed-demos"

// ── Composite build ─────────────────────────────────────────────────
// The demos live inside the qed-framework repo, so the framework
// is one directory up. SUT repos use the same pattern but with:
//   includeBuild("../qed-framework")
//
includeBuild("..")
includeBuild("../QED-Shared")