package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.app.Activity;
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
import com.bristol.hackerhunt.helloworld.TwinInputRunnable;
import com.emredavarci.circleprogressbar.CircleProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerListView implements IPlayerListView {

    private final LayoutInflater inflater;
    private final Handler uiHandler;      // This is used to run changes to the UI on the UI thread.

    private final LinearLayout playerList;
    private final View emphasisOverlay;

    private String targetCodeName;
    private String pressedPlayerCardPlayerId = "";

    private final Map<String, Integer> playerIdListItemIdMap;
    private final Map<String, String> playerIdNameMap;
    private final Map<String, String> playerIdCodeNameMap;
    private final Map<String, Integer> playerIdIntColourMap;
    private List<String> nearbyPlayerIds;
    private List<String> interceptExchangeIds;

    private Map<String, Integer> playerIdLocationMap;

    private StringInputRunnable beginExposeOnClickRunner;
    private StringInputRunnable beginExchangeOnClickRunner;
    private StringInputRunnable beginSelectedInterceptOnClickRunner;
    private StringInputRunnable darkenOnCardPressRunnable;
    private StringInputRunnable restoreOnBackgroundPressRunnable;

    private boolean exchangeStarted = false;
    private boolean exchangeSuccess = false;
    private boolean exchangeFailure = false;
    private String exchangePlayerId;

    private boolean exposeStarted = false;

    private boolean interceptStarted = false;
    private boolean interceptSuccess = false;
    private boolean interceptFailure = false;
    private String interceptPlayerId;

    private static final long INTERACTION_DISPLAY_PERIOD = 3;

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
        this.interceptExchangeIds = new ArrayList<>();
        this.playerIdLocationMap = new HashMap<>();
        this.playerIdIntColourMap = new HashMap<>();

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
        updateAllTargetCodenameColors();
    }

    private void updateAllTargetCodenameColors() {
        for (String playerId : playerIdListItemIdMap.keySet()) {
            if (playerIdCodeNameMap.containsKey(playerId)) {
                displayPlayerCodeName(playerId);
            }
        }
    }

    @Override
    public void revealPlayerHackerName(String playerId, final String hackerName) {
        playerIdCodeNameMap.put(playerId, hackerName);
        displayPlayerCodeName(playerId);
    }

    private void displayPlayerCodeName(String playerId) {

        Context context = playerList.getContext();
        int id = playerIdListItemIdMap.get(playerId);
        String codeName = playerIdCodeNameMap.get(playerId);

        RelativeLayout listItem = playerList.findViewById(id);
        if (listItem != null) {
            final TextView nameView = listItem.findViewById(R.id.player_hacker_name);

            setTextOfView(nameView, codeName);

            if (codeName != null && codeName.equals(targetCodeName)) {
                nameView.setBackgroundColor(ContextCompat.getColor(context,
                        R.color.player_is_target_codename));
            } else {
                nameView.setBackgroundColor(ContextCompat.getColor(context,
                        R.color.player_is_not_target_codename));
            }
        }
    }

    // an on-click listener that should be run when the user "taps-out" of the interaction buttons.
    private View.OnClickListener cancelInteractionOnClickListener(final String playerId) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressedPlayerCardPlayerId = "";
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
                pressedPlayerCardPlayerId = playerId;
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
        Log.i("Inserting Player", playerId);
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
        evidenceGathered.setText(String.valueOf(progress));

        // if the code name of the player is known, reveal it.
        if (playerIdCodeNameMap.containsKey(playerId)) {
            Log.i("I Know Codename of", playerId);
            displayPlayerCodeName(playerId);
        }

        // load the player card into the list.
        playerList.addView(listItem, 0);

        // update the look & behavior of the player card depending on where the player is.
        if (nearby) {
            nearbyPlayerEntryColoring(playerId);
            listItem.setOnClickListener(playerCardOnClickListener(playerId, listItem));
            setExposeOnClickListener(playerId);

            if (exchangeStarted) {
                disableExchangeButton(playerId);

                if (exchangePlayerId.equals(playerId)) {
                    displayExchangeRequested(playerId);
                }
            }
            else {
                enableExchangeButton(playerId);

                if (exchangePlayerId != null && exchangePlayerId.equals(playerId) && exchangeSuccess) {
                    displayExchangeSuccess(playerId);
                }
                if (exchangePlayerId != null && exchangePlayerId.equals(playerId) && exchangeFailure) {
                    displayExchangeFailure(playerId);
                }
            }

            if (interceptStarted) {
                disableInterceptButton(playerId);

                if (interceptPlayerId.equals(playerId)){
                    displayInterceptPending(playerId);
                }
            }
            else {
                enableInterceptButton(playerId);

                if (interceptPlayerId != null && interceptPlayerId.equals(playerId) && interceptSuccess) {
                    displayInterceptSuccess(playerId);
                }
                if (interceptPlayerId != null && interceptPlayerId.equals(playerId) && interceptFailure) {
                    displayInterceptFailure(playerId);
                }
            }

            if(interceptExchangeIds.contains(playerId)){
                disableExchangeButton(playerId);
                disableInterceptButton(playerId);
            }
        }
        else {
            listItem.setOnClickListener(null);

            if (exchangeStarted && exchangePlayerId.equals(playerId)) {
                displayExchangeRequested(playerId);
            }

            if (exchangePlayerId != null && exchangePlayerId.equals(playerId) && exchangeSuccess) {
                displayExchangeSuccess(playerId);
            }
            if (exchangePlayerId != null && exchangePlayerId.equals(playerId) && exchangeFailure) {
                displayExchangeFailure(playerId);
            }

            if (interceptStarted && interceptPlayerId.equals(playerId)) {
                displayInterceptPending(playerId);
            }

            if (interceptPlayerId != null && interceptPlayerId.equals(playerId) && interceptSuccess) {
                displayInterceptSuccess(playerId);
            }
            if (interceptPlayerId != null && interceptPlayerId.equals(playerId) && interceptFailure) {
                displayInterceptFailure(playerId);
            }

            if (pressedPlayerCardPlayerId.equals(playerId)) {
                restoreOnBackgroundPressRunnable.run(playerId);
            }
            darkenFarAwayPlayerEntries(playerId);
        }
        updateAllTargetCodenameColors();
    }

    private void enableExchangeButton(String playerId) {
        getExchangeButton(playerId).setBackgroundResource(R.drawable.exchange_button);
        setExchangeOnClickListener(playerId);
    }

    private void enableAllExchangeButtons() {
        for (String playerId : playerIdNameMap.keySet()) {
            enableExchangeButton(playerId);
        }
    }

    private void disableExchangeButton(String playerId) {
        getExchangeButton(playerId).setBackgroundResource(R.drawable.exchange_button_greyed);
        removeExchangeOnClickListener(playerId);
    }

    private void disableAllExchangeButtons() {
        for (String playerId : playerIdNameMap.keySet()) {
            disableExchangeButton(playerId);
        }
    }

    private void disableAllExposeButtons() {
        for (String playerId :playerIdNameMap.keySet()) {
            disableExposeButton(playerId);
        }
    }

    private void disableExposeButton(String playerId) {
        getExposeButton(playerId).setBackgroundResource(R.drawable.expose_button_greyed);
        removeExposeOnClickListener(playerId);
    }

    private void enableAllExposeButtons() {
        for (String playerId : playerIdNameMap.keySet()) {
            enableExposeButton(playerId);
        }
    }

    private void enableExposeButton(String playerId) {
        getExposeButton(playerId).setBackgroundResource(R.drawable.expose_button);
        setExposeOnClickListener(playerId);
    }

    private void removeExposeOnClickListener(String playerId) {
        getExposeButton(playerId).setOnClickListener(null);
    }

    @Override
    public void enableAllButtons() {
        enableAllExposeButtons();

        //Now disable correct buttons again

        //Only enable intercepts if not doing one
        if(!interceptStarted){
            enableAllInterceptButtons();
        }

        //Only enable exchanges if not doing one
        if(!exchangeStarted){
            enableAllExchangeButtons();
        }

        //Disable Exchange/Intercept for specific player if
        // currently Intercept/Exchange active
        for(String playerId : interceptExchangeIds){
            disableExchangeButton(playerId);
            disableInterceptButton(playerId);
        }
    }

    @Override
    public void disableAllButtons(){
        disableAllExchangeButtons();
        disableAllInterceptButtons();
        disableAllExposeButtons();
    }



    private View getExposeButton(String playerId){
        return getPlayerCard(playerId).findViewById(R.id.gameplay_expose_button);
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

    private void setInterceptOnClickListener(final String playerId) {
        getInterceptButton(playerId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interceptStarted = true;
                interceptExchangeIds.add(playerId);
                interceptPlayerId = playerId;
                displayInterceptPending(playerId);
                beginSelectedInterceptOnClickRunner.run(playerId);
                disableAllInterceptButtons();
                disableExchangeButton(playerId);
            }
        });
    }

    private void enableInterceptButton(String playerId) { 
        getInterceptButton(playerId).setBackgroundResource(R.drawable.intercept_button);
        setInterceptOnClickListener(playerId);
    }

    private void enableAllInterceptButtons() {
        for (String playerId : playerIdNameMap.keySet()) {
            enableInterceptButton(playerId);
        }
    }

    private void disableInterceptButton(String playerId) {
        View button = getInterceptButton(playerId);
        button.setBackgroundResource(R.drawable.intercept_button_greyed);
        button.setOnClickListener(null);
    }

    private void disableAllInterceptButtons() {
        for (String playerId : playerIdNameMap.keySet()) {
            disableInterceptButton(playerId);
        }
    }

    @Override
    public void increasePlayerIntel(final String playerId, final int intelIncrement) {
        if (!playerIdListItemIdMap.containsKey(playerId)) {
            Log.e("Error","Error: player is not listed as playing the game.");
        }
        else {
            if (getPlayerIntel(playerId) < 100) {
                int id = playerIdListItemIdMap.get(playerId);
                RelativeLayout listItem = playerList.findViewById(id);
                final CircleProgressBar intelBar = listItem.findViewById(R.id.player_intel_circle);

                final Context context = intelBar.getContext();
                final float intel = intelBar.getProgress();
                final float newProgress = (intel + intelIncrement >= 100) ? 100 : intel + intelIncrement;

                intelBar.setText("+" + String.valueOf(intelIncrement));
                intelBar.setTextColor(ContextCompat.getColor(playerList.getContext(),
                        R.color.progress_bar_increase));

                final Handler increaseHandler = new Handler();
                increaseHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (intelBar.getProgress() < newProgress) {
                            intelBar.setProgress(Math.min(newProgress, intelBar.getProgress() + 2));
                            increaseHandler.postDelayed(this, 25);
                        }

                        else if (newProgress >= 100) {
                            setFullIntelCircleProgressBarColours(playerId, intelBar);
                        }
                    }
                }, 25);

                final Handler restoreTextHandler = new Handler();
                restoreTextHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (newProgress >= 100) {
                            intelBar.setTextColor(ContextCompat.getColor(context, R.color.progress_bar_complete_evidence_text));
                        }
                        else {
                            intelBar.setTextColor(ContextCompat.getColor(context, R.color.progress_bar_text));
                        }
                        intelBar.setText(Integer.toString((int) newProgress));
                        intelBar.setProgress(newProgress);
                        exchangeSuccess = false;
                        interceptSuccess = false;
                    }
                }, 1000);
            }
        }
    }

    private void setExchangeOnClickListener(final String playerId) {
        getExchangeButton(playerId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exchangeStarted = true;
                interceptExchangeIds.add(playerId);
                exchangePlayerId = playerId;
                displayExchangeRequested(playerId);
                beginExchangeOnClickRunner.run(playerId);
                disableAllExchangeButtons();
                disableInterceptButton(playerId);
            }
        });
    }

    private void setFullIntelCircleProgressBarColours(String playerId, CircleProgressBar progressBar) {
        if (nearbyPlayerIds.contains(playerId)) {
            progressBar.setTextColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_complete_evidence_text));
            progressBar.setProgressColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_complete_evidence));
        }
        else {
            // TODO
            progressBar.setTextColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_complete_evidence_text));
            progressBar.setProgressColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_complete_evidence));
        }
    }

    private void setNotFullIntelCircleProgressBarColoursNearby(CircleProgressBar progressBar) {
        progressBar.setTextColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_text));
        progressBar.setProgressColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar));
        progressBar.setBackgroundColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_background));
    }

    private void setNotFullIntelCircleProgressBarColoursFar(CircleProgressBar progressBar) {
        progressBar.setTextColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_text_far));
        progressBar.setProgressColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_far));
        progressBar.setBackgroundColor(ContextCompat.getColor(progressBar.getContext(),
                    R.color.progress_bar_background_far));
    }

    @Override
    public void decreasePlayerIntel(String playerId, int intelIncrement) {
        Log.d("Decrease Intel", Integer.toString(intelIncrement));
        if (!playerIdListItemIdMap.containsKey(playerId)) {
            throw new IllegalArgumentException("Error: player is not listed as playing the game.");
        }
        else {
            int id = playerIdListItemIdMap.get(playerId);
            RelativeLayout listItem = playerList.findViewById(id);
            final CircleProgressBar intelBar = listItem.findViewById(R.id.player_intel_circle);

            float intel = intelBar.getProgress();
            final float newIntel = Math.max(0, intel - intelIncrement);

            if (newIntel >= 100) {
                setFullIntelCircleProgressBarColours(playerId, intelBar);
            }
            else {
                setNotFullIntelCircleProgressBarColoursNearby(intelBar);
            }

            final Handler decreaseHandler = new Handler();
            decreaseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (intelBar.getProgress() > newIntel) {
                        intelBar.setProgress(Math.max(newIntel, intelBar.getProgress() - 2));
                        intelBar.setText(Integer.toString((int) intelBar.getProgress()));
                        decreaseHandler.postDelayed(this, 25);
                    }
                }
            }, 25);
        }
    }

    @Override
    public void clearPlayerIntel(String playerId) {
        if (playerIdListItemIdMap.containsKey(playerId)) {
            int id = playerIdListItemIdMap.get(playerId);
            RelativeLayout listItem = playerList.findViewById(id);
            final CircleProgressBar intelBar = listItem.findViewById(R.id.player_intel_circle);

            setNotFullIntelCircleProgressBarColoursNearby(intelBar);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (intelBar.getProgress() > 0) {
                        intelBar.setProgress(Math.max(0, intelBar.getProgress() - 2));
                        intelBar.setText(Integer.toString((int) intelBar.getProgress()));
                        handler.postDelayed(this, 25);
                    }
                    else {
                        // TODO.
                    }
                }
            }, 25);
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
            updateAllTargetCodenameColors();
            updateAllPlayerLocation();
            this.nearbyPlayerIds = newNearbyPlayerIds;
        }
    }

    private void displayExchangeRequested(String playerId) {
        //hideIntStatus(playerId);

        setIntStatusText(playerId,"Exchange Requested\u00A0");
        setIntStatusColour(playerId,getColor(R.color.gameplay_time_left));
        setIntStatusImage(playerId, R.drawable.exchange_blue);

        displayIntStatus(playerId);
    }

    private void displayInterceptPending(String playerId){
        //hideIntStatus(playerId);

        setIntStatusText(playerId,"Intercept Pending\u00A0");
        setIntStatusColour(playerId,getColor(R.color.gameplay_time_left));
        setIntStatusImage(playerId, R.drawable.intercept_blue);

        displayIntStatus(playerId);
    }

    private void timedHideIntStatus(final String playerId){
        final View intStatus = getIntStatusFlag(playerId);
        intStatus.postDelayed(
                new Runnable() {
                    public void run(){
                        hideIntStatus(playerId);
                    }
                }, INTERACTION_DISPLAY_PERIOD*1000);
    }

    private void timedHideExchangeStatus(final String playerId){
        final View intStatus = getIntStatusFlag(playerId);
        intStatus.postDelayed(
                new Runnable() {
                    public void run(){
                        exchangeSuccess = false;
                        exchangeFailure = false;
                        hideIntStatus(playerId);
                    }
                }, INTERACTION_DISPLAY_PERIOD*1000);
    }

    private void timedHideInterceptStatus(final String playerId){
        final View intStatus = getIntStatusFlag(playerId);
        intStatus.postDelayed(
                new Runnable() {
                    public void run(){
                        interceptSuccess = false;
                        interceptFailure = false;
                        hideIntStatus(playerId);
                    }
                }, INTERACTION_DISPLAY_PERIOD*1000);
    }

    private void displayExchangeSuccess(String playerId) {
        //hideIntStatus(playerId);

        setIntStatusText(playerId,"Exchange Success\u00A0");
        exchangeSuccess = true;
        setIntStatusColour(playerId, getColor(R.color.interaction_success));
        setIntStatusImage(playerId, R.drawable.exchange_button_green);

        displayIntStatus(playerId);
    }

    private void timedDisplayExchangeSuccess(final String playerId){
        displayExchangeSuccess(playerId);
        timedHideExchangeStatus(playerId);
    }

    private void displayExchangeFailure(String playerId) {
        //hideIntStatus(playerId);

        setIntStatusText(playerId,"Exchange Rejected\u00A0");
        exchangeFailure = true;
        setIntStatusColour(playerId,getColor(R.color.interaction_failure));
        setIntStatusImage(playerId, R.drawable.exchange_button_red);

        displayIntStatus(playerId);
    }

    private void timedDisplayExchangeFailure(final String playerId){
        displayExchangeFailure(playerId);
        timedHideExchangeStatus(playerId);
    }

    private void displayInterceptSuccess(String playerId) {
        //hideIntStatus(playerId);

        setIntStatusText(playerId,"Intercept Success\u00A0");
        interceptSuccess = true;
        setIntStatusColour(playerId,getColor(R.color.interaction_success));
        setIntStatusImage(playerId, R.drawable.intercept_button_green);

        displayIntStatus(playerId);
    }

    private void timedDisplayInterceptSuccess(final String playerId){
        displayInterceptSuccess(playerId);
        timedHideInterceptStatus(playerId);
    }

    private void displayInterceptFailure(String playerId) {
        //hideIntStatus(playerId);

        setIntStatusText(playerId,"Intercept Failure\u00A0");
        interceptFailure = true;
        setIntStatusColour(playerId,getColor(R.color.interaction_failure));
        setIntStatusImage(playerId, R.drawable.intercept_button_red);

        displayIntStatus(playerId);

    }

    private void timedDisplayInterceptFailure(final String playerId){
        displayInterceptFailure(playerId);
        timedHideInterceptStatus(playerId);
    }

    private void displayIntStatus(String playerId) {
        showIntStatusContainer(playerId);
        showIntStatusImage(playerId);
        showIntStatusText(playerId);
    }

    private void hideIntStatus(String playerId) {
        hideIntStatusContainer(playerId);
        hideIntStatusImage(playerId);
        hideIntStatusText(playerId);
    }

    private void hideIntStatusContainer(String playerId){
        getPlayerCard(playerId).findViewById(R.id.exchange_requested).setVisibility(View.INVISIBLE);
    }

    private void showIntStatusContainer(String playerId){
        getPlayerCard(playerId).findViewById(R.id.exchange_requested).setVisibility(View.VISIBLE);
    }

    private void hideIntStatusText(String playerId){
        getPlayerCard(playerId).findViewById(R.id.exchange_requested_text).setVisibility(View.INVISIBLE);
    }

    private void showIntStatusText(String playerId){
        getPlayerCard(playerId).findViewById(R.id.exchange_requested_text).setVisibility(View.VISIBLE);
    }

    private void hideIntStatusImage(String playerId){
        getPlayerCard(playerId).findViewById(R.id.exchange_requested_icon).setVisibility(View.INVISIBLE);
    }

    private void showIntStatusImage(String playerId){
        getPlayerCard(playerId).findViewById(R.id.exchange_requested_icon).setVisibility(View.VISIBLE);
    }

    private View getIntStatusFlag(String playerId) {
        return getPlayerCard(playerId).findViewById(R.id.exchange_requested);
    }

    private void setIntStatusText(String playerId, String text){
        hideIntStatusText(playerId);
        TextView tv = getPlayerCard(playerId).findViewById(R.id.exchange_requested_text);
        tv.setText(text);
        showIntStatusText(playerId);
    }

    private void setIntStatusColour(String playerId, int colour){
        TextView tv = getPlayerCard(playerId).findViewById(R.id.exchange_requested_text);
        Log.d("Colour set", Integer.toHexString(colour));
        tv.setTextColor(colour);
    }

    private void setIntStatusImage(String playerId, int resId){
        hideIntStatusImage(playerId);
        ImageView iv = getPlayerCard(playerId).findViewById(R.id.exchange_requested_icon);
        iv.setImageResource(resId);
        showIntStatusImage(playerId);
    }

    private ImageView getPlayerFlagView(String playerId){
        if(playerId.isEmpty()){
            Log.i("Flag View", "Empty playerId");
        }
        return getPlayerCard(playerId).findViewById(R.id.player_card_flag);

    }

    @Override
    public TwinInputRunnable changeLocationRunnable(){
        return new TwinInputRunnable() {
            @Override
            public void run(String playerId, String flagString) {
                int flag = Integer.parseInt(flagString);
                changePlayerLocation(playerId, flag);
            }
        };
    }

    private int getFlag(int flag){
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

        return imageId;
    }

    @Override
    public void changePlayerLocation(String playerId, int flag){
        if(!playerId.isEmpty()) {
            if (playerIdLocationMap.containsKey(playerId)) {
                playerIdLocationMap.remove(playerId);
                playerIdLocationMap.put(playerId, flag);
                //Can't use replace due to API level
            } else {
                playerIdLocationMap.put(playerId, flag);
            }
            ImageView iv = getPlayerFlagView(playerId);

            int imageId = getFlag(flag);


            iv.setImageResource(imageId);
        }
        else{
            Log.d("Change Location", "Empty string for playerId");
        }
    }

    private void updateAllPlayerLocation(){
        int flag;
        int imageId;
        ImageView iv;
        for(String playerId : playerIdLocationMap.keySet()){
            flag = playerIdLocationMap.get(playerId);

            iv = getPlayerFlagView(playerId);

            imageId = getFlag(flag);

            iv.setImageResource(imageId);

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
                view.setVisibility(View.VISIBLE);
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

    private void nearbyPlayerEntryColoring(String playerId) {
        Context context = playerList.getContext();

        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.findViewById(R.id.player_item_background)
                .setBackgroundResource(R.drawable.player_card);
        entry.findViewById(R.id.far_overlay).setVisibility(View.GONE);

        CircleProgressBar pb = entry.findViewById(R.id.player_intel_circle);
        pb.setBackgroundColor(ContextCompat.getColor(context,
                R.color.progress_bar_background));

        if (getPlayerIntel(playerId) >= 100) {
            setFullIntelCircleProgressBarColours(playerId, pb);
        }
        else {
            setNotFullIntelCircleProgressBarColoursNearby(pb);
        }
    }

    private void darkenFarAwayPlayerEntries(String playerId) {
 /*       Context context = playerList.getContext();

        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.findViewById(R.id.player_item_background)
                .setBackgroundResource(R.drawable.player_card_far);

        CircleProgressBar pb = entry.findViewById(R.id.player_intel_circle);
        if (getPlayerIntel(playerId) >= 100) {
            setFullIntelCircleProgressBarColours(playerId, pb);
        }
        else {
            setNotFullIntelCircleProgressBarColoursFar(pb);
        }

        pb.setBackgroundColor(ContextCompat.getColor(context,
                R.color.progress_bar_background_far));
*/
        nearbyPlayerEntryColoring(playerId);
        int viewId = playerIdListItemIdMap.get(playerId);
        View entry = playerList.findViewById(viewId);
        entry.findViewById(R.id.far_overlay).setVisibility(View.VISIBLE);
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

                playerIdIntColourMap.put(playerId, ((TextView) playerCard.findViewById(R.id.exchange_requested_text)).getCurrentTextColor());

                // handle core of the player card:
                ((TextView) playerCard.findViewById(R.id.player_name))
                        .setTextColor(getColor(R.color.player_card_name_darkened));
                ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                        .setImageResource(R.drawable.player_card_divider_darkened);

                View background = playerCard.findViewById(R.id.player_item_background);
                int cardBackground = R.drawable.player_card_darkened;
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

                if (getPlayerIntel(playerId) >= 100) {
                    progressBarColor = getColor(R.color.progress_bar_complete_evidence_darkened);
                    progressBarTextColor = getColor(R.color.progress_bar_complete_evidence_text_darkened);
                }

                circleProgressBar.setProgressColor(progressBarColor);
                circleProgressBar.setBackgroundColor(progressBarBackgroundColor);
                circleProgressBar.setTextColor(progressBarTextColor);

                ((TextView) playerCard.findViewById(R.id.exchange_requested_text))
                        .setTextColor(getColor(R.color.player_card_name_darkened));
                ((ImageView) playerCard.findViewById(R.id.exchange_requested_icon))
                        .setColorFilter(getColor(R.color.player_card_name_darkened), PorterDuff.Mode.MULTIPLY);
                ((ImageView) playerCard.findViewById(R.id.player_card_flag))
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

    private void restoreUiColours(String playerId) {
        View playerCard = getPlayerCard(playerId);

        // handle core of the player card:
        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(getColor(R.color.player_card_name));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider);

        View background = playerCard.findViewById(R.id.player_item_background);
        int cardBackground = R.drawable.player_card;
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
        int progressBarBackgroundColor = getColor(R.color.progress_bar_background);

        if (getPlayerIntel(playerId) >= 100) {
            setFullIntelCircleProgressBarColours(playerId, circleProgressBar);
        }
        else {
            setNotFullIntelCircleProgressBarColoursNearby(circleProgressBar);
        }

        circleProgressBar.setBackgroundColor(progressBarBackgroundColor);


        try{
            ((TextView) playerCard.findViewById(R.id.exchange_requested_text))
                    .setTextColor(playerIdIntColourMap.get(playerId));
        } catch (NullPointerException e){
            ((TextView) playerCard.findViewById(R.id.exchange_requested_text))
                    .setTextColor(getColor(R.color.gameplay_time_left));
        }
        playerIdIntColourMap.remove(playerId);
        ((ImageView) playerCard.findViewById(R.id.exchange_requested_icon)).clearColorFilter();
        ((ImageView) playerCard.findViewById(R.id.player_card_flag)).clearColorFilter();

    }

    @Override
    public void exchangeRequestComplete(String playerId, boolean success) {
        exchangeStarted = false;
        enableAllExchangeButtons();

        if(interceptExchangeIds.contains(playerId)){
            interceptExchangeIds.remove(playerId);
            enableInterceptButton(playerId);
        }
        Log.d("Exchange complete", Boolean.toString(success) + playerId);
        if(success){
            timedDisplayExchangeSuccess(playerId);
        }
        else {
            timedDisplayExchangeFailure(playerId);
        }
    }

    @Override
    public void interceptAttemptComplete(String playerId, boolean success) {
        interceptStarted = false;
        enableAllInterceptButtons();

        if(interceptExchangeIds.contains(playerId)){
            interceptExchangeIds.remove(playerId);
            enableExchangeButton(playerId);
        }
        Log.d("Intercept complete", Boolean.toString(success) + playerId);

        if(success){
            timedDisplayInterceptSuccess(playerId);
        }
        else {
            timedDisplayInterceptFailure(playerId);
        }
    }

    private int getColor(int id) {
        return playerList.getResources().getColor(id);
    }

}
