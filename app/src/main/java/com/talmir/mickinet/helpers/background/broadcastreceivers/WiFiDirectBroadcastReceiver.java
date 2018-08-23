/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.talmir.mickinet.helpers.background.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;
import com.talmir.mickinet.fragments.DeviceDetailFragment;
import com.talmir.mickinet.fragments.DeviceListFragment;
import com.talmir.mickinet.helpers.background.services.CountDownService;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private Channel channel;
    private HomeActivity activity;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, HomeActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    /**
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null)
                manager.requestPeers(channel, (PeerListListener) activity.getFragmentManager().findFragmentById(R.id.frag_list));
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null)
                return;

            DeviceListFragment listFragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
            LinearLayout ll = (LinearLayout) listFragment.getView();
            int c = ll.getChildCount();

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);

//                if (!CountDownService.isRunning())
//                    startCountDownIfNecessary(context);

                // disable views
                for (int i = 0; i < c; i++) {
                    ll.getChildAt(i).setEnabled(false);
                    if (i == 1) {
                        CardView cv = (CardView)ll.getChildAt(i);
                        RelativeLayout rl = (RelativeLayout) cv.getChildAt(0);
                        int cc = rl.getChildCount();
                        for (int j = 0; j < cc; j++)
                            rl.getChildAt(j).setEnabled(false);
                    }
                }
            } else {
                // It's a disconnect. enable view elements
                activity.resetData();
                for (int i = 0; i < c; i++) {
                    ll.getChildAt(i).setEnabled(true);
                    if (i == 1) {
                        CardView cv = (CardView)ll.getChildAt(i);
                        RelativeLayout rl = (RelativeLayout) cv.getChildAt(0);
                        int cc = rl.getChildCount();
                        for (int j = 0; j < cc; j++)
                            rl.getChildAt(j).setEnabled(true);
                    }
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
            fragment.updateThisDevice(intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }

    private synchronized void startCountDownIfNecessary(Context context) {
        /*
         * We should do our battery related optimisation things here.
         * Thus, this part has HIGH priority.
         *
         * get charging status and/or battery level from {@link batteryInfoBroadcastReceiver}
         * before starting {@see CountDownService} and creating its object on memory.
         */

        SharedPreferences timerPreference = PreferenceManager.getDefaultSharedPreferences(context);
        if (timerPreference.getBoolean("pref_show_advanced_confs", false) &&
            timerPreference.getBoolean("pref_enable_countdown", false))
        {
            float level = BatteryPowerConnectionReceiver.getLevel();
            int chargePlugType = BatteryPowerConnectionReceiver.getChargePlugType();

            Intent countDownServiceIntent = new Intent(context, CountDownService.class);
            if (BatteryPowerConnectionReceiver.isCharging()) {
                countDownServiceIntent.putExtra("battery_charge_level", level);
                countDownServiceIntent.putExtra("battery_charge_type", chargePlugType);
            } else {
                countDownServiceIntent.putExtra("battery_charge_level", level);
                countDownServiceIntent.putExtra("battery_charge_type", -1);
            }
            context.startService(countDownServiceIntent);
        }
    }
}
