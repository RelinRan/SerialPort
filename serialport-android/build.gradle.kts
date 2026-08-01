plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    id("org.jetbrains.dokka")
}

android {
    namespace = "io.github.relinran.serialport.android"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    sourceSets["main"].jniLibs.srcDirs("src/main/jniLibs")
    publishing { singleVariant("release") { withSourcesJar() } }
}

dependencies {
    api(project(":serialport-core"))
    implementation("androidx.annotation:annotation:1.9.1")
}

group = "io.github.relinran"
version = providers.gradleProperty("VERSION_NAME").get()

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
            }
        }
    }
}
