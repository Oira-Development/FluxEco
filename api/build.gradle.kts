plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

group = "io.oira"
version = findProperty("projectVersion")?.toString() ?: "1.0.0"

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.jar {
    archiveBaseName.set("FluxEco-API") // final jar will be like FluxEco-API-1.0.0.jar
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}
