package hu.justice.pvpcore;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import hu.justice.pvpcore.levels.Levels;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;


public class LevelPlaceholder extends PlaceholderExpansion {

    private final App plugin; // 



    public LevelPlaceholder(App plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors()); // 


    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "sky";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); // 


    }

    @Override
    public boolean persist() {
        return true; // 


    }

    @Override
    public String onPlaceholderRequest(Player pl, String identifier) {
        if (pl == null ) {
            return null; // 


        }

        if (identifier.equalsIgnoreCase("player_level")) { // 


            return String.valueOf(Levels.getLevel(pl.getName()).getLvl());
        }

        if (identifier.equalsIgnoreCase("player_xp")) { // 


            return String.valueOf(Levels.getLevel(pl.getName()).getXp());
        }
        if (identifier.equalsIgnoreCase("player_maxxp")) { // 


            return String.valueOf(Levels.getLevel(pl.getName()).getMaxXp());
        }
        return null; // 


    }

	
}
