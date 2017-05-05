package com.talmir.mickinet.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.talmir.mickinet.activities.HomeActivity;

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

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
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
                    Log.d(HomeActivity.TAG, e.toString());
                }

                // put all data to stream
                copyFile(inputStream, outputStream, context);

                Log.d(HomeActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(HomeActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
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
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value};
    }

    /**
     * Sends data
     *
     * @param inputStream input stream
     * @param out outputstream
     * @param c context
     * @return true if succeeded, false otherwise
     */
    private boolean copyFile(InputStream inputStream, OutputStream out, Context c) {
        // http://stackoverflow.com/a/19561265/4057688
        byte buf[] = new byte[8192];
        int len;
        try {
            // generate unique id every time to show new notification each time
            int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            NotificationManager mNotifyManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c);

            // Issues the notification
            mBuilder.setTicker("File is on the way")
                    .setContentTitle("Sending file")
                    .setOngoing(true)
                    .setContentText("Sending...")
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setProgress(0, 0, true);
            mNotifyManager.notify(id, mBuilder.build());

            while ((len = inputStream.read(buf)) != -1)
                out.write(buf, 0, len);

            out.close();
            out.flush();
            inputStream.close();

            // When the loop is finished, updates the notification
            mBuilder.setTicker("File sent")
                    .setContentTitle("File sent")
                    .setContentText("")
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setOngoing(false)
                    .setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());

            return true;
        } catch (IOException e) {
            Log.d(HomeActivity.TAG, e.toString());
            return false;
        }
    }
}
