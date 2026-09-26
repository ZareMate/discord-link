# Discord Link

A NeoForge 1.21.1 mod that hosts its Discord bot inside the Minecraft server. No separate bot process is required.

## Flow

1. Player runs `/link`.
2. The mod creates a six-digit code valid for 15 minutes.
3. Player uses Discord `/link-account code:<code>`.
4. The embedded bot validates the code and stores the Minecraft UUID ↔ Discord ID mapping.
5. The first successful link grants the configured reward.
6. Offline players receive the reward when they next join.
7. `/unlinkdiscord` and Discord `/unlink-account` remove the association without removing the original reward.

## LuckPerms permissions

LuckPerms is supported on NeoForge. The admin commands use these permission nodes:

- `discordlink.admin.check` — `/checklink` and `/discordcheck`
- `discordlink.admin.unlink` — `/discordunlink`

LuckPerms permission checks include inherited permissions, so a group can be granted either node. A wildcard such as `discordlink.admin.*` can cover both through LuckPerms' normal permission calculation.

The personal commands `/link`, `/discord`, and `/unlinkdiscord` remain available to players without an admin permission.

## Minecraft commands

- `/link`
- `/discord`
- `/unlinkdiscord`
- `/checklink <player>` — `discordlink.admin.check`
- `/checklink id <discord-id>` — `discordlink.admin.check`
- `/discordcheck <player>` — `discordlink.admin.check`
- `/discordcheck id <discord-id>` — `discordlink.admin.check`
- `/discordunlink <player>` — `discordlink.admin.unlink`

## Discord commands

- `/link-account code:<six-digit-code>`
- `/unlink-account`

The bot registers the commands automatically when it starts.

## Configuration

After the first launch, edit `config/discordlink-common.toml`:

```toml
[discord]
botToken = "PUT-YOUR-BOT-TOKEN-HERE"
guildId = "YOUR-DISCORD-SERVER-ID"
inviteUrl = "https://discord.gg/m64gHWxhW7"

[reward]
item = "numismatics:cog"
count = 2
```

Never commit your bot token.

## Discord bot setup

Create a Discord application/bot and invite it to your server with the `bot` and `applications.commands` scopes. Put the bot token in `botToken`.

## Data

Persistent data is stored in `<minecraft-server>/discord-link.json`. It contains UUID/name mappings, Discord IDs/tags, link timestamps, reward state, pending rewards, and temporary codes.

## Server lifecycle

The bot starts with the Minecraft server and shuts down with it. If `botToken` is empty, Minecraft still starts normally and the Discord bot stays disabled.

## Build

Requires Java 21. The project uses NeoForge's current ModDevGradle toolchain for 1.21.1. ModDevGradle provides an alternate pipeline that can skip Minecraft decompilation/recompilation, which this server-only mod uses for faster and more reliable builds.

```bash
gradle clean build
```

The finished mod is written to `build/libs/`. JDA is packaged into the mod with NeoForge Jar-in-Jar.

## License

MIT
