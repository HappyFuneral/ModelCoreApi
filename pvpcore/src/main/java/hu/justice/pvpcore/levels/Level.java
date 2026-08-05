package hu.justice.pvpcore.levels;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;



public class Level {

	private int lvl;
	private int xp;
	private int maxXp;
	private String name;
	private ArrayList<String> rewards;
	private int count;
	
	public Level(String name, int lvl,int xp, int maxXp,ArrayList<String> rewards) {
		this.lvl = lvl;
		this.maxXp = maxXp;
		this.name = name;
		this.xp = xp;
		this.rewards = rewards;
		if (lvl < 5) {
			count = 200;
		} else {
			count=((lvl/5))*200*((lvl/5))*4;
		}
		Player pl = Bukkit.getPlayer(name);
		
	}
	
	public void addXp(int xp1) {
		if (lvl>=30)return;
		xp+=xp1;
		Levels.refresh(this, Bukkit.getPlayer(name), new ArrayList<String>());
		if (xp>=maxXp) {
			levelUp();
		}
	}
	public void levelUp() {
		ArrayList<String> rs= new ArrayList<>();
		while (xp>=maxXp) {
			xp = xp-maxXp;
			lvl++;
			if (lvl % 5 == 0) {
				count*=4;
			} 
			maxXp+=count;
			for (Reward reward : Rewards.getRewards()) {
				if (reward.getLvl() == lvl) 
				{
					for (String r : reward.getRewards()) {
						rewards.add(r);
						rs.add(r);
					}
					return;
				}
			}
		}
		Levels.refresh(this, Bukkit.getPlayer(name), rs);
	}
	
	public int getLvl() {
		return lvl;
	}

	public void setLvl(int lvl) {
		this.lvl = lvl;
	}

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}

	public int getMaxXp() {
		return maxXp;
	}

	public void setMaxXp(int maxXp) {
		this.maxXp = maxXp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getRewards() {
		return rewards;
	}

	public void setRewards(ArrayList<String> rewards) {
		this.rewards = rewards;
	}


}
