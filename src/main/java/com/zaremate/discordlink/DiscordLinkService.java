package com.zaremate.discordlink;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class DiscordLinkService extends ListenerAdapter {
    private final MinecraftServer server;
    private final DiscordLinkStore store;
    private JDA jda;

    public DiscordLinkService(MinecraftServer server) {
        this.server = server;
        Path file = server.getServerDirectory().resolve("discord-link.json");
        this.store = new DiscordLinkStore(file);
    }

    public void start() {
        String token = DiscordLinkConfig.BOT_TOKEN.get();
        if (token == null || token.isBlank()) {
            System.out.println("[DiscordLink] Bot not started: config/discordlink-common.toml has an empty botToken.");
            return;
        }
        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(this)
                    .build();
        } catch (Exception e) {
            System.err.println("[DiscordLink] Failed to start Discord bot: " + e);
        }
    }

    public void stop() {
        if (jda != null) {
            jda.shutdownNow();
            jda = null;
        }
    }

    public void onPlayerLogin(ServerPlayer player) {
        if (store.takeOwed(player.getUUID())) {
            giveReward(player);
            player.sendSystemMessage(Component.literal("Discord link reward added to your inventory."));
        }
        if (store.get(player.getUUID()) == null) {
            player.sendSystemMessage(Component.literal("Link your Discord with /link to get the Discord-link reward."));
        }
    }

    public String generateCode(ServerPlayer player) {
        String code;
        do {
            code = Integer.toString(ThreadLocalRandom.current().nextInt(100000, 1000000));
        } while (store.consumeCode(code) != null);
        store.createCode(player.getUUID(), player.getGameProfile().getName(), code);
        return code;
    }

    public boolean confirm(String code, String discordId, String discordTag) {
        DiscordLinkStore.Code ticket = store.consumeCode(code.trim());
        if (ticket == null) return false;

        UUID uuid = UUID.fromString(ticket.uuid);
        UUID owner = store.ownerOf(discordId);
        if (owner != null && !owner.equals(uuid)) return false;

        DiscordLinkStore.Link link = store.get(uuid);
        boolean alreadyRewarded = link != null && link.rewarded;
        if (link == null) {
            link = new DiscordLinkStore.Link();
            link.uuid = ticket.uuid;
            link.minecraftName = ticket.minecraftName;
        }

        link.discordId = discordId;
        link.discordTag = discordTag;
        link.linkedAt = System.currentTimeMillis();
        link.rewarded = true;
        store.putLink(link);

        if (!alreadyRewarded) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                giveReward(player);
                player.sendSystemMessage(Component.literal("Discord linked to " + discordTag + ". You received your reward!"));
            } else {
                store.owe(uuid);
            }
        }
        return true;
    }

    public boolean unlink(UUID uuid) {
        DiscordLinkStore.Link link = store.get(uuid);
        if (link == null || link.discordId == null) return false;
        store.unlink(uuid);
        return true;
    }

    public DiscordLinkStore.Link getLink(UUID uuid) {
        return store.get(uuid);
    }

    public UUID getLinkByDiscord(String discordId) {
        return store.ownerOf(discordId);
    }

    private void giveReward(ServerPlayer player) {
        ResourceLocation id = ResourceLocation.parse(DiscordLinkConfig.REWARD_ITEM.get());
        var item = BuiltInRegistries.ITEM.get(id);
        if (item == null) {
            System.err.println("[DiscordLink] Reward item does not exist: " + id);
            return;
        }
        player.getInventory().placeItemBackInInventory(
                new ItemStack(item, DiscordLinkConfig.REWARD_COUNT.get()));
    }

    @Override
    public void onReady(ReadyEvent event) {
        registerDiscordCommands(event.getJDA());
    }

    private void registerDiscordCommands(JDA api) {
        String guildId = DiscordLinkConfig.GUILD_ID.get();
        var commands = java.util.List.of(
                Commands.slash("link-account", "Link your Minecraft account")
                        .addOption(OptionType.STRING, "code", "Six-digit code from /link", true),
                Commands.slash("unlink-account", "Unlink your Minecraft account")
        );
        if (guildId != null && !guildId.isBlank() && api.getGuildById(guildId) != null) {
            api.getGuildById(guildId).updateCommands().addCommands(commands).queue();
        } else {
            api.updateCommands().addCommands(commands).queue();
        }
        System.out.println("[DiscordLink] Discord bot online as " + api.getSelfUser().getAsTag());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String guildId = DiscordLinkConfig.GUILD_ID.get();
        if (guildId != null && !guildId.isBlank() && (event.getGuild() == null || !guildId.equals(event.getGuild().getId()))) {
            event.reply("This bot is not configured for this server.").setEphemeral(true).queue();
            return;
        }
        if (event.getName().equals("link-account")) {
            String code = event.getOption("code").getAsString();
            User user = event.getUser();
            event.deferReply(true).queue(hook -> server.execute(() -> {
                boolean ok = confirm(code, user.getId(), user.getAsTag());
                hook.editOriginal(ok
                        ? "Your Discord account is now linked to the Minecraft account."
                        : "Invalid/expired code, or your Discord account is already linked to another Minecraft account.")
                        .queue();
            }));
        } else if (event.getName().equals("unlink-account")) {
            UUID uuid = getLinkByDiscord(event.getUser().getId());
            event.deferReply(true).queue(hook -> {
                if (uuid == null) {
                    hook.editOriginal("Your Discord account is not linked.").queue();
                    return;
                }
                server.execute(() -> hook.editOriginal(
                        unlink(uuid) ? "Your Discord account has been unlinked. You keep the original reward."
                                     : "The account could not be unlinked.").queue());
            });
        }
    }
}
