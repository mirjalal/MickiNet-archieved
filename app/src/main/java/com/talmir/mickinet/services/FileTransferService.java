package com.talmir.mickinet.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.talmir.mickinet.activities.HomeActivity;
import com.talmir.mickinet.helpers.FileSenderAsyncTask;

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

    public static final String ACTION_SEND_FILE = "com.talmir.filesharer.SEND_FILE";
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

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            String fileNameAndExtension = intent.getExtras().getString(EXTRAS_FILE_NAME);

            Socket socket = new Socket();

            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                OutputStream stream = socket.getOutputStream();
//                stream.write(fileNameAndExtension.getBytes("UTF-8"));
                ContentResolver cr = context.getContentResolver();
                InputStream inputStream = null;
                try {
                    inputStream = cr.openInputStream(Uri.parse(fileUri));
//                    Toast.makeText(context, Uri.parse(fileUri).toString(), Toast.LENGTH_SHORT).show();
//                    BufferedOutputStream out = new BufferedOutputStream(stream);
//                    try (DataOutputStream d = new DataOutputStream(out)) {
//                        d.writeUTF(fileName);
//                        MediaStore.Files.copy(file.toPath(), d);
//                    }
                } catch (FileNotFoundException e) {
                    Log.d(HomeActivity.TAG, e.toString());
                }
                FileSenderAsyncTask.copyFile(inputStream, stream, fileNameAndExtension);
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
}
