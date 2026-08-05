package hu.justice.pvpcore.party;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hu.justice.pvpcore.App;
import hu.justice.pvpcore.tools.Server;

public class Party {

	private Player leader;
	private int cd;
	private ArrayList<Player> players = new ArrayList<>();
	private ArrayList<Invite> invites = new ArrayList<>();
	public Party(Player leader, ArrayList<Player> players) {
		setLeader(leader);
		setPlayers(players);
	}
	
	
	public boolean isInvited(Player pl) {
		for (Invite invite : invites) {
			if (invite.getInvited() == pl) {
				return true;
			}
		}
		return false;
	}
	public Invite getInvite(Player pl) {
		for (Invite invite : invites) {
			if (invite.getInvited() == pl) {
				return invite;
			}
		}
		return null;
	}
	public void acceptInvite(Player pl) {
		getInvite(pl).setAccepted(true);
		Server.sendMessage(pl, "Elfogadtad a meghívót!");
	}
	
	public void cancelInvite(Player pl) {
		if (!isInvited(pl))return;
		invites.remove(getInvite(pl));
		Server.sendMessage(pl, "Meghívó elutasítva!");
	}
	
	
	public void invite(Player pl) {
		if (isInvited(pl) || players.contains(pl))return;
		invites.add(new Invite(pl,false));
		Server.sendMessage(pl, "Meg lettél hívva "+leader.getName()+" partyba!");
		cd = Bukkit.getScheduler().scheduleSyncRepeatingTask(App.getPlugin(App.class), new Runnable() {
			int cooldown = 30;
			@Override
			public void run() {
				if (!isInvited(pl) || players.contains(pl)) Bukkit.getScheduler().cancelTask(cd);
				
				if (cooldown == 30) {
					Server.sendMessage(pl, "A party meghívó "+cooldown+" múlva lejár!");
				}
				else if(cooldown == 15) {
					Server.sendMessage(pl, "A party meghívó "+cooldown+" múlva lejár!");
				}
				else if (cooldown == 0) {
					invites.remove(getInvite(pl));
					Server.sendMessage(pl, "Lejárt a meghívó!");
					Bukkit.getScheduler().cancelTask(cd);
				}
				else if(getInvite(pl).isAccepted()) {
					invites.remove(getInvite(pl));
					players.add(pl);
					Bukkit.getScheduler().cancelTask(cd);
				}
				cooldown--;
			}

		},20,20);
	}
	
	public void kickPlayer(Player pl) {
		if (players.contains(pl)) {
			players.remove(pl);
		}
	}
	public Player getLeader() {
		return leader;
	}
	public void setLeader(Player leader) {
		this.leader = leader;
	}
	public ArrayList<Player> getPlayers() {
		return players;
	}
	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}

}
class Invite {
	private Player invited;
	private boolean accepted;

	public Invite(Player invited, boolean accepted) {
		setInvited(invited);
		setAccepted(accepted);
	}
	
	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public Player getInvited() {
		return invited;
	}

	public void setInvited(Player invited) {
		this.invited = invited;
	}

}
