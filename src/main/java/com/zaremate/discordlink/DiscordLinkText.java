package com.zaremate.discordlink;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public final class DiscordLinkText {
    public static final int ACCENT = 0x4498DB;
    public static final int GOOD = 0x72FF43;
    public static final int BAD = 0xFF4343;
    public static final int MUTED = 0xAAAAAA;
    public static final int WHITE = 0xFFFFFF;
    private static final int DARK = 0x555555;
    private static final String DIVIDER = "────────────────────────────────────────";

    public static Component divider() {
        return Component.literal(DIVIDER)
                .setStyle(Style.EMPTY.withColor(DARK));
    }

    public static Component title(String text) {
        return Component.literal(" " + text)
                .setStyle(Style.EMPTY.withColor(ACCENT).withBold(true));
    }

    public static Component text(String text) {
        return Component.literal(text)
                .setStyle(Style.EMPTY.withColor(MUTED));
    }

    public static Component label(String label, String value) {
        return Component.literal("│ " + label + ": ")
                .setStyle(Style.EMPTY.withColor(MUTED))
                .append(Component.literal(value)
                        .setStyle(Style.EMPTY.withColor(WHITE).withBold(true)));
    }

    public static Component clickable(String text, ClickEvent.Action action, String value, String hover) {
        return Component.literal(text)
                .setStyle(Style.EMPTY
                        .withColor(WHITE)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(action, value))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal(hover))));
    }

    public static Component good(String text) {
        return Component.literal("[!] ")
                .setStyle(Style.EMPTY.withColor(GOOD).withBold(true))
                .append(Component.literal(text)
                        .setStyle(Style.EMPTY.withColor(WHITE)));
    }

    public static Component bad(String text) {
        return Component.literal("[!] ")
                .setStyle(Style.EMPTY.withColor(BAD).withBold(true))
                .append(Component.literal(text)
                        .setStyle(Style.EMPTY.withColor(WHITE)));
    }

    public static Component prefixed(String text) {
        return Component.literal("│ ")
                .setStyle(Style.EMPTY.withColor(ACCENT))
                .append(Component.literal(text)
                        .setStyle(Style.EMPTY.withColor(MUTED)));
    }

    public static Component blank() {
        return Component.empty();
    }

    private DiscordLinkText() {}
}
