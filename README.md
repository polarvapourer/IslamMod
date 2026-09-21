# IslamMod

IslamMod is a Minecraft Forge mod for Minecraft 26.2 that adds Islamic-themed gameplay, prayer mechanics, a mosque structure, an imam villager profession, and an in-game Quran reader.

## Features

- **Prayer mat:** Use the prayer mat during a designated salah window to perform Fajr, Dhuhr, Asr, Maghrib, or Isha.
- **Prayer reminders:** Players receive an in-game notification when each salah window begins.
- **Daily prayer requirement:** A player who completes no valid prayer during a Minecraft day is sent to the Nether when the next day begins.
- **Nether trial:** Players sent to the Nether receive three randomly selected collection tasks from a Nether-material pool before the return portal will unlock.
- **Minbar:** Adds a placeable minbar block and item used by the imam profession.
- **Mosque structure:** Includes a domed mosque with full glass windows, a readable `MOSQUE` sign above the entrance, one minbar, a chest containing a prayer mat, a bed for the imam, and an entrance approach.
- **Imam villager:** Adds an imam profession linked to minbars, with cleric-style trades and imam-specific localization.
- **Quran:** Adds a Quran item that opens the bundled Quran text in a book-style GUI.
- **Halal and haram food:** Recognized haram foods, including pork, rotten flesh, spider eyes, poisonous potatoes, and pufferfish, poison the player when eaten. Other foods are left unchanged.
- **Project identity:** Uses the `islammod` mod ID and the `IslamMod` project name.

## Building

Use the included Gradle wrapper:

```powershell
.\gradlew.bat clean build
```

The built mod JAR is created in `build\libs`.

## Running the client

```powershell
.\gradlew.bat runClient
```

This project requires Java 25, Forge 65.1.0, and Minecraft 26.2.

## Downloads

- [Download IslamMod version 1.0.3](https://github.com/polarvapourer/IslamMod/releases/tag/v1.0.3)
- [Download the complete project as a ZIP](https://github.com/polarvapourer/IslamMod/archive/refs/heads/main.zip)

The release page is the recommended download location. The project ZIP includes the required Minecraft resources, textures, structure data, Gradle files, and build configuration.

## Installing the downloaded JAR

The downloaded JAR is a **Forge** mod and will not load in a vanilla Minecraft profile.

1. Install Minecraft Forge **65.1.x for Minecraft 26.2**.
2. Launch the Forge 65.1.x installation once, then close Minecraft.
3. Copy `IslamMod-1.0.3.jar` directly into the `mods` folder for that Forge installation. On Windows, this is normally `%APPDATA%\.minecraft\mods`. Do not put it in a version subfolder such as `%APPDATA%\.minecraft\mods\26.2`.
4. Launch Minecraft using the Forge installation, not the standard/latest-release installation.

The JAR must be used with Minecraft 26.2 and Forge 65.1.x. It is not compatible with a different Minecraft version, Fabric, NeoForge, or a vanilla profile. Download a newly built JAR after source changes; an older release asset will not include later fixes.
