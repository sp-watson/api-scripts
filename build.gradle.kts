import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    implementation("org.apache.poi:poi:5.1.0")
    implementation("org.apache.poi:poi-ooxml:5.1.0")
    testImplementation(kotlin("test"))
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.28.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}


tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "16"
        }
    }
    test {
        useJUnitPlatform {
            excludeTags("race-condition-test")
        }
    }
}

