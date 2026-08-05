package hu.justice.pvpcore.exp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import hu.justice.pvpcore.App;


public class ExperienceConfig {
	private static App plugin = App.getPlugin(App.class);
	
	private static FileConfiguration config;
	private static File file;
	
	/*
	 * This method is build the experiences.yml config file, 
	 * exclude the entities what can not be killed by other entites
	 * and build the config file helped by the EntityType.values() method.
	 * This method called when the plugin starting.
	 */
	public  static void setup() {
		if(!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdir();
		}
		
		setFile(new File(plugin.getDataFolder(), "experiences.yml"));
		if (!getFile().exists()) {
			try {
				getFile().createNewFile();
				setConfig(YamlConfiguration.loadConfiguration(file));
				saveConfig();

				for (EntityType type : EntityType.values()) {
					if (type.isSpawnable())
						config.set("mob."+type, 1);
				}
				for (Material type : Material.values()) {
					if (type.isBlock())
						config.set("block."+type, 1);
				}
				saveConfig();
				Bukkit.getServer().getConsoleSender().sendMessage("experiences.yml succesfully created!");
			} catch (IOException e) {
				Bukkit.getServer().getConsoleSender().sendMessage("Error: experiences.yml file could not created!");	
			}
		} else {
			Bukkit.getServer().getConsoleSender().sendMessage("experiences.yml succesfully loaded!");
			setConfig(YamlConfiguration.loadConfiguration(file));
		}
	}

	public static FileConfiguration getConfig() {
		return config;
	}

	private static void setConfig(FileConfiguration config) {
		ExperienceConfig.config = config;
	}

	public static File getFile() {
		return file;
	}

	private static void setFile(File file) {
		ExperienceConfig.file = file;
	}
	public static void saveConfig() {
		try {
			getConfig().save(file);
		} catch(IOException e) {
			Bukkit.getServer().getConsoleSender().sendMessage(Color.RED+"Error: experiences.yml file could not save.");
		}
	}
	public static void reloadConfig() {
		setConfig(YamlConfiguration.loadConfiguration(file));
	}
}
