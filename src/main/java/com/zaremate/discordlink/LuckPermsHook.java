package com.zaremate.discordlink;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public final class LuckPermsHook {
    public static final String ADMIN_CHECK = "discordlink.admin.check";
    public static final String ADMIN_UNLINK = "discordlink.admin.unlink";

    private LuckPermsHook() {}

    public static boolean has(CommandSourceStack source, String permission) {
        // Keep normal vanilla OP access working while also supporting LuckPerms.
        if (source.hasPermission(2)) {
            return true;
        }

        if (source.getEntity() == null) {
            return false;
        }

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return false;
        }

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            var user = luckPerms.getUserManager().getUser(player.getUUID());
            if (user == null) {
                return false;
            }

            return user.getCachedData()
                    .getPermissionData()
                    .checkPermission(permission)
                    .asBoolean();
        } catch (IllegalStateException e) {
            // LuckPerms is not loaded. Fall back to vanilla permission handling.
            return source.hasPermission(2);
        } catch (Throwable e) {
            System.err.println("[DiscordLink] LuckPerms permission check failed for "
                    + player.getGameProfile().getName() + ": " + e);
            return source.hasPermission(2);
        }
    }

    public static boolean isLoaded() {
        try {
            LuckPermsProvider.get();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
