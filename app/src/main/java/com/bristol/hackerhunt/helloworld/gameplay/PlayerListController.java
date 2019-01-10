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
    private final Map<String, Integer> playerIdListItemIdMap;
    private final Map<String, String> playerIdNameMap;
    private List<String> nearbyPlayerIds;

    public PlayerListController(LayoutInflater inflater, LinearLayout playerList) {
        this.inflater = inflater;
        this.playerList  = playerList;
        this.playerIdListItemIdMap = new HashMap<>();
        this.playerIdNameMap = new HashMap<>();

        this.nearbyPlayerIds = new ArrayList<>();
    }

    public void insertPlayer(String playerId, String playerName) {
        playerIdNameMap.put(playerId, playerName);
        insertPlayer(playerId, false, 0);
    }

    private void insertPlayer(String playerId, boolean nearby, int progress) {
        LinearLayout listItem = (LinearLayout) inflater.inflate(R.layout.gameplay_player_list_item, null);

        int playerItemId = View.generateViewId();
        playerIdListItemIdMap.put(playerId, playerItemId);
        listItem.setId(playerItemId);

        TextView playerNameView = listItem.findViewById(R.id.player_name);
        ProgressBar intelGathered = listItem.findViewById(R.id.player_intel_bar);

        String playerName = playerIdNameMap.get(playerId);
        playerNameView.setText(playerName);
        intelGathered.setProgress(progress);

        if (nearby) {
            listItem.findViewById(R.id.player_item_background).setBackgroundColor(ContextCompat.getColor(playerList.getContext(), R.color.gameplay_nearby_player_background));
            playerNameView.setTextColor(ContextCompat.getColor(playerList.getContext(), R.color.gameplay_nearby_player_name));
        }

        playerList.addView(listItem, 0);
    }

    public void increasePlayerIntel(String playerId, int intelIncrement) {
        if (!playerIdListItemIdMap.containsKey(playerId)) {
            throw new IllegalArgumentException("Error: player is not listed as playing the game.");
        }
        else {
            int id = playerIdListItemIdMap.get(playerId);
            LinearLayout listItem = playerList.findViewById(id);
            ProgressBar intelBar = listItem.findViewById(R.id.player_intel_bar);
            intelBar.incrementProgressBy(intelIncrement);
        }
    }

    public void updateNearbyPlayers(List<String> newNearbyPlayerIds) {
        for (String playerId : nearbyPlayerIds) {
            if (!newNearbyPlayerIds.contains(playerId)) {
                int intel = getPlayerIntel(playerId);
                removeListItemEntry(playerId);
                insertPlayer(playerId, false, intel);
            }
        }

        for (String playerId : newNearbyPlayerIds) {
            int intel = getPlayerIntel(playerId);
            removeListItemEntry(playerId);
            insertPlayer(playerId, true, intel);
        }

        this.nearbyPlayerIds = newNearbyPlayerIds;
    }

    private int getPlayerIntel(String playerId) {
        if (!playerIdListItemIdMap.containsKey(playerId)) {
            throw new IllegalArgumentException("Error: player is not listed as playing the game.");
        }
        else {
            int id = playerIdListItemIdMap.get(playerId);
            LinearLayout ll = playerList.findViewById(id);
            ProgressBar ib = ll.findViewById(R.id.player_intel_bar);
            return ib.getProgress();
        }
    }

    private void removeListItemEntry(String playerId) {
        int id = playerIdListItemIdMap.get(playerId);
        LinearLayout view = playerList.findViewById(id);
        playerList.removeView(view);
    }
}
