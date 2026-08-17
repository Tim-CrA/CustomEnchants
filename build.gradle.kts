plugins {
    java
}

group = "dev.timca"
version = "1.2.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}

// Copy the built plugin to the test server
tasks.register<Copy>("installPlugin") {
    dependsOn(tasks.jar)
    from(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
    into("D:\\mcdevtest\\mc-srv\\mc2\\data\\plugins")
    rename { "${project.name}-${project.version}.jar" }
    doFirst {
        println("Copying plugin JAR: `${project.name}-${project.version}.jar` -> `D:\\mcdevtest\\mc-srv\\mc2\\data\\plugins`...")
    }
}