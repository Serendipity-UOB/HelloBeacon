package com.bristol.hackerhunt.helloworld.gameplay;

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

    //TODO.
    void executingTakedownPrompt();

    //TODO
    void takedownSuccessPrompt(String homeBeaconName);

    //TODO
    void takedownNotYourTargetPrompt();

    //TODO
    void takedownInsufficientIntelPrompt();

    // TODO
    void closeConsole();

    // TODO
    void exchangeRequestedPrompt();

    //TODO
    void exchangeSuccessPrompt();

    //TODO
    void exchangeFailedPrompt();
}
