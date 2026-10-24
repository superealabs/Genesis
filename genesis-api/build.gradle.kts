plugins {
    id("org.springframework.boot") version "4.0.8"
    id("io.spring.dependency-management") version "1.1.7"
}

version = "1.0.0"

dependencies {
    implementation(project(":genesis-core"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation ("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
// Identité et configuration Git isolées pour les tests des sous-processus Core.
tasks.test {
    environment("GIT_CONFIG_GLOBAL", "/dev/null")
    environment("GIT_CONFIG_NOSYSTEM", "1")
    environment("GIT_CONFIG_COUNT", "4")
    environment("GIT_CONFIG_KEY_0", "user.name")
    environment("GIT_CONFIG_VALUE_0", "Genesis API tests")
    environment("GIT_CONFIG_KEY_1", "user.email")
    environment("GIT_CONFIG_VALUE_1", "genesis-tests@example.invalid")
    environment("GIT_CONFIG_KEY_2", "commit.gpgsign")
    environment("GIT_CONFIG_VALUE_2", "false")
    environment("GIT_CONFIG_KEY_3", "core.hooksPath")
    environment("GIT_CONFIG_VALUE_3", "/dev/null")
}
