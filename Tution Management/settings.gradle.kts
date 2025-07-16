


pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")  // Add JitPack repository here
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")  // Add JitPack repository here as well
    }
}

rootProject.name = "Tution Management"
include(":app")
