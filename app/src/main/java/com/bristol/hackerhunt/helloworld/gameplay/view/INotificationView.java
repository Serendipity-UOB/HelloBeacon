package com.bristol.hackerhunt.helloworld.gameplay.view;

public interface INotificationView {

    void networkError();

    /**
     * Shows a notification stating that the expose had failed due to insufficient evidence.
     * @param playerRealName target's real name.
     */
    void exposeFailedInsufficientEvidence(String playerRealName);

    /**
     * Shows a notification stating that the expose had failed due to an incorrect target.
     * @param playerRealName target's real name.
     */
    void exposeFailedNotYourTarget(String playerRealName);

    /**
     * Shows a notification stating that an error has caused the expose to fail
     */
    void exposeFailedNetworkError();

    /**
     * Shows a notification stating that an exchange had been requested.
     * @param playerRealName interactee real name.
     */
    void exchangeRequested(String playerRealName);

    /**
     * Shows a notification stating that an exchange has been successful
     * @param interacteeName The person who was exchanged with.
     * @param mutualContactName The mutual contact also gained evidence on.
     */
    void exchangeSuccessful(String interacteeName, String mutualContactName);

    /**
     * Shows a notification stating that an exchange has been successful
     * @param interacteeName The person who was exchanged with.
     */
    void exchangeSuccessful(String interacteeName);

    /**
     * Shows a notification stating that an exchange had failed, due to rejection.
     * @param interacteeName name.
     */
    void exchangeFailedRejection(String interacteeName);

    /**
     * Shows a notification stating that an exchange had failed, due to timing out.
     * @param interacteeName name
     */
    void exchangeFailedTimedOut(String interacteeName);

    /**
     * Shows a notification stating that an intercept is being attempted.
     * @param playerRealName name
     */
    void attemptingToIntercept(String playerRealName);

    /**
     * Shows a notification stating that an intercept had failed, due to no exchange taking place.
     * @param playerRealName name
     */
    void interceptFailedNoExchange(String playerRealName);

    /**
     * Shows a notification stating that an intercept had failed due to no evidence being shared.
     */
    void interceptFailedNoEvidenceShared();

    /**
     * Shows a notification stating that an intercept has been successful.
     * @param targetName name.
     * @param mutualContactName secondary name.
     */
    void interceptSucceeded(String targetName, String mutualContactName);

}
