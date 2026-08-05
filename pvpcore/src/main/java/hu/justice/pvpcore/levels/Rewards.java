package hu.justice.pvpcore.levels;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;



public class Rewards {
	
	private static ArrayList<Reward> rewards = new ArrayList<>();
	
	public static void create(Reward reward) {
		rewards.add(reward);
		RewardConfiguration.getConfig().set(reward.getLvl()+".rewards", reward.getRewards());
		RewardConfiguration.saveConfig();
	}
	
	public static void build() {
		RewardConfiguration.setup();
		FileConfiguration config = RewardConfiguration.getConfig();
		try {
			for (String str : config.getConfigurationSection("rewards").getKeys(false)) {
					Reward reward = new Reward(Integer.parseInt(str),(ArrayList<String>)config.getStringList("rewards."+str));
					rewards.add(reward);

			}
		}catch(Exception ex) {
			System.out.println(ex);
		}
	}		
	
	public static ArrayList<Reward> getRewards() {
		return rewards;
	}

	public static void setRewards(ArrayList<Reward> rewards) {
		Rewards.rewards = rewards;
	}
}
