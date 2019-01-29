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

    private static final int POLLING_PERIOD = 10;               // given in seconds
    private static final double GAMEPLAY_DURATION = 2;          // given in minutes.
    private static final int EXCHANGE_POLLING_DURATION = 20;    // given in seconds;
    private static final int EXCHANGE_POLLING_PERIOD = 3;       // given in seconds.

    private PlayerIdentifiers playerIdentifiers;

    private IPlayerListView playerListView;
    private IPlayerStatusBarView playerStatusBarView;
    private IConsoleView consoleView;

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

        initializePlayerListView();
        initializePlayerStatusBarView();
        initializeExchangeButton();
        initializeTakeDownButton();

        initializeGameStateController();
        initializeServerRequestController();
        initializeConsoleView();
        initializeBeaconController();

        gameStateController.setOnNearestBeaconBeingHomeBeaconListener(new Runnable() {
            @Override
            public void run() {
                consoleView.closeConsole();
            }
        });

        startGameTimer();

        // First task: player needs to head to their home beacon.
        consoleView.goToStartBeaconPrompt(gameStateController.getHomeBeaconName());

        try {
            beaconController.startScanning();
            serverRequestsController.startInfoRequest();

            // polling
            Timer timer = new Timer(false);
            timer.scheduleAtFixedRate(pollServer(),0, POLLING_PERIOD * 1000);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        beaconController.stopScanning();
    }

    private void initializeGameStateController() {
        this.gameStateController = new GameStateController(playerListView, playerStatusBarView,
                playerIdentifiers, getIntent().getStringExtra("start_beacon_minor"),
                getIntent().getStringExtra("start_beacon_name"));
    }

    private void initializeServerRequestController() {
        this.serverRequestsController = new GameplayServerRequestsController(this, gameStateController);
        serverRequestsController.registerTakedownSuccessRunnable(takedownSuccessfulRunnable());
    }

    private void initializeBeaconController() {
        this.beaconController = new BeaconController(this, gameStateController);
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
                        consoleView.playerGotTakenDownPrompt(gameStateController.getHomeBeaconName());
                        gameStateController.loseHalfOfPlayersIntel();
                        gameStateController.resetPlayerTakenDown();
                        serverRequestsController.newTargetRequest();
                    }
                    if (gameStateController.playersTargetHasBeenTakenDown()) {
                        consoleView.playersTargetTakenDownPrompt(gameStateController.getHomeBeaconName());
                        gameStateController.resetPlayersTargetHasBeenTakenDown();
                        serverRequestsController.newTargetRequest();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializePlayerStatusBarView() {
        this.playerStatusBarView = new PlayerStatusBarView(findViewById(R.id.gameplay_player_status_bar));
    }

    private void initializeExchangeButton() {
        Button exchangeButton = findViewById(R.id.gameplay_exchange_button);
        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInteractionButtons();
                showExchangeSelectPlayerButton();
                playerListView.beginExchange();
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
                playerListView.beginTakedown();
            }
        });
    }

    private StringInputRunnable beginSelectedTakedownOnClickRunner() {
        return new StringInputRunnable() {
            @Override
            public void run(String targetId) {
                if (!gameStateController.playerHasFullIntel(targetId)) {
                    consoleView.takedownInsufficientIntelPrompt();
                }
                else if (!gameStateController.getTargetPlayerId().equals(targetId)) {
                    consoleView.takedownNotYourTargetPrompt();
                }
                else {
                    consoleView.executingTakedownPrompt();
                    try {
                        serverRequestsController.takeDownRequest(targetId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                showInteractionButtons();
                hideTakedownSelectPlayerButton();
                playerListView.resumeGameplayAfterInteraction();
            }
        };
    }

    private StringInputRunnable beginSelectedExchangeOnClickRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(String interacteeId) {
                consoleView.exchangeRequestedPrompt();
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
                    consoleView.exchangeFailedPrompt();
                    finishExchange();
                    cancel();
                }
                else {
                    try {
                        serverRequestsController.exchangeRequest(interacteeId, details);

                        if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                            for (String id : details.gainedIntelPlayerIds) {
                                gameStateController.increasePlayerIntel(id);
                            }
                            consoleView.exchangeSuccessPrompt();
                            finishExchange();
                            cancel();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, EXCHANGE_POLLING_PERIOD * 1000);
    }

    private void finishExchange() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showInteractionButtons();
                hideExchangeSelectPlayerButton();
                playerListView.resumeGameplayAfterInteraction();
            }
        });
    }

    private Runnable takedownSuccessfulRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                String homeBeaconName = gameStateController.getHomeBeaconName();
                consoleView.takedownSuccessPrompt(homeBeaconName);
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

    private void initializeConsoleView() {
        final View overlay = findViewById(R.id.gameplay_console_overlay);
        this.consoleView = new ConsoleView(overlay);
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
                consoleView.endOfGamePrompt(thisContext, intent);
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

    private void initializePlayerListView() {
        this.playerListView = new PlayerListView(LayoutInflater.from(this),
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
