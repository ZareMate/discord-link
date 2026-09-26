package com.zaremate.discordlink;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DiscordLinkConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> BOT_TOKEN;
    public static final ModConfigSpec.ConfigValue<String> GUILD_ID;
    public static final ModConfigSpec.ConfigValue<String> INVITE_URL;
    public static final ModConfigSpec.ConfigValue<String> REWARD_ITEM;
    public static final ModConfigSpec.IntValue REWARD_COUNT;
    public static final ModConfigSpec.ConfigValue<String> REWARD_NAME;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Discord bot settings. Never commit botToken.").push("discord");
        BOT_TOKEN = b.define("botToken", "");
        GUILD_ID = b.define("guildId", "");
        INVITE_URL = b.define("inviteUrl", "https://discord.gg/m64gHWxhW7");
        b.pop();
        b.comment("First-link reward.").push("reward");
        REWARD_ITEM = b.define("item", "numismatics:cog");
        REWARD_COUNT = b.defineInRange("count", 2, 1, 64);
        REWARD_NAME = b.define("name", "2 Cogs");
        b.pop();
        SPEC = b.build();
    }

    private DiscordLinkConfig() {}
}
