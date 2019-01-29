package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.gameplay.view.IPlayerStatusBarView;

public class PlayerStatusBarView implements IPlayerStatusBarView {

    private final View playerStatusBar;

    public PlayerStatusBarView(View playerStatusBar) {
        this.playerStatusBar = playerStatusBar;
    }

    @Override
    public void setPlayerTargetHackerName(String targetHackerName) {
        TextView targetHackerNameView = playerStatusBar.findViewById(R.id.gameplay_player_target);
        String prefix = playerStatusBar.getContext().getString(R.string.gameplay_player_target);
        targetHackerNameView.setText(prefix + " " + targetHackerName);
    }

    @Override
    public void setPlayerPoints(String points) {
        TextView pointsTextView = playerStatusBar.findViewById(R.id.gameplay_player_leaderboard_points);
        String prefix = playerStatusBar.getContext().getString(R.string.gameplay_player_leaderboard_points);
        pointsTextView.setText(prefix + " " + points);
    }

    @Override
    public void setPlayerLeaderboardPosition(String position) {
        TextView positionTextView = playerStatusBar.findViewById(R.id.gameplay_player_leaderboard_position);
        String prefix = playerStatusBar.getContext().getString(R.string.gameplay_player_leaderboard_position);
        positionTextView.setText(prefix + " #" + position);
    }
}
