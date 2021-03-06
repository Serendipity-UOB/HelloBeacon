package com.bristol.hackerhunt.helloworld.joinGame;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    private static int POLLING_PERIOD = 1; // in seconds

    private IJoinGameServerRequestController serverRequestController;

    private GameInfo gameInfo;
    private boolean joinedGame = false;
    private boolean joinPressed = false;
    private boolean timerStarted = false;
    private boolean gameStarted = false;
    private PlayerIdentifiers playerIdentifiers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        this.gameInfo = new GameInfo();
        this.serverRequestController = new JoinGameServerRequestController(this, gameInfo);
        this.playerIdentifiers = getIntent().getParcelableExtra("player_identifiers");

        replaceStringInTextView(R.id.join_game_welcome_text, "$PLAYER_NAME", playerIdentifiers.getRealName());
        updateNumberOfPlayersInGame("--");
        updateTimeLeftUntilGame("--:--");

        initializeJoinGameButton();

        Timer timer = new Timer(true);
        TimerTask task = pollServer(playerIdentifiers, timer);
        task.run();
        timer.scheduleAtFixedRate(pollServer(playerIdentifiers, timer), 0, POLLING_PERIOD * 1000);

        //timer.schedule(pressJoinGameButton(),2);
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
                    if (!gameStarted) {
                        Log.i("Game Info", "Requested");
                        serverRequestController.gameInfoRequest();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Starting timer thread
                if (gameInfo.countdownStatus == CountdownStatus.ACTIVE) {
                    if (gameInfo.gameStarted) {
                        gameStarted = true;
                        serverRequestController.cancelAllRequests();
                        goToGameplayActivity(playerIdentifiers);
                        this.cancel();
                    }
                    else {
                        updateNumberOfPlayersInGame(gameInfo.numberOfPlayers.toString());
                        updateTimeLeftUntilGame(gameInfo.visibleTimeLeft);

                        if (!gameInfo.visibleTimeLeft.equals("--:--")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView text = findViewById(R.id.join_game_success);
                                    text.setVisibility(View.VISIBLE);
                                    text.setText(R.string.join_game_waiting_for_start);
                                }
                            });
                        }

                        if (!joinPressed) {
                            showJoinGameButton();
                            pressJoinGameButton();
                        }
                    }
                }
                if (gameInfo.countdownStatus == CountdownStatus.NO_GAME && !timerStarted) {
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
                                    joinStatus.setText(R.string.join_game_no_game_available);
                                }
                            }
                    );
                }
            }
        };
    }

    private CountDownTimer startGameTimer(final PlayerIdentifiers playerIdentifiers,
                                          final Timer timer,
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
                    gameStarted = true;
                    serverRequestController.cancelAllRequests();
                    goToGameplayActivity(playerIdentifiers);
                }
                else {
                    goToTitleScreenActivity();
                }
            }
        };
    }

    private void pressJoinGameButton() {
        this.joinPressed = true;

        final Activity that = this;
        final Button joinGameButton = findViewById(R.id.join_game_button);

                try {
                    Log.i("Join Game", "Requested");
                    serverRequestController.joinGameRequest(playerIdentifiers.getPlayerId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                that.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinGameButton.setVisibility(View.GONE);
                        TextView text = findViewById(R.id.join_game_success);
                        text.setVisibility(View.VISIBLE);
                        text.setText(R.string.join_game_waiting);
                    }
                });
                TextView joinStatus = findViewById(R.id.join_game_success);


                (new Timer()).schedule(checkHomeBeaconHasBeenReceived(joinStatus), 1000);


    }

    private void initializeJoinGameButton() {
        hideJoinGameButton();
        final Button joinGameButton = findViewById(R.id.join_game_button);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressJoinGameButton();
            }
        });
    }

    private void hideJoinGameButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Button joinGameButton = findViewById(R.id.join_game_button);
                joinGameButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showJoinGameButton() {
        if(!joinedGame) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Button joinGameButton = findViewById(R.id.join_game_button);
                    joinGameButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private TimerTask checkHomeBeaconHasBeenReceived(final TextView joinStatus) {
        final Activity that = this;
        return new TimerTask() {
            @Override
            public void run() {
                if (gameInfo.startBeaconName != null) {
                    that.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
        // intent.putExtra("start_beacon_major", gameInfo.startBeaconMajor);
        intent.putExtra("start_beacon_name", gameInfo.startBeaconName);
        startActivity(intent);
    }

    private void goToTitleScreenActivity() {
        Intent intent = new Intent(JoinGameActivity.this, TitleScreenActivity.class);
        startActivity(intent);
    }

    private void updateNumberOfPlayersInGame(final String players) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.join_game_players_in_game)).setText(players);
            }
        });
    }

    private void updateTimeLeftUntilGame(final String time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.join_game_time_until_game)).setText(time);
            }
        });
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
}
