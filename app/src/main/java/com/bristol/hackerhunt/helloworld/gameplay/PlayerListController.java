package com.bristol.hackerhunt.helloworld.gameplay;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerListController {
    private final LayoutInflater inflater;
    private final LinearLayout playerList;
    private final Map<String, Integer> playerListItemIdMap;
    private final int intelIncrement;
    private List<String> nearbyPlayers;

    public PlayerListController(LayoutInflater inflater, LinearLayout playerList, int intelIncrement) {
        this.inflater = inflater;
        this.playerList  = playerList;
        this.playerListItemIdMap = new HashMap<>();

        if (intelIncrement < 0 || intelIncrement > 100) {
            throw new IllegalArgumentException("Error: increment used for intel needs to be a percentage");
        }
        this.intelIncrement = intelIncrement;

        this.nearbyPlayers = new ArrayList<>();
    }

    public void insertPlayer(String playerName) {
        insertPlayer(playerName, false, 0);
    }

    private void insertPlayer(String playerName, boolean nearby, int progress) {
        LinearLayout listItem = (LinearLayout) inflater.inflate(R.layout.gameplay_player_list_item, null);

        int playerItemId = View.generateViewId();
        playerListItemIdMap.put(playerName, playerItemId);
        listItem.setId(playerItemId);

        TextView playerNameView = listItem.findViewById(R.id.player_name);
        ProgressBar intelGathered = listItem.findViewById(R.id.player_intel_bar);

        playerNameView.setText(playerName);
        intelGathered.setProgress(progress);
        if (nearby) {
            listItem.findViewById(R.id.player_item_background).setBackgroundColor(ContextCompat.getColor(playerList.getContext(), R.color.gameplay_nearby_player_background));
            playerNameView.setTextColor(ContextCompat.getColor(playerList.getContext(), R.color.gameplay_nearby_player_name));
        }

        playerList.addView(listItem, 0);
    }

    public void increasePlayerIntel(String playerName) {
        if (!playerListItemIdMap.containsKey(playerName)) {
            throw new IllegalArgumentException("Error: player is not listed as playing the game.");
        }
        else {
            int id = playerListItemIdMap.get(playerName);
            LinearLayout listItem = playerList.findViewById(id);
            ProgressBar intelBar = listItem.findViewById(R.id.player_intel_bar);
            intelBar.incrementProgressBy(intelIncrement);
        }
    }

    public void updateNearbyPlayers(List<String> newNearbyPlayerNames) {
        for (String playerName : nearbyPlayers) {
            if (!newNearbyPlayerNames.contains(playerName)) {
                int intel = getPlayerIntel(playerName);
                removeListItemEntry(playerName);
                insertPlayer(playerName, false, intel);
            }
        }

        for (String playerName : newNearbyPlayerNames) {
            int intel = getPlayerIntel(playerName);
            removeListItemEntry(playerName);
            insertPlayer(playerName, true, intel);
        }

        this.nearbyPlayers = newNearbyPlayerNames;
    }

    private int getPlayerIntel(String playerName) {
        if (!playerListItemIdMap.containsKey(playerName)) {
            throw new IllegalArgumentException("Error: player is not listed as playing the game.");
        }
        else {
            int id = playerListItemIdMap.get(playerName);
            LinearLayout ll = playerList.findViewById(id);
            ProgressBar ib = ll.findViewById(R.id.player_intel_bar);
            return ib.getProgress();
        }
    }

    private void removeListItemEntry(String playerName) {
        int id = playerListItemIdMap.get(playerName);
        LinearLayout view = playerList.findViewById(id);
        playerList.removeView(view);
    }
}
