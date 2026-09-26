package com.zaremate.discordlink;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class DiscordLinkCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("link").requires(s -> s.isPlayer())
                .executes(ctx -> link(ctx.getSource())));
        dispatcher.register(Commands.literal("discord").requires(s -> s.isPlayer())
                .executes(ctx -> link(ctx.getSource())));
        dispatcher.register(Commands.literal("unlinkdiscord").requires(s -> s.isPlayer())
                .executes(ctx -> unlinkSelf(ctx.getSource())));

        dispatcher.register(Commands.literal("checklink").requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> check(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))
                .then(Commands.literal("id")
                        .then(Commands.argument("discordid", StringArgumentType.word())
                                .executes(ctx -> checkId(ctx.getSource(), StringArgumentType.getString(ctx, "discordid"))))));

        dispatcher.register(Commands.literal("discordcheck").requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> check(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))
                .then(Commands.literal("id")
                        .then(Commands.argument("discordid", StringArgumentType.word())
                                .executes(ctx -> checkId(ctx.getSource(), StringArgumentType.getString(ctx, "discordid"))))));

        dispatcher.register(Commands.literal("discordunlink").requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> unlinkOther(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static int link(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DiscordLinkService service = DiscordLinkEvents.service();
        DiscordLinkStore.Link existing = service.getLink(player.getUUID());

        if (existing != null && existing.discordId != null) {
            player.sendSystemMessage(Component.literal("You are already linked to " + existing.discordTag + "."));
            player.sendSystemMessage(Component.literal("Run /unlinkdiscord first to link another Discord account."));
            return 0;
        }

        String code = service.generateCode(player);
        player.sendSystemMessage(Component.literal("=== DISCORD LINK ==="));
        player.sendSystemMessage(Component.literal("Open: " + DiscordLinkConfig.INVITE_URL.get()));
        player.sendSystemMessage(Component.literal("In Discord use /link-account with code: " + code));
        player.sendSystemMessage(Component.literal("Reward: " + DiscordLinkConfig.REWARD_COUNT.get() + "x " + DiscordLinkConfig.REWARD_ITEM.get()));
        return 1;
    }

    private static int unlinkSelf(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!DiscordLinkEvents.service().unlink(player.getUUID())) {
            source.sendFailure(Component.literal("You are not linked to any Discord account."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Your Discord account has been unlinked. You keep your original reward."), false);
        return 1;
    }

    private static int check(CommandSourceStack source, String name) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(name);
        if (player == null) {
            source.sendFailure(Component.literal("Player is not online."));
            return 0;
        }
        DiscordLinkStore.Link link = DiscordLinkEvents.service().getLink(player.getUUID());
        if (link == null || link.discordId == null) {
            source.sendFailure(Component.literal(name + " is not linked."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(name + " is linked to " + link.discordTag + " (" + link.discordId + ")."), false);
        return 1;
    }

    private static int checkId(CommandSourceStack source, String discordId) {
        var service = DiscordLinkEvents.service();
        var uuid = service.getLinkByDiscord(discordId);
        if (uuid == null) {
            source.sendFailure(Component.literal("No account is linked to " + discordId + "."));
            return 0;
        }
        var link = service.getLink(uuid);
        source.sendSuccess(() -> Component.literal(discordId + " is linked to " + link.minecraftName + " (" + uuid + ")."), false);
        return 1;
    }

    private static int unlinkOther(CommandSourceStack source, String name) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(name);
        if (player == null) {
            source.sendFailure(Component.literal("Player is not online."));
            return 0;
        }
        if (!DiscordLinkEvents.service().unlink(player.getUUID())) {
            source.sendFailure(Component.literal("Player is not linked."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(name + " has been unlinked. They keep their original reward."), true);
        return 1;
    }

    private DiscordLinkCommands() {}
}
