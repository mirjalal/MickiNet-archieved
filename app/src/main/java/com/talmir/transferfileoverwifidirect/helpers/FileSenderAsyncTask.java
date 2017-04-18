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
    private static String mFileNameAndExtension;

    /**
     * @param context    {@link HomeActivity}
     * @param statusText status of the {@link View}
     */
    public FileSenderAsyncTask(Context context, View statusText) {
        this.context = context;
        this.statusText = (TextView) statusText;
    }

    @SuppressLint("LongLogTag")
    public static boolean copyFile(InputStream inputStream, OutputStream out, String fileNameAndExtension) {
        mFileNameAndExtension = fileNameAndExtension;
        byte buf[] = new byte[4096];
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

    private static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[4096];
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

    @SuppressLint("LongLogTag")
    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(4126);
            Log.d(HomeActivity.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();

            Log.d(HomeActivity.TAG, "Server: connection done");
            long current = System.currentTimeMillis();
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + context.getPackageName() + "/wifip2pshared-" + current);
//            final File f = new File(Environment.getExternalStorageDirectory() + "/MickiNet/" + mFileNameAndExtension);

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.d(HomeActivity.TAG, "server: copying files " + f.toString());

//            String contentType = null;
//            try {
//                InputStream stream = new FileInputStream(f);
//
//                AutoDetectParser parser = new AutoDetectParser();
//                BodyContentHandler handler = new BodyContentHandler();
//                Metadata metadata = new Metadata();
//
//                try {
////                     This step here is a little expensive
//                    parser.parse(stream, handler, metadata);
//                } catch (SAXException | TikaException e) {
//                    e.printStackTrace();
//                } finally {
//                    stream.close();
//                }
//
////                 metadata is a HashMap, you can loop over it see what you need. Alternatively, I think Content-Type is what you need
//                contentType = metadata.get("Content-Type");
//                Log.e("contentType", contentType+"");
//            }
//            catch (Exception e) {
//                Log.e("contentType: ", e.getMessage());
//            }

            InputStream inputStream = client.getInputStream();
            copyFile(inputStream, new FileOutputStream(f));



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
            statusText.setText("File copied - " + result);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + result), "*/*");
            context.startActivity(intent);
        }
    }

    @Override
    protected void onPreExecute() {
        statusText.setText("Opening a server socket");
    }
}