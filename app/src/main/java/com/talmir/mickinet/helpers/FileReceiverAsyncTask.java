package com.talmir.mickinet.helpers;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.HomeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Date;

/**
 * TODO: comment should be added!
 */
public class FileReceiverAsyncTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private static String fileName;

    // TODO: should be changed to List<String> clientIpAddressList in future
    private static String clientIpAddress = "";

    public static String getFileName() {
        return fileName;
    }
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
            int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            NotificationManager mNotifyManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c);

            // Issues the notification
            mBuilder.setTicker("Receiving the file")
                    .setContentTitle("File Receive")
                    .setContentText("Receiving the file")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true)
                    .setSound(Uri.parse("android.resource://" + c.getPackageName() + "/" + R.raw.file_receive))
                    .setVibrate(new long[] { 0, 1000, 200, 1000 });

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
                    .setLights(Color.rgb(0, 78, 142), 1000, 700)
                    .setOngoing(false);

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
        } catch (Exception e) {
            Log.e(HomeActivity.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            fileName = result;

            final AlertDialog builder = new AlertDialog.Builder(context).create();
            builder.setTitle("Enter file name and extension");
            builder.setCancelable(false);

            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View view = inflater.inflate(R.layout.file_receive_dialog, null);

            final EditText fileNameWithExtension = (EditText) view.findViewById(R.id.file_name);
            final Button saveButton = (Button) view.findViewById(R.id.save);
            final Button cancelButton = (Button) view.findViewById(R.id.cancel);

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fileNameWithExtension.getText().toString().length() == 0) {
                        Toast t = Toast.makeText(context, "Don't leave it blank.", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.TOP, 0, 0);
                        t.show();
                    } else {
                        File receivedFile = new File(URI.create("file://" + getFileName()));
                        File newFile = new File(URI.create("file://" + receivedFile.getAbsolutePath() + fileNameWithExtension.getText().toString()));

                        if (receivedFile.renameTo(newFile)) {
                            builder.dismiss();
                            Toast t = Toast.makeText(context, "Saved!", Toast.LENGTH_LONG);
                            t.setGravity(Gravity.CENTER, 0, 0);
                            t.show();
                            try {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                newFile.createNewFile();
                                intent.setDataAndType(Uri.parse("file://" + newFile.getAbsolutePath()), "*/*");
                                context.startActivity(intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            Log.e("file", "cannot rename");
                    }
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.dismiss();
                }
            });

            builder.setView(view);
            builder.show();

//            context.startActivity(new Intent(context, SearchMimeTypeActivity.class));
        } else {
            Toast t = Toast.makeText(context, "Cannot receive file. Check app permissions.", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
    }
}