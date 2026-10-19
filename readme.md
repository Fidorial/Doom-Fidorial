# Doom-Fidorial

Doom should be able to run everywhere, even on Minecraft! This is an example on
[Fidorial](https://github.com/Euphillya/Fidorial): Doom runs **on the server** (a Java port of
`linuxdoom-1.10`) and is shown on an **unmodified** Minecraft client, using nothing but a resource pack (core shaders)
and maps.

> [!IMPORTANT]
> **Unofficial fan project.** It is not affiliated with, endorsed by or sponsored by id Software,
> ZeniMax Media, Bethesda, Microsoft or Mojang.
>
> * The game engine is derived from the **DOOM source code released by id Software**
    >   ([id-Software/DOOM](https://github.com/id-Software/DOOM), `linuxdoom-1.10`), Copyright (C) 1993-1996
    >   id Software, Inc., now ZeniMax Media Inc., which id Software released under the
    >   **GNU General Public License, version 2**. Using, modifying and redistributing this code is
    >   subject to the terms of that license and to the rights of id Software and ZeniMax Media.
> * **No game data is included.** The commercial IWADs (`doom.wad`, `doom2.wad`, ...) are copyrighted by
    >   id Software and are not distributed here: use them only if you own a legitimate copy of the game.
    >   Without an IWAD, the plugin can download [Freedoom](https://freedoom.github.io/), a free and
    >   independent game made by its own community under a BSD license.
> * DOOM is a registered trademark of id Software LLC, a ZeniMax Media company. Minecraft is a trademark
    >   of Mojang AB. All other trademarks belong to their respective owners; they are used here only to
    >   describe what the project runs on.

## Requirements

|                  | Minimum                                                                                                                                                                                            |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Minecraft client | **Java Edition 26.4 Snapshot 4**, unmodified (no mod, no client-side plugin)                                                                                                                       |
| Server           | **Fidorial** for Minecraft 26.4 Snapshot 4, with `fidorial-api` `26.4-snapshot-4-build.29+0.1.0` or later, which has the player events and `MapView.hang` that Doom uses                           |
| Java             | **25**, to run the server and to build the plugin                                                                                                                                                  |
| Resource pack    | the pack the plugin serves must be **accepted** by each player: without it the screen and the sounds cannot be drawn. Set `pack-required=true` in `doom.properties` to kick players who decline it |
| Network          | clients must reach the pack's HTTP port, `pack-port` (TCP 8166 by default); outside a LAN, set `pack-public-url`                                                                                   |
| Game data        | an IWAD you own (`doom.wad`, `doom2.wad`, ...), or Internet access on the first start to download Freedoom                                                                                         |
| Players          | 1 to 4 per game (single player, cooperative or deathmatch); each running game uses one server thread at 35 Hz                                                                                      |

## Building

The plugin is built against `fr.fidorial:fidorial-api`, in the version given by `fidorialApiVersion` in
`gradle.properties`. It is looked up in the local Maven repository first, then on repo.euphyllia.moe:

* by default, `26.4-snapshot-4-build.+` takes the latest build published for that Minecraft version. A
  precise version can be pinned instead, `26.4-snapshot-4-build.<n>+0.1.0`, where `<n>` restarts at 1
  with each Minecraft version; the GitHub pre-release of every Fidorial build gives its exact version;
* with a clone of Fidorial, run `./gradlew :fidorial-api:publishToMavenLocal` there, then put the
  published version (for instance `26.4-snapshot-4-local+0.1.0`) in `fidorialApiVersion`.

Java 25 is required; the Gradle wrapper downloads it when needed.

* `./gradlew build` produces `build/libs/DoomPlugin.jar`;
* `./gradlew deploy -PfidorialServerDir=<server folder>` copies it into the `plugins/` folder of a server.

## Usage

1. Put `DoomPlugin.jar` in the `plugins/` folder of a Fidorial server and start it.
2. On the first start, the plugin creates `plugins/doom/doom.properties`. Put an IWAD in that folder (`doom.wad`,
   `doom2.wad`, `freedoom1.wad`...). Without one, Freedoom is downloaded, unless the
   download is turned off in `doom.properties`.
3. The resource pack is built on the first start (sounds and OPL music, about 1 to 2 minutes), cached in
   `plugins/doom/cache`, then served over HTTP on `pack-port` (8166). On the Internet, set
   `pack-public-url` (for instance `http://play.example.com:8166`) and open that port.
4. In game: `/doom` (clickable menu), `/doom play [skill 1-5] [episode] [map]`, `/doom coop ...`,
   `/doom deathmatch ...`, `/doom join <player>`, `/doom quit`.

## Controls

| Minecraft                             | Doom                          |
|---------------------------------------|-------------------------------|
| WASD (ZQSD on AZERTY)                 | move forward and back, strafe |
| mouse                                 | turn                          |
| right click (held) / left click       | fire                          |
| space                                 | use (doors, switches)         |
| Ctrl or Shift                         | run                           |
| 1 to 7                                | weapons; 9: next weapon       |
| F (swap hands)                        | automap                       |
| Q (drop; A on AZERTY)                 | toggle always run             |
| chat: `iddqd`, `idkfa`, `idclev12`... | cheat codes                   |

Other settings: `/doom size <3-11>`, `/doom map`, `/doom run`, `/doom cheat <code>`.

## How it works

* **Engine**: `engine/` is a faithful port of linuxdoom. The simulation matches the original tic for
  tic: the Freedoom demos play back exactly as in Chocolate Doom. Each game runs on its own thread at
  35 Hz, with up to 4 players (single player, cooperative, deathmatch), and the DEHACKED lumps of IWADs
  are supported (texts, frames, par times).
* **Picture**: the 320x200 screen is cut into 4 strips of 80 columns, each sent in a map (`map_item_data`, only the rows
  that changed). The 4 maps hang with `MapView.hang` in invisible
  frames that only the player sees, in the block of their head. The `text.vsh` shader recognises a
  signature in the header of each map and stretches its quad over its part of the screen; `text.fsh`
  reads back the color code of each pixel (a hash table of the 244 map colors) and draws it with the
  real Doom palette, including the 14 palettes for damage, bonuses and the radiation suit.
* **Sound**: the DMX sounds of the WAD are encoded to Ogg Vorbis by a small built-in Java encoder and
  played positionally around the player. Music (MUS or MIDI) is played by an OPL2 emulation (Nuked
  OPL3, bit for bit) driven like the DMX driver of Doom 1.9, then encoded.
* **Controls**: Fidorial API events (`PlayerInputEvent`, `PlayerUseItemEvent`, `PlayerSwingEvent`,
  `PlayerItemHeldEvent`, `PlayerSwapHandItemsEvent`, `PlayerDropItemEvent`, `PlayerMoveEvent`). The
  player is frozen with `Player.setAttributeBase` (`movement_speed`, `jump_strength` and interaction
  ranges at 0) and given back their values with `resetAttributeBase` when leaving.

## About `tables.bin`

`src/main/resources/fr/euphyllia/fidorial/doom/engine/tables.bin` is **not** game data and **not**
executable code. It holds the three trigonometry lookup tables of `tables.c` in `linuxdoom-1.10`,
copied value for value, and nothing else:

| Order | Table         | Entries | What it is                                                                                            |
|-------|---------------|---------|-------------------------------------------------------------------------------------------------------|
| 1     | `finetangent` | 4096    | tangent of the fine angles, in 16.16 fixed point                                                      |
| 2     | `finesine`    | 10240   | sine of the fine angles (5/4 of a turn, so that `finecosine` is a view into it), in 16.16 fixed point |
| 3     | `tantoangle`  | 2049    | arctangent of a slope, as a 32-bit binary angle                                                       |

Every value is a 32-bit big-endian integer, with no header: 16385 values, 65540 bytes. `Tables.java`
reads them in this order with a `DataInputStream`.

They live in a file rather than in Java source because arrays that large do not fit in the 64 KB a Java
method may hold. They cannot be recomputed with `Math.sin` either: the original values have their own
rounding, and the engine needs them exactly to stay tic for tic identical to Doom.

## License and credits

This project is distributed under the **GNU General Public License, version 2**: see [LICENSE](LICENSE).
It comes **without any warranty**. Fidorial itself is licensed separately; this plugin only uses its
API.

| Part                                                         | Derived from                                                                                                 | License            |
|--------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|--------------------|
| Game engine (`engine/`)                                      | DOOM source code, `linuxdoom-1.10`, by id Software ([id-Software/DOOM](https://github.com/id-Software/DOOM)) | GPL v2             |
| `engine/tables.bin`                                          | the `finetangent`, `finesine` and `tantoangle` tables of `tables.c` in `linuxdoom-1.10`                      | GPL v2             |
| OPL2 emulation (`audio/Opl2.java`)                           | [Nuked OPL3](https://github.com/nukeykt/Nuked-OPL3) by Nuke.YKT (Alexey Khokholov)                           | LGPL v2.1 or later |
| Music driver (`audio/MusicRenderer.java`)                    | `i_oplmusic.c` of [Chocolate Doom](https://github.com/chocolate-doom/chocolate-doom) by Simon Howard         | GPL v2 or later    |
| Fidorial integration, resource pack, shaders, Vorbis encoder | written for this project                                                                                     | GPL v2             |

DOOM, the original game and its data remain the property of id Software and ZeniMax Media. If you are
a rights holder and believe something here should not be, please open an issue.
