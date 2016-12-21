package com.talmir.transferfileoverwifidirect.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.talmir.transferfileoverwifidirect.activities.HomeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

 /**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileSenderAsyncTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private TextView statusText;

    /**
     * @param context {@link HomeActivity}
     * @param statusText status of the {@link View}
     */
    public FileSenderAsyncTask(Context context, View statusText) {
        this.context = context;
        this.statusText = (TextView) statusText;
    }

    @SuppressLint("LongLogTag")
    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            Log.d(HomeActivity.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();

//            String fileName = "";
//            BufferedInputStream buffinBufferedInputStream = new BufferedInputStream(client.getInputStream());
//            try (DataInputStream d = new DataInputStream(buffinBufferedInputStream)) {
//                fileName = d.readUTF();
//                buffinBufferedInputStream.close();
//            }
//            Toast.makeText(context, fileName, Toast.LENGTH_SHORT).show();


            Log.d(HomeActivity.TAG, "Server: connection done");
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                    + ".jpg");

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.d(HomeActivity.TAG, "server: copying files " + f.toString());
            InputStream inputstream = client.getInputStream();
            copyFile(inputstream, new FileOutputStream(f));
            serverSocket.close();
            return f.getAbsolutePath();
        } catch (IOException e) {
            Log.e(HomeActivity.TAG, e.getMessage());
            return null;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            statusText.setText("File copied - " + result);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + result), "*/*");
            context.startActivity(intent);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        statusText.setText("Opening a server socket");
    }


     @SuppressLint("LongLogTag")
     public static boolean copyFile(InputStream inputStream, OutputStream out) {
         byte buf[] = new byte[1024];
         int len;
         try {
             while ((len = inputStream.read(buf)) != -1)
                 out.write(buf, 0, len);

             out.close();
             inputStream.close();
             return true;
         } catch (IOException e) {
             Log.d(HomeActivity.TAG, e.toString());
             return false;
         }
     }
}