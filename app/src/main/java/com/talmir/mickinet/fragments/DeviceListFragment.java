package com.talmir.mickinet.fragments;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.background.IDeviceActionListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.os.Looper.getMainLooper;

/**
 * A ListFragment that displays available peers on discovery and requests the parent activity to
 * handle user interaction events
 */
public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener {
    ProgressDialog progressDialog = null;
    View mContentView = null;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pDevice device; // this device
    public static WifiP2pDevice connectedDevice = null; // connected device (used in WiFiDirectBroadcastReceiver class)

    @NonNull
    private String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return getString(R.string.available);
            case WifiP2pDevice.INVITED:
                return getString(R.string.invited);
            case WifiP2pDevice.CONNECTED:
                return getString(R.string.connected);
            case WifiP2pDevice.FAILED:
                return getString(R.string.failed);
            case WifiP2pDevice.UNAVAILABLE:
                return getString(R.string.unavailable);
            default:
                return getString(R.string.unknown);
        }
    }
//
//    static {
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_device_list, null);

        CardView c = mContentView.findViewById(R.id.this_device);
        c.setOnClickListener(v -> {
            changeDeviceName();
        });

        return mContentView;
    }

    /**
     * @return this device
     */
    public WifiP2pDevice getDevice() {
        return device;
    }

    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getActivity().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.connect_to_device).setCancelable(false);
        connectedDevice = (WifiP2pDevice) getListAdapter().getItem(position);

        if (batteryPct <= 0.20) {
            alertDialog
                    .setMessage("Battery about die. Do you really want to connect?\n\n" +
                        Html.fromHtml(
                                String.format(
                                        "<b>" + getString(R.string.name) + "</b>%1$s<br>" +
                                        "<b>" + getString(R.string.status) + "</b>%2$s<br>" +
                                        "<b>" + getString(R.string.mac_address) + "</b>%3$s<br>" +
                                        "<b>" + getString(R.string.is_group_owner) + "</b>%4$s",
                                        connectedDevice.deviceName,
                                        getDeviceStatus(connectedDevice.status),
                                        connectedDevice.deviceAddress,
                                        connectedDevice.isGroupOwner() ? getString(R.string.yes) : getString(R.string.no)
                                )
                        )
                    );
        } else if (batteryPct < 0.33 && batteryPct > 0.20) {
            alertDialog
                    .setMessage("Wi-Fi Direct drains battery fast. Do you really want to connect?\n\n" +
                            Html.fromHtml(
                                    String.format(
                                            "<b>" + getString(R.string.name) + "</b>%1$s<br>" +
                                            "<b>" + getString(R.string.status) + "</b>%2$s<br>" +
                                            "<b>" + getString(R.string.mac_address) + "</b>%3$s<br>" +
                                            "<b>" + getString(R.string.is_group_owner) + "</b>%4$s",
                                            connectedDevice.deviceName,
                                            getDeviceStatus(connectedDevice.status),
                                            connectedDevice.deviceAddress,
                                            connectedDevice.isGroupOwner() ? getString(R.string.yes) : getString(R.string.no)
                                    )
                            )
                    );
        } else {
            alertDialog
                    .setMessage(
                            Html.fromHtml(
                                    String.format(
                                            "<b>" + getString(R.string.name) + "</b>%1$s<br>" +
                                            "<b>" + getString(R.string.status) + "</b>%2$s<br>" +
                                            "<b>" + getString(R.string.mac_address) + "</b>%3$s<br>" +
                                            "<b>" + getString(R.string.is_group_owner) + "</b>%4$s",
                                            connectedDevice.deviceName,
                                            getDeviceStatus(connectedDevice.status),
                                            connectedDevice.deviceAddress,
                                            connectedDevice.isGroupOwner() ? getString(R.string.yes) : getString(R.string.no)
                                    )
                            )
                    );
        }
        alertDialog
                .setPositiveButton(R.string.connect, (dialogInterface, i) -> {
                    final WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = connectedDevice.deviceAddress;
                    SharedPreferences wpsSetting = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    config.wps.setup =
                            wpsSetting.getBoolean("pref_show_advanced_confs", false) ?
                                    wpsSetting.getInt("pref_advanced_wps_modes", 0x00000000) :
                                    WpsInfo.PBC;

                    ((IDeviceActionListener) getActivity()).connect(config);
                })
                .setNegativeButton(R.string.cancel, (dialog, id1) -> dialog.cancel())
                .show();
    }

    /**
     * Update UI for this device.
     *
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = mContentView.findViewById(R.id.my_name);
        view.setText(device.deviceName);
        view = mContentView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
        view = mContentView.findViewById(R.id.my_address);
        view.setText(device.deviceAddress);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        peers.clear();
        peers.addAll(wifiP2pDeviceList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    /**
     *
     */
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getString(R.string.cancel_tip));
        progressDialog.setMessage(getString(R.string.finding_devices));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setProgressNumberFormat(null);
        progressDialog.setProgressPercentFormat(null);
        progressDialog.setOnCancelListener(dialog -> Toast.makeText(getActivity(), R.string.discovery_cancelled, Toast.LENGTH_LONG).show());
        progressDialog.show();
    }

    /**
     * Array adapters for ListFragment that maintains WifiP2pDevice list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
        private List<WifiP2pDevice> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        WiFiPeerListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = convertView.findViewById(R.id.device_name);
                TextView bottom = convertView.findViewById(R.id.device_details);
                if (top != null)
                    top.setText(device.deviceName);
                if (bottom != null)
                    bottom.setText(getDeviceStatus(device.status));
            }
            return convertView;
        }
    }

    /**
     * Changes device name with entered text.
     *
     * @return true if device name successfully changed, false otherwise.
     */
    private void changeDeviceName() {

        final android.app.AlertDialog alert = new android.app.AlertDialog.Builder(getActivity()).create();
        alert.setTitle("Change device name");

        final View view = getActivity().getLayoutInflater().inflate(R.layout.change_device_name_alertdialog_layout, null);

        final TextInputLayout mNewDeviceNameParent = view.findViewById(R.id.newDeviceNameParent);
        final EditText mNewDeviceName = view.findViewById(R.id.newDeviceName);

        final Button mSubmit = view.findViewById(R.id.submit);
        final Button mCancel = view.findViewById(R.id.cancel);

        mNewDeviceName.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        mNewDeviceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNewDeviceNameParent.setError(s.toString().length() < 3 ? "Enter 3 characters at least" : null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSubmit.setOnClickListener(v -> {
            if (mNewDeviceName.getText().toString().trim().length() > 0 && mNewDeviceNameParent.getError() == null) {

                final WifiP2pManager[] mManager = new WifiP2pManager[1];
                WifiP2pManager.Channel channel;
                try {
                    mManager[0] = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
                    channel = mManager[0].initialize(
                            getActivity(),
                            getMainLooper(),
                            () -> mManager[0] = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE)
                    );
                    Class[] paramTypes = new Class[3];
                    paramTypes[0] = WifiP2pManager.Channel.class;
                    paramTypes[1] = String.class;
                    paramTypes[2] = WifiP2pManager.ActionListener.class;
                    Method setDeviceName = mManager[0].getClass().getMethod("setDeviceName", paramTypes);
                    setDeviceName.setAccessible(true);

                    Object arglist[] = new Object[3];
                    arglist[0] = channel;
                    arglist[1] = mNewDeviceName.getText().toString().trim();
                    arglist[2] = new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            alert.dismiss();
                            Toast.makeText(getActivity(), "Device name changed!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(getActivity(), "Device name not changed!", Toast.LENGTH_SHORT).show();
                        }
                    };
                    setDeviceName.invoke(mManager[0], arglist);

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            else
                mNewDeviceNameParent.setError("Enter valid device name.");
        });

        mCancel.setOnClickListener(v -> alert.dismiss());

        alert.setView(view);
        alert.setCancelable(true);
        alert.show();
    }
}
