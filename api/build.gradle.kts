plugins {
    val kotlinVersion = "2.2.0"
    kotlin("jvm") version kotlinVersion
}

group = "fr.sercurio"

version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("io.ktor:ktor-network:2.2.3")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("org.apache.commons:commons-configuration2:2.12.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}



tasks.test { useJUnitPlatform() }
