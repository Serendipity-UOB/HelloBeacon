package com.bristol.hackerhunt.helloworld.gameplay;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameplayServerRequestsController {
    private final GameState gameState;

    public GameplayServerRequestsController(GameState gameState) {
        this.gameState = gameState;
    }

    // TODO: GET /startInfo
    public void startInfoRequest() throws JSONException {
        // this is just a placeholder.
        String response = "{\"all_players\":[{\"id\":\"1\",\"real_name\":\"Tom\",\"hacker_name\":\"Tom\"},{\"id\":\"2\",\"real_name\":\"Tilly\",\"hacker_name\":\"cutie_kitten\"},{\"id\":\"3\",\"real_name\":\"Louis\",\"hacker_name\":\"Louis\"},{\"id\":\"4\",\"real_name\":\"David\",\"hacker_name\":\"David\"},{\"id\":\"5\",\"real_name\":\"Jack\",\"hacker_name\":\"Jack\"},{\"id\":\"7\",\"real_name\":\"Tilo\",\"hacker_name\":\"Tilo\"},{\"id\":\"8\",\"real_name\":\"Beth\",\"hacker_name\":\"Beth\"},{\"id\":\"9\",\"real_name\":\"Becky\",\"hacker_name\":\"Becky\"},{\"id\":\"10\",\"real_name\":\"Bradley\",\"hacker_name\":\"Bradley\"}]}";
        JSONObject obj = new JSONObject(response);
        JSONArray allPlayers =  obj.getJSONArray("all_players");

        List<PlayerIdentifiers> l = new ArrayList<>();
        for (int i = 0; i < allPlayers.length(); i++) {
            l.add(jsonToPlayerIdentifiers(allPlayers.getJSONObject(i)));
        }
        gameState.setAllPlayers(l);
    }

    // TODO: POST /newTarget { player_id }
    public void newTargetRequest() throws JSONException {
        // this is just a placeholder.
        String response = "{\"target_player_id\": \"2\"}";
        JSONObject obj = new JSONObject(response);

        String targetPlayerId = obj.getString("target_player_id");
        gameState.updateTargetPlayer(targetPlayerId);
    }

    // TODO: POST /playerUpdate { player_id, beacons[{beacon_minor, rssi}] }
    public void playerUpdateRequest() throws JSONException {
        // this is just a placeholder.
        String response = "{\"nearby_players\":[\"2\",\"3\",\"4\"],\"state\":{\"points\":516,\"position\":\"2\"}}";
        JSONObject obj = new JSONObject(response);

        JSONArray nearbyPlayerIdsJson = obj.getJSONArray("nearby_players");
        List<String> nearbyPlayerIds = new ArrayList<>();
        for (int i = 0; i < nearbyPlayerIdsJson.length(); i++) {
            nearbyPlayerIds.add(nearbyPlayerIdsJson.getString(i));
        }
        gameState.updateNearbyPlayers(nearbyPlayerIds);

        JSONObject state = obj.getJSONObject("state");
        if (state.has("points")) {
            int points = state.getInt("points");
            gameState.updatePoints(points);
        }
        if (state.has("position")) {
            String position = state.getString("position");
            gameState.updatePosition(position);
        }

        if (!obj.has("update")) {
            gameState.updateStatus(new ArrayList<PlayerUpdate>());
        }
    }

    // TODO: POST /exchange { interacter_id, interactee_id }
    public void exchangeRequest() {

    }

    // TODO: POST /takeDown { player_id, target_id }
    public void takeDownRequest() {

    }

    private PlayerIdentifiers jsonToPlayerIdentifiers(JSONObject obj) throws JSONException {
        String playerRealName = obj.getString("real_name");
        String playerHackerName = obj.getString("hacker_name");
        String playerId = obj.getString("id");

        return new PlayerIdentifiers(playerRealName, playerHackerName, playerId);
    }
}
