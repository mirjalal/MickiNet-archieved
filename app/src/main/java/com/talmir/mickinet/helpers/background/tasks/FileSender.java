package com.talmir.mickinet.helpers.background.tasks;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;
import com.talmir.mickinet.helpers.MixedUtils;
import com.talmir.mickinet.helpers.background.CrashReport;
import com.talmir.mickinet.helpers.room.sent.SentFilesEntity;
import com.talmir.mickinet.helpers.room.sent.SentFilesViewModel;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author miri
 * @since 8/12/2018
 */
public class FileSender extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<Context> contextRef;

    private static final int id = 871;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private static boolean isTaskRunning;

    /**
     * see {@link com.talmir.mickinet.helpers.background.broadcastreceivers.WiFiDirectBroadcastReceiver}
     * for usages
     */
    public static boolean getIsTaskRunning() { return isTaskRunning; }

    private boolean _isArchFile;
    private ArrayList<String> fileDirList;
    private ArrayList<String> fileNameList;

    private boolean _res = false;
    private SentFilesEntity sfe;

    private String PARAM_GROUP_OWNER_ADDRESS;
    private static final int PARAM_GROUP_OWNER_PORT = 4126;
    private static final int PARAM_SOCKET_TIMEOUT = 5000;
    private String PARAM_FILE_PATH;
    private String PARAM_FILE_NAME;

    public FileSender(Context context, @NotNull String... params) {
        contextRef = new WeakReference<>(context);

        PARAM_GROUP_OWNER_ADDRESS = params[0];
        PARAM_FILE_PATH = params[1];
        PARAM_FILE_NAME = params[2];

        mBuilder = new NotificationCompat.Builder(contextRef.get(), null);
        mNotifyManager = (NotificationManager) contextRef.get().getSystemService(Context.NOTIFICATION_SERVICE);

        sfe = new SentFilesEntity();
    }

    FileSender(Context context, @NotNull String[] fileList, @NotNull String... params) {
        _isArchFile = true;

        contextRef = new WeakReference<>(context);

        fileDirList = new ArrayList<>(fileList.length);
        fileNameList = new ArrayList<>(fileList.length);
        for (String file : fileList) {
            fileDirList.add(file);
            fileNameList.add(file.substring(file.lastIndexOf('/') + 1));
        }
        PARAM_GROUP_OWNER_ADDRESS = params[0];
        PARAM_FILE_NAME = params[2];
        PARAM_FILE_PATH = "file:///data/data/com.talmir.mickinet/files/backup/tempZipBackup.mickinet_arch";
        mBuilder = new NotificationCompat.Builder(contextRef.get(), null);
        mNotifyManager = (NotificationManager) contextRef.get().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        isTaskRunning = true;

        // Issues the notification
        mBuilder.setTicker(contextRef.get().getString(R.string.file_send))
                .setContentTitle(contextRef.get().getString(R.string.sending_file))
                .setContentText(contextRef.get().getString(R.string.sending))
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setProgress(0, 0, true);

        mNotifyManager.notify(id, mBuilder.build());
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (!_isArchFile) {
            sfe.s_f_name = PARAM_FILE_NAME;
            sfe.s_f_time = new Date();

            // get mimetype to determine that in which folder will be used
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(PARAM_FILE_NAME.substring(PARAM_FILE_NAME.lastIndexOf('.') + 1));
            if (mimeType != null) {
                if (mimeType.startsWith("image"))
                    sfe.s_f_type = "1";
                else if (mimeType.startsWith("video"))
                    sfe.s_f_type = "2";
                else if (mimeType.startsWith("music") || mimeType.startsWith("audio"))
                    sfe.s_f_type = "3";
                else if (mimeType.equals("application/vnd.android.package-archive"))
                    sfe.s_f_type = "4";
                else
                    sfe.s_f_type = "5";
            } else
                sfe.s_f_type = "5";
        }

        // put all data to stream
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(PARAM_GROUP_OWNER_ADDRESS, PARAM_GROUP_OWNER_PORT)), PARAM_SOCKET_TIMEOUT);

            // get output stream to write data
            OutputStream outputStream = socket.getOutputStream();
            // byte[] of file name
            final byte[] full_file_name = PARAM_FILE_NAME.getBytes(Charset.forName("UTF-8"));
            // get file name length
            final int count = full_file_name.length;

            // write file name length as byte[] to outputStream
            outputStream.write(getByteArrayFromInt(count));
            // write file name as byte[] to outputStream
            outputStream.write(full_file_name, 0, count);

            ContentResolver cr = contextRef.get().getContentResolver();
            InputStream inputStream = null;
            try {
                inputStream = cr.openInputStream(Uri.parse(PARAM_FILE_PATH));
            } catch (FileNotFoundException e) {
                CrashReport.report(contextRef.get(), e.getMessage());
            }

            sendFile(inputStream, outputStream, contextRef.get());
            _res = true;
        } catch (Exception e) {
            CrashReport.report(contextRef.get(), e.getMessage());
        } finally {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Give up
                }
            }
        }

        return _res;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        SentFilesViewModel sfvm = HomeActivity.getSentFilesViewModel();//ViewModelProviders.of((FragmentActivity) contextRef.get()).get(SentFilesViewModel.class);
        final Date d = new Date();
        if (_res) { // _res = true cond.
            // When the loop is finished, updates the notification
            mBuilder.setTicker(contextRef.get().getString(R.string.successful))
                    .setContentText(contextRef.get().getString(R.string.file_sent))
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setOngoing(false)
                    .setProgress(0, 0, false);

            SharedPreferences notificationSettings = PreferenceManager.getDefaultSharedPreferences(contextRef.get());
            if (notificationSettings.getBoolean("notifications_new_file_send", false)) {
                mBuilder.setSound(
                    Uri.parse(
                        PreferenceManager.getDefaultSharedPreferences(contextRef.get()).getString(
                            "notifications_new_file_send_ringtone",
                            "android.resource://" + contextRef.get().getPackageName() + "/" + R.raw.file_receive)
                    )
                );

                if (notificationSettings.getBoolean("notifications_new_file_send_vibrate", true))
                    mBuilder.setVibrate(new long[]{500});

                mBuilder.setLights(
                    Color.parseColor("#" + notificationSettings.getString("notifications_new_file_send_led_light", contextRef.get().getString(R.string.cyan_color))),
                    700,
                    500
                );
            } else {
                mBuilder.setSound(
                    Uri.parse("android.resource://" + contextRef.get().getPackageName() + "/" + R.raw.file_receive)
                );

                mBuilder.setLights(
                    Color.parseColor("#" + contextRef.get().getString(R.string.cyan_color)), 700, 500
                );
            }
            mNotifyManager.notify(id, mBuilder.build());

            if (_isArchFile) {
                for (int i = 0; i < fileNameList.size(); i++) {
                    String fileName = fileNameList.get(i);
                    sfe = new SentFilesEntity();
                    sfe.s_f_name = fileName;

                    String destDir;
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf('.') + 1));
                    if (mimeType != null) {
                        if (mimeType.startsWith("image")) {
                            sfe.s_f_type = "1";
                            destDir = "/storage/emulated/0/MickiNet/Photos/Sent/" + fileName;
                        } else if (mimeType.startsWith("video")) {
                            sfe.s_f_type = "2";
                            destDir = "/storage/emulated/0/MickiNet/Videos/Sent/" + fileName;
                        } else if (mimeType.startsWith("music") || mimeType.startsWith("audio")) {
                            sfe.s_f_type = "3";
                            destDir = "/storage/emulated/0/MickiNet/Media/Sent/" + fileName;
                        } else if (mimeType.equals("application/vnd.android.package-archive")) {
                            sfe.s_f_type = "4";
                            destDir = "/storage/emulated/0/MickiNet/APKs/Sent/" + fileName;
                        } else {
                            sfe.s_f_type = "5";
                            destDir = "/storage/emulated/0/MickiNet/Others/Sent/" + fileName;
                        }
                    } else {
                        sfe.s_f_type = "5";
                        destDir = "/storage/emulated/0/MickiNet/Others/Sent/" + fileName;
                    }
                    sfe.s_f_operation_status = "1";
                    sfe.s_f_time = d;
                    sfvm.insert(sfe);

                    // after inserting file information to db, copy that file to proper dir
                    try {
                        MixedUtils.copyFileToDir(
                            contextRef.get(),
                            Uri.parse(fileDirList.get(i)),
                            new File(destDir)
                        );
                        // TODO: test the line below
                        // TODO: user her hansisa bir anda butun qovluqlari silse, fışdırdı...
                    } catch (IOException ignored) {  }
                }
            } else {
                sfe.s_f_operation_status = "1";
                sfvm.insert(sfe);
            }
        } else {
            mBuilder.setContentText(contextRef.get().getString(R.string.file_sent_fail))
                    .setTicker(contextRef.get().getString(R.string.file_is_not_sent))
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());

            if (_isArchFile) {
                for (String fileName : fileNameList) {
                    sfe = new SentFilesEntity();
                    sfe.s_f_name = fileName;

                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf('.') + 1));
                    if (mimeType != null) {
                        if (mimeType.startsWith("image"))
                            sfe.s_f_type = "1";
                        else if (mimeType.startsWith("video"))
                            sfe.s_f_type = "2";
                        else if (mimeType.startsWith("music") || mimeType.startsWith("audio"))
                            sfe.s_f_type = "3";
                        else if (mimeType.equals("application/vnd.android.package-archive"))
                            sfe.s_f_type = "4";
                        else
                            sfe.s_f_type = "5";
                    } else
                        sfe.s_f_type = "5";

                    sfe.s_f_operation_status = "0";
                    sfe.s_f_time = d;
                    sfvm.insert(sfe);
                }
            } else {
                sfe.s_f_operation_status = "0";
                sfvm.insert(sfe);
            }
        }

        isTaskRunning = false;
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
     * @param out output stream
     * @param context context
     */
    private void sendFile(InputStream inputStream, OutputStream out, @NotNull Context context) {
        // Q) Why 8192 ?
        // A) http://stackoverflow.com/a/19561265/4057688
        byte buffer[] = new byte[2048];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1)
                out.write(buffer, 0, len);

            out.close();
            out.flush();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("send file", e.getMessage());
            //Log.e( "sendFile: ", "problem burdadi nese");
            //CrashReport.report(context, this.getClass().getName(), e);
            mBuilder.setContentText(context.getString(R.string.file_sent_fail))
                    .setTicker(context.getString(R.string.file_is_not_sent))
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());
        }
    }
}
