package com.talmir.mickinet.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.fragments.DeviceDetailFragment;
import com.talmir.mickinet.fragments.DeviceListFragment;
import com.talmir.mickinet.helpers.background.IDeviceActionListener;
import com.talmir.mickinet.helpers.background.broadcastreceivers.BatteryPowerConnectionReceiver;
import com.talmir.mickinet.helpers.background.broadcastreceivers.WiFiDirectBroadcastReceiver;
import com.talmir.mickinet.helpers.background.services.CountDownService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HomeActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, IDeviceActionListener {

    // ++++++++++++++++++++++++++++++++ Permissions ++++++++++++++++++++++++++++++++++++ //
    private static final int INITIAL_REQUEST = 603;
    private static final int CAMERA_REQUEST = 376;
    private static final int STORAGE_REQUEST = 759;
    private static final int CONTACTS_REQUEST = 623;

    private static final String CAMERA_PERMISSIONS = Manifest.permission.CAMERA;
    private static final String CONTACTS_READ_PERMISSION = Manifest.permission.READ_CONTACTS;
    private static final String READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WRITE_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final String[] INITIAL_PERMISSIONS = {
            CAMERA_PERMISSIONS,
            CONTACTS_READ_PERMISSION,
            READ_STORAGE_PERMISSION,
            WRITE_STORAGE_PERMISSION
    };

    private boolean canAccessCamera() {
        // holy crap! WTF ? why I wrote Location thing instead of CAMERA ?
        // return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
        return hasPermission(CAMERA_PERMISSIONS);
    }

    private boolean canAccessExternalStorage() {
        return hasPermission(READ_STORAGE_PERMISSION) && hasPermission(WRITE_STORAGE_PERMISSION);
    }

    private boolean canAccessContacts() {
        return hasPermission(CONTACTS_READ_PERMISSION);
    }

    private boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission));
    }
    // -------------------------------- Permissions ------------------------------------ //

    // +++++++++++++++++++++++++++ WiFi Direct specific ++++++++++++++++++++++++++++++++ //
    private final IntentFilter intentFilter = new IntentFilter();
    private static WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private static WifiP2pManager.Channel channel;
    private BroadcastReceiver wifiDirectBroadcastReceiver = null;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);

        if (fragmentList != null)
            fragmentList.clearPeers();

        if (fragmentDetails != null)
            fragmentDetails.resetViews();

//        if (CountDownService.isRunning())
//            stopService(new Intent(getApplicationContext(), CountDownService.class));

        start_discovery.setVisibility(View.VISIBLE);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reasonCode) {
                if (reasonCode == WifiP2pManager.ERROR)
                    Toast.makeText(getApplicationContext(), R.string.discovery_error_1, Toast.LENGTH_LONG).show();
                else if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED)
                    Toast.makeText(getApplicationContext(), R.string.discovery_error_2, Toast.LENGTH_LONG).show();
                else if (reasonCode == WifiP2pManager.BUSY)
                    Toast.makeText(getApplicationContext(), R.string.discovery_error_3, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), R.string.discovery_error_4, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int reasonCode) {
                if (reasonCode == WifiP2pManager.ERROR)
                    Toast.makeText(getApplicationContext(), R.string.discovery_error_1, Toast.LENGTH_LONG).show();
                else if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED)
                    Toast.makeText(getApplicationContext(), R.string.discovery_error_2, Toast.LENGTH_LONG).show();
                else if (reasonCode == WifiP2pManager.BUSY)
                    Toast.makeText(getApplicationContext(), R.string.discovery_error_3, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), R.string.discovery_error_4, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(getApplicationContext(), R.string.channel_lost, Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(getApplicationContext(), getMainLooper(), this);
        } else {
            Toast.makeText(getApplicationContext(), R.string.connection_lost_permanently, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {
        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), R.string.aborting_connection, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        if (reasonCode == WifiP2pManager.ERROR)
                            Toast.makeText(getApplicationContext(), R.string.discovery_error_1, Toast.LENGTH_LONG).show();
                        else if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED)
                            Toast.makeText(getApplicationContext(), R.string.discovery_error_2, Toast.LENGTH_LONG).show();
                        else if (reasonCode == WifiP2pManager.BUSY)
                            Toast.makeText(getApplicationContext(), R.string.discovery_error_3, Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), R.string.discovery_error_4, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
    // --------------------------- WiFi Direct specific -------------------------------- //


    private BroadcastReceiver batteryInfoBroadcastReceiver = null;
    private FloatingActionButton start_discovery;

    // Great article!
    // https://medium.com/@chrisbanes/appcompat-v23-2-age-of-the-vectors-91cbafa87c88#.59mn8eem4
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread t = new Thread(() -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if (!prefs.getBoolean("firstTimeRun?", false))
                startActivity(new Intent(getApplicationContext(), IntroductionActivity.class));
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (!canAccessCamera() || !canAccessExternalStorage() || !canAccessContacts())
                        requestPermissions(INITIAL_PERMISSIONS, INITIAL_REQUEST);
            }
        });
        t.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        File rootDir = new File(Environment.getExternalStorageDirectory() + "/MickiNet/");
        rootDir.mkdirs();

        copyRawFile(R.raw.file_receive);

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(getApplicationContext(), getMainLooper(), null);

        start_discovery = findViewById(R.id.start_discover);
        start_discovery.setOnClickListener(v -> {
            if (!isWifiP2pEnabled) {
                AlertDialog wifiOnOffAlertDialog = new AlertDialog.Builder(getApplicationContext()).create();
                wifiOnOffAlertDialog.setTitle(getString(R.string.turn_on_wifi));
                wifiOnOffAlertDialog.setMessage(getString(R.string.turn_on_wifi_message));
                wifiOnOffAlertDialog.setIcon(R.drawable.ic_signal_wifi_off);
                wifiOnOffAlertDialog.setCancelable(true);
                wifiOnOffAlertDialog.show();
                return;
            }
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
            fragment.onInitiateDiscovery();
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), R.string.discovery_started, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int reasonCode) {
                    if (reasonCode == WifiP2pManager.ERROR)
                        Toast.makeText(getApplicationContext(), R.string.discovery_error_1, Toast.LENGTH_LONG).show();
                    else if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED)
                        Toast.makeText(getApplicationContext(), R.string.discovery_error_2, Toast.LENGTH_LONG).show();
                    else if (reasonCode == WifiP2pManager.BUSY)
                        Toast.makeText(getApplicationContext(), R.string.discovery_error_3, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(), R.string.discovery_error_4, Toast.LENGTH_LONG).show();
                }
            });
        });
//        Intent intent = getIntent();
//        if (intent.getAction().equals(Intent.ACTION_SEND) && intent.getType() != null) {
//            Toast toast = Toast.makeText(this, "send intent", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
//            Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
//            DeviceDetailFragment.executeSendIntent(this, fileUri);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (!canAccessCamera())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requestPermissions(new String[]{CAMERA_PERMISSIONS}, STORAGE_REQUEST);
                break;
            case STORAGE_REQUEST:
                if (!canAccessExternalStorage())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requestPermissions(new String[]{READ_STORAGE_PERMISSION, WRITE_STORAGE_PERMISSION}, STORAGE_REQUEST);
                break;
            case CONTACTS_REQUEST:
                if (!canAccessContacts())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requestPermissions(new String[]{CONTACTS_READ_PERMISSION}, STORAGE_REQUEST);
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset_statistics:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.action_file_statistics:
                startActivity(new Intent(getApplicationContext(), FileStatisticsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        wifiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(wifiDirectBroadcastReceiver, intentFilter);
        batteryInfoBroadcastReceiver = new BatteryPowerConnectionReceiver();
        registerReceiver(batteryInfoBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onPause() {
        unregisterReceiver(wifiDirectBroadcastReceiver);
        unregisterReceiver(batteryInfoBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (CountDownService.isRunning())
            stopService(new Intent(getApplicationContext(), CountDownService.class));
        try {
            unregisterReceiver(wifiDirectBroadcastReceiver);
            wifiDirectBroadcastReceiver = null;
            unregisterReceiver(batteryInfoBroadcastReceiver);
            batteryInfoBroadcastReceiver = null;
        } catch (IllegalArgumentException ignored) {}
        super.onDestroy();
    }

    /**
     * Copies a raw resource into the alarms directory on the device's shared storage
     *
     * @param resID The resource ID of the raw resource to copy, in the form of R.raw.*
     */
    private void copyRawFile(int resID) {
        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS);
        // Make sure the directory exists
        // noinspection ResultOfMethodCallIgnored
        path.mkdirs();
        File outFile = new File(path, "MickiNet default.mp3");
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getResources().openRawResource(resID);
            outputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, bytesRead);
        } catch (Exception ignored) {

        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                // Means there was an error trying to close the streams, so do nothing
            }
        }
    }
}
