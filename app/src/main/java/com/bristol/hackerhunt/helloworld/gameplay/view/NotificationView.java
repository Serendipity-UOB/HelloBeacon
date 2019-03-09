package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;

public class NotificationView implements INotificationView {

    private static final long NOTIFICATION_DURATION = 3000; // given in milliseconds.

    private final View notificationOverlay;

    public NotificationView(View notificationOverlay) {
        this.notificationOverlay = notificationOverlay;
    }

    @Override
    public void exposeFailedInsufficientEvidence(String playerRealName) {
        setBadNotificationCard();
        setNotificationText("Exchange failed\nInsufficient evidence on " + playerRealName + ".");
        popUpNotification();
    }

    @Override
    public void exposeFailedNotYourTarget(String playerCodeName) {
        setBadNotificationCard();
        setNotificationText("Exchange failed\n" + playerCodeName + " is not your target.");
        popUpNotification();
    }

    @Override
    public void exposeFailedNetworkError() {
        setBadNotificationCard();
        setNotificationText("Exchange failed\nNetwork error.");
        popUpNotification();
    }

    @Override
    public void attemptingToIntercept(String playerRealName) {
        setNeutralNotificationCard();
        setNotificationText(insertPlayerName(R.string.notification_attempting_intercept, playerRealName));
        popUpNotification();
    }

    private String insertPlayerName(int stringId, String playerName) {
        String text = notificationOverlay.getContext().getResources().getString(stringId);
        return text.replace("$PLAYER_NAME", playerName);
    }

    private void popUpNotification() {
        notificationOverlay.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationOverlay.setVisibility(View.GONE);
            }
        }, NOTIFICATION_DURATION);
    }

    private View getNotificationCard() {
        return notificationOverlay.findViewById(R.id.gameplay_notification);
    }

    private void setPositiveNotificationCard() {
        getNotificationCard().setBackgroundResource(R.drawable.good_full_pop_up);
    }

    private void setNeutralNotificationCard() {
        getNotificationCard().setBackgroundResource(R.drawable.neutral_full_pop_up);
    }

    private void setBadNotificationCard() {
        getNotificationCard().setBackgroundResource(R.drawable.bad_full_pop_up);
    }

    private void setNotificationText(String message) {
        TextView text = notificationOverlay.findViewById(R.id.gameplay_notification_text);
        text.setText(message);
    }

    private void setNotificationText(int resourceId) {
        TextView text = notificationOverlay.findViewById(R.id.gameplay_notification_text);
        text.setText(resourceId);
    }
}
