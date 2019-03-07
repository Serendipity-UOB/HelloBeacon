package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;

public class NotificationView implements INotificationView {

    private final View notificationOverlay;

    public NotificationView(View notificationOverlay) {
        this.notificationOverlay = notificationOverlay;
    }

    @Override
    public void exposeFailedInsufficientEvidence(String playerRealName) {
        notificationOverlay.setVisibility(View.VISIBLE);
        setBadNotificationCard();
        setNotificationText("Exchange failed\nInsufficient evidence on " + playerRealName + ".");
    }

    @Override
    public void exposeFailedNotYourTarget(String playerCodeName) {
        notificationOverlay.setVisibility(View.VISIBLE);
        setBadNotificationCard();
        setNotificationText("Exchange failed\n" + playerCodeName + " is not your target.");
    }

    @Override
    public void exposeFailedNetworkError(String playerRealName) {
        notificationOverlay.setVisibility(View.VISIBLE);
        setBadNotificationCard();
        setNotificationText("Exchange failed\nNetwork error.");
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
