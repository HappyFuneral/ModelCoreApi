package hu.justice.pvpcore.levels;



import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;



public class LevelUpListener implements Listener {

	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
			Levels.build(e.getPlayer().getName());
			PermissionManager.setup(e.getPlayer(), Levels.getLevel(e.getPlayer().getName()));
		
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player pl = e.getPlayer();
		PermissionManager.unSetup(pl, Levels.getLevel(pl.getName()));
	   	Levels.getLevels().remove(Levels.getLevel(pl.getName()));
	}
}
