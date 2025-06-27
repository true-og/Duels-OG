buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("java") // Tell gradle this is a java project.
    id("java-library") // Import helper for source-based libraries.
    id("com.diffplug.spotless") version "7.0.4" // Import auto-formatter.
    id("com.gradleup.shadow") version "8.3.6" // Import shadow API.
    eclipse // Import eclipse plugin for IDE integration.
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "purpur-repo"
        url = uri("https://repo.purpurmc.org/snapshots")
    }
    maven {
        name = "spigot-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "bungeecord-repo"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    maven {
        name = "mojang-repo"
        url = uri("https://libraries.minecraft.net/")
    }
    maven {
        name = "clip-repo"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "enginehub-repo"
        url = uri("https://maven.enginehub.org/repo/")
    }
    maven {
        name = "codemc-repo"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven {
        name = "essentialsx-repo"
        url = uri("https://repo.essentialsx.net/releases/")
    }
    flatDir { dirs("${rootDir}/libs/") }
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
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "purpur-repo"
            url = uri("https://repo.purpurmc.org/snapshots")
        }
        maven {
            name = "spigot-repo"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
        maven {
            name = "bungeecord-repo"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            name = "mojang-repo"
            url = uri("https://libraries.minecraft.net/")
        }
        maven {
            name = "clip-repo"
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
        maven {
            name = "enginehub-repo"
            url = uri("https://maven.enginehub.org/repo/")
        }
        maven {
            name = "codemc-repo"
            url = uri("https://repo.codemc.io/repository/maven-public/")
        }
        maven {
            name = "essentialsx-repo"
            url = uri("https://repo.essentialsx.net/releases/")
        }
        flatDir { dirs("${rootDir}/libs/") }
    }

    dependencies {
        compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT") // Declare purpur API version to be packaged.
        compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3") // Import MiniPlaceholders API.
        implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.compilerArgs.add("-Xlint:deprecation") // Triggers deprecation warning messages.
        options.encoding = "UTF-8"
        options.isFork = true
    }

    tasks.withType<Jar> {
        manifest { attributes("Implementation-Title" to project.name, "Implementation-Version" to project.version) }
    }

    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    spotless {
        java {
            removeUnusedImports()
            palantirJavaFormat()
        }
    }

    tasks.named("build") { dependsOn("spotlessApply") }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}

spotless {
    java {
        removeUnusedImports()
        palantirJavaFormat()
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
        target("build.gradle.kts", "settings.gradle.kts")
    }
}

tasks.named("build") { dependsOn("spotlessApply") }
