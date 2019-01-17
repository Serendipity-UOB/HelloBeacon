package com.bristol.hackerhunt.helloworld.gameplay;

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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BeaconController implements IBeaconController {

    private final IGameStateController gameStateController;
    private ProximityManager proximityManager;

    BeaconController(Context context, IGameStateController gameStateController) {
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
                .activityCheckConfiguration(ActivityCheckConfiguration.DISABLED)
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

                String nearestMinor = "";
                int nearestRssi = Integer.MIN_VALUE;

                for (IBeaconDevice device : iBeaconDevices) {
                    String minor = Integer.toString(device.getMinor());
                    int rssi = device.getRssi();

                    if (rssi > nearestRssi) {
                        nearestRssi = rssi;
                        nearestMinor = minor;
                    }

                    gameStateController.updateBeacon(minor, rssi);
                }
                gameStateController.setNearestBeaconMinor(nearestMinor);
            }
        };
    }
}
