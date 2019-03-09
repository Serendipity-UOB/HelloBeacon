package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.Typewriter;

public class ConsoleView implements IConsoleView {

    private static final int TYPEWRITER_SPEED = 10;     // given in milliseconds.

    private final View overlay;
    private final View consoleView;
    private final View consoleWrapper;
    private final TextView consoleViewTitle;
    private final TextView consoleViewText;
    private final TextView consoleTapToCloseMessage;

    private final Typewriter typewriter;

    // status flags
    private String currentHomeBeacon = "";
    private boolean interactionInProgress;
    private boolean playerGotTakenDownInProgress;
    private boolean playersTargetGotTakenDownInProgress;
    private boolean takedownSuccessInProgress;

    public ConsoleView(View consolePromptContainer) {
        this.overlay = consolePromptContainer;
        this.consoleView = overlay.findViewById(R.id.gameplay_console);
        this.consoleViewTitle = overlay.findViewById(R.id.full_pop_up_title);
        this.consoleViewText = overlay.findViewById(R.id.gameplay_console_text);
        this.consoleTapToCloseMessage = overlay.findViewById(R.id.console_close_message);
        this.consoleWrapper = overlay.findViewById(R.id.full_pop_up_wrapper);

        this.typewriter = new Typewriter(TYPEWRITER_SPEED);

        this.interactionInProgress = false;
        this.playerGotTakenDownInProgress = false;
        this.playersTargetGotTakenDownInProgress = false;
        this.takedownSuccessInProgress = false;

        enableCloseConsole();
    }

    private void resetConsoleInProgressFlags() {
        playerGotTakenDownInProgress = false;
        playersTargetGotTakenDownInProgress = false;
        takedownSuccessInProgress = false;
    }

    private void enableCloseConsole() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
                resetConsoleInProgressFlags();
            }
        };

        setCloseOnClickListener(listener);
        consoleTapToCloseMessage.setVisibility(View.VISIBLE);
    }

    private void enableCloseConsoleWithoutOverride() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
                if (playerGotTakenDownInProgress) {
                    playerGotTakenDownPrompt(currentHomeBeacon);
                }
                else if (playersTargetGotTakenDownInProgress) {
                    playersTargetTakenDownPrompt(currentHomeBeacon);
                }
                else if (takedownSuccessInProgress) {
                    takedownSuccessPrompt(currentHomeBeacon);
                }
                else {
                    overlay.setVisibility(View.GONE);
                }
            }
        };

        setCloseOnClickListener(listener);
        consoleTapToCloseMessage.setVisibility(View.VISIBLE);
    }

    void setCloseOnClickListener(View.OnClickListener listener) {
        overlay.setOnClickListener(listener);
        consoleView.setOnClickListener(listener);
        consoleViewText.setOnClickListener(listener);
    }

    private void disableCloseConsole() {
        setCloseOnClickListener(null);
        consoleTapToCloseMessage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void goToStartBeaconPrompt(String homeBeaconName) {
        // disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        setNeutralConsole();

        setConsoleTitle(R.string.console_start_title);
        goToStartBeaconConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    private void setNeutralConsole() {
        consoleViewTitle.setTextColor(consoleView.getResources()
                .getColor(R.color.neutral_full_screen_notif_title));
        consoleWrapper.setBackgroundResource(R.drawable.neutral_full_pop_up);
    }

    private void setGoodConsole() {
        consoleViewTitle.setTextColor(consoleView.getResources()
                .getColor(R.color.good_full_screen_notif_title));
        consoleWrapper.setBackgroundResource(R.drawable.good_full_pop_up);
    }

    private void setBadConsole() {
        consoleViewTitle.setTextColor(consoleView.getResources()
                .getColor(R.color.bad_full_screen_notif_title));
        consoleWrapper.setBackgroundResource(R.drawable.bad_full_pop_up);
    }

    private void goToStartBeaconConsoleMessage(String homeBeaconName) {
        String message = overlay.getContext().getString(R.string.console_start_beacon_message);
        message = message.replace("$BEACON", homeBeaconName);
        setConsoleMessage(message);
    }

    @Override
    public void playersTargetTakenDownPrompt(String homeBeaconName) {
        disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        this.playersTargetGotTakenDownInProgress = true;
        setNeutralConsole();
        setConsoleTitle(R.string.console_target_taken_down_title);
        playersTargetGotTakenDownConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    private void setConsoleTitle(int id) {
        String title = consoleView.getResources().getString(id);
        consoleViewTitle.setText(title);
    }

    private void playersTargetGotTakenDownConsoleMessage(String homeBeaconName) {
        String message = consoleView.getResources()
                .getString(R.string.console_target_taken_down_message);
        message = message.replace("$BEACON", homeBeaconName);
        setConsoleMessage(message);
    }

    @Override
    public void playerGotTakenDownPrompt(String homeBeaconName) {
        disableCloseConsole();

        this.playerGotTakenDownInProgress = true;
        this.currentHomeBeacon = homeBeaconName;
        setBadConsole();
        setConsoleTitle(R.string.console_taken_down_title);
        playerTakenDownConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    private void playerTakenDownConsoleMessage(String homeBeaconName) {
        String message = consoleView.getResources()
                .getString(R.string.console_taken_down_message);
        message = message.replace("$BEACON", homeBeaconName);
        setConsoleMessage(message);
    }

    @Override
    public void endOfGamePrompt(final Context context, final Intent goToLeaderboardIntent) {
        disableCloseConsole();

        setNeutralConsole();
        setConsoleMessage(context.getString(R.string.game_over_console_message));
        setConsoleTitle(R.string.game_over_console_title);

        this.interactionInProgress = false;

        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("App", "Going to leaderboard.");
                context.startActivity(goToLeaderboardIntent);
            }
        };
        consoleViewText.setOnClickListener(cl);
        consoleView.setOnClickListener(cl);
    }

    @Override
    public void missionUpdatePrompt(String beaconName, String missionStatement) {
        // TODO: make this update close when the beacon is within range.
        enableCloseConsole(); // for testing
        setNeutralConsole();
        setConsoleMessage(missionStatement);
        setConsoleTitle(R.string.mission_update_title);
    }

    @Override
    public void missionSuccessPrompt(String missionSuccessMessage) {
        enableCloseConsole();
        setGoodConsole();
        setConsoleMessage(missionSuccessMessage);
        setConsoleTitle(R.string.mission_success_title);
    }

    @Override
    public void missionFailedPrompt(String missionFailedMessage) {
        enableCloseConsole();
        setBadConsole();
        setConsoleMessage(missionFailedMessage);
        setConsoleTitle(R.string.mission_failed_title);
    }

    @Override
    public void executingTakedownPrompt() {
        disableCloseConsole();
        setConsoleMessage("TAKEDOWN_INIT\n\nExecuting attack...");
        this.interactionInProgress = false;
    }

    @Override
    public void takedownSuccessPrompt(String homeBeaconName) {
        disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        this.takedownSuccessInProgress = true;

        setGoodConsole();
        setConsoleTitle(R.string.console_expose_success_title);
        String message = consoleView.getResources()
                .getString(R.string.console_expose_success_message);
        message = message.replace("$HOME", homeBeaconName);
        setConsoleMessage(message);

        this.interactionInProgress = false;
    }

    private void setConsoleMessage(String message) {
        consoleViewText.setText(message);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void takedownNotYourTargetPrompt() {
        enableCloseConsoleWithoutOverride();

        this.interactionInProgress = false;
        setConsoleMessage("TAKEDOWN_FAILURE\n\nNot your target");
    }

    @Override
    public void takedownInsufficientIntelPrompt() {
        enableCloseConsoleWithoutOverride();

        this.interactionInProgress = false;
        setConsoleMessage("TAKEDOWN_FAILURE\n\nInsufficient Intel");
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
        setConsoleMessage("EXCHANGE_REQUESTED\n\nWaiting for handshake");
    }

    @Override
    public void exchangeSuccessPrompt() {
        enableCloseConsoleWithoutOverride();

        this.interactionInProgress = false;
        setConsoleMessage("EXCHANGE_SUCCESS\n\nIntel gained");
    }

    @Override
    public void exchangeFailedPrompt() {
        enableCloseConsoleWithoutOverride();

        this.interactionInProgress = false;
        setConsoleMessage("EXCHANGE_FAIL\n\nHandshake incomplete");
    }

    @Override
    public void enableTapToClose()  {
        enableCloseConsole();
    }
}
