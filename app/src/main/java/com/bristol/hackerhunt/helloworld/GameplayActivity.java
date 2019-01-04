package com.bristol.hackerhunt.helloworld;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

public class GameplayActivity extends AppCompatActivity {

    private static final long GAMEPLAY_DURATION = 10; // given in minutes.
    private PlayerIdentifiers playerIdentifiers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);
        getPlayerIdentifiers();

        final Button endGameButton = findViewById(R.id.end_game_button);
        endGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLeaderboardActivity();
            }
        });

        startGameTimer();

        // Server polling: all players

        // Request new target from the server at the start of the game.
        setPlayerTargetHackerName("cutie_kitten");

        // Server polling: receive leaderboard position.
        setPlayerLeaderboardPosition("2");

        // Server polling: receive points.
        setPlayerPoints("516");

        // Server polling: nearby players
    }

    private void startGameTimer() {
        // Starting timer thread
        new CountDownTimer(GAMEPLAY_DURATION * 60 * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                String formattedTime = formatTime(millisUntilFinished);
                updateTimeLeftUntilGameOver(formattedTime);
            }

            @Override
            public void onFinish() {
                goToLeaderboardActivity();
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

    private void goToLeaderboardActivity() {
        Intent intent = new Intent(GameplayActivity.this, LeaderboardActivity.class);
        intent.putExtra(getString(R.string.player_identifiers_intent_key), playerIdentifiers);
        startActivity(intent);
    }

    private void getPlayerIdentifiers() {
        this.playerIdentifiers = getIntent().getParcelableExtra(getString(R.string.player_identifiers_intent_key));
    }

    private void setPlayerTargetHackerName(String targetHackerName) {
        TextView targetHackerNameView = findViewById(R.id.gameplay_player_target);
        String prefix = getString(R.string.gameplay_player_target);
        targetHackerNameView.setText(prefix + targetHackerName);
    }

    private void setPlayerLeaderboardPosition(String position) {
        TextView positionTextView = findViewById(R.id.gameplay_player_leaderboard_position);
        String prefix = getString(R.string.gameplay_player_leaderboard_position);
        positionTextView.setText(prefix + "#" + position);
    }

    private void setPlayerPoints(String points) {
        TextView pointsTextView = findViewById(R.id.gameplay_player_leaderboard_points);
        String prefix = getString(R.string.gameplay_player_leaderboard_points);
        pointsTextView.setText(prefix + points);
    }
}
