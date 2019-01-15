package com.bristol.hackerhunt.helloworld.joinGame;

import org.json.JSONException;

/**
 * Controller class responsible for join game server requests.
 */
public interface IJoinGameServerRequestController {

    /**
     * Request game information from the server:
     * GET /gameInfo
     *
     * @throws JSONException the response of the server could not be parsed into JSON.
     */
    void gameInfoRequest() throws JSONException;

    /**
     * Request to join the game from the server.
     * POST /joinGame { player_id }
     *
     * @throws JSONException the response of the server could not be parsed into JSON.
     */
    void joinGameRequest(String playerId) throws JSONException;

    /**
     * Cancel all requests.
     */
    void cancelAllRequests();
}
