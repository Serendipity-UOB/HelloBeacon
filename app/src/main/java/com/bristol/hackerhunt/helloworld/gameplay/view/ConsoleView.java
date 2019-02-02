package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.Typewriter;

public class ConsoleView implements IConsoleView {

    private static final int TYPEWRITER_SPEED = 10;     // given in milliseconds.

    private final View overlay;
    private final View consoleView;
    private final TextView consoleViewText;

    private final Typewriter typewriter;

    private boolean interactionInProgress;

    public ConsoleView(View consolePromptContainer) {
        this.overlay = consolePromptContainer;
        this.consoleView = overlay.findViewById(R.id.gameplay_console);
        this.consoleViewText = overlay.findViewById(R.id.gameplay_console_text);

        this.typewriter = new Typewriter(TYPEWRITER_SPEED);

        this.interactionInProgress = false;

        enableCloseConsole();
    }

    private void enableCloseConsole() {
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
            }
        });
        showCloseButton();
    }

    private void disableCloseConsole() {
        overlay.setOnClickListener(null);
        hideCloseButton();
    }

    private void showCloseButton() {
        ((TextView) consoleView.findViewById(R.id.console_close_button)).setText("X ");
    }

    private void hideCloseButton() {
        ((TextView) consoleView.findViewById(R.id.console_close_button)).setText(" ");
    }

    @Override
    public void goToStartBeaconPrompt(String homeBeaconName) {
        // disableCloseConsole();
        goToStartBeaconConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    private void goToStartBeaconConsoleMessage(String homeBeaconName) {
        String message = overlay.getContext().getString(R.string.console_start_beacon_message);
        message = message.replace("$BEACON", homeBeaconName);
        consoleMessage(message);
    }

    @Override
    public void playersTargetTakenDownPrompt(String homeBeaconName) {
        disableCloseConsole();
        playersTargetGotTakenDownConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    private void playersTargetGotTakenDownConsoleMessage(String homeBeaconName) {
        String message = "Too slow; your target has been taken down.\n\nReturn to $BEACON to receive your new target";
        message = message.replace("$BEACON", homeBeaconName);
        consoleMessage(message);
    }

    @Override
    public void playerGotTakenDownPrompt(String homeBeaconName) {
        disableCloseConsole();
        playerTakenDownConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    private void playerTakenDownConsoleMessage(String homeBeaconName) {
        String message = "You have been taken down.\n\nReturn to $BEACON.";
        message = message.replace("$BEACON", homeBeaconName);
        consoleMessage(message);
    }

    @Override
    public void endOfGamePrompt(final Context context, final Intent goToLeaderboardIntent) {
        disableCloseConsole();
        consoleMessage(context.getString(R.string.game_over_console_message));
        this.interactionInProgress = false;
        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(goToLeaderboardIntent);
            }
        });
    }

    @Override
    public void executingTakedownPrompt() {
        disableCloseConsole();
        consoleMessage("TAKEDOWN_INIT\n\nExecuting attack...");
        this.interactionInProgress = false;
    }

    @Override
    public void takedownSuccessPrompt(String homeBeaconName) {
        disableCloseConsole();
        String message = "TAKEDOWN_SUCCESS\n\nReturn to $HOME for new target.";
        message = message.replace("$HOME", homeBeaconName);
        this.interactionInProgress = false;
        consoleMessage(message);
    }

    private void consoleMessage(String message) {
        typewriter.animateText(consoleViewText, message);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void takedownNotYourTargetPrompt() {
        enableCloseConsole();
        this.interactionInProgress = false;
        consoleMessage("TAKEDOWN_FAILURE\n\nNot your target");
    }

    @Override
    public void takedownInsufficientIntelPrompt() {
        enableCloseConsole();

        this.interactionInProgress = false;
        consoleMessage("TAKEDOWN_FAILURE\n\nInsufficient Intel");
    }

    @Override
    public void closeConsole() {
        if (overlay.getVisibility() != View.GONE && !interactionInProgress) {
            overlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void exchangeRequestedPrompt() {
        disableCloseConsole();
        this.interactionInProgress = true;
        consoleMessage("EXCHANGE_REQUESTED\n\nWaiting for handshake");
    }

    @Override
    public void exchangeSuccessPrompt() {
        enableCloseConsole();
        this.interactionInProgress = false;
        consoleMessage("EXCHANGE_SUCCESS\n\nIntel gained");
    }

    @Override
    public void exchangeFailedPrompt() {
        enableCloseConsole();
        this.interactionInProgress = false;
        consoleMessage("EXCHANGE_FAIL\n\nHandshake incomplete");
    }
}
