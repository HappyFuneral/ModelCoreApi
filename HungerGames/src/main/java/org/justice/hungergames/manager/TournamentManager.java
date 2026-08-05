package org.justice.hungergames.manager;

import org.justice.hungergames.HungerGames;
import org.justice.hungergames.HungerGamesGame;
import org.justice.hungergames.model.Arena;
import org.justice.hungergames.model.Tournament;

import java.util.*;

public class TournamentManager {
    private List<Tournament> activeTournaments;
    private HungerGamesManager hungerGamesManager; // Hogy el tudja indítani a játékot
    private ArenaManager arenaManager; // Hogy tudjon arénát választani
    private HungerGames plugin; // Referencia a fő pluginre a BukkitScheduler miatt

    public TournamentManager(HungerGamesManager hungerGamesManager, ArenaManager arenaManager, HungerGames plugin) {
        this.activeTournaments = new ArrayList<>();
        this.hungerGamesManager = hungerGamesManager;
        this.arenaManager = arenaManager;
        this.plugin = plugin;
        scheduleTournamentChecks();
    }

    public void createTournament(String id, String name, String arenaId, long registrationDurationMillis, long startDelayMillis, List<String> prizes) {
        Arena arena = arenaManager.getArena(arenaId);
        if (arena == null) {
            System.err.println("Aréna nem található: " + arenaId);
            return;
        }

        long registrationEndTime = System.currentTimeMillis() + registrationDurationMillis;
        long startTime = registrationEndTime + startDelayMillis;
        Tournament newTournament = new Tournament(id, name, arena, registrationEndTime, startTime, prizes);
        activeTournaments.add(newTournament);
        System.out.println("Bajnokság létrehozva: " + name + ", Regisztráció vége: " + new Date(registrationEndTime) + ", Kezdés: " + new Date(startTime));
        // Itt menteni kellene az adatbázisba/configba a bajnokságot
    }

    public boolean registerPlayerToTournament(String tournamentId, UUID playerId) {
        return activeTournaments.stream()
                .filter(t -> t.getId().equals(tournamentId))
                .findFirst()
                .map(tournament -> tournament.registerPlayer(playerId))
                .orElse(false);
    }

    private void scheduleTournamentChecks() {
        // Bukkit scheduler: minden X másodpercenként ellenőrizni az aktív bajnokságokat
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Tournament> iterator = activeTournaments.iterator();
            while (iterator.hasNext()) {
                Tournament tournament = iterator.next();
                long currentTime = System.currentTimeMillis();

                switch (tournament.getCurrentState()) {
                    case REGISTRATION:
                        if (currentTime >= tournament.getRegistrationEndTime()) {
                            if (tournament.getRegisteredPlayers().size() >= tournament.getArena().getMinPlayers()) {
                                tournament.currentState = Tournament.TournamentState.WAITING_TO_START;
                                System.out.println("Bajnokság regisztráció lezárult: " + tournament.getName());
                            } else {
                                System.out.println("Bajnokság megszakítva (kevés jelentkező): " + tournament.getName());
                                // Refund, üzenetek
                                iterator.remove(); // Töröljük a bajnokságot
                            }
                        }
                        break;
                    case WAITING_TO_START:
                        if (currentTime >= tournament.getStartTime()) {
                            // Elindítjuk a Hunger Games játékot a regisztrált játékosokkal
                            HungerGamesGame game = hungerGamesManager.createNewGame(tournament.getArena(), tournament.getRegisteredPlayers());
                            tournament.startTournamentGame(game);
                            System.out.println("Bajnokság játék elindult: " + tournament.getName());
                        }
                        break;
                    case IN_PROGRESS:
                        // A HungerGamesGame fogja értesíteni a TournamentManagert, ha vége a játéknak.
                        // (pl. egy callback metóduson keresztül)
                        break;
                    case ENDED:
                        // Bajnokság lezárult, díjak kiosztva
                        System.out.println("Bajnokság befejeződött: " + tournament.getName());
                        iterator.remove(); // Töröljük az aktív bajnokságok közül
                        break;
                }
            }
        }, 20L * 5, 20L * 5); // Minden 5 másodpercben fut
    }

    // Callback metódus a HungerGamesManager-től, amikor egy Tournament játék véget ér
    public void onTournamentGameEnded(String tournamentId, UUID winnerId) {
        activeTournaments.stream()
                .filter(t -> t.getId().equals(tournamentId))
                .findFirst()
                .ifPresent(tournament -> tournament.endTournament(winnerId));
    }
}