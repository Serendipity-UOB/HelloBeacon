package com.bristol.hackerhunt.helloworld.gameplay;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.model.InteractionStatus;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class ConsoleController implements IConsoleController {

    private static final int EXCHANGE_POLLING_DURATION = 20; // given in seconds;
    private static final int POLLING_PERIOD = 3; // given in seconds.

    private final View overlay;

    private final IGameStateController gameStateController;
    private final IGameplayServerRequestsController serverRequestsController;

    ConsoleController(View consolePromptContainer, IGameStateController gameStateController,
                      IGameplayServerRequestsController serverRequestsController) {
        this.overlay = consolePromptContainer;
        this.gameStateController = gameStateController;
        this.serverRequestsController = serverRequestsController;

        // this is only temporary.
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void goToStartBeaconPrompt() {
        TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        String message = overlay.getContext().getString(R.string.console_start_beacon_message);
        message = message.replace("$BEACON", gameStateController.getHomeBeacon());
        consoleView.setText(message);
        overlay.setVisibility(View.VISIBLE);

        // TODO: wait until player reaches beacon.
    }

    @Override
    public void mutualExchangePrompt() {
        final TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        final String[] message = {"Scan target NFC tag."};
        consoleView.setText(message[0]);

        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: scan NFC.
                final String targetId = "4";

                consoleView.setText("NFC scan successful. Exchange in progress.");
                consoleView.setOnClickListener(null);

                beginExchangeServerPolling(consoleView, targetId);
            }
        });

        overlay.setVisibility(View.VISIBLE);
    }

    private void beginExchangeServerPolling(final TextView consoleView, final String playerId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            final long t0 = System.currentTimeMillis();
            final InteractionDetails details = new InteractionDetails();

            @Override
            public void run() {
                if (System.currentTimeMillis() - t0 > EXCHANGE_POLLING_DURATION * 1000) {
                    consoleView.setText("EXCHANGE_FAILED");
                    cancel();
                }
                else {
                    try {
                        serverRequestsController.exchangeRequest(playerId, details);

                        if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                            consoleView.setText("EXCHANGE_SUCCESS");
                            for (String id : details.gainedIntelPlayerIds) {
                                gameStateController.increasePlayerIntel(id);
                            }
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
        final TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        consoleView.setText("Scan target NFC tag.");

        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: NFC has been scanned.
                String targetId = "5";

                if (!gameStateController.playerHasFullIntel(targetId)) {
                    consoleView.setText("Error: not enough intel has been collected.");
                }
                else if (!targetId.equals(gameStateController.getTargetPlayerId())) {
                    consoleView.setText("Error: player is not the target.");
                }
                else {
                    try {
                        serverRequestsController.takeDownRequest(targetId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String message = "TAKEDOWN_SUCCESS\n\n\nReturn to $BEACON for new target.";
                    message = message.replace("$BEACON", gameStateController.getHomeBeacon());
                    consoleView.setText(message);

                    // TODO: wait for player to go to correct beacon.

                    try {
                        serverRequestsController.newTargetRequest();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void playersTargetTakenDownPrompt() {
        final TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        String message = "Too slow; your target has been taken down.\n\nReturn to $BEACON to receive your new target";
        message = message.replace("$BEACON", gameStateController.getHomeBeacon());
        consoleView.setText(message);
        overlay.setVisibility(View.VISIBLE);

        // TODO: Wait for player to go to beacon.

        consoleView.setText("Receiving new target...");
        try {
            serverRequestsController.newTargetRequest();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playerGotTakenDownPrompt() {
        gameStateController.loseHalfOfPlayersIntel();

        final TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        String message = "You have been taken down.\n\nReturn to $BEACON.";
        message = message.replace("$BEACON", gameStateController.getHomeBeacon());
        consoleView.setText(message);
        overlay.setVisibility(View.VISIBLE);

        // TODO: Wait for player to go to beacon.
    }

    @Override
    public void endOfGamePrompt(final Context context, final Intent goToLeaderboardIntent) {
        final TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        final String[] message = {"Incoming message...\n\nGood work. Return your equipment to the base station to collect your award.\n\n\n - Anon"};
        consoleView.setText(message[0]);

        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(goToLeaderboardIntent);
            }
        });

        overlay.setVisibility(View.VISIBLE);
    }
}
