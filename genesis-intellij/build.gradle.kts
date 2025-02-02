val projectVersion = rootProject.version

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.3"
}

intellij {
    version.set("2024.3")
    type.set("IU")
    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    implementation(project(":genesis-core"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("243.*")
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
        archiveFileName.set("genesis-intellij-${projectVersion}.zip")
    }

    buildSearchableOptions {
        enabled = false
    }
}