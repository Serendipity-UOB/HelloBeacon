package com.bristol.hackerhunt.helloworld.tutorial;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
        CircleProgressBar intelBar3 = findViewById(R.id.intel_bar_3).findViewById(R.id.tutorial_player_intel_circle_100);
        CircleProgressBar intelBar4 = findViewById(R.id.intel_bar_4).findViewById(R.id.tutorial_player_intel_circle_far);
        CircleProgressBar intelBar5 = findViewById(R.id.intel_bar_5).findViewById(R.id.tutorial_player_intel_circle_far);
        CircleProgressBar intelBar6 = findViewById(R.id.intel_bar_6).findViewById(R.id.tutorial_player_intel_circle_far);

        intelBar1.setProgress(100);
        intelBar2.setProgress(20);
        intelBar3.setProgress(100);

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
                restoreStatusBar();

                findViewById(R.id.welcome_to_spy_expose).setVisibility(View.GONE);
                findViewById(R.id.this_is_your_status).setVisibility(View.VISIBLE);
                break;

            case 2:
                darkenStatusBar();
                restoreTargetBox();

                findViewById(R.id.this_is_your_status).setVisibility(View.GONE);
                findViewById(R.id.this_is_your_target).setVisibility(View.VISIBLE);
                break;

            case 3:
                darkenTargetBox();
                restoreTimeLeft();

                findViewById(R.id.this_is_your_target).setVisibility(View.GONE);
                findViewById(R.id.this_is_the_remaining_game_time).setVisibility(View.VISIBLE);
                break;

            case 4:
                darkenTimeLeft();
                restoreExamplePlayerCard();

                findViewById(R.id.this_is_the_remaining_game_time).setVisibility(View.GONE);
                findViewById(R.id.this_is_another_agent).setVisibility(View.VISIBLE);
                break;

            case 5:

                findViewById(R.id.this_is_another_agent).setVisibility(View.GONE);
                findViewById(R.id.this_icon_displays_evidence).setVisibility(View.VISIBLE);
                break;

            case 6:
                findViewById(R.id.this_icon_displays_evidence).setVisibility(View.GONE);
                findViewById(R.id.codename_reveal).setVisibility(View.VISIBLE);
                break;

            case 7:
                restorePressedPlayerCard();
                restore3rdPlayerCard();

                findViewById(R.id.codename_reveal).setVisibility(View.GONE);
                findViewById(R.id.nearby_agents).setVisibility(View.VISIBLE);
                break;

            case 8:
                darkenExamplePlayerCard();
                darkenPressedPlayerCard();
                darken3rdPlayerCard();

                restoreFarAwayPlayerCards();

                findViewById(R.id.nearby_agents).setVisibility(View.GONE);
                findViewById(R.id.faraway_agents).setVisibility(View.VISIBLE);
                break;

            case 9:
                darkenExamplePlayerCard();
                darken3rdPlayerCard();
                darkenFarAwayPlayerCards();

                restorePressedPlayerCard();

                findViewById(R.id.faraway_agents).setVisibility(View.GONE);
                pressedPlayerCard.findViewById(R.id.interaction_buttons).setVisibility(View.VISIBLE);
                findViewById(R.id.press_exchange_to).setVisibility(View.VISIBLE);
                break;

            case 10:
                pressedPlayerCard.findViewById(R.id.interaction_buttons)
                        .findViewById(R.id.gameplay_exchange_button)
                        .setBackgroundResource(R.drawable.exchange_button_greyed);

                findViewById(R.id.press_exchange_to).setVisibility(View.GONE);
                findViewById(R.id.you_can_have_one_request_at_a_time).setVisibility(View.VISIBLE);
                break;

            case 11:
                pressedPlayerCard.findViewById(R.id.interaction_buttons)
                        .findViewById(R.id.gameplay_exchange_button)
                        .setBackgroundResource(R.drawable.exchange_button);

                findViewById(R.id.you_can_have_one_request_at_a_time).setVisibility(View.GONE);
                findViewById(R.id.you_can_try_intercepting).setVisibility(View.VISIBLE);
                break;

            case 12:
                pressedPlayerCard.findViewById(R.id.interaction_buttons)
                        .findViewById(R.id.gameplay_intercept_button)
                        .setBackgroundResource(R.drawable.intercept_button_greyed);

                findViewById(R.id.you_can_try_intercepting).setVisibility(View.GONE);
                findViewById(R.id.you_can_only_have_one_intercept_at_a_time).setVisibility(View.VISIBLE);
                break;

            case 13:
                pressedPlayerCard.findViewById(R.id.interaction_buttons)
                        .findViewById(R.id.gameplay_intercept_button)
                        .setBackgroundResource(R.drawable.intercept_button);

                findViewById(R.id.you_can_only_have_one_intercept_at_a_time).setVisibility(View.GONE);
                findViewById(R.id.you_can_try_exposing).setVisibility(View.VISIBLE);
                break;

            case 14:
                darkenPressedPlayerCard();
                restoreQuestionIcon();

                pressedPlayerCard.findViewById(R.id.interaction_buttons).setVisibility(View.GONE);
                findViewById(R.id.you_can_try_exposing).setVisibility(View.GONE);
                findViewById(R.id.press_question_mark_to).setVisibility(View.VISIBLE);
                break;

            case 15:
                Intent intent = new Intent(TutorialActivity.this, CreateProfileActivity.class);
                startActivity(intent);
                break;
        }
        currentStep++;
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
        // agent name:
        ((TextView) findViewById(R.id.gameplay_player_name_title))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_player_name));
        ((TextView) findViewById(R.id.gameplay_player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.gameplay_player_name));

        // stats:
        LinearLayout stats = findViewById(R.id.gameplay_player_stats);
        for (int i = 0; i < stats.getChildCount(); i++) {
            View child = stats.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(ContextCompat.getColor(this, R.color.gameplay_player_stats));
            }
        }

        ((ImageView) findViewById(R.id.current_game_zone_logo)).clearColorFilter();
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
        codename.setBackgroundColor(ContextCompat.getColor(this, R.color.player_is_target_codename_darkened));

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_100);
        circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_darkened));
        circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_text_darkened));
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
        codename.setBackgroundColor(ContextCompat.getColor(this, R.color.player_is_target_codename));

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_100);
        circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence));
        circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_text));
    }

    private void darkenPressedPlayerCard() {
        View playerCard = findViewById(R.id.player_card);

        ((TextView) playerCard.findViewById(R.id.player_name))
                .setTextColor(ContextCompat.getColor(this, R.color.player_card_name_darkened));
        ((ImageView) playerCard.findViewById(R.id.player_card_divider))
                .setImageResource(R.drawable.player_card_divider_darkened);

        playerCard.findViewById(R.id.player_item_background).setBackgroundResource(R.drawable.player_card_darkened);

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

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_100);
        circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_darkened));
        circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_text_darkened));
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

        CircleProgressBar circleProgressBar = playerCard.findViewById(R.id.tutorial_player_intel_circle_100);
        circleProgressBar.setProgressColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence));
        circleProgressBar.setTextColor(ContextCompat.getColor(this, R.color.progress_bar_complete_evidence_text));
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
}
