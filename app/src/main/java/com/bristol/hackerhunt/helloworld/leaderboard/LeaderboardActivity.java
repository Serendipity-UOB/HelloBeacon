package com.bristol.hackerhunt.helloworld.leaderboard;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.TitleScreenActivity;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        while (leaderboardItems.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        insertLeaderboard();
        hideLoadingBar();
        showLeaderboard();
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
            insertLeaderboardItem(i, leaderboardItems.get(i-1));
        }
    }

    private void insertLeaderboardItem(int position, LeaderboardItem item) {
        RelativeLayout itemView = (RelativeLayout) inflater.inflate(R.layout.leaderboard_list_item, null);

        TextView playerName = itemView.findViewById(R.id.player_name);
        TextView playerScore = itemView.findViewById(R.id.player_score);

        String name = "#" + Integer.toString(position) + " " + item.playerName;
        String score = Integer.toString(item.score);

        playerName.setText(name);
        playerScore.setText(score);

        if (this.playerIdentifiers.getNfcId().equals(item.playerId)) {
            int color = ContextCompat.getColor(this, R.color.gameplay_nearby_player_name);
            playerName.setTextColor(color);
            playerScore.setTextColor(color);
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
