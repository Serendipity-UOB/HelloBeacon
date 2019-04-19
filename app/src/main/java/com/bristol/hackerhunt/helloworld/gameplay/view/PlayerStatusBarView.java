package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;

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
    public void setPlayerLocation(int flag){
        ImageView iv = playerStatusBar.findViewById(R.id.current_game_zone_logo);
        int imageId = R.drawable.beacon_valor;
        if(flag == 0){
            imageId = R.drawable.un_flag_small;
        }
        else if(flag == 1){
            imageId = R.drawable.italy_flag_dark;
        }
        else if(flag == 2){
            imageId = R.drawable.sweden_flag_dark;
        }
        else if(flag == 3){
            imageId = R.drawable.switzerland_flag_dark;
        }
        else if(flag == 4){
            imageId = R.drawable.czech_republic_flag_dark;
        }
        else{
            Log.d("Bad Flag", "Flag number " + Integer.toString(flag));
        }
        iv.setImageResource(imageId);
    }

    @Override
    public StringInputRunnable changePlayerLocationRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(String input) {
                int flag = Integer.parseInt(input);
                setPlayerLocation(flag);
            }
        };
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

        // game zone logo:
        ((ImageView) playerStatusBar.findViewById(R.id.current_game_zone_logo))
                .setColorFilter(getColor(R.color.player_card_name_darkened), PorterDuff.Mode.MULTIPLY);

    }

    @Override
    public void restore() {
        // agent name:
        ((TextView) playerStatusBar.findViewById(R.id.gameplay_player_name_title))
                .setTextColor(getColor(R.color.gameplay_player_name));
        ((TextView) playerStatusBar.findViewById(R.id.gameplay_player_name))
                .setTextColor(getColor(R.color.gameplay_player_name));

        // stats:
        LinearLayout stats = playerStatusBar.findViewById(R.id.gameplay_player_stats);
        for (int i = 0; i < stats.getChildCount(); i++) {
            View child = stats.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(getColor(R.color.gameplay_player_stats));
            }
        }

        // target:
        View targetWrapper = playerStatusBar.findViewById(R.id.status_target_wrapper);
        targetWrapper.setBackgroundResource(R.drawable.target_status_bar_border);
        ((TextView) targetWrapper.findViewById(R.id.status_target_prefix))
                .setTextColor(getColor(R.color.gameplay_target_prefix));
        ((TextView) targetWrapper.findViewById(R.id.gameplay_player_target))
                .setTextColor(getColor(R.color.gameplay_target));

        // time left:
        View timeWrapper = playerStatusBar.findViewById(R.id.status_time_wrapper);
        timeWrapper.setBackgroundResource(R.drawable.time_status_bar_border);
        ((TextView) timeWrapper.findViewById(R.id.gameplay_time_left))
                .setTextColor(getColor(R.color.gameplay_time_left));

        ((ImageView) playerStatusBar.findViewById(R.id.current_game_zone_logo)).clearColorFilter();
    }

    private int getColor(int id) {
        return playerStatusBar.getResources().getColor(id);
    }
}
