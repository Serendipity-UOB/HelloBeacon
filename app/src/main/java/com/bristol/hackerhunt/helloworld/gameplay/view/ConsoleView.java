package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.app.Activity;
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
    private static final long MISSION_DURATION = 15000;

    private final View overlay;
    private final View consoleView;
    private final View consoleWrapper;
    private final TextView consoleViewTitle;
    private final TextView consoleViewText;
    private final TextView consoleTapToCloseMessage;
    private final ImageView consoleViewImage;
    private final TextView consoleViewCountdown;

    private final Activity caller;

    // status flags
    private String currentHomeBeacon = "";
    private boolean interactionInProgress;
    private boolean playerGotTakenDownInProgress;
    private boolean playersTargetGotTakenDownInProgress;
    private boolean takedownSuccessInProgress;

    private boolean notFirstMission = false;

    private CountDownTimer cTimer = null;

    public ConsoleView(View consolePromptContainer, Activity caller) {
        this.overlay = consolePromptContainer;
        this.caller = caller;
        this.consoleView = overlay.findViewById(R.id.gameplay_console);
        this.consoleViewTitle = overlay.findViewById(R.id.full_pop_up_title);
        this.consoleViewText = overlay.findViewById(R.id.gameplay_console_text);
        this.consoleTapToCloseMessage = overlay.findViewById(R.id.console_close_message);
        this.consoleWrapper = overlay.findViewById(R.id.full_pop_up_wrapper);
        this.consoleViewImage = overlay.findViewById(R.id.agency_logo);
        this.consoleViewCountdown = overlay.findViewById(R.id.mission_countdown);

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
        hideTimer();
        disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        setNeutralConsole();

        setConsoleTitle(R.string.console_start_title);
        goToStartBeaconConsoleMessage(homeBeaconName);
        this.interactionInProgress = false;
    }

    @Override
    public void applicationError() {
        hideTimer();
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
    public void setConsoleImage(int res){
        consoleViewImage.setImageResource(res);
    }

    @Override
    public void playersTargetTakenDownPrompt(String homeBeaconName) {
        hideTimer();
        disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        this.playersTargetGotTakenDownInProgress = true;
        setNeutralConsole();
        setConsoleTitle(R.string.console_target_taken_down_title);
        setConsoleImage(R.drawable.un_flag_small);
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
        hideTimer();

        disableCloseConsole();

        this.playerGotTakenDownInProgress = true;
        this.currentHomeBeacon = homeBeaconName;
        setBadConsole();
        setConsoleTitle(R.string.console_taken_down_title);
        setConsoleImage(R.drawable.un_flag_small);
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
        hideTimer();

        disableCloseConsole();

        setNeutralConsole();
        setConsoleMessage(context.getString(R.string.game_over_console_message));
        setConsoleTitle(R.string.game_over_console_title);
        setConsoleImage(R.drawable.un_flag_small);


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
        hideTimer();

        disableCloseConsole();
        setNeutralConsole();
        setConsoleMessage(missionStatement);
        setConsoleTitle(R.string.mission_update_title);
        setConsoleImage(getConsoleFlag(getFlagFromMission(missionStatement)));
        if(notFirstMission) {
            if(!missionStatement.toLowerCase().contains("was last seen in")) {
                startMissionTimer();
            }
        }
        else{
            notFirstMission = true;
        }
    }

    @Override
    public void missionSuccessPrompt(String missionSuccessMessage) {
        hideTimer();

        enableCloseConsole();
        setGoodConsole();
        setConsoleMessage(missionSuccessMessage);
        setConsoleTitle(R.string.mission_success_title);
        Log.d("Mission Flag", Integer.toString(getFlagFromMission(missionSuccessMessage)));
        setConsoleImage(getConsoleFlag(getFlagFromMission(missionSuccessMessage)));
    }

    @Override
    public void missionFailedPrompt(String missionFailedMessage) {
        hideTimer();

        enableCloseConsole();
        setBadConsole();
        setConsoleMessage(missionFailedMessage);
        setConsoleTitle(R.string.mission_failed_title);

        int res = getConsoleFlag(getFlagFromMission(missionFailedMessage));
        setConsoleImage(res);
    }

    @Override
    public int getConsoleFlag(int flag){
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

    private int getFlagFromMission(String details){
        //Default to UN Flag
        int flag = 0;
        String lowerDetails = details.toLowerCase();
        if(lowerDetails.contains("italy")){
            flag = 1;
        }
        else if(lowerDetails.contains("sweden")){
            flag = 2;
        }
        else if(lowerDetails.contains("switzerland")){
            flag = 3;
        }
        else if(lowerDetails.contains("czech republic")){
            flag = 4;
        }
        return flag;
    }

    private CountDownTimer missionTimer(){

        return new CountDownTimer(15*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String formattedTime = formatTime(millisUntilFinished);
                updateMissionTimer(formattedTime);
            }

            @Override
            public void onFinish() {
                updateMissionTimer("00");
            }
        };
    }

    private String formatTime(long milliseconds) {
        Long seconds = (milliseconds) / 1000;
        String formattedTime = "";
        if (seconds < 10) formattedTime = formattedTime + "0";
        return formattedTime + seconds.toString();
    }

    private void updateMissionTimer(final String time) {
        consoleViewCountdown.setText(time);
    }

    private void startMissionTimer(){
        showTimer();
        missionTimer().start();
    }

    @Override
    public void setCountdownText(String time){
        consoleViewCountdown.setText(time);
    }

    private void hideTimer(){
        consoleView.findViewById(R.id.mission_countdown).setVisibility(View.INVISIBLE);
    }

    private void showTimer(){
        consoleView.findViewById(R.id.mission_countdown).setVisibility(View.VISIBLE);
    }

    @Override
    public void exposeSuccessPrompt(String homeBeaconName) {
        hideTimer();
        disableCloseConsole();

        this.currentHomeBeacon = homeBeaconName;
        this.takedownSuccessInProgress = true;

        setGoodConsole();
        setConsoleTitle(R.string.console_expose_success_title);
        setConsoleImage(R.drawable.un_flag_small);
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
