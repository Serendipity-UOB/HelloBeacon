package com.bristol.hackerhunt.helloworld.gameplay.controller;

import com.bristol.hackerhunt.helloworld.gameplay.PlayerUpdate;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import java.util.HashMap;
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
    String getHomeBeaconName();

    /**
     * @return the target player's NFC ID.
     */
    String getTargetPlayerId();

    /**
     * @return the major of the nearest beacon.
     */
    String getNearestBeaconMajor();

    /**
     * @return A mapping from player id to real name.
     */
    HashMap<String, String> getPlayerIdRealNameMap();

    /**
     * Set the nearest beacon minor.
     */
    void setNearestBeaconMajor(String major);

    /**
     * Get the max rssi of a given beacon major.
     * @param major the major of the desired beacon.
     * @return the last recorded rssi.
     */
    int getBeaconRssi(String major);

    /**
     * Get the rssi of a specific beacon.
     * @param major major.
     * @param minor minor
     * @return rssi.
     */
    int getBeaconRssi(String major, String minor);

    void removeBeacon(String major, String minor);

    /**
     * @return the playing player has been taken down.
     */
    boolean playerHasBeenTakenDown();

    /**
     * Reset the status of the player; they have now not been taken down.
     */
    void resetPlayerTakenDown();

    /**
     * Get the exposerId variable from object
     * @return The string id of exposer
     */
    String getExposerId();

    /**
     * Resets the exposerId variable from object
     */
    void resetExposerId();

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
     * @param intelIncrement the amount to increase intel by as a percentage
     */
    void increasePlayerIntel(String playerId, int intelIncrement);

    /**
     * @param targetId ID of the desired player.
     * @return full intel learned on player.
     */
    boolean playerHasFullIntel(String targetId);

    /**
     * Non-zero intel.
     * @param targetId id
     * @return true if non zero.
     */
    boolean playerHasNonZeroIntel(String targetId);

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
    void updateLeaderboardPosition(String position);

    /**
     * Notifies UI of received exchange request from
     * @param reqId the requester's ID
     */
    void updateExchangeReceive(String reqId);

    /**
     * Handles a new mission coming in with description missionId
     * @param missionId mission description string
     */
    void handleNewMission(String missionId);

    /**
     * Update nearby beacon.
     * @param major major of beacon.
     * @param rssi recorded rssi of beacon.
     */
    void updateBeacon(String major, String minor, int rssi);

    /**
     * @return a set of all recorded beacon majors.
     */
    Set<String> getAllBeaconMajors();

    /**
     * @param major beacon major.
     * @return set of all minors associated with major.
     */
    Set<String> getAllBeaconMinors(String major);

    /**
     * Update the player's statuses.
     * @param updates a list of player updates.
     * @param exposerId the id of the player who exposed user if any
     */
    void updateStatus(List<PlayerUpdate> updates, String exposerId);

    /**
     * Sets a task that runs if the nearest beacon is the home beacon.
     * @param runnable a runnable that contains the task to run.
     */
    void setOnNearestBeaconBeingHomeBeaconListener(Runnable runnable);

    /**
     * Initiates change in game state when the player is at their home beacon.
     */
    void playerIsAtHomeBeacon();

    /**
     * States that the game has ended.
     */
    void setGameOver();

    /**
     * @return true if game over, false otherwise.
     */
    boolean gameHasEnded();
}
