package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.Typewriter;

public class ConsoleView implements IConsoleView {

    private static final int TYPEWRITER_SPEED = 10;     // given in milliseconds.
    private static final long MISSION_DURATION = 30000;

    private final View overlay;
    private final View consoleView;
    private final View consoleWrapper;
    private final TextView consoleViewTitle;
    private final TextView consoleViewText;
    private final TextView consoleTapToCloseMessage;

    // status flags
    private String currentHomeBeacon = "";
    private boolean interactionInProgress;
    private boolean playerGotTakenDownInProgress;
    private boolean playersTargetGotTakenDownInProgress;
    private boolean takedownSuccessInProgress;

    private boolean firstMission = true;

    private CountDownTimer cTimer = null;

    public ConsoleView(View consolePromptContainer) {
        this.overlay = consolePromptContainer;
        this.consoleView = overlay.findViewById(R.id.gameplay_console);
        this.consoleViewTitle = overlay.findViewById(R.id.full_pop_up_title);
        this.consoleViewText = overlay.findViewById(R.id.gameplay_console_text);
        this.consoleTapToCloseMessage = overlay.findViewById(R.id.console_close_message);
        this.consoleWrapper = overlay.findViewById(R.id.full_pop_up_wrapper);

        this.interactionInProgress = false;
        this.playerGotTakenDownInProgress = false;
        this.playersTargetGotTakenDownInProgress = false;
        this.takedownSuccessInProgress = false;

        enableCloseConsole();
    }

    private void resetConsoleInProgressFlags() {
        playerGotTakenDownInProgress = false;
        playersTargetGotTakenDownInProgress = false;
        takedownSuccessInProgress = false;
    }

    private void enableCloseConsole() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
                resetConsoleInProgressFlags();
            }
        };

        setCloseOnClickListener(listener);
        consoleTapToCloseMessage.setVisibility(View.VISIBLE);
    }

    private void enableCloseConsoleWithoutOverride() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
                if (playerGotTakenDownInProgress) {
                    playerGotTakenDownPrompt(currentHomeBeacon);
                }
                else if (playersTargetGotTakenDownInProgress) {
                    playersTargetTakenDownPrompt(currentHomeBeacon);
                }
                else if (takedownSuccessInProgress) {
                    exposeSuccessPrompt(currentHomeBeacon);
                }
                else {
                    overlay.setVisibility(View.GONE);
                }
            }
        };

        setCloseOnClickListener(listener);
        consoleTapToCloseMessage.setVisibility(View.VISIBLE);
    }

    void setCloseOnClickListener(View.OnClickListener listener) {
        overlay.setOnClickListener(listener);
        consoleView.setOnClickListener(listener);
        consoleViewText.setOnClickListener(listener);
    }

    private void disableCloseConsole() {
        setCloseOnClickListener(null);
        consoleTapToCloseMessage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void goToStartBeaconPrompt(String homeBeaconName) {
        disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        setNeutralConsole();

        setConsoleTitle(R.string.console_start_title);
        goToStartBeaconConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    @Override
    public void applicationError() {
        enableCloseConsole();
        setNeutralConsole();

        setConsoleTitle(R.string.console_error_title);
        setConsoleMessage("Application error, please try again later.");
    }

    private void setNeutralConsole() {
        consoleViewTitle.setTextColor(consoleView.getResources()
                .getColor(R.color.neutral_full_screen_notif_title));
        consoleWrapper.setBackgroundResource(R.drawable.neutral_full_pop_up);
    }

    private void setGoodConsole() {
        consoleViewTitle.setTextColor(consoleView.getResources()
                .getColor(R.color.good_full_screen_notif_title));
        consoleWrapper.setBackgroundResource(R.drawable.good_full_pop_up);
    }

    private void setBadConsole() {
        consoleViewTitle.setTextColor(consoleView.getResources()
                .getColor(R.color.bad_full_screen_notif_title));
        consoleWrapper.setBackgroundResource(R.drawable.bad_full_pop_up);
    }

    private void goToStartBeaconConsoleMessage(String homeBeaconName) {
        String message = overlay.getContext().getString(R.string.console_start_beacon_message);
        message = message.replace("$BEACON", homeBeaconName);
        setConsoleMessage(message);
    }

    @Override
    public void playersTargetTakenDownPrompt(String homeBeaconName) {
        disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        this.playersTargetGotTakenDownInProgress = true;
        setNeutralConsole();
        setConsoleTitle(R.string.console_target_taken_down_title);
        playersTargetGotTakenDownConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    private void setConsoleTitle(int id) {
        String title = consoleView.getResources().getString(id);
        consoleViewTitle.setText(title);
    }

    private void playersTargetGotTakenDownConsoleMessage(String homeBeaconName) {
        String message = consoleView.getResources()
                .getString(R.string.console_target_taken_down_message);
        message = message.replace("$BEACON", homeBeaconName);
        setConsoleMessage(message);
    }

    @Override
    public void playerGotTakenDownPrompt(String homeBeaconName) {
        disableCloseConsole();

        this.playerGotTakenDownInProgress = true;
        this.currentHomeBeacon = homeBeaconName;
        setBadConsole();
        setConsoleTitle(R.string.console_taken_down_title);
        playerTakenDownConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    private void playerTakenDownConsoleMessage(String homeBeaconName) {
        String message = consoleView.getResources()
                .getString(R.string.console_taken_down_message);
        message = message.replace("$BEACON", homeBeaconName);
        setConsoleMessage(message);
    }

    @Override
    public void endOfGamePrompt(final Context context, final Intent goToLeaderboardIntent) {
        disableCloseConsole();

        setNeutralConsole();
        setConsoleMessage(context.getString(R.string.game_over_console_message));
        setConsoleTitle(R.string.game_over_console_title);

        this.interactionInProgress = false;

        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("App", "Going to leaderboard.");
                context.startActivity(goToLeaderboardIntent);
            }
        };
        consoleViewText.setOnClickListener(cl);
        consoleView.setOnClickListener(cl);
    }

    @Override
    public void missionUpdatePrompt(String missionStatement) {
        disableCloseConsole();
        setNeutralConsole();
        setConsoleMessage(missionStatement);
        setConsoleTitle(R.string.mission_update_title);
        if(!firstMission) {
            startMissionTimer();
            firstMission = false;
        }
    }

    @Override
    public void missionSuccessPrompt(String missionSuccessMessage) {
        enableCloseConsole();
        setGoodConsole();
        setConsoleMessage(missionSuccessMessage);
        setConsoleTitle(R.string.mission_success_title);
    }

    @Override
    public void missionFailedPrompt(String missionFailedMessage) {
        enableCloseConsole();
        setBadConsole();
        setConsoleMessage(missionFailedMessage);
        setConsoleTitle(R.string.mission_failed_title);
    }

    @Override
    public void setConsoleFlag(int flag){
        ImageView iv = consoleView.findViewById(R.id.agency_logo);
        int imageId = R.drawable.beacon_valor;
        if(flag == 0){
            imageId = R.drawable.un_flag;
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
        iv.setImageResource(imageId);
    }

    private void startMissionTimer(){
        final TextView tv = consoleView.findViewById(R.id.mission_countdown);

        cTimer = new CountDownTimer(MISSION_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv.setText(Long.toString(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                tv.setText("");
                // v potential NPE inducing v
                wipeTimer();
            }
        };


        cTimer.start();
    }

    private void wipeTimer(){
        cTimer = null;
    }

    @Override
    public void exposeSuccessPrompt(String homeBeaconName) {
        disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        this.takedownSuccessInProgress = true;

        setGoodConsole();
        setConsoleTitle(R.string.console_expose_success_title);
        String message = consoleView.getResources()
                .getString(R.string.console_expose_success_message);
        message = message.replace("$BEACON", homeBeaconName);
        setConsoleMessage(message);

        this.interactionInProgress = false;
    }

    private void setConsoleMessage(String message) {
        consoleViewText.setText(message);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void closeConsole() {
        if (overlay.getVisibility() != View.GONE && !interactionInProgress) {
            overlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void enableTapToClose()  {
        enableCloseConsole();
    }
}
