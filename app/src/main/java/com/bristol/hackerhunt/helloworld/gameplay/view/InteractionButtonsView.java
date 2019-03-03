package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bristol.hackerhunt.helloworld.R;

public class InteractionButtonsView implements IInteractionButtonsView {

    private final Activity activity;

    public InteractionButtonsView(Activity activity, Runnable exchangeButtonOnClickRunnable,
                                  Runnable takedownButtonOnClickRunnable,
                                  Runnable cancelTakedownOnClickRunnable,
                                  Runnable cancelExchangeOnClickRunnable) {
        this.activity = activity;
        initializeExchangeButton(exchangeButtonOnClickRunnable);
        initializeTakeDownButton(takedownButtonOnClickRunnable);
        initializeCancelExchangeButton(cancelExchangeOnClickRunnable);
        initializeCancelTakedownButton(cancelTakedownOnClickRunnable);
    }

    private void initializeCancelExchangeButton(final Runnable runnable) {
        Button cancelExchangeButton = activity.findViewById(R.id.gameplay_exchange_select_player_button);
        cancelExchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideExchangeSelectPlayerButton();
                showInteractionButtons();
                runnable.run();
            }
        });
    }

    private void initializeCancelTakedownButton(final Runnable runnable) {
        Button cancelTakedownButton = activity.findViewById(R.id.takedown_exchange_select_player_button);
        cancelTakedownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTakedownSelectPlayerButton();
                showInteractionButtons();
                runnable.run();
            }
        });
    }

    private void initializeExchangeButton(final Runnable runnable) {
        Button exchangeButton = activity.findViewById(R.id.gameplay_exchange_button);
        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInteractionButtons();
                showExchangeSelectPlayerButton();
                runnable.run();
            }
        });
    }

    private void initializeTakeDownButton(final Runnable runnable) {
        Button takeDownButton = activity.findViewById(R.id.gameplay_takedown_button);
        takeDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInteractionButtons();
                showTakedownSelectPlayerButton();
                runnable.run();
            }
        });
    }

    private void initializeInterceptButton(final Runnable runnable) {
        Button interceptButton = activity.findViewById(R.id.gameplay_intercept_button);
        interceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInteractionButtons();
                showInterceptSelectPlayerButton();
                runnable.run();
            }
        });
    }

    @Override
    public void hideInteractionButtons() {
        LinearLayout buttons =  activity.findViewById(R.id.interaction_buttons);
        buttons.setVisibility(View.GONE);
    }

    @Override
    public void showInteractionButtons() {
        LinearLayout buttons = activity.findViewById(R.id.interaction_buttons);
        buttons.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTakedownSelectPlayerButton() {
        Button takedownSelectPlayerButton = activity.findViewById(R.id.takedown_exchange_select_player_button);
        takedownSelectPlayerButton.setVisibility(View.GONE);
    }

    @Override
    public void showTakedownSelectPlayerButton() {
        Button takedownSelectPlayerButton = activity.findViewById(R.id.takedown_exchange_select_player_button);
        takedownSelectPlayerButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideExchangeSelectPlayerButton() {
        Button takedownSelectPlayerButton = activity.findViewById(R.id.gameplay_exchange_select_player_button);
        takedownSelectPlayerButton.setVisibility(View.GONE);
    }

    @Override
    public void showExchangeSelectPlayerButton() {
        Button takedownSelectPlayerButton = activity.findViewById(R.id.gameplay_exchange_select_player_button);
        takedownSelectPlayerButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideInterceptSelectPlayerButton() {
        Button interceptSelectPlayerButton = activity.findViewById(R.id.gameplay_intercept_select_player_button);
        interceptSelectPlayerButton.setVisibility(View.GONE);
    }

    @Override
    public void showInterceptSelectPlayerButton() {
        Button interceptSelectPlayerButton = activity.findViewById(R.id.gameplay_intercept_select_player_button);
        interceptSelectPlayerButton.setVisibility(View.VISIBLE);
    }
}
