package hu.justice.pvpcore.exp;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public class Experiences {

	/********************************
	 * Farming Experience
	 * Killing Experience
	 * Mining Experience
	 * From taking damage with
	 * or by smthing
	 * 
	 * ...... 
	 * 
	 * This class for gain 
	 * experience from mobs,
	 * mining, from killing 
	 * players, taking damage
	 * etc. 
	 ********************************/
	
	private static ArrayList<Experience> exps = new ArrayList<Experience>();
	
	public static Experience getExp(EntityType type) {
		for (Experience exp : exps) {
			if (exp.getEntityType() == type) {
				return exp;
			}
		}
		return null;
	}

	public static Experience getExp(Material type) {
		for (Experience exp : exps) {
			if (exp.getBlock() == type) {
				return exp;
			}
		}
		return null;
	}
	public static void build() {
		ExperienceConfig.setup();
		FileConfiguration config = ExperienceConfig.getConfig();
		for (String str : config.getConfigurationSection("mob").getKeys(false)) {
			Experience exp = new Experience(EntityType.valueOf(str),config.getInt("mob."+str));
			System.out.println(exp.getEntityType()+": "+exp.getXp());
			exps.add(exp);			
		}
		for (String str : config.getConfigurationSection("block").getKeys(false)) {
			Experience exp = new Experience(Material.valueOf(str),config.getInt("block."+str));
			System.out.println(exp.getBlock()+": "+exp.getXp());
			exps.add(exp);
		}
		System.out.println();

	}
	
	public static ArrayList<Experience> getExps() {
		return exps;
	}

	public static void setExps(ArrayList<Experience> exps) {
		Experiences.exps = exps;
	}

}
