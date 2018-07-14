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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.background.IDeviceActionListener;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_device_list, null);
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
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
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
                TextView top = (TextView) convertView.findViewById(R.id.device_name);
                TextView bottom = (TextView) convertView.findViewById(R.id.device_details);
                if (top != null)
                    top.setText(device.deviceName);
                if (bottom != null)
                    bottom.setText(getDeviceStatus(device.status));
            }
            return convertView;
        }
    }
}
