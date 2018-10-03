package com.talmir.mickinet.helpers.background.tasks;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.talmir.mickinet.R;
import com.talmir.mickinet.fragments.DeviceDetailFragment;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 *     ZipperAsyncTask class represents zipping process
 *     when multiple files selected to share from other
 *     applications. If user's device does not have a
 *     connection, proper methods will be executed. After
 *     finishing of zipping process, user will notified
 *     about it. This means app is ready to send the zip
 *     file when a connection established.
 *     Zip operation could not be cancelled during the
 *     process. However, when it is done user can interact
 *     with it (finished process) by notification. This
 *     allows user to cancel postponed send operation.
 * </p>
 * @author miri
 * @since 8/25/2018
 */
public class ZipperAsyncTask extends AsyncTask<Void, Integer, Boolean> {
    private static WeakReference<Context> _contextRef;
    private String[] _input_files;
    private static String _output_file;
    private long _totalLen;
    private boolean _offlineJob;

    private static final int id = 446;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private static final String NOTIFICATION_ACTION = "notification_action";

    public ZipperAsyncTask(@NotNull WeakReference<Context> contextReference, String[] files, String zipFileName, long totalFileLength, boolean workOffline) {
        _contextRef = contextReference;
        _input_files = files;
        _output_file = zipFileName;
        _totalLen = totalFileLength;
        _offlineJob = workOffline;

        mBuilder = new NotificationCompat.Builder(contextReference.get(), null);
        mNotifyManager = (NotificationManager) contextReference.get().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        mBuilder.setTicker("Process started")
                .setContentTitle("Processing...")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_processing)
                .setProgress(0, 0, false);

        mNotifyManager.notify(id, mBuilder.build());
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        long _len = 0L;
        BufferedInputStream origin;
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(_output_file))) {
            int BUFFER_SIZE = 6 * 1024;
            byte data[] = new byte[BUFFER_SIZE];
            for (String file : _input_files) {
                // get input stream from file
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    int previousProgress = 0;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                        _len += count;
                        int progress = (int) (_len * 100 / _totalLen);
                        if (progress > previousProgress) {
                            previousProgress = progress;
                            publishProgress(previousProgress);
                        }
                    }
                } finally {
                    origin.close();
                }
            }
        } catch (Exception e) {
            Log.e(ZipperAsyncTask.class.getName(), e.getMessage());
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mBuilder.setProgress(100, values[0], false).setContentText(+values[0] + "% done");
        mNotifyManager.notify(id, mBuilder.build());
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Intent action1Intent = new Intent(_contextRef.get(), NotificationActionService.class).setAction(NOTIFICATION_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(_contextRef.get(), 0, action1Intent, PendingIntent.FLAG_ONE_SHOT);

        mBuilder.setContentTitle("Processing finished")
                .setProgress(0, 0, false)
//                .setSmallIcon(R.drawable.ic_waiting_connection)
                .setStyle(new NotificationCompat.InboxStyle().addLine("Waiting for connection to continue"))
                .addAction(R.drawable.ic_cancel, _contextRef.get().getString(R.string.cancel), pendingIntent);
        mNotifyManager.notify(id, mBuilder.build());

        if (!_offlineJob) {
            final String f = _output_file.substring(_output_file.lastIndexOf('/') + 1);
            final String[] params = new String[] {
                DeviceDetailFragment.getIpAddressByDeviceType(),
                _contextRef.get().getFilesDir() + "/backup/" + f,
                f
            };
            mNotifyManager.cancel(id);

            new FileSenderAsyncTask(_contextRef.get(), _input_files, params).executeOnExecutor(THREAD_POOL_EXECUTOR);
        }
    }

    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String action = intent.getAction();
            if (NOTIFICATION_ACTION.equals(action)) {
                final File jobCancellationFile = new File(_output_file);
                if (jobCancellationFile.exists())
                    jobCancellationFile.delete();
                NotificationManagerCompat.from(this).cancel(id);
            }
        }
    }
}
