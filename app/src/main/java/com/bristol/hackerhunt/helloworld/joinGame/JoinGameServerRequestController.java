package com.bristol.hackerhunt.helloworld.joinGame;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinGameServerRequestController {

    private final GameInfo gameInfo;

    public JoinGameServerRequestController(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    // TODO: GET /gameInfo
    public void gameInfoRequest() throws JSONException {
         // this is a placeholder
        String response = "{\"start_time\":0.17,\"number_players\":2}";
        JSONObject obj = new JSONObject(response);

        double minutesToStart = obj.getDouble("start_time");
        int numberOfPlayers = obj.getInt("number_players");

        if (gameInfo.minutesToStart == null) {
            gameInfo.minutesToStart = minutesToStart;
        }
        gameInfo.numberOfPlayers = numberOfPlayers;
    }

    // TODO: POST /joinGame { player_id }
    public void joinGameRequest() throws JSONException {
        // this is a placeholder
        String response = "{\"start_beacon\":\"Beacon A\"}";
        JSONObject obj = new JSONObject(response);

        gameInfo.startBeacon = obj.getString("start_beacon");
    }
}
