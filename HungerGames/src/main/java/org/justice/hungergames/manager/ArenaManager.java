package org.justice.hungergames.manager;

import org.bukkit.Location;
import org.justice.hungergames.HungerGames;
import org.justice.hungergames.model.Arena;

import java.util.HashMap;
import java.util.Map;

public class ArenaManager {
    private Map<String, Arena> arenas; // Arena ID alapján
    private HungerGames plugin; // Referencia a fő plugin osztályra (konfiguráció betöltéséhez)

    public ArenaManager(HungerGames plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        loadArenasFromConfig();
    }

    private void loadArenasFromConfig() {
        // Betölti az arénákat a config.yml-ből
        // Példa struktúra config.yml-ben:
        /*
        arenas:
          arena1:
            name: "Erdei Harcmező"
            lobby_spawn: "world,0,64,0"
            spawn_points:
              - "world,10,70,10"
              - "world,20,70,20"
            chest_locations:
              - "world,5,65,5"
              - "world,15,65,15"
            deathmatch_spawn: "world,0,80,0"
            min_players: 2
            max_players: 24
            deathmatch_threshold: 4
            chest_loot:
              starter_chest:
                - material: WOODEN_SWORD
                  min_amount: 1
                  max_amount: 1
                  chance: 1.0
                - material: APPLE
                  min_amount: 2
                  max_amount: 5
                  chance: 0.8
              rare_chest:
                - material: IRON_CHESTPLATE
                  min_amount: 1
                  max_amount: 1
                  chance: 0.5
        */
        // Implementáció: fájlbeolvasás, YAML parse, Arena objektumok létrehozása
        // Fontos: a Location objektumokhoz World, X, Y, Z koordináták kellenek
    }

    public Arena getArena(String id) {
        return arenas.get(id);
    }

    public void addArena(Arena arena) {
        arenas.put(arena.getId(), arena);
        // Menteni is kell a config.yml-be
    }

    public void removeArena(String id) {
        arenas.remove(id);
        // Menteni is kell a config.yml-be
    }

    // Admin parancsokhoz segítő metódusok:
    public void setArenaSpawnPoint(String arenaId, Location loc) { /* ... */ }
    public void addArenaChestLocation(String arenaId, Location loc) { /* ... */ }
    // stb. Ezek módosítják az Arena objektumokat és mentik a configot
}