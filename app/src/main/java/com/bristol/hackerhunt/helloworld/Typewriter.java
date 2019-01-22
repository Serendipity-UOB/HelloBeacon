package com.bristol.hackerhunt.helloworld;

import android.os.Handler;
import android.widget.TextView;

/**
 * Help: https://stackoverflow.com/questions/6700374/android-character-by-character-display-text-animation
 */
public class Typewriter {

    private final Handler handler;
    private final Runnable characterAdder;

    private String mText;
    private TextView textView;
    private int mIndex;
    private long mDelay; //default

    public Typewriter(long millis) {
        this.mDelay = millis;

        this.handler = new Handler();
        this.characterAdder = new Runnable() {
            @Override
            public void run() {
                textView.setText(mText.subSequence(0, mIndex++));
                if(mIndex <= mText.length()) {
                    handler.postDelayed(characterAdder, mDelay);
                }
            }
        };
    }

    public void animateText(TextView textView, String text) {
        this.textView = textView;
        this.mText = text;
        this.mIndex = 0;

        textView.setText("");
        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        this.mDelay = millis;
    }
}
