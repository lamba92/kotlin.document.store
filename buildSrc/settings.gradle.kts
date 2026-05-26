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
            // Lockstep with the host IDEA-sync AGP pin: buildSrc has its own libs catalog, so it
            // needs the same 9.0.0 downgrade or AgpVersionCompatibilityRule trips during sync.
            val isIdeaSync = System.getProperty("idea.sync.active") == "true"
            val isAndroidStudio = System.getProperty("idea.platform.prefix") == "AndroidStudio"
            if (isIdeaSync && !isAndroidStudio) {
                version("android-gradle-plugin", "9.0.0")
            }
        }
    }
}