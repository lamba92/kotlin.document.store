plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }
    macosArm64()
    iosArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
                api(libs.kotlinx.serialization.core)
            }
        }
    }
}
