package com.zaremate.discordlink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DiscordLinkStore {
    public static final class Link {
        public String uuid;
        public String minecraftName;
        public String displayName;
        public String discordId;
        public String discordTag;
        public long linkedAt;
        public boolean rewarded;
    }

    public static final class Code {
        public String uuid;
        public String minecraftName;
        public long createdAt;
    }

    public static final class Data {
        public Map<String, Link> links = new HashMap<>();
        public Map<String, String> discordIndex = new HashMap<>();
        public Map<String, Code> codes = new HashMap<>();
        public Map<String, Boolean> owed = new HashMap<>();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private Data data;

    public DiscordLinkStore(Path file) {
        this.file = file;
        load();
    }

    public synchronized void load() {
        try {
            if (Files.exists(file)) data = GSON.fromJson(Files.readString(file), Data.class);
        } catch (Exception e) {
            System.err.println("[DiscordLink] Could not load data: " + e);
        }
        if (data == null) data = new Data();
        if (data.links == null) data.links = new HashMap<>();
        if (data.discordIndex == null) data.discordIndex = new HashMap<>();
        if (data.codes == null) data.codes = new HashMap<>();
        if (data.owed == null) data.owed = new HashMap<>();
    }

    private void save() {
        try {
            Files.writeString(file, GSON.toJson(data));
        } catch (IOException e) {
            throw new RuntimeException("Could not save Discord Link data", e);
        }
    }

    public synchronized Link get(UUID uuid) { return data.links.get(uuid.toString()); }

    public synchronized UUID findUuidByMinecraftName(String name) {
        for (Link link : data.links.values()) {
            if (link != null && link.minecraftName != null && link.minecraftName.equalsIgnoreCase(name)) {
                return UUID.fromString(link.uuid);
            }
        }
        return null;
    }

    public synchronized java.util.Set<String> getKnownMinecraftNames() {
        java.util.Set<String> names = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Link link : data.links.values()) {
            if (link != null && link.minecraftName != null && !link.minecraftName.isBlank()) {
                names.add(link.minecraftName);
            }
        }
        return names;
    }

    public synchronized UUID ownerOf(String discordId) {
        String value = data.discordIndex.get(discordId);
        return value == null ? null : UUID.fromString(value);
    }

    public synchronized void createCode(UUID uuid, String name, String code) {
        data.codes.entrySet().removeIf(e -> e.getValue() != null && uuid.toString().equals(e.getValue().uuid));
        Code c = new Code();
        c.uuid = uuid.toString();
        c.minecraftName = name;
        c.createdAt = System.currentTimeMillis();
        data.codes.put(code, c);
        save();
    }

    public synchronized Code consumeCode(String code) {
        Code c = data.codes.get(code);
        if (c == null) return null;
        if (System.currentTimeMillis() - c.createdAt > 15 * 60_000L) {
            data.codes.remove(code);
            save();
            return null;
        }
        data.codes.remove(code);
        save();
        return c;
    }

    public synchronized void putLink(Link link) {
        Link old = data.links.get(link.uuid);
        if (old != null && old.discordId != null && !old.discordId.equals(link.discordId)) {
            data.discordIndex.remove(old.discordId);
        }
        data.links.put(link.uuid, link);
        if (link.discordId != null) data.discordIndex.put(link.discordId, link.uuid);
        save();
    }

    public synchronized void unlink(UUID uuid) {
        Link link = data.links.get(uuid.toString());
        if (link == null) return;
        if (link.discordId != null) data.discordIndex.remove(link.discordId);
        link.discordId = null;
        link.discordTag = null;
        link.linkedAt = 0;
        data.links.put(uuid.toString(), link);
        save();
    }

    public synchronized void owe(UUID uuid) {
        data.owed.put(uuid.toString(), true);
        save();
    }

    public synchronized boolean takeOwed(UUID uuid) {
        if (!Boolean.TRUE.equals(data.owed.get(uuid.toString()))) return false;
        data.owed.remove(uuid.toString());
        save();
        return true;
    }

    public synchronized void updateDisplay(UUID uuid, String display) {
        Link link = data.links.get(uuid.toString());
        if (link == null) return;
        link.displayName = display;
        data.links.put(uuid.toString(), link);
        save();
    }
}
