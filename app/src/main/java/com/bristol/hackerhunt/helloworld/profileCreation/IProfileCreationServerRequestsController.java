package com.bristol.hackerhunt.helloworld.profileCreation;

import org.json.JSONException;

public interface IProfileCreationServerRequestsController {

    /**
     * Cancel all requests.
     */
    void cancelAllRequests();

    /**
     * Request to register player with server.
     * POST /registerPlayer { real_name, hacker_name, nfc_id }
     *
     * @param realName real name.
     * @param hackerName hacker name.
     * @param nfcId NFC ID
     * @throws JSONException server's response couldn't be parsed into JSON.
     */
    void registerPlayerRequest(String realName, String hackerName, String nfcId) throws JSONException;

    /**
     * Registers a runnable to run a method upon receiving that the submitted profile is valid.
     * @param runnable A runnable.
     */
    void registerOnProfileValidRunnable(Runnable runnable);

    /**
     * Registers a runnable to run a method upon receiving that the submitted profile is invalid.
     * @param runnable A runnable.
     */
    void registerOnProfileInvalidRunnable(Runnable runnable);
}
