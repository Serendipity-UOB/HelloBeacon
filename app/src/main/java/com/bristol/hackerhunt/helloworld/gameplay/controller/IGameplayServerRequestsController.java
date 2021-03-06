package com.bristol.hackerhunt.helloworld.gameplay.controller;

import com.bristol.hackerhunt.helloworld.TwinInputRunnable;
import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;

import org.json.JSONException;

/**
 * A controller class used to fulfil requests to the gameplay server.
 */
public interface IGameplayServerRequestsController {

    /**
     * Cancel all requests submitted to the Volley queue, freeing up associated memory.
     */
    void cancelAllRequests();

    /**
     * Submits a start info request to the server:
     * GET /startInfo
     *
     * @throws JSONException if the server JSON response cannot be parsed.
     */
    void startInfoRequest() throws JSONException;

    /**
     * Checks if a player is at their home beacon.
     *
     * @throws JSONException
     */
    void isAtHomeBeaconRequest() throws JSONException;

    /**
     * Submits a new target request to the server:
     * POST /newTarget { player_id }
     *
     * @throws JSONException if the server JSON response cannot be parsed.
     */
    void newTargetRequest() throws JSONException;

    /**
     * Submits a player update request to the server:
     * POST /playerUpdate { player_id, beacons[{beacon_minor, rssi}] }
     *
     * @throws JSONException if the server JSON response cannot be parsed.
     */
    void playerUpdateRequest() throws JSONException;

    /**
     * Submits a player exchange request to the server, and updates InteractionDetails with the
     * status of the exchange:
     * POST /exchange { interacter_id, interactee_id }
     *
     * @param interacteeId the ID of the player that the current player is interacting with.
     * @param details      a class outlining the current status of the exchange.
     * @throws JSONException if the server JSON response cannot be parsed.
     */
    void exchangeRequest(String interacteeId, InteractionDetails details) throws JSONException;


    /**
     * Handles an exchange response from player concerning an exchange offered to them
     *
     * @param interacteeId the ID of the player that current player is interacting with
     * @param response     the response to the exchange,
     *                     0 = Wait
     *                     1 = Accept
     *                     2 = Reject
     * @param details      a class outlining the current status of exchange
     * @throws JSONException if unparseable JSON
     */
    void exchangeResponse(String interacteeId, int response, InteractionDetails details) throws JSONException;

    /**
     * Registers a runnable used when a mission update occurs
     * @param runnable the runnable
     */
    void registerMissionUpdateRunnable(StringInputRunnable runnable);

    void registerMissionSuccessRunnable(StringInputRunnable runnable);

    void registerMissionFailureRunnable(StringInputRunnable runnable);


    /**
     * Submits a player take down request to the server:
     * POST /takeDown { player_id, target_id }
     *
     * @param targetId the ID of the player that the current player is attempting to take down.
     * @throws JSONException if the server JSON response cannot be parsed.
     */
    void exposeRequest(String targetId) throws JSONException;

    /**
     * Submits a player intercept request to the server:
     * POST /intercept { target_id }
     *
     * @param interacteeId the ID of the player that the current player is attempting to intercept
     *                     an exchange from.
     * @param details      the interaction details class
     * @throws JSONException
     */
    void interceptRequest(String interacteeId, InteractionDetails details) throws JSONException;

    /**
     * Registers a runnable that is run when an intercept is successful.
     *
     * @param interceptSuccessRunnable a runnable
     */
    void registerInterceptSuccessRunnable(Runnable interceptSuccessRunnable);

    /**
     * Handles a mission update request
     * @param details interaction details of update
     * @throws JSONException
     */
    void missionUpdateRequest(InteractionDetails details) throws JSONException;

    /**
     * Registers a runnable that is run when a takedown is successful.
     *
     * @param exposeSuccessRunnable a runnable.
     */
    void registerExposeSuccessRunnable(StringInputRunnable exposeSuccessRunnable);

    void registerExposeFailedRunnable(Runnable exposeFailedRunnable);

    void registerChangePlayerLocationRunnable(StringInputRunnable runnable);

    void registerChangeLocationRunnable(TwinInputRunnable runnable);

    void registerDisableInteractionsRunnable(Runnable runnable);

    void registerEnableInteractionsRunnable(Runnable runnable);

    void registerNewTargetConsoleRunnable(StringInputRunnable runnable);

    void registerPlayerTakenDownRunnable(StringInputRunnable runnable);
}