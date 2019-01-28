package com.bristol.hackerhunt.helloworld.gameplay;

/**
 * A controller class responsible for the status bar of the UI.
 */
public interface IPlayerStatusBarView {

    /**
     * Set the hacker name of the player's target.
     * @param targetHackerName the target hacker name.
     */
    void setPlayerTargetHackerName(String targetHackerName);

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
}
