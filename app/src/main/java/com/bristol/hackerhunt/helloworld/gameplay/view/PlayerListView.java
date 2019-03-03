package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

public class PlayerListView implements IPlayerListView {

    private final LayoutInflater inflater;
    private final Handler uiHandler;      // This is used to run changes to the UI on the UI thread.

    private final LinearLayout playerList;

    private final Map<String, Integer> playerIdListItemIdMap;
    private final Map<String, String> playerIdNameMap;
    private final Map<String, String> playerIdHackerNameMap;
    private List<String> nearbyPlayerIds;

    private StringInputRunnable beginSelectedTakedownOnClickRunner;
    private StringInputRunnable beginSelectedExchangeOnClickRunner;
    private StringInputRunnable beginSelectedInterceptOnClickRunner;

    private boolean exchangeStarted = false;
    private boolean takedownStarted = false;
    private boolean interceptStarted = false;

    /**
     * Constructor
     * @param inflater Inflater used to insert and render new UI components.
     * @param playerList The view that wraps the list of players in the UI.
     * @param beginSelectedTakedownOnClickRunner A Runner that will initialize the takedown process
     *                                           on a selected player when run.
     * @param beginSelectedExchangeOnClickRunner A Runner that will initialize the mutual exchange
     *                                           process on a selected player when run.
     * @param beginSelectedInterceptOnClickRunner A Runner that initialize the intercept process
     *                                            on a selected player when run.
     */
    public PlayerListView(LayoutInflater inflater, LinearLayout playerList,
                   StringInputRunnable beginSelectedTakedownOnClickRunner,
                   StringInputRunnable beginSelectedExchangeOnClickRunner,
                   StringInputRunnable beginSelectedInterceptOnClickRunner) {
        this.inflater = inflater;
        this.playerList  = playerList;
        this.playerIdListItemIdMap = new HashMap<>();
        this.playerIdNameMap = new HashMap<>();
        this.playerIdHackerNameMap = new HashMap<>();
        this.nearbyPlayerIds = new ArrayList<>();

        this.beginSelectedTakedownOnClickRunner = beginSelectedTakedownOnClickRunner;
        this.beginSelectedExchangeOnClickRunner = beginSelectedExchangeOnClickRunner;
        this.beginSelectedInterceptOnClickRunner = beginSelectedInterceptOnClickRunner;

        this.uiHandler = new Handler(playerList.getContext().getMainLooper());
    }

    @Override
    public void revealPlayerHackerName(String playerId, final String hackerName) {
        playerIdHackerNameMap.put(playerId, hackerName);
        int id = playerIdListItemIdMap.get(playerId);
        LinearLayout listItem = playerList.findViewById(id);
        final TextView nameView = listItem.findViewById(R.id.player_hacker_name);

        if (!nearbyPlayerIds.contains(playerId)) {
            setTextOfView(nameView, hackerName, R.color.gameplay_far_player_name);
        }
        else {
            setTextOfView(nameView, hackerName);
        }
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
            listItem.findViewById(R.id.player_item_background)
                    .setBackgroundColor(ContextCompat.getColor(playerList.getContext(),
                            R.color.gameplay_nearby_player_background));
            playerNameView
                    .setTextColor(ContextCompat.getColor(playerList.getContext(),
                            R.color.gameplay_nearby_player_name));
        }

        if (playerIdHackerNameMap.containsKey(playerId)) {
            TextView hackerNameView = listItem.findViewById(R.id.player_hacker_name);
            String hackerName = playerIdHackerNameMap.get(playerId);
            if (nearby) {
                setTextOfView(hackerNameView, hackerName);
            }
            else {
                setTextOfView(hackerNameView, hackerName, R.color.gameplay_far_player_name);
            }
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
            TextView hackerName = listItem.findViewById(R.id.player_hacker_name);
            hackerName.setVisibility(View.GONE);
            playerIdHackerNameMap.remove(playerId);
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

                    if (exchangeStarted || takedownStarted) {
                        clearOnClickListener(playerId);
                        darkenFarAwayPlayerEntries(playerId);
                    }
                }
            }

            for (String playerId : newNearbyPlayerIds) {
                int intel = getPlayerIntel(playerId);
                removeListItemEntry(playerId);
                insertPlayer(playerId, true, intel);

                if (exchangeStarted) {
                    setExchangeOnClickListener(playerId);
                }
                if (takedownStarted) {
                    setTakedownOnClickListener(playerId);
                }
                if (interceptStarted) {
                    setInterceptOnClickListener((playerId));
                }
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
                if (view.getVisibility() == View.GONE) {
                    view.setVisibility(View.VISIBLE);
                }
                view.setText(text);
            }
        });
    }

    // Run text update on UI thread.
    private void setTextOfView(final TextView view, final String text, final int colorId) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (view.getVisibility() == View.GONE) {
                    view.setVisibility(View.VISIBLE);
                }
                view.setText(text);
                view.setTextColor(ContextCompat.getColor(view.getContext(), colorId));
            }
        });
    }

    @Override
    public void beginTakedown() {
        this.takedownStarted = true;
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
                R.color.progress_bar_background_far_interaction)));
        pb.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context,
                R.color.progress_bar_far_interaction)));
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
    public void resumeGameplayAfterInteraction() {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!nearbyPlayerIds.contains(playerId)) {
                restoreFarAwayPlayerEntry(playerId);
            }
            else {
                clearOnClickListener(playerId);
            }
        }
        this.takedownStarted = false;
        this.exchangeStarted = false;
        this.interceptStarted = false;
    }

    private void restoreFarAwayPlayerEntry(String playerId) {
        Context context = playerList.getContext();

        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.findViewById(R.id.player_item_background).setBackgroundColor(ContextCompat.getColor(context,
                R.color.gameplay_far_player_background));

        ProgressBar pb = entry.findViewById(R.id.player_intel_bar);
        pb.setProgressBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(pb.getContext(),
                R.color.progress_bar_background)));
        pb.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(pb.getContext(),
                R.color.progress_bar)));
    }

    private void clearOnClickListener(String playerId) {
        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.setOnClickListener(null);
    }

    @Override
    public void beginExchange() {
        this.exchangeStarted = true;
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!nearbyPlayerIds.contains(playerId)) {
                darkenFarAwayPlayerEntries(playerId);
            }
            else {
                setExchangeOnClickListener(playerId);
            }
        }
    }

    @Override
    public void beginIntercept() {
        this.interceptStarted = true;
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!nearbyPlayerIds.contains(playerId)) {
                darkenFarAwayPlayerEntries(playerId);
            }
            else {
                setInterceptOnClickListener(playerId);
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

    private void setInterceptOnClickListener(final String playerId) {
        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginSelectedInterceptOnClickRunner.run(playerId);
            }
        });
    }
}
