<h1>Duels-OG</h1> 

The [TrueOG Network](https://trueog.net) fork of [Duels](https://github.com/Realizedd/Duels) - a duel plugin for Spigot. <a href="https://www.spigotmc.org/resources/duels.20171/">Spigot Project Page</a>

Changes from Duels:

- Added a chat wizard for setting a bounty amount post-duel command in the GUI by @NotAlexNoyle
- Remove update checker by @NotAlexNoyle
- Only start Duels from one world, configurable in config.yml by @NotAlexNoyle
- Inventory loss bug fixes by @szumielxd
- Essentials API NPE fix by @skbeh
- Update deprecated pickup API by @NotAlexNoyle
- Added an option to hide the Duel winning title message by @NotAlexNoyle
- Added an option to hide the bet winning title message by @NotAlexNoyle
- Prevent players from stealing Duels items from the inventory by @dwaslashe
- Prevent players from stealing Duels items from the ground by @SrBedrock
- Whitelist arenas/worlds and fix duel transportation, with [PetTeleport-OG](https://github.com/true-og/PetTeleport-OG) and [HorseTp-OG](https://github.com/true-og/HorseTp-OG) support by @NotAlexNoyle
- Add [EternalCombat](https://github.com/EternalCodeTeam/EternalCombat) support by @NotAlexNoyle
- Replace BountyHunters hook with [PlayerBounties-OG](https://github.com/true-og/PlayerBounties-OG) support by @NotAlexNoyle
- Replaced Vault hook with [DiamondBank-OG](https://github.com/true-og/DiamondBank-OG) API by @NotAlexNoyle
- Replaced Multiverse hook with [MyWorlds](https://github.com/true-og/MyWorlds) API by @NotAlexNoyle

---

* **[Wiki](https://github.com/Realizedd/Duels/wiki)**
* **[Commands](https://github.com/Realizedd/Duels/wiki/commands)**
* **[Permissions](https://github.com/Realizedd/Duels/wiki/permissions)**
* **[Placeholders](https://github.com/Realizedd/Duels/wiki/placeholders)**
* **[Extensions](https://github.com/Realizedd/Duels/wiki/extensions)**
* **[config.yml](https://github.com/Realizedd/Duels/blob/master/duels-plugin/src/main/resources/config.yml)**
* **[lang.yml](https://github.com/Realizedd/Duels/blob/master/duels-plugin/src/main/resources/lang.yml)**
* **[Support Discord](https://discord.gg/RNy45sg)**


### Getting the dependency

#### Repository
Gradle:
```groovy
maven {
    name 'jitpack-repo'
    url 'https://jitpack.io'
}
```

Maven:
```xml
<repository>
  <id>jitpack-repo</id>
  <url>https://jitpack.io</url>
</repository>
```

#### Dependency
Gradle:
```groovy
compile group: 'com.github.Realizedd.Duels', name: 'duels-api', version: '3.5.1'
```  

Maven:
```xml
<dependency>
    <groupId>com.github.Realizedd.Duels</groupId>
    <artifactId>duels-api</artifactId>
    <version>3.5.1</version>
    <scope>provided</scope>
</dependency>
```

### plugin.yml
Add Duels as a soft-depend to ensure Duels is fully loaded before your plugin.
```yaml
soft-depend: [Duels]
```

### Getting the API instance

```java
@Override
public void onEnable() {
  Duels api = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");
}
```
