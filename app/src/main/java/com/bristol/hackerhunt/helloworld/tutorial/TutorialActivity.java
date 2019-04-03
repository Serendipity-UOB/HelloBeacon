package com.bristol.hackerhunt.helloworld.tutorial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bristol.hackerhunt.helloworld.R;
import com.emredavarci.circleprogressbar.CircleProgressBar;

public class TutorialActivity extends AppCompatActivity {

    private int currentStep;
    private View playerCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        this.currentStep = 0;
        this.playerCard = findViewById(R.id.player_card);

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
        intelBar5.setProgress(50);
        intelBar6.setProgress(50);

        findViewById(R.id.clicker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
    }

    private void next() {
        switch (currentStep) {
            case 0:
                findViewById(R.id.welcome_to_spy_expose).setVisibility(View.VISIBLE);
                break;

            case 1:
                findViewById(R.id.welcome_to_spy_expose).setVisibility(View.GONE);
                findViewById(R.id.this_is_your_status).setVisibility(View.VISIBLE);
                break;

            case 2:
                findViewById(R.id.this_is_your_status).setVisibility(View.GONE);
                findViewById(R.id.this_is_your_target).setVisibility(View.VISIBLE);
                break;

            case 3:
                findViewById(R.id.this_is_your_target).setVisibility(View.GONE);
                findViewById(R.id.this_is_the_remaining_game_time).setVisibility(View.VISIBLE);
                break;

            case 4:
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
                findViewById(R.id.codename_reveal).setVisibility(View.GONE);
                findViewById(R.id.nearby_agents).setVisibility(View.VISIBLE);
                break;

            case 8:
                findViewById(R.id.nearby_agents).setVisibility(View.GONE);
                findViewById(R.id.faraway_agents).setVisibility(View.VISIBLE);
                break;

            case 9:
                findViewById(R.id.faraway_agents).setVisibility(View.GONE);
                playerCard.findViewById(R.id.interaction_buttons).setVisibility(View.VISIBLE);
                findViewById(R.id.press_exchange_to).setVisibility(View.VISIBLE);
                break;

            case 10:
                findViewById(R.id.press_exchange_to).setVisibility(View.GONE);
                findViewById(R.id.you_can_have_one_request_at_a_time).setVisibility(View.VISIBLE);
                break;

            case 11:
                findViewById(R.id.you_can_have_one_request_at_a_time).setVisibility(View.GONE);
                findViewById(R.id.you_can_try_intercepting).setVisibility(View.VISIBLE);
                break;

            case 12:
                findViewById(R.id.you_can_try_intercepting).setVisibility(View.GONE);
                findViewById(R.id.you_can_only_have_one_intercept_at_a_time).setVisibility(View.VISIBLE);
                break;

            case 13:
                findViewById(R.id.you_can_only_have_one_intercept_at_a_time).setVisibility(View.GONE);
                findViewById(R.id.you_can_try_exposing).setVisibility(View.VISIBLE);
                break;

            case 14:
                findViewById(R.id.you_can_try_exposing).setVisibility(View.GONE);
                findViewById(R.id.press_question_mark_to).setVisibility(View.VISIBLE);
                break;

            case 15:
                findViewById(R.id.press_question_mark_to).setVisibility(View.GONE);
                break;
        }
        currentStep++;
    }
}
