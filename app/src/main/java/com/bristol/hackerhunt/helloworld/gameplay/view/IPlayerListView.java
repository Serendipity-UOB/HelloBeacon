package com.bristol.hackerhunt.helloworld.gameplay.view;

import java.util.List;

/**
 * Controller class responsible for the visible list of players in the gameplay UI.
 */
public interface IPlayerListView {

    /**
     * Sets the codename of the player's target.
     * @param codeName codename.
     */
    void setTargetCodeName(String codeName);

    /**
     * Reveal the hacker name of a player on the list.
     * @param playerId the ID of the player.
     * @param hackerName the hacker name of the player to be revealed.
     */
    void revealPlayerHackerName(String playerId, final String hackerName);

    /**
     * Insert a new player into the list.
     * @param playerId the ID of the player to insert.
     * @param playerName the name of the player to display in the UI.
     */
    void insertPlayer(String playerId, String playerName);

    /**
     * Increase the intel of a given player.
     * @param playerId the ID of the player.
     * @param intelIncrement the amount of intel to add, in a percentage.
     */
    void increasePlayerIntel(String playerId, int intelIncrement);

    /**
     * Decrease the intel of a given player.
     * @param playerId the ID of the player.
     * @param intelIncrement the amount of intel to subtract, in a percentage.
     */
    void decreasePlayerIntel(String playerId, int intelIncrement);

    /**
     * Update the visible list of nearby players in the list UI.
     * @param newNearbyPlayerIds the list of nearby player IDs.
     */
    void updateNearbyPlayers(List<String> newNearbyPlayerIds);

    /**
     * Begins process of takedown; greys out far away players, and add select on-click listeners to
     * nearby players
     */
    void beginTakedown();

    /**
     * Resumes gameplay after interaction, by returning the player list to how it was before.
     */
    void resumeGameplayAfterInteraction();

    /**
     * Begins process of mutual takedown; greys out far away players, and add select on-click
     * listeners to nearby players
     */
    void beginExchange();
}
