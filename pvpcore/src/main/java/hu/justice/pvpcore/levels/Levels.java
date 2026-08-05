package hu.justice.pvpcore.levels;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import org.bukkit.entity.Player;

import hu.justice.pvpcore.App;

public class Levels {

	private static ArrayList<Level> levels = new ArrayList<Level>();
	
	public static void create(Level level) {
		levels.add(level);
		
	}
	public static void refresh(Level level,Player pl,ArrayList<String> rewards) {

	}

	public static void build(String pl) {
 
	 	if (!isContain(pl)) {
			ArrayList<String> list = new ArrayList<>();
		
			create(new Level(pl,1,0,250,list));
			return;
		}
		if (isContain(pl))
			return;

	}
	
	public static boolean isContain(String name) {
		for (Level level : levels) {
			if (level.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	public static Level getLevel(String name) {
		for (Level level : levels) {
			if (level.getName().equals(name)) {
				return level;
			}
		}
		return null;
	}
	
	public static ArrayList<Level> getLevels() {
		return levels;
	}

	public static void setLevels(ArrayList<Level> levels) {
		Levels.levels = levels;
	}

	public static void loadLevels() throws IOException {

		Gson gson = new Gson();
		File file = new File(App.getPlugin().getDataFolder().getAbsolutePath() + "/levels.json");
		if (file.exists()){
			Reader reader = new FileReader(file);
			Level[] n = gson.fromJson(reader, Level[].class);
			levels = new ArrayList<>(Arrays.asList(n));
			System.out.println("Levels loaded.");
		}

	}

	public static void saveNotes() throws IOException {

		Gson gson = new Gson();
		System.out.println(App.getPlugin().getDataFolder().getAbsolutePath());
		File file = new File(App.getPlugin().getDataFolder().getAbsolutePath() + "/levels.json");
		file.getParentFile().mkdir();
		file.createNewFile();
		Writer writer = new FileWriter(file, false);
		gson.toJson(levels, writer);
		writer.flush();
		writer.close();
		System.out.println("Levels saved.");

	}

}
