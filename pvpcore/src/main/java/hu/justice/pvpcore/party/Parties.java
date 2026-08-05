package hu.justice.pvpcore.party;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.entity.Player;

import hu.justice.pvpcore.levels.Levels;
import hu.justice.pvpcore.tools.Server;

public class Parties {
	
	private static ArrayList<Party> parties = new ArrayList<>();

	public static ArrayList<Party> getParties() {
		return parties;
	}

	public static void setParties(ArrayList<Party> parties) {
		Parties.parties = parties;
	}

	
	public static Party getParty(Player pl) {
		for (Party party : parties) {
			if (party.getPlayers().contains(pl)) {
				return party;
			}
		}
		return null;
	}
	
	public static void leaveParty(Player pl) {
		if(!alreadyInParty(pl))return;
		else if(getParty(pl).getLeader() == pl) {
			if (getParty(pl).getPlayers().size() == 1) {
				removeParty(pl);
				Server.sendMessage(pl, "Kiléptél a partyból és ezzel törlődött a party!");
			}
			else {
				getParty(pl).setLeader(getParty(pl).getPlayers().get(1));
				getParty(pl).getPlayers().remove(pl);
				Server.sendMessage(pl, "Kiléptél a partyból!");

			}
		}
	}
	
	public static boolean isCreated(Player pl) {
		for (Party party : parties) {
			if (party.getLeader() == pl) {
				return true;
			}
		}
		return false;
	}
	
	public static void createParty(Player pl) {
		if (alreadyInParty(pl) || isCreated(pl))return;
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(pl);
		Party party = new Party(pl,players);
		parties.add(party);					
		Server.sendMessage(pl, "&aParty sikeresen létrehozva!");
	}
	public static void removeParty(Player pl) {
		if (!isLeader(pl) || !isCreated(pl))return;
		getParty(pl).getPlayers().forEach(p -> Server.sendMessage(p, "&cParty feloszlott!"));
		parties.remove(getParty(pl));
	}
	public static boolean alreadyInParty(Player pl ) {
		for (Party party : parties) {
			if (party.getPlayers().contains(pl)) {
				return true;
			}
		}
		return false;
	}
	public static boolean alreadyInThisParty(Player pl ,Party party) {
		if (party==null)return false;
		if (party.getPlayers().contains(pl)) {
			return true;
		}
		
		return false;
	}
	public static boolean isLeader(Player pl ) {
		for (Party party : parties) {
			if (party.getLeader() == pl) {
				return true;
			}
		}
		return false;
	}
	public static void shareExp(Player player, Party party, int xp, boolean sameParty) {
		if (alreadyInThisParty(player,party) && !sameParty) {
			for (Player pl : party.getPlayers()) {
				if (pl == player) {
					Levels.getLevel(pl.getName()).addXp(xp);
				} else {
					Levels.getLevel(pl.getName()).addXp(xp/party.getPlayers().size());
				}
			}
		}else {
			Levels.getLevel(player.getName()).addXp(xp);
		}

	}
}
