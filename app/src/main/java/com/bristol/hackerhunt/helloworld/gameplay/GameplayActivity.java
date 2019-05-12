package com.bristol.hackerhunt.helloworld.gameplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
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
    private static final int POLLING_SCALAR_MS = 1000;          // given in milliseconds
    private static final int EXCHANGE_POLLING_PERIOD = 1;       // given in seconds.
    private static final int CONSOLE_POPUP_DELAY_PERIOD = 1;    // given in seconds.
    private static final int MISSION_POLLING_PERIOD = 1;        // given in seconds.

    private static final int WAIT = 0;
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
    private boolean notFirstConsole = false;
    private boolean gameOver = false;
    private boolean closeConsoleOnHomeBeaconNearby = false;
    private boolean newTargetRequested = true;
    private int currentPlayerExchangeResponse = WAIT;

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
                Log.d("Home Beacon", "Nearby");
                Log.d("Close Console", Boolean.toString(closeConsoleOnHomeBeaconNearby));
                if (!gameOver && closeConsoleOnHomeBeaconNearby) {
                    Log.d("Home Beacon", "Closing console, home beacon nearby");
                    if(notFirstConsole){
                        Log.d("Not First Console", "True");
                        consoleView.enableTapToClose();
                        closeConsoleAfterDelay();
                    }
                    else{
                        notFirstConsole = true;
                    }
                    closeConsoleOnHomeBeaconNearby = true;
                }
            }
        });


        initializeStatusBarPlayerName();

        closeConsoleOnHomeBeaconNearby = true;

        // First task: player needs to head to their home beacon.

        try {
            beaconController.startScanning();
            serverRequestsController.startInfoRequest();

            // polling
            Timer timer = new Timer(false);
            pollServer().run();
            timer.scheduleAtFixedRate(pollServer(),0, POLLING_PERIOD * POLLING_SCALAR_MS);

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

    public StringInputRunnable newTargetConsole(){
        return new StringInputRunnable() {
            @Override
            public void run(String targetId) {
                consoleView.newTargetPrompt(gameStateController.getTargetName(targetId));
            }
        };
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        serverRequestsController.registerMissionSuccessRunnable(handleSuccessfulMissionRunnable());
        serverRequestsController.registerMissionFailureRunnable(handleFailedMissionRunnable());
        serverRequestsController.registerChangePlayerLocationRunnable(this.playerStatusBarView.changePlayerLocationRunnable());
        serverRequestsController.registerChangeLocationRunnable(this.playerListView.changeLocationRunnable());
        serverRequestsController.registerEnableInteractionsRunnable(enableInteractionsRunnable());
        serverRequestsController.registerDisableInteractionsRunnable(disableInteractionsRunnable());
        serverRequestsController.registerNewTargetConsoleRunnable(newTargetConsole());
    }

    private StringInputRunnable exposeSuccessfulRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(String targetId) {
                String homeBeaconName = gameStateController.getHomeBeaconName();
                closeConsoleOnHomeBeaconNearby = true;
                consoleView.exposeSuccessPrompt(homeBeaconName);
                notificationVibrate();
                Log.d("Clear Evidence", targetId);
                gameStateController.clearAllEvidence(targetId);
                newTargetRequested = true;
            }
        };
    }


    private void notificationVibrate() {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 400 milliseconds
        v.vibrate(500);
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

    private Runnable disableInteractionsRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                playerListView.disableAllButtons();
            }
        };
    }

    private Runnable enableInteractionsRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                playerListView.enableAllButtons();
            }
        };
    }

    private StringInputRunnable onAcceptExchangeRequestRunnable() {
        return new StringInputRunnable() {

            @Override
            public void run(final String playerId) {
                //final InteractionDetails details = new InteractionDetails();


                //serverRequestsController.exchangeResponse(playerId, ACCEPT, details);
                //beginExchangeResponseServerPolling(playerId);
                currentPlayerExchangeResponse = ACCEPT;
                exchangeRequestView.hideDialogueBox();

            }
        };
    }

    private StringInputRunnable handleNewMissionRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(final String missionDetails) {
                Log.d("New Mission", missionDetails);
                closeConsoleOnHomeBeaconNearby = false;
                consoleView.missionUpdatePrompt(missionDetails);
                consoleView.setConsoleImage(consoleView.getConsoleFlag(getFlagFromMission(missionDetails)));
                notificationVibrate();
                if(missionDetails.toLowerCase().contains("was last seen in")){
                    consoleView.enableTapToClose();
                }
                else {
                    beginMissionUpdateServerPolling();
                }

            }
        };
    }

    private int getFlagFromMission(String details){
        //Default to UN Flag
        int flag = 0;
        String lowerDetails = details.toLowerCase();
        if(lowerDetails.contains("italy")){
            flag = 1;
        }
        else if(lowerDetails.contains("sweden")){
            flag = 2;
        }
        else if(lowerDetails.contains("switzerland")){
            flag = 3;
        }
        else if(lowerDetails.contains("czech republic")){
            flag = 4;
        }
        return flag;
    }

    private StringInputRunnable handleSuccessfulMissionRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(String input) {
                Log.d("Mission Success",input);
                consoleView.missionSuccessPrompt(input);
                notificationVibrate();

            }
        };
    }

    private StringInputRunnable handleFailedMissionRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(String input) {
                Log.d("Mission Failure",input);
                consoleView.missionFailedPrompt(input);
                notificationVibrate();

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
                    if (details.status.equals(InteractionStatus.IN_PROGRESS)) {
                        serverRequestsController.missionUpdateRequest(details);
                        details.status = InteractionStatus.RESPONSE_PENDING;
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Mission Poll", "Polling");

                if (details.status.equals(InteractionStatus.ERROR)) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            consoleView.applicationError();
                        }
                    });
                }

                if (!details.status.equals(InteractionStatus.RESPONSE_PENDING) && !details.status.equals(InteractionStatus.IN_PROGRESS)){
                    cancel();
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
                    if (details.status.equals(InteractionStatus.IN_PROGRESS)) {

                        String playerResponse = "Wait";
                        if (currentPlayerExchangeResponse == REJECT) {
                            playerResponse = "Reject";
                        }
                        if (currentPlayerExchangeResponse == ACCEPT) {
                            playerResponse = "Accept";
                        }
                        Log.d("Exchange Response", "Action: " + playerResponse);

                        serverRequestsController.exchangeResponse(playerId, currentPlayerExchangeResponse, details);
                        details.status = InteractionStatus.RESPONSE_PENDING;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (details.status.equals(InteractionStatus.FAILED)) {
                    currentPlayerExchangeResponse = WAIT;
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.SUCCESSFUL)) {

                    if (currentPlayerExchangeResponse == ACCEPT) {
                        //final boolean secondaryExists = details.gainedIntelPlayerIds.size() > 1;

                        that.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playerListView.exchangeRequestComplete(playerId, true);
                                /* NOTIFICATIONS
                                if (secondaryExists) {
                                    notificationView.exchangeSuccessful(getPlayerName(playerId),
                                            getPlayerName(details.gainedIntelPlayerIds.get(1)));
                                } else {
                                    notificationView.exchangeSuccessful(getPlayerName(playerId));
                                }
                                */
                            }
                        });
                    }

                    currentPlayerExchangeResponse = WAIT;
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.ERROR)) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notificationView.applicationError();
                            playerListView.exchangeRequestComplete(playerId, false);
                            currentPlayerExchangeResponse = WAIT;
                            cancel();
                        }
                    });
                }
            }
        },0, EXCHANGE_POLLING_PERIOD * 1000);
    }

    private StringInputRunnable onRejectExchangeRequestRunnable() {
        final Activity that = this;
        return new StringInputRunnable() {
            @Override
            public void run(final String playerId) {
                currentPlayerExchangeResponse = REJECT;
                that.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        exchangeRequestView.hideDialogueBox();
                    }
                });

            }
        };
    }

    private TimerTask pollServer() {
        return new TimerTask() {
            @Override
            public void run() {
                if (gameOver) {
                    beaconController.stopScanning();
                    serverRequestsController.cancelAllRequests();
                    cancel();
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
                                currentPlayerExchangeResponse = WAIT;
                                beginExchangeResponseServerPolling(id);
                            }

                            if (gameStateController.playerHasBeenTakenDown()) {
                                closeConsoleOnHomeBeaconNearby = true;
                                String exposerId = gameStateController.getExposerId();
                                String exposerName = getPlayerName(exposerId);
                                gameStateController.resetExposerId();

                                consoleView.playerGotTakenDownPrompt(gameStateController.getHomeBeaconName());
                                notificationVibrate();
                                gameStateController.loseHalfOfPlayersIntel();
                                gameStateController.resetPlayerTakenDown();
                            }
                            /*
                            if (gameStateController.playersTargetHasBeenTakenDown()) {
                                newTargetRequested = true;
                                closeConsoleOnHomeBeaconNearby = true;
                                consoleView.playersTargetTakenDownPrompt(gameStateController.getHomeBeaconName());
                                notificationVibrate();

                                gameStateController.resetPlayersTargetHasBeenTakenDown();
                            }
                            */
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
                //notificationView.attemptingToIntercept(getPlayerName(interacteeId));
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
                    if (details.status.equals(InteractionStatus.IN_PROGRESS)) {
                        Log.d("Intercept", "Polling");
                        serverRequestsController.interceptRequest(interacteeId, details);
                        details.status = InteractionStatus.RESPONSE_PENDING;
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
                //Specific Cases
                if (details.status.equals(InteractionStatus.FAILED)) {
                    Log.d("Intercept", "Failed");
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //notificationView.interceptFailedNoExchange(getPlayerName(interacteeId));
                            playerListView.interceptAttemptComplete(interacteeId, false);
                        }
                    });
                    cancel();
                }
                else if(details.status.equals(InteractionStatus.REJECTED)) {
                    Log.d("Intercept","Rejected");
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //notificationView.interceptFailedNoEvidenceShared();
                            playerListView.interceptAttemptComplete(interacteeId, false);
                        }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                    Log.d("Intercept", "Successful");
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //notificationView.interceptSucceeded(getPlayerName(interacteeId),
                            //        getPlayerName(details.gainedIntelPlayerIds.get(1)));
                                    playerListView.interceptAttemptComplete(interacteeId, true);
                        }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.ERROR)) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notificationView.applicationError();
                            playerListView.interceptAttemptComplete(interacteeId, false);
                        }
                    });
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
                //notificationView.exchangeRequested(getPlayerName(interacteeId));
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
                    if (details.status.equals(InteractionStatus.IN_PROGRESS)) {
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
                            //notificationView.exchangeFailedTimedOut(getPlayerName(interacteeId));
                            playerListView.exchangeRequestComplete(interacteeId, false);
                        }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.REJECTED)) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //notificationView.exchangeFailedRejection(getPlayerName(interacteeId));
                            playerListView.exchangeRequestComplete(interacteeId, false);
                        }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.SUCCESSFUL)) {
                    //final boolean secondaryExists = details.gainedIntelPlayerIds.size() > 1;

                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playerListView.exchangeRequestComplete(interacteeId, true);
                            /* NOTIFICATIONS
                            if (secondaryExists) {
                                notificationView.exchangeSuccessful(getPlayerName(interacteeId),
                                        getPlayerName(details.gainedIntelPlayerIds.get(1)));
                            }

                            else {
                                notificationView.exchangeSuccessful(getPlayerName(interacteeId));
                            }*/
                        }
                    });
                    cancel();
                }
                else if (details.status.equals(InteractionStatus.ERROR)) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notificationView.applicationError();
                            playerListView.exchangeRequestComplete(interacteeId, false);
                            cancel();
                        }
                    });
                }
            }
        }, 0, EXCHANGE_POLLING_PERIOD * 1000);
    }

    private void initializeConsoleView() {
        final View overlay = findViewById(R.id.gameplay_console_overlay);
        final Activity that = this;
        this.consoleView = new ConsoleView(overlay, that);
    }

    private void startGameTimer(long durationInMinutes) {
        long duration = (durationInMinutes * 60 * 1000) + 60000;

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
