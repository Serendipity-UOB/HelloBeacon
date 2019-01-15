package com.bristol.hackerhunt.helloworld.joinGame;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinGameServerRequestController implements IJoinGameServerRequestController {

    private static final String SERVER_ADDRESS = "";
    private static final String GAME_INFO_URL = "/gameInfo";
    private static final String JOIN_GAME_URL = "/joinGame";

    private final RequestQueue requestQueue;
    private final GameInfo gameInfo;

    JoinGameServerRequestController(Context context, GameInfo gameInfo) {
        this.requestQueue = Volley.newRequestQueue(context);
        this.gameInfo = gameInfo;
    }

    @Override
    public void cancelAllRequests() {
        requestQueue.stop();
    }

    @Override
    public void gameInfoRequest() throws JSONException {
         // this is a placeholder
        String response = "{\"start_time\":0.25,\"number_players\":2}";
        JSONObject obj = new JSONObject(response);
        updateGameInfo(obj);

        // TODO: requestQueue.add(volleyGameInfoRequest());
    }

    private JsonObjectRequest volleyGameInfoRequest() throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    updateGameInfo(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                throw new IllegalStateException("Error: " + error.getMessage());
            }
        };

        return new JsonObjectRequest(Request.Method.GET, SERVER_ADDRESS + GAME_INFO_URL, null,
                listener, errorListener);
    }

    private void updateGameInfo(JSONObject gameInfoJson) throws JSONException {
        double minutesToStart = gameInfoJson.getDouble("start_time");
        int numberOfPlayers = gameInfoJson.getInt("number_players");

        if (gameInfo.minutesToStart == null) {
            gameInfo.minutesToStart = minutesToStart;
        }
        gameInfo.numberOfPlayers = numberOfPlayers;
    }

    @Override
    public void joinGameRequest(String playerId) throws JSONException {
        // this is a placeholder
        String response = "{\"start_beacon\":\"Beacon A\"}";
        JSONObject obj = new JSONObject(response);
        gameInfo.startBeacon = obj.getString("start_beacon");

        // TODO: requestQueue.add(volleyJoinGameRequest(playerId));
    }

    private JsonObjectRequest volleyJoinGameRequest(String playerId) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    gameInfo.startBeacon = response.getString("start_beacon");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                throw new IllegalStateException("Error: " + error.getMessage());
            }
        };

        JSONObject requestBody = new JSONObject();
        requestBody.put("player_id", playerId);

        return new JsonObjectRequest(Request.Method.PUT, SERVER_ADDRESS + JOIN_GAME_URL, requestBody,
                listener, errorListener);
    }
}
