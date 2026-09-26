package com.zaremate.discordlink;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(DiscordLinkMod.MOD_ID)
public final class DiscordLinkMod {
    public static final String MOD_ID = "discordlink";

    public DiscordLinkMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, DiscordLinkConfig.SPEC);
        DiscordLinkEvents.register();
    }
}
