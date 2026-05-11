@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    rulesMode = RulesMode.PREFER_SETTINGS
    versionCatalogs {
        create("libs") {
            from(files("../gradle/versions.toml"))
            // Mirror the main-settings IJ-sync AGP downgrade: buildSrc has its own libs
            // catalog, and without this override the convention plugins keep applying AGP
            // 9.1.1 to submodule projects while the host runs 9.0.0-alpha06 during sync,
            // tripping AgpVersionCompatibilityRule.
            val isIdeaSync = System.getProperty("idea.sync.active") == "true"
            val isAndroidStudio = System.getProperty("idea.platform.prefix") == "AndroidStudio"
            if (isIdeaSync && !isAndroidStudio) {
                version("android-gradle-plugin", "9.0.0-alpha06")
            }
        }
    }
}