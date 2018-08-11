package com.talmir.mickinet.helpers.background.services;

import android.app.Application;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.webkit.MimeTypeMap;

import com.talmir.mickinet.R;
import com.talmir.mickinet.fragments.DeviceDetailFragment;
import com.talmir.mickinet.helpers.background.CrashReport;
import com.talmir.mickinet.helpers.background.FileReceiverAsyncTask;
import com.talmir.mickinet.helpers.room.sent.SentFilesEntity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

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

//    private SentFilesViewModel sfvm;
    private SentFilesEntity sfe;

    public FileTransferService() {
        super("FileTransferService");
//        sfvm = HomeActivity.getSentFilesViewModel();
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
        Application context = getApplication();

        if (Objects.requireNonNull(intent.getAction()).equals(ACTION_SEND_FILE)) {
            String fileUri = Objects.requireNonNull(intent.getExtras()).getString(EXTRAS_FILE_PATH);
            String fileName = intent.getExtras().getString(EXTRAS_FILE_NAME);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            sfe = new SentFilesEntity();
            sfe.s_f_name = fileName;

            // get mimetype to determine that in which folder'll be used
            assert fileName != null;
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf('.') + 1));
            assert mimeType != null;
            if (mimeType.startsWith("image"))
                sfe.s_f_type = "1";
            else if (mimeType.startsWith("video"))
                sfe.s_f_type = "2";
//            else if (mimeType.startsWith("music") || mimeType.startsWith("audio"))
//                sfe.s_f_type = "4"; // for now, music types are accepted as others
            else if (mimeType.equals("application/vnd.android.package-archive"))
                sfe.s_f_type = "3";
            else
                sfe.s_f_type = "4";

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
                    CrashReport.report(getApplicationContext(), FileTransferService.class.getName(), e);
                }
                // put all data to stream
                copyFile(inputStream, outputStream, context);
            } catch (Exception e) {
                CrashReport.report(getApplicationContext(), FileReceiverAsyncTask.class.getName(), e);
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
    @NonNull
    @Contract(pure = true)
    private static byte[] getByteArrayFromInt(int value) {
        return new byte[]{ (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
    }

    /**
     * @param inputStream input stream
     * @param out outputstream
     * @param app context
     */
    private void copyFile(InputStream inputStream, OutputStream out, @NotNull Application app) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss", new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry()/*, Locale.getDefault().getDisplayVariant()*/));
        sfe.s_f_time = sdf.format(new Date());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(app, null);
        NotificationManager mNotifyManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        // generate unique s_f_id every time to show new notification each time
        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        // Q) Why 8192 ?
        // A) http://stackoverflow.com/a/19561265/4057688
        byte buffer[] = new byte[8192];
        int len;
        try {
            // Issues the notification
            mBuilder.setTicker(app.getString(R.string.file_send))
                    .setContentTitle(app.getString(R.string.sending_file))
                    .setContentText(app.getString(R.string.sending))
                    .setOngoing(true)
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setProgress(0, 0, true);

            assert mNotifyManager != null;
            mNotifyManager.notify(id, mBuilder.build());
            while ((len = inputStream.read(buffer)) != -1)
                out.write(buffer, 0, len);

            out.close();
            out.flush();
            inputStream.close();

            // When the loop is finished, updates the notification
            mBuilder.setTicker(app.getString(R.string.successful))
//                    .setContentTitle(app.getString(R.string.file_sent))
                    .setContentText(app.getString(R.string.file_sent))
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setOngoing(false)
                    .setProgress(0, 0, false);

            SharedPreferences notificationSettings = PreferenceManager.getDefaultSharedPreferences(app);
            if (notificationSettings.getBoolean("notifications_new_file_send", false)) {
                mBuilder.setSound(
                        Uri.parse(
                                PreferenceManager.getDefaultSharedPreferences(app).getString(
                                        "notifications_new_file_send_ringtone",
                                        "android.resource://" + app.getPackageName() + "/" + R.raw.file_receive)
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
                    Uri.parse("android.resource://" + app.getPackageName() + "/" + R.raw.file_receive)
                );

                mBuilder.setLights(
                    Color.parseColor("#" + getString(R.string.cyan_color)), 700, 500
                );
            }
            mNotifyManager.notify(id, mBuilder.build());

            sfe.s_f_operation_status = "1";
            DeviceDetailFragment.getSentFilesViewModel().insert(sfe);
        } catch (Exception e) {
            CrashReport.report(getApplicationContext(), FileTransferService.class.getName(), e);

            mBuilder.setContentText(app.getString(R.string.file_sent_fail))
                    .setTicker(app.getString(R.string.file_is_not_sent))
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

            assert mNotifyManager != null;
            mNotifyManager.notify(id, mBuilder.build());

            sfe.s_f_operation_status = "0";
            DeviceDetailFragment.getSentFilesViewModel().insert(sfe);
        }
    }
}
