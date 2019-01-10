package com.bristol.hackerhunt.helloworld.gameplay;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState {

    private static final int INTEL_INCREMENT = 20; // a percentage

    private final PlayerListController playerListController;
    private final PlayerStatusBarController playerStatusBarController;

    private final PlayerIdentifiers playerIdentifiers;
    private final Map<String, Integer> beaconMinorRssiMap;
    private List<PlayerUpdate> playerUpdates;
    private int points;
    private String position;

    private final Map<String, PlayerDetails> allPlayersMap; // key: player_id (nfc)
    private List<String> nearbyPlayerIds;
    private String targetPlayerId;

    public GameState(PlayerListController playerListController,
                     PlayerStatusBarController playerStatusBarController,
                     PlayerIdentifiers playerIdentifiers) {
        this.playerListController = playerListController;
        this.playerStatusBarController = playerStatusBarController;
        this.playerIdentifiers = playerIdentifiers;
        this.beaconMinorRssiMap = new HashMap<>();
        this.allPlayersMap = new HashMap<>();
        this.nearbyPlayerIds = new ArrayList<>();
        this.playerUpdates = new ArrayList<>();
        this.playerUpdates.add(PlayerUpdate.REQ_NEW_TARGET);

        this.points = 0;
        this.position = "Loading...";
    }

    private class PlayerDetails {
        final String realName;
        final String hackerName;
        int intel;

        PlayerDetails(String realName, String hackerName) {
            this.realName = realName;
            this.hackerName = hackerName;
            this.intel = 0;
        }
    }

    public void increasePlayerIntel(String playerId) {
        PlayerDetails pd = allPlayersMap.get(playerId);
        pd.intel = pd.intel + INTEL_INCREMENT;
        playerListController.increasePlayerIntel(playerId, INTEL_INCREMENT);
    }

    public void setAllPlayers(List<PlayerIdentifiers> allPlayersIdentifiers) {
        for (PlayerIdentifiers playerIdentifiers : allPlayersIdentifiers) {
            PlayerDetails pd = new PlayerDetails(playerIdentifiers.getRealName(),
                    playerIdentifiers.getHackerName());

            allPlayersMap.put(playerIdentifiers.getNfcId(), pd);

            playerListController.insertPlayer(playerIdentifiers.getNfcId(),
                    playerIdentifiers.getRealName()); //todo: change to identifiers.
        }
    }

    public boolean allPlayersHaveBeenSet() {
        return (allPlayersMap.keySet().size() > 0);
    }

    public JSONObject getJsonPlayerUpdate() throws JSONException {
        JSONObject playerUpdate = new JSONObject();
        JSONArray beacons = new JSONArray();

        for (String minor : beaconMinorRssiMap.keySet()) {
            JSONObject obj = new JSONObject();
            obj.put("beacon_minor", minor);
            obj.put("rssi", beaconMinorRssiMap.get(minor));
            beacons.put(obj);
        }

        playerUpdate.put("player_id", playerIdentifiers.getNfcId());
        playerUpdate.put("beacons", beacons);
        return playerUpdate;
    }

    public String getPlayerId() {
        return this.playerIdentifiers.getNfcId();
    }

    public void updateTargetPlayer(String targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
        String hackerName = allPlayersMap.get(targetPlayerId).hackerName;
        playerStatusBarController.setPlayerTargetHackerName(hackerName);
    }

    public void updateNearbyPlayers(List<String> playerIds) {
        nearbyPlayerIds = playerIds;
        playerListController.updateNearbyPlayers(playerIds);
    }

    public void updatePoints(int points) {
        this.points = points;
        playerStatusBarController.setPlayerPoints(Integer.toString(points));
    }

    public void updatePosition(String position) {
        this.position = position;
        playerStatusBarController.setPlayerLeaderboardPosition(position);
    }

    public void updateBeacon(String minor, int rssi) {
        beaconMinorRssiMap.put(minor, rssi);
    }

    public void updateStatus(List<PlayerUpdate> updates) {
        this.playerUpdates = updates;
    }
}
