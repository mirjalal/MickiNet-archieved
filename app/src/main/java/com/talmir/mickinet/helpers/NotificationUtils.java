package com.talmir.mickinet.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.talmir.mickinet.R;

public final class NotificationUtils {
//	private static int notificationID;
//	private static String notificationChannel;
//	private static NotificationManager notification_manager;
//	private static NotificationCompat.Builder notificationBuilder;
//
//	public static void init(Context context, String notificationChannel, int notificationID) {
//		NotificationUtils.notificationID = notificationID;
//		NotificationUtils.notificationChannel = notificationChannel;
//
//		notification_manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		createNotificationChannel(context);
//		notificationBuilder = new NotificationCompat.Builder(context, notificationChannel);
//	}
//
//	private static void createNotificationChannel(Context context) {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//			int importance = NotificationManager.IMPORTANCE_HIGH;
//			NotificationChannel n_channel = new NotificationChannel(notificationChannel, context.getString(R.string.notify_channel_name), importance);
//			n_channel.setDescription(context.getString(R.string.notify_channel_desc));
//			notification_manager.createNotificationChannel(n_channel);
//		}
//	}
//
//	public static void showNotification(String key) {
//
//	}
	
	public static void setNotificationDefaults(NotificationCompat.Builder builder, Context context) {
		builder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.file_receive));
		builder.setLights(Color.parseColor("#" + context.getString(R.string.cyan_color)), 700, 500);
	}
	
	public static void setNotificationSound(NotificationCompat.Builder builder, Context context, String key) {
		builder.setSound(
			Uri.parse(
				PreferenceManager
					.getDefaultSharedPreferences(context)
					.getString(
						"notifications_new_file_" + key + "_ringtone",
						"android.resource://" + context.getPackageName() + "/" + R.raw.file_receive
					)
			)
		);
	}
	
	public static void setNotificationVibration(NotificationCompat.Builder builder, SharedPreferences preferences, String key) {
		if (preferences.getBoolean("notifications_new_file_" + key + "_vibrate", true))
			builder.setVibrate(new long[] { 500 });
	}
	
	public static void setNotificationLight(NotificationCompat.Builder builder, SharedPreferences preferences, Context context, String key) {
		builder.setLights(Color.parseColor("#" + preferences.getString("notifications_new_file_" + key + "_led_light", context.getString(R.string.cyan_color))), 700, 500);
	}
}
