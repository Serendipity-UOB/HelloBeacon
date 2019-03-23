package com.bristol.hackerhunt.helloworld.joinGame;

public class GameInfo {
    public Double minutesToStart;
    public Integer numberOfPlayers;
    public String startBeaconMajor;
    public String startBeaconName;
    public CountdownStatus countdownStatus;

    public GameInfo() {
        this.countdownStatus = CountdownStatus.WAITING_FOR_RESPONSE;
    }
}
