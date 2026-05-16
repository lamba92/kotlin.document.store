@file:Suppress("OPT_IN_USAGE")

plugins {
    `publishing-convention`
    `kotlin-multiplatform-with-android-convention`
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    android {
        namespace = "com.github.lamba92.kotlin.document.store.core"
    }

    jvm()
    js {
        browser()
    }
    mingwX64()

    linuxX64()
    linuxArm64()

    macosArm64()

    iosArm64()
    iosSimulatorArm64()

    watchosArm64()
    watchosSimulatorArm64()

    tvosArm64()
    tvosSimulatorArm64()

    androidNativeX64()
    androidNativeX86()
    androidNativeArm64()
    androidNativeArm32()

    wasmWasi {
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {

        commonMain {
            dependencies {
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}
