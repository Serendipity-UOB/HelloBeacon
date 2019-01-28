package com.bristol.hackerhunt.helloworld.gameplay;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.StringInputRunnable;
import com.bristol.hackerhunt.helloworld.leaderboard.LeaderboardActivity;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.model.InteractionStatus;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class GameplayActivity extends AppCompatActivity {

    private static final int POLLING_PERIOD = 10; // in seconds
    private static final double GAMEPLAY_DURATION = 0.5; // given in minutes.
    private static final int EXCHANGE_POLLING_DURATION = 20; // given in seconds;
    private static final int EXCHANGE_POLLING_PERIOD = 3; // given in seconds.

    private PlayerIdentifiers playerIdentifiers;

    private IPlayerListController playerListController;
    private IPlayerStatusBarController playerStatusBarController;
    private IConsoleController consoleController;
    private IGameplayServerRequestsController serverRequestsController;
    private IGameStateController gameStateController;
    private IBeaconController beaconController;

    private boolean gameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);
        getPlayerIdentifiers();

        initializePlayerListController();
        initializePlayerStatusBarController();
        initializeExchangeButton();
        initializeTakeDownButton();

        this.gameStateController = new GameStateController(playerListController, playerStatusBarController,
                playerIdentifiers, getIntent().getStringExtra("start_beacon_minor"),
                getIntent().getStringExtra("start_beacon_name"));

        this.serverRequestsController = new GameplayServerRequestsController(this, gameStateController);
        serverRequestsController.registerTakedownSuccessRunnable(takedownSuccessfulRunnable());

        initializeConsoleController();
        this.beaconController = new BeaconController(this, gameStateController);

        gameStateController.setOnNearestBeaconBeingHomeBeaconListener(new Runnable() {
            @Override
            public void run() {
                consoleController.closeConsole();
            }
        });

        startGameTimer();

        // First task: player needs to head to their home beacon.
        consoleController.goToStartBeaconPrompt();

        try {
            beaconController.startScanning();
            serverRequestsController.startInfoRequest();
            // serverRequestsController.newTargetRequest();

            // polling
            Timer timer = new Timer(false);
            timer.scheduleAtFixedRate(pollServer(),0, POLLING_PERIOD * 1000);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private TimerTask pollServer() {
        return new TimerTask() {
            @Override
            public void run() {
                if (gameOver) {
                    cancel();
                    beaconController.stopScanning();
                    serverRequestsController.cancelAllRequests();
                }

                pollServerTask();
            }
        };
    }

    private void pollServerTask() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverRequestsController.playerUpdateRequest();

                    if (gameStateController.playerHasBeenTakenDown()) {
                        consoleController.playerGotTakenDownPrompt();
                        gameStateController.resetPlayerTakenDown();
                        serverRequestsController.newTargetRequest();
                    }
                    if (gameStateController.playersTargetHasBeenTakenDown()) {
                        consoleController.playersTargetTakenDownPrompt();
                        gameStateController.resetPlayersTargetHasBeenTakenDown();
                        serverRequestsController.newTargetRequest();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializePlayerStatusBarController() {
        this.playerStatusBarController = new PlayerStatusBarController(findViewById(R.id.gameplay_player_status_bar));
    }

    private void initializeExchangeButton() {
        Button exchangeButton = findViewById(R.id.gameplay_exchange_button);
        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInteractionButtons();
                showExchangeSelectPlayerButton();
                playerListController.beginExchange();
            }
        });
    }

    private void initializeTakeDownButton() {
        Button takeDownButton = findViewById(R.id.gameplay_takedown_button);
        takeDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInteractionButtons();
                showTakedownSelectPlayerButton();
                playerListController.beginTakedown();
            }
        });
    }

    private StringInputRunnable beginSelectedTakedownOnClickRunner() {
        return new StringInputRunnable() {
            @Override
            public void run(String targetId) {
                if (!gameStateController.playerHasFullIntel(targetId)) {
                    consoleController.takedownInsufficientIntelPrompt();
                }
                else if (!gameStateController.getTargetPlayerId().equals(targetId)) {
                    consoleController.takedownNotYourTargetPrompt();
                }
                else {
                    consoleController.executingTakedownPrompt();
                    try {
                        serverRequestsController.takeDownRequest(targetId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                showInteractionButtons();
                hideTakedownSelectPlayerButton();
                playerListController.resumeGameplay();
            }
        };
    }

    private StringInputRunnable beginSelectedExchangeOnClickRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(String interacteeId) {
                consoleController.exchangeRequestedPrompt();
                beginExchangeServerPolling(interacteeId);
            }
        };
    }

    private void beginExchangeServerPolling(final String interacteeId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            final long t0 = System.currentTimeMillis();
            final InteractionDetails details = new InteractionDetails();

            @Override
            public void run() {
                if (System.currentTimeMillis() - t0 > EXCHANGE_POLLING_DURATION * 1000) {
                    consoleController.exchangeFailedPrompt();
                    showInteractionButtons();
                    hideExchangeSelectPlayerButton();
                    playerListController.resumeGameplay();
                    cancel();
                }
                else {
                    try {
                        serverRequestsController.exchangeRequest(interacteeId, details);

                        if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                            for (String id : details.gainedIntelPlayerIds) {
                                gameStateController.increasePlayerIntel(id);
                            }
                            consoleController.exchangeSuccessPrompt();
                            showInteractionButtons();
                            hideExchangeSelectPlayerButton();
                            playerListController.resumeGameplay();
                            cancel();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, EXCHANGE_POLLING_PERIOD * 1000);
    }

    private Runnable takedownSuccessfulRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                String homeBeaconName = gameStateController.getHomeBeaconName();
                consoleController.takedownSuccessPrompt(homeBeaconName);
            }
        };
    }

    // TODO: put into buttonsView class
    private void hideInteractionButtons() {
        LinearLayout buttons = findViewById(R.id.interaction_buttons);
        buttons.setVisibility(View.GONE);
    }

    private void showInteractionButtons() {
        LinearLayout buttons = findViewById(R.id.interaction_buttons);
        buttons.setVisibility(View.VISIBLE);
    }

    private void hideTakedownSelectPlayerButton() {
        Button takedownSelectPlayerButton = findViewById(R.id.takedown_exchange_select_player_button);
        takedownSelectPlayerButton.setVisibility(View.GONE);
    }

    private void showTakedownSelectPlayerButton() {
        Button takedownSelectPlayerButton = findViewById(R.id.takedown_exchange_select_player_button);
        takedownSelectPlayerButton.setVisibility(View.VISIBLE);
    }

    private void hideExchangeSelectPlayerButton() {
        Button takedownSelectPlayerButton = findViewById(R.id.gameplay_exchange_select_player_button);
        takedownSelectPlayerButton.setVisibility(View.GONE);
    }

    private void showExchangeSelectPlayerButton() {
        Button takedownSelectPlayerButton = findViewById(R.id.gameplay_exchange_select_player_button);
        takedownSelectPlayerButton.setVisibility(View.VISIBLE);
    }

    private void initializeConsoleController() {
        final View overlay = findViewById(R.id.gameplay_console_overlay);
        this.consoleController = new ConsoleController(overlay, gameStateController, serverRequestsController);
    }

    private void startGameTimer() {
        final Context thisContext = this;
        long duration = (long) (GAMEPLAY_DURATION * 60 * 1000);

        // Starting timer thread
        new CountDownTimer(duration, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                String formattedTime = formatTime(millisUntilFinished);
                updateTimeLeftUntilGameOver(formattedTime);
            }

            @Override
            public void onFinish() {
                gameOver = true;
                Intent intent = new Intent(GameplayActivity.this, LeaderboardActivity.class);
                intent.putExtra(getString(R.string.player_identifiers_intent_key), playerIdentifiers);

                Bundle bundle = new Bundle();
                bundle.putSerializable(String.valueOf(R.string.all_players_map_intent_key), gameStateController.getPlayerIdRealNameMap() );
                intent.putExtras(bundle);
                consoleController.endOfGamePrompt(thisContext, intent);
            }
        }.start();
    }

    private void updateTimeLeftUntilGameOver(String formattedTime) {
        TextView timeLeftView = findViewById(R.id.gameplay_time_left);
        timeLeftView.setText(formattedTime);
    }

    // returns time in the form 00:00.
    private String formatTime(long milliseconds) {
        Long minutes = milliseconds / 60000;
        Long seconds = (milliseconds - minutes * 60000) / 1000;
        String formattedTime = minutes.toString() + ":" ;
        if (minutes < 10) formattedTime = "0" + formattedTime;
        if (seconds < 10) formattedTime = formattedTime + "0";
        return formattedTime + seconds.toString();
    }

    private void initializePlayerListController() {
        this.playerListController = new PlayerListController(LayoutInflater.from(this),
                (LinearLayout) findViewById(R.id.gameplay_player_list),
                beginSelectedTakedownOnClickRunner(), beginSelectedExchangeOnClickRunnable());
    }

    private void goToLeaderboardActivity() {
        Intent intent = new Intent(GameplayActivity.this, LeaderboardActivity.class);
        intent.putExtra(getString(R.string.player_identifiers_intent_key), playerIdentifiers);
        startActivity(intent);
    }

    private void getPlayerIdentifiers() {
        this.playerIdentifiers = getIntent().getParcelableExtra(getString(R.string.player_identifiers_intent_key));
    }
}
