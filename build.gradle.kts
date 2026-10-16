import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    java
}

group = "dev.timca"
version = "2.1.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

val latestApi = configurations.create("latestApi")

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    latestApi("io.papermc.paper:paper-api:26.2.build.129-stable")
}

// Compile-check against the newest Paper API with JDK 25: catches API drift that
// would break the plugin on the latest servers while the main build targets 1.21.11.
val compileLatest by tasks.registering(JavaCompile::class) {
    source = sourceSets.main.get().allJava
    classpath = latestApi
    destinationDirectory.set(layout.buildDirectory.dir("classes-latest"))
    options.encoding = "UTF-8"
    options.release = 17
    javaCompiler.set(project.the<JavaToolchainService>().compilerFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
}

tasks.register("checkLatestApi") {
    group = "verification"
    description = "Compile the plugin against the newest Paper API (26.2)"
    dependsOn(compileLatest)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // Java 17 bytecode so the jar also loads on servers running Java 17
    // (Minecraft 1.18+); Paper-only APIs are guarded at runtime.
    options.release = 17
}

// paper-api is compiled for JVM 21; keep resolving that variant while emitting
// Java 17 bytecode from our own sources.
configurations.compileClasspath {
    attributes {
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
    }
}

tasks.processResources {
    filesMatching(listOf("paper-plugin.yml", "plugin.yml")) {
        expand("version" to project.version)
    }
}
