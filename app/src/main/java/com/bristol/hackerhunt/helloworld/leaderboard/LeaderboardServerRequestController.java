package com.bristol.hackerhunt.helloworld.leaderboard;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardServerRequestController implements ILeaderboardServerRequestController {

    private final String SERVER_ADDRESS;
    private final String GET_INFO_URL;

    private final RequestQueue requestQueue;

    private final Map<String,String> playerIdsRealNameMap;
    private final PlayerIdentifiers playerIdentifiers;

    LeaderboardServerRequestController(Context context, Map<String,String> playerIdsRealNameMap, PlayerIdentifiers playerIdentifiers) {
        this.requestQueue = Volley.newRequestQueue(context);

        this.playerIdentifiers = playerIdentifiers;
        this.playerIdsRealNameMap = playerIdsRealNameMap;

        this.SERVER_ADDRESS = context.getString(R.string.server_address);
        this.GET_INFO_URL = context.getString(R.string.game_info_request);
    }

    @Override
    public void getInfoRequest(List<LeaderboardItem> leaderboardList) throws JSONException {
        // placeholder
        String response = "{\"leaderboard\":[{\"player_id\":\"0\",\"score\":1000},{\"player_id\":\"1\",\"score\":565},{\"player_id\":\"2\",\"score\":500}]}";
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
            item.playerName = getPlayerName(item.playerId);
            item.score = itemJson.getInt("score");

            items.add(item);
        }

        leaderboardList.addAll(items);
    }

    private String getPlayerName(String playerId) {
        if (playerIdsRealNameMap.containsKey(playerId)) {
            return playerIdsRealNameMap.get(playerId);
        }
        else if (playerIdentifiers.getPlayerId().equals(playerId)) {
            return playerIdentifiers.getRealName();
        }
        else {
            return "Unknown";
        }
    }
}
