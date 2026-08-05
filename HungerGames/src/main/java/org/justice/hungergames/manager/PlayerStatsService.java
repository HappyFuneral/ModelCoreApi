package org.justice.hungergames.manager;

import org.justice.hungergames.HungerGamesGame;

import java.util.UUID;

public class PlayerStatsService {
    // API-n keresztül kommunikál, nem tárolja lokálisan az összes adatot
    // De gyűjtheti a jelenlegi játék statisztikáit ideiglenesen

    public void recordGameStats(UUID playerId, HungerGamesGame.PlayerStatus playerStatus, boolean isWinner) {
        // Ez a metódus hívódik meg a játék végén
        // Összegyűjti az aktuális játékból származó adatokat (kills, timeSurvived)
        // és elküldi az API-nak mentésre.

        // Példa az API hívásra (feltételezve, hogy van egy 'apiClient' objektum)
        /*
        apiClient.sendPlayerGameStats(
            playerId.toString(),
            playerStatus.getKills(),
            playerStatus.getTimeSurvived(),
            isWinner,
            // További adatok, amiket statisztikázni szeretnénk
        );
        */

        System.out.println("Statisztikák rögzítve " + playerId + " számára: Kills=" + playerStatus.getKills() + ", Túlélési idő=" + playerStatus.getTimeSurvived() + ", Nyertes=" + isWinner);
    }

    // További statisztikák, amiket gyűjthetünk és küldhetünk az API-nak:
    // - Összes játszott játék
    // - Összes győzelem
    // - K/D arány
    // - Átlagos túlélési idő
    // - Legtöbb kill egy játékban
    // - Összes gyűjtött item (ez bonyolultabb, de lehetséges)

    // Ezeket a statisztikákat az API tárolja és onnan lekérdezhetővé teszi, pl. ranglistához.
    // A PlayerRank osztályunk a RankSystem-ben a League of Legends-szerű rangpontokat kezeli,
    // míg ez a Service a mélyebb, játékmenethez kapcsolódó statisztikákat.
}