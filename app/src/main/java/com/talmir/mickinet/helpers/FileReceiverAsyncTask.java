package com.talmir.mickinet.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * TODO: comment should be added!
 */
public class FileReceiverAsyncTask extends AsyncTask<Void, Void, String> {

    // TODO: should be changed to List<String> clientIpAddressList in future
    private static String clientIpAddress = "";
    private Context context;

    private int id;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public FileReceiverAsyncTask(Context context) {
        this.context = context;
    }

    public static String getClientIpAddress() {
        return clientIpAddress;
    }

    @Nullable
    private static String copyFile(InputStream inputStream) {
        // http://stackoverflow.com/a/19561265/4057688
        byte buf[] = new byte[8192];

        int len;
        try {
            byte[] buffer_fileNameLength = new byte[4];
            inputStream.read(buffer_fileNameLength, 0, 4);
            int fileNameLength = getIntFromByteArray(buffer_fileNameLength);

            byte[] buffer_fileName = new byte[fileNameLength];
            inputStream.read(buffer_fileName, 0, fileNameLength);
            final String fileName = new String(buffer_fileName, Charset.forName("UTF-8"));

            final File receivedFile = new File(Environment.getExternalStorageDirectory() + "/MickiNet/" + fileName);
            File dirs = new File(receivedFile.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            receivedFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(receivedFile);

            while ((len = inputStream.read(buf)) != -1)
                outputStream.write(buf, 0, len);

            outputStream.close();
            outputStream.flush();
            inputStream.close();

            return receivedFile.getAbsolutePath();
        } catch (IOException e) {
            Log.d(HomeActivity.TAG, e.toString());
            return null;
        }
    }

    private static int getIntFromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(4126);
            Log.d(HomeActivity.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();

            id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(context);

            // Issues the notification
            mBuilder.setTicker("Receiving the file")
                    .setContentTitle("File Receive")
                    .setContentText("Receiving the file")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true)
                    .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.file_receive))
                    .setVibrate(new long[]{0, 1000, 200, 1000})
                    .setProgress(0, 0, true);

            mNotifyManager.notify(id, mBuilder.build());

            final String temp_str = client.getRemoteSocketAddress().toString();
            clientIpAddress = temp_str.substring(1, temp_str.indexOf(':'));

            Log.d(HomeActivity.TAG, "Server: connection done");
//            final long current = System.currentTimeMillis();
//            final File f = new File(Environment.getExternalStorageDirectory() + "/MickiNet/" + current);
//
//            File dirs = new File(f.getParent());
//            if (!dirs.exists())
//                dirs.mkdirs();
//            f.createNewFile();

            InputStream inputStream = client.getInputStream();
            final String result = copyFile(inputStream/*, new FileOutputStream(f), context*/);

            client.close();
            serverSocket.close();

            return result;
        } catch (Exception e) {
            Log.e(HomeActivity.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {

            Intent openReceivedFile = new Intent(Intent.ACTION_VIEW);
            openReceivedFile.setDataAndType(
                    Uri.parse("file://" + result),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(result.substring(result.lastIndexOf('.') + 1))
            );

            PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, 0, openReceivedFile, PendingIntent.FLAG_ONE_SHOT);

            // When the loop is finished, updates the notification
            mBuilder.setContentTitle("File received")
                    .setContentText("Click to open")
                    .setTicker("File received")
                    .setContentIntent(notificationPendingIntent)
                    .setProgress(0, 0, false)
                    .setSmallIcon(R.drawable.ic_done)
                    .setLights(Color.rgb(0, 78, 142), 1000, 700)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());
        } else {
            mBuilder.setContentText("File receive failed")
                    .setTicker("File does not received")
                    .setProgress(0, 0, false)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setLights(Color.rgb(0, 78, 142), 1000, 700)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());

        }
    }
}