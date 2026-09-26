package com.zaremate.discordlink;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class DiscordLinkCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("link")
                .requires(CommandSourceStack::isPlayer)
                .executes(ctx -> link(ctx.getSource())));

        dispatcher.register(Commands.literal("discord")
                .requires(CommandSourceStack::isPlayer)
                .executes(ctx -> link(ctx.getSource())));

        dispatcher.register(Commands.literal("unlinkdiscord")
                .requires(CommandSourceStack::isPlayer)
                .executes(ctx -> unlinkSelf(ctx.getSource())));

        dispatcher.register(Commands.literal("checklink")
                .requires(source -> LuckPermsHook.has(source, LuckPermsHook.ADMIN_CHECK))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> check(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player"))))
                .then(Commands.literal("id")
                        .then(Commands.argument("discordid", StringArgumentType.word())
                                .executes(ctx -> checkId(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "discordid"))))));

        dispatcher.register(Commands.literal("discordcheck")
                .requires(source -> LuckPermsHook.has(source, LuckPermsHook.ADMIN_CHECK))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> check(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player"))))
                .then(Commands.literal("id")
                        .then(Commands.argument("discordid", StringArgumentType.word())
                                .executes(ctx -> checkId(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "discordid"))))));

        dispatcher.register(Commands.literal("discordunlink")
                .requires(source -> LuckPermsHook.has(source, LuckPermsHook.ADMIN_UNLINK))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> unlinkOther(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player")))));
    }

    private static DiscordLinkService service() {
        return DiscordLinkEvents.service();
    }

    private static Component prefix(String text) {
        return Component.literal("│ ")
                .setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(DiscordLinkText.ACCENT))
                .append(Component.literal(text)
                        .setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(DiscordLinkText.MUTED)));
    }

    private static int link(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DiscordLinkService service = service();

        DiscordLinkStore.Link existing = service.getLink(player.getUUID());
        if (existing != null && existing.discordId != null) {
            player.sendSystemMessage(DiscordLinkText.divider());
            player.sendSystemMessage(DiscordLinkText.bad(
                    "Your account is already linked to " + existing.discordTag + "."));
            player.sendSystemMessage(prefix("Run /unlinkdiscord first to link another Discord account."));
            player.sendSystemMessage(DiscordLinkText.divider());
            return 0;
        }

        String code = service.generateCode(player);

        Component codeComponent = DiscordLinkText.clickable(
                code,
                ClickEvent.Action.COPY_TO_CLIPBOARD,
                code,
                "Click to copy your code");

        player.sendSystemMessage(DiscordLinkText.divider());
        player.sendSystemMessage(DiscordLinkText.title("LINK YOUR DISCORD"));
        player.sendSystemMessage(DiscordLinkText.blank());
        player.sendSystemMessage(prefix("Connect your Minecraft account to our Discord server."));
        player.sendSystemMessage(DiscordLinkText.blank());

        Component discordLine = Component.empty()
                .append(DiscordLinkText.prefixed("Open "))
                .append(DiscordLinkText.clickable(
                        "[DISCORD]",
                        ClickEvent.Action.OPEN_URL,
                        DiscordLinkConfig.INVITE_URL.get(),
                        "Open the Discord server"));
        player.sendSystemMessage(discordLine);

        Component commandLine = Component.empty()
                .append(DiscordLinkText.prefixed("Then use "))
                .append(DiscordLinkText.clickable(
                        "[/link-account]",
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/link-account code:" + code,
                        "Click to insert the command"));
        player.sendSystemMessage(commandLine);

        player.sendSystemMessage(Component.empty()
                .append(DiscordLinkText.label("Your code", ""))
                .append(codeComponent));
        player.sendSystemMessage(DiscordLinkText.label("Reward",
                DiscordLinkConfig.REWARD_COUNT.get() + "x " + DiscordLinkConfig.REWARD_ITEM.get()));
        player.sendSystemMessage(DiscordLinkText.blank());
        player.sendSystemMessage(prefix("This code expires in 15 minutes."));
        player.sendSystemMessage(DiscordLinkText.divider());
        return 1;
    }

    private static int unlinkSelf(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!service().unlink(player.getUUID())) {
            source.sendFailure(DiscordLinkText.bad("Your account is not linked to Discord."));
            return 0;
        }

        source.sendSuccess(() -> DiscordLinkText.good(
                "Your Discord account has been unlinked. You keep your original reward."), false);
        return 1;
    }

    private static int check(CommandSourceStack source, String name) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(name);
        if (player == null) {
            source.sendFailure(DiscordLinkText.bad("Player '" + name + "' is not online."));
            return 0;
        }

        DiscordLinkStore.Link link = service().getLink(player.getUUID());
        if (link == null || link.discordId == null) {
            source.sendFailure(DiscordLinkText.bad(name + " is not linked to Discord."));
            return 0;
        }

        source.sendSuccess(() -> DiscordLinkText.good(
                name + " is linked to " + link.discordTag + " (" + link.discordId + ")."), false);
        return 1;
    }

    private static int checkId(CommandSourceStack source, String discordId) {
        var uuid = service().getLinkByDiscord(discordId);
        if (uuid == null) {
            source.sendFailure(DiscordLinkText.bad(
                    "No Minecraft account is linked to " + discordId + "."));
            return 0;
        }

        var link = service().getLink(uuid);
        source.sendSuccess(() -> DiscordLinkText.good(
                discordId + " is linked to " + link.minecraftName + " (" + uuid + ")."), false);
        return 1;
    }

    private static int unlinkOther(CommandSourceStack source, String name) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(name);
        if (player == null) {
            source.sendFailure(DiscordLinkText.bad("Player '" + name + "' is not online."));
            return 0;
        }

        if (!service().unlink(player.getUUID())) {
            source.sendFailure(DiscordLinkText.bad("Player '" + name + "' is not linked."));
            return 0;
        }

        source.sendSuccess(() -> DiscordLinkText.good(
                name + " has been unlinked. They keep their original reward."), true);
        return 1;
    }

    private DiscordLinkCommands() {}
}
