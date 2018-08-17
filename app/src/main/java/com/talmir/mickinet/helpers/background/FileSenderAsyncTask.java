package com.talmir.mickinet.helpers.background;

import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.room.sent.SentFilesEntity;
import com.talmir.mickinet.helpers.room.sent.SentFilesViewModel;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * @author miri
 * @since 8/12/2018
 */
public class FileSenderAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<Context> contextRef;

    private static final int id = 871;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    private boolean _res = false;
    private SentFilesEntity sfe;

    private String PARAM_GROUP_OWNER_ADDRESS;
    private static final int PARAM_GROUP_OWNER_PORT = 4126;
    private static final int PARAM_SOCKET_TIMEOUT = 5000;
    private String PARAM_FILE_PATH;
    private String PARAM_FILE_NAME;

    public FileSenderAsyncTask(Context context, @NotNull String... params) {
        contextRef = new WeakReference<>(context);

        PARAM_GROUP_OWNER_ADDRESS = params[0];
        PARAM_FILE_PATH = params[1];
        PARAM_FILE_NAME = params[2];

        mBuilder = new NotificationCompat.Builder(contextRef.get(), null);
        mNotifyManager = (NotificationManager) contextRef.get().getSystemService(Context.NOTIFICATION_SERVICE);

        sfe = new SentFilesEntity();
    }

    @Override
    protected void onPreExecute() {
        // Issues the notification
        mBuilder.setTicker(contextRef.get().getString(R.string.file_send))
                .setContentTitle(contextRef.get().getString(R.string.sending_file))
                .setContentText(contextRef.get().getString(R.string.sending))
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setProgress(0, 0, true);

        mNotifyManager.notify(id, mBuilder.build());
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        sfe.s_f_name = PARAM_FILE_NAME;

        // get mimetype to determine that in which folder will be used
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(PARAM_FILE_NAME.substring(PARAM_FILE_NAME.lastIndexOf('.') + 1));
        if (Objects.requireNonNull(mimeType).startsWith("image"))
            sfe.s_f_type = "1";
        else if (mimeType.startsWith("video"))
            sfe.s_f_type = "2";
        else if (mimeType.startsWith("music") || mimeType.startsWith("audio"))
            sfe.s_f_type = "4"; // for now, music types are accepted as others
        else if (mimeType.equals("application/vnd.android.package-archive"))
            sfe.s_f_type = "3";
        else
            sfe.s_f_type = "4";

        // put all data to stream
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry()/*, Locale.getDefault().getDisplayVariant()*/));
        sfe.s_f_time = sdf.format(new Date());

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
                CrashReport.report(contextRef.get(), this.getClass().getName(), e);
            }

            sendFile(inputStream, outputStream, contextRef.get());
            _res = true;
        } catch (Exception e) {
            CrashReport.report(contextRef.get(), this.getClass().getName(), e);
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
        super.onPostExecute(result);

        if (_res) { // _res = true cond.
            // When the loop is finished, updates the notification
            mBuilder.setTicker(contextRef.get().getString(R.string.successful))
//                    .setContentTitle(app.getString(R.string.file_sent))
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

            sfe.s_f_operation_status = "1";
        } else {
            mBuilder.setContentText(contextRef.get().getString(R.string.file_sent_fail))
                    .setTicker(contextRef.get().getString(R.string.file_is_not_sent))
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());

            sfe.s_f_operation_status = "0";
        }
        SentFilesViewModel sfvm = ViewModelProviders.of((FragmentActivity) contextRef.get()).get(SentFilesViewModel.class);
        sfvm.insert(sfe);
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
        byte buffer[] = new byte[8192];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1)
                out.write(buffer, 0, len);

            out.close();
            out.flush();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e( "sendFile: ", "problem burdadi nese");
            CrashReport.report(context, this.getClass().getName(), e);
            mBuilder.setContentText(context.getString(R.string.file_sent_fail))
                    .setTicker(context.getString(R.string.file_is_not_sent))
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setProgress(0, 0, false)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());
        }
    }
}
