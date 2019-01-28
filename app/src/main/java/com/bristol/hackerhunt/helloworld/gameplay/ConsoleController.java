package com.bristol.hackerhunt.helloworld.gameplay;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.INfcController;
import com.bristol.hackerhunt.helloworld.NfcController;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.Typewriter;
import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.model.InteractionStatus;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class ConsoleController implements IConsoleController {

    private static final int EXCHANGE_POLLING_DURATION = 20; // given in seconds;
    private static final int POLLING_PERIOD = 3; // given in seconds.

    private final View overlay;
    private final TextView consoleView;

    private final IGameStateController gameStateController;
    private final IGameplayServerRequestsController serverRequestsController;
    private final INfcController nfcController;

    private final Typewriter typewriter;

    ConsoleController(View consolePromptContainer, IGameStateController gameStateController,
                      IGameplayServerRequestsController serverRequestsController) {
        this.overlay = consolePromptContainer;
        this.gameStateController = gameStateController;
        this.serverRequestsController = serverRequestsController;
        this.nfcController = new NfcController();
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
    public void goToStartBeaconPrompt() {
        // disableCloseConsole();
        goToStartBeaconConsoleMessage();
    }

    private void goToStartBeaconConsoleMessage() {
        String message = overlay.getContext().getString(R.string.console_start_beacon_message);
        message = message.replace("$BEACON", gameStateController.getHomeBeaconName());
        typewriter.animateText(consoleView, message);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void mutualExchangePrompt() {
        enableCloseConsole();
        scanTargetNfcTagConsoleMessage();

        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: scan NFC.
                final String targetId = "0"; //nfcController.readNfcTag();

                consoleView.setText(R.string.NFC_scan_successful_exchange);
                consoleView.setOnClickListener(null);
                disableCloseConsole();

                beginExchangeServerPolling(consoleView, targetId);
            }
        });
    }

    private void beginExchangeServerPolling(final TextView consoleView, final String playerId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            final long t0 = System.currentTimeMillis();
            final InteractionDetails details = new InteractionDetails();

            @Override
            public void run() {
                if (System.currentTimeMillis() - t0 > EXCHANGE_POLLING_DURATION * 1000) {
                    consoleView.setText(R.string.exchange_failed_message);
                    enableCloseConsole();
                    cancel();
                }
                else {
                    try {
                        serverRequestsController.exchangeRequest(playerId, details);

                        if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                            consoleView.setText(R.string.exchange_success_message);
                            for (String id : details.gainedIntelPlayerIds) {
                                gameStateController.increasePlayerIntel(id);
                            }
                            enableCloseConsole();
                            cancel();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, POLLING_PERIOD * 1000);
    }

    @Override
    public void targetTakedownPrompt() {
        enableCloseConsole();


        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: Scan NFC tag.
                String targetId = "0";// nfcController.readNfcTag();

                if (!gameStateController.playerHasFullIntel(targetId)) {
                    consoleView.setText(R.string.not_enough_intel_take_down_message);
                }
                else if (!targetId.equals(gameStateController.getTargetPlayerId())) {
                    consoleView.setText(R.string.player_isnt_target_takedown_message);
                }
                else {
                    try {
                        serverRequestsController.takeDownRequest(targetId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    disableCloseConsole();
                    takedownSuccessConsoleMessage();

                    // TODO: wait for player to go to correct beacon.
                }
            }
        });
    }

    private void scanTargetNfcTagConsoleMessage() {
        typewriter.animateText(consoleView, consoleView.getContext().getString(R.string.scan_target_nfc_message));
        overlay.setVisibility(View.VISIBLE);
    }

    private void takedownSuccessConsoleMessage() {
        String message = "TAKEDOWN_SUCCESS\n\n\nReturn to $BEACON for new target.";
        message = message.replace("$BEACON", gameStateController.getHomeBeaconName());
        typewriter.animateText(consoleView, message);
    }

    @Override
    public void playersTargetTakenDownPrompt() {
        disableCloseConsole();
        playersTargetGotTakenDownConsoleMessage();

        // TODO: Wait for player to go to beacon.
    }

    private void playersTargetGotTakenDownConsoleMessage() {
        String message = "Too slow; your target has been taken down.\n\nReturn to $BEACON to receive your new target";
        message = message.replace("$BEACON", gameStateController.getHomeBeaconName());
        typewriter.animateText(consoleView, message);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void playerGotTakenDownPrompt() {
        disableCloseConsole();
        gameStateController.loseHalfOfPlayersIntel();
        playerTakenDownConsoleMessage();

        // TODO: Wait for player to go to beacon.
    }

    private void playerTakenDownConsoleMessage() {
        String message = "You have been taken down.\n\nReturn to $BEACON.";
        message = message.replace("$BEACON", gameStateController.getHomeBeaconName());
        typewriter.animateText(consoleView, message);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void endOfGamePrompt(final Context context, final Intent goToLeaderboardIntent) {
        disableCloseConsole();
        consoleMessage("Incoming message...\n\nGood work. Return your equipment to the base " +
                "station to collect your award.\n\n\n - Anon");

        // TODO: return to base station.

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
