package com.bristol.hackerhunt.helloworld.gameplay;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import java.util.List;
import java.util.Set;

public interface IGameStateController {

    /**
     * @return the ID of the playing player.
     */
    String getPlayerId();

    /**
     * @return the player's home beacon.
     */
    String getHomeBeacon();

    /**
     * @return the target player's NFC ID.
     */
    String getTargetPlayerId();

    /**
     * Get the rssi of a given beacon.
     * @param minor the minor of the desired beacon.
     * @return the last recorded rssi.
     */
    int getBeaconRssi(String minor);

    /**
     * @return the playing player has been taken down.
     */
    boolean playerHasBeenTakenDown();

    /**
     * Reset the status of the player; they have now not been taken down.
     */
    void resetPlayerTakenDown();

    /**
     * @return the playing player's target has been taken down.
     */
    boolean playersTargetHasBeenTakenDown();

    /**
     * Reset the status of the player; their target has now not been taken down yet.
     */
    void resetPlayersTargetHasBeenTakenDown();

    /**
     * Increases the intel learned about the given player.
     * @param playerId the NFC ID of the target player.
     */
    void increasePlayerIntel(String playerId);

    /**
     * @param targetId ID of the desired player.
     * @return full intel learned on player.
     */
    boolean playerHasFullIntel(String targetId);

    /**
     * Lose half of the player's learned intel on other players.
     */
    void loseHalfOfPlayersIntel();

    /**
     * Set all of the players playing the game.
     * @param allPlayersIdentifiers a list of player identifiers for every player.
     */
    void setAllPlayers(List<PlayerIdentifiers> allPlayersIdentifiers);

    /**
     * @return all players have been set.
     */
    boolean allPlayersHaveBeenSet();

    /**
     * Update the target player.
     * @param targetPlayerId the new target's NFC ID.
     */
    void updateTargetPlayer(String targetPlayerId);

    /**
     * Update nearby players to the player.
     * @param playerIds a list of nearby player IDs.
     */
    void updateNearbyPlayers(List<String> playerIds);

    /**
     * Update player's points
     * @param points points.
     */
    void updatePoints(int points);

    /**
     * Update the player's leaderboard position.
     * @param position leaderboard position.
     */
    void updatePosition(String position);

    /**
     * Update nearby beacon.
     * @param minor minor of beacon.
     * @param rssi recorded rssi of beacon.
     */
    void updateBeacon(String minor, int rssi);

    /**
     * @return a set of all recorded beacon minors.
     */
    Set<String> getAllBeaconMinors();

    /**
     * Update the player's statuses.
     * @param updates a list of player updates.
     */
    void updateStatus(List<PlayerUpdate> updates);
}
