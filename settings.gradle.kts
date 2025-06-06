@file:Suppress("UnstableApiUsage")

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
    id("com.gradle.develocity") version "4.0.2"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    rulesMode = RulesMode.PREFER_SETTINGS
}

rootProject.name = "kotlin-document-store"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":core",
    ":samples:js-http-client",
    ":samples:kmp-app",
    ":samples:ktor-server",
    ":stores:browser",
    ":stores:leveldb",
    ":stores:mvstore",
    ":tests",
    ":version-catalog",
)

val levelDbPath: Path = file("../kotlin-leveldb").toPath()

val localLeveldbExists = levelDbPath.isDirectory() && levelDbPath.resolve("settings.gradle.kts").exists()

val useLocalLevelDb: Boolean? by settings

if (localLeveldbExists && !isCi && useLocalLevelDb == true) {
    includeBuild(levelDbPath.absolutePathString()) {
        val endings = listOf(
            "jvm",
            "js",

            "mingwx64",
            "linuxx64",
            "linuxarm64",
            "macosx64",
            "macosarm64",

            "iosarm64",
            "iosx64",
            "iosSimulatorarm64",
            "watchosarm64",
            "watchosx64",
            "watchosSimulatorarm64",
            "tvosarm64",
            "tvosx64",
            "tvosSimulatorarm64",
            "androidarm64",

            "android",
            "androidarm32",
            "androidx86",
            "androidx64"
        )
        dependencySubstitution {
            substitute(module("com.github.lamba92:kotlin-leveldb")).using(project(":"))
            endings.forEach {
                substitute(module("com.github.lamba92:kotlin-leveldb-$it")).using(project(":"))
            }
        }
    }
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        publishing {
            onlyIf { isCi }
        }
    }
}

val isCi
    get() = System.getenv("CI") == "true"