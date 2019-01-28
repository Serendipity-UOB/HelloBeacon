package com.bristol.hackerhunt.helloworld.gameplay;

import com.bristol.hackerhunt.helloworld.model.InteractionDetails;

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
     * @param interacteeId the NFC ID of the player that the current player is interacting with.
     * @param details a class outlining the current status of the exchange.
     * @throws JSONException if the server JSON response cannot be parsed.
     */
    void exchangeRequest(String interacteeId, InteractionDetails details) throws JSONException;

    /**
     * Submits a player take down request to the server:
     * POST /takeDown { player_id, target_id }
     *
     * @param targetId the NFC ID of the player that the current player is attempting to take down.
     * @throws JSONException if the server JSON response cannot be parsed.
     */
    void takeDownRequest(String targetId) throws JSONException;

    // TODO
    void registerTakedownSuccessRunnable(Runnable takedownSuccessRunnable);
}
