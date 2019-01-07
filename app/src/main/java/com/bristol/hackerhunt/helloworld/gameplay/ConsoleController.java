package com.bristol.hackerhunt.helloworld.gameplay;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;

public class ConsoleController {
    private final View overlay;
    private String homeBeacon;

    public ConsoleController(View consolePromptContainer, String homeBeacon) {
        this.overlay = consolePromptContainer;
        this.homeBeacon = homeBeacon;

        // this is only temporary.
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
            }
        });
    }

    public void goToStartBeaconPrompt() {
        TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        String message = overlay.getContext().getString(R.string.console_start_beacon_message);
        message = message.replace("$BEACON", homeBeacon);
        consoleView.setText(message);
        overlay.setVisibility(View.VISIBLE);
    }

    // todo: this function is only a placeholder, functionality needs to be overhauled.
    public void mutualExchangePrompt() {
        final TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        final String[] message = {"Scan target NFC tag."};
        consoleView.setText(message[0]);

        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message[0] = "EXCHANGE_SUCCESS";
                consoleView.setText(message[0]);
            }
        });


        overlay.setVisibility(View.VISIBLE);
    }

    // todo: this function is only a placeholder, functionality needs to be overhauled.
    public void targetTakedownPrompt() {
        final TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        final String[] message = {"Scan target NFC tag."};
        consoleView.setText(message[0]);

        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message[0] = "TAKEDOWN_SUCCESS\n\n\nReturn to $BEACON for new target.";
                message[0] = message[0].replace("$BEACON", homeBeacon);
                consoleView.setText(message[0]);
            }
        });


        overlay.setVisibility(View.VISIBLE);
    }

    public void endOfGamePrompt(final Context context, final Intent goToLeaderboardIntent) {
        final TextView consoleView = overlay.findViewById(R.id.gameplay_console);
        final String[] message = {"Incoming message...\n\nGood work. Return your equipment to the base station to collect your award.\n\n\n - Anon"};
        consoleView.setText(message[0]);

        consoleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(goToLeaderboardIntent);
            }
        });

        overlay.setVisibility(View.VISIBLE);
    }
}
