@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    `publishing-convention`
    `kotlin-multiplatform-convention`
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {

        commonMain {
            dependencies {
                api(projects.core)
                api(npm("idb-keyval", "6.2.1"))
            }
        }

        commonTest {
            dependencies {
                implementation(projects.tests)
            }
        }
    }
}
