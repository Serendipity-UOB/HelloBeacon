package com.bristol.hackerhunt.helloworld.gameplay.view;

public interface IExchangeRequestView {

    /**
     * Causes the exchange dialog box to appear, with a message referring to the player with the given name.
     * @param playerRealName requester's real name.
     * @param playerId requester's ID.
     */
    void showDialogueBox(String playerRealName, String playerId);

    /**
     * Hides the dialogue box.
     */
    void hideDialogueBox();

}
