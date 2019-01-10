package com.bristol.hackerhunt.helloworld;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.gameplay.GameplayActivity;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinGameActivity extends AppCompatActivity {
    private Integer noOfPlayers = 1;
    private long timeToGameStart = 10000;
    private String startBeacon = "Beacon A";

    private boolean joinedGame = false;
    private boolean startedTimer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        final PlayerIdentifiers playerIdentifiers = getIntent().getParcelableExtra("player_identifiers");

        replaceStringInTextView(R.id.join_game_welcome_text, "$PLAYER_NAME", playerIdentifiers.getRealName());
        updateNumberOfPlayersInGame("Loading...");
        updateTimeLeftUntilGame("Loading...");

        initializeJoinGameButton();

        // "Receiving" information; insert server calls here.
        updateNumberOfPlayersInGame(noOfPlayers.toString());

        // Starting timer thread
        new CountDownTimer(timeToGameStart, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                String formattedTime = formatTime(millisUntilFinished);
                updateTimeLeftUntilGame(formattedTime);
            }

            @Override
            public void onFinish() {
                if (joinedGame) {
                    goToGameplayActivity(playerIdentifiers);
                }
                else {
                    goToTitleScreenActivity();
                }
            }
        }.start();
    }

    private void initializeJoinGameButton() {
        final Button joinGameButton = findViewById(R.id.join_game_button);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGameButton.setVisibility(View.GONE);
                findViewById(R.id.join_game_success).setVisibility(View.VISIBLE);
                joinedGame = true;
            }
        });
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
        intent.putExtra("start_beacon", startBeacon);
        startActivity(intent);
    }

    private void goToTitleScreenActivity() {
        Intent intent = new Intent(JoinGameActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private JSONObject joinGameJson(String playerId) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("player_id", playerId);
        return obj;
    }

    private void updateNumberOfPlayersInGame(String players) {
        appendStringToTextView(R.id.join_game_players_in_game, getResources().getString(R.string.join_game_players_in_game), players);
    }

    private void updateTimeLeftUntilGame(String time) {
        appendStringToTextView(R.id.join_game_time_until_game, getResources().getString(R.string.join_game_time_until_game), time);
    }

    private void replaceStringInTextView(int viewId, String oldString, String newString) {
        TextView textView = findViewById(viewId);
        String oldText = textView.getText().toString();
        String newText = oldText.replace(oldString, newString);
        textView.setText(newText);
    }

    private void appendStringToTextView(int viewId, String text, String suffix) {
        TextView textView = findViewById(viewId);
        textView.setText(text + " " + suffix);
    }
}
