val projectVersion = rootProject.version

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":genesis-core"))
    implementation("com.fifesoft:rsyntaxtextarea:3.3.3")
    implementation("org.jfree:jfreechart:1.5.4")

    intellijPlatform {
        intellijIdeaUltimate("2026.1.3")
        testBundledModule("com.intellij.modules.ultimate")
    }
}

intellijPlatform {
    pluginConfiguration {
        id.set("org.labs.genesis")
        name.set("Genesis")

        ideaVersion {
            sinceBuild.set("251")
            untilBuild.set("262.*")
        }
    }

    signing {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
