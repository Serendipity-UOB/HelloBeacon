package com.bristol.hackerhunt.helloworld.leaderboard;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bristol.hackerhunt.helloworld.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardServerRequestController implements ILeaderboardServerRequestController {

    private final String SERVER_ADDRESS;
    private final String GET_INFO_URL;

    private final RequestQueue requestQueue;

    LeaderboardServerRequestController(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);

        this.SERVER_ADDRESS = context.getString(R.string.server_address);
        this.GET_INFO_URL = context.getString(R.string.game_info_request);
    }

    @Override
    public void getInfoRequest(List<LeaderboardItem> leaderboardList) throws JSONException {
        // placeholder
        String response = "{\"leaderboard\":[{\"player_id\":\"55\",\"player_name\":\"Rockie\",\"score\":1000},{\"player_id\":\"1\",\"player_name\":\"Tom\",\"score\":565},{\"player_id\":\"2\",\"player_name\":\"Tilly\",\"score\":500},{\"player_id\":\"3\",\"player_name\":\"Louis\",\"score\":488},{\"player_id\":\"4\",\"player_name\":\"David\",\"score\":450},{\"player_id\":\"5\",\"player_name\":\"Jack\",\"score\":433},{\"player_id\":\"7\",\"player_name\":\"Tilo\",\"score\":400},{\"player_id\":\"8\",\"player_name\":\"Beth\",\"score\":388},{\"player_id\":\"9\",\"player_name\":\"Becky\",\"score\":350},{\"player_id\":\"10\",\"player_name\":\"Bradley\",\"score\":300}]}";
        JSONObject obj = new JSONObject(response);
        addLeaderboardItems(obj, leaderboardList);

        // TODO: requestQueue.add(volleyGetInfoRequest(leaderboardList));
    }

    private JsonObjectRequest volleyGetInfoRequest(final List<LeaderboardItem> leaderboardList)
            throws JSONException {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    addLeaderboardItems(response, leaderboardList);
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

        return new JsonObjectRequest(Request.Method.GET, SERVER_ADDRESS + GET_INFO_URL, null,
                listener, errorListener);
    }

    private void addLeaderboardItems(JSONObject leaderboardJson, List<LeaderboardItem> leaderboardList)
            throws JSONException {
        JSONArray leaderboard = leaderboardJson.getJSONArray("leaderboard");
        List<LeaderboardItem> items = new ArrayList<>();

        for (int i = 0; i < leaderboard.length(); i++) {
            LeaderboardItem item = new LeaderboardItem();
            JSONObject itemJson = leaderboard.getJSONObject(i);

            item.playerId = itemJson.getString("player_id");
            item.playerName = itemJson.getString("player_name");
            item.score = itemJson.getInt("score");

            items.add(item);
        }

        leaderboardList.addAll(items);
    }
}
