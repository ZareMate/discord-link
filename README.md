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

## Minecraft commands

- `/link`
- `/discord`
- `/unlinkdiscord`
- `/checklink <player>` — permission level 2
- `/checklink id <discord-id>` — permission level 2
- `/discordcheck <player>` — permission level 2
- `/discordcheck id <discord-id>` — permission level 2
- `/discordunlink <player>` — permission level 2

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

Requires Java 21:

```bash
./gradlew build
```

The finished mod is written to `build/libs/`. JDA is packaged into the mod with NeoForge Jar-in-Jar.

## License

MIT
