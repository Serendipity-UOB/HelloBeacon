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
import com.bristol.hackerhunt.helloworld.StringInputRunnable;
import com.bristol.hackerhunt.helloworld.gameplay.PlayerUpdate;
import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.model.InteractionStatus;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameplayServerRequestsController implements IGameplayServerRequestsController {

    private String SERVER_ADDRESS;
    private String START_INFO_URL;
    private String NEW_TARGET_URL;
    private String PLAYER_UPDATE_URL;
    private String PLAYER_AT_HOME_URL;
    private String EXCHANGE_REQUEST_URL;
    private String TAKE_DOWN_URL;
    private String INTERCEPT_URL;
    private String EXCHANGE_RESPONSE_URL;
    private String MISSION_URL;

    private final int EXCHANGE_PRIMARY_INCREMENT = 10;
    private final int EXCHANGE_SECONDARY_INCREMENT = 20;
    private final int INTERCEPT_PRIMARY_INCREMENT = 30;
    private final int INTERCEPT_SECONDARY_INCREMENT = 10;

    private RequestQueue requestQueue;
    private IGameStateController gameStateController;

    private int statusCode = 0;

    private Runnable exposeSuccessRunnable;
    private Runnable exposeFailedRunnable;
    private Runnable interceptSuccessRunnable;
    private StringInputRunnable missionUpdateRunnable;


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
        this.EXCHANGE_REQUEST_URL = context.getString(R.string.exchange_request);
        this.EXCHANGE_RESPONSE_URL = context.getString(R.string.exchange_response);
        this.TAKE_DOWN_URL = context.getString(R.string.takedown_request);
        this.PLAYER_AT_HOME_URL = context.getString(R.string.home_beacon_request);
        this.INTERCEPT_URL = context.getString(R.string.intercept_request);
        this.MISSION_URL = context.getString(R.string.mission_update_request);
    }

    @Override
    public void cancelAllRequests() {
        requestQueue.stop();
    }

    @Override
    public void startInfoRequest() {
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

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + START_INFO_URL, new JSONObject(),
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
        updateNearbyPlayers(obj);
        updatePlayerPoints(obj);
        updateLeaderboardPosition(obj);
        updateExchangeReceive(obj);
        checkForPlayerStatusChanges(obj);
        checkForMission(obj);
        checkGameOver(obj);
    }

    private void updateNearbyPlayers(JSONObject obj) throws JSONException {
        JSONArray nearbyPlayerIdsJson = obj.getJSONArray("nearby_players");
        List<String> nearbyPlayerIds = new ArrayList<>();
        for (int i = 0; i < nearbyPlayerIdsJson.length(); i++) {
            nearbyPlayerIds.add(nearbyPlayerIdsJson.getString(i));
        }
        gameStateController.updateNearbyPlayers(nearbyPlayerIds);
    }

    private void updatePlayerPoints(JSONObject obj) throws JSONException {
        if (obj.has("reputation")) {
            int points = obj.getInt("reputation");
            gameStateController.updatePoints(points);
        }
    }

    private void updateLeaderboardPosition(JSONObject obj) throws JSONException {
        if (obj.has("position")) {
            String position = obj.getString("position");
            gameStateController.updateLeaderboardPosition(position);
        }
    }

    private void updateExchangeReceive(JSONObject obj) throws JSONException {
        if(obj.has("exchange_pending")) {
            String reqId = obj.getString("exchange_pending");
            gameStateController.updateExchangeReceive(reqId);
        }
    }

    private void checkForMission(JSONObject obj) throws JSONException {
        if(obj.has("mission_description")) {
            String missionId = obj.getString("mission_description");
            missionUpdateRunnable.run(missionId);
        }
    }

    private void checkForPlayerStatusChanges(JSONObject obj) throws JSONException {
        List<PlayerUpdate> updates = new ArrayList<>();
        String exposedId = "";
        if (obj.has("exposed")) {
            boolean takenDown = obj.getBoolean("exposed");
            if (obj.has("exposed_id")) {
                exposedId = obj.getString("exposed_id");
                if (takenDown) {
                    updates.add(PlayerUpdate.TAKEN_DOWN);
                }
            }
        }
        if (obj.has("req_new_target")) {
            boolean reqNewTarget = obj.getBoolean("exposed");
            if (reqNewTarget) {
                updates.add(PlayerUpdate.REQ_NEW_TARGET);
            }
        }

        gameStateController.updateStatus(updates, exposedId);
    }

    private void checkGameOver(JSONObject obj) throws JSONException {
        if (obj.has("game_over")) {
            boolean gameOver = obj.getBoolean("game_over");
            if (gameOver) {
                gameStateController.setGameOver();
            }
        }
    }

    private JSONObject playerUpdateRequestBody() throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("player_id", gameStateController.getPlayerId());

        JSONArray beacons = new JSONArray();
        for (String major : gameStateController.getAllBeaconMajors()) {
            for (String minor : gameStateController.getAllBeaconMinors(major)) {
                JSONObject beaconJson = new JSONObject();
                beaconJson.put("beacon_major", major);
                beaconJson.put("beacon_minor", minor);
                beaconJson.put("rssi", gameStateController.getBeaconRssi(major, minor));
                beacons.put(beaconJson);
            }
        }
        requestBody.put("beacons", beacons);

        return requestBody;
    }

    @Override
    public void missionUpdateRequest(InteractionDetails details) throws JSONException {
        requestQueue.add(volleyMissionUpdateRequest(details));
    }

    private JsonObjectRequest volleyMissionUpdateRequest(final InteractionDetails details) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (statusCode == 200) {
                    try {
                        missionSuccess(details, response); //TODO Define
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(statusCode == 100){
                    if(response.has("time_remaining")){
                        try {
                            int timeRemaining = response.getInt("time_remaining");
                            missionPending(details, timeRemaining, response); //TODO Define
                        } catch (JSONException e) {
                            // TODO: handle.
                        }
                    }
                }
                else if(statusCode == 204){
                    try {
                        missionFailure(details, response); //TODO Define
                    } catch (JSONException e) {
                        // TODO: handle
                    }
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(statusCode == 400) {
                    // Log.d("Network", "400 Error received");
                    //TODO Correct Behaviour?
                }
                statusCode = 0;
            }
        };

        return new JsonObjectRequest(Request.Method.GET, SERVER_ADDRESS + MISSION_URL, new JSONObject(), listener, errorListener) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    statusCode = response.statusCode;
                }
                return super.parseNetworkResponse(response);
            }
        };
    }

    private void missionSuccess(InteractionDetails details, JSONObject obj) throws JSONException {
        if(obj.has("rewards")){
            JSONArray rewardArray = obj.getJSONArray("rewards");
            for(int i = 0; i < rewardArray.length(); i++){
                JSONObject rewardRow = rewardArray.getJSONObject(i);
                String rewardId = rewardRow.getString("player_id");
                int rewardAmount = rewardRow.getInt("evidence");
                gameStateController.increasePlayerIntel(rewardId, rewardAmount);
            }

            gameStateController.missionSuccessful();
        }
        details.status = InteractionStatus.SUCCESSFUL;
    }

    private void missionPending(InteractionDetails details, int timeRemaining, JSONObject obj) throws JSONException {
        //TODO Define, probably tell gameStateController something
        details.status = InteractionStatus.IN_PROGRESS;
        details.missionTime = timeRemaining;
    }

    private void missionFailure(InteractionDetails details, JSONObject obj) throws JSONException {
        //TODO Define probably tell gameStateController something
        gameStateController.missionFailed();
        details.status = InteractionStatus.FAILED;
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
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (statusCode == 400) {
                    // Log.d("Network", "400 Error received");
                    unsuccessfulExchange(details);
                }
                else if (statusCode != 201 && statusCode != 202){
                    // Log.d("Network", "Different server error received: " + Integer.toString(statusCode));
                    unsuccessfulExchange(details);
                }
                statusCode = 0;
            }
        };

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + EXCHANGE_REQUEST_URL,
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
        /*
        details.gainedIntelPlayerIds.add(interacteeId);
        if (!secondaryId.equals("0"))
            details.gainedIntelPlayerIds.add(secondaryId);
        */
        gameStateController.increasePlayerIntel(interacteeId, EXCHANGE_PRIMARY_INCREMENT);
        details.gainedIntelPlayerIds.add(interacteeId);
        gameStateController.increasePlayerIntel(secondaryId, EXCHANGE_SECONDARY_INCREMENT);
        details.gainedIntelPlayerIds.add(secondaryId);
        details.status = InteractionStatus.SUCCESSFUL;
    }

    private void unsuccessfulExchange(InteractionDetails details) {
        details.status = InteractionStatus.FAILED;
    }

    private void rejectedExchange(InteractionDetails details) {
        details.status = InteractionStatus.REJECTED;
    }

    //6c - Exchange Request
    private JSONObject exchangeRequestBody(String interacteeId) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("requester_id", gameStateController.getPlayerId());
        requestBody.put("responder_id", interacteeId);

        JSONArray contactIds = new JSONArray();
        for (String playerId : gameStateController.getPlayerIdRealNameMap().keySet()) {
            if (gameStateController.playerHasNonZeroIntel(playerId)) {
                JSONObject contactId = new JSONObject();
                contactId.put("contact_id", playerId);
                contactIds.put(contactId);
            }
        }
        requestBody.put("contact_ids", contactIds);

        // Log.d("Network", requestBody.toString());
        return requestBody;
    }

    private void pendingExchange(String interacteeId, InteractionDetails details, JSONObject obj) throws JSONException {
        int timeRemaining = obj.getInt("time_remaining"); //TODO Define what happens here
    }

    @Override
    public void exchangeResponse(String interacteeId, int response, InteractionDetails details) throws JSONException {
        requestQueue.add(volleyExchangeResponse(interacteeId, response, details));
    }

    private JsonObjectRequest volleyExchangeResponse(final String interacteeId, int playerResponse, final InteractionDetails details) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (statusCode == 202){
                    try {
                        successfulExchange(interacteeId, details, response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (statusCode == 206){
                    try {
                        pendingExchange(interacteeId, details, response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (statusCode == 205){
                    rejectedExchange(details);
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (statusCode == 400) {
                    // Log.d("Network", "400 Error received");
                    unsuccessfulExchange(details);
                }
                else if (statusCode == 408) {
                    // Log.d("Network", "408 Error received");
                    unsuccessfulExchange(details);
                }
                else if (statusCode != 201 && statusCode != 202){
                    // Log.d("Network", "Different server error received: " + Integer.toString(statusCode));
                    unsuccessfulExchange(details);
                }
                statusCode = 0;
            }
        };

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + EXCHANGE_RESPONSE_URL,
                exchangeResponseBody(interacteeId, playerResponse), listener, errorListener) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    statusCode = response.statusCode;
                }
                return super.parseNetworkResponse(response);
            }
        };
    }

    //6d - Exchange Response
    private JSONObject exchangeResponseBody(String interacteeId, int response) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("player_id", gameStateController.getPlayerId());
        requestBody.put("exchanger_id", interacteeId);
        requestBody.put("response", response);

        JSONArray contactIds = new JSONArray();
        for (String playerId : gameStateController.getPlayerIdRealNameMap().keySet()) {
            if (gameStateController.playerHasNonZeroIntel(playerId)) {
                JSONObject contactId = new JSONObject();
                contactId.put("contact_id", playerId);
                contactIds.put(contactId);
            }
        }
        requestBody.put("contact_ids", contactIds);

        // Log.d("Network", requestBody.toString());
        return requestBody;
    }

    @Override
    public void interceptRequest(String interacteeId, final InteractionDetails details) throws JSONException {
        requestQueue.add(volleyInterceptRequest(interacteeId, details));
    }

    private JsonObjectRequest volleyInterceptRequest(final String interacteeId, final InteractionDetails details) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (statusCode == 200){
                    try {
                        interceptSuccess(response, details);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (statusCode == 204){
                    try {
                        interceptFailure(details);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (statusCode == 400) {
                    // Log.d("Network", "400 Error received");
                    interceptError(error, details);
                }
                statusCode = 0;
            }
        };

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + INTERCEPT_URL,
                interceptRequestBody(interacteeId), listener, errorListener) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    statusCode = response.statusCode;
                }
                return super.parseNetworkResponse(response);
            }
        };
    }

    private void interceptSuccess(JSONObject obj, InteractionDetails details) throws JSONException {
        if(obj.has("primary_id")){
            String primaryId = obj.getString("primary_id");
            if(obj.has("primary_evidence")){
                int primaryEvidence = obj.getInt("primary_evidence");
                gameStateController.increasePlayerIntel(primaryId,primaryEvidence);
            }
            details.gainedIntelPlayerIds.add(primaryId);
        }
        if(obj.has("secondary_id")){
            String secondaryId = obj.getString("secondary_id");
            if(obj.has("secondary_evidence")){
                int secondaryEvidence = obj.getInt("secondary_evidence");
                gameStateController.increasePlayerIntel(secondaryId,secondaryEvidence);
            }
            details.gainedIntelPlayerIds.add(secondaryId);
        }
        details.status = InteractionStatus.SUCCESSFUL;
    }

    private void interceptFailure(InteractionDetails details) throws JSONException {
        details.status = InteractionStatus.FAILED;
        if(statusCode == 204){
            //No Content
            //No exchange happened
            details.status = InteractionStatus.NO_EVIDENCE;
        }

    }

    private void interceptError(VolleyError error, InteractionDetails details) {
        details.status = InteractionStatus.FAILED;
    }

    private JSONObject interceptRequestBody(String interacteeId) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("target_id", interacteeId);
        return requestBody;
    }

    @Override
    public void registerMissionUpdateRunnable(StringInputRunnable runnable) {
        this.missionUpdateRunnable = runnable;
    }

    @Override
    public void registerInterceptSuccessRunnable(Runnable runnable) {
        this.interceptSuccessRunnable = runnable;
    }

    @Override
    public void exposeRequest(String targetId) throws JSONException {
        requestQueue.add(volleyExposeRequest(targetId));
    }

    private JsonObjectRequest volleyExposeRequest(String targetId) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                exposeSuccessRunnable.run();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                exposeFailedRunnable.run();
            }
        };

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + TAKE_DOWN_URL,
                exposeRequestBody(targetId), listener, errorListener);
    }

    private JSONObject exposeRequestBody(String targetId) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("player_id", gameStateController.getPlayerId());
        requestBody.put("target_id", targetId);
        return requestBody;
    }

    private PlayerIdentifiers jsonToPlayerIdentifiers(JSONObject obj) throws JSONException {
        String playerRealName = obj.getString("real_name");
        String playerHackerName = obj.getString("code_name");
        String playerId = obj.getString("id");

        return new PlayerIdentifiers(playerRealName, playerHackerName, playerId);
    }

    @Override
    public void registerExposeSuccessRunnable(Runnable exposeSuccessRunnable) {
        this.exposeSuccessRunnable = exposeSuccessRunnable;
    }

    @Override
    public void registerExposeFailedRunnable(Runnable exposeFailedRunnable) {
        this.exposeFailedRunnable = exposeFailedRunnable;
    }

    @Override
    public void isAtHomeBeaconRequest() throws JSONException {
        requestQueue.add(volleyIsAtHomeBeaconRequest());
    }

    private JsonObjectRequest volleyIsAtHomeBeaconRequest() throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                atHomeUpdate(response);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                throw new IllegalStateException("Error: " + error.getMessage());
            }
        };

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + PLAYER_AT_HOME_URL,
                playerUpdateRequestBody(), listener, errorListener);
    }

    private void atHomeUpdate(JSONObject response) {
        try {
            Boolean playerIsAtHome = response.getBoolean("home");
            if (playerIsAtHome) {
                gameStateController.playerIsAtHomeBeacon();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
