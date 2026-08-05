package org.justice.authapi.controllers;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.justice.modelapicorereloaded.annotation.GetMapping;
import org.justice.modelapicorereloaded.annotation.HeaderParam;
import org.justice.modelapicorereloaded.annotation.RestController;
import org.justice.modelapicorereloaded.response.Response;
import org.justice.modelapicorereloaded.restapi.JwtUtil;

import java.util.Map;
import java.util.UUID;

@RestController(path = "/points")
public class PlayerPointsController {



    @GetMapping("/get")
    public Response<Object> getPoints(@HeaderParam("Authorization") String authHeader){
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (plugin instanceof PlayerPoints) {
            PlayerPoints playerPoints = (PlayerPoints) plugin;
            String token = authHeader.replace("Bearer ", "");
            String user = JwtUtil.getUsernameFromToken(token);
            UUID uuid = playerPoints.getAPI().getAccountUUIDByName(user);
            return Response.ok(Map.of("points",playerPoints.getAPI().look(uuid)));
        } else {

            return Response.error("Hiba a lekérés során!");
        }
    }
}
