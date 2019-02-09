package com.bristol.hackerhunt.helloworld.gameplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;
import com.bristol.hackerhunt.helloworld.gameplay.controller.BeaconController;
import com.bristol.hackerhunt.helloworld.gameplay.controller.GameStateController;
import com.bristol.hackerhunt.helloworld.gameplay.controller.GameplayServerRequestsController;
import com.bristol.hackerhunt.helloworld.gameplay.controller.IBeaconController;
import com.bristol.hackerhunt.helloworld.gameplay.controller.IGameStateController;
import com.bristol.hackerhunt.helloworld.gameplay.controller.IGameplayServerRequestsController;
import com.bristol.hackerhunt.helloworld.gameplay.view.ConsoleView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IConsoleView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IInteractionButtonsView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IPlayerListView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IPlayerStatusBarView;
import com.bristol.hackerhunt.helloworld.gameplay.view.InteractionButtonsView;
import com.bristol.hackerhunt.helloworld.gameplay.view.PlayerListView;
import com.bristol.hackerhunt.helloworld.gameplay.view.PlayerStatusBarView;
import com.bristol.hackerhunt.helloworld.leaderboard.LeaderboardActivity;
import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.model.InteractionStatus;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class GameplayActivity extends AppCompatActivity {

    private static final int POLLING_PERIOD = 1;                // given in seconds
    private static final double GAMEPLAY_DURATION = 5;          // given in minutes.
    private static final int EXCHANGE_POLLING_PERIOD = 1;       // given in seconds.
    private static final int CONSOLE_POPUP_DELAY_PERIOD = 3;    // given in seconds.

    private PlayerIdentifiers playerIdentifiers;

    private IPlayerListView playerListView;
    private IPlayerStatusBarView playerStatusBarView;
    private IConsoleView consoleView;
    private IInteractionButtonsView interactionButtonsView;

    private IGameplayServerRequestsController serverRequestsController;
    private IGameStateController gameStateController;
    private IBeaconController beaconController;

    private boolean gameOver = false;
    private boolean closeConsoleOnHomeBeaconNearby = false;
    private boolean newTargetRequested = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);

        getPlayerIdentifiers();

        initializePlayerListView();
        initializePlayerStatusBarView();
        initializeInteractionButtonsView();

        initializeGameStateController();
        initializeServerRequestController();
        initializeConsoleView();
        initializeBeaconController();

        gameStateController.setOnNearestBeaconBeingHomeBeaconListener(new Runnable() {
            @Override
            public void run() {
                if (!gameOver && closeConsoleOnHomeBeaconNearby) {
                    Log.d("App", "Closing console, home beacon nearby");
                    consoleView.enableTapToClose();
                    closeConsoleOnHomeBeaconNearby = false;
                }
            }
        });

        startGameTimer();

        // First task: player needs to head to their home beacon.
        closeConsoleOnHomeBeaconNearby = true;
        consoleView.goToStartBeaconPrompt(gameStateController.getHomeBeaconName());

        try {
            beaconController.startScanning();
            serverRequestsController.startInfoRequest();

            // polling
            Timer timer = new Timer(false);
            pollServer().run();
            timer.scheduleAtFixedRate(pollServer(),0, POLLING_PERIOD * 1000);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void closeConsoleAfterDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                consoleView.closeConsole();
                if (newTargetRequested) {
                    try {
                        serverRequestsController.newTargetRequest();
                        newTargetRequested = false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, CONSOLE_POPUP_DELAY_PERIOD * 1000);
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }

    @Override
    protected void onStop() {
        super.onStop();
        beaconController.stopScanning();
    }

    private void initializeGameStateController() {
        this.gameStateController = new GameStateController(playerListView, playerStatusBarView,
                playerIdentifiers, getIntent().getStringExtra("start_beacon_major"),
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
                else {
                    pollServerTask();
                }
            }
        };
    }

    private void pollServerTask() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (gameStateController.gameHasEnded()) {
                        gameOver();
                    }
                    else {
                        serverRequestsController.playerUpdateRequest();
                        serverRequestsController.isAtHomeBeaconRequest();

                        if (gameStateController.playerHasBeenTakenDown()) {
                            closeConsoleOnHomeBeaconNearby = true;
                            consoleView.playerGotTakenDownPrompt(gameStateController.getHomeBeaconName());
                            gameStateController.loseHalfOfPlayersIntel();
                            gameStateController.resetPlayerTakenDown();
                        }
                        if (gameStateController.playersTargetHasBeenTakenDown()) {
                            newTargetRequested = true;
                            closeConsoleOnHomeBeaconNearby = true;
                            consoleView.playersTargetTakenDownPrompt(gameStateController.getHomeBeaconName());
                            gameStateController.resetPlayersTargetHasBeenTakenDown();
                        }
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

    private void initializeInteractionButtonsView() {
        this.interactionButtonsView = new InteractionButtonsView(this, exchangeButtonOnClickRunnable(),
                takedownButtonOnClickRunnable(), resumeGameAfterInteractionRunnable(),
                resumeGameAfterInteractionRunnable());
    }

    private Runnable resumeGameAfterInteractionRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                playerListView.resumeGameplayAfterInteraction();
            }
        };
    }

    private Runnable takedownButtonOnClickRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                playerListView.beginTakedown();
            }
        };
    }

    private Runnable exchangeButtonOnClickRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                playerListView.beginExchange();
            }
        };
    }

    private StringInputRunnable beginSelectedTakedownOnClickRunner() {
        return new StringInputRunnable() {
            @Override
            public void run(String targetId) {
                if (!gameStateController.playerHasFullIntel(targetId)) {
                    consoleView.takedownInsufficientIntelPrompt();
                }
                else if (gameStateController.getTargetPlayerId() == null ||
                !gameStateController.getTargetPlayerId().equals(targetId)) {
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
                interactionButtonsView.showInteractionButtons();
                interactionButtonsView.hideTakedownSelectPlayerButton();
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
        final Activity that = this;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            final long t0 = System.currentTimeMillis();
            final InteractionDetails details = new InteractionDetails();

            @Override
            public void run() {
                if (details.status.equals(InteractionStatus.FAILED)) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            consoleView.exchangeFailedPrompt();
                            finishExchange();
                        }
                    });
                    cancel();
                }
                else {
                    try {
                        if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                            cancel();
                            for (String id : details.gainedIntelPlayerIds) {
                                gameStateController.increasePlayerIntel(id);
                            }

                            that.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    consoleView.exchangeSuccessPrompt();
                                    finishExchange();
                                }
                            });
                        }
                        else if (details.status.equals(InteractionStatus.IN_PROGRESS)) {
                            serverRequestsController.exchangeRequest(interacteeId, details);
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
                interactionButtonsView.showInteractionButtons();
                interactionButtonsView.hideExchangeSelectPlayerButton();
                playerListView.resumeGameplayAfterInteraction();
            }
        });
    }

    private Runnable takedownSuccessfulRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                String homeBeaconName = gameStateController.getHomeBeaconName();
                closeConsoleOnHomeBeaconNearby = true;
                consoleView.takedownSuccessPrompt(homeBeaconName);
                newTargetRequested = true;
            }
        };
    }

    private void initializeConsoleView() {
        final View overlay = findViewById(R.id.gameplay_console_overlay);
        this.consoleView = new ConsoleView(overlay);
    }

    private void startGameTimer() {
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
                gameStateController.setGameOver();
            }
        }.start();
    }

    private void gameOver() {
        gameOver = true;
        Intent intent = new Intent(GameplayActivity.this, LeaderboardActivity.class);
        intent.putExtra(getString(R.string.player_identifiers_intent_key), playerIdentifiers);

        Bundle bundle = new Bundle();
        bundle.putSerializable(String.valueOf(R.string.all_players_map_intent_key), gameStateController.getPlayerIdRealNameMap() );
        intent.putExtras(bundle);
        consoleView.endOfGamePrompt(this, intent);
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
