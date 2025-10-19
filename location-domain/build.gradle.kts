plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    // Shared models module – domain works with data classes shared between layers
    implementation(project(":shared-models"))

    // Coroutines for async Flow-based APIs in repository contracts
    implementation(libs.kotlinx.coroutines.core)

    // Optional unit test dependencies
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}