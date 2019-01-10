package com.bristol.hackerhunt.helloworld.gameplay;

import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;

public class PlayerStatusBarController {
    private final View playerStatusBar;

    public PlayerStatusBarController(View playerStatusBar) {
        this.playerStatusBar = playerStatusBar;
    }

    public void setPlayerTargetHackerName(String targetHackerName) {
        TextView targetHackerNameView = playerStatusBar.findViewById(R.id.gameplay_player_target);
        String prefix = playerStatusBar.getContext().getString(R.string.gameplay_player_target);
        targetHackerNameView.setText(prefix + " " + targetHackerName);
    }

    public void setPlayerPoints(String points) {
        TextView pointsTextView = playerStatusBar.findViewById(R.id.gameplay_player_leaderboard_points);
        String prefix = playerStatusBar.getContext().getString(R.string.gameplay_player_leaderboard_points);
        pointsTextView.setText(prefix + " " + points);
    }

    public void setPlayerLeaderboardPosition(String position) {
        TextView positionTextView = playerStatusBar.findViewById(R.id.gameplay_player_leaderboard_position);
        String prefix = playerStatusBar.getContext().getString(R.string.gameplay_player_leaderboard_position);
        positionTextView.setText(prefix + " #" + position);
    }
}
