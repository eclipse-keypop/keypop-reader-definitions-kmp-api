// `pluginManagement` comes first, as Gradle documents for the settings file: the block governs how
// the plugins of the build script are resolved, so nothing may precede it.
pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    // mavenLocal() carries the usual caveats -- no checksums, and a stale artifact silently
    // shadows the published one -- but it is how a change is tested across the Keypop modules
    // before being published. This module depends on none of them; the entry is kept so that
    // every Keypop KMP settings file differs only by `rootProject.name`.
    mavenLocal()
    mavenCentral()
  }
}

rootProject.name = "keypop-reader-definitions-kmp-api"
