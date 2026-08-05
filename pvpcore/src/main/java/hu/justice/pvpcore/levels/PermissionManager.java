package hu.justice.pvpcore.levels;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import hu.justice.pvpcore.App;




public class PermissionManager {
	private static HashMap<UUID,PermissionAttachment> list = new HashMap<>();
	private static App main = App.getPlugin(App.class);
	
	public static void setup(Player pl , Level level) {
		PermissionAttachment attachment = pl.addAttachment(main);
		list.put(pl.getUniqueId(), attachment);
		setPermission(pl.getUniqueId(), level);
	}
	private static void setPermission(UUID uuid, Level level) {
		PermissionAttachment att = list.get(uuid);
		
			for (String strting2 : level.getRewards()) {
				att.setPermission(strting2, true);
				System.out.println("SAD "+strting2);
			
		}
	}
	private static void unsetPermission(UUID uuid, Level level) {
		PermissionAttachment att = list.get(uuid);
		
		for (String strting2 : level.getRewards()) {
			att.setPermission(strting2,false);
			System.out.println("SAD "+uuid.toString());
		
	}
	}
		public static void unSetup(Player pl , Level level) {
			PermissionAttachment attachment = pl.addAttachment(main);
			list.remove(pl.getUniqueId(), attachment);
			unsetPermission(pl.getUniqueId(), level);
		
 }
		public static void setSinglePermission(String permission, Player pl) {
			PermissionAttachment att = list.get(pl.getUniqueId());
			att.setPermission(permission, true);
		}
		public static void unsetSinglePermission(String permission, Player pl) {
			PermissionAttachment att = list.get(pl.getUniqueId());
			att.setPermission(permission,false);
		}
}