package com.talmir.transferfileoverwifidirect.activities;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.talmir.transferfileoverwifidirect.R;
import com.talmir.transferfileoverwifidirect.fragments.DeviceDetailFragment;
import com.talmir.transferfileoverwifidirect.fragments.DeviceListFragment;
import com.talmir.transferfileoverwifidirect.helpers.IDeviceActionListener;
import com.talmir.transferfileoverwifidirect.helpers.WiFiDirectBroadcastReceiver;

public class HomeActivity extends AppCompatActivity implements
        WifiP2pManager.ChannelListener,
        IDeviceActionListener
{
    /** ++++++++++++++++++++++++++++++++ Permissions ++++++++++++++++++++++++++++++++++++ */
    private static final String[] INITIAL_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int INITIAL_REQUEST = 0x4e;
    private static final int CAMERA_REQUEST = INITIAL_REQUEST + 1;
    private static final int STORAGE_REQUEST = INITIAL_REQUEST + 2;

    private boolean canAccessCamera() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean canReadExternalStorage() {
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean canWriteExternalStorage() {
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission));
        return true;
    }
    /** -------------------------------- Permissions ------------------------------------ */



    /** +++++++++++++++++++++++++++ WiFi Direct specific ++++++++++++++++++++++++++++++++ */
    public static final String TAG = "com.talmir.transferfileoverwifidirect";

    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
//    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equalsIgnoreCase("MyBroadcast"))
//                disconnect();
//        }
//    };

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
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null)
            fragmentList.clearPeers();

        if (fragmentDetails != null)
            fragmentDetails.resetViews();
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        // cihaz haqqinda etrafli melumat
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(HomeActivity.this, "Connect failed. Retry.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(
                    this,
                    "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {
        /**
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(HomeActivity.this, "Aborting connection", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(HomeActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
    /** --------------------------- WiFi Direct specific -------------------------------- */


    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean previouslyStarted = prefs.getBoolean("loseVirginity?", false);
                if(!previouslyStarted)
                    startActivity(new Intent(HomeActivity.this, IntroductionActivity.class));
            }
        });
        t.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            if (!canAccessCamera() || !canReadExternalStorage() || !canWriteExternalStorage())
//                requestPermissions(INITIAL_PERMISSIONS, INITIAL_REQUEST);

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_SEND)) {
            Toast toast = Toast.makeText(this, "send intent", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (canAccessCamera())
                    Toast.makeText(this, "camera thing", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "camera thing no", Toast.LENGTH_LONG).show();
                break;
            case STORAGE_REQUEST:
                if (canReadExternalStorage() && canWriteExternalStorage())
                    Toast.makeText(this, "storage thing", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "storage no", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_items, menu);
        return true;
    }

    /**
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @SuppressLint("LongLogTag")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {
                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else
                    Log.e(TAG, "channel or manager is null");
                return true;
            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(this, R.string.p2p_off_warning, Toast.LENGTH_LONG).show();
                    return true;
                }
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(HomeActivity.this, "Discovery started", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(HomeActivity.this, "Discovery failed. Reason code: " + reasonCode, Toast.LENGTH_LONG).show();
                    }
                });
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
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
//        try {
            unregisterReceiver(receiver);
//        } catch (Exception ignored) {}
    }
}
