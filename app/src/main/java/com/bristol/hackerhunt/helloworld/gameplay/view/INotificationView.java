package com.bristol.hackerhunt.helloworld.gameplay.view;

public interface INotificationView {

    void exposeFailedInsufficientEvidence(String playerRealName);

    void exposeFailedNotYourTarget(String playerCodName);

    void exposeFailedNetworkError();

    void exchangeRequested(String playerRealName);

    void exchangeSuccessful(String interacteeName, String mutualContactName);

    void exchangeFailedRejection(String interacteeName);

    void exchangeFailedTimedOut(String interacteeName);

    void attemptingToIntercept(String playerRealName);

    void interceptFailedNoExchange(String playerRealName);

    void interceptFailedNoEvidenceShared();

    void interceptSucceeded(String targetName, String mutualContactName);

}
