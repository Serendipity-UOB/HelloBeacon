package com.bristol.hackerhunt.helloworld.gameplay;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.Typewriter;

public class ConsoleView implements IConsoleView {

    private final View overlay;
    private final TextView consoleView;

    private final Typewriter typewriter;

    ConsoleView(View consolePromptContainer) {
        this.overlay = consolePromptContainer;
        this.consoleView = overlay.findViewById(R.id.gameplay_console);

        this.typewriter = new Typewriter(10);

        enableCloseConsole();
    }

    private void enableCloseConsole() {
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
            }
        });
    }

    private void disableCloseConsole() {
        overlay.setOnClickListener(null);
    }

    @Override
    public void goToStartBeaconPrompt(String homeBeaconName) {
        // disableCloseConsole();
        goToStartBeaconConsoleMessage(homeBeaconName);
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
    }

    private void playerTakenDownConsoleMessage(String homeBeaconName) {
        String message = "You have been taken down.\n\nReturn to $BEACON.";
        message = message.replace("$BEACON", homeBeaconName);
        consoleMessage(message);
    }

    @Override
    public void endOfGamePrompt(final Context context, final Intent goToLeaderboardIntent) {
        disableCloseConsole();
        consoleMessage("Incoming message...\n\nGood work. Return your equipment to the base " +
                "station to collect your award.\n\n\n - Anon");

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
    }

    @Override
    public void takedownSuccessPrompt(String homeBeaconName) {
        disableCloseConsole();
        String message = "TAKEDOWN_SUCCESS\n\nReturn to $HOME for new target.";
        message = message.replace("$HOME", homeBeaconName);
        consoleMessage(message);
    }

    private void consoleMessage(String message) {
        typewriter.animateText(consoleView, message);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void takedownNotYourTargetPrompt() {
        enableCloseConsole();
        consoleMessage("TAKEDOWN_FAILURE\n\nNot your target");
    }

    @Override
    public void takedownInsufficientIntelPrompt() {
        enableCloseConsole();
        consoleMessage("TAKEDOWN_FAILURE\n\nInsufficient Intel");
    }

    @Override
    public void closeConsole() {
        if (overlay.getVisibility() != View.GONE) {
            overlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void exchangeRequestedPrompt() {
        disableCloseConsole();
        consoleMessage("EXCHANGE_REQUESTED\n\nWaiting for handshake");
    }

    @Override
    public void exchangeSuccessPrompt() {
        enableCloseConsole();
        consoleMessage("EXCHANGE_SUCCESS\n\nIntel gained");
    }

    @Override
    public void exchangeFailedPrompt() {
        enableCloseConsole();
        consoleMessage("EXCHANGE_FAIL\n\nHandshake incomplete");
    }
}
