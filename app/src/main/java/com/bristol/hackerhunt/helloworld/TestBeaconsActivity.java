package com.bristol.hackerhunt.helloworld;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.SpaceListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSpaceListener;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.spec.EddystoneFrameType;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestBeaconsActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PERMISSIONS = 100;
    private TextView mShowCount;
    private ProximityManager proximityManager;

    private Map<String, Double> beaconMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        KontaktSDK.initialize(this);
        this.mShowCount = (TextView) findViewById(R.id.show_count);
        this.beaconMap = new HashMap<>();

        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.setIBeaconListener(createIBeaconListener());
        proximityManager.setEddystoneListener(createEddystoneListener());
        proximityManager.setSpaceListener(createSpaceListener());

        proximityManager.configuration()
                .scanMode(ScanMode.BALANCED)
                .scanPeriod(ScanPeriod.create(3000, 2000))
                .activityCheckConfiguration(ActivityCheckConfiguration.create(5000, 1000))
                .forceScanConfiguration(ForceScanConfiguration.DISABLED)
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5))
                .rssiCalculator(RssiCalculators.DEFAULT)
                .cacheFileName("Example")
                .resolveShuffledInterval(3)
                .monitoringEnabled(true)
                .monitoringSyncInterval(10)
                .eddystoneFrameTypes(Arrays.asList(EddystoneFrameType.UID, EddystoneFrameType.URL));

        // Set up the button to go to the test request page.
        final Button button = (Button) findViewById(R.id.go_to_test_request);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TestBeaconsActivity.this, TestRequestActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startScanning();
    }

    @Override
    protected void onStop() {
        proximityManager.stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;
        super.onDestroy();
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }

    //Since Android Marshmallow starting a Bluetooth Low Energy scan requires permission from location group.
    private void checkPermissions() {
        int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult) {
            //Permission not granted so we ask for it. Results are handled in onRequestPermissionsResult() callback.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (REQUEST_CODE_PERMISSIONS == requestCode) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location permissions are mandatory to use BLE features on Android 6.0 or higher", Toast.LENGTH_LONG).show();
        }
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                super.onIBeaconDiscovered(ibeacon, region);
                String uuid = ibeacon.getUniqueId();
                String log = "Beacon discovered: " + uuid;
                Log.i("Beacon", log);
                double rssi = ibeacon.getDistance();

                beaconMap.put(uuid, rssi);
                writeBeaconInformation();
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeaconDevices, IBeaconRegion region) {
                for (String id : beaconMap.keySet()) {
                    beaconMap.put(id, 0.0);
                }

                // super.onIBeaconsUpdated(iBeaconDevices, region);
                Log.i("Bluetooth",  "Beacon updates");
                for (IBeaconDevice device : iBeaconDevices) {
                    String uuid = device.getUniqueId();
                    double rssi = device.getRssi();
                    String log = "Beacon: " + uuid + ", Signal strength: " + rssi;
                    Log.i("Beacon", log);
                    beaconMap.put(uuid, rssi);

                    writeBeaconInformation();
                }
            }

            @Override
            public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
                String uuid = ibeacon.getUniqueId();
                beaconMap.put(uuid, 0.0);

                writeBeaconInformation();
            }
        };
    }

    private EddystoneListener createEddystoneListener() {
        return new SimpleEddystoneListener() {
            @Override
            public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                // super.onEddystoneDiscovered(eddystone, namespace);
                // String uuid = eddystone.getUniqueId();
                // String log = "Beacon discovered: " + uuid;
                // Log.i("Beacon", log);
                // beaconMap.put(uuid, eddystone.getRssi());

                // writeBeaconInformation();
            }

            @Override
            public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
                // super.onEddystonesUpdated(eddystones, namespace);
                // Log.i("Bluetooth",  "Beacon updates");
                //for (IEddystoneDevice eddystone : eddystones) {
                //    String uuid = eddystone.getUniqueId();
                //    int rssi = eddystone.getRssi();
                //    String log = "Beacon: " + uuid + ", Signal strength: " + rssi;
                //    Log.i("Beacon", log);
                //    beaconMap.put(uuid, rssi);

                //    writeBeaconInformation();
                //}
            }

            @Override
            public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                // super.onEddystoneLost(eddystone, namespace);
                // String uuid = eddystone.getUniqueId();
                // String log = "Beacon lost: " + uuid;
                // Log.i("Bluetooth", log);
                // beaconMap.remove(uuid);

                // writeBeaconInformation();
            }
        };
    }

    private SpaceListener createSpaceListener() {
        return new SimpleSpaceListener() {
            @Override
            public void onRegionEntered(IBeaconRegion region) {
                super.onRegionEntered(region);
                Log.i("Sample", "Region entered: " + region.toString());
            }

            @Override
            public void onNamespaceEntered(IEddystoneNamespace namespace) {
                super.onNamespaceEntered(namespace);
                Log.i("Sample", "Namespace entered.");
            }
        };
    }

    private void writeBeaconInformation() {
        StringBuilder out = new StringBuilder();
        String closestBeacon= "";
        double closestStrength = Integer.MIN_VALUE;
        for (String uuid : this.beaconMap.keySet()) {
           double rssi = this.beaconMap.get(uuid);
           String log = "Beacon: " + uuid + ", Strength: " + rssi + ".\n";
           if (rssi > closestStrength && rssi != 0) {
               closestBeacon = uuid;
               closestStrength = rssi;
           }
           out.append(log);
        }
        out.append("\n");
        out.append("Closest beacon: " + closestBeacon + ".\n");
        mShowCount.setText(out.toString());
    }

    private double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }
}
