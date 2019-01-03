package com.bristol.hackerhunt.helloworld;

import android.content.Intent;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

public class JoinGameActivity extends AppCompatActivity {
    private Integer noOfPlayers = 1;
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

        final Button joinGameButton = findViewById(R.id.join_game_button);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGameButton.setVisibility(View.GONE);
                findViewById(R.id.join_game_success).setVisibility(View.VISIBLE);
                joinedGame = true;
            }
        });

        // "Receiving" information; insert server calls here.
        updateNumberOfPlayersInGame(noOfPlayers.toString());

        // Starting timer thread
        new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                Long minutes = millisUntilFinished / 60000;
                Long seconds = (millisUntilFinished - minutes * 60000) / 1000;
                String formattedTime = minutes.toString() + ":" ;
                if (minutes < 10) formattedTime = "0" + formattedTime;
                if (seconds < 10) formattedTime = formattedTime + "0";
                formattedTime = formattedTime + seconds.toString();

                updateTimeLeftUntilGame(formattedTime);
            }

            @Override
            public void onFinish() {
                if (joinedGame) {
                    Intent intent = new Intent(JoinGameActivity.this, GameplayActivity.class);
                    intent.putExtra("player_identifiers", playerIdentifiers);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(JoinGameActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        }.start();
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
