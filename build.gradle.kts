plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24" apply false
    id("com.gradleup.shadow") version "8.3.5" apply false
}

group = "org.labs"
version = "0.0.1-SNAPSHOT"

extra["versions"] = mapOf(
    "junit" to "5.10.3",
    "lombok" to "1.18.36",
    "kotlin" to "1.9.24",
    "jackson" to "2.18.1",
    "intellijPlugin" to "1.17.3"
)

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    val versions = rootProject.extra["versions"] as Map<*, *>

    dependencies {
        // Lombok
        compileOnly("org.projectlombok:lombok:${versions["lombok"]}")
        annotationProcessor("org.projectlombok:lombok:${versions["lombok"]}")

        implementation("org.jetbrains:annotations:24.0.1")

        // Testing
        testImplementation(platform("org.junit:junit-bom:${versions["junit"]}"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.mockito:mockito-core:5.11.0")
        testImplementation("org.assertj:assertj-core:3.25.3")
        testImplementation("org.jetbrains.kotlin:kotlin-test:${versions["kotlin"]}")
    }

    tasks {
        test {
            useJUnitPlatform()
        }

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions.jvmTarget = "17"
        }
    }
}