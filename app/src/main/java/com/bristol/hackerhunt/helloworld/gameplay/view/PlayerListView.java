package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;
import com.emredavarci.circleprogressbar.CircleProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerListView implements IPlayerListView {

    private final LayoutInflater inflater;
    private final Handler uiHandler;      // This is used to run changes to the UI on the UI thread.

    private final LinearLayout playerList;
    private final View emphasisOverlay;

    private String targetCodeName;

    private final Map<String, Integer> playerIdListItemIdMap;
    private final Map<String, String> playerIdNameMap;
    private final Map<String, String> playerIdHackerNameMap;
    private List<String> nearbyPlayerIds;

    private StringInputRunnable beginSelectedTakedownOnClickRunner;
    private StringInputRunnable beginSelectedExchangeOnClickRunner;
    private StringInputRunnable darkenOnCardPressRunnable;
    private StringInputRunnable restoreOnBackgroundPressRunnable;

    private boolean exchangeStarted = false;
    private boolean takedownStarted = false;

    /**
     * Constructor
     * @param inflater Inflater used to insert and render new UI components.
     * @param playerList The view that wraps the list of players in the UI.
     * @param beginSelectedTakedownOnClickRunner A Runner that will initialize the takedown process
     *                                           on a selected player when run.
     * @param beginSelectedExchangeOnClickRunner A Runner that will initialize the mutual exchange
     *                                           process on a selected player when run.
     */
    public PlayerListView(LayoutInflater inflater, LinearLayout playerList,
                   StringInputRunnable beginSelectedTakedownOnClickRunner,
                   StringInputRunnable beginSelectedExchangeOnClickRunner,
                   StringInputRunnable darkenOnCardPressRunnable,
                   StringInputRunnable restoreOnBackgroundPressRunnable) {
        this.inflater = inflater;
        this.playerList  = playerList;
        this.playerIdListItemIdMap = new HashMap<>();
        this.playerIdNameMap = new HashMap<>();
        this.playerIdHackerNameMap = new HashMap<>();
        this.nearbyPlayerIds = new ArrayList<>();

        this.emphasisOverlay = playerList.getRootView().findViewById(R.id.emphasis_overlay);

        this.beginSelectedTakedownOnClickRunner = beginSelectedTakedownOnClickRunner;
        this.beginSelectedExchangeOnClickRunner = beginSelectedExchangeOnClickRunner;
        this.darkenOnCardPressRunnable = darkenOnCardPressRunnable;
        this.restoreOnBackgroundPressRunnable = restoreOnBackgroundPressRunnable;

        this.uiHandler = new Handler(playerList.getContext().getMainLooper());
    }

    @Override
    public void setTargetCodeName(String codename) {
        this.targetCodeName = codename;
    }

    @Override
    public void revealPlayerHackerName(String playerId, final String hackerName) {
        Context context = playerList.getContext();

        playerIdHackerNameMap.put(playerId, hackerName);
        int id = playerIdListItemIdMap.get(playerId);
        RelativeLayout listItem = playerList.findViewById(id);
        final TextView nameView = listItem.findViewById(R.id.player_hacker_name);

        setTextOfView(nameView, hackerName);

        if (hackerName.equals(targetCodeName)) {
            nameView.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.player_is_target_codename));
        }
        else {
            nameView.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.player_is_not_target_codename));
        }
    }

    private View.OnClickListener cancelInteractionOnClickListener(final String playerId,
                                                                  final RelativeLayout playerCard) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreOnBackgroundPressRunnable.run(playerId);

                restoreOnClickListenersForPlayerCards();
                emphasisOverlay.setVisibility(View.GONE);
                View buttons = playerCard.findViewById(R.id.interaction_buttons);
                buttons.setVisibility(View.GONE);
            }
        };
    }

    private void enableTapToCancelInteraction(final String playerId,
                                              final RelativeLayout playerCard) {
        emphasisOverlay.setVisibility(View.VISIBLE);
        emphasisOverlay.setOnClickListener(cancelInteractionOnClickListener(playerId, playerCard));
    }

    private View.OnClickListener playerCardOnClickListener(final String playerId,
                                                           final RelativeLayout playerCard) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View buttons = playerCard.findViewById(R.id.interaction_buttons);
                buttons.setVisibility(View.VISIBLE);
                darkenOnCardPressRunnable.run(playerId);
                removeOnClickListenersOnAllCardsApartFromPlayerId(playerId);
                enableTapToCancelInteraction(playerId, playerCard);
            }
        };
    }

    @Override
    public void insertPlayer(String playerId, String playerName) {
        playerIdNameMap.put(playerId, playerName);
        insertPlayer(playerId, false, 0);
    }

    private void insertPlayer(String playerId, boolean nearby, int progress) {
        RelativeLayout listItem = (RelativeLayout) inflater.inflate(R.layout.gameplay_player_list_item, null);
        listItem.setOnClickListener(playerCardOnClickListener(playerId, listItem));

        int playerItemId = View.generateViewId();
        playerIdListItemIdMap.put(playerId, playerItemId);
        listItem.setId(playerItemId);

        TextView playerNameView = listItem.findViewById(R.id.player_name);
        CircleProgressBar intelGathered = listItem.findViewById(R.id.player_intel_circle);

        String playerName = playerIdNameMap.get(playerId);
        setTextOfView(playerNameView, playerName);
        intelGathered.setProgress(progress);

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

        // update the look of the player card depending on where the player is.
        if (nearby) {
            restoreFarAwayPlayerEntry(playerId);
        }
        else {
            darkenFarAwayPlayerEntries(playerId);
        }
    }

    @Override
    public void increasePlayerIntel(String playerId, int intelIncrement) {
        if (!playerIdListItemIdMap.containsKey(playerId)) {
            Log.e("Error","Error: player is not listed as playing the game.");
        }
        else {
            int id = playerIdListItemIdMap.get(playerId);
            RelativeLayout listItem = playerList.findViewById(id);
            CircleProgressBar intelBar = listItem.findViewById(R.id.player_intel_circle);

            float intel = intelBar.getProgress();
            intelBar.setProgress(intel + intelIncrement);

            if (intel == 100) {
                setFullIntelCircleProgressBarColours(intelBar);
            }
        }
    }

    private void setFullIntelCircleProgressBarColours(CircleProgressBar progressBar) {
        progressBar.setTextColor(R.color.progress_bar_complete_evidence_text);
        progressBar.setProgressColor(R.color.progress_bar_complete_evidence);
    }

    @Override
    public void decreasePlayerIntel(String playerId, int intelIncrement) {
        if (!playerIdListItemIdMap.containsKey(playerId)) {
            throw new IllegalArgumentException("Error: player is not listed as playing the game.");
        }
        else {
            int id = playerIdListItemIdMap.get(playerId);
            RelativeLayout listItem = playerList.findViewById(id);
            CircleProgressBar intelBar = listItem.findViewById(R.id.player_intel_circle);

            float intel = intelBar.getProgress();
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
            RelativeLayout ll = playerList.findViewById(id);
            CircleProgressBar ib = ll.findViewById(R.id.player_intel_circle);
            return (int) ib.getProgress();
        }
    }

    private void removeListItemEntry(String playerId) {
        int id = playerIdListItemIdMap.get(playerId);
        RelativeLayout view = playerList.findViewById(id);
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
                if (view.getVisibility() == View.GONE || view.getVisibility() == View.VISIBLE) {
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
    }

    private void restoreFarAwayPlayerEntry(String playerId) {
        Context context = playerList.getContext();

        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.findViewById(R.id.player_item_background)
                .setBackgroundResource(R.drawable.player_card);

        CircleProgressBar pb = entry.findViewById(R.id.player_intel_circle);
        pb.setBackgroundColor(ContextCompat.getColor(context,
                R.color.progress_bar_background));
        pb.setProgressColor(ContextCompat.getColor(context,
                R.color.progress_bar));
        pb.setTextColor(ContextCompat.getColor(context,
                R.color.progress_bar_text));
    }

    private void darkenFarAwayPlayerEntries(String playerId) {
        Context context = playerList.getContext();

        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.findViewById(R.id.player_item_background)
                .setBackgroundResource(R.drawable.player_card_far);

        CircleProgressBar pb = entry.findViewById(R.id.player_intel_circle);
        pb.setBackgroundColor(ContextCompat.getColor(context,
                R.color.progress_bar_background_far));
        pb.setProgressColor(ContextCompat.getColor(context,
                R.color.progress_bar_far));
        pb.setTextColor(ContextCompat.getColor(context,
                R.color.progress_bar_text_far));
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

    private void removeOnClickListenersOnAllCardsApartFromPlayerId(String exemptPlayerId) {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!playerId.equals(exemptPlayerId)) {
                int id = playerIdListItemIdMap.get(playerId);
                RelativeLayout playerCard = playerList.findViewById(id);
                playerCard.setOnClickListener(null);
            }
        }
    }

    private void restoreOnClickListenersForPlayerCards() {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            int id = playerIdListItemIdMap.get(playerId);
            RelativeLayout playerCard = playerList.findViewById(id);
            playerCard.setOnClickListener(playerCardOnClickListener(playerId, playerCard));
        }
    }

    @Override
    public void darken(String exemptPlayerId) {

        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!playerId.equals(exemptPlayerId)) {
                int id = playerIdListItemIdMap.get(playerId);
                RelativeLayout playerCard = playerList.findViewById(id);

                // handle core of the player card:
                ((TextView) playerCard.findViewById(R.id.player_name))
                        .setTextColor(getColor(R.color.player_card_name_darkened));
                ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                        .setImageResource(R.drawable.player_card_divider_darkened);

                View background = playerCard.findViewById(R.id.player_item_background);
                int cardBackground = R.drawable.player_card_far_darkened;
                if (nearbyPlayerIds.contains(playerId)) {
                    cardBackground = R.drawable.player_card_darkened;
                }
                background.setBackgroundResource(cardBackground);

                // handle any revealed codenames:
                TextView codename = playerCard.findViewById(R.id.player_hacker_name);
                codename.setTextColor(getColor(R.color.player_card_target_codename_text_darkened));
                int codenameBackground = getColor(R.color.player_is_not_target_codename_darkened);
                if (playerIdHackerNameMap.containsKey(playerId) &&
                        targetCodeName.equals(playerIdHackerNameMap.get(playerId))) {
                    codenameBackground = getColor(R.color.player_is_target_codename_darkened);
                }
                codename.setBackgroundColor(codenameBackground);

                // handle evidence bars:
                CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.player_intel_circle);
                int progressBarColor = getColor(R.color.progress_bar_darkened);
                int progressBarBackgroundColor = getColor(R.color.progress_bar_background_darkened);
                int progressBarTextColor = getColor(R.color.progress_bar_text_darkened);

                if (getPlayerIntel(playerId) == 100) {
                    progressBarColor = getColor(R.color.progress_bar_complete_evidence_darkened);
                    progressBarTextColor = getColor(R.color.progress_bar_complete_evidence_text_darkened);
                } else if (!nearbyPlayerIds.contains(playerId)) {
                    progressBarColor = getColor(R.color.progress_bar_far_darkened);
                    progressBarBackgroundColor = getColor(R.color.progress_bar_background_far_darkened);
                    progressBarTextColor = getColor(R.color.progress_bar_text_far_darkened);
                }
                circleProgressBar.setProgressColor(progressBarColor);
                circleProgressBar.setBackgroundColor(progressBarBackgroundColor);
                circleProgressBar.setTextColor(progressBarTextColor);
            }
        }
    }

    @Override
    public void restore() {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            int id = playerIdListItemIdMap.get(playerId);
            RelativeLayout playerCard = playerList.findViewById(id);

            // handle core of the player card:
            ((TextView) playerCard.findViewById(R.id.player_name))
                    .setTextColor(getColor(R.color.player_card_name));
            ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                    .setImageResource(R.drawable.player_card_divider);

            View background = playerCard.findViewById(R.id.player_item_background);
            int cardBackground = R.drawable.player_card_far;
            if (nearbyPlayerIds.contains(playerId)) {
                cardBackground = R.drawable.player_card;
            }
            background.setBackgroundResource(cardBackground);

            // handle any revealed codenames:
            TextView codename = playerCard.findViewById(R.id.player_hacker_name);
            codename.setTextColor(getColor(R.color.player_card_target_codename_text));
            int codenameBackground = getColor(R.color.player_is_not_target_codename);
            if (playerIdHackerNameMap.containsKey(playerId) &&
                    targetCodeName.equals(playerIdHackerNameMap.get(playerId))) {
                codenameBackground = getColor(R.color.player_is_target_codename);
            }
            codename.setBackgroundColor(codenameBackground);

            // handle evidence bars:
            CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.player_intel_circle);
            int progressBarColor = getColor(R.color.progress_bar);
            int progressBarBackgroundColor = getColor(R.color.progress_bar_background);
            int progressBarTextColor = getColor(R.color.progress_bar_text);

            if (getPlayerIntel(playerId) == 100) {
                progressBarColor = getColor(R.color.progress_bar_complete_evidence);
                progressBarTextColor = getColor(R.color.progress_bar_complete_evidence_text);
            } else if (!nearbyPlayerIds.contains(playerId)) {
                progressBarColor = getColor(R.color.progress_bar_far);
                progressBarBackgroundColor = getColor(R.color.progress_bar_background_far);
                progressBarTextColor = getColor(R.color.progress_bar_text_far);
            }
            circleProgressBar.setProgressColor(progressBarColor);
            circleProgressBar.setBackgroundColor(progressBarBackgroundColor);
            circleProgressBar.setTextColor(progressBarTextColor);
        }
    }

    private int getColor(int id) {
        return playerList.getResources().getColor(id);
    }
}
