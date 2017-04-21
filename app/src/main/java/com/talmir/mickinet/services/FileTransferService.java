package com.talmir.mickinet.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

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

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            Socket socket = new Socket();
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream inputStream = null;
                try {
                    inputStream = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(HomeActivity.TAG, e.toString());
                }
                copyFile(inputStream, stream, context);
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

    private boolean copyFile(InputStream inputStream, OutputStream out, Context c) {
        // http://stackoverflow.com/a/19561265/4057688
        byte buf[] = new byte[8192];
        int len;
        try {
            int id = 1;
            NotificationManager mNotifyManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c);

            // Issues the notification
            mBuilder.setTicker("File is on the way")
                    .setContentTitle("Sending file")
//                    .setContentText("Sending the file")
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setSound(Uri.parse("android.resource://" + c.getPackageName() + "/" + R.raw.file_receive));

            // Sets an activity indicator for an operation of indeterminate length
            mBuilder.setProgress(0, 0, true);
            mNotifyManager.notify(id, mBuilder.build());

            while ((len = inputStream.read(buf)) != -1)
                out.write(buf, 0, len);

            out.close();
            inputStream.close();

            // When the loop is finished, updates the notification
            mBuilder.setTicker("File sent")
                    .setContentText("File send")
                    .setProgress(0, 0, false)
                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                    .setLights(Color.rgb(0, 78, 142), 1500, 1000); // 1 saat

            mNotifyManager.notify(id, mBuilder.build());

            return true;
        } catch (IOException e) {
            Log.d(HomeActivity.TAG, e.toString());
            return false;
        }
    }
}
