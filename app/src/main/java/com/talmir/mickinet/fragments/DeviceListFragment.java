package com.talmir.mickinet.fragments;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
    private WifiP2pDevice device;

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
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
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
        final WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.connect_to_device);
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml(
                                String.format(
                                        "<b>" + getString(R.string.name) + "</b>%1$s<br>" +
                                        "<b>" + getString(R.string.status) + "</b>%2$s<br>" +
                                        "<b>" + getString(R.string.mac_address) + "</b>%3$s<br>" +
                                        "<b>" + getString(R.string.is_group_owner) + "</b>%4$s",
                                        device.deviceName,
                                        getDeviceStatus(device.status),
                                        device.deviceAddress,
                                        device.isGroupOwner() ? getString(R.string.yes) : getString(R.string.no)
                                )
                        )
                )
                .setCancelable(false)
                .setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;
                        config.wps.setup = WpsInfo.PBC;
                        // connect to device
                        ((IDeviceActionListener) getActivity()).connect(config);
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Update UI for this device.
     *
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
        view.setText(device.deviceName);
        view = (TextView) mContentView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
        view = (TextView) mContentView.findViewById(R.id.my_address);
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
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(
                getActivity(),
                getString(R.string.cancel_tip),
                getString(R.string.finding_devices),
                true,
                true,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(getActivity(), R.string.discovery_cancelled, Toast.LENGTH_LONG).show();
                    }
                }
        );
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
