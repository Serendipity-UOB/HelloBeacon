package com.bristol.hackerhunt.helloworld.gameplay.controller;

import android.util.Log;

import com.bristol.hackerhunt.helloworld.gameplay.PlayerUpdate;
import com.bristol.hackerhunt.helloworld.gameplay.view.IPlayerListView;
import com.bristol.hackerhunt.helloworld.gameplay.view.IPlayerStatusBarView;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameStateController implements IGameStateController {
    //TODO Consider changing to 10 to better show quantum
    private static final int INTEL_INCREMENT = 20; // a percentage

    private final IPlayerListView playerListController;
    private final IPlayerStatusBarView playerStatusBarController;

    private final Map<String, Map<String, Integer>> beaconMajorMinorRssiMap;
    private final Map<String, String> beaconMajorNameMap;
    private String nearestBeaconMajor;
    private final String homeBeaconMajor;
    private Runnable onNearestBeaconHomeRunnable;

    private final PlayerIdentifiers playerIdentifiers;
    private List<PlayerUpdate> playerUpdates;
    private int points;
    private String leaderboardPosition;

    private final Map<String, PlayerDetails> allPlayersMap; // key: player_id (nfc)
    private List<String> nearbyPlayerIds;
    private String targetPlayerId;

    private boolean gameOver;

    public GameStateController(IPlayerListView playerListController,
                               IPlayerStatusBarView playerStatusBarController,
                               PlayerIdentifiers playerIdentifiers,
                               String homeBeaconMajor,
                               String homeBeaconName) {
        this.playerListController = playerListController;
        this.playerStatusBarController = playerStatusBarController;
        this.playerIdentifiers = playerIdentifiers;
        this.allPlayersMap = new HashMap<>();
        this.nearbyPlayerIds = new ArrayList<>();
        this.playerUpdates = new ArrayList<>();

        this.beaconMajorMinorRssiMap = new HashMap<>();
        this.beaconMajorNameMap = new HashMap<>();
        this.homeBeaconMajor = homeBeaconMajor;
        this.beaconMajorNameMap.put(homeBeaconMajor, homeBeaconName);

        this.points = 0;
        this.leaderboardPosition = "Loading...";
        nearestBeaconMajor = "none";

        this.gameOver = false;
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

    @Override
    public String getHomeBeaconName() {
        return beaconMajorNameMap.get(homeBeaconMajor);
    }

    @Override
    public String getTargetPlayerId() {
        return this.targetPlayerId;
    }

    @Override
    public String getNearestBeaconMajor() {
        return nearestBeaconMajor;
    }

    @Override
    public HashMap<String, String> getPlayerIdRealNameMap() {
        HashMap<String,String> map = new HashMap<>();
        for (String id : allPlayersMap.keySet()) {
            map.put(id, allPlayersMap.get(id).realName);
        }
        return map;
    }

    @Override
    public void setNearestBeaconMajor(String major) {
        this.nearestBeaconMajor = major;
        Log.i("NMajor", "New Nearest Major. Major: " + major);
    }

    @Override
    public void playerIsAtHomeBeacon() {
        if (onNearestBeaconHomeRunnable != null) {
            onNearestBeaconHomeRunnable.run();
        }
    }

    @Override
    public boolean playerHasBeenTakenDown() {
        return playerUpdates.contains(PlayerUpdate.TAKEN_DOWN);
    }

    @Override
    public void resetPlayerTakenDown() {
        if (playerUpdates.contains(PlayerUpdate.TAKEN_DOWN)) {
            playerUpdates.remove(PlayerUpdate.TAKEN_DOWN);
        }
    }

    @Override
    public boolean playersTargetHasBeenTakenDown() {
        return playerUpdates.contains(PlayerUpdate.REQ_NEW_TARGET);
    }

    @Override
    public void resetPlayersTargetHasBeenTakenDown() {
        if (playerUpdates.contains(PlayerUpdate.REQ_NEW_TARGET)) {
            playerUpdates.remove(PlayerUpdate.REQ_NEW_TARGET);
        }
    }

    @Override
    public void increasePlayerIntel(String playerId, int intelIncrement) {
        if (!playerId.equals("0") && allPlayersMap.get(playerId) != null) {
            PlayerDetails pd = allPlayersMap.get(playerId);
            pd.intel = Math.min(100, pd.intel + intelIncrement);
            playerListController.increasePlayerIntel(playerId, intelIncrement);
            if (playerHasFullIntel(playerId)) {
                playerListController.revealPlayerHackerName(playerId, pd.hackerName);
            }
        }
    }

    @Override
    public boolean playerHasFullIntel(String targetId) {
        return (allPlayersMap.get(targetId).intel == 100);
    }

    @Override
    public void loseHalfOfPlayersIntel() {
        for (String id : allPlayersMap.keySet()) {
            if (allPlayersMap.get(id).intel <= 100) {
                int increments = (allPlayersMap.get(id).intel / INTEL_INCREMENT);
                if (increments % 2 == 1) {
                    increments++;
                }
                int decrease = (increments / 2);
                for (int i = 0; i < decrease; i++) {
                    allPlayersMap.get(id).intel = allPlayersMap.get(id).intel - INTEL_INCREMENT;
                    playerListController.decreasePlayerIntel(id, INTEL_INCREMENT);
                }
            }
        }
    }

    @Override
    public void setAllPlayers(List<PlayerIdentifiers> allPlayersIdentifiers) {
        for (PlayerIdentifiers playerIdentifiers : allPlayersIdentifiers) {
            PlayerDetails pd = new PlayerDetails(playerIdentifiers.getRealName(),
                    playerIdentifiers.getHackerName());

            allPlayersMap.put(playerIdentifiers.getPlayerId(), pd);

            playerListController.insertPlayer(playerIdentifiers.getPlayerId(),
                    playerIdentifiers.getRealName());
        }
    }

    @Override
    public boolean allPlayersHaveBeenSet() {
        return (allPlayersMap.keySet().size() > 0);
    }

    @Override
    public String getPlayerId() {
        return this.playerIdentifiers.getPlayerId();
    }

    @Override
    public void updateTargetPlayer(String targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
        String codeName = allPlayersMap.get(targetPlayerId).hackerName;
        playerListController.setTargetCodeName(codeName);
        playerStatusBarController.setPlayerTargetCodeName(codeName);
    }

    @Override
    public void updateNearbyPlayers(List<String> playerIds) {
        List<String> nearbyPlayers = new ArrayList<>();
        for (String id : playerIds) {
            if (allPlayersMap.containsKey(id)) {
                nearbyPlayers.add(id);
            }
        }
        nearbyPlayerIds = nearbyPlayers;
        playerListController.updateNearbyPlayers(nearbyPlayers);
    }

    @Override
    public void updatePoints(int points) {
        this.points = points;
        playerStatusBarController.setPlayerPoints(Integer.toString(points));
    }

    @Override
    public void updateLeaderboardPosition(String position) {
        this.leaderboardPosition = position;
        playerStatusBarController.setPlayerLeaderboardPosition(position);
    }

    @Override
    public void updateExchangeReceive(String reqId) {
        //TODO Define behaviour, likely a "console view" thing
    }

    @Override
    public void handleNewMission(String missionId) {
        //TODO Define behaviour, also likely a console view thing
    }

    @Override
    public void updateBeacon(String major, String minor, int rssi) {
        if (beaconMajorMinorRssiMap.containsKey(major)) {
            beaconMajorMinorRssiMap.get(major).put(minor, rssi);
        }
        else {
            Map<String, Integer> minorRssiMap = new HashMap<>();
            minorRssiMap.put(minor, rssi);
            beaconMajorMinorRssiMap.put(major, minorRssiMap);
        }
    }

    @Override
    public Set<String> getAllBeaconMajors() {
        return beaconMajorMinorRssiMap.keySet();
    }

    @Override
    public Set<String> getAllBeaconMinors(String major) {
        return beaconMajorMinorRssiMap.get(major).keySet();
    }

    public int getBeaconRssi(String major) {
        Map<String, Integer> beaconMinorRssiMap = beaconMajorMinorRssiMap.get(major);
        int maxRssi = Integer.MIN_VALUE;
        for (String minor : beaconMinorRssiMap.keySet()) {
            if (beaconMinorRssiMap.get(minor) > maxRssi) {
                maxRssi = beaconMinorRssiMap.get(minor);
            }
        }
        return maxRssi;
    }

    @Override
    public int getBeaconRssi(String major, String minor) {
        return beaconMajorMinorRssiMap.get(major).get(minor);
    }

    @Override
    public void updateStatus(List<PlayerUpdate> updates) {
        this.playerUpdates = updates;
    }

    @Override
    public void setOnNearestBeaconBeingHomeBeaconListener(Runnable runnable) {
        this.onNearestBeaconHomeRunnable = runnable;
    }

    @Override
    public boolean playerHasNonZeroIntel(String targetId) {
        return allPlayersMap.get(targetId).intel > 0;
    }

    @Override
    public void removeBeacon(String major, String minor) {
        if (beaconMajorMinorRssiMap.containsKey(major)) {
            Map<String, Integer> minorMap = beaconMajorMinorRssiMap.get(major);
            minorMap.remove(minor);
        }
    }

    @Override
    public void setGameOver() {
        gameOver = true;
    }

    @Override
    public boolean gameHasEnded() {
        return gameOver;
    }
}
