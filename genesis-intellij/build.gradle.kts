import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

val projectVersion = rootProject.version
val developmentIdeVersion = "2026.1.3"

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
        intellijIdeaUltimate(developmentIdeVersion)
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

// The development IDE has an isolated config directory. Inherit only its initial
// UI theme from the launching IDE; keep the rest of the sandbox settings separate.
fun copyLauncherTheme(sandboxConfig: File) {
    val explicitConfig = providers.gradleProperty("genesis.hostConfigDir")
        .orElse(providers.environmentVariable("IDEA_CONFIG_PATH"))
        .orNull
    val launcherVersion = providers.systemProperty("idea.version").orElse(developmentIdeVersion).get()
    val version = Regex("\\d{4}\\.\\d+").find(launcherVersion)?.value ?: "2026.1"
    val userHome = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    val settingsRoot = when {
        os.contains("mac") -> File(userHome, "Library/Application Support/JetBrains")
        os.contains("win") -> File(System.getenv("APPDATA") ?: File(userHome, "AppData/Roaming").path, "JetBrains")
        else -> File(System.getenv("XDG_CONFIG_HOME") ?: File(userHome, ".config").path, "JetBrains")
    }
    val candidates = if (explicitConfig != null) listOf(File(explicitConfig)) else listOf(
        File(settingsRoot, "IntelliJIdea$version"),
        File(settingsRoot, "IdeaIC$version")
    )
    // An existing options directory without laf.xml means the IDE uses its default
    // theme. Do not mistake that for an unknown config directory or retain an old override.
    val sourceConfig = candidates.firstOrNull { File(it, "options").isDirectory }
    if (sourceConfig == null) {
        logger.lifecycle("No launcher config found; set -Pgenesis.hostConfigDir=<IDE config directory> to select it.")
        return
    }
    val source = File(sourceConfig, "options/laf.xml")
    val target = File(sandboxConfig, "options/laf.xml")
    if (source.canonicalFile == target.canonicalFile) return
    if (source.isFile) {
        target.parentFile.mkdirs()
        source.copyTo(target, overwrite = true)
        logger.lifecycle("Inherited IntelliJ theme from ${sourceConfig.absolutePath}")
    } else {
        check(!target.exists() || target.delete()) { "Unable to clear the sandbox theme override: $target" }
        logger.lifecycle("Inherited default IntelliJ theme from ${sourceConfig.absolutePath} (no laf.xml override)")
    }
}

tasks.named<RunIdeTask>("runIde") {
    doFirst {
        copyLauncherTheme(sandboxConfigDirectory.get().asFile)
    }
}

// Allows verification of theme inheritance without opening another IDE process.
tasks.register("syncRunIdeTheme") {
    group = "intellij platform"
    description = "Copies the launcher IDE UI theme into the runIde sandbox."
    doLast {
        copyLauncherTheme(tasks.named<RunIdeTask>("runIde").get().sandboxConfigDirectory.get().asFile)
    }
}
