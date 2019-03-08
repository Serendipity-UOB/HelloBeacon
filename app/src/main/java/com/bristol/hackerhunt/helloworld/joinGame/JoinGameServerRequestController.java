package com.bristol.hackerhunt.helloworld.joinGame;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bristol.hackerhunt.helloworld.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class JoinGameServerRequestController implements IJoinGameServerRequestController {

    private final String SERVER_ADDRESS;
    private final String GAME_INFO_URL;
    private final String JOIN_GAME_URL;

    private final RequestQueue requestQueue;
    private final GameInfo gameInfo;

    private int statusCode = 0;

    JoinGameServerRequestController(Context context, GameInfo gameInfo) {
        this.requestQueue = Volley.newRequestQueue(context);
        this.gameInfo = gameInfo;

        this.SERVER_ADDRESS = context.getString(R.string.server_address);
        this.GAME_INFO_URL = context.getString(R.string.game_info_request);
        this.JOIN_GAME_URL = context.getString(R.string.join_game_request);
    }

    @Override
    public void cancelAllRequests() {
        requestQueue.stop();
    }

    @Override
    public void gameInfoRequest() {
         // this is a placeholder
        // String response = "{\"start_time\":\"17:59:50\",\"number_players\":2}";
        // JSONObject obj = new JSONObject(response);
        // updateGameInfo(obj);

        requestQueue.add(volleyGameInfoRequest());
    }

    private JsonObjectRequest volleyGameInfoRequest() {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (statusCode == 200) {
                        updateGameInfo(response);
                    }
                    else if (statusCode == 204) {
                        gameInfo.minutesToStart = -1.0;
                        gameInfo.numberOfPlayers = -1;
                        Log.d("JoinGame", "Status code 204");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                statusCode = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Network", error.getMessage());

                // an error indicates that there's no game available to join.
                gameInfo.minutesToStart = -1.0;
                gameInfo.numberOfPlayers = -1;
            }
        };

        return new JsonObjectRequest(Request.Method.GET, SERVER_ADDRESS + GAME_INFO_URL, new JSONObject(),
                listener, errorListener) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    statusCode = response.statusCode;
                }
                return super.parseNetworkResponse(response);
            }
        };
    }

    private void updateGameInfo(JSONObject gameInfoJson) throws JSONException {
        String timeToStartJson = gameInfoJson.getString("start_time");
        double minutesToStart = calculateTimeRemainingInMinutes(timeToStartJson);

        int numberOfPlayers = gameInfoJson.getInt("number_players");

        if (gameInfo.minutesToStart == null) {
            gameInfo.minutesToStart = minutesToStart;
        }
        gameInfo.numberOfPlayers = numberOfPlayers;
    }

    private float calculateTimeRemainingInMinutes(String startTime) {
        Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        int currentHour = c2.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c2.get(Calendar.MINUTE);
        int currentSecond = c2.get(Calendar.SECOND);

        int currentTotal = currentSecond + 60 * (currentMinute + 60 * currentHour);

        String[] startTimeArr = startTime.split(":");
        float startHour = Float.parseFloat(startTimeArr[0]);
        float startMinute = Float.parseFloat(startTimeArr[1]);
        float startSecond = Float.parseFloat(startTimeArr[2]);
        float startTotal = startSecond + 60 * (startMinute + 60 * startHour);

        Log.d("JoinGame", "Time remaining: " + Float.toString((startTotal - currentTotal) / 60));
        return ((startTotal - currentTotal) / 60);
    }

    @Override
    public void joinGameRequest(String playerId) throws JSONException {
        // this is a placeholder
        // String response = "{\"home_beacon_minor\":\"4\",\"home_beacon_name\":\"Beacon A\"}";
        // JSONObject obj = new JSONObject(response);
        // gameInfo.startBeaconMinor = obj.getString("home_beacon_minor");
        // gameInfo.startBeaconName = obj.getString("home_beacon_name");

        requestQueue.add(volleyJoinGameRequest(playerId));
    }

    private JsonObjectRequest volleyJoinGameRequest(String playerId) throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    gameInfo.startBeaconMajor = response.getString("home_beacon_major");
                    gameInfo.startBeaconName = response.getString("home_beacon_name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Log.d("Network", Integer.toString(error.networkResponse.statusCode));
                }
                noGameAvailableToJoin();
            }
        };

        JSONObject requestBody = new JSONObject();
        requestBody.put("player_id", playerId);

        return new JsonObjectRequest(Request.Method.POST, SERVER_ADDRESS + JOIN_GAME_URL, requestBody,
                listener, errorListener);
    }

    private void noGameAvailableToJoin() {
        gameInfo.minutesToStart = -1.0;
        gameInfo.numberOfPlayers = -1;
    }
}
