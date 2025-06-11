buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    java
    `java-library`
    eclipse
    id("com.gradleup.shadow") version "8.3.5"
}

allprojects {
    group = "me.realized"
    version = "3.5.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "eclipse")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
        maven { name = "purpur-repo"; url = uri("https://repo.purpurmc.org/snapshots") }
        maven { name = "spigot-repo"; url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { name = "bungeecord-repo"; url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { name = "mojang-repo"; url = uri("https://libraries.minecraft.net/") }
        maven { name = "clip-repo"; url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
        maven { name = "enginehub-repo"; url = uri("https://maven.enginehub.org/repo/") }
        maven { name = "codemc-repo"; url = uri("https://repo.codemc.io/repository/maven-public/") }
        maven { name = "essentialsx-repo"; url = uri("https://repo.essentialsx.net/releases/") }

        flatDir { dirs("${rootDir}/libs/") }
    }

    dependencies {
        compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
        compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        options.isFork = true
    }

    tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
            )
        }
    }

    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

