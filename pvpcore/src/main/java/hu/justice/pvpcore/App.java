package hu.justice.pvpcore;


import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import hu.justice.pvpcore.levels.Levels;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import hu.justice.pvpcore.commands.party;
import hu.justice.pvpcore.exp.Experiences;
import hu.justice.pvpcore.exp.MobExpListener;
import hu.justice.pvpcore.levels.LevelUpListener;
import hu.justice.pvpcore.levels.Rewards;

import java.io.IOException;

public class App extends JavaPlugin{



	private static App plugin;

	public static WorldGuardPlugin worldGuardPlugin;

	public WorldGuardPlugin getWorldGuard() {
		Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}

		return (WorldGuardPlugin) plugin;
	}

	public void onEnable() {
		plugin = this;
		worldGuardPlugin = getWorldGuard();
		Rewards.build();
		try {
			Levels.loadLevels();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Experiences.build();
		Bukkit.getPluginManager().registerEvents(new LevelUpListener(), this);
		Bukkit.getPluginManager().registerEvents(new MobExpListener(), this);
		new party().addCommand();

		if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderApi")) {
			new LevelPlaceholder(this).register();
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		try {
			Levels.saveNotes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static App getPlugin() {
		return plugin;
	}
}
 