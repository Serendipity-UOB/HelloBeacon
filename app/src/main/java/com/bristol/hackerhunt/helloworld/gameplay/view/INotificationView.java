package com.bristol.hackerhunt.helloworld.gameplay.view;

public interface INotificationView {

    void exposeFailedInsufficientEvidence(String playerRealName);

    void exposeFailedNotYourTarget(String playerCodName);

    void exposeFailedNetworkError();

    void exchangeRequested(String playerRealName);

    void attemptingToIntercept(String playerRealName);
}
