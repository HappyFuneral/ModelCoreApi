package hu.justice.pvpcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import hu.justice.pvpcore.App;
import hu.justice.pvpcore.party.Parties;
import hu.justice.pvpcore.tools.Server;


public class party implements CommandExecutor{
	public void addCommand() {
		Bukkit.getPluginCommand("party").setExecutor(this);
	}
	
	App main = App.getPlugin(App.class);
	@Override
	public boolean onCommand(CommandSender send, Command cmd, String str, String[] args) {
		if (send instanceof Player) {
			Player pl = (Player)send;
		
			if (args.length == 1 && args[0].toLowerCase().equals("create")) {
				if (Parties.alreadyInParty(pl)) {
					Server.sendMessage(pl, "Már benne vagy a partyban!");
				} else if (Parties.isCreated(pl)) {
					Server.sendMessage(pl, "Ez a party már létezik!");
				}
				Parties.createParty(pl);
			} else if(args.length == 1 && args[0].toLowerCase().equals("delete")) {
				if (!Parties.isLeader(pl)) {
					Server.sendMessage(pl, "&cNem te vagy a party tulajdonosa!");
				}
				else if(!Parties.isCreated(pl)) {
					Server.sendMessage(pl, "&cEzt a partyt nem tudod törölni mivel nem létezik!");
				}
				Parties.removeParty(pl);
			}
			else if(args.length == 2 && args[0].toLowerCase().equals("invite")) {
				try {
					if (Parties.isLeader(pl)) {
						Player invited = Bukkit.getPlayer(args[1]);
						if (Parties.alreadyInParty(invited)) {
							Server.sendMessage(pl, "Ezt a játékost nem tudod meghívni, mert partyban van!");
						} else {
							Server.sendMessage(pl, "&aMeghívtad &b" +invited.getName()+ "&ajátékost a partyba!");
							 Parties.getParty(pl).invite(invited);

						}
					} else if (!Parties.isLeader(pl)){
						Server.sendMessage(pl, "Nem te vagy a party tulajdonosa!");
					} else {
						Server.sendMessage(pl, "Nincs party még létrehozva! Hozz létre egyet a /party create parancs segítségével!");

					}
				} catch(NullPointerException ex) {
					Server.sendMessage(pl, "Nincs ilyen játékos vagy nem rendelkezel partyval!");
				}
			}
			
			else if (args.length == 2 && args[0].toLowerCase().equals("accept")) {
				try {
					if (Parties.alreadyInParty(pl)) {
						Server.sendMessage(pl,"&cNem tudod elfogadni, mivel már benne vagy egy partyban!");
					} else {
						Player partyLeader = Bukkit.getPlayer(args[1]);
						Parties.getParty(partyLeader).acceptInvite(pl);
						Server.sendMessage(partyLeader, pl.getName()+" elfogadta a meghívót!");
						Parties.getParty(pl).getPlayers().forEach(p -> Server.sendMessage(p, p.getName()+" csatlakozott a partyhoz!"));
					}
				} catch(NullPointerException ex) {
					Server.sendMessage(pl, "Nincs ilyen party vagy nem vagy meghívva!");
				}
			}
			else if (args.length == 2 && args[0].toLowerCase().equals("deny")) {
				try {
					Player partyLeader = Bukkit.getPlayer(args[1]);
					Parties.getParty(partyLeader).cancelInvite(pl);
					Server.sendMessage(partyLeader, pl.getName()+" elutasította a meghívót!");
				} catch(NullPointerException ex) {
					Server.sendMessage(pl, "Nincs ilyen party!");
					ex.printStackTrace();
				}
			}
		
			if (args.length == 1 && args[0].toLowerCase().equals("leave")) {
				if (Parties.alreadyInParty(pl)) {
					Parties.leaveParty(pl);
					Parties.getParty(pl).getPlayers().forEach(p -> Server.sendMessage(p, p.getName()+" kilépett a partyból!"));
					Server.sendMessage(pl, "Kiléptél a partyból!");
				} else {
					Server.sendMessage(pl, "Nem vagy partyban!");
				}
			}
			else if(args.length == 2 && args[0].toLowerCase().equals("kick")) {
				try {
					Player kicked = Bukkit.getPlayer(args[1]);
					if (Parties.isLeader(pl) && !Parties.isLeader(kicked)) {
						Parties.getParty(pl).kickPlayer(kicked);
						Server.sendMessage(kicked, "Ki lettél rúgva a partyból!");
						Server.sendMessage(pl, kicked.getName()+" ki lett rúgva a partyból!");
					} else if (Parties.isLeader(kicked)) {
						Server.sendMessage(pl, "A party vezetőt nem tudod kirúgni!");
					} else if (!Parties.isLeader(kicked)) {
						Server.sendMessage(pl, "Nem te vagy a party tulajdonosa!");
					} else {
						Server.sendMessage(pl, "&cNem tudod ezt megcsinálni!");
					}
				} catch(NullPointerException ex) {
					Server.sendMessage(pl, "Nincs ilyen játékos!");
					ex.printStackTrace();
				}
			}
			else if (args.length == 1 && args[0].toLowerCase().equals("members")) {
				if (!Parties.alreadyInParty(pl))return false;
				pl.sendMessage(Server.colorText("&7 [&3Party tagok&7] "));
				for (Player player : Parties.getParty(pl).getPlayers()) {
					
					pl.sendMessage(Server.colorText(Parties.isLeader(player) ? "&c- "+player.getName()+"(Leader)" :  "&b- "+player.getName()+""));
				}
			}
			else if (args.length == 0){
				info(pl);
			}
		}
		if (send instanceof ConsoleCommandSender) {
			
			if (args.length == 3 && args[0].toLowerCase().equals("sharexp")){
				try {
					Player pl = Bukkit.getPlayer(args[1]);
					int exp = Integer.parseInt(args[2]);
					Parties.shareExp(pl, Parties.getParty(pl), exp,false);
				} catch (NumberFormatException ex) {
					System.out.println("Kérlek számot adj meg!");
				}
				
				
			}
		}
		return false;
	}
	public static void info(Player pl) {
		pl.sendMessage("");
		pl.sendMessage(Server.colorText("&e&l◆&r&3 /party create &e&l➔&r&b létrehoz egy új partyt!"));
		pl.sendMessage(Server.colorText("&e&l◆&r&3 /party delete &e&l➔&r&b törli az adott partyt!"));
		pl.sendMessage(Server.colorText("&e&l◆&r&3 /party leave &e&l➔&r&b kilépés a partyból!"));
		pl.sendMessage(Server.colorText("&e&l◆&r&3 /party invite <játékos> &e&l➔&r&b meghívsz egy játékost a partyba!"));
		pl.sendMessage(Server.colorText("&e&l◆&r&3 /party accept <party> &e&l➔&r&b elfogadod a party meghívást!"));
		pl.sendMessage(Server.colorText("&e&l◆&r&3 /party deny <party> &e&l➔&r&b elutasítod a party meghívást!"));
		pl.sendMessage(Server.colorText("&e&l◆&r&3 /party kick <játékos> &e&l➔&r&b kirúgsz egy játékost a partyból!"));
		pl.sendMessage(Server.colorText("&e&l◆&r&3 /party members &e&l➔&r&b az összes tag a partyban!"));
	}
}
