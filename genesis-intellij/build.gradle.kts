import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
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

    // Configuration du vérificateur pour la version 2.x du plugin
    verifyPlugin {
        failureLevel.set(listOf(
            VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS,
            VerifyPluginTask.FailureLevel.INVALID_PLUGIN,
            VerifyPluginTask.FailureLevel.INTERNAL_API_USAGES,
            VerifyPluginTask.FailureLevel.MISSING_DEPENDENCIES
            // DEPRECATED_API et SCHEDULED_FOR_REMOVAL_API_USAGES sont volontairement exclus
        ))
    }
}