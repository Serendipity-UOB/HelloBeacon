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

import com.bristol.hackerhunt.helloworld.leaderboard.LeaderboardActivity;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class GameplayActivity extends AppCompatActivity {

    private static final int POLLING_PERIOD = 10; // in seconds
    private static final double GAMEPLAY_DURATION = 0.5; // given in minutes.

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
        initializeConsoleController();
        this.beaconController = new BeaconController(this, gameStateController);

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
                consoleController.mutualExchangePrompt();
            }
        });
    }

    private void initializeTakeDownButton() {
        Button takeDownButton = findViewById(R.id.gameplay_takedown_button);
        takeDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consoleController.targetTakedownPrompt();
            }
        });
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
                (LinearLayout) findViewById(R.id.gameplay_player_list));
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
