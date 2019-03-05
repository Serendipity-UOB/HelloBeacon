package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.view.View;
import android.widget.LinearLayout;
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

    @Override
    public void darken() {
        // agent name:
        ((TextView) playerStatusBar.findViewById(R.id.gameplay_player_name_title))
                .setTextColor(getColor(R.color.gameplay_player_name_darkened));
        ((TextView) playerStatusBar.findViewById(R.id.gameplay_player_name))
                .setTextColor(getColor(R.color.gameplay_player_name_darkened));

        // stats:
        LinearLayout stats = playerStatusBar.findViewById(R.id.gameplay_player_stats);
        for (int i = 0; i < stats.getChildCount(); i++) {
            View child = stats.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(getColor(R.color.gameplay_player_stats_darkened));
            }
        }

        // target:
        View targetWrapper = playerStatusBar.findViewById(R.id.status_target_wrapper);
        targetWrapper.setBackgroundResource(R.drawable.target_status_bar_border_darkened);
        ((TextView) targetWrapper.findViewById(R.id.status_target_prefix))
                .setTextColor(getColor(R.color.gameplay_target_prefix_darkened));
        ((TextView) targetWrapper.findViewById(R.id.gameplay_player_target))
                .setTextColor(getColor(R.color.gameplay_target_darkened));

        // time left:
        View timeWrapper = playerStatusBar.findViewById(R.id.status_time_wrapper);
        timeWrapper.setBackgroundResource(R.drawable.time_status_bar_border_darkened);
        ((TextView) timeWrapper.findViewById(R.id.gameplay_time_left))
                .setTextColor(getColor(R.color.gameplay_time_left_darkened));
    }

    private int getColor(int id) {
        return playerStatusBar.getResources().getColor(id);
    }
}
