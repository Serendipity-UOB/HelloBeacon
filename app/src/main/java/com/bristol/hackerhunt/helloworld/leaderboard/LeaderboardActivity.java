package com.bristol.hackerhunt.helloworld.leaderboard;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.TitleScreenActivity;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LeaderboardActivity extends AppCompatActivity {

    private PlayerIdentifiers playerIdentifiers;
    private ILeaderboardServerRequestController serverRequestController;

    private List<LeaderboardItem> leaderboardItems;
    private LinearLayout leaderboardList;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        HashMap<String,String> playerIdsRealNameMap = (HashMap<String, String>)
                getIntent().getExtras().getSerializable(String.valueOf(R.string.all_players_map_intent_key));
        Log.d("debug",playerIdsRealNameMap.toString());

        this.playerIdentifiers = getIntent().getParcelableExtra(getString(R.string.player_identifiers_intent_key));

        this.leaderboardList = findViewById(R.id.leaderboard_list);
        this.inflater = LayoutInflater.from(this);

        this.serverRequestController = new LeaderboardServerRequestController(this, playerIdsRealNameMap, playerIdentifiers);
        this.leaderboardItems = new ArrayList<>();
        initializeReturnToTitleButton();

        try {
            serverRequestController.getInfoRequest(leaderboardItems);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // idle wait, loading.
        final Activity that = this;
        Timer timer = new Timer(false);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!leaderboardItems.isEmpty()) {
                    that.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    insertLeaderboard();
                                    hideLoadingBar();
                                    showLeaderboard();
                                }
                            }
                    );
                    cancel();
                }
            }
        }, 0, 1000);
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }

    private void initializeReturnToTitleButton() {
        final Button returnToTitleButton = findViewById(R.id.return_to_title_button);
        returnToTitleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeaderboardActivity.this, TitleScreenActivity.class);
                startActivity(intent);
            }
        });
    }

    private void insertLeaderboard() {
        for (int i = 1; i <= leaderboardItems.size(); i++) {
            insertLeaderboardItem(leaderboardItems.get(i-1));
        }
    }

    private void insertLeaderboardItem(LeaderboardItem item) {
        LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.leaderboard_list_item, null);

        TextView playerName = itemView.findViewById(R.id.player_name);
        TextView playerScore = itemView.findViewById(R.id.player_score);
        TextView playerPosition = itemView.findViewById(R.id.leaderboard_position);
        View itemViewBackground = itemView.findViewById(R.id.leaderboard_list_item_background);
        ImageView crown = itemView.findViewById(R.id.crown);

        String name = item.playerName;
        String score = Integer.toString(item.score) + " rep";
        int position = item.position;

        playerName.setText(name);
        playerScore.setText(score);
        playerPosition.setText("#" + String.valueOf(position));

        if (this.playerIdentifiers.getPlayerId().equals(item.playerId)) {
            itemViewBackground.setBackgroundResource(R.drawable.player_card);
        }

        // set position crowns.
        if (position == 1) {
            crown.setImageResource(R.drawable.gold_crown);
        }
        else if (position == 2) {
            crown.setImageResource(R.drawable.silver_crown);
        }
        else if (position == 3) {
            crown.setImageResource(R.drawable.bronze_crown);
        }
        else {
            crown.setVisibility(View.INVISIBLE);
        }

        leaderboardList.addView(itemView);
    }

    private void showLeaderboard() {
        View view = (View) leaderboardList.getParent();
        view.setVisibility(View.VISIBLE);
    }

    private void hideLoadingBar() {
        View view = findViewById(R.id.leaderboard_loading);
        view.setVisibility(View.GONE);
    }
}
