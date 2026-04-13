plugins {
    val androidVersion = "9.1.1"
    id("com.android.library") version androidVersion apply false
    id("com.android.application") version androidVersion apply false

    val kotlinVersion = "2.3.20"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion apply false


    id("com.diffplug.spotless") version "8.4.0"
}