# IslamMod

IslamMod is a Minecraft Forge mod for Minecraft 26.2 that adds Islamic-themed gameplay, prayer mechanics, a mosque structure, an imam villager profession, and an in-game Quran reader.

## Features

- **Prayer mat:** Use the prayer mat during a designated salah window to perform Fajr, Dhuhr, Asr, Maghrib, or Isha.
- **Prayer reminders:** Players receive an in-game notification when each salah window begins.
- **Daily prayer requirement:** A player who completes no valid prayer during a Minecraft day is sent to the Nether when the next day begins.
- **Prayer rug:** Adds a placeable prayer rug block and item used by the imam profession.
- **Mosque structure:** Includes a domed mosque with full glass windows, a readable `MOSQUE` sign above the entrance, one prayer rug, a chest containing a prayer mat, a bed for the imam, and an entrance approach.
- **Imam villager:** Adds an imam profession linked to prayer rugs, with cleric-style trades and imam-specific localization.
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
