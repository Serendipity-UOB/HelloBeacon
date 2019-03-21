package com.bristol.hackerhunt.helloworld.gameplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
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
import com.bristol.hackerhunt.helloworld.gameplay.view.ExchangeRequestView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IConsoleView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IExchangeRequestView;
import com.bristol.hackerhunt.helloworld.gameplay.view.INotificationView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IPlayerListView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IPlayerStatusBarView;
import com.bristol.hackerhunt.helloworld.gameplay.view.NotificationView;
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
    private static final int EXCHANGE_POLLING_PERIOD = 1;       // given in seconds.
    private static final int CONSOLE_POPUP_DELAY_PERIOD = 3;    // given in seconds.
    private static final int MISSION_POLLING_PERIOD = 1;        // given in seconds.
    private static final int ACCEPT = 1;
    private static final int REJECT = 2;

    private PlayerIdentifiers playerIdentifiers;

    private IPlayerListView playerListView;
    private IPlayerStatusBarView playerStatusBarView;
    private IConsoleView consoleView;
    private INotificationView notificationView;
    private IExchangeRequestView exchangeRequestView;

    private IGameplayServerRequestsController serverRequestsController;
    private IGameStateController gameStateController;
    private IBeaconController beaconController;

    private boolean timerStarted = false;
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
        initializeNotificationView();
        initializeExchangeRequestView();

        initializeGameStateController();
        initializeServerRequestController();
        initializeConsoleView();
        initializeBeaconController();

        gameStateController.setOnNearestBeaconBeingHomeBeaconListener(new Runnable() {
            @Override
            public void run() {
                if (!gameOver && closeConsoleOnHomeBeaconNearby) {
                    Log.d("App", "Closing console, home beacon nearby");
                    //consoleView.enableTapToClose();
                    closeConsoleAfterDelay();
                    closeConsoleOnHomeBeaconNearby = false;
                }
            }
        });

        initializeStatusBarPlayerName();

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
                consoleView.enableTapToClose();
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
        this.gameStateController = new GameStateController(playerListView,
                playerStatusBarView,
                consoleView,
                playerIdentifiers,
                getIntent().getStringExtra("start_beacon_name"));
    }

    private void initializeServerRequestController() {
        this.serverRequestsController = new GameplayServerRequestsController(this, gameStateController);
        serverRequestsController.registerExposeSuccessRunnable(exposeSuccessfulRunnable());
        serverRequestsController.registerExposeFailedRunnable(exposeFailedRunnable());
        serverRequestsController.registerMissionUpdateRunnable(handleNewMissionRunnable());
    }

    private Runnable exposeSuccessfulRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                String homeBeaconName = gameStateController.getHomeBeaconName();
                closeConsoleOnHomeBeaconNearby = true;
                consoleView.exposeSuccessPrompt(homeBeaconName);
                newTargetRequested = true;
            }
        };
    }

    // This should never be called because the client should pick up on invalid requests, but if it
    // does it indicates either a network error or a client error.
    private Runnable exposeFailedRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                notificationView.exposeFailedNetworkError();
            }
        };
    }

    private void initializeBeaconController() {
        this.beaconController = new BeaconController(this, gameStateController);
    }

    private void initializeNotificationView() {
        this.notificationView = new NotificationView(findViewById(R.id.gameplay_notification_overlay));
    }

    private void initializeExchangeRequestView() {
        this.exchangeRequestView = new ExchangeRequestView(findViewById(R.id.exchange_request_overlay),
                onAcceptExchangeRequestRunnable(),
                onRejectExchangeRequestRunnable());
    }

    private StringInputRunnable onAcceptExchangeRequestRunnable() {
        return new StringInputRunnable() {

            @Override
            public void run(final String playerId) {
                //final InteractionDetails details = new InteractionDetails();


                //serverRequestsController.exchangeResponse(playerId, ACCEPT, details);
                beginExchangeResponseServerPolling(playerId);
                exchangeRequestView.hideDialogueBox();

            }
        };
    }

    private StringInputRunnable handleNewMissionRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(final String missionDetails) {
                Log.d("New Mission", missionDetails);
                consoleView.missionUpdatePrompt(missionDetails);
                beginMissionUpdateServerPolling();
            }
        };
    }

    private void beginMissionUpdateServerPolling() {
        final Activity that = this;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            final InteractionDetails details = new InteractionDetails();

            @Override
            public void run() {
                try {
                    serverRequestsController.missionUpdateRequest(details);

                    if (details.status == InteractionStatus.FAILED){
                        cancel();
                        that.runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                consoleView.missionFailedPrompt("Mission Failed");
                                //TODO Right message
                            }
                        });
                    }
                    else if(details.status == InteractionStatus.SUCCESSFUL){
                        cancel();
                        that.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                consoleView.missionSuccessPrompt("Mission Success");
                                //TODO Right message
                            }
                        });
                    }
                    else if(details.status == InteractionStatus.IN_PROGRESS){
                        that.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int timeRemaining = details.missionTime;
                                //Tell the UI something
                            }
                        });
                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },0, MISSION_POLLING_PERIOD * 1000);
    }

    private void beginExchangeResponseServerPolling(final String playerId) {
        final Activity that = this;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            final InteractionDetails details = new InteractionDetails();
            @Override
            public void run() {
                try {
                    serverRequestsController.exchangeResponse(playerId, ACCEPT, details);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (details.status.equals(InteractionStatus.FAILED)) {
                    that.runOnUiThread(new Runnable() {
                            @Override
                            public void run() { notificationView.exchangeFailedTimedOut(playerId); }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                    final boolean secondaryExists = details.gainedIntelPlayerIds.size() > 1;

                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playerListView.exchangeRequestComplete(playerId);

                            if (secondaryExists) {
                                notificationView.exchangeSuccessful(getPlayerName(playerId),
                                        getPlayerName(details.gainedIntelPlayerIds.get(1)));
                            }

                            else {
                                notificationView.exchangeSuccessful(getPlayerName(playerId),"");
                            }
                        }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.IN_PROGRESS)) {
                    //TODO Do something maybe??
                    //Haven't set this for exchange response
                }

            }
        },1000, EXCHANGE_POLLING_PERIOD * 1000);
    }

    private StringInputRunnable onRejectExchangeRequestRunnable() {
        final Activity that = this;
        return new StringInputRunnable() {
            @Override
            public void run(final String playerId) {
                final InteractionDetails details = new InteractionDetails();
                try {
                    serverRequestsController.exchangeResponse(playerId, REJECT, details);
                    if (details.status.equals(InteractionStatus.REJECTED)) {
                        Log.d("Reject Exchange", "Successfully rejected");
                    }
                    else {
                        Log.d("Reject Exchange", "Something went wrong");
                        //Some error in server stuff
                        //Maybe just let it time out??
                        //Better than trying again
                    }
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            exchangeRequestView.hideDialogueBox();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
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
                        if (!timerStarted && gameStateController.getGameDuration() > 0) {
                            startGameTimer(gameStateController.getGameDuration());
                            timerStarted = true;
                        }

                        if (timerStarted) {
                            serverRequestsController.playerUpdateRequest();
                            serverRequestsController.isAtHomeBeaconRequest();

                            if (gameStateController.exchangeHasBeenRequested()) {
                                String id = gameStateController.getExchangeRequesterId();
                                exchangeRequestView.showDialogueBox(getPlayerName(id), id);
                                gameStateController.completeExchangeRequest();
                            }

                            if (gameStateController.playerHasBeenTakenDown()) {
                                closeConsoleOnHomeBeaconNearby = true;
                                String exposerId = gameStateController.getExposerId();
                                String exposerName = getPlayerName(exposerId);
                                gameStateController.resetExposerId();

                                //TODO Full screen notification call for exposure

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

    private StringInputRunnable beginSelectedInterceptOnClickRunnable(){
        return new StringInputRunnable() {
            @Override
            public void run(final String interacteeId) {
                notificationView.attemptingToIntercept(getPlayerName(interacteeId));
                beginInterceptServerPolling(interacteeId);
                restoreScreenOnPlayerCardPress();
            }
        };
    }

    private void beginInterceptServerPolling(final String interacteeId) {
        final Activity that = this;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            final InteractionDetails details = new InteractionDetails();

            @Override
            public void run() {
                try {
                    if (details.status == InteractionStatus.IN_PROGRESS) {
                        Log.d("Intecept", "Polling");
                        serverRequestsController.interceptRequest(interacteeId, details);
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
                if (details.status.equals(InteractionStatus.FAILED)) {
                    Log.d("Intercept", "Failed");
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notificationView.interceptFailedNoExchange(getPlayerName(interacteeId));
                        }
                    });
                    playerListView.interceptAttemptComplete();
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                    Log.d("Intercept", "Successful");
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notificationView.interceptSucceeded(getPlayerName(interacteeId),
                                    getPlayerName(details.gainedIntelPlayerIds.get(1)));
                        }
                    });
                    playerListView.interceptAttemptComplete();
                    cancel();
                }
            }
        }, 0, EXCHANGE_POLLING_PERIOD * 1000);
    }

    private StringInputRunnable beginExposeOnClickRunner() {
        return new StringInputRunnable() {
            @Override
            public void run(String targetId) {
                if (!gameStateController.playerHasFullIntel(targetId)) {
                    String targetRealName = gameStateController.getPlayerIdRealNameMap().get(targetId);
                    notificationView.exposeFailedInsufficientEvidence(targetRealName);
                }
                else if (gameStateController.getTargetPlayerId() == null ||
                        !gameStateController.getTargetPlayerId().equals(targetId)) {
                    String targetRealName = gameStateController.getPlayerIdRealNameMap().get(targetId);
                    notificationView.exposeFailedNotYourTarget(targetRealName);
                }
                else {
                    try {
                        serverRequestsController.exposeRequest(targetId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // navigate away from the interaction buttons:
                restoreScreenOnPlayerCardPress();
            }
        };
    }

    private StringInputRunnable beginSelectedExchangeOnClickRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(String interacteeId) {
                notificationView.exchangeRequested(getPlayerName(interacteeId));
                beginExchangeServerPolling(interacteeId);
                restoreScreenOnPlayerCardPress();
            }
        };
    }

    private void beginExchangeServerPolling(final String interacteeId) {
        final Activity that = this;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            final InteractionDetails details = new InteractionDetails();

            @Override
            public void run() {
                try {

                    if (details.status == InteractionStatus.IN_PROGRESS) {
                        serverRequestsController.exchangeRequest(interacteeId, details);
                        details.status = InteractionStatus.RESPONSE_PENDING;
                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }
                if (details.status.equals(InteractionStatus.FAILED)) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notificationView.exchangeFailedTimedOut(getPlayerName(interacteeId));
                            playerListView.exchangeRequestComplete(interacteeId);
                        }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.REJECTED)) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notificationView.exchangeFailedRejection(getPlayerName(interacteeId));
                            playerListView.exchangeRequestComplete(interacteeId);
                        }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                    final boolean secondaryExists = details.gainedIntelPlayerIds.size() > 1;

                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playerListView.exchangeRequestComplete(interacteeId);

                            if (secondaryExists) {
                                notificationView.exchangeSuccessful(getPlayerName(interacteeId),
                                        getPlayerName(details.gainedIntelPlayerIds.get(1)));
                            }

                            else {
                                notificationView.exchangeSuccessful(getPlayerName(interacteeId),"");
                            }
                        }
                    });
                    cancel();
                }
            }
        }, 0, EXCHANGE_POLLING_PERIOD * 1000);
    }

    private void initializeConsoleView() {
        final View overlay = findViewById(R.id.gameplay_console_overlay);
        this.consoleView = new ConsoleView(overlay);
    }

    private void startGameTimer(long durationInMinutes) {
        long duration = (durationInMinutes * 60 * 1000);

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


    //TODO Bug here or in game over test
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
                beginExposeOnClickRunner(),
                beginSelectedExchangeOnClickRunnable(),
                beginSelectedInterceptOnClickRunnable(),
                darkenScreenOnPlayerCardPressRunnable(),
                restoreScreenOnPlayerCardPressRunnable());
    }

    private void goToLeaderboardActivity() {
        Intent intent = new Intent(GameplayActivity.this, LeaderboardActivity.class);
        intent.putExtra(getString(R.string.player_identifiers_intent_key), playerIdentifiers);
        startActivity(intent);
    }

    private void getPlayerIdentifiers() {
        this.playerIdentifiers = getIntent().getParcelableExtra(getString(R.string.player_identifiers_intent_key));
    }

    private void initializeStatusBarPlayerName(){
        this.playerStatusBarView.setPlayerName(this.playerIdentifiers.getRealName());
    }

    // a runnable used to darken the screen after a player card has been selected, excluding the one that was pressed.
    private StringInputRunnable darkenScreenOnPlayerCardPressRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(final String exemptPlayerId) {
                playerListView.darken(exemptPlayerId);
                playerStatusBarView.darken();

                View background = findViewById(R.id.gameplay_background);
                background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                                R.color.gameplay_background_darkened));
                background.setOnClickListener(cancelInteractionButtonsOnClickListener(exemptPlayerId));
                findViewById(R.id.gameplay_player_list).setOnClickListener(cancelInteractionButtonsOnClickListener(exemptPlayerId));
            }
        };
    }

    private void restoreScreenOnPlayerCardPress() {
        playerListView.restore();
        playerStatusBarView.restore();

        View background = findViewById(R.id.gameplay_background);
        background.setBackgroundResource(R.drawable.tile_background);
        background.setOnClickListener(null);
        findViewById(R.id.gameplay_player_list).setOnClickListener(null);
    }

    // a runnable used to cancel/"tap-out" after the interaction buttons have appeared.
    private StringInputRunnable restoreScreenOnPlayerCardPressRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(String exemptPlayerId) {
                restoreScreenOnPlayerCardPress();
            }
        };
    }

    // an on-click listener used to restore the screen after the interaction buttons have appeared, to cancel.
    private View.OnClickListener cancelInteractionButtonsOnClickListener(final String exemptPlayerId) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Click", "Background pressed.");
                restoreScreenOnPlayerCardPressRunnable().run(exemptPlayerId);
            }
        };
    }

    private String getPlayerName(String playerId) {
        return gameStateController.getPlayerIdRealNameMap().get(playerId);
    }
}
