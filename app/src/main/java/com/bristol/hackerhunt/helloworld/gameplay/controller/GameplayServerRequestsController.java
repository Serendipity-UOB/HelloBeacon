package com.bristol.hackerhunt.helloworld.gameplay.controller;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;
import com.bristol.hackerhunt.helloworld.TwinInputRunnable;
import com.bristol.hackerhunt.helloworld.gameplay.PlayerUpdate;
import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.model.InteractionStatus;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
    private String CODENAME_DISCOVERED_URL;

    private RequestQueue requestQueue;
    private IGameStateController gameStateController;

    private int statusCode = 0;

    private StringInputRunnable exposeSuccessRunnable;
    private Runnable exposeFailedRunnable;
    private Runnable interceptSuccessRunnable;
    private StringInputRunnable missionUpdateRunnable;
    private StringInputRunnable missionSuccessRunnable;
    private StringInputRunnable missionFailureRunnable;
    private StringInputRunnable changePlayerLocationRunnable;
    private TwinInputRunnable changeLocationRunnable;
    private Runnable disableInteractionsRunnable;
    private Runnable enableInteractionsRunnable;
    private StringInputRunnable newTargetConsoleRunnable;
    private StringInputRunnable playerTakenDownRunnable;

    private Map<String, Integer> statusCodeRequestMap;

    private List<String> codenameDiscoveredList;

    private Boolean notFirstTarget = false;


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
        this.CODENAME_DISCOVERED_URL = context.getString(R.string.codename_discovered_request);

        this.statusCodeRequestMap = new HashMap<>();
        this.codenameDiscoveredList = new ArrayList<>();
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



    private JsonObjectRequestWithNull volleyStartInfoRequest() throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    setAllPlayers(response);
                    setEndTime(response);
                    setTarget(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Network", error.getMessage());
            }
        };

        JSONObject startInfoBody = new JSONObject();
        startInfoBody.put("player_id",gameStateController.getPlayerId());
        Log.d("My Player id",gameStateController.getPlayerId());

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + START_INFO_URL, startInfoBody,
                listener, errorListener,setStatusCodeRunnable(), statusCodeRequestMap);
    }

    private void setTarget(JSONObject response) throws JSONException{
        String targetId = response.getString("first_target_id");
        gameStateController.updateTargetPlayer(targetId);

    }

    private float calculateTimeRemainingInMinutes(String startTime) {
        Calendar c2 = Calendar.getInstance(TimeZone.getDefault());

        int currentHour = c2.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c2.get(Calendar.MINUTE);
        int currentSecond = c2.get(Calendar.SECOND);

        int currentTotal = currentSecond + 60 * (currentMinute + 60 * currentHour);

        String[] startTimeArr = startTime.split(":");
        float startHour = Float.parseFloat(startTimeArr[0]);
        float startMinute = Float.parseFloat(startTimeArr[1]);
        float startSecond = 0;
        if (startTimeArr.length > 2) {
            startSecond = Float.parseFloat(startTimeArr[2]);
        }
        float startTotal = startSecond + 60 * (startMinute + 60 * startHour);

        Log.d("JoinGame", "Time remaining: " + Float.toString((startTotal - currentTotal) / 60.0f));
        return ((startTotal - (float) currentTotal) / 60.0f);
    }

    private void setEndTime(JSONObject response) throws JSONException {
        String endTime = response.getString("end_time");
        Log.d("StartInfo", "Game duration: " + calculateTimeRemainingInMinutes(endTime));
        gameStateController.setGameDuration((long) (calculateTimeRemainingInMinutes(endTime)));
    }

    private void setAllPlayers(JSONObject allPlayersJson) throws JSONException {

        JSONArray allPlayers =  allPlayersJson.getJSONArray("all_players");

        Log.v("StartInfo", "Player JSON: " + allPlayers.toString());
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

    private JsonObjectRequestWithNull volleyNewTargetRequest() throws JSONException {
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
                Log.d("New Target", "Error Response, Something went wrong");
            }
        };

        JSONObject requestBody = new JSONObject();
        requestBody.put("player_id", gameStateController.getPlayerId());

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + NEW_TARGET_URL, requestBody,
                listener, errorListener,setStatusCodeRunnable(),statusCodeRequestMap);
    }

    private void updateTargetPlayer(JSONObject obj) throws JSONException {
        String targetPlayerId = obj.getString("target_player_id");
        gameStateController.updateTargetPlayer(targetPlayerId);

        newTargetConsoleRunnable.run(targetPlayerId);


    }

    @Override
    public void playerUpdateRequest() throws JSONException {
        requestQueue.add(volleyPlayerUpdateRequest());
    }

    private JsonObjectRequestWithNull volleyPlayerUpdateRequest() throws JSONException {
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
                String statusCode = "NaN";
                if (error.networkResponse != null) {
                    statusCode = String.valueOf(error.networkResponse.statusCode);
                }
                Log.d("Update", "Recieved status code " + statusCode);
            }
        };

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + PLAYER_UPDATE_URL,
                playerUpdateRequestBody(), listener, errorListener,setStatusCodeRunnable(), statusCodeRequestMap);
    }

    private void playerUpdate(JSONObject obj) throws JSONException {
        //Do state updates based on JSONObject obj
        updateNearbyPlayers(obj);
        updatePlayerPoints(obj);
        updateLeaderboardPosition(obj);
        updatePlayerLocation(obj);
        updateLocations(obj);
        updateExchangeReceive(obj);
        checkForPlayerStatusChanges(obj);
        checkForMission(obj);
        checkGameOver(obj);
    }

    private void updatePlayerLocation(JSONObject obj) throws JSONException {
        if(obj.has("location")){
            int flag = obj.getInt("location");
            changePlayerLocationRunnable.run(Integer.toString(flag));

            //If at UN
            if(flag == 0){
                disableInteractionsRunnable.run();
            }
            else{
                enableInteractionsRunnable.run();
            }
        }
    }

    private void updateLocations(JSONObject obj) throws JSONException {
        String id;
        String location;
        JSONObject nearbyPlayer;
        JSONArray nearbyPlayerIdsJson = obj.getJSONArray("nearby_players");

        for (int i = 0; i < nearbyPlayerIdsJson.length(); i++) {
            nearbyPlayer = nearbyPlayerIdsJson.getJSONObject(i);
            if(nearbyPlayer.has("id")){
                id = nearbyPlayer.getString("id");
                Log.v("Nearby Players", "Got id" + id);
                if(!id.equals(gameStateController.getPlayerId())) {
                    if (nearbyPlayer.has("location")) {
                        location = Integer.toString(nearbyPlayer.getInt("location"));
                        changeLocationRunnable.run(id, location);
                    }
                }
            }
        }

        JSONObject farPlayer;
        JSONArray farPlayerIdsJson = obj.getJSONArray("far_players");

        for (int i = 0; i < farPlayerIdsJson.length(); i++){
            farPlayer = farPlayerIdsJson.getJSONObject(i);
            if(farPlayer.has("id")){
                id = farPlayer.getString("id");
                Log.v("Far Players", "Got id" + id);
                if(!id.equals(gameStateController.getPlayerId())) {
                    if (farPlayer.has("location")) {
                        location = Integer.toString(farPlayer.getInt("location"));
                        changeLocationRunnable.run(id, location);
                    }
                }
            }
        }
    }

    private void updateNearbyPlayers(JSONObject obj) throws JSONException {
        JSONArray nearbyPlayerIdsJson = obj.getJSONArray("nearby_players");
        List<String> nearbyPlayerIds = new ArrayList<>();
        for (int i = 0; i < nearbyPlayerIdsJson.length(); i++) {
            nearbyPlayerIds.add(nearbyPlayerIdsJson.getJSONObject(i).getString("id"));
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
            if(!reqId.equals("0")) {
                gameStateController.updateExchangeReceive(reqId);
            }
        }
    }

    private void checkForPlayerStatusChanges(JSONObject obj) throws JSONException {
        List<PlayerUpdate> updates = new ArrayList<>();
        String exposedId = "";
        if (obj.has("exposed_by")) {
                exposedId = obj.getString("exposed_by");
                if (!exposedId.equals("0")) {
                    updates.add(PlayerUpdate.TAKEN_DOWN);
                    gameStateController.setExposerId(exposedId);
                    playerTakenDownRunnable.run(exposedId);
                }
        }
        if (obj.has("req_new_target")) {
            boolean reqNewTarget = obj.getBoolean("req_new_target");
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

    private void checkForMission(JSONObject obj) throws JSONException {
        if(obj.has("mission_description")) {
            Log.d("Mission", obj.getString("mission_description"));
            String missionId = obj.getString("mission_description");
            if(!missionId.equals("")) {
                missionUpdateRunnable.run(missionId);
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
        Log.v("Player Update Body", requestBody.toString());

        return requestBody;
    }

    @Override
    public void missionUpdateRequest(InteractionDetails details) throws JSONException {
        requestQueue.add(volleyMissionUpdateRequest(details));
    }

    private JsonObjectRequestWithNull volleyMissionUpdateRequest(final InteractionDetails details) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String key = SERVER_ADDRESS + MISSION_URL;

                if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 200) {
                    missionSuccess(details, response);
                }
                else if(statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 206){
                    missionPending(details, response);
                }
                else if(statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 203){
                    missionFailure(details, response);
                }
                else if(statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 204){
                    missionPending(details, response);
                }
                else if(statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 205){
                    missionCancelled(details, response);
                }
                else {
                    Log.v("Mission", response.toString());
                    details.status = InteractionStatus.ERROR;
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse != null && error.networkResponse.statusCode == 203){
                    try {
                        missionFailure(details, new JSONObject("{\"failure_description\": \"Mission was failed.\"}"));
                    } catch (JSONException e) {
                        Log.d("Mission", e.getMessage());
                    }
                }
                else if(error.networkResponse != null && error.networkResponse.statusCode == 205){
                    try {
                        missionCancelled(details, new JSONObject("{\"failure_description\": \"Mission was cancelled.\"}"));
                    } catch (JSONException e) {
                        Log.d("Mission", e.getMessage());
                    }
                }
                else if(error.networkResponse != null && error.networkResponse.statusCode == 204){
                    missionPending(details, new JSONObject());
                }
                else if(error.networkResponse != null && error.networkResponse.statusCode == 206){
                    missionPending(details, new JSONObject());
                }
                else if(error.networkResponse == null){
                    Log.v("Mission", "Null network response");
                    try {
                        missionCancelled(details, new JSONObject("{\"failure_description\": \"Mission was cancelled.\"}"));
                    } catch (JSONException e){
                        Log.d("Mission", e.getMessage());
                    }
                }
                else {
                    Log.v("Mission", Integer.toString(error.networkResponse.statusCode));
                    try {
                        missionCancelled(details, new JSONObject("{\"failure_description\": \"Mission was cancelled.\"}"));
                    } catch (JSONException e){
                        Log.d("Mission", e.getMessage());
                    }
                }
                statusCode = 0;
            }
        };

        JSONObject missionUpdateBody = new JSONObject();
        missionUpdateBody.put("player_id", gameStateController.getPlayerId());

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + MISSION_URL, missionUpdateBody, listener, errorListener,setStatusCodeRunnable(), statusCodeRequestMap);
    }

    private void missionSuccess(InteractionDetails details, JSONObject obj) {
        String success = "Test Mission Success";

        try {
            details.status = InteractionStatus.SUCCESSFUL;
            Log.d("Mission Success", obj.toString());

            if (obj.has("evidence")) {
                JSONArray evidence = obj.getJSONArray("evidence");

                for (int i = 0; i < evidence.length(); i++) {
                    JSONObject entry = evidence.getJSONObject(i);
                    String playerId = entry.getString("player_id");
                    int evidenceAmount = entry.getInt("amount");
                    gameStateController.increasePlayerIntel(playerId, evidenceAmount);
                    checkPlayerFullIntelAndSend(playerId);

                }
            }


        }
        catch (JSONException e) {
            Log.d("Mission", e.getMessage());
        }

        try{
            if (obj.has("success_description")) {
                success = obj.getString("success_description");
                Log.i("Success Description", success);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        missionSuccessRunnable.run(success);
    }

    private void missionPending(InteractionDetails details, JSONObject obj) {
        details.status = InteractionStatus.IN_PROGRESS;
        if (obj.has("time_remaining")) {
            try {
                details.missionTime = obj.getInt("time_remaining");
            } catch (JSONException e) {
                Log.d("Mission", e.getMessage());
            }
        }
    }


    private void missionFailure(InteractionDetails details, JSONObject obj) {
        details.status = InteractionStatus.FAILED;
        String failure = "Mission Failed";

        if(obj.has("failure_description")){
            try {
                Log.d("Mission Fail GSvr", obj.getString("failure_description"));
                failure = obj.getString("failure_description");
                Log.i("Failure Description", failure);
            } catch (JSONException e) {
                Log.d("Mission", e.getMessage());
            }
        }

        missionFailureRunnable.run(failure);
    }

    private void missionCancelled(InteractionDetails details, JSONObject obj){
        details.status = InteractionStatus.NO_EVIDENCE;
    }

    private void checkPlayerFullIntelAndSend(String playerId){
        if(gameStateController.playerHasFullIntel(playerId)){
            if(!codenameDiscoveredList.contains(playerId)){
                try {
                    Log.i("Codename Discovered : ", playerId);
                    codenameDiscovered(playerId);
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
            else{
                codenameDiscoveredList.add(playerId);
            }

        }
    }

    private void codenameDiscovered(String playerId) throws JSONException{
        requestQueue.add(volleyCodenameDiscovered(playerId));
    }

    private JsonObjectRequestWithNull volleyCodenameDiscovered(final String playerId)throws JSONException{
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response){
                String key = SERVER_ADDRESS + EXCHANGE_REQUEST_URL;

                Log.d("Codename discovered", Integer.toString(statusCode));
                if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 200){
                    Log.v("Codename discovered", "200");
                }
                statusCode = 0;
            }

        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Exchange request", "Error received:" + error.getMessage());
            }
        };

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + CODENAME_DISCOVERED_URL,
                codenameBody(playerId), listener, errorListener, setStatusCodeRunnable(), statusCodeRequestMap);
    }

    private JSONObject codenameBody(String playerId) throws JSONException {
        JSONObject requestBody = new JSONObject();

        requestBody.put("playerId", gameStateController.getPlayerId());


        // Log.d("Network", requestBody.toString());
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

    private JsonObjectRequestWithNull volleyExchangeRequest(final String interacteeId, final InteractionDetails details) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String key = SERVER_ADDRESS + EXCHANGE_REQUEST_URL;

                Log.d("Exchange Request code", Integer.toString(statusCode));
                if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 202) {
                    try {
                        successfulExchange(interacteeId, details, response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 201) {
                    pollExchange(details,response);
                }
                else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 204) {
                    rejectedExchange(details);
                }
                else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 206) {
                    pollExchange(details,response);
                }
                else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 400 ||
                        statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 408 ||
                        statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 404) {
                    unsuccessfulExchange(details);
                }
                else {
                    Log.d("Exchange request", "Error received, code: " + statusCode);
                    details.status = InteractionStatus.ERROR;
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                    // Log.d("Network", "400 Error received");
                    unsuccessfulExchange(details);
                }
                else if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                    rejectedExchange(details);
                }
                else if (error.networkResponse != null && error.networkResponse.statusCode == 408){
                    unsuccessfulExchange(details);
                }
                else if (error.networkResponse != null && error.networkResponse.statusCode == 404){
                    unsuccessfulExchange(details);
                }
                else {
                    Log.d("Exchange request", "Error received:" + error.getMessage());
                    rejectedExchange(details);
                }
                statusCode = 0;
            }
        };

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + EXCHANGE_REQUEST_URL,
                exchangeRequestBody(interacteeId), listener, errorListener, setStatusCodeRunnable(), statusCodeRequestMap);
    }

    private void pollExchange(InteractionDetails details, JSONObject obj){
        details.status = InteractionStatus.IN_PROGRESS;
    }

    private void successfulExchange(String interacteeId, InteractionDetails details, JSONObject obj) throws JSONException {

        if (obj.has("evidence")) {
            JSONArray evidenceGained = obj.getJSONArray("evidence");
            for (int i = 0; i < evidenceGained.length(); i++) {
                JSONObject entry = evidenceGained.getJSONObject(i);
                String playerId = entry.getString("player_id");
                int amount = entry.getInt("amount");
                gameStateController.increasePlayerIntel(playerId, amount);
                checkPlayerFullIntelAndSend(playerId);
                details.gainedIntelPlayerIds.add(playerId);
            }
        }

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
        Log.d("Exchange Requester", gameStateController.getPlayerId());
        Log.d("Exchange Responder", interacteeId);
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
        details.status = InteractionStatus.IN_PROGRESS;
    }

    @Override
    public void exchangeResponse(String interacteeId, int response, InteractionDetails details) throws JSONException {
        requestQueue.add(volleyExchangeResponse(interacteeId, response, details));
    }

    private JsonObjectRequestWithNull volleyExchangeResponse(final String interacteeId, final int playerResponse, final InteractionDetails details) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String key = SERVER_ADDRESS + EXCHANGE_RESPONSE_URL;

                    if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 202) {
                        if (playerResponse == 1) {
                            Log.d("Exchange Response", "Response accept successful");
                            successfulExchange(interacteeId, details, response);
                        }
                    } else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 206) {
                        Log.d("Exchange Response", "Response pending");
                        pendingExchange(interacteeId, details, response);
                    } else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 205) {
                        Log.d("Exchange Response", "Response reject successful");
                        details.status = InteractionStatus.SUCCESSFUL;
                    } else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 408) {
                        details.status = InteractionStatus.FAILED;
                    } else {
                        Log.d("Exchange Response", "Other code recieved: " + statusCode);
                        details.status = InteractionStatus.IN_PROGRESS;
                    }

                } catch (JSONException e) {
                    Log.d("Exchange response", e.getMessage());
                    details.status = InteractionStatus.ERROR;
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 205){         // user rejects it successfully
                    Log.d("Exchange Response", "Response reject successful");
                    details.status = InteractionStatus.SUCCESSFUL;
                }
                else if (error.networkResponse != null && error.networkResponse.statusCode == 408) {   // timeout
                    details.status = InteractionStatus.FAILED;
                }
                else {                          // edge case
                    Log.d("Exchange Response", "Error " + error.getMessage());
                    details.status = InteractionStatus.ERROR;
                }

                statusCode = 0;
            }
        };

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + EXCHANGE_RESPONSE_URL,
                exchangeResponseBody(interacteeId, playerResponse), listener, errorListener, setStatusCodeRunnable(), statusCodeRequestMap);
    }

    //6d - Exchange Response
    private JSONObject exchangeResponseBody(String interacteeId, int response) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("responder_id", gameStateController.getPlayerId());
        requestBody.put("requester_id", interacteeId);
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

    private JsonObjectRequestWithNull volleyInterceptRequest(final String interacteeId, final InteractionDetails details) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String key = SERVER_ADDRESS + INTERCEPT_URL;

                if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 200){
                    try {
                        interceptSuccess(response, details);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 204){
                    interceptFailure(details);
                }
                else if (statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 206 || statusCode == 201){
                    interceptPending(details);
                }
                else {
                    details.status = InteractionStatus.IN_PROGRESS;
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                    interceptFailure(details);
                }
                else if (error.networkResponse != null && (error.networkResponse.statusCode == 206 || error.networkResponse.statusCode == 201)) {
                    interceptPending(details);
                }
                else {
                    interceptError(error, details);
                }

                statusCode = 0;
            }
        };

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + INTERCEPT_URL,
                interceptRequestBody(interacteeId), listener, errorListener, setStatusCodeRunnable(),statusCodeRequestMap);
    }

    private void interceptSuccess(JSONObject obj, InteractionDetails details) throws JSONException {

        if (obj.has("evidence")) {
            JSONArray evidenceGained = obj.getJSONArray("evidence");
            for (int i = 0; i < evidenceGained.length(); i++) {
                JSONObject entry = evidenceGained.getJSONObject(i);

                if(entry.has("player_id")){
                    String playerId = entry.getString("player_id");
                    if(entry.has("amount")){
                        int amount = entry.getInt("amount");
                        gameStateController.increasePlayerIntel(playerId, amount);
                    }
                    checkPlayerFullIntelAndSend(playerId);
                    details.gainedIntelPlayerIds.add(playerId);
                }


            }
        }
        Log.v("Intercept", "Success");
        details.status = InteractionStatus.SUCCESSFUL;
    }

    private void interceptFailure(InteractionDetails details) {
        Log.v("Intercept", "Failure");
        details.status = InteractionStatus.FAILED;
    }

    private void interceptPending(InteractionDetails details) {
        Log.v("Intercept", "Pending");
        details.status = InteractionStatus.IN_PROGRESS;
    }

    private void interceptError(VolleyError error, InteractionDetails details) {
        Integer statusCode = 0;
        if (error.networkResponse != null) {
            statusCode = error.networkResponse.statusCode;
        }
        Log.d("Intercept", "Server error: " + statusCode);
        details.status = InteractionStatus.FAILED;
    }

    private JSONObject interceptRequestBody(String interacteeId) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("target_id", interacteeId);
        requestBody.put("player_id", gameStateController.getPlayerId());
        return requestBody;
    }

    @Override
    public void registerDisableInteractionsRunnable(Runnable runnable){
        this.disableInteractionsRunnable = runnable;
    }

    @Override
    public void registerEnableInteractionsRunnable(Runnable runnable){
        this.enableInteractionsRunnable = runnable;
    }

    @Override
    public void registerMissionUpdateRunnable(StringInputRunnable runnable) {
        this.missionUpdateRunnable = runnable;
    }

    @Override
    public void registerMissionSuccessRunnable(StringInputRunnable runnable) {
        this.missionSuccessRunnable = runnable;
    }

    @Override
    public void registerMissionFailureRunnable(StringInputRunnable runnable) {
        this.missionFailureRunnable = runnable;
    }

    @Override
    public void registerInterceptSuccessRunnable(Runnable runnable) {
        this.interceptSuccessRunnable = runnable;
    }

    @Override
    public void registerChangePlayerLocationRunnable(StringInputRunnable runnable){
        this.changePlayerLocationRunnable = runnable;
    }

    @Override
    public void registerChangeLocationRunnable(TwinInputRunnable runnable){
        this.changeLocationRunnable = runnable;
    }

    @Override
    public void registerNewTargetConsoleRunnable(StringInputRunnable runnable){
        this.newTargetConsoleRunnable = runnable;
    }

    @Override
    public void registerPlayerTakenDownRunnable(StringInputRunnable runnable){
        this.playerTakenDownRunnable = runnable;
    }

    @Override
    public void exposeRequest(String targetId) throws JSONException {
        requestQueue.add(volleyExposeRequest(targetId));
    }

    private JsonObjectRequestWithNull volleyExposeRequest(final String targetId) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                exposeSuccessRunnable.run(targetId);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                exposeFailedRunnable.run();
            }
        };

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + TAKE_DOWN_URL,
                exposeRequestBody(targetId), listener, errorListener,setStatusCodeRunnable(),statusCodeRequestMap);
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
    public void registerExposeSuccessRunnable(StringInputRunnable exposeSuccessRunnable) {
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

    private JsonObjectRequestWithNull volleyIsAtHomeBeaconRequest() throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String key = SERVER_ADDRESS + PLAYER_AT_HOME_URL;

                if(statusCodeRequestMap.containsKey(key) && statusCodeRequestMap.get(key) == 200) {
                    atHomeUpdate(response);
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(statusCode == 400) {
                    Log.d("Network", "No home beacon found");
                }
                statusCode = 0;
            }
        };

        return new JsonObjectRequestWithNull(Request.Method.POST, SERVER_ADDRESS + PLAYER_AT_HOME_URL,
                playerUpdateRequestBody(), listener, errorListener,setStatusCodeRunnable(), statusCodeRequestMap);
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

    private void setStatusCode(int code){
        this.statusCode = code;
    }

    private StringInputRunnable setStatusCodeRunnable() {
        return new StringInputRunnable() {
            @Override
            public void run(final String input) {
                int code = Integer.parseInt(input);
                setStatusCode(code);
            }
        };
    }
}
