package com.talmir.mickinet.helpers.background.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.talmir.mickinet.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * checkout: https://stackoverflow.com/a/22498307/4057688
 */
public class CountDownService extends Service {

    private static final String COUNTDOWN_SERVICE = "com.talmir.mickinet.countdown_service";
    private static CountDownTimer countDownTimer = null;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    private static boolean isStarted;

    public static boolean isRunning() {
        return isStarted;
    }

    public CountDownService() {  }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mNotifyManager.cancelAll();
        countDownTimer.cancel();
        isStarted = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            float chargeLevel = intent.getFloatExtra("battery_charge_level", 0.00f);
            int chargeType = intent.getIntExtra("battery_charge_type", -1);
            if (chargeType > -1) {
                if (chargeType == BatteryManager.BATTERY_PLUGGED_USB) {
                    startCountDown(180000); // 180 seconds
                } else if (chargeType == BatteryManager.BATTERY_PLUGGED_AC) {
                    startCountDown(420000); // 7 minutes
                } else if (chargeType == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
                    startCountDown(300000); // 5 minutes
                } else {
                    // are there any other 'plug types' rather than above?
                    // if so, add correct conditions
                }
            } else {
                if (chargeLevel <= 0.20) {
                    startCountDown(90000); // 90 seconds
                } else if (chargeLevel < 0.33 && chargeLevel > 0.20) {
                    startCountDown(150000); // 150 seconds
                } else if (chargeLevel < 0.80 && chargeLevel > 0.33) {
                    startCountDown(300000); // 5 minutes
                } else {
                    startCountDown(600000); // 10 minutes
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startCountDown(final long millisInFuture) {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext());

        mBuilder.setTicker("Timer started")
                .setContentTitle("Timer running")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_countdown)
                .setNumber(0);

        mNotifyManager.notify(1, mBuilder.build());

        countDownTimer = new CountDownTimer(millisInFuture, (long) 1000) {
            int i = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                i++;
                mBuilder.setProgress((int)(millisInFuture / 1000), (int)(millisInFuture / 1000 - millisUntilFinished / 1000), false)
                        .setContentText(
                                "Time left: " +
                                String.format(
                                        Locale.ROOT,
                                        "%02d:%02d sec",
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                                                )
                                )
                        );
                mNotifyManager.notify(1, mBuilder.build());
            }

            @Override
            public void onFinish() {
                cancel();
                mBuilder.setProgress(0, 0, false).setOngoing(false).setContentTitle("Time reached").setContentText("Timer finished.");
                mNotifyManager.notify(1, mBuilder.build());
                isStarted = false;

                // TODO: get battery level/charge status and then start it again
//                cancel();
//                start();
            }
        };
        countDownTimer.start();
        isStarted = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
