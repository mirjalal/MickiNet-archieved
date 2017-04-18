package com.talmir.transferfileoverwifidirect.fragments;

import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.talmir.transferfileoverwifidirect.R;
import com.talmir.transferfileoverwifidirect.activities.HomeActivity;
import com.talmir.transferfileoverwifidirect.helpers.IDeviceActionListener;

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

    @SuppressLint("LongLogTag")
    private static String getDeviceStatus(int deviceStatus) {
        Log.d(HomeActivity.TAG, "Peer status: " + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
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
//        CardView cardView = (CardView) mContentView.findViewById(R.id.this_device);
//        cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
//                alertDialogBuilder.setTitle("Device information");
//                alertDialogBuilder
//                        .setMessage(
//                                Html.fromHtml(String.format(
//                                        "Name: %1$s%nStatus: %2$s%nMAC Address: %3$s%nIs group owner: %4$s%nWiFi Direct IP Address: %5$s",
//                                        device.deviceName,
//                                        device.status,
//                                        device.deviceAddress,
//                                        device.isGroupOwner() ? "Yes" : "No",
//                                        getDottedDecimalIP(getLocalIPAddress())))
//                        )
//                        .setCancelable(false)
//                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                AlertDialog alertDialog = alertDialogBuilder.create();
//                alertDialog.show();
//            }
//        });

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
//        ((IDeviceActionListener) getActivity()).showDetails(device);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Connect to device?");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            alertDialogBuilder
                    .setMessage(
                            Html.fromHtml(
                                    String.format(
                                    "<b>Name</b>: %1$s<br><b>Status</b>: %2$s<br><b>MAC Address</b>: %3$s<br><b>Is group owner</b>: %4$s",
                                    device.deviceName,
                                    getDeviceStatus(device.status),
                                    device.deviceAddress,
                                    device.isGroupOwner() ? "Yes" : "No"
                                ), Html.FROM_HTML_MODE_LEGACY, null, null
                            )
                    )
                    .setCancelable(false)
                    .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;
                            config.wps.setup = WpsInfo.PBC;
                            // connect to device
                            ((IDeviceActionListener) getActivity()).connect(config);
                        }
                    })
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        }
        else {
            alertDialogBuilder
                    .setMessage(
                            Html.fromHtml(
                                    String.format(
                                            "<b>Name</b>: %1$s<br><b>Status</b>: %2$s<br><b>MAC Address</b>: %3$s<br><b>Is group owner</b>: %4$s",
                                            device.deviceName,
                                            getDeviceStatus(device.status),
                                            device.deviceAddress,
                                            device.isGroupOwner() ? "Yes" : "No"
                                    )
                            )
                    )
                    .setCancelable(false)
                    .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;
                            config.wps.setup = WpsInfo.PBC;
                            // connect to device
                            ((IDeviceActionListener) getActivity()).connect(config);
                        }
                    })
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        }
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
        if (peers.size() == 0) {
            Log.d(HomeActivity.TAG, "No devices found");
            return;
        }
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
                "Press back to cancel", "Finding peers...",
                true,
                true,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(getActivity(), "Discovery cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

//    @Nullable
//    private byte[] getLocalIPAddress() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress()) {
//                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
//                            return inetAddress.getAddress();
//                        }
//                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
//                    }
//                }
//            }
//        } catch (SocketException | NullPointerException ex) {
//            Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
//        }
//        return null;
//    }
//
//    @Contract(pure = true)
//    private String getDottedDecimalIP(byte[] ipAddr) {
//        //convert to dotted decimal notation:
//        String ipAddrStr = "";
//        for (int i = 0; i < ipAddr.length; i++) {
//            if (i > 0) {
//                ipAddrStr += ".";
//            }
//            ipAddrStr += ipAddr[i] & 0xFF;
//        }
//        return ipAddrStr;
//    }

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
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null)
                    top.setText(device.deviceName);
                if (bottom != null)
                    bottom.setText(getDeviceStatus(device.status));
            }

            return v;
        }
    }
}
