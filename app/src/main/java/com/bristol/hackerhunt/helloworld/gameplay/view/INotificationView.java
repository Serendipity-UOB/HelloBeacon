package com.bristol.hackerhunt.helloworld.gameplay.view;

public interface INotificationView {

    public void exposeFailedInsufficientEvidence(String playerRealName);

    public void exposeFailedNotYourTarget(String playerCodName);

    public void exposeFailedNetworkError();

    public void attemptingToIntercept(String playerRealName);
}
