package hu.justice.pvpcore.tools;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.ChatColor;

public class Server {
	
	private static String name = "&6&lMine&f&lMC &7-> &a"; 
	private static String prefix = "&7> &fMine&6MC &7<";
	public static void broadcastMessage(String msg) {
		Bukkit.broadcastMessage(name+msg);
	}
	public static String getName() {
		return name;
	}
	public static void sendMessage(Player pl, String msg) {
		pl.sendMessage(ChatColor.translateAlternateColorCodes('&', name+msg));
	}
	public static void kickPlayer(Player pl, String msg) {
		pl.kickPlayer(name+msg);
	}
	public static String getPrefix() {
		return prefix;
	}
	public static void setPrefix(String prefix) {
		Server.prefix = prefix;
	}
	public static String colorText(String text){
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	public static String colorSendMessage(String text){
		return ChatColor.translateAlternateColorCodes('&', text);
	}

}
