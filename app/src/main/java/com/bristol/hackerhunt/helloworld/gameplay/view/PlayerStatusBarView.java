package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;

public class PlayerStatusBarView implements IPlayerStatusBarView {

    private final View playerStatusBar;

    public PlayerStatusBarView(View playerStatusBar) {
        this.playerStatusBar = playerStatusBar;
    }

    @Override
    public void setPlayerTargetCodeName(String targetCodeName) {
        TextView targetCodeNameView = playerStatusBar.findViewById(R.id.gameplay_player_target);
        targetCodeNameView.setText(targetCodeName);
    }

    @Override
    public void setPlayerPoints(String points) {
        TextView pointsTextView = playerStatusBar.findViewById(R.id.gameplay_player_leaderboard_points);
        pointsTextView.setText(points + " rep\u00A0");
    }

    @Override
    public void setPlayerLeaderboardPosition(String position) {
        TextView positionTextView = playerStatusBar.findViewById(R.id.gameplay_player_leaderboard_position);
        positionTextView.setText("#" + position + "\u00A0");
    }

    @Override
    public void setPlayerName(String playerName){
        TextView nameTextView = playerStatusBar.findViewById(R.id.gameplay_player_name);
        nameTextView.setText(playerName + "\u00A0");
    }
}
