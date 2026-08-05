package org.justice.hungergames;

import org.justice.hungergames.model.Arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class HungerGamesGame {
    private int gameId;
    private Arena arena; // Az aréna, ahol a játék zajlik
    private GameState currentState;
    private List<UUID> playersInGame;
    private Map<UUID, PlayerStatus> playerStatuses; // Élő/halott állapot és egyéb statisztikák
    private UUID winnerId;
    private long startTime;
    private List<UUID> spectators; // Nézők listája

    public HungerGamesGame(int gameId, Arena arena, List<UUID> initialPlayers) {
        this.gameId = gameId;
        this.arena = arena;
        this.currentState = GameState.WAITING;
        this.playersInGame = new ArrayList<>(initialPlayers);
        this.playerStatuses = initialPlayers.stream()
                .collect(Collectors.toMap(uuid -> uuid, uuid -> new PlayerStatus(uuid)));
        this.spectators = new ArrayList<>();
    }

    public enum GameState {
        WAITING, COUNTDOWN, IN_GAME, DEATHMATCH, ENDED
    }

    // PlayerStatus belső osztály a statisztikákhoz
    public static class PlayerStatus {
        private UUID playerId;
        private boolean isAlive;
        private int kills;
        private long timeSurvived;
        // További statisztikák ide jöhetnek
        public PlayerStatus(UUID playerId) {
            this.playerId = playerId;
            this.isAlive = true;
            this.kills = 0;
            this.timeSurvived = 0;
        }
        // Getters and Setters
        public UUID getPlayerId() { return playerId; }
        public boolean isAlive() { return isAlive; }
        public void setAlive(boolean alive) { isAlive = alive; }
        public int getKills() { return kills; }
        public void addKill() { this.kills++; }
        public void setTimeSurvived(long timeSurvived) { this.timeSurvived = timeSurvived; }
        public long getTimeSurvived() { return timeSurvived; }
    }

    // --- Játékmenet Logika ---

    public void startGame() {
        if (currentState == GameState.WAITING && playersInGame.size() >= arena.getMinPlayers()) {
            this.currentState = GameState.COUNTDOWN;
            // Teleportáljuk a játékosokat az aréna spawn pontjaira
            // Kitöltjük a chesteket a configban megadott loot-tal (lásd Arena osztály)
            // Indítunk egy visszaszámlálót
            // Miután a visszaszámlálás lejárt:
            this.currentState = GameState.IN_GAME;
            this.startTime = System.currentTimeMillis();
            // broadcast üzenet: "A játék elkezdődött!"
        }
    }

    public void playerDied(UUID playerId) {
        PlayerStatus status = playerStatuses.get(playerId);
        if (status != null && status.isAlive()) {
            status.setAlive(false);
            status.setTimeSurvived(System.currentTimeMillis() - startTime); // Túlélési idő rögzítése
            playersInGame.remove(playerId); // Eltávolítjuk az élő játékosok közül

            // Lehetőség nézővé válásra
            // Eltávolítjuk a játékost a játékból, pl. teleportáljuk a hubra
            // Küldünk üzenetet a halálról

            long alivePlayers = playersInGame.size();
            if (alivePlayers <= 1) { // Utolsó játékos vagy senki nem maradt
                endGame();
            } else if (alivePlayers <= arena.getDeathmatchThreshold()) {
                initiateDeathmatch();
            }
        }
    }

    public void playerKilledPlayer(UUID killerId, UUID victimId) {
        PlayerStatus killerStatus = playerStatuses.get(killerId);
        if (killerStatus != null) {
            killerStatus.addKill();
            // Broadcast üzenet: "JátékosX megölte JátékosY-t!"
        }
        playerDied(victimId);
    }

    private void initiateDeathmatch() {
        if (currentState == GameState.IN_GAME) {
            this.currentState = GameState.DEATHMATCH;
            // Teleportáljuk a megmaradt játékosokat a deathmatch arénába
            // Kisebb aréna, több akció, broadcast üzenetek
            // Adott idő után, ha még mindig van több játékos, kényszerített győzteshirdetés (pl. pontok alapján)
        }
    }

    private void endGame() {
        this.currentState = GameState.ENDED;
        if (playersInGame.size() == 1) {
            this.winnerId = playersInGame.get(0);
            // Broadcast üzenet a győztesről
        } else {
            this.winnerId = null; // Nincs győztes (pl. mindenki feladta, vagy timeout)
        }
        // Itt jöhet a RankSystem.updatePlayerRank() hívása minden játékosra
        // És a TournamentManager értesítése, ha bajnoksági játékról van szó.
        // Ez a logika kerül a HungerGamesManager-be
    }

    // Getters
    public int getGameId() { return gameId; }
    public Arena getArena() { return arena; }
    public GameState getCurrentState() { return currentState; }
    public List<UUID> getPlayersInGame() { return playersInGame; }
    public UUID getWinnerId() { return winnerId; }
    public PlayerStatus getPlayerStatus(UUID playerId) { return playerStatuses.get(playerId); }
    public List<UUID> getSpectators() { return spectators; }

    public void addSpectator(UUID playerId) {
        if (!playersInGame.contains(playerId) && !spectators.contains(playerId)) {
            spectators.add(playerId);
            // Teleportáljuk a nézőt az arénába, gamemode beállítás, stb.
        }
    }
}