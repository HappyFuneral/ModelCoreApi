package org.justice.hungergames.model;

import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class Arena {
    private String id; // Egyedi azonosító
    private String name;
    private Location lobbySpawn; // Lobby ahonnan be lehet lépni
    private List<Location> spawnPoints; // Játékos spawn pontok
    private List<Location> chestLocations; // Chestek helyei
    private Location deathmatchSpawn; // Deathmatch aréna spawn pontja
    private int minPlayers;
    private int maxPlayers;
    private int deathmatchThreshold; // Ennyi élő játékosnál indul a deathmatch
    private Map<String, List<ChestLootItem>> chestLootConfig; // Chest loot kategóriák

    public Arena(String id, String name, Location lobbySpawn, List<Location> spawnPoints, List<Location> chestLocations, Location deathmatchSpawn, int minPlayers, int maxPlayers, int deathmatchThreshold, Map<String, List<ChestLootItem>> chestLootConfig) {
        this.id = id;
        this.name = name;
        this.lobbySpawn = lobbySpawn;
        this.spawnPoints = spawnPoints;
        this.chestLocations = chestLocations;
        this.deathmatchSpawn = deathmatchSpawn;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.deathmatchThreshold = deathmatchThreshold;
        this.chestLootConfig = chestLootConfig;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Location getLobbySpawn() { return lobbySpawn; }
    public List<Location> getSpawnPoints() { return spawnPoints; }
    public List<Location> getChestLocations() { return chestLocations; }
    public Location getDeathmatchSpawn() { return deathmatchSpawn; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getDeathmatchThreshold() { return deathmatchThreshold; }
    public Map<String, List<ChestLootItem>> getChestLootConfig() { return chestLootConfig; }

    // Belső osztály a chest loot elemekhez
    public static class ChestLootItem {
        private String material; // Pl. "DIAMOND_SWORD"
        private int minAmount;
        private int maxAmount;
        private double chance; // Esély, hogy megjelenjen

        public ChestLootItem(String material, int minAmount, int maxAmount, double chance) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.chance = chance;
        }

        // Getters
        public String getMaterial() { return material; }
        public int getMinAmount() { return minAmount; }
        public int getMaxAmount() { return maxAmount; }
        public double getChance() { return chance; }
    }
}