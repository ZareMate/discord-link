package com.zaremate.discordlink;

import java.util.Optional;
import java.util.UUID;

/**
 * Public API for Discord Link.
 *
 * <p>Other server-side mods can use this API to read the authoritative
 * Minecraft-to-Discord link state without accessing the store directly.</p>
 */
public final class DiscordLinkAPI {
    private DiscordLinkAPI() {}

    /**
     * Returns the current Discord link for a Minecraft player.
     *
     * @return the immutable link snapshot, or {@link Optional#empty()} when
     *         the player is not linked
     */
    public static Optional<PlayerLink> getPlayerLink(UUID playerUuid) {
        if (playerUuid == null) {
            return Optional.empty();
        }

        DiscordLinkService service = DiscordLinkEvents.service();
        if (service == null) {
            return Optional.empty();
        }

        DiscordLinkStore.Link link = service.getLink(playerUuid);
        if (link == null || link.discordId == null || link.discordId.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new PlayerLink(
                parseUuid(link.uuid, playerUuid),
                link.minecraftName,
                link.displayName,
                link.discordId,
                link.discordTag,
                link.linkedAt,
                link.rewarded
        ));
    }

    /**
     * Returns whether the Minecraft player currently has a Discord account
     * linked.
     */
    public static boolean isLinked(UUID playerUuid) {
        return getPlayerLink(playerUuid).isPresent();
    }

    private static UUID parseUuid(String value, UUID fallback) {
        try {
            return value == null ? fallback : UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    /**
     * Immutable public representation of a Discord Link association.
     *
     * @param minecraftUuid Minecraft player UUID
     * @param minecraftName stored Minecraft name
     * @param displayName stored Discord display name, when available
     * @param discordId Discord user ID
     * @param discordTag Discord user tag at the time it was stored
     * @param linkedAt link timestamp in milliseconds since Unix epoch
     * @param rewarded whether the initial reward has been claimed
     */
    public record PlayerLink(
            UUID minecraftUuid,
            String minecraftName,
            String displayName,
            String discordId,
            String discordTag,
            long linkedAt,
            boolean rewarded
    ) {}
}
