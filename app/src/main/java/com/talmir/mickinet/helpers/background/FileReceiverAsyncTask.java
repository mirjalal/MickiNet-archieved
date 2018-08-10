package com.talmir.mickinet.helpers.background;

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
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesEntity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

//    private ReceivedFilesViewModel mReceivedFilesViewModel;
    private ReceivedFilesEntity rfe;

    public FileReceiverAsyncTask(Context context, View view/*, ReceivedFilesViewModel mReceivedFilesViewModel*/) {
        mContext = context;
        mView = view;
//        this.mReceivedFilesViewModel = mReceivedFilesViewModel;
    }

    @Contract(pure = true)
    public static String getClientIpAddress() {
        return clientIpAddress;
    }

    public static void setClientIpAddress(@NonNull String ipAddress) {
        clientIpAddress = ipAddress;
    }

    /**
     * Helps to convert byte[] to int
     *
     * @param bytes to be converted to int
     * @return int value of bytes
     */
    @Contract(pure = true)
    private static int getIntFromByteArray(@NotNull byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    /**
     * Saves received input stream as a file to proper folder
     * in device's internal storage.
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
        // read more: http://stackoverflow.com/a/19561265/4057688
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

            // by calling this func make sure that folder structure is OK
            createNestedFolders();

            // get mimetype to determine that in which folder'll be used
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf('.') + 1));

            rfe = new ReceivedFilesEntity();
            rfe.f_name = fileName;

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss", new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry()/*, Locale.getDefault().getDisplayVariant()*/));
            rfe.f_time = sdf.format(/*Calendar.getInstance().getTime()*/new Date());

            // check for mimetypes
            final File receivedFile;
            if (mimeType.startsWith("image")) {
                receivedFile = new File(Environment.getExternalStorageDirectory() + "/MickiNet/Photos/Received/" + fileName);
                rfe.f_type = "1";
            }
            else if (mimeType.startsWith("video")) {
                receivedFile = new File(Environment.getExternalStorageDirectory() + "/MickiNet/Videos/Received/" + fileName);
                rfe.f_type = "2";
            }
            else if (mimeType.startsWith("music") || mimeType.startsWith("audio")) {
                receivedFile = new File(Environment.getExternalStorageDirectory() + "/MickiNet/Musics/Received/" + fileName);
                rfe.f_type = "4"; // for now, music types are accepted as others
            }
            else if (mimeType.equals("application/vnd.android.package-archive")) {
                receivedFile = new File(Environment.getExternalStorageDirectory() + "/MickiNet/APKs/Received/" + fileName);
                rfe.f_type = "3";
            }
            else {
                receivedFile = new File(Environment.getExternalStorageDirectory() + "/MickiNet/Others/Received/" + fileName);
                rfe.f_type = "4";
            }

            File dirs = new File(receivedFile.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            // save data as a file
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

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(4126);
            Socket client = serverSocket.accept();

            // generate unique id to show a new notification each time a file received
            id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(mContext, null);

            // Issues the notification
            mBuilder.setTicker(mContext.getString(R.string.receiving_file))
                    .setContentTitle(mContext.getString(R.string.file_receive))
                    .setContentText(mContext.getString(R.string.receiving_file))
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true)
                    .setProgress(0, 0, true);

            mNotifyManager.notify(id, mBuilder.build());

            InputStream inputStream = client.getInputStream();
            final String result = saveFile(inputStream);

            client.close();
            serverSocket.close();

            return result;
        } catch (Exception e) {
            CrashReport.report(mContext, FileReceiverAsyncTask.class.getName(), e);
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

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

            // get notification settings & apply them to notification
            if (settings.getBoolean("notifications_new_file_receive", false)) {
                mBuilder.setSound(
                        Uri.parse(
                                PreferenceManager.getDefaultSharedPreferences(mContext).getString(
                                        "notifications_new_file_receive_ringtone",
                                        "android.resource://" + mContext.getPackageName() + "/" + R.raw.file_receive)
                        )
                );

                if (settings.getBoolean("notifications_new_file_receive_vibrate", true))
                    mBuilder.setVibrate(new long[]{500});

                mBuilder.setLights(
                        Color.parseColor("#" + settings.getString("notifications_new_file_receive_led_light", mContext.getString(R.string.cyan_color))),
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

            // if preference checked open file, show snackbar otherwise
            if (settings.getBoolean("pref_auto_open_received_file", false)) {
                mContext.startActivity(openReceivedFile);
            } else {
//                Snackbar.make(mView, mContext.getString(R.string.click_to_open), Snackbar.LENGTH_LONG)
//                        .setAction(mContext.getString(R.string.open_file), v -> mContext.startActivity(openReceivedFile))
//                        .show();
            }

            rfe.f_operation_status = "1";
            HomeActivity.getReceivedFilesViewModel().insert(rfe);
        } else {
            mBuilder.setContentText(mContext.getString(R.string.file_receive_fail))
                    .setTicker(mContext.getString(R.string.file_is_not_received))
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());

            rfe.f_operation_status = "0";
            HomeActivity.getReceivedFilesViewModel().insert(rfe);
        }
    }

    private void createNestedFolders() {
        File rootDir = new File(Environment.getExternalStorageDirectory() + "/MickiNet/");
        rootDir.mkdirs();

        File p = new File(rootDir, "/Photos/");
        p.mkdirs();
        new File(p, "/Sent/").mkdirs();
        new File(p, "/Received/").mkdirs();

        File v = new File(rootDir, "/Videos/");
        v.mkdirs();
        new File(v, "/Sent/").mkdirs();
        new File(v, "/Received/").mkdirs();

        File m = new File(rootDir, "/Musics/");
        m.mkdirs();
        new File(m, "/Sent/").mkdirs();
        new File(m, "/Received/").mkdirs();

        File f = new File(rootDir, "/Others/");
        f.mkdirs();
        new File(f, "/Sent/").mkdirs();
        new File(f, "/Received/").mkdirs();

        File a = new File(rootDir, "/APKs/");
        a.mkdirs();
        new File(a, "/Sent/").mkdirs();
        new File(a, "/Received/").mkdirs();
    }
}
