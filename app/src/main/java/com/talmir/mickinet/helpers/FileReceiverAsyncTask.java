package com.talmir.mickinet.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
 * A custom class that receives stream and saves in
 * device storage.
 *
 *
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

    /**
     * Saves received input stream as a file in
     * device's internal storage.
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
    private static String saveFile(InputStream inputStream) {
        // recommended max buffer size is 8192.
        // read about in: http://stackoverflow.com/a/19561265/4057688
        byte buf[] = new byte[8192];

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

            // create a file in internal storage to dump data to it
            final File receivedFile = new File(Environment.getExternalStorageDirectory() + "/MickiNet/" + fileName);
            File dirs = new File(receivedFile.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            receivedFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(receivedFile);

            // read stream and write it to file
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

    /**
     * Helps to convert byte[] to int
     *
     * @param bytes to be converted to int
     * @return int value of bytes
     */
    private static int getIntFromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(4126);
            Log.d(HomeActivity.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();

            // generate unique id to show new notification each time a file received
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
                    .setProgress(0, 0, true);

            mNotifyManager.notify(id, mBuilder.build());

            final String temp_str = client.getRemoteSocketAddress().toString();
            clientIpAddress = temp_str.substring(1, temp_str.indexOf(':'));

            Log.d(HomeActivity.TAG, "Server: connection done");

            InputStream inputStream = client.getInputStream();
            final String result = saveFile(inputStream);

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
            // intent to open received file from notification click
            Intent openReceivedFile = new Intent(Intent.ACTION_VIEW);
            openReceivedFile.setDataAndType(
                    Uri.parse("file://" + result),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(result.substring(result.lastIndexOf('.') + 1))
            );

            PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, 0, openReceivedFile, PendingIntent.FLAG_ONE_SHOT);

            mBuilder.setTicker("File received")
                    .setContentTitle("File received")
                    .setContentText(result.substring(result.lastIndexOf('/') + 1))
                    .setSmallIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.drawable.ic_done : android.R.drawable.stat_sys_download_done)
                    .setOngoing(false)
                    .setProgress(0, 0, false)
                    .setContentIntent(notificationPendingIntent)
                    .setLights(Color.rgb(0, 218, 214), 700, 500);

            mNotifyManager.notify(id, mBuilder.build());
        } else {
            mBuilder.setContentText("An error occurred. File receive failed.")
                    .setTicker("File does not received")
                    .setProgress(0, 0, false)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setLights(Color.rgb(0, 78, 142), 1000, 700)
                    .setOngoing(false);

            mNotifyManager.notify(id, mBuilder.build());
        }
    }
}