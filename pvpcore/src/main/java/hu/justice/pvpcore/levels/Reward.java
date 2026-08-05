package hu.justice.pvpcore.levels;

import java.util.ArrayList;


public class Reward {

	private int lvl;
	private ArrayList<String> rewards;
	
	public Reward(int lvl, ArrayList<String> rewards) {
		setLvl(lvl);
		setRewards(rewards);
	}

	public int getLvl() {
		return lvl;
	}

	public void setLvl(int lvl) {
		this.lvl = lvl;
	}

	public ArrayList<String> getRewards() {
		return rewards;
	}

	public void setRewards(ArrayList<String> rewards) {
		this.rewards = rewards;
	}


}
