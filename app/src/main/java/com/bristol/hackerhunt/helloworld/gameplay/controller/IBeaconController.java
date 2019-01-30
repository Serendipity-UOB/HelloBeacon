package com.bristol.hackerhunt.helloworld.gameplay.controller;

/**
 * A controller used to make use of logic with relation to the Kontakt beacons.
 */
public interface IBeaconController {

    /**
     * Begin searching for beacons.
     */
    void startScanning();

    /**
     * Finish searching for beacons.
     */
    void stopScanning();

}
