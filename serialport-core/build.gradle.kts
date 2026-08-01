plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jetbrains.dokka")
}

kotlin { jvmToolchain(21) }

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

group = "io.github.relinran"
version = providers.gradleProperty("VERSION_NAME").get()

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            artifact(tasks.dokkaHtml)
        }
    }
}
