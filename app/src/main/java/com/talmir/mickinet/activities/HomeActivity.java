package com.talmir.mickinet.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.talmir.mickinet.R;
import com.talmir.mickinet.fragments.DeviceDetailFragment;
import com.talmir.mickinet.fragments.DeviceListFragment;
import com.talmir.mickinet.helpers.MixedUtils;
import com.talmir.mickinet.helpers.adapters.ReceivedFilesListAdapter;
import com.talmir.mickinet.helpers.adapters.SentFilesListAdapter;
import com.talmir.mickinet.helpers.background.IDeviceActionListener;
import com.talmir.mickinet.helpers.background.broadcastreceivers.WiFiDirectBroadcastReceiver;
import com.talmir.mickinet.helpers.background.services.CountDownService;
import com.talmir.mickinet.helpers.background.tasks.FileSender;
import com.talmir.mickinet.helpers.background.tasks.Zipper;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesViewModel;
import com.talmir.mickinet.helpers.room.sent.SentFilesViewModel;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Objects;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class HomeActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, IDeviceActionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // +++++++++++++++++++++++++++ WiFi Direct specific ++++++++++++++++++++++++++++++++ //
    private final IntentFilter intentFilter = new IntentFilter();
    private static WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private static WifiP2pManager.Channel channel;
    private WiFiDirectBroadcastReceiver wifiDirectBroadcastReceiver = null;

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

        start_discovery.show();
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
                Objects.requireNonNull(fragment.getView()).setVisibility(View.GONE);
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


    private static final int LOCATION_REQUEST = 1249;

    private BroadcastReceiver batteryInfoBroadcastReceiver = null;
    private GoogleApiClient googleApiClient;
    private FloatingActionButton start_discovery;

    private static SentFilesListAdapter mSentFilesListAdapter;
    public static SentFilesViewModel mSentFilesViewModel;

    public static ReceivedFilesViewModel mReceivedFilesViewModel;
    private static ReceivedFilesListAdapter mReceivedFilesListAdapter;

    @Contract(pure = true)
    public static SentFilesViewModel getSentFilesViewModel() {
        return mSentFilesViewModel;
    }

    @Contract(pure = true)
    public static ReceivedFilesViewModel getReceivedFilesViewModel() {
        return mReceivedFilesViewModel;
    }

    @Contract(pure = true)
    public static SentFilesListAdapter getSentFilesListAdapter() {
        return mSentFilesListAdapter;
    }

    @Contract(pure = true)
    public static ReceivedFilesListAdapter getReceivedFilesListAdapter() {
        return mReceivedFilesListAdapter;
    }

    // https://medium.com/@chrisbanes/appcompat-v23-2-age-of-the-vectors-91cbafa87c88#.59mn8eem4
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        // “The (Complete) Android Splash Screen Guide” by @elvisnchidera
        // https://android.jlelse.eu/the-complete-android-splash-screen-guide-c7db82bce565
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseAnalytics.getInstance(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (prefs.getBoolean("firstTimeRun?", Boolean.TRUE)) {
            new MaterialShowcaseView.Builder(this)
                    .setTarget(DeviceListFragment.deviceDetailConstraintLayoutRef.get())
                    .setDismissOnTargetTouch(true)
                    .setMaskColour(R.color.colorAccent)
                    .setShapePadding(32)
                    .setDismissText(getString(R.string.got_it))
                    .setContentText(getString(R.string.showcase1_content))
                    .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                    .withRectangleShape()
                    .singleUse("cardViewShow") // provide a unique ID used to ensure it is only shown once
                    .show().addShowcaseListener(new IShowcaseListener() {
                @Override
                public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                }

                @Override
                public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                    new MaterialShowcaseView.Builder(HomeActivity.this)
                            .setTarget(start_discovery)
                            .setDismissOnTargetTouch(true)
                            .setMaskColour(R.color.colorAccent)
                            .setShapePadding(32)
                            .setDismissText(getString(R.string.okay_got_it))
                            .setContentText(getString(R.string.showcase2_content))
                            .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                            .withCircleShape()
                            .singleUse("fabShow") // provide a unique ID used to ensure it is only shown once
                            .show();
                }
            });
            startActivity(new Intent(HomeActivity.this, IntroActivity.class));
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            if (canAccessCamera() || canAccessExternalStorage() || canAccessContacts())
//                requestPermissions(INITIAL_PERMISSIONS, INITIAL_REQUEST);

        MixedUtils.createNestedFolders();

        copyRawFile();

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
                AlertDialog wifiOnOffAlertDialog = new AlertDialog.Builder(this).create();
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

        mSentFilesViewModel = ViewModelProviders.of(HomeActivity.this).get(SentFilesViewModel.class);
        mSentFilesListAdapter = new SentFilesListAdapter(this);
        mSentFilesViewModel.getAllSentFiles().observe(HomeActivity.this, mSentFilesListAdapter::setSentFiles);

        mReceivedFilesViewModel = ViewModelProviders.of(HomeActivity.this).get(ReceivedFilesViewModel.class);
        mReceivedFilesListAdapter = new ReceivedFilesListAdapter(this);
        mReceivedFilesViewModel.getAllReceivedFiles().observe(HomeActivity.this, mReceivedFilesListAdapter::setReceivedFiles);

        if (getIntent() != null &&
            (Intent.ACTION_SEND.equals(getIntent().getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()))
        )
            onNewIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (wifiDirectBroadcastReceiver != null && wifiDirectBroadcastReceiver.getConnectionStatus()) {
            // handle data receive from other applications when device is connected
            handleData(intent, "tempZipBackup", false);
        } else {
            if (Intent.ACTION_SEND.equals(getIntent().getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()) && intent.getData() != null) {
                final String[] procName = new String[1];
                final AlertDialog alert = new AlertDialog.Builder(this).create();
                final View postponedProcessLayout = LayoutInflater.from(this).inflate(R.layout.postponed_process_name, null);
                final TextInputLayout til = postponedProcessLayout.findViewById(R.id.processNameParent);
                final EditText editText = postponedProcessLayout.findViewById(R.id.processName);

                Toast.makeText(this, "action" + intent.getAction(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "type" + intent.getType(), Toast.LENGTH_SHORT).show();

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        final String s1 = s.toString();
                        if (s1.length() < 1) {
                            // use R.string.enter_dev_name_error - don't give attention to its name :|
                            til.setError(getString(R.string.enter_dev_name_error));
                            alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            final File _f = new File(getFilesDir().toString() + "/backup/" + s1 + ".zip");
                            if (_f.exists()) {
                                til.setError("Try another name");
                                alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                            } else {
                                procName[0] = s1;
                                til.setError(null);
                                alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                // ask user to create the job schedule
                alert.setTitle("Postpone sending?");
                alert.setMessage("Your device doesn't have a connection. We'll will ask you to send your files when you connected. To begin processing please give a name.");
                alert.setView(postponedProcessLayout);
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "create", (dialog, which) -> {
                    // handle data and work offline
                    handleData(intent, procName[0], true);
                });
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> alert.dismiss());
                alert.show();
                // initially disable POSITIVE_BUTTON
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset_statistics:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.action_file_statistics:
                startActivity(new Intent(getApplicationContext(), FileStatisticsActivity.class));
                mSentFilesListAdapter.getSentFilesCountByTypes();
                mReceivedFilesListAdapter.getReceivedFilesCountByTypes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        wifiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(wifiDirectBroadcastReceiver, intentFilter);
//        batteryInfoBroadcastReceiver = new BatteryPowerConnectionReceiver();
//        registerReceiver(batteryInfoBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onStop() {
        unregisterReceiver(wifiDirectBroadcastReceiver);
//        unregisterReceiver(batteryInfoBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (CountDownService.isRunning())
            stopService(new Intent(getApplicationContext(), CountDownService.class));
        try {
            unregisterReceiver(wifiDirectBroadcastReceiver);
            wifiDirectBroadcastReceiver = null;
//            unregisterReceiver(batteryInfoBroadcastReceiver);
//            batteryInfoBroadcastReceiver = null;
        } catch (IllegalArgumentException ignored) {}
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case LOCATION_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        // TODO: start discovery
                        break;
                    case Activity.RESULT_CANCELED:
                        // the user was asked to change settings, but chose not to
                        Toast.makeText(getApplicationContext(), "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
            default: break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkLocationSettings() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                // All location settings are satisfied. The client can initialize location
                // requests here.

                // TODO: start discovery
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(HomeActivity.this, LOCATION_REQUEST);
                        } catch (IntentSender.SendIntentException | ClassCastException ignored) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(getApplicationContext(), "Location settings are not satisfied. However, we have no way to fix the settings.", Toast.LENGTH_SHORT).show();
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    private void handleData(@NotNull Intent intent, String procName, boolean handleOffline) {
        final String action = intent.getAction();
        final String type = intent.getType();

        Uri uri;
        String[] params = new String[3];
        params[0] = DeviceDetailFragment.getIpAddressByDeviceType();
        final String path = "/storage/emulated/0/MickiNet/";

        if (Intent.ACTION_SEND.equals(action) && type != null && intent.getData() != null) {
            Log.e("send type is", "single");
            uri = intent.getData();
            params[1] = uri.toString();
            params[2] = MixedUtils.getFileName(this, uri);

            new FileSender(this, params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            try {
                String inner;
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(params[2].substring(params[2].lastIndexOf('.') + 1));
                if (mimeType != null) {
                    if (mimeType.startsWith("image"))
                        inner = "Photos/";
                    else if (mimeType.startsWith("video"))
                        inner = "Videos/";
                    else if (mimeType.startsWith("music") || mimeType.startsWith("audio"))
                        inner = "Media/";
                    else if (mimeType.equals("application/vnd.android.package-archive"))
                        inner = "APKs/";
                    else
                        inner = "Others/";
                } else
                    inner = "Others/";

                MixedUtils.copyFileToDir(
                    this,
                    uri,
                    new File(path + inner + "Sent/" + params[2])
                );
            } catch (IOException ignored) {
            }
        }

        // multiple files selected from other application
        if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null && intent.getClipData() != null) {
            int clipSize = intent.getClipData().getItemCount();

            String backupPath = getFilesDir().toString() + "/backup";
            final File backupDBFolder = new File(backupPath);
            backupDBFolder.mkdir();

            String[] filePaths = new String[clipSize];
            File _f;
            long filesTotalLength = 0;
            for (int i = 0; i < clipSize; i++) {
                uri = intent.getClipData().getItemAt(i).getUri();
                _f = new File(
                    uri.toString().startsWith("file:///") ?
			                uri.toString().replace("file:///", "") :
			                MixedUtils.getRealPathFromUri(getApplicationContext(), uri)
                );
                filesTotalLength += _f.length();
                filePaths[i] = _f.getAbsolutePath();
            }

            new Zipper(
                    new WeakReference<>(getApplicationContext()),
                    filePaths,
                    backupPath + "/" + procName + ".mickinet_arch",
                    filesTotalLength,
                    handleOffline
            ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Copies a raw resource into the alarms directory on the device's shared storage
     */
    private void copyRawFile() {
        // Make sure the shared storage is currently writable
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS);
        path.mkdirs(); // make sure the directory exists
        File outFile = new File(path, "MickiNet default.mp3");
        try (InputStream inputStream = getResources().openRawResource(R.raw.file_receive); FileOutputStream outputStream = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, bytesRead);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
