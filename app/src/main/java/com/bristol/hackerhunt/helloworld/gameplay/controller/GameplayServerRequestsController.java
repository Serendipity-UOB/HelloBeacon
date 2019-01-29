package com.bristol.hackerhunt.helloworld.gameplay.controller;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.gameplay.PlayerUpdate;
import com.bristol.hackerhunt.helloworld.gameplay.controller.IGameStateController;
import com.bristol.hackerhunt.helloworld.gameplay.controller.IGameplayServerRequestsController;
import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.model.InteractionStatus;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameplayServerRequestsController implements IGameplayServerRequestsController {

    private final String SERVER_ADDRESS;
    private final String START_INFO_URL;
    private final String NEW_TARGET_URL;
    private final String PLAYER_UPDATE_URL;
    private final String EXCHANGE_URL;
    private final String TAKE_DOWN_URL;

    private final RequestQueue requestQueue;
    private final IGameStateController gameStateController;

    private int statusCode = 0;

    private Runnable takedownSuccessRunnable;

    /**
     * Class constructor.
     * @param context Context of activity using the controller.
     * @param gameStateController The GameStateController used to control the state of the game.
     */
    public GameplayServerRequestsController(Context context, IGameStateController gameStateController) {
        this.requestQueue = Volley.newRequestQueue(context);
        this.gameStateController = gameStateController;

        this.SERVER_ADDRESS = context.getString(R.string.server_address);
        this.START_INFO_URL = context.getString(R.string.start_info_request);
        this.NEW_TARGET_URL = context.getString(R.string.new_target_request);
        this.PLAYER_UPDATE_URL = context.getString(R.string.player_update_request);
        this.EXCHANGE_URL = context.getString(R.string.exchange_request);
        this.TAKE_DOWN_URL = context.getString(R.string.takedown_request);
    }


    @Override
    public void cancelAllRequests() {
        requestQueue.stop();
    }

    @Override
    public void startInfoRequest() throws JSONException {
        // this is just a placeholder.
        // String response = "{\"all_players\":[{\"id\":\"1\",\"real_name\":\"Tom\",\"hacker_name\":\"Tom\"},{\"id\":\"2\",\"real_name\":\"Tilly\",\"hacker_name\":\"cutie_kitten\"},{\"id\":\"3\",\"real_name\":\"Louis\",\"hacker_name\":\"Louis\"},{\"id\":\"4\",\"real_name\":\"David\",\"hacker_name\":\"CookingKing\"},{\"id\":\"5\",\"real_name\":\"Jack\",\"hacker_name\":\"falafel\"},{\"id\":\"7\",\"real_name\":\"Tilo\",\"hacker_name\":\"Tilo\"},{\"id\":\"8\",\"real_name\":\"Beth\",\"hacker_name\":\"Beth\"},{\"id\":\"9\",\"real_name\":\"Becky\",\"hacker_name\":\"Becky\"},{\"id\":\"10\",\"real_name\":\"Bradley\",\"hacker_name\":\"Bradley\"}]}";
        // JSONObject obj = new JSONObject(response);

        // setAllPlayers(obj);

        requestQueue.add(volleyStartInfoRequest());
    }

    private JsonObjectRequest volleyStartInfoRequest() {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    setAllPlayers(response);
                    newTargetRequest();
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

        return new JsonObjectRequest(Request.Method.GET, SERVER_ADDRESS + START_INFO_URL, null,
                listener, errorListener);
    }

    private void setAllPlayers(JSONObject allPlayersJson) throws JSONException {
        JSONArray allPlayers =  allPlayersJson.getJSONArray("all_players");
        List<PlayerIdentifiers> allPlayersIdentifiers = new ArrayList<>();

        for (int i = 0; i < allPlayers.length(); i++) {
            JSONObject obj = allPlayers.getJSONObject(i);
            if (!isCurrentPlayer(obj)) {
                allPlayersIdentifiers.add(jsonToPlayerIdentifiers(obj));
            }
        }
        gameStateController.setAllPlayers(allPlayersIdentifiers);
    }

    private boolean isCurrentPlayer(JSONObject obj) throws JSONException {
        String playerId = obj.getString("id");
        return playerId.equals(this.gameStateController.getPlayerId());
    }

    @Override
    public void newTargetRequest() throws JSONException {
        // this is just a placeholder.
        // String response = "{\"target_player_id\": \"2\"}";
        // JSONObject obj = new JSONObject(response);
        // updateTargetPlayer(obj);

        requestQueue.add(volleyNewTargetRequest());
    }

    private JsonObjectRequest volleyNewTargetRequest() throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    updateTargetPlayer(response);
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
        requestBody.put("player_id", gameStateController.getPlayerId());

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + NEW_TARGET_URL, requestBody,
                listener, errorListener);
    }

    private void updateTargetPlayer(JSONObject obj) throws JSONException {
        String targetPlayerId = obj.getString("target_player_id");
        gameStateController.updateTargetPlayer(targetPlayerId);
    }

    @Override
    public void playerUpdateRequest() throws JSONException {
        // this is just a placeholder.
        // String response = "{\"nearby_players\":[\"2\",\"3\",\"4\"],\"points\":516,\"position\":\"2\"}";
        // JSONObject obj = new JSONObject(response);
        // playerUpdate(obj);

        requestQueue.add(volleyPlayerUpdateRequest());
    }

    private JsonObjectRequest volleyPlayerUpdateRequest() throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    playerUpdate(response);
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

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + PLAYER_UPDATE_URL,
                playerUpdateRequestBody(), listener, errorListener);
    }

    private void playerUpdate(JSONObject obj) throws JSONException {
        JSONArray nearbyPlayerIdsJson = obj.getJSONArray("nearby_players");
        List<String> nearbyPlayerIds = new ArrayList<>();
        for (int i = 0; i < nearbyPlayerIdsJson.length(); i++) {
            nearbyPlayerIds.add(nearbyPlayerIdsJson.getString(i));
        }
        gameStateController.updateNearbyPlayers(nearbyPlayerIds);

        if (obj.has("points")) {
            int points = obj.getInt("points");
            gameStateController.updatePoints(points);
        }
        if (obj.has("position")) {
            String position = obj.getString("position");
            gameStateController.updateLeaderboardPosition(position);
        }

        List<PlayerUpdate> updates = new ArrayList<>();
        if (obj.has("taken_down")) {
            boolean takenDown = obj.getInt("taken_down") == 1;
            if (takenDown) {
                updates.add(PlayerUpdate.TAKEN_DOWN);
            }
        }
        if (obj.has("req_new_target")) {
            boolean reqNewTarget = obj.getInt("req_new_target") == 1;
            if (reqNewTarget) {
                updates.add(PlayerUpdate.REQ_NEW_TARGET);
            }
        }

        gameStateController.updateStatus(updates);
    }

    private JSONObject playerUpdateRequestBody() throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("player_id", gameStateController.getPlayerId());

        JSONArray beacons = new JSONArray();
        for (String minor : gameStateController.getAllBeaconMinors()) {
            JSONObject beaconJson = new JSONObject();
            beaconJson.put("beacon_minor", minor);
            beaconJson.put("rssi", gameStateController.getBeaconRssi(minor));
            beacons.put(beaconJson);
        }
        requestBody.put("beacons", beacons);

        return requestBody;
    }

    @Override
    public void exchangeRequest(String interacteeId, InteractionDetails details) throws JSONException {
        // this is just a placeholder (assuming success).
        // String response = "{\"secondary_id\":\"1\"}";
        // JSONObject obj = new JSONObject(response);
        // successfulExchange(interacteeId, details, obj);

        requestQueue.add(volleyExchangeRequest(interacteeId, details));
    }

    private JsonObjectRequest volleyExchangeRequest(final String interacteeId, final InteractionDetails details) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (statusCode == 200) {
                    try {
                        successfulExchange(interacteeId, details, response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                unsuccessfulExchange(details);
            }
        };

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + EXCHANGE_URL,
                exchangeRequestBody(interacteeId), listener, errorListener) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    statusCode = response.statusCode;
                }
                return super.parseNetworkResponse(response);
            }
        };
    }

    private void successfulExchange(String interacteeId, InteractionDetails details, JSONObject obj) throws JSONException {
        String secondaryId = obj.getString("secondary_id");
        details.gainedIntelPlayerIds.add(interacteeId);
        details.gainedIntelPlayerIds.add(secondaryId);
        details.status = InteractionStatus.SUCCESSFUL;
    }

    private void unsuccessfulExchange(InteractionDetails details) {
        details.status = InteractionStatus.IN_PROGRESS;
    }

    private JSONObject exchangeRequestBody(String interacteeId) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("interacter_id", gameStateController.getPlayerId());
        requestBody.put("interactee_id", interacteeId);

        JSONArray contactIds = new JSONArray();
        for (String playerId : gameStateController.getPlayerIdRealNameMap().keySet()) {
            if (gameStateController.playerHasFullIntel(playerId)) {
                JSONObject contactId = new JSONObject();
                contactId.put("contact_id", playerId);
                contactIds.put(contactId);
            }
        }
        requestBody.put("contact_ids", contactIds);

        return requestBody;
    }

    @Override
    public void takeDownRequest(String targetId) throws JSONException {
        requestQueue.add(volleyTakeDownRequest(targetId));
    }

    private JsonObjectRequest volleyTakeDownRequest(String targetId) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                takedownSuccessRunnable.run();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // do nothing.
            }
        };

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + TAKE_DOWN_URL,
                takeDownRequestBody(targetId), listener, errorListener);
    }

    private JSONObject takeDownRequestBody(String targetId) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("player_id", gameStateController.getPlayerId());
        requestBody.put("target_id", targetId);
        return requestBody;
    }

    private PlayerIdentifiers jsonToPlayerIdentifiers(JSONObject obj) throws JSONException {
        String playerRealName = obj.getString("real_name");
        String playerHackerName = obj.getString("hacker_name");
        String playerId = obj.getString("id");

        return new PlayerIdentifiers(playerRealName, playerHackerName, playerId);
    }

    @Override
    public void registerTakedownSuccessRunnable(Runnable runnable) {
        this.takedownSuccessRunnable = runnable;
    }
}