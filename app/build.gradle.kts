plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin { jvmToolchain(17) }

android {
  namespace = "fr.sercurio.soulseek"
  compileSdk = 37

  defaultConfig {
    minSdk = 29
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    viewBinding = true
    compose = true
  }
}

dependencies {
  implementation(project(":api"))

  // UI / Android
  implementation(libs.bundles.android.ui)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.compose)
  implementation(libs.bundles.navigation)

  // Koin
  implementation(libs.bundles.koin.android.bundle)

  debugImplementation(libs.androidx.compose.ui.tooling)
  testImplementation(libs.bundles.unit.testing)
}
