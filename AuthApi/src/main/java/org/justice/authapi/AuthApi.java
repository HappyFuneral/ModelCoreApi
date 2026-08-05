package org.justice.authapi;

import net.luckperms.api.LuckPerms;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.justice.authapi.controllers.AuthController;
import org.justice.authapi.controllers.LuckpermsController;
import org.justice.authapi.controllers.PlayerPointsController;
import org.justice.modelapicorereloaded.restapi.RestApiServer;

import java.io.IOException;

public final class AuthApi extends JavaPlugin {

    RestApiServer restApiServer;


    @Override
    public void onEnable() {
        try {
            restApiServer =  new RestApiServer(8080);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        restApiServer.registerController(new AuthController());
        restApiServer.registerController(new PlayerPointsController());
        restApiServer.registerController(new LuckpermsController());

        restApiServer.start();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");

        if (plugin == null) {
            getLogger().warning("PlayerPoints plugin not found!");
        } else if (!(plugin instanceof PlayerPoints)) {
            getLogger().warning("Plugin 'PlayerPoints' found, but is not an instance of PlayerPoints!");
        } else {
            PlayerPoints playerPoints = (PlayerPoints) plugin;
            getLogger().info("PlayerPoints plugin successfully hooked!");
        }
        Plugin lp = Bukkit.getPluginManager().getPlugin("LuckPerms");

        if (lp == null) {
            getLogger().warning("LuckPerms plugin not found!");
        } else if (!(lp instanceof LuckPerms)) {
            getLogger().warning("Plugin 'LuckPerms' found, but is not an instance of LuckPerms!");
        } else {
            LuckPerms playerPoints = (LuckPerms) lp;
            getLogger().info("LuckPerms plugin successfully hooked!");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
