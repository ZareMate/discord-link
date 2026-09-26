package com.zaremate.discordlink;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.concurrent.atomic.AtomicReference;

public final class DiscordLinkEvents {
    private static final AtomicReference<DiscordLinkService> SERVICE = new AtomicReference<>();

    public static void register() {
        NeoForge.EVENT_BUS.register(new DiscordLinkEvents());
    }

    @SubscribeEvent
    public void serverStarting(ServerAboutToStartEvent event) {
        DiscordLinkService service = new DiscordLinkService(event.getServer());
        SERVICE.set(service);
        service.start();
    }

    @SubscribeEvent
    public void serverStopping(ServerStoppingEvent event) {
        DiscordLinkService service = SERVICE.getAndSet(null);
        if (service != null) service.stop();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        DiscordLinkCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        DiscordLinkService service = SERVICE.get();
        if (service != null && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            service.onPlayerLogin(player);
        }
    }

    static DiscordLinkService service() {
        return SERVICE.get();
    }
}
