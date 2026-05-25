<h1>Duels-OG</h1> 

The [TrueOG Network](https://trueog.net) fork of [Duels](https://github.com/Realizedd/Duels) - a duel plugin for Spigot. <a href="https://www.spigotmc.org/resources/duels.20171/">Spigot Project Page</a>

Changes from Duels:

#### @NotAlexNoyle

- Added a chat wizard for setting a bounty amount post-duel command in the GUI
- Remove update checker
- Only start Duels from one world, configurable in config.yml
- Update deprecated pickup API
- Added an option to hide the Duel winning title message
- Added an option to hide the bet winning title message
- Whitelist arenas/worlds and fix duel transportation, with [PetTeleport-OG](https://github.com/true-og/PetTeleport-OG) and [HorseTp-OG](https://github.com/true-og/HorseTp-OG) support
- Add [EternalCombat](https://github.com/EternalCodeTeam/EternalCombat) support
- Replace BountyHunters hook with [PlayerBounties-OG](https://github.com/true-og/PlayerBounties-OG) support
- Replaced Vault hook with [DiamondBank-OG](https://github.com/true-og/DiamondBank-OG) API
- Removed Multiverse hook; world whitelisting now uses plain Bukkit world names with [MyWorlds](https://github.com/true-og/MyWorlds) listed as a soft-depend to ensure managed worlds load before Duels
- Bundled live TrueOG Network config files for plug-and-play deployment
- Bundled default kits (chain, iron, diamond, leather, netherite, debuff, nodebuff) in the plugin jar
- Removed WorldGuard region restrictions inside duel arenas
- Fixed victory message formatting
- Fixed kits equipping into an empty inventory caused by a delayed inventory reset wiping the kit
- Added separate configurable end-of-match broadcast commands for bet and no-bet duels
- Fixed diamond bet payouts so the total bounty equals the bet amount (the loser pays the winner, instead of both staking and the winner receiving double)
- Added a message telling the bet maker when their opponent cannot afford the bet
- Pluralize "Diamond"/"Diamonds" in messages based on the amount
- Fixed a bug where a selected kit could not be de-selected, blocking own-inventory duels
- Select a random kit and arena if none are selected
- Strip kit-item NBT from outgoing packets via [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) so legacy clients (1.8+ through ViaVersion) can eat, draw bows, and hold items without desync, while keeping kit-item protection intact

#### @szumielxd

- Inventory loss bug fixes

#### @skbeh

- Essentials API NPE fix

#### @dwaslashe

- Prevent players from stealing Duels items from the inventory

#### @SrBedrock

- Prevent players from stealing Duels items from the ground

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
