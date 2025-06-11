import org.apache.tools.ant.filters.ReplaceTokens
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.gradleup.shadow")
}

tasks.clean {
    doFirst { delete("$rootDir/out/") }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from(sourceSets.main.get().resources.srcDirs) {
        include("**/*.yml")
        filter<ReplaceTokens>("tokens" to mapOf("VERSION" to project.version))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    destinationDirectory.set(file("$rootDir/build/libs/"))
    archiveFileName.set("${parent!!.name}-${project.version}.jar")

    dependencies {
        include(project(":duels-api"))
        include(project(":duels-worldguard"))
        include(project(":duels-worldguard-v7"))
        include(dependency("com.fasterxml.jackson.core:.*"))
    }

    val relocateBase = "${project.group}.${parent!!.name.lowercase()}.shaded."
    relocate("com.fasterxml.jackson.core", "${relocateBase}jackson-core")
}

tasks.named("build") {
    dependsOn("shadowJar")
}

dependencies {
    compileOnly("org.jetbrains:annotations-java5:22.0.0")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    implementation("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT")
    implementation("com.mojang:authlib:1.5.21")
    implementation("me.clip:placeholderapi:2.11.6")
    implementation("com.SirBlobman.combatlogx:CombatLogX-API:10.0.0.0-SNAPSHOT")

    implementation("net.essentialsx:EssentialsX:2.19.2") {
        isTransitive = false
    }

    implementation(mapOf("name" to "MVdWPlaceholderAPI-3.1.1"))
    implementation(mapOf("name" to "Vault-1.6.7"))
    implementation(mapOf("name" to "CombatTagPlus"))
    implementation(mapOf("name" to "PvPManager-3.7.16"))
    implementation(mapOf("name" to "Factions-1.6.9.5-U0.1.14"))
    implementation(mapOf("name" to "MassiveCore"))
    implementation(mapOf("name" to "Factions"))
    implementation(mapOf("name" to "MyPet-2.3.4"))
    implementation(mapOf("name" to "BountyHunters-2.2.6"))
    implementation(mapOf("name" to "SimpleClans-2.14.4.1"))
    implementation(mapOf("name" to "LeaderHeadsAPI"))

    implementation(project(":duels-api"))
    implementation(project(":duels-worldguard"))
    implementation(project(":duels-worldguard-v7"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
}



