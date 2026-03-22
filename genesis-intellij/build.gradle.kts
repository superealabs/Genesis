val projectVersion = rootProject.version

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.3"
}

val targetIde = "IU"
val platformVersion = "2025.3"
val artifactSuffix = "intellij"

intellij {
    // Dynamiquement injecté selon le build (ou l'IDE actif)
    version.set(platformVersion)
    type.set(targetIde)
    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    implementation(project(":genesis-core"))
    implementation("com.fifesoft:rsyntaxtextarea:3.3.3")
}

tasks {
    patchPluginXml {
        // Extraction dynamique de la version de build (ex: "2024.3" -> "243", "2025.3" -> "253")
        val buildNumber = platformVersion.substring(2, 4) + platformVersion.substring(5, 6)
        sinceBuild.set(buildNumber)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildPlugin {
        archiveFileName.set("genesis-$artifactSuffix-${platformVersion}.zip")
    }

    buildSearchableOptions {
        enabled = false
    }
}
