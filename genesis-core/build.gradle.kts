val versions = rootProject.extra["versions"] as Map<*, *>

dependencies {
    // Runtime dependencies
    implementation("org.jline:jline:3.27.1")
    implementation("com.formdev:flatlaf:3.5.2")
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
    implementation("org.freemarker:freemarker:2.3.33")
    implementation("com.fasterxml.jackson.core:jackson-core:${versions["jackson"]}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${versions["jackson"]}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${versions["jackson"]}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.mysql:mysql-connector-j:9.0.0")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11")
    implementation("com.oracle.database.jdbc:ojdbc8:23.5.0.24.07")
    implementation("org.apache.velocity:velocity-engine-core:2.4.1")
    implementation("org.jetbrains:annotations:24.0.1")

    // Logging
    testImplementation("org.slf4j:slf4j-simple:2.0.12")
}