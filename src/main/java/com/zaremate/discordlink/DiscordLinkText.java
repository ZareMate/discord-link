package com.zaremate.discordlink;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent;

public final class DiscordLinkText {
    private static final int ACCENT = 0x4498DB;
    private static final int GOOD = 0x72FF43;
    private static final int BAD = 0xFF4343;
    private static final int MUTED = 0xAAAAAA;
    private static final int WHITE = 0xFFFFFF;
    private static final String DIVIDER = "────────────────────────────────────────";

    public static Component divider() {
        return Component.literal(DIVIDER).setStyle(Style.EMPTY.withColor(0x555555));
    }

    public static Component title(String text) {
        return Component.literal(" " + text)
                .setStyle(Style.EMPTY.withColor(ACCENT).withBold(true));
    }

    public static Component label(String label, String value) {
        return Component.literal("│ ")
                .setStyle(Style.EMPTY.withColor(ACCENT))
                .append(Component.literal(label + ": ")
                        .setStyle(Style.EMPTY.withColor(MUTED)))
                .append(Component.literal(value)
                        .setStyle(Style.EMPTY.withColor(WHITE).withBold(true)));
    }

    public static Component action(String text, ClickEvent.Action action, String value, String hover) {
        Style style = Style.EMPTY
                .withColor(WHITE)
                .withBold(true)
                .withClickEvent(new ClickEvent(action, value))
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal(hover).withStyle(ChatFormatting.GRAY)));
        return Component.literal(text).setStyle(style);
    }

    public static Component good(String text) {
        return Component.literal("[!] ")
                .setStyle(Style.EMPTY.withColor(GOOD).withBold(true))
                .append(Component.literal(text).setStyle(Style.EMPTY.withColor(WHITE)));
    }

    public static Component bad(String text) {
        return Component.literal("[!] ")
                .setStyle(Style.EMPTY.withColor(BAD).withBold(true))
                .append(Component.literal(text).setStyle(Style.EMPTY.withColor(WHITE)));
    }

    public static void block(ServerPlayerLike player, Component... lines) {
        player.tell(divider());
        for (Component line : lines) {
            player.tell(Component.literal(" ").append(line));
        }
        player.tell(divider());
    }

    public interface ServerPlayerLike {
        void tell(Component component);
    }

    private DiscordLinkText() {}
}
