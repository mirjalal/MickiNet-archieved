package com.talmir.mickinet.helpers.background.tasks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.talmir.mickinet.BuildConfig;
import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;
import com.talmir.mickinet.helpers.MixedUtils;
import com.talmir.mickinet.helpers.background.CrashReport;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesEntity;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesViewModel;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * A custom class that receives stream and saves it
 * as file in device storage.
 */
public class FileReceiver extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> contextRef;
    private WeakReference<View> viewRef;

    private int id;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private static boolean isTaskRunning;

    /**
     * see {@link com.talmir.mickinet.helpers.background.broadcastreceivers.WiFiDirectBroadcastReceiver}
     * for usages
     */
    public static boolean getIsTaskRunning() { return isTaskRunning; }

    private boolean isArchive;

    private ReceivedFilesEntity rfe;

    public FileReceiver(Context context, View view) {
        contextRef = new WeakReference<>(context);
        viewRef = new WeakReference<>(view);
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
        isTaskRunning = true;

        // recommended max buffer size is 8192.
        // read more: http://stackoverflow.com/a/19561265/4057688
        byte buf[] = new byte[2048];

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
            MixedUtils.createNestedFolders();

            // get mimetype to determine that in which folder'll be used
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf('.') + 1));

            rfe = new ReceivedFilesEntity();
            rfe.r_f_name = fileName;

            rfe.r_f_time = new Date();

            // check for mimetypes
            final File receivedFile;
            if (!fileName.substring(fileName.lastIndexOf('.') + 1).equals("mickinet_arch")) {
                if (mimeType != null) {
                    if (mimeType.startsWith("image")) {
                        receivedFile = new File("/storage/emulated/0/MickiNet/Photos/Received/" + fileName);
                        rfe.r_f_type = "1";
                    } else if (mimeType.startsWith("video")) {
                        receivedFile = new File("/storage/emulated/0/MickiNet/Videos/Received/" + fileName);
                        rfe.r_f_type = "2";
                    } else if (mimeType.startsWith("music") || mimeType.startsWith("audio")) {
                        receivedFile = new File("/storage/emulated/0/MickiNet/Media/Received/" + fileName);
                        rfe.r_f_type = "3"; // for now, music types are accepted as others
                    } else if (mimeType.equals("application/vnd.android.package-archive")) {
                        receivedFile = new File("/storage/emulated/0/MickiNet/APKs/Received/" + fileName);
                        rfe.r_f_type = "4";
                    } else {
                        receivedFile = new File("/storage/emulated/0/MickiNet/Others/Received/" + fileName);
                        rfe.r_f_type = "5";
                    }
                } else {
                    receivedFile = new File("/storage/emulated/0/MickiNet/Others/Received/" + fileName);
                    rfe.r_f_type = "5";
                }
            } else {
                isArchive = true;
                receivedFile = new File("/storage/emulated/0/MickiNet/.temp/tempBackupZip.mickinet_arch");
            }

//            File dirs = new File(receivedFile.getParent());
//            if (!dirs.exists())
//                dirs.mkdirs();

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
            mNotifyManager = (NotificationManager) contextRef.get().getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(contextRef.get(), null);

            // Issues the notification
            mBuilder.setTicker(contextRef.get().getString(R.string.receiving_file))
                    .setContentTitle(contextRef.get().getString(R.string.file_receive))
                    .setContentText(contextRef.get().getString(R.string.receiving_file))
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
            CrashReport.report(contextRef.get(), e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            final Intent openReceivedFile = new Intent(Intent.ACTION_VIEW);

            // if received file isn't an archive file, then set pending intent for click-to-open
            if (!isArchive) {
                // intent to open received file from notification click
                openReceivedFile.setDataAndType(
                    FileProvider.getUriForFile(
                        contextRef.get(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        new File(result)
                    ),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(result.substring(result.lastIndexOf('.') + 1))
                );
                openReceivedFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                PendingIntent notificationPendingIntent = PendingIntent.getActivity(contextRef.get(), 0, openReceivedFile, PendingIntent.FLAG_ONE_SHOT);
                mBuilder.setTicker(contextRef.get().getString(R.string.file_received))
                        .setContentTitle(contextRef.get().getString(R.string.file_received))
                        .setContentText(result.substring(result.lastIndexOf('/') + 1))
                        .setSmallIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.drawable.ic_receive_done : android.R.drawable.stat_sys_download_done)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setContentIntent(notificationPendingIntent);
            }
            else {
                mBuilder.setTicker("Files received")
                        .setContentTitle("All files received")
                        .setContentText("")
                        .setSmallIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.drawable.ic_receive_done : android.R.drawable.stat_sys_download_done)
                        .setProgress(0, 0, false)
                        .setOngoing(false);
            }

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(contextRef.get());
            // get notification settings & apply them to notification
            if (settings.getBoolean("notifications_new_file_receive", false)) {
                mBuilder.setSound(
                    Uri.parse(
                        PreferenceManager.getDefaultSharedPreferences(contextRef.get()).getString(
                            "notifications_new_file_receive_ringtone",
                            "android.resource://" + contextRef.get().getPackageName() + "/" + R.raw.file_receive
                        )
                    )
                );

                if (settings.getBoolean("notifications_new_file_receive_vibrate", true))
                    mBuilder.setVibrate(new long[]{500});

                mBuilder.setLights(
                    Color.parseColor("#" + settings.getString("notifications_new_file_receive_led_light", contextRef.get().getString(R.string.cyan_color))),
                    700,
                    500
                );
            } else {
                mBuilder.setSound(Uri.parse("android.resource://" + contextRef.get().getPackageName() + "/" + R.raw.file_receive));
                mBuilder.setLights(
                    Color.parseColor("#" + contextRef.get().getString(R.string.cyan_color)), 700, 500
                );
            }

            mNotifyManager.notify(id, mBuilder.build());

            if (!isArchive) {
                // if file is not an archive file & auto-open property is checked open received file
                // automatically, show snackbar otherwise
                if (settings.getBoolean("pref_auto_open_received_file", false)) {
                    contextRef.get().startActivity(openReceivedFile);
                } else {
                    Snackbar.make(viewRef.get(), contextRef.get().getString(R.string.click_to_open), Snackbar.LENGTH_LONG)
                            .setActionTextColor(contextRef.get().getResources().getColor(R.color.white))
                            .setAction(contextRef.get().getString(R.string.open_file), v -> contextRef.get().startActivity(openReceivedFile))
                            .show();
                }

                rfe.r_f_operation_status = "1";
            } else {
                // received file is archive file; unzip it to the /.temp/ folder
                new Unzipper(contextRef, "/storage/emulated/0/MickiNet/.temp/tempBackupZip.mickinet_arch").executeOnExecutor(THREAD_POOL_EXECUTOR);
            }
        } else {
            mBuilder.setContentText(contextRef.get().getString(R.string.file_receive_fail))
                    .setTicker(contextRef.get().getString(R.string.file_is_not_received))
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());

            if (!isArchive) rfe.r_f_operation_status = "0";
        }
        if (!isArchive) {
            ReceivedFilesViewModel rfvm = HomeActivity.getReceivedFilesViewModel();//ViewModelProviders.of((FragmentActivity) contextRef.get()).get(ReceivedFilesViewModel.class);
            rfvm.insert(rfe);
        }

        isTaskRunning = false;
    }
}
