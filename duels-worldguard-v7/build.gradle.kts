plugins { `java-library` }

dependencies {
    implementation("com.sk89q.worldguard:worldguard-bukkit:7.0.0") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    implementation(project(":duels-worldguard"))
}

