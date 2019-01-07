package com.bristol.hackerhunt.helloworld.gameplay;

import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;

public class ConsoleController {
    private final View overlay;

    public ConsoleController(View consolePromptContainer) {
        this.overlay = consolePromptContainer;

        // this is only temporary.
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
            }
        });
    }

    public void goToStartBeaconPrompt(String beacon) {
        TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        String message = overlay.getContext().getString(R.string.console_start_beacon_message);
        message = message.replace("$BEACON", beacon);
        consoleView.setText(message);
        overlay.setVisibility(View.VISIBLE);
    }
}
