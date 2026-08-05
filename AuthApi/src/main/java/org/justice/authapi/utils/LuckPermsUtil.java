package org.justice.authapi.utils;


import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.UUID;

public class LuckPermsUtil {

    private final LuckPerms luckPerms;

    public LuckPermsUtil() {
        this.luckPerms = LuckPermsProvider.get();
    }

    /**
     * Lekéri a játékos elsődleges csoportját (primary group) név alapján.
     */
    public void getPrimaryGroup(String playerName, Consumer<String> callback) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = offlinePlayer.getUniqueId();
        luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            if (user != null) {
                callback.accept(user.getPrimaryGroup());
            } else {
                callback.accept(null);
            }
        });
    }

    /**
     * Lekéri a játékos elsődleges csoportját (primary group) UUID alapján.
     */
    public void getPrimaryGroup(UUID uuid, Consumer<String> callback) {
        luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            if (user != null) {
                callback.accept(user.getPrimaryGroup());
            } else {
                callback.accept(null);
            }
        });
    }

    /**
     * Lekéri a játékos összes csoportját (nem csak a primary-t).
     */
    public void getAllGroups(String playerName, Consumer<Set<String>> callback) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = offlinePlayer.getUniqueId();
        luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            if (user != null) {
                Set<String> groups = user.getNodes().stream()
                        .filter(node -> node instanceof InheritanceNode)
                        .map(node -> ((InheritanceNode) node).getGroupName())
                        .collect(Collectors.toSet());
                callback.accept(groups);
            } else {
                callback.accept(Collections.emptySet());
            }
        });
    }

    /**
     * Ellenőrzi, hogy a játékos egy adott csoport tagja-e.
     */
    public void hasGroup(String playerName, String group, Consumer<Boolean> callback) {
        getAllGroups(playerName, groups -> callback.accept(groups.contains(group)));
    }
}
