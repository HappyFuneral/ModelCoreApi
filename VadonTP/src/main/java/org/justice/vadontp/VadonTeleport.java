package org.justice.vadontp;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class VadonTeleport extends JavaPlugin implements Listener {

    private final Random random = new Random();
    private int minX, maxX, minZ, maxZ;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("RandomTeleportSign plugin bekapcsolva.");
    }

    private void loadConfig() {
        minX = getConfig().getInt("min-x", -5000);
        maxX = getConfig().getInt("max-x", 5000);
        minZ = getConfig().getInt("min-z", -5000);
        maxZ = getConfig().getInt("max-z", 5000);
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {
        Player player = event.getPlayer();
        String line0 = ChatColor.stripColor(event.getLine(0));
        String line1 = ChatColor.stripColor(event.getLine(1));

        if (line0.equalsIgnoreCase("[RandomTP]" ) && !line1.isEmpty() ) {
            if (player.hasPermission("rtpsign.create")) {
                event.setLine(0, ChatColor.GREEN + "[RandomTP]");
                event.setLine(1, ChatColor.RED + line1);
                player.sendMessage(ChatColor.GREEN + "Teleport tábla sikeresen létrehozva!");
            } else {
                event.setLine(0, ChatColor.RED + "Nincs jogod!");
                event.setLine(1, "");
                event.setLine(2, "");
                event.setLine(3, "");
                player.sendMessage(ChatColor.RED + "Nincs jogosultságod RTP tábla létrehozására!");
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        String line0 = ChatColor.stripColor(sign.getLine(0));
        String line1 = ChatColor.stripColor(sign.getLine(1));

        if (!line0.equalsIgnoreCase("[RandomTP]")) return;

        Player player = event.getPlayer();
        World world = Bukkit.getWorld(line1);
        if(player.getGameMode() == GameMode.CREATIVE && player.hasPermission("rtpsign.edit") ) return;

        if (world == null) {
            player.sendMessage(ChatColor.RED + "A világ nem található: " + line1);
            return;
        }
        if (!player.hasPermission("rtp.teleport."+line1)){
            player.sendMessage(ChatColor.RED + "Nincs jogod, hogy ide teleportálj: " + line1);
            return;
        }
        Location loc = getSafeRandomLocation(world);
        if (loc == null) {
            player.sendMessage(ChatColor.RED + "Nem találtam biztonságos helyet. Próbáld újra.");
            return;
        }

        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "Elteleportáltál egy biztonságos, véletlenszerű helyre!");
    }

    private Location getSafeRandomLocation(World world) {
        for (int i = 0; i < 20; i++) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int z = random.nextInt(maxZ - minZ + 1) + minZ;
            int y = world.getHighestBlockYAt(x, z);
            Material below = world.getBlockAt(x, y - 1, z).getType();

            if (isSafeBlock(below)) {
                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
        }
        return null;
    }

    private boolean isSafeBlock(Material block) {
        return block.isSolid() && block != Material.LAVA && block != Material.LEGACY_STATIONARY_WATER
                && block != Material.WATER && block != Material.LEGACY_STATIONARY_WATER
                && block != Material.CACTUS && block != Material.FIRE;
    }
}
