package com.bristol.hackerhunt.helloworld.gameplay.view;

public interface INotificationView {

    public void exposeFailedInsufficientEvidence(String playerRealName);

    public void exposeFailedNotYourTarget(String playerCodeName);

    public void exposeFailedNetworkError(String playerRealName);
}
