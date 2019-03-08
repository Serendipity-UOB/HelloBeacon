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
    void missionUpdatePrompt(String beaconName, String missionStatement);

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
     * Reveals a console prompt that tells the player that takedown is being executed. Tap out
     * is disabled.
     */
    void executingTakedownPrompt();

    /**
     * Reveals a console prompt that tells the player that intercept is being executed. Tap out
     * enabled.
     */
    void executingInterceptPrompt();

    /**
     * Reveals a console prompt that tells the player that their takedown was a success, and that
     * they need to return to their home beacon. Tap out enabled.
     * @param homeBeaconName the home beacon name.
     */
    void takedownSuccessPrompt(String homeBeaconName);

    /**
     * Reveals a console prompt that tells the the player that their takedown recipient isn't their
     * target.
     */
    void takedownNotYourTargetPrompt();

    /**
     * Reveals a console prompt that tells the the player that they don't have enough intel on their
     * takedown target.
     */
    void takedownInsufficientIntelPrompt();

    /**
     * Closes the console box.
     */
    void closeConsole();

    /**
     * Reveals a console prompt that tells the the player that their exchange has been requested.
     */
    void exchangeRequestedPrompt();

    /**
     * Reveals a console prompt that tells the the player that their exchange has been successful.
     */
    void exchangeSuccessPrompt();

    /**
     * Reveals a console prompt that tells the the player that their exchange has failed.
     */
    void exchangeFailedPrompt();

    /**
     * Enables user to tap console pop up to close.
     */
    void enableTapToClose();
}
