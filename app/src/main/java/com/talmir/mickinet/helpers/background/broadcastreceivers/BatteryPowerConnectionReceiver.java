package com.talmir.mickinet.helpers.background.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryPowerConnectionReceiver extends BroadcastReceiver {

    private static boolean isCharging;
    private static int chargePlug = -1;
    private static float batteryLevel;

    public BatteryPowerConnectionReceiver() {
        super();
    }

    public static int getChargePlugType() {
        return chargePlug;
    }

    public static boolean isCharging() {
        return isCharging;
    }

    public static float getLevel() {
        return batteryLevel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
        chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (!isCharging) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryLevel = level / (float)scale;
        }
    }
}
