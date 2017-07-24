package com.talmir.mickinet.helpers.background;

import android.net.wifi.p2p.WifiP2pConfig;

/**
 * An interface-callback for the activity to listen to fragment interaction events.
 */
public interface IDeviceActionListener {

//    void showDetails(WifiP2pDevice device);

    void cancelDisconnect();

    void connect(WifiP2pConfig config);

    void disconnect();
}
