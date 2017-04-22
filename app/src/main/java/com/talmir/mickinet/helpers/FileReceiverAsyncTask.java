package com.talmir.mickinet.helpers;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;
import com.talmir.mickinet.activities.SearchMimeTypeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TODO: comment should be added!
 */
public class FileReceiverAsyncTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private static String fileName;

    private final static int id = 1;

    // TODO: should be changed to List<String> clientIpAddressList in future
    private static String clientIpAddress = "";

    public static String getFileName() {
        return fileName;
    }

    // TODO: should be changed to List<String> getClientIpAddressList in future
    public static String getClientIpAddress() { return clientIpAddress; }

    /**
     * @param context    {@link HomeActivity}
     */
    public FileReceiverAsyncTask(Context context) {
        this.context = context;
    }

    // TODO: notification hissesi duzeldilmelidir
    private static boolean copyFile(InputStream inputStream, OutputStream out, Context c) {
        // http://stackoverflow.com/a/19561265/4057688
        byte buf[] = new byte[8192];
        int len;
        try {
            NotificationManager mNotifyManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c);

            // Issues the notification
            mBuilder.setTicker("Receiving the file")
                    .setContentTitle("File Receive")
                    .setContentText("Receiving the file")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true)
                    .setSound(Uri.parse("android.resource://" + c.getPackageName() + "/" + R.raw.file_receive));

            // Sets an activity indicator for an operation of indeterminate length
            mBuilder.setProgress(0, 0, true);
            mNotifyManager.notify(id, mBuilder.build());

            while ((len = inputStream.read(buf)) != -1)
                out.write(buf, 0, len);

            out.close();
            out.flush();
            inputStream.close();

            // When the loop is finished, updates the notification
            mBuilder.setContentText("File received")
                    .setTicker("File received")
                    .setProgress(0, 0, false)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setLights(Color.rgb(0, 78, 142), 1500, 1000)
                    .setOngoing(false)
                    .setVibrate(new long[] {0, 1000, 200, 1000 });

            mNotifyManager.notify(id, mBuilder.build());

            return true;
        } catch (IOException e) {
            Log.d(HomeActivity.TAG, e.toString());
            return false;
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(4126);
            Log.d(HomeActivity.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();

            final String temp_str = client.getRemoteSocketAddress().toString();
            clientIpAddress = temp_str.substring(1, temp_str.indexOf(':'));

            Log.d(HomeActivity.TAG, "Server: connection done");
            final long current = System.currentTimeMillis();
            final File f = new File(Environment.getExternalStorageDirectory() + "/MickiNet/" + current);

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.d(HomeActivity.TAG, "server: copying files " + f.toString());

            InputStream inputStream = client.getInputStream();
            copyFile(inputStream, new FileOutputStream(f), context);

            client.close();
            serverSocket.close();
            return f.getAbsolutePath();
        } catch (IOException e) {
            Log.e(HomeActivity.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            fileName = result;
            context.startActivity(new Intent(context, SearchMimeTypeActivity.class));
        }
    }

//    @Override
//    protected void onPreExecute() {
//
//    }
}