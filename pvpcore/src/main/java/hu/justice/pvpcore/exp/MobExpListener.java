package hu.justice.pvpcore.exp;

import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.commands.task.RegionLister;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import hu.justice.pvpcore.App;
import hu.justice.pvpcore.tools.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import hu.justice.pvpcore.levels.Levels;
import hu.justice.pvpcore.party.Parties;
import hu.justice.pvpcore.party.Party;


public class MobExpListener implements Listener {

	
	/**************************
	 * This method call when 
	 * an entity died.
	 * The killer(player) gain
	 * some xp and than the
	 * scoreboard refresh
	 * 
	 **************************
	 */
	@EventHandler
	public void onKillMob(EntityDeathEvent e) {
		if (e.getEntity().getKiller() instanceof Player) {
			Player pl = (Player) e.getEntity().getKiller();
			boolean sameParty = false;
			if (e.getEntity() instanceof Player) {
				Party killerParty = Parties.getParty(pl);
				if (killerParty.getPlayers().contains((Player)e.getEntity())) {
					sameParty = true;
				}
			}
			Parties.shareExp(pl, Parties.getParty(pl), Experiences.getExp(e.getEntity().getType()).getXp(), sameParty);
		}
			
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent e){
		Player pl = e.getPlayer();
		LocalPlayer localPlayer = App.worldGuardPlugin.wrapPlayer(pl);
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(localPlayer.getLocation());
		if (!set.testState(localPlayer, Flags.BLOCK_BREAK)){
			Server.sendMessage(pl, "Itt nem szerezhetsz xp-t!");
		} else
			Parties.shareExp(pl, Parties.getParty(pl), Experiences.getExp(e.getBlock().getType()).getXp(),false);
	}
}
