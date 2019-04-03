package com.bristol.hackerhunt.helloworld.tutorial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bristol.hackerhunt.helloworld.R;
import com.emredavarci.circleprogressbar.CircleProgressBar;

public class TutorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

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
    }
}
