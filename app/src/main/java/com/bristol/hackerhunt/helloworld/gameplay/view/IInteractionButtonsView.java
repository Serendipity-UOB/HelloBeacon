package com.bristol.hackerhunt.helloworld.gameplay.view;

public interface IInteractionButtonsView {

    /**
     * Hides the buttons prompting interaction
     */
    void hideInteractionButtons();

    /**
     * Shows the buttons prompting interaction
     */
    void showInteractionButtons();

    /**
     * Hides the takedown select prompt.
     */
    void hideTakedownSelectPlayerButton();

    /**
     * Shows the takedown select prompt.
     */
    void showTakedownSelectPlayerButton();

    /**
     * Hides the exchange select prompt.
     */
    void hideExchangeSelectPlayerButton();

    /**
     * Shows the takedown select prompt.
     */
    void showExchangeSelectPlayerButton();
}
