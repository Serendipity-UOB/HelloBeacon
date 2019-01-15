package com.bristol.hackerhunt.helloworld.leaderboard;

import org.json.JSONException;

import java.util.List;

public interface ILeaderboardServerRequestController {

    /**
     * Request leaderboard information.
     * GET /endInfo
     *
     * @param leaderboardList a list of leaderboard items.
     */
    void getInfoRequest(List<LeaderboardItem> leaderboardList) throws JSONException;
}
