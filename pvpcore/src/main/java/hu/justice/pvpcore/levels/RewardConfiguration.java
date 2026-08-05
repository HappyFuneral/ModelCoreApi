package hu.justice.pvpcore.levels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.justice.pvpcore.App;


public class RewardConfiguration {
private static App plugin = App.getPlugin(App.class);
	
	private static FileConfiguration config;
	private static File file;
	
	public  static void setup() {
		if(!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdir();
		}
		
		setFile(new File(plugin.getDataFolder(), "rewards.yml"));
		if (!getFile().exists()) {
			try {
				ArrayList<String> rewards = new ArrayList<>();
				getFile().createNewFile();
				setConfig(YamlConfiguration.loadConfiguration(file));
				rewards.add("example.permission");
				config.set("rewards.0", rewards);
				saveConfig();
				Bukkit.getServer().getConsoleSender().sendMessage("rewards.yml succesfully created!");
			} catch (IOException e) {
				Bukkit.getServer().getConsoleSender().sendMessage("Error: rewards.yml file could not created!");	
			}
		} else {
			Bukkit.getServer().getConsoleSender().sendMessage("rewards.yml succesfully loaded!");
			setConfig(YamlConfiguration.loadConfiguration(file));
		}
	}

	public static FileConfiguration getConfig() {
		return config;
	}

	private static void setConfig(FileConfiguration config) {
		RewardConfiguration.config = config;
	}

	public static File getFile() {
		return file;
	}

	private static void setFile(File file) {
		RewardConfiguration.file = file;
	}
	public static void saveConfig() {
		try {
			getConfig().save(file);
		} catch(IOException e) {
			Bukkit.getServer().getConsoleSender().sendMessage(Color.RED+"Error: rewards.yml file could not save.");
		}
	}
	public static void reloadConfig() {
		setConfig(YamlConfiguration.loadConfiguration(file));
	}
}


