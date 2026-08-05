package org.justice.authapi.controllers;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.justice.authapi.UserRequest;
import org.justice.modelapicorereloaded.annotation.*;
import org.justice.modelapicorereloaded.response.Response;
import org.justice.modelapicorereloaded.restapi.JwtUtil;

import java.util.Map;

@RestController(path = "/auth")
public class AuthController {
    AuthMeApi authMeApi = AuthMeApi.getInstance();

    @PostMapping("/login")
    public Response<Object> login(@RequestBody UserRequest request){
        if (!authMeApi.isRegistered(request.username)){
            return Response.error("A karakter nincs regisztrálva!");
        }
        boolean passwordMatches = authMeApi.checkPassword(request.username, request.password);
        if (!passwordMatches){
           return Response.error("Nem egyezik a jelszó!");
       }
       return Response.ok("Sikeres bejelentkezés!", Map.of("accessToken",JwtUtil.generateToken(request.username)));
    }

    @PostMapping("/register")
    public Response<Object> register(@RequestBody UserRequest request){
        if (authMeApi.isRegistered(request.username)){
            return Response.error("A karakter regisztrálva van!");
        }
        boolean registerResult = authMeApi.registerPlayer(request.username, request.password);
        if (registerResult){
            return Response.ok("Sikeres regisztráció!", Map.of("accessToken",JwtUtil.generateToken(request.username)));

        } else {
            return Response.error("Sikertelen volt a regisztráció!");
        }
    }
    @PostMapping("/changepassword")
    @AuthenticationRequired
    public String changePassword(@RequestBody String password,@HeaderParam("Authorization") String authHeader){
        String token = authHeader.replace("Bearer ", "");
        String user = JwtUtil.getUsernameFromToken(token);
        if (user.isEmpty()){
            return "Nem vagy bejelentkezve!";
        }

        return "Sikeres jelszó változtatás!";
    }


    @AuthenticationRequired
    @GetMapping("/validateToken")
    public Response<Object> validateToken(@HeaderParam("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        boolean validated = JwtUtil.validateToken(token);
        if(validated){
            return Response.ok("Érvényes a token!",Map.of("validated", true));
        }
        return Response.ok("Érvénytelen a token!",Map.of("validated", false));
    }

    @GetMapping("/{player}")
    public String player(@PathParam("player") String player){
        return player;
    }
}
