package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.content.Context;
import android.content.Intent;

/**
 * Controller responsible for console pop up UIs and player interaction.
 */
public interface IConsoleView {

    /**
     * Prompts player to go to their start beacon, and waits until they do so.
     */
    void goToStartBeaconPrompt(String homeBeaconName);

    void applicationError();

    /**
     * Prompts player to go to their home beacon, as their target has been taken down.
     */
    void playersTargetTakenDownPrompt(String homeBeaconName);

    /**
     * Tells player that they have been taken down, and associated functionality.
     */
    void playerGotTakenDownPrompt(String homeBeaconName);

    /**
     * End of game prompt.
     * @param context the context of the activity running the console controller.
     * @param goToLeaderboardIntent an intent leading to the leaderboard activity.
     */
    void endOfGamePrompt(final Context context, final Intent goToLeaderboardIntent);

    /**
     * Prompt to give the player an extra mission to complete.
     * @param beaconName The name of the beacon that the player needs to go to.
     * @param missionStatement The mission they need to complete.
     */
    void missionUpdatePrompt(String missionStatement);

    /**
     * Prompt that the mission was successful
     * @param missionSuccessMessage success message.
     */
    void missionSuccessPrompt(String missionSuccessMessage);

    /**
     * Prompt that the mission had failed.
     * @param missionFailedMessage failed message.
     */
    void missionFailedPrompt(String missionFailedMessage);

    /**
     * Reveals a console prompt that tells the player that their takedown was a success, and that
     * they need to return to their home beacon. Tap out enabled.
     * @param homeBeaconName the home beacon name.
     */
    void exposeSuccessPrompt(String homeBeaconName);

    /**
     * Closes the console box.
     */
    void closeConsole();

    /**
     * Enables user to tap console pop up to close.
     */
    void enableTapToClose();

    int getConsoleFlag(int flag);

    void setConsoleImage(int res);
}
