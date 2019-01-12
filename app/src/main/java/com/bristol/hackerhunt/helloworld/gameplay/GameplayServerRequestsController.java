package com.bristol.hackerhunt.helloworld.gameplay;

import com.bristol.hackerhunt.helloworld.model.InteractionDetails;
import com.bristol.hackerhunt.helloworld.model.InteractionStatus;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameplayServerRequestsController {
    private final GameStateController gameStateController;

    public GameplayServerRequestsController(GameStateController gameStateController) {
        this.gameStateController = gameStateController;
    }

    // TODO: GET /startInfo
    public void startInfoRequest() throws JSONException {
        // this is just a placeholder.
        String response = "{\"all_players\":[{\"id\":\"1\",\"real_name\":\"Tom\",\"hacker_name\":\"Tom\"},{\"id\":\"2\",\"real_name\":\"Tilly\",\"hacker_name\":\"cutie_kitten\"},{\"id\":\"3\",\"real_name\":\"Louis\",\"hacker_name\":\"Louis\"},{\"id\":\"4\",\"real_name\":\"David\",\"hacker_name\":\"CookingKing\"},{\"id\":\"5\",\"real_name\":\"Jack\",\"hacker_name\":\"falafel\"},{\"id\":\"7\",\"real_name\":\"Tilo\",\"hacker_name\":\"Tilo\"},{\"id\":\"8\",\"real_name\":\"Beth\",\"hacker_name\":\"Beth\"},{\"id\":\"9\",\"real_name\":\"Becky\",\"hacker_name\":\"Becky\"},{\"id\":\"10\",\"real_name\":\"Bradley\",\"hacker_name\":\"Bradley\"}]}";
        JSONObject obj = new JSONObject(response);
        JSONArray allPlayers =  obj.getJSONArray("all_players");

        List<PlayerIdentifiers> l = new ArrayList<>();
        for (int i = 0; i < allPlayers.length(); i++) {
            l.add(jsonToPlayerIdentifiers(allPlayers.getJSONObject(i)));
        }
        gameStateController.setAllPlayers(l);
    }

    // TODO: POST /newTarget { player_id }
    public void newTargetRequest() throws JSONException {
        // this is just a placeholder.
        String response = "{\"target_player_id\": \"2\"}";
        JSONObject obj = new JSONObject(response);

        String targetPlayerId = obj.getString("target_player_id");
        gameStateController.updateTargetPlayer(targetPlayerId);
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
        gameStateController.updateNearbyPlayers(nearbyPlayerIds);

        JSONObject state = obj.getJSONObject("state");
        if (state.has("points")) {
            int points = state.getInt("points");
            gameStateController.updatePoints(points);
        }
        if (state.has("position")) {
            String position = state.getString("position");
            gameStateController.updatePosition(position);
        }

        if (!obj.has("update")) {
            gameStateController.updateStatus(new ArrayList<PlayerUpdate>());
        }
    }

    // TODO: POST /exchange { interacter_id, interactee_id }
    public void exchangeRequest(String interacteeId, InteractionDetails details) throws JSONException {
        // this is just a placeholder (assuming success).
        String response = "{\"secondary_id\":\"5\"}";
        JSONObject obj = new JSONObject(response);

        // TODO: check if interaction successful

        // assuming successful:
        String secondaryId = obj.getString("secondary_id");
        details.gainedIntelPlayerIds.add(interacteeId);
        details.gainedIntelPlayerIds.add(secondaryId);
        details.status = InteractionStatus.SUCCESSFUL;
    }

    // TODO: POST /takeDown { player_id, target_id }
    public void takeDownRequest(String targetId) {

    }

    private PlayerIdentifiers jsonToPlayerIdentifiers(JSONObject obj) throws JSONException {
        String playerRealName = obj.getString("real_name");
        String playerHackerName = obj.getString("hacker_name");
        String playerId = obj.getString("id");

        return new PlayerIdentifiers(playerRealName, playerHackerName, playerId);
    }
}
