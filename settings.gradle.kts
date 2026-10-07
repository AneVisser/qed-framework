pluginManagement {
    plugins {
        kotlin("plugin.lombok") version "2.4.20"
    }
}
rootProject.name = "qed-framework"
includeBuild("QED-Api-Contract") {
    dependencySubstitution {
        substitute(module("com.qed:QED-Api-Contract")).using(project(":"))
    }
}
