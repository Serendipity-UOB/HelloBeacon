package com.bristol.hackerhunt.helloworld.gameplay;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameStateController {

    private static final int INTEL_INCREMENT = 20; // a percentage

    private final PlayerListController playerListController;
    private final PlayerStatusBarController playerStatusBarController;

    private final PlayerIdentifiers playerIdentifiers;
    private final Map<String, Integer> beaconMinorRssiMap;
    private List<PlayerUpdate> playerUpdates;
    private final String homeBeacon;
    private int points;
    private String position;

    private final Map<String, PlayerDetails> allPlayersMap; // key: player_id (nfc)
    private List<String> nearbyPlayerIds;
    private String targetPlayerId;

    public GameStateController(PlayerListController playerListController,
                               PlayerStatusBarController playerStatusBarController,
                               PlayerIdentifiers playerIdentifiers,
                               String homeBeacon) {
        this.playerListController = playerListController;
        this.playerStatusBarController = playerStatusBarController;
        this.playerIdentifiers = playerIdentifiers;
        this.beaconMinorRssiMap = new HashMap<>();
        this.allPlayersMap = new HashMap<>();
        this.nearbyPlayerIds = new ArrayList<>();
        this.playerUpdates = new ArrayList<>();

        this.homeBeacon = homeBeacon;
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

    public String getHomeBeacon() {
        return homeBeacon;
    }

    public String getTargetPlayerId() {
        return this.targetPlayerId;
    }

    public boolean playerHasBeenTakenDown() {
        return playerUpdates.contains(PlayerUpdate.TAKEN_DOWN);
    }

    public void resetPlayerTakenDown() {
        if (playerUpdates.contains(PlayerUpdate.TAKEN_DOWN)) {
            playerUpdates.remove(PlayerUpdate.TAKEN_DOWN);
        }
    }

    public boolean playersTargetHasBeenTakenDown() {
        return playerUpdates.contains(PlayerUpdate.REQ_NEW_TARGET);
    }

    public void resetPlayersTargetHasBeenTakenDown() {
        if (playerUpdates.contains(PlayerUpdate.REQ_NEW_TARGET)) {
            playerUpdates.remove(PlayerUpdate.REQ_NEW_TARGET);
        }
    }

    public void increasePlayerIntel(String playerId) {
        PlayerDetails pd = allPlayersMap.get(playerId);
        pd.intel = Math.min(100, pd.intel + INTEL_INCREMENT);
        playerListController.increasePlayerIntel(playerId, INTEL_INCREMENT);
        if (playerHasFullIntel(playerId)) {
            playerListController.revealPlayerHackerName(playerId, pd.hackerName);
        }
    }

    public boolean playerHasFullIntel(String targetId) {
        return (allPlayersMap.get(targetId).intel == 100);
    }

    public void loseHalfOfPlayersIntel() {
        for (String id : allPlayersMap.keySet()) {
            if (allPlayersMap.get(id).intel < 100) {
                int decrease = (allPlayersMap.get(id).intel / 2) / INTEL_INCREMENT;
                for (int i = 0; i < decrease; i++) {
                   playerListController.decreasePlayerIntel(id, INTEL_INCREMENT);
                }
            }
        }
    }

    public void setAllPlayers(List<PlayerIdentifiers> allPlayersIdentifiers) {
        for (PlayerIdentifiers playerIdentifiers : allPlayersIdentifiers) {
            PlayerDetails pd = new PlayerDetails(playerIdentifiers.getRealName(),
                    playerIdentifiers.getHackerName());

            allPlayersMap.put(playerIdentifiers.getNfcId(), pd);

            playerListController.insertPlayer(playerIdentifiers.getNfcId(),
                    playerIdentifiers.getRealName());
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

    public Set<String> getAllBeaconMinors() {
        return beaconMinorRssiMap.keySet();
    }

    public int getBeaconRssi(String minor) {
        return beaconMinorRssiMap.get(minor);
    }

    public void updateStatus(List<PlayerUpdate> updates) {
        this.playerUpdates = updates;
    }
}
