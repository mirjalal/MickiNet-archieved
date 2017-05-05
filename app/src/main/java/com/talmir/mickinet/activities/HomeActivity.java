package com.talmir.mickinet.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import com.talmir.mickinet.helpers.IDeviceActionListener;
import com.talmir.mickinet.helpers.WiFiDirectBroadcastReceiver;

public class HomeActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, IDeviceActionListener {

    public static final String TAG = "com.talmir.mickinet";

    /**
     * ++++++++++++++++++++++++++++++++ Permissions ++++++++++++++++++++++++++++++++++++
     */
    private static final int INITIAL_REQUEST = 603;
    private static final int CAMERA_REQUEST = 376;
    private static final int STORAGE_REQUEST = 759;
    private static final int CONTACTS_REQUEST = 623;

    private static final String CAMERA_PERMISSIONS = Manifest.permission.CAMERA;
    private static final String CONTACTS_PERMISSION = Manifest.permission.READ_CONTACTS;
    private static final String READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WRITE_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final String[] INITIAL_PERMISSIONS = {
            CAMERA_PERMISSIONS,
//            CONTACTS_PERMISSION,
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
        return hasPermission(CONTACTS_PERMISSION);
    }

    private boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission));
    }
    /** -------------------------------- Permissions ------------------------------------ */

    /**
     * +++++++++++++++++++++++++++ WiFi Direct specific ++++++++++++++++++++++++++++++++
     */
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

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
                if (reason == WifiP2pManager.ERROR)
                    Toast.makeText(HomeActivity.this, "The operation failed due to an internal error.", Toast.LENGTH_LONG).show();
                else if (reason == WifiP2pManager.P2P_UNSUPPORTED)
                    Toast.makeText(HomeActivity.this, "The operation failed because p2p is unsupported on the device.", Toast.LENGTH_LONG).show();
                else if (reason == WifiP2pManager.BUSY)
                    Toast.makeText(HomeActivity.this, "The device is busy and unable to service the request.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(HomeActivity.this, "An unknown error occurred.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                if (reasonCode == WifiP2pManager.ERROR)
                    Toast.makeText(HomeActivity.this, "The operation failed due to an internal error.", Toast.LENGTH_LONG).show();
                else if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED)
                    Toast.makeText(HomeActivity.this, "The operation failed because p2p is unsupported on the device.", Toast.LENGTH_LONG).show();
                else if (reasonCode == WifiP2pManager.BUSY)
                    Toast.makeText(HomeActivity.this, "The device is busy and unable to service the request.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(HomeActivity.this, "Unknown error occurred.", Toast.LENGTH_LONG).show();
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
    /**
     * --------------------------- WiFi Direct specific --------------------------------
     */

    // Great article!
    // https://medium.com/@chrisbanes/appcompat-v23-2-age-of-the-vectors-91cbafa87c88#.59mn8eem4
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean previouslyStarted = prefs.getBoolean("loseVirginity?", false);
                if (!previouslyStarted)
                    startActivity(new Intent(HomeActivity.this, IntroductionActivity.class));
            }
        });
        t.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!canAccessCamera() || !canAccessExternalStorage() || !canAccessContacts())
                requestPermissions(INITIAL_PERMISSIONS, INITIAL_REQUEST);

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        channel = manager.initialize(this, getMainLooper(), null);

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
                if (canAccessCamera())
                    Toast.makeText(this, "camera thing", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "camera thing no", Toast.LENGTH_LONG).show();
                break;
            case STORAGE_REQUEST:
                if (canAccessExternalStorage())
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_discover:
                if (!isWifiP2pEnabled) {
                    AlertDialog wifiOnOffAlertDialog = new AlertDialog.Builder(this).create();
                    wifiOnOffAlertDialog.setTitle("Turn on WiFi?");
                    wifiOnOffAlertDialog.setMessage("WiFi is turned off. Before starting discovery MickiNet needs to enable WiFi.");
                    wifiOnOffAlertDialog.setIcon(R.drawable.ic_signal_wifi_off);
                    wifiOnOffAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "turn on", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            wifi.setWifiEnabled(true);
                            try {
                                Thread.sleep(700); // .5 sec is enough to wait...
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
                            fragment.onInitiateDiscovery();
                            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(HomeActivity.this, "Discovery started", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(int reasonCode) {
                                    Toast.makeText(HomeActivity.this, "Discovery failed.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                    wifiOnOffAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    wifiOnOffAlertDialog.setCancelable(true);
                    wifiOnOffAlertDialog.show();
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
            case R.id.action_settings:
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                return true;
            //        SharedPreferences sh_preference = PreferenceManager.getDefaultSharedPreferences(c);
//        String strRingtonePreference = sh_preference.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
//        boolean vibrate = sh_preference.getBoolean("notifications_new_message_vibrate", true);
//        Log.e("prefKey", Uri.parse(strRingtonePreference).toString());
//        Log.e("prefKey", vibrate + "");
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
        unregisterReceiver(receiver);
    }
}
