package com.bristol.hackerhunt.helloworld.gameplay;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerListController implements IPlayerListController {
    private final LayoutInflater inflater;
    private final LinearLayout playerList;
    private final Map<String, Integer> playerIdListItemIdMap;
    private final Map<String, String> playerIdNameMap;
    private List<String> nearbyPlayerIds;

    private StringInputRunnable beginSelectedTakedownOnClickRunner;
    private StringInputRunnable beginSelectedExchangeOnClickRunner;

    private final Handler uiHandler;

    PlayerListController(LayoutInflater inflater, LinearLayout playerList,
                         StringInputRunnable beginSelectedTakedownOnClickRunner,
                         StringInputRunnable beginSelectedExchangeOnClickRunner) {
        this.inflater = inflater;
        this.playerList  = playerList;
        this.playerIdListItemIdMap = new HashMap<>();
        this.playerIdNameMap = new HashMap<>();
        this.nearbyPlayerIds = new ArrayList<>();

        this.beginSelectedTakedownOnClickRunner = beginSelectedTakedownOnClickRunner;
        this.beginSelectedExchangeOnClickRunner = beginSelectedExchangeOnClickRunner;

        this.uiHandler = new Handler(playerList.getContext().getMainLooper());
    }

    @Override
    public void revealPlayerHackerName(String playerId, final String hackerName) {
        int id = playerIdListItemIdMap.get(playerId);
        LinearLayout listItem = playerList.findViewById(id);
        final TextView nameView = listItem.findViewById(R.id.player_name);
        this.playerIdNameMap.put(playerId, hackerName);

        setTextOfView(nameView, hackerName);
    }

    @Override
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
        setTextOfView(playerNameView, playerName);
        intelGathered.setProgress(progress);

        if (nearby) {
            listItem.findViewById(R.id.player_item_background).setBackgroundColor(ContextCompat.getColor(playerList.getContext(), R.color.gameplay_nearby_player_background));
            playerNameView.setTextColor(ContextCompat.getColor(playerList.getContext(), R.color.gameplay_nearby_player_name));
        }

        playerList.addView(listItem, 0);
    }

    @Override
    public void increasePlayerIntel(String playerId, int intelIncrement) {
        if (!playerIdListItemIdMap.containsKey(playerId)) {
            throw new IllegalArgumentException("Error: player is not listed as playing the game.");
        }
        else {
            int id = playerIdListItemIdMap.get(playerId);
            LinearLayout listItem = playerList.findViewById(id);
            ProgressBar intelBar = listItem.findViewById(R.id.player_intel_bar);

            int intel = intelBar.getProgress();
            intelBar.setProgress(intel + intelIncrement);
        }
    }

    @Override
    public void decreasePlayerIntel(String playerId, int intelIncrement) {
        if (!playerIdListItemIdMap.containsKey(playerId)) {
            throw new IllegalArgumentException("Error: player is not listed as playing the game.");
        }
        else {
            int id = playerIdListItemIdMap.get(playerId);
            LinearLayout listItem = playerList.findViewById(id);
            ProgressBar intelBar = listItem.findViewById(R.id.player_intel_bar);

            int intel = intelBar.getProgress();
            intelBar.setProgress(intel - intelIncrement);
        }
    }

    @Override
    public void updateNearbyPlayers(List<String> newNearbyPlayerIds) {
        if (!newNearbyPlayerIds.containsAll(nearbyPlayerIds) || !nearbyPlayerIds.containsAll(newNearbyPlayerIds)) {

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

    // Run text update on UI thread.
    private void setTextOfView(final TextView view, final String text) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                view.setText(text);
            }
        });
    }

    @Override
    public void beginTakedown() {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!nearbyPlayerIds.contains(playerId)) {
                darkenFarAwayPlayerEntries(playerId);
            }
            else {
                setTakedownOnClickListener(playerId);
            }
        }
    }

    private void darkenFarAwayPlayerEntries(String playerId) {
        Context context = playerList.getContext();

        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.findViewById(R.id.player_item_background).setBackgroundColor(ContextCompat.getColor(context,
                R.color.gameplay_nearby_player_background_interaction));

        ProgressBar pb = entry.findViewById(R.id.player_intel_bar);
        pb.setProgressBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context,
                R.color.gameplay_far_player_progress_bar_background_interaction)));
        pb.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context,
                R.color.gameplay_far_player_progress_bar_interaction)));
    }

    private void setTakedownOnClickListener(final String playerId) {
        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginSelectedTakedownOnClickRunner.run(playerId);
            }
        });
    }

    @Override
    public void resumeGameplay() {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!nearbyPlayerIds.contains(playerId)) {
                restoreFarAwayPlayerEntry(playerId);
            }
            else {
                clearOnClickListener(playerId);
            }
        }
    }

    private void restoreFarAwayPlayerEntry(String playerId) {
        Context context = playerList.getContext();

        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.findViewById(R.id.player_item_background).setBackgroundColor(ContextCompat.getColor(context,
                R.color.gameplay_far_player_background));

        ProgressBar pb = entry.findViewById(R.id.player_intel_bar);
        pb.setProgressBackgroundTintMode(null);
        pb.setProgressTintMode(null);
    }

    private void clearOnClickListener(String playerId) {
        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.setOnClickListener(null);
    }

    @Override
    public void beginExchange() {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!nearbyPlayerIds.contains(playerId)) {
                darkenFarAwayPlayerEntries(playerId);
            }
            else {
                setExchangeOnClickListener(playerId);
            }
        }
    }

    private void setExchangeOnClickListener(final String playerId) {
        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginSelectedExchangeOnClickRunner.run(playerId);
            }
        });
    }
}
