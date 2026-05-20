import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import java.util.Locale

plugins {
    id("com.gradleup.shadow") version "8.3.9" apply false
    id("eclipse")
    `java-library`
}

fun localJar(name: String) = files(rootProject.file("libs/$name.jar"))

allprojects {
    group = "me.realized"
    version = "3.5.0"
}

subprojects {
    apply(plugin = "eclipse")
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()

        maven(url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")) {
            name = "spigot-repo"
        }

        maven(url = uri("https://oss.sonatype.org/content/repositories/snapshots/")) {
            name = "bungeecord-repo"
        }

        maven(url = uri("https://libraries.minecraft.net/")) {
            name = "mojang-repo"
        }

        maven(url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")) {
            name = "clip-repo"
        }

        maven(url = uri("https://maven.enginehub.org/repo/")) {
            name = "enginehub-repo"
        }

        maven(url = uri("https://repo.codemc.io/repository/maven-public/")) {
            name = "codemc-repo"
        }

        maven(url = uri("https://repo.essentialsx.net/releases/")) {
            name = "essentialsx-repo"
        }
    }
}

project(":duels-api") {
    dependencies {
        compileOnly("org.jetbrains:annotations-java5:22.0.0")
        compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT")
    }
}

project(":duels-worldguard") {
    dependencies {
        api("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT")
    }
}

project(":duels-worldguard-v6") {
    dependencies {
        compileOnly("com.sk89q:worldguard:6.1.1-SNAPSHOT") {
            exclude(group = "org.bukkit", module = "bukkit")
        }
        implementation(project(":duels-worldguard"))
    }
}

project(":duels-worldguard-v7") {
    dependencies {
        compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.0") {
            exclude(group = "org.bukkit", module = "bukkit")
        }
        implementation(project(":duels-worldguard"))
    }
}

project(":duels-plugin") {
    apply(plugin = "com.gradleup.shadow")

    val shade = configurations.create("shade") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    dependencies {
        compileOnly("org.jetbrains:annotations-java5:22.0.0")

        compileOnly("org.projectlombok:lombok:1.18.22")
        annotationProcessor("org.projectlombok:lombok:1.18.22")

        compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT")
        compileOnly("com.mojang:authlib:1.5.21")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("com.SirBlobman.combatlogx:CombatLogX-API:10.0.0.0-SNAPSHOT")

        compileOnly("net.essentialsx:EssentialsX:2.19.2") {
            isTransitive = false
        }

        compileOnly(localJar("MVdWPlaceholderAPI-3.1.1"))
        compileOnly(localJar("CombatTagPlus"))
        compileOnly(localJar("PvPManager-3.7.16"))
        compileOnly(localJar("Factions-1.6.9.5-U0.1.14"))
        compileOnly(localJar("MassiveCore"))
        compileOnly(localJar("Factions"))
        compileOnly(localJar("SimpleClans-2.14.4.1"))
        compileOnly(localJar("LeaderHeadsAPI"))

        implementation(project(":duels-api"))
        implementation(project(":duels-worldguard"))
        implementation(project(":duels-worldguard-v6"))
        implementation(project(":duels-worldguard-v7"))
        implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")

        add(shade.name, project(":duels-api"))
        add(shade.name, project(":duels-worldguard"))
        add(shade.name, project(":duels-worldguard-v6"))
        add(shade.name, project(":duels-worldguard-v7"))
        add(shade.name, "com.fasterxml.jackson.core:jackson-databind:2.13.1")
    }

    tasks.named<ProcessResources>("processResources") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filesMatching("**/*.yml") {
            filter(ReplaceTokens::class, "tokens" to mapOf("VERSION" to project.version.toString()))
        }
    }

    tasks.named<ShadowJar>("shadowJar") {
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
        archiveFileName.set("${rootProject.name}-${project.version}.jar")
        configurations = listOf(shade)

        val shadedPrefix = "${project.group}.${rootProject.name.lowercase(Locale.ROOT).replace("-", "_")}.shaded."
        relocate("com.fasterxml.jackson.core", shadedPrefix + "jackson-core")
    }

    tasks.named("build") {
        dependsOn(tasks.named("shadowJar"))
    }
}
