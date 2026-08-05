package org.justice.hungergames.model;

import org.justice.hungergames.HungerGamesGame;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tournament {
    private String id;
    private String name;
    private Arena arena; // Melyik arénában zajlik
    public TournamentState currentState;
    private List<UUID> registeredPlayers;
    private UUID winnerId;
    private long registrationEndTime; // Mikor zárul a jelentkezés
    private long startTime; // Mikor kezdődik a bajnokság
    private List<String> prizes; // Díjak listája

    public Tournament(String id, String name, Arena arena, long registrationEndTime, long startTime, List<String> prizes) {
        this.id = id;
        this.name = name;
        this.arena = arena;
        this.registrationEndTime = registrationEndTime;
        this.startTime = startTime;
        this.prizes = prizes;
        this.currentState = TournamentState.REGISTRATION;
        this.registeredPlayers = new ArrayList<>();
    }

    public enum TournamentState {
        REGISTRATION, WAITING_TO_START, IN_PROGRESS, ENDED
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Arena getArena() { return arena; }
    public TournamentState getCurrentState() { return currentState; }
    public List<UUID> getRegisteredPlayers() { return registeredPlayers; }
    public UUID getWinnerId() { return winnerId; }
    public long getRegistrationEndTime() { return registrationEndTime; }
    public long getStartTime() { return startTime; }
    public List<String> getPrizes() { return prizes; }


    public boolean registerPlayer(UUID playerId) {
        if (currentState == TournamentState.REGISTRATION && System.currentTimeMillis() < registrationEndTime) {
            if (!registeredPlayers.contains(playerId)) {
                registeredPlayers.add(playerId);
                // Üzenet a játékosnak
                return true;
            }
        }
        return false;
    }

    public void unregisterPlayer(UUID playerId) {
        if (currentState == TournamentState.REGISTRATION && registeredPlayers.contains(playerId)) {
            registeredPlayers.remove(playerId);
            // Üzenet a játékosnak
        }
    }

    public void startTournamentGame(HungerGamesGame game) {
        this.currentState = TournamentState.IN_PROGRESS;
        // Elindítja a HungerGamesGame-et a regisztrált játékosokkal
        // A game.endGame() metódus fogja majd értesíteni a TournamentManagert
        // Fontos: a Tournament játékban Nincs rangpont változás, csak díjak!
    }

    public void endTournament(UUID winnerId) {
        this.winnerId = winnerId;
        this.currentState = TournamentState.ENDED;
        // Díjak kiosztása a győztesnek
        // broadcast üzenet a bajnokság eredményéről
    }
}