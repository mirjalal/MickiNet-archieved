package com.talmir.mickinet.helpers.background.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.background.FileReceiverAsyncTask;
import com.talmir.mickinet.helpers.background.ReportCrash;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    public static final String ACTION_SEND_FILE = "com.talmir.mickinet.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_uri";
    public static final String EXTRAS_FILE_NAME = "file_name_and_extension";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    private static final int SOCKET_TIMEOUT = 5000;

    public FileTransferService() {
        super("FileTransferService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FileTransferService(String name) {
        super(name);
    }

    /**
     *
     * @param intent which comes from {@link com.talmir.mickinet.fragments.DeviceDetailFragment#onActivityResult(int, int, Intent)}
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String fileName = intent.getExtras().getString(EXTRAS_FILE_NAME);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            Socket socket = new Socket();
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                // get output stream to write data
                OutputStream outputStream = socket.getOutputStream();
                // byte[] of file name
                final byte[] full_file_name = fileName.getBytes(Charset.forName("UTF-8"));
                // get file name length
                final int count = full_file_name.length;

                // write file name length as byte[] to outputStream
                outputStream.write(getByteArrayFromInt(count));
                // write file name as byte[] to outputStream
                outputStream.write(full_file_name, 0, count);

                ContentResolver cr = context.getContentResolver();
                InputStream inputStream = null;
                try {
                    inputStream = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    ReportCrash.report(getApplicationContext(), FileTransferService.class.getName(), e);
                }
                // put all data to stream
                copyFile(inputStream, outputStream, context);
            } catch (Exception e) {
                ReportCrash.report(getApplicationContext(), FileReceiverAsyncTask.class.getName(), e);
            } finally {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Give up
                    }
                }
            }
        }
    }

    /**
     * Converts int to byte[]
     *
     * @param value file name length
     * @return file name length as byte[]
     */
    private static byte[] getByteArrayFromInt(int value) {
        return new byte[]{ (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
    }

    /**
     * @param inputStream input stream
     * @param out outputstream
     * @param context context
     */
    private void copyFile(InputStream inputStream, OutputStream out, Context context) {
        // http://stackoverflow.com/a/19561265/4057688
        byte buffer[] = new byte[8192];
        int len;
        try {
            // generate unique id every time to show new notification each time
            int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            // Issues the notification
            mBuilder.setTicker(context.getString(R.string.file_send))
                    .setContentTitle(context.getString(R.string.sending_file))
                    .setContentText(context.getString(R.string.sending))
                    .setOngoing(true)
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setProgress(0, 0, true);

            mNotifyManager.notify(id, mBuilder.build());
            while ((len = inputStream.read(buffer)) != -1)
                out.write(buffer, 0, len);

            out.close();
            out.flush();
            inputStream.close();

            // When the loop is finished, updates the notification
            mBuilder.setTicker(context.getString(R.string.file_sent))
                    .setContentTitle(context.getString(R.string.file_sent))
                    .setContentText(context.getString(R.string.successful))
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setOngoing(false)
                    .setProgress(0, 0, false);

            SharedPreferences notificationSettings = PreferenceManager.getDefaultSharedPreferences(context);
            if (notificationSettings.getBoolean("notifications_new_file_send", false)) {
                mBuilder.setSound(
                        Uri.parse(
                                PreferenceManager.getDefaultSharedPreferences(context).getString(
                                        "notifications_new_file_send_ringtone",
                                        "android.resource://" + context.getPackageName() + "/" + R.raw.file_receive)
                        )
                );

                if (notificationSettings.getBoolean("notifications_new_file_send_vibrate", true))
                    mBuilder.setVibrate(new long[]{500});

                mBuilder.setLights(
                        Color.parseColor("#" + notificationSettings.getString("notifications_new_file_send_led_light", getString(R.string.cyan_color))),
                        700,
                        500
                );
            } else {
                mBuilder.setSound(
                    Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.file_receive)
                );

                mBuilder.setLights(
                    Color.parseColor("#" + getString(R.string.cyan_color)), 700, 500
                );
            }
            mNotifyManager.notify(id, mBuilder.build());
        } catch (Exception e) {
            ReportCrash.report(getApplicationContext(), FileTransferService.class.getName(), e);
        }
    }
}
