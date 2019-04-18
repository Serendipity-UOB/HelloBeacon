package com.bristol.hackerhunt.helloworld.tutorial;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.profileCreation.CreateProfileActivity;
import com.emredavarci.circleprogressbar.CircleProgressBar;

import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {

    private int currentStep;
    private View pressedPlayerCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        this.currentStep = 1;
        this.pressedPlayerCard = findViewById(R.id.player_card);

        CircleProgressBar intelBar1 = findViewById(R.id.intel_bar_1).findViewById(R.id.tutorial_player_intel_circle_100);
        CircleProgressBar intelBar2 = findViewById(R.id.intel_bar_2).findViewById(R.id.tutorial_player_intel_circle_25);
        CircleProgressBar intelBar3 = findViewById(R.id.intel_bar_3).findViewById(R.id.tutorial_player_intel_circle_0);
        CircleProgressBar intelBar4 = findViewById(R.id.intel_bar_4).findViewById(R.id.tutorial_player_intel_circle_far);
        CircleProgressBar intelBar5 = findViewById(R.id.intel_bar_5).findViewById(R.id.tutorial_player_intel_circle_far);
        CircleProgressBar intelBar6 = findViewById(R.id.intel_bar_6).findViewById(R.id.tutorial_player_intel_circle_far);

        intelBar1.setProgress(0);
        intelBar1.setText("0");
        findViewById(R.id.player_card_1).findViewById(R.id.player_hacker_name).setVisibility(View.INVISIBLE);

        intelBar2.setProgress(0);
        intelBar2.setText("0");

        intelBar4.setProgress(50);
        intelBar4.setBackgroundColor(ContextCompat.getColor(this,R.color.progress_bar_background_far_darkened));
        intelBar5.setProgress(50);
        intelBar5.setBackgroundColor(ContextCompat.getColor(this,R.color.progress_bar_background_far_darkened));
        intelBar6.setProgress(50);
        intelBar6.setBackgroundColor(ContextCompat.getColor(this,R.color.progress_bar_background_far_darkened));

        findViewById(R.id.clicker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });

        darkenStatusBar();
        darkenTargetBox();
        darkenTimeLeft();
        darkenExamplePlayerCard();
        darkenPressedPlayerCard();
        darken3rdPlayerCard();
        darkenFarAwayPlayerCards();
        darkenBackground();

        findViewById(R.id.welcome_to_spy_expose).setVisibility(View.VISIBLE);
    }

    private void next() {
        switch (currentStep) {
            case 0:
                /* Welcome to SpyWhere */
                darkenStatusBar();
                darkenTargetBox();
                darkenTimeLeft();
                darkenExamplePlayerCard();
                darkenPressedPlayerCard();
                darken3rdPlayerCard();
                darkenFarAwayPlayerCards();
                darkenBackground();

                findViewById(R.id.welcome_to_spy_expose).setVisibility(View.VISIBLE);
                break;

            case 1:
                /* This is your position and reputation */
                restoreStatusBar();

                findViewById(R.id.welcome_to_spy_expose).setVisibility(View.GONE);
                findViewById(R.id.this_is_your_position_and_reputation).setVisibility(View.VISIBLE);
                break;

            case 2:
                /* This is your target */
                darkenStatusBar();
                restoreTargetBox();

                findViewById(R.id.this_is_your_position_and_reputation).setVisibility(View.GONE);
                findViewById(R.id.this_is_your_target).setVisibility(View.VISIBLE);
                break;

            case 3:
                /* This is the remaining game time */
                darkenTargetBox();
                restoreTimeLeft();

                findViewById(R.id.this_is_your_target).setVisibility(View.GONE);
                findViewById(R.id.this_is_the_remaining_game_time).setVisibility(View.VISIBLE);
                break;

            case 4:
                /* This is another agent */
                darkenTimeLeft();
                restoreExamplePlayerCard();

                findViewById(R.id.this_is_the_remaining_game_time).setVisibility(View.GONE);
                findViewById(R.id.this_is_another_agent).setVisibility(View.VISIBLE);
                break;

            case 5:
                /* This is the evidence bar */
                darkenExamplePlayerCard();
                restoreExamplePlayerCardEvidence();

                findViewById(R.id.this_is_another_agent).setVisibility(View.GONE);
                findViewById(R.id.this_icon_displays_evidence).setVisibility(View.VISIBLE);
                break;

            case 6:
                /* If you have 100 evidence, the agent's codename is revealed */
                CircleProgressBar bar = findViewById(R.id.player_card_1).findViewById(R.id.tutorial_player_intel_circle_100);
                bar.setProgress(100);
                bar.setText("100");
                restoreExamplePlayerCardEvidence();

                findViewById(R.id.this_icon_displays_evidence).setVisibility(View.GONE);
                findViewById(R.id.codename_reveal).setVisibility(View.VISIBLE);
                break;

            case 7:
                /* These are your nearby agents */
                restoreExamplePlayerCard();
                restorePressedPlayerCard();
                restore3rdPlayerCard();

                disableOnTapProgression();
                pressedPlayerCard.setOnClickListener(nextOnClickListener());

                findViewById(R.id.codename_reveal).setVisibility(View.GONE);
                findViewById(R.id.nearby_agents).setVisibility(View.VISIBLE);
                break;

            case 8:
                /* Tap exchange to send an exchange request to Tilly */
                darkenExamplePlayerCard();
                darken3rdPlayerCard();
                darkenFarAwayPlayerCards();

                restorePressedPlayerCard();
                openInteractionButtons();

                pressedPlayerCard.setOnClickListener(null);
                pressedPlayerCard.findViewById(R.id.gameplay_exchange_button).setOnClickListener(nextOnClickListener());

                findViewById(R.id.nearby_agents).setVisibility(View.GONE);
                findViewById(R.id.press_exchange_to).setVisibility(View.VISIBLE);

                break;

            case 9:
                /* You can only have one exchange request active at a time */
                pressedExchangeButton();

                pressedPlayerCard.findViewById(R.id.gameplay_exchange_button).setOnClickListener(null);
                enableOnTapProgression();

                findViewById(R.id.press_exchange_to).setVisibility(View.GONE);
                findViewById(R.id.you_can_have_one_request_at_a_time).setVisibility(View.VISIBLE);

                break;

            case 10:
                /* You've gained evidence from your exchange request */
                exchangeRequestComplete();
                closeInteractionButtons();

                restoreExamplePlayerCard();
                restore3rdPlayerCard();

                increaseTillyEvidence(25);
                increaseLouisEvidence(10);

                findViewById(R.id.you_can_have_one_request_at_a_time).setVisibility(View.GONE);
                findViewById(R.id.you_gained_evidence_from_exchange).setVisibility(View.VISIBLE);
                break;

            case 11:
                /* Tilly requested an exchange; Accept it! */
                disableOnTapProgression();

                pressedPlayerCard.setOnClickListener(null);
                pressedPlayerCard.findViewById(R.id.gameplay_exchange_button).setOnClickListener(nextOnClickListener());

                findViewById(R.id.exchange_request_overlay).setVisibility(View.VISIBLE);
                findViewById(R.id.accept_exchange_button).setOnClickListener(nextOnClickListener());

                findViewById(R.id.you_gained_evidence_from_exchange).setVisibility(View.GONE);
                findViewById(R.id.Tilly_requested_an_exchange).setVisibility(View.VISIBLE);
                break;

            case 12:
                /* You gained more evidence! */
                findViewById(R.id.accept_exchange_button).setOnClickListener(null);
                findViewById(R.id.exchange_request_overlay).setVisibility(View.GONE);
                enableOnTapProgression();

                increaseTillyEvidence(25);
                increaseLouisEvidence(10);

                findViewById(R.id.Tilly_requested_an_exchange).setVisibility(View.GONE);
                findViewById(R.id.you_gained_evidence_from_exchange).setVisibility(View.VISIBLE);
                break;

            case 13:
                /* Tilly is interacting with Louis; now is your change to run an Intercept! */
                disableOnTapProgression();
                pressedPlayerCard.setOnClickListener(nextOnClickListener());

                findViewById(R.id.you_gained_evidence_from_exchange).setVisibility(View.GONE);
                findViewById(R.id.Tilly_is_interacting_with_Louis).setVisibility(View.VISIBLE);
                break;

            case 14:
                /* Tap intercept */
                darkenExamplePlayerCard();
                darken3rdPlayerCard();
                openInteractionButtons();

                pressedPlayerCard.setOnClickListener(null);
                pressedPlayerCard.findViewById(R.id.gameplay_exchange_button).setOnClickListener(null);
                pressedPlayerCard.findViewById(R.id.gameplay_intercept_button).setOnClickListener(nextOnClickListener());

                findViewById(R.id.Tilly_is_interacting_with_Louis).setVisibility(View.GONE);
                findViewById(R.id.tap_intercept).setVisibility(View.VISIBLE);
                break;

            case 15:
                /* Only one intercept may be active at a time */
                pressedInterceptButton();

                pressedPlayerCard.findViewById(R.id.gameplay_intercept_button).setOnClickListener(null);
                enableOnTapProgression();

                findViewById(R.id.tap_intercept).setVisibility(View.GONE);
                findViewById(R.id.one_intercept_at_a_time).setVisibility(View.VISIBLE);
                break;

            case 16:
                /* Intercept on Tilly and Louis was successful */
                interceptAttemptComplete();

                closeInteractionButtons();

                restore3rdPlayerCard();
                restoreExamplePlayerCard();
                restorePressedPlayerCard();

                increaseTillyEvidence(50);
                increaseLouisEvidence(10);

                findViewById(R.id.one_intercept_at_a_time).setVisibility(View.GONE);
                findViewById(R.id.intercept_successful).setVisibility(View.VISIBLE);
                break;

            case 17:
                /* Your target's codename has been revealed */
                pressedPlayerCard.setOnClickListener(nextOnClickListener());
                disableOnTapProgression();

                findViewById(R.id.intercept_successful).setVisibility(View.GONE);
                findViewById(R.id.your_target_has_been_exposed).setVisibility(View.VISIBLE);
                break;

            case 18:
                /* Click Expose! */
                pressedPlayerCard.setOnClickListener(null);
                openInteractionButtons();

                findViewById(R.id.gameplay_expose_button).setOnClickListener(nextOnClickListener());

                findViewById(R.id.your_target_has_been_exposed).setVisibility(View.GONE);
                findViewById(R.id.expose_your_target).setVisibility(View.VISIBLE);
                break;

            case 19:
                /* Expose successful */
                closeInteractionButtons();
                releaseTillyEvidence();

                restoreExamplePlayerCard();
                restorePressedPlayerCard();
                restore3rdPlayerCard();

                enableOnTapProgression();

                findViewById(R.id.expose_your_target).setVisibility(View.GONE);
                findViewById(R.id.expose_was_successful).setVisibility(View.VISIBLE);
                break;

            case 20:
                /* Good luck my doods */
                darkenExamplePlayerCard();
                darkenPressedPlayerCard();
                darken3rdPlayerCard();

                findViewById(R.id.expose_was_successful).setVisibility(View.GONE);
                findViewById(R.id.good_luck).setVisibility(View.VISIBLE);
                break;

            case 21:
                Intent intent = new Intent(TutorialActivity.this, CreateProfileActivity.class);
                startActivity(intent);
                break;
        }
        currentStep++;
    }

    private View.OnClickListener nextOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        };
    }

    private void darkenBackground() {
        View background = findViewById(R.id.gameplay_background);
        background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.gameplay_background_darkened));
    }

    void restoreQuestionIcon() {
        ((ImageView) findViewById(R.id.current_game_zone_logo)).clearColorFilter();
    }

    private void darkenStatusBar() {
        // agent name:
        ((TextView) findViewById(R.id.gameplay_player_name_title))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_player_name_darkened));
        ((TextView) findViewById(R.id.gameplay_player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_player_name_darkened));

        // stats:
        LinearLayout stats = findViewById(R.id.gameplay_player_stats);
        for (int i = 0; i < stats.getChildCount(); i++) {
            View child = stats.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(ContextCompat.getColor(this, R.color.gameplay_player_stats_darkened));
            }
        }

        // game zone logo:
        ((ImageView) findViewById(R.id.current_game_zone_logo))
               .setColorFilter(ContextCompat.getColor(this, R.color.player_card_name_darkened), PorterDuff.Mode.MULTIPLY);
    }

    private void restoreStatusBar() {
        // stats:
        LinearLayout stats = findViewById(R.id.gameplay_player_stats);
        for (int i = 0; i < stats.getChildCount(); i++) {
            View child = stats.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(ContextCompat.getColor(this, R.color.gameplay_player_stats));
            }
        }

        //((ImageView) findViewById(R.id.current_game_zone_logo)).clearColorFilter();
    }

    private void darkenTargetBox() {
        View targetWrapper = findViewById(R.id.status_target_wrapper);

        targetWrapper.setBackgroundResource(R.drawable.target_status_bar_border_darkened);
        ((TextView) targetWrapper.findViewById(R.id.status_target_prefix))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_target_prefix_darkened));
        ((TextView) targetWrapper.findViewById(R.id.gameplay_player_target))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_target_darkened));
    }

    private void restoreTargetBox() {
        View targetWrapper = findViewById(R.id.status_target_wrapper);

        targetWrapper.setBackgroundResource(R.drawable.target_status_bar_border);
        ((TextView) targetWrapper.findViewById(R.id.status_target_prefix))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_target_prefix));
        ((TextView) targetWrapper.findViewById(R.id.gameplay_player_target))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_target));
    }

    private void darkenTimeLeft() {
        View timeWrapper = findViewById(R.id.status_time_wrapper);
        timeWrapper.setBackgroundResource(R.drawable.time_status_bar_border_darkened);
        ((TextView) timeWrapper.findViewById(R.id.gameplay_time_left))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_time_left_darkened));
    }

    private void restoreTimeLeft() {
        View timeWrapper = findViewById(R.id.status_time_wrapper);
        timeWrapper.setBackgroundResource(R.drawable.time_status_bar_border);
        ((TextView) timeWrapper.findViewById(R.id.gameplay_time_left))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_time_left));
    }

    private void darkenExamplePlayerCard() {
        View playerCard = findViewById(R.id.player_card_1);

        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name_darkened));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider_darkened);

        playerCard.findViewById(R.id.player_item_background).setBackgroundResource(R.drawable.player_card_darkened);

        TextView codename = playerCard.findViewById(R.id.player_hacker_name);
        codename.setTextColor(ContextCompat.getColor(this, R.color.player_card_target_codename_text_darkened));
        codename.setBackgroundColor(ContextCompat.getColor(this, R.color.player_is_not_target_codename_darkened));

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_100);
        if (circleProgressBar.getProgress() >= 100) {
            circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_darkened));
            circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_text_darkened));
        }
        else {
            circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_darkened));
            circleProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.progress_bar_background_darkened));
            circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_text_darkened));
        }
    }

    private void restoreExamplePlayerCard() {
        View playerCard = findViewById(R.id.player_card_1);

        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider);

        playerCard.findViewById(R.id.player_item_background).setBackgroundResource(R.drawable.player_card);

        TextView codename = playerCard.findViewById(R.id.player_hacker_name);
        codename.setTextColor(ContextCompat.getColor(this, R.color.player_card_target_codename_text));
        codename.setBackgroundColor(ContextCompat.getColor(this, R.color.player_is_not_target_codename));

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_100);
        if (circleProgressBar.getProgress() >= 100) {
            circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence));
            circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_text));
        }
        else {
            circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar));
            circleProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.progress_bar_background));
            circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_text));
        }
    }

    private void restoreExamplePlayerCardEvidence() {
        View playerCard = findViewById(R.id.player_card_1);

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_100);
        if (circleProgressBar.getProgress() >= 100) {
            circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence));
            circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_text));

            TextView codename = playerCard.findViewById(R.id.player_hacker_name);
            codename.setBackgroundColor(ContextCompat.getColor(this, R.color.player_is_not_target_codename));
            codename.setTextColor(ContextCompat.getColor(this, R.color.player_card_target_codename_text));
            codename.setVisibility(View.VISIBLE);
        }
        else {
            circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar));
            circleProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.progress_bar_background));
            circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_text));
        }
    }

    private void darkenPressedPlayerCard() {
        View playerCard = findViewById(R.id.player_card);

        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name_darkened));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider_darkened);

        playerCard.findViewById(R.id.player_item_background).setBackgroundResource(R.drawable.player_card_darkened);

        TextView codename = playerCard.findViewById(R.id.player_hacker_name);
        codename.setTextColor(ContextCompat.getColor(this, R.color.player_card_target_codename_text_darkened));
        codename.setBackgroundColor(ContextCompat.getColor(this, R.color.player_is_not_target_codename_darkened));

        ((TextView) playerCard.findViewById(R.id.exchange_requested_text))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name_darkened));
        ((ImageView) playerCard.findViewById(R.id.exchange_requested_icon))
                .setColorFilter(ContextCompat.getColor(this, R.color.player_card_name_darkened), PorterDuff.Mode.MULTIPLY);

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_25);
        circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_darkened));
        circleProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.progress_bar_background_darkened));
        circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_darkened));
    }

    private void restorePressedPlayerCard() {
        View playerCard = findViewById(R.id.player_card);

        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider);

        playerCard.findViewById(R.id.player_item_background).setBackgroundResource(R.drawable.player_card);

        ((TextView) playerCard.findViewById(R.id.exchange_requested_text))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name));
        ((ImageView) playerCard.findViewById(R.id.exchange_requested_icon)).clearColorFilter();

        TextView codename = playerCard.findViewById(R.id.player_hacker_name);
        codename.setTextColor(ContextCompat.getColor(this, R.color.player_card_target_codename_text));

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_25);
        circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar));
        circleProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.progress_bar_background));
        circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_text));
    }

    private void darken3rdPlayerCard() {
        View playerCard = findViewById(R.id.player_card_3);

        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name_darkened));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider_darkened);

        playerCard.findViewById(R.id.player_item_background).setBackgroundResource(R.drawable.player_card_darkened);

        TextView codename = playerCard.findViewById(R.id.player_hacker_name);
        codename.setTextColor(ContextCompat.getColor(this, R.color.player_card_target_codename_text_darkened));
        codename.setBackgroundColor(ContextCompat.getColor(this, R.color.player_is_not_target_codename_darkened));

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_0);
        circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_darkened));
        circleProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.progress_bar_background_darkened));
        circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_text_darkened));
    }

    private void restore3rdPlayerCard() {
        View playerCard = findViewById(R.id.player_card_3);

        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider);

        playerCard.findViewById(R.id.player_item_background).setBackgroundResource(R.drawable.player_card);

        TextView codename = playerCard.findViewById(R.id.player_hacker_name);
        codename.setTextColor(ContextCompat.getColor(this, R.color.player_card_target_codename_text));
        codename.setBackgroundColor(ContextCompat.getColor(this, R.color.player_is_not_target_codename));

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_0);
        circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar));
        circleProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.progress_bar_background));
        circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_text));
    }

    private void darkenFarAwayPlayerCards() {
        List<View> playerCards = new ArrayList<>();
        playerCards.add(findViewById(R.id.player_card_4));
        playerCards.add(findViewById(R.id.player_card_5));
        playerCards.add(findViewById(R.id.player_card_6));

        for (View playerCard : playerCards) {
            darkenFarAwayPlayerCard(playerCard);
        }
    }

    private void darkenFarAwayPlayerCard(View playerCard) {
        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name_darkened));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider_darkened);

        View background = playerCard.findViewById(R.id.player_item_background);
        background.setBackgroundResource(R.drawable.player_card_far_darkened);

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_far);
        int progressBarColor = ContextCompat.getColor(this, R.color.progress_bar_far_darkened);
        int progressBarBackgroundColor = ContextCompat.getColor(this, R.color.progress_bar_background_far_darkened);
        int progressBarTextColor = ContextCompat.getColor(this, R.color.progress_bar_text_far_darkened);
        circleProgressBar.setProgressColor(progressBarColor);
        circleProgressBar.setBackgroundColor(progressBarBackgroundColor);
        circleProgressBar.setTextColor(progressBarTextColor);
    }

    private void restoreFarAwayPlayerCards() {
        List<View> playerCards = new ArrayList<>();
        playerCards.add(findViewById(R.id.player_card_4));
        playerCards.add(findViewById(R.id.player_card_5));
        playerCards.add(findViewById(R.id.player_card_6));

        for (View playerCard : playerCards) {
            restoreFarAwayPlayerCard(playerCard);
        }
    }

    private void restoreFarAwayPlayerCard(View playerCard) {
        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider);

        View background = playerCard.findViewById(R.id.player_item_background);
        background.setBackgroundResource(R.drawable.player_card_far);

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_far);
        int progressBarColor = ContextCompat.getColor(this, R.color.progress_bar_far);
        int progressBarBackgroundColor = ContextCompat.getColor(this, R.color.progress_bar_background_far);
        int progressBarTextColor = ContextCompat.getColor(this, R.color.progress_bar_text_far);
        circleProgressBar.setProgressColor(progressBarColor);
        circleProgressBar.setBackgroundColor(progressBarBackgroundColor);
        circleProgressBar.setTextColor(progressBarTextColor);
    }

    private void disableOnTapProgression() {
        findViewById(R.id.clicker).setOnClickListener(null);
        findViewById(R.id.clicker).setVisibility(View.GONE);
    }

    private void enableOnTapProgression() {
        findViewById(R.id.clicker).setVisibility(View.VISIBLE);
        findViewById(R.id.clicker).setOnClickListener(nextOnClickListener());
    }

    private void openInteractionButtons() {
        pressedPlayerCard.findViewById(R.id.interaction_buttons).setVisibility(View.VISIBLE);
    }

    private void closeInteractionButtons() {
        pressedPlayerCard.findViewById(R.id.interaction_buttons).setVisibility(View.GONE);
    }

    private void pressedExchangeButton() {
        pressedPlayerCard.findViewById(R.id.interaction_buttons)
                .findViewById(R.id.gameplay_exchange_button)
                .setBackgroundResource(R.drawable.exchange_button_greyed);

        pressedPlayerCard.findViewById(R.id.exchange_requested).setVisibility(View.VISIBLE);
    }

    private void exchangeRequestComplete() {
        pressedPlayerCard.findViewById(R.id.interaction_buttons)
                .findViewById(R.id.gameplay_exchange_button)
                .setBackgroundResource(R.drawable.exchange_button);

        pressedPlayerCard.findViewById(R.id.exchange_requested).setVisibility(View.INVISIBLE);
    }

    private void pressedInterceptButton() {
        pressedPlayerCard.findViewById(R.id.interaction_buttons)
                .findViewById(R.id.gameplay_intercept_button)
                .setBackgroundResource(R.drawable.intercept_button_greyed);
    }

    private void interceptAttemptComplete() {
        pressedPlayerCard.findViewById(R.id.interaction_buttons)
                .findViewById(R.id.gameplay_intercept_button)
                .setBackgroundResource(R.drawable.intercept_button);
    }

    private void increaseTillyEvidence(int evidence) {
        CircleProgressBar bar = pressedPlayerCard.findViewById(R.id.tutorial_player_intel_circle_25);
        increaseEvidence(bar, evidence);
    }

    private void releaseTillyEvidence() {
        final CircleProgressBar bar = pressedPlayerCard.findViewById(R.id.tutorial_player_intel_circle_25);

        final Context context = this;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bar.getProgress() > 0) {
                    bar.setProgress(Math.max(0, bar.getProgress() - 2));
                    bar.setText(Integer.toString((int) bar.getProgress()));
                    handler.postDelayed(this, 25);
                }
                else {
                    pressedPlayerCard.findViewById(R.id.player_hacker_name)
                            .setBackgroundColor(ContextCompat.getColor(context, R.color.player_is_not_target_codename));
                }
            }
        }, 25);
    }

    private void increaseLouisEvidence(int evidence) {
        CircleProgressBar bar = findViewById(R.id.player_card_3).findViewById(R.id.tutorial_player_intel_circle_0);
        increaseEvidence(bar, evidence);
    }

    private void increaseEvidence(final CircleProgressBar bar, final int evidence) {
        final float currentProgress = bar.getProgress();
        bar.setText("+" + Integer.toString(evidence));
        bar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_increase));

        final Context context = this;
        final int finalEvidence = (int) currentProgress + evidence;

        final Handler increaseHandler = new Handler();
        increaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bar.getProgress() < finalEvidence) {
                    bar.setProgress(Math.min(finalEvidence, bar.getProgress() + 2));
                    increaseHandler.postDelayed(this, 25);
                }
                else if (finalEvidence >=100) {
                    bar.setProgressColor(ContextCompat.getColor(context, R.color.progress_bar_complete_evidence));
                    pressedPlayerCard.findViewById(R.id.player_hacker_name).setVisibility(View.VISIBLE);
                    pressedPlayerCard.findViewById(R.id.player_hacker_name)
                            .setBackgroundColor(ContextCompat.getColor(context, R.color.player_is_target_codename));
                }
            }
        }, 25);

        final Handler restoreTextHandler = new Handler();
        restoreTextHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (finalEvidence >= 100) {
                    bar.setTextColor(ContextCompat.getColor(context, R.color.progress_bar_complete_evidence_text));
                }
                else {
                    bar.setTextColor(ContextCompat.getColor(context, R.color.progress_bar_text));
                }
                bar.setText(Integer.toString(finalEvidence));

            }
        }, 1000);
    }
}
