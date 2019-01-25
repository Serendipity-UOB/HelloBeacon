package com.bristol.hackerhunt.helloworld.gameplay;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameStateController implements IGameStateController {

    private static final int INTEL_INCREMENT = 20; // a percentage

    private final IPlayerListController playerListController;
    private final IPlayerStatusBarController playerStatusBarController;

    private final Map<String, Integer> beaconMinorRssiMap;
    private final Map<String, String> beaconMinorNameMap;
    private String nearestBeaconMinor;
    private final String homeBeaconMinor;
    private Runnable onNearestBeaconHomeRunnable;

    private final PlayerIdentifiers playerIdentifiers;
    private List<PlayerUpdate> playerUpdates;
    private int points;
    private String leaderboardPosition;

    private final Map<String, PlayerDetails> allPlayersMap; // key: player_id (nfc)
    private List<String> nearbyPlayerIds;
    private String targetPlayerId;

    public GameStateController(IPlayerListController playerListController,
                               IPlayerStatusBarController playerStatusBarController,
                               PlayerIdentifiers playerIdentifiers,
                               String homeBeaconMinor,
                               String homeBeaconName) {
        this.playerListController = playerListController;
        this.playerStatusBarController = playerStatusBarController;
        this.playerIdentifiers = playerIdentifiers;
        this.allPlayersMap = new HashMap<>();
        this.nearbyPlayerIds = new ArrayList<>();
        this.playerUpdates = new ArrayList<>();

        this.beaconMinorRssiMap = new HashMap<>();
        this.beaconMinorNameMap = new HashMap<>();
        this.homeBeaconMinor = homeBeaconMinor;
        this.beaconMinorNameMap.put(homeBeaconMinor, homeBeaconName);

        this.points = 0;
        this.leaderboardPosition = "Loading...";
        nearestBeaconMinor = "none";
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
        return beaconMinorNameMap.get(homeBeaconMinor);
    }

    @Override
    public String getTargetPlayerId() {
        return this.targetPlayerId;
    }

    @Override
    public String getNearestBeaconMinor() {
        return nearestBeaconMinor;
    }

    @Override
    public void setNearestBeaconMinor(String minor) {
        this.nearestBeaconMinor = minor;

        if (onNearestBeaconHomeRunnable != null && nearestBeaconMinor.equals(homeBeaconMinor)) {
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
    public void increasePlayerIntel(String playerId) {
        PlayerDetails pd = allPlayersMap.get(playerId);
        pd.intel = Math.min(100, pd.intel + INTEL_INCREMENT);
        playerListController.increasePlayerIntel(playerId, INTEL_INCREMENT);
        if (playerHasFullIntel(playerId)) {
            playerListController.revealPlayerHackerName(playerId, pd.hackerName);
        }
    }

    @Override
    public boolean playerHasFullIntel(String targetId) {
        return (allPlayersMap.get(targetId).intel == 100);
    }

    @Override
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

    @Override
    public void setAllPlayers(List<PlayerIdentifiers> allPlayersIdentifiers) {
        for (PlayerIdentifiers playerIdentifiers : allPlayersIdentifiers) {
            PlayerDetails pd = new PlayerDetails(playerIdentifiers.getRealName(),
                    playerIdentifiers.getHackerName());

            allPlayersMap.put(playerIdentifiers.getNfcId(), pd);

            playerListController.insertPlayer(playerIdentifiers.getNfcId(),
                    playerIdentifiers.getRealName());
        }
    }

    @Override
    public boolean allPlayersHaveBeenSet() {
        return (allPlayersMap.keySet().size() > 0);
    }

    @Override
    public String getPlayerId() {
        return this.playerIdentifiers.getNfcId();
    }

    @Override
    public void updateTargetPlayer(String targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
        String hackerName = allPlayersMap.get(targetPlayerId).hackerName;
        playerStatusBarController.setPlayerTargetHackerName(hackerName);
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
    public void updateBeacon(String minor, int rssi) {
        beaconMinorRssiMap.put(minor, rssi);
    }

    @Override
    public Set<String> getAllBeaconMinors() {
        return beaconMinorRssiMap.keySet();
    }

    public int getBeaconRssi(String minor) {
        return beaconMinorRssiMap.get(minor);
    }

    @Override
    public void updateStatus(List<PlayerUpdate> updates) {
        this.playerUpdates = updates;
    }

    @Override
    public void setOnNearestBeaconBeingHomeBeaconListener(Runnable runnable) {
        this.onNearestBeaconHomeRunnable = runnable;
    }
}
