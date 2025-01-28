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

        val webMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(npm("idb-keyval", "6.2.1"))
                api(projects.core)
            }
        }
        jsMain {
            dependsOn(webMain)
        }
        wasmJsMain {
            dependsOn(webMain)
        }
        jsTest {
            dependencies {
                implementation(projects.tests)
            }
        }
        wasmJsTest {
            dependencies {
                implementation(projects.tests)
            }
        }
    }
}
