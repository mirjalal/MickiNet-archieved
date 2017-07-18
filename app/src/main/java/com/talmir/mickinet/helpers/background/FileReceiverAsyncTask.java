package com.talmir.mickinet.helpers.background;

import com.google.firebase.crash.FirebaseCrash;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.talmir.mickinet.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * A custom class that receives stream and saves it
 * as file in device storage.
 */
public class FileReceiverAsyncTask extends AsyncTask<Void, Void, String> {

    private static String clientIpAddress = "";
    private Context mContext;
    private View mView;

    private int id;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public FileReceiverAsyncTask(Context context, View view) {
        mContext = context;
        mView = view;
    }

    public static void setClientIpAddress(@NonNull String ipAddress) { clientIpAddress = ipAddress; }

    public static String getClientIpAddress() {
        return clientIpAddress;
    }

    /**
     * Saves received input stream as a file in
     * device's internal storage.
     *
     * First 4 bytes of the stream represents length
     * of file name (int). Next bytes contains file name.
     * After file name in input stream rest of the bytes
     * represents actual data(file).
     *
     * @param inputStream stream to work on it
     * @return absolute path of saved file
     */
    @Nullable
    private String saveFile(InputStream inputStream) {
        // recommended max buffer size is 8192.
        // read about more: http://stackoverflow.com/a/19561265/4057688
        byte buf[] = new byte[8192];

        int len;
        try {
            byte[] buffer_fileNameLength = new byte[4];
            // get the length of file name as byte[]
            inputStream.read(buffer_fileNameLength, 0, 4);
            // convert byte[] to int to get how long is file name
            int fileNameLength = getIntFromByteArray(buffer_fileNameLength);

            byte[] buffer_fileName = new byte[fileNameLength];
            // read file name as byte[] from stream
            inputStream.read(buffer_fileName, 0, fileNameLength);
            // get file name as string from byte[]
            final String fileName = new String(buffer_fileName, Charset.forName("UTF-8"));

            // create a file in internal storage to dump data to it
            final File receivedFile = new File(Environment.getExternalStorageDirectory() + "/MickiNet/" + fileName);
            File dirs = new File(receivedFile.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            receivedFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(receivedFile);

            // read stream and write it to file
            while ((len = inputStream.read(buf)) != -1)
                outputStream.write(buf, 0, len);

            outputStream.close();
            outputStream.flush();
            inputStream.close();

            return receivedFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helps to convert byte[] to int
     *
     * @param bytes to be converted to int
     * @return int value of bytes
     */
    private static int getIntFromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(4126);
            Socket client = serverSocket.accept();

            final String temp_str = client.getRemoteSocketAddress().toString();
            clientIpAddress = temp_str.substring(1, temp_str.indexOf(':'));

            // generate unique id to show a new notification each time a file received
            id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(mContext);

            // Issues the notification
            mBuilder.setTicker(mContext.getString(R.string.receiving_file))
                    .setContentTitle(mContext.getString(R.string.file_receive))
                    .setContentText(mContext.getString(R.string.receiving_file))
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true)
//                    .setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.file_receive))
                    .setProgress(0, 0, true);

            mNotifyManager.notify(id, mBuilder.build());

//            final String temp_str = client.getRemoteSocketAddress().toString();
//            clientIpAddress = temp_str.substring(1, temp_str.indexOf(':'));

            InputStream inputStream = client.getInputStream();
            final String result = saveFile(inputStream);

            client.close();
            serverSocket.close();

            return result;
        } catch (Exception e) {
            FirebaseCrash.log(FileReceiverAsyncTask.class.getName());
            FirebaseCrash.report(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // intent to open received file from notification click
            final Intent openReceivedFile = new Intent(Intent.ACTION_VIEW);
            openReceivedFile.setDataAndType(
                    Uri.parse("file://" + result),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(result.substring(result.lastIndexOf('.') + 1))
            );

            PendingIntent notificationPendingIntent = PendingIntent.getActivity(mContext, 0, openReceivedFile, PendingIntent.FLAG_ONE_SHOT);

            mBuilder.setTicker(mContext.getString(R.string.file_received))
                    .setContentTitle(mContext.getString(R.string.file_received))
                    .setContentText(result.substring(result.lastIndexOf('/') + 1))
                    .setSmallIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.drawable.ic_done : android.R.drawable.stat_sys_download_done)
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                    .setContentIntent(notificationPendingIntent);

            SharedPreferences notificationSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
            if (notificationSettings.getBoolean("notifications_new_file_receive", false)) {
                mBuilder.setSound(
                        Uri.parse(
                                PreferenceManager.getDefaultSharedPreferences(mContext).getString(
                                        "notifications_new_file_receive_ringtone",
                                        "android.resource://" + mContext.getPackageName() + "/" + R.raw.file_receive)
                        )
                );

                if (notificationSettings.getBoolean("notifications_new_file_receive_vibrate", true))
                    mBuilder.setVibrate(new long[]{500});

                mBuilder.setLights(
                        Color.parseColor("#" + notificationSettings.getString("notifications_new_file_receive_led_light", mContext.getString(R.string.cyan_color))),
                        700,
                        500
                );
            } else {
                mBuilder.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.file_receive));

                mBuilder.setLights(
                    Color.parseColor("#" + mContext.getString(R.string.cyan_color)), 700, 500
                );
            }

            mNotifyManager.notify(id, mBuilder.build());

            Snackbar.make(mView, mContext.getString(R.string.click_to_open), Snackbar.LENGTH_LONG)
                    .setAction(mContext.getString(R.string.open_file), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mContext.startActivity(openReceivedFile);
                        }
                    })
                    .show();
        } else {
            mBuilder.setContentText(mContext.getString(R.string.file_receive_fail))
                    .setTicker(mContext.getString(R.string.file_is_not_received))
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());
        }
    }
}
