package com.bristol.hackerhunt.helloworld.gameplay.view;

import com.bristol.hackerhunt.helloworld.StringInputRunnable;

/**
 * A controller class responsible for the status bar of the UI.
 */
public interface IPlayerStatusBarView {

    /**
     * Set the hacker name of the player's target.
     * @param targetHackerName the target hacker name.
     */
    void setPlayerTargetCodeName(String targetHackerName);

    /**
     * Set the player's points.
     * @param points points.
     */
    void setPlayerPoints(String points);

    /**
     * Set player's leaderboard position.
     * @param position position.
     */
    void setPlayerLeaderboardPosition(String position);

    /**
     * Set player's name
     * @param playerName the player's name
     */
    void setPlayerName(String playerName);

    /**
     * Sets the image for player location
     * @param flag TODO int ResId determining the flag to be displayed
     */
    void setPlayerLocation(int flag);

    /**
     * Darken all of the elements in the status bar.
     */
    void darken();

    /**
     * Restore the status bar colors after being darkened.
     */
    void restore();

    StringInputRunnable changePlayerLocationRunnable();
}
