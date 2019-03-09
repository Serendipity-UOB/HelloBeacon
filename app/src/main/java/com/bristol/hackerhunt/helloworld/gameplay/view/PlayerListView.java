package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.content.Context;
import android.graphics.PorterDuff;
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
    private final Map<String, String> playerIdCodeNameMap;
    private List<String> nearbyPlayerIds;

    private StringInputRunnable beginExposeOnClickRunner;
    private StringInputRunnable beginExchangeOnClickRunner;
    private StringInputRunnable beginSelectedInterceptOnClickRunner;
    private StringInputRunnable darkenOnCardPressRunnable;
    private StringInputRunnable restoreOnBackgroundPressRunnable;

    private boolean exchangeStarted = false;
    private boolean exposeStarted = false;
    private boolean interceptStarted = false;

    /**
     * Constructor
     * @param inflater Inflater used to insert and render new UI components.
     * @param playerList The view that wraps the list of players in the UI.
     * @param beginExposeOnClickRunner A Runner that will initialize the takedown process
     *                                           on a selected player when run.
     * @param beginExchangeOnClickRunner A Runner that will initialize the mutual exchange
     *                                           process on a selected player when run.
     * @param beginSelectedInterceptOnClickRunner A Runner that initialize the intercept process
     *                                            on a selected player when run.
     */
    public PlayerListView(LayoutInflater inflater, LinearLayout playerList,
                   StringInputRunnable beginExposeOnClickRunner,
                   StringInputRunnable beginExchangeOnClickRunner,
                   StringInputRunnable beginSelectedInterceptOnClickRunner,
                   StringInputRunnable darkenOnCardPressRunnable,
                   StringInputRunnable restoreOnBackgroundPressRunnable) {
        this.inflater = inflater;
        this.playerList  = playerList;
        this.playerIdListItemIdMap = new HashMap<>();
        this.playerIdNameMap = new HashMap<>();
        this.playerIdCodeNameMap = new HashMap<>();
        this.nearbyPlayerIds = new ArrayList<>();

        this.beginExposeOnClickRunner = beginExposeOnClickRunner;
        this.beginExchangeOnClickRunner = beginExchangeOnClickRunner;
        this.beginSelectedInterceptOnClickRunner = beginSelectedInterceptOnClickRunner;
        this.emphasisOverlay = playerList.getRootView().findViewById(R.id.emphasis_overlay);

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

        playerIdCodeNameMap.put(playerId, hackerName);
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

    // an on-click listener that should be run when the user "taps-out" of the interaction buttons.
    private View.OnClickListener cancelInteractionOnClickListener(final String playerId) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreOnBackgroundPressRunnable.run(playerId);
            }
        };
    }

    // an on-click listener run when a player card is clicked, causing the interaction buttons to appear.
    private View.OnClickListener playerCardOnClickListener(final String playerId,
                                                           final RelativeLayout playerCard) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View buttons = playerCard.findViewById(R.id.interaction_buttons);
                buttons.setVisibility(View.VISIBLE);
                emphasisOverlay.setVisibility(View.VISIBLE);
                darkenOnCardPressRunnable.run(playerId);
                tapToCancelOnClickListenersOnAllCardsApartFromPlayerId(playerId);
            }
        };
    }

    @Override
    public void insertPlayer(String playerId, String playerName) {
        playerIdNameMap.put(playerId, playerName);
        insertPlayer(playerId, false, 0);
    }

    // inserts a player into the list.
    private void insertPlayer(String playerId, boolean nearby, int progress) {
        // initialize new player card.
        RelativeLayout listItem = (RelativeLayout) inflater.inflate(R.layout.gameplay_player_list_item, null);

        int playerItemId = View.generateViewId();
        playerIdListItemIdMap.put(playerId, playerItemId);
        listItem.setId(playerItemId);

        TextView playerNameView = listItem.findViewById(R.id.player_name);
        CircleProgressBar evidenceGathered = listItem.findViewById(R.id.player_intel_circle);

        // set player name and evidence.
        String playerName = playerIdNameMap.get(playerId);
        setTextOfView(playerNameView, playerName);
        evidenceGathered.setProgress(progress);

        // if the code name of the player is known, reveal it.
        if (playerIdCodeNameMap.containsKey(playerId)) {
            TextView codeNameView = listItem.findViewById(R.id.player_hacker_name);
            String codeName = playerIdCodeNameMap.get(playerId);
            if (nearby) {
                setTextOfView(codeNameView, codeName);
            }
            else {
                setTextOfView(codeNameView, codeName, R.color.gameplay_far_player_name);
            }
        }

        // load the player card into the list.
        playerList.addView(listItem, 0);

        // update the look & behavior of the player card depending on where the player is.
        if (nearby) {
            restoreFarAwayPlayerEntry(playerId);
            listItem.setOnClickListener(playerCardOnClickListener(playerId, listItem));
            setExposeOnClickListener(playerId);

            if (exchangeStarted) {
                disableExchangeButton(playerId);
            }
            else {
                enableExchangeButton(playerId);
            }

            if (interceptStarted) {
                disableInterceptButton(playerId);
            }
            else {
                enableInterceptButton(playerId);
            }
        }
        else {
            darkenFarAwayPlayerEntries(playerId);
            listItem.setOnClickListener(null);
        }
    }

    private void enableExchangeButton(String playerId) {
        getExchangeButton(playerId).setBackgroundResource(R.drawable.exchange_button);
        setExchangeOnClickListener(playerId);
    }

    private void enableAllExchangeButtons() {
        for (String playerId : nearbyPlayerIds) {
            enableExchangeButton(playerId);
        }
    }

    private void disableExchangeButton(String playerId) {
        getExchangeButton(playerId).setBackgroundResource(R.drawable.exchange_button_greyed);
        removeExchangeOnClickListener(playerId);
    }

    private void disableAllExchangeButtons() {
        for (String playerId : nearbyPlayerIds) {
            disableExchangeButton(playerId);
        }
    }

    private View getPlayerCard(String playerId) {
        int id = playerIdListItemIdMap.get(playerId);
        return playerList.findViewById(id);
    }

    // sets the on-click listener to be run when the expose button for a player card is pressed.
    private void setExposeOnClickListener(final String playerId) {
        View exposeButton = getPlayerCard(playerId).findViewById(R.id.gameplay_expose_button);
        exposeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginExposeOnClickRunner.run(playerId);
            }
        });
    }

    // TODO: attach to intercept button.
    private void setInterceptOnClickListener(final String playerId) {
        getInterceptButton(playerId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interceptStarted = true;
                beginSelectedInterceptOnClickRunner.run(playerId);
                disableAllInterceptButtons();
            }
        });
    }

    private void enableInterceptButton(String playerId) {
        getInterceptButton(playerId).setBackgroundResource(R.drawable.intercept_button);
        setInterceptOnClickListener(playerId);
    }

    private void enableAllInterceptButtons() {
        for (String playerId : nearbyPlayerIds) {
            enableExchangeButton(playerId);
        }
    }

    private void disableInterceptButton(String playerId) {
        View button = getInterceptButton(playerId);
        button.setBackgroundResource(R.drawable.intercept_button_greyed);
        button.setOnClickListener(null);
    }

    private void disableAllInterceptButtons() {
        for (String playerId : nearbyPlayerIds) {
            disableInterceptButton(playerId);
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

    private void setExchangeOnClickListener(final String playerId) {
        getExchangeButton(playerId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exchangeStarted = true;
                beginExchangeOnClickRunner.run(playerId);
                disableAllExchangeButtons();
            }
        });
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

                    clearOnClickListener(playerId);
                    darkenFarAwayPlayerEntries(playerId);
                }
            }

            for (String playerId : newNearbyPlayerIds) {
                int intel = getPlayerIntel(playerId);
                removeListItemEntry(playerId);
                insertPlayer(playerId, true, intel);

                if (exchangeStarted) {
                    setExchangeOnClickListener(playerId);
                }
            }

            this.nearbyPlayerIds = newNearbyPlayerIds;
        }
    }

    @Override
    public void displayExchangeRequested(String playerId) {
        getExchangeRequestedFlag(playerId).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideExchangeRequested(String playerId) {
        getExchangeRequestedFlag(playerId).setVisibility(View.INVISIBLE);
    }

    private View getExchangeRequestedFlag(String playerId) {
        return getPlayerCard(playerId).findViewById(R.id.exchange_requested);
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

    public View getExchangeButton(String playerId) {
        return getPlayerCard(playerId).findViewById(R.id.gameplay_exchange_button);
    }

    public View getInterceptButton(String playerId) {
        return getPlayerCard(playerId).findViewById(R.id.gameplay_intercept_button);
    }

    private void removeExchangeOnClickListener(String playerId) {
        getExchangeButton(playerId).setOnClickListener(null);
    }

    private void tapToCancelOnClickListenersOnAllCardsApartFromPlayerId(String exemptPlayerId) {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (!playerId.equals(exemptPlayerId)) {
                View playerCard = getPlayerCard(playerId);
                playerCard.setOnClickListener(cancelInteractionOnClickListener(exemptPlayerId));
            }
        }
    }

    private void restoreOnClickListenersForPlayerCards() {
        for( String playerId : nearbyPlayerIds) {
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
                if (playerIdCodeNameMap.containsKey(playerId) &&
                        targetCodeName.equals(playerIdCodeNameMap.get(playerId))) {
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


                ((TextView) playerCard.findViewById(R.id.exchange_requested_text))
                        .setTextColor(getColor(R.color.player_card_name_darkened));
                ((ImageView) playerCard.findViewById(R.id.exchange_requested_icon))
                        .setColorFilter(getColor(R.color.player_card_name_darkened), PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    @Override
    public void restore() {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            View playerCard = getPlayerCard(playerId);

            restoreUiColours(playerId);
            restoreOnClickListenersForPlayerCards();
            emphasisOverlay.setVisibility(View.GONE);
            View buttons = playerCard.findViewById(R.id.interaction_buttons);
            buttons.setVisibility(View.GONE);
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

    private void restoreUiColours(String playerId) {
        View playerCard = getPlayerCard(playerId);

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
        if (playerIdCodeNameMap.containsKey(playerId) &&
                targetCodeName.equals(playerIdCodeNameMap.get(playerId))) {
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

        ((TextView) playerCard.findViewById(R.id.exchange_requested_text))
                .setTextColor(getColor(R.color.player_card_name));
        ((ImageView) playerCard.findViewById(R.id.exchange_requested_icon)).clearColorFilter();
    }

    private int getColor(int id) {
        return playerList.getResources().getColor(id);
    }

}
