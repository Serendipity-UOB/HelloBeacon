package com.bristol.hackerhunt.helloworld.leaderboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardServerRequestController {

    // TODO: GET /endInfo
    public void getInfoRequest(List<LeaderboardItem> leaderboardList) throws JSONException {
        // placeholder
        String response = "{\"leaderboard\":[{\"player_id\":\"55\",\"player_name\":\"Rockie\",\"score\":1000},{\"player_id\":\"1\",\"player_name\":\"Tom\",\"score\":565},{\"player_id\":\"2\",\"player_name\":\"Tilly\",\"score\":500},{\"player_id\":\"3\",\"player_name\":\"Louis\",\"score\":488},{\"player_id\":\"4\",\"player_name\":\"David\",\"score\":450},{\"player_id\":\"5\",\"player_name\":\"Jack\",\"score\":433},{\"player_id\":\"7\",\"player_name\":\"Tilo\",\"score\":400},{\"player_id\":\"8\",\"player_name\":\"Beth\",\"score\":388},{\"player_id\":\"9\",\"player_name\":\"Becky\",\"score\":350},{\"player_id\":\"10\",\"player_name\":\"Bradley\",\"score\":300}]}";
        JSONObject obj = new JSONObject(response);

        JSONArray leaderboard = obj.getJSONArray("leaderboard");
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
