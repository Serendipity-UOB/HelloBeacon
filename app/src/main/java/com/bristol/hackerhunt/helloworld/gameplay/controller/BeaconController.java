package com.bristol.hackerhunt.helloworld.gameplay.controller;

import android.content.Context;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.spec.EddystoneFrameType;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BeaconController implements IBeaconController {

    private static int INACTIVITY_TIMEOUT = 5000;
    private static int INACTIVITY_CHECK_PERIOD = 1000;

    private final IGameStateController gameStateController;
    private ProximityManager proximityManager;

    public BeaconController(Context context, IGameStateController gameStateController) {
        this.gameStateController = gameStateController;
        KontaktSDK.initialize(context);
        initializeProximityManager(context);
    }

    @Override
    public void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }

    @Override
    public void stopScanning() {
        proximityManager.stopScanning();
        proximityManager.disconnect();
    }

    private void initializeProximityManager(Context context) {
        proximityManager = ProximityManagerFactory.create(context);
        proximityManager.setIBeaconListener(createIBeaconListener());

        proximityManager.configuration()
                .scanMode(ScanMode.BALANCED)
                .scanPeriod(ScanPeriod.create(3000, 2000))
                .activityCheckConfiguration(ActivityCheckConfiguration.create(INACTIVITY_TIMEOUT, INACTIVITY_CHECK_PERIOD))
                .forceScanConfiguration(ForceScanConfiguration.DISABLED)
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5))
                .rssiCalculator(RssiCalculators.DEFAULT)
                .cacheFileName("Example")
                .resolveShuffledInterval(3)
                .monitoringEnabled(true)
                .monitoringSyncInterval(10)
                .eddystoneFrameTypes(Arrays.asList(EddystoneFrameType.UID, EddystoneFrameType.URL));
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                super.onIBeaconDiscovered(ibeacon, region);

                String minor = Integer.toString(ibeacon.getMinor());
                int rssi = ibeacon.getRssi();

                gameStateController.updateBeacon(minor, rssi);
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeaconDevices, IBeaconRegion region) {
                super.onIBeaconsUpdated(iBeaconDevices, region);

                for (String minor : gameStateController.getAllBeaconMinors()) {
                    gameStateController.updateBeacon(minor, 0);
                }

                String nearestMinor = "";
                int nearestRssi = Integer.MIN_VALUE;

                for (IBeaconDevice device : iBeaconDevices) {
                    String minor = Integer.toString(device.getMinor());
                    int rssi = device.getRssi();

                    if (rssi > nearestRssi && rssi <= 0) {
                        nearestRssi = rssi;
                        nearestMinor = minor;
                    }

                    gameStateController.updateBeacon(minor, rssi);
                }

                gameStateController.setNearestBeaconMinor(nearestMinor);
            }

            @Override
            public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
                String minor = Integer.toString(ibeacon.getMinor());
                gameStateController.updateBeacon(minor, 0);

                // Update nearest minor.
                if (gameStateController.getNearestBeaconMinor().equals(minor)) {
                    Integer maxValue = Integer.MIN_VALUE;
                    String nearestMinor = "";
                    for (String m : gameStateController.getAllBeaconMinors()) {
                        int rssi = gameStateController.getBeaconRssi(m);
                        if (rssi > maxValue && rssi != 0) {
                            maxValue = rssi;
                            nearestMinor = m;
                        }
                    }
                    gameStateController.setNearestBeaconMinor(nearestMinor);
                }
            }
        };
    }
}
