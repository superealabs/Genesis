val projectVersion = rootProject.version

plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":genesis-core"))
}

tasks.register<JavaExec>("runGenesisCLI") {
    group = "application"
    description = "Lance la classe principale GenesisCLI"
    mainClass.set("org.labs.GenesisCLI")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

tasks.shadowJar {
    archiveBaseName.set("genesis-cli")
    archiveClassifier.set("")
    archiveVersion.set("$projectVersion")
    manifest {
        attributes["Main-Class"] = "org.labs.GenesisCLI"
    }
}
