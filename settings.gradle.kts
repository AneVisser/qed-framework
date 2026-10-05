pluginManagement {
    plugins {
        kotlin("plugin.lombok") version "2.4.20"
    }
}
rootProject.name = "qed-framework"
includeBuild("QED-Shared") {
    dependencySubstitution {
        substitute(module("com.qed:QED-Shared")).using(project(":"))
    }
}
