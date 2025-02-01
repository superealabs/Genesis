dependencies {
    implementation(project(":genesis-core"))
}


tasks.jar {
    manifest {
        attributes["Main-Class"] = "GenesisApiCLI"
    }
}