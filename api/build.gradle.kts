plugins { id("org.jetbrains.kotlin.jvm") }

group = "fr.sercurio"

version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.bundles.api.core)
    testImplementation(libs.bundles.unit.testing)
}

tasks.test { useJUnitPlatform() }
