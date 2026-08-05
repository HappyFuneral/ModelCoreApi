package org.justice.hungergames.manager;

import org.justice.hungergames.HungerGamesGame;
import org.justice.hungergames.model.Arena;

import java.util.*;
import java.util.stream.Collectors;

public class HungerGamesManager {
    private List<HungerGamesGame> activeGames;
    private Map<UUID, HungerGamesGame> playersInGameMap; // Játékos UUID -> Aktív játék
    private RankSystem rankSystem;
    private ArenaManager arenaManager;
    private PlayerStatsService playerStatsService; // Új
    private int nextGameId = 1;

    // Várólista a játékosoknak, akik játékra várnak
    private Map<UUID, Long> waitingPlayers; // Játékos UUID -> csatlakozási idő (timeouthoz)

    // Matchmaking beállítások
    private int maxRankPointDifference; // Configból jön, pl. 100 pont

    public HungerGamesManager(RankSystem rankSystem, ArenaManager arenaManager, PlayerStatsService playerStatsService, int maxRankPointDifference) {
        this.activeGames = new ArrayList<>();
        this.playersInGameMap = new HashMap<>();
        this.rankSystem = rankSystem;
        this.arenaManager = arenaManager;
        this.playerStatsService = playerStatsService;
        this.waitingPlayers = new HashMap<>();
        this.maxRankPointDifference = maxRankPointDifference;
    }

    // Játékos csatlakozik a várólistára
    public void addPlayerToQueue(UUID playerId) {
        waitingPlayers.put(playerId, System.currentTimeMillis());
        tryToMatchmake();
    }

    public void removePlayerFromQueue(UUID playerId) {
        waitingPlayers.remove(playerId);
        // Üzenet a játékosnak
    }

    // Matchmaking logika
    private void tryToMatchmake() {
        if (waitingPlayers.size() < 2) return; // Minimum 2 játékos kell

        List<PlayerRank> potentialPlayers = waitingPlayers.keySet().stream()
                .map(rankSystem::getPlayerRank) // Lekérjük a játékos rangját
                .sorted(Comparator.comparingInt(PlayerRank::getRankPoints)) // Rendezés pontok szerint
                .collect(Collectors.toList());

        List<UUID> matchedPlayers = new ArrayList<>();

        for (int i = 0; i < potentialPlayers.size(); i++) {
            PlayerRank p1 = potentialPlayers.get(i);
            matchedPlayers.add(p1.getPlayerId());

            for (int j = i + 1; j < potentialPlayers.size(); j++) {
                PlayerRank p2 = potentialPlayers.get(j);

                // Pontkülönbség ellenőrzés
                if (Math.abs(p1.getRankPoints() - p2.getRankPoints()) <= maxRankPointDifference) {
                    matchedPlayers.add(p2.getPlayerId());
                    // Ha elegendő játékos gyűlt össze egy arénához, indíthatunk játékot
                    // Ezt a logikát érdemes finomítani, pl. nem csak kettesével keresni
                    // Hanem összeállítani egy csoportot egy adott arénához
                    Arena suitableArena = findSuitableArena(matchedPlayers.size());
                    if (suitableArena != null) {
                        createNewGame(suitableArena, matchedPlayers);
                        matchedPlayers.forEach(waitingPlayers::remove); // Eltávolítjuk a matched játékosokat a várólistáról
                        return; // Egy játékot elindítottunk, következő körben újra mehet a matchmaking
                    }
                }
            }
            matchedPlayers.clear(); // Ha nem találtunk elég játékost az aktuális 'p1'-hez
        }

        // Itt jöhet a "lazább" matchmaking, ha sokáig várnak a játékosok
        // Pl. ha egy játékos X ideje vár, akkor kibővítjük a pontkülönbséget, vagy egyáltalán nem nézzük.
    }

    private Arena findSuitableArena(int playerCount) {
        // Megkeresi a legalkalmasabb arénát a játékosszám alapján
        // Elsősorban olyan arénát keressen, aminek van elég szabad spawn pontja
        return arenaManager.getArenas().values().stream()
                .filter(arena -> arena.getMinPlayers() <= playerCount && arena.getMaxPlayers() >= playerCount)
                .findFirst() // Vagy válasszon a szabad arénák közül
                .orElse(null);
    }


    public HungerGamesGame createNewGame(Arena arena, List<UUID> players) {
        HungerGamesGame newGame = new HungerGamesGame(nextGameId++, arena, players);
        activeGames.add(newGame);
        players.forEach(uuid -> playersInGameMap.put(uuid, newGame)); // Játékosok hozzáadása a játékhoz
        newGame.startGame();
        return newGame;
    }

    public void onPlayerDeath(UUID gameId, UUID deadPlayerId, UUID killerId) {
        activeGames.stream()
                .filter(game -> game.getGameId() == gameId)
                .findFirst()
                .ifPresent(game -> {
                    if (killerId != null) {
                        game.playerKilledPlayer(killerId, deadPlayerId);
                    } else {
                        game.playerDied(deadPlayerId);
                    }

                    if (game.getCurrentState() == HungerGamesGame.GameState.ENDED) {
                        processGameResults(game);
                        activeGames.remove(game);
                        game.getPlayersInGame().forEach(playersInGameMap::remove); // Eltávolítjuk a játékosokat a map-ből
                    }
                });
    }

    private void processGameResults(HungerGamesGame game) {
        UUID winner = game.getWinnerId();
        for (UUID playerId : game.getPlayerStatuses().keySet()) {
            boolean isWinner = playerId.equals(winner);
            // Rangpontok frissítése
            rankSystem.updatePlayerRank(playerId, isWinner);
            // Statisztikák rögzítése
            playerStatsService.recordGameStats(playerId, game.getPlayerStatus(playerId), isWinner);
        }
    }

    public HungerGamesGame getGameByPlayer(UUID playerId) {
        return playersInGameMap.get(playerId);
    }
}