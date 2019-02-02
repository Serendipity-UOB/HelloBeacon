package com.bristol.hackerhunt.helloworld.joinGame;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.TitleScreenActivity;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.gameplay.GameplayActivity;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class JoinGameActivity extends AppCompatActivity {

    private static int POLLING_PERIOD = 3; // in seconds

    private IJoinGameServerRequestController serverRequestController;

    private GameInfo gameInfo;
    private boolean joinedGame = false;
    private boolean timerStarted = false;
    private PlayerIdentifiers playerIdentifiers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        this.gameInfo = new GameInfo();
        this.serverRequestController = new JoinGameServerRequestController(this, gameInfo);
        this.playerIdentifiers = getIntent().getParcelableExtra("player_identifiers");

        replaceStringInTextView(R.id.join_game_welcome_text, "$PLAYER_NAME", playerIdentifiers.getRealName());
        updateNumberOfPlayersInGame("Loading...");
        updateTimeLeftUntilGame("Loading...");

        initializeJoinGameButton();


        Timer timer = new Timer(true);
        TimerTask task = pollServer(playerIdentifiers, timer);
        task.run();
        timer.scheduleAtFixedRate(pollServer(playerIdentifiers, timer), 0, POLLING_PERIOD * 1000);
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }
    private TimerTask pollServer(final PlayerIdentifiers playerIdentifiers, final Timer timer) {
        final Activity that = this;

        return new TimerTask() {
            @Override
            public void run() {
                try {
                    serverRequestController.gameInfoRequest();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Starting timer thread
                if (gameInfo.minutesToStart != null && gameInfo.minutesToStart >= 0 && !timerStarted) {
                    startCountdownToGameStart(playerIdentifiers, timer);
                }
                if (gameInfo.numberOfPlayers != null && gameInfo.numberOfPlayers >= 0 ) {
                    updateNumberOfPlayersInGame(gameInfo.numberOfPlayers.toString());
                }
                if (gameInfo.minutesToStart != null && gameInfo.minutesToStart < 0) {
                    updateTimeLeftUntilGame("--:--");
                    updateNumberOfPlayersInGame("--");

                    that.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    final Button joinGameButton = findViewById(R.id.join_game_button);
                                    joinGameButton.setVisibility(View.GONE);
                                    TextView joinStatus = findViewById(R.id.join_game_success);
                                    joinStatus.setVisibility(View.VISIBLE);
                                    joinStatus.setText("NO_GAME_AVAILABLE");
                                }
                            }
                    );
                    cancel();
                }
            }
        };
    }

    private void startCountdownToGameStart(final PlayerIdentifiers playerIdentifiers, final Timer timer) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startGameTimer(playerIdentifiers, timer, (long) (gameInfo.minutesToStart * 60 * 1000)).start();
            }
        });
        timerStarted = true;
    }

    private CountDownTimer startGameTimer(final PlayerIdentifiers playerIdentifiers, final Timer timer,
                                          long millisecondsToGameStart) {
        return new CountDownTimer(millisecondsToGameStart, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                String formattedTime = formatTime(millisUntilFinished);
                updateTimeLeftUntilGame(formattedTime);
            }

            @Override
            public void onFinish() {
                if (joinedGame) {
                    serverRequestController.cancelAllRequests();
                    goToGameplayActivity(playerIdentifiers);
                }
                else {
                    goToTitleScreenActivity();
                }
            }
        };
    }

    private void initializeJoinGameButton() {
        final Button joinGameButton = findViewById(R.id.join_game_button);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGameButton.setVisibility(View.GONE);
                try {
                    serverRequestController.joinGameRequest(playerIdentifiers.getPlayerId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                TextView joinStatus = findViewById(R.id.join_game_success);
                findViewById(R.id.join_game_success).setVisibility(View.VISIBLE);

                (new Timer()).schedule(checkHomeBeaconHasBeenRecieved(joinStatus), 1000);
            }
        });
    }

    private TimerTask checkHomeBeaconHasBeenRecieved(final TextView joinStatus) {
        final Activity that = this;
        return new TimerTask() {
            @Override
            public void run() {
                if (gameInfo.startBeaconMinor != null) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            joinStatus.setText(R.string.join_game_success);
                            joinedGame = true;
                        }
                    });
                    cancel();
                }
            }
        };
    }

    private String formatTime(long milliseconds) {
        Long minutes = milliseconds / 60000;
        Long seconds = (milliseconds - minutes * 60000) / 1000;
        String formattedTime = minutes.toString() + ":" ;
        if (minutes < 10) formattedTime = "0" + formattedTime;
        if (seconds < 10) formattedTime = formattedTime + "0";
        return formattedTime + seconds.toString();
    }

    private void goToGameplayActivity(PlayerIdentifiers playerIdentifiers) {
        Intent intent = new Intent(JoinGameActivity.this, GameplayActivity.class);
        intent.putExtra("player_identifiers", playerIdentifiers);
        intent.putExtra("start_beacon_minor", gameInfo.startBeaconMinor);
        intent.putExtra("start_beacon_name", gameInfo.startBeaconName);
        startActivity(intent);
    }

    private void goToTitleScreenActivity() {
        Intent intent = new Intent(JoinGameActivity.this, TitleScreenActivity.class);
        startActivity(intent);
    }

    private void updateNumberOfPlayersInGame(String players) {
        appendStringToTextView(R.id.join_game_players_in_game, getResources().getString(R.string.join_game_players_in_game), players);
    }

    private void updateTimeLeftUntilGame(String time) {
        appendStringToTextView(R.id.join_game_time_until_game, getResources().getString(R.string.join_game_time_until_game), time);
    }

    private void replaceStringInTextView(final int viewId, final String oldString, final String newString) {
        this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = findViewById(viewId);
                        String oldText = textView.getText().toString();
                        String newText = oldText.replace(oldString, newString);
                        textView.setText(newText);
                    }
                }
        );
    }

    private void appendStringToTextView(final int viewId, final String text, final String suffix) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(viewId);
                textView.setText(text + " " + suffix);
            }
        });
    }
}
