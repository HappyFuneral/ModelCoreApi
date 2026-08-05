package org.justice.authapi.controllers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.justice.authapi.utils.LuckPermsUtil;
import org.justice.modelapicorereloaded.annotation.*;
import org.justice.modelapicorereloaded.response.Response;
import org.justice.modelapicorereloaded.restapi.JwtUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;


@RestController(path="/luckperms")
public class LuckpermsController {
    private LuckPerms luckPerms;

    public LuckpermsController(){
        this.luckPerms = LuckPermsProvider.get();

    }

    @GetMapping("/get")
    @AuthenticationRequired
    public Response<Object> get(@HeaderParam("Authorization") String authHeader){
        String token = authHeader.replace("Bearer ", "");
        String usernameFromToken = JwtUtil.getUsernameFromToken(token);


        OfflinePlayer player = Bukkit.getOfflinePlayer(usernameFromToken);
        UUID uuid = player.getUniqueId();

        CompletableFuture<User> future = luckPerms.getUserManager().loadUser(uuid);
        User user = future.join(); // vagy callback, ha aszinkron kell

        if (user == null) {
            return Response.error("User not found");
        }

        String group = user.getPrimaryGroup();
        luckPerms.getGroupManager().getGroup(group).getDisplayName();
        return Response.ok(Map.of("group", group));
    }
}
