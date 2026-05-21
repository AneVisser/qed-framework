rootProject.name = "qed-framework"
includeBuild("QED-Shared") {
    dependencySubstitution {
        substitute(module("com.qed:QED-Shared")).using(project(":"))
    }
}
