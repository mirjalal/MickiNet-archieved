package com.talmir.mickinet.helpers.background.tasks;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesEntity;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * <p>
 *     UnZipperAsyncTask class represents unzipping
 *     process. Unzips received archive file contents
 *     to the same folder. We should do unzip process
 *     as fast as we can.
 * </p>
 *
 * @author miri
 * @since 8/25/2018
 */
public class UnzipperAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<Context> _context_ref;
    private String _mickinet_arch_file;
    private final String _extract_dest = "/storage/emulated/0/MickiNet/.temp/";
    private ArrayList<String> _zipped_files;

    private static final int id = 88;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    UnzipperAsyncTask(@NotNull WeakReference<Context> contextReference, String zipLocation) {
        _context_ref = contextReference;
        _mickinet_arch_file = zipLocation;

        mBuilder = new NotificationCompat.Builder(contextReference.get(), null);
        mNotifyManager = (NotificationManager) contextReference.get().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        mBuilder.setTicker("Processing...")
                .setContentTitle("Please wait. This will finish soon")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_processing)
                .setProgress(0, 0, true);

        mNotifyManager.notify(id, mBuilder.build());
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            _zipped_files = new ArrayList<>();

            InputStream fin = new FileInputStream(_mickinet_arch_file);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                _zipped_files.add(ze.getName());

                FileOutputStream fout = new FileOutputStream(new File(_extract_dest, ze.getName()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                // reading and writing
                while ((count = zin.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                    byte[] bytes = baos.toByteArray();
                    fout.write(bytes);
                    baos.reset();
                }
                fout.close();
                zin.closeEntry();
            }
            zin.close();
            Log.e("Finished unzip", "unzipping is ok");
            return true;
        } catch (Exception e) {
            Log.e("Unzip Error", e.getMessage() + "");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        NotificationManagerCompat.from(_context_ref.get()).cancel(id);

        final ReceivedFilesViewModel rfvm = HomeActivity.getReceivedFilesViewModel();
        ReceivedFilesEntity rfe;
        final Date date = new Date();
        final String rootFilesDir = "/storage/emulated/0/MickiNet/";
        String inner;

        for (String name : _zipped_files) {
            rfe = new ReceivedFilesEntity();
            rfe.r_f_name = name;
            rfe.r_f_operation_status = aBoolean ? "1" : "0";
            rfe.r_f_time = date;

            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(name.lastIndexOf('.') + 1));
            if (mimeType != null) {
                if (mimeType.startsWith("image")) {
                    rfe.r_f_type = "1";
                    inner = "Photos/Received/";
                } else if (mimeType.startsWith("video")) {
                    rfe.r_f_type = "2";
                    inner = "Videos/Received/";
                } else if (mimeType.startsWith("music") || mimeType.startsWith("audio")) {
                    rfe.r_f_type = "3";
                    inner = "Media/Received/";
                } else if (mimeType.equals("application/vnd.android.package-archive")) {
                    rfe.r_f_type = "4";
                    inner = "APKs/Received/";
                } else {
                    rfe.r_f_type = "5";
                    inner = "Others/Received/";
                }
            } else {
                rfe.r_f_type = "5";
                inner = "Others/Received/";
            }
            rfvm.insert(rfe);

            // move file to proper folder
            final File _unzipped = new File(_extract_dest + name);
            final File _move_to = new File(rootFilesDir + inner + name);
            _unzipped.renameTo(_move_to);
        }
        // delete /.temp/ folder
        deleteRecursive(new File("/storage/emulated/0/MickiNet/.temp/"));
    }

    private void deleteRecursive(@NotNull File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
