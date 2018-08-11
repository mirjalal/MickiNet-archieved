package com.talmir.mickinet.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.ApkShareActivity;
import com.talmir.mickinet.helpers.background.CrashReport;
import com.talmir.mickinet.helpers.background.FileReceiverAsyncTask;
import com.talmir.mickinet.helpers.background.IDeviceActionListener;
import com.talmir.mickinet.helpers.background.services.FileTransferService;
import com.talmir.mickinet.helpers.room.received.ReceivedFilesViewModel;
import com.talmir.mickinet.helpers.room.sent.SentFilesViewModel;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener  {

    private static final int ACTION_CHOOSE_APP_RESULT_CODE = 290;
    private static final int ACTION_CHOOSE_FILE_RESULT_CODE = 551;
    private static final int ACTION_TAKE_PICTURE_RESULT_CODE = 640;
    private static final int ACTION_TAKE_VIDEO_RESULT_CODE = 722;

    private static WifiP2pInfo info;
    public ProgressDialog progressDialog = null;

    // 0 - group owner (server), 1 - client
    private static int deviceType = -1;

    private static SentFilesViewModel mSentFilesViewModel;
    private static ReceivedFilesViewModel mReceivedFilesViewModel;

    @Contract(pure = true)
    public static SentFilesViewModel getSentFilesViewModel() {
        return mSentFilesViewModel;
    }

    @Contract(pure = true)
    public static ReceivedFilesViewModel getReceivedFilesViewModel() {
        return mReceivedFilesViewModel;
    }

    // Great article!
    // https://medium.com/@chrisbanes/appcompat-v23-2-age-of-the-vectors-91cbafa87c88#.59mn8eem4
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mContentView = inflater.inflate(R.layout.fragment_device_detail, null);

        mSentFilesViewModel = ViewModelProviders.of((FragmentActivity) getActivity()).get(SentFilesViewModel.class);
        mReceivedFilesViewModel = ViewModelProviders.of((FragmentActivity) getActivity()).get(ReceivedFilesViewModel.class);

        mContentView.findViewById(R.id.photo_camera_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), ACTION_TAKE_PICTURE_RESULT_CODE);
            } else {
                if (FileReceiverAsyncTask.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), "Sorry! You'll be able to send files after a successful connection.", Toast.LENGTH_LONG).show();
                else {
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), ACTION_TAKE_PICTURE_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.video_camera_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE), ACTION_TAKE_VIDEO_RESULT_CODE);
            } else {
                if (FileReceiverAsyncTask.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), "Sorry! You'll be able to send files after a successful connection.", Toast.LENGTH_LONG).show();
                else {
                    startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE), ACTION_TAKE_VIDEO_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.file_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(fileIntent, ACTION_CHOOSE_FILE_RESULT_CODE);
            } else {
                if (FileReceiverAsyncTask.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), "Sorry! You'll be able to send files after a successful connection.", Toast.LENGTH_LONG).show();
                else {
                    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    fileIntent.setType("*/*");
                    fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(fileIntent, ACTION_CHOOSE_FILE_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.pick_app_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                startActivityForResult(new Intent(getActivity(), ApkShareActivity.class), ACTION_CHOOSE_APP_RESULT_CODE);
            } else {
                if (FileReceiverAsyncTask.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), "Sorry! You'll be able to send files after a successful connection.", Toast.LENGTH_LONG).show();
                else {
                    startActivityForResult(new Intent(getActivity(), ApkShareActivity.class), ACTION_CHOOSE_APP_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.disconnect_action).setOnClickListener(v -> ((IDeviceActionListener) getActivity()).disconnect());

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(
            FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
            deviceType == 1 ? info.groupOwnerAddress.getHostAddress() : FileReceiverAsyncTask.getClientIpAddress()
        );
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 4126);

        switch (requestCode) {
            case ACTION_TAKE_PICTURE_RESULT_CODE:
                // User has taken a picture. Transfer it to group owner i.e peer using FileTransferService
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, getFileName(uri));
                    getActivity().startService(serviceIntent);
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_TAKE_VIDEO_RESULT_CODE:
                // User has taken a video_camera. Transfer it to group owner i.e peer using FileTransferService.
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, getFileName(uri));
                    getActivity().startService(serviceIntent);
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_CHOOSE_FILE_RESULT_CODE:
                // User has picked a file. Transfer it to group owner i.e peer using FileTransferService.
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, getFileName(uri));
                    getActivity().startService(serviceIntent);
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_CHOOSE_APP_RESULT_CODE:
                if (data != null && data.getExtras().getBoolean("share_apk")) {
                    String apk_dir = data.getExtras().getString("apk_dir");
                    String apk_name = data.getExtras().getString("apk_name");
                    if (apk_dir != null && apk_name != null) {
                        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "file://" + apk_dir);
                        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, apk_name + ".apk");
                        getActivity().startService(serviceIntent);
                    } else {
                        CrashReport.report(getActivity(), DeviceDetailFragment.class.getName());
                    }
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_an_app, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        DeviceDetailFragment.info = info;
        getView().setVisibility(View.VISIBLE);

        new FileReceiverAsyncTask(getActivity(), getView()/*, mReceivedFilesViewModel*/).execute();

        if (info.groupFormed && info.isGroupOwner) {
            Thread ipReceiverThread = receiveIpAddress();
            ipReceiverThread.setPriority(10);
            ipReceiverThread.start();

            deviceType = 0;

            if (!ipReceiverThread.isInterrupted())
                ipReceiverThread.interrupt();
        } else if (info.groupFormed) {
            Thread ipSenderThread = sendIpAddress();
            ipSenderThread.setPriority(10);
            ipSenderThread.start();

            deviceType = 1;

            if (!ipSenderThread.isInterrupted())
                ipSenderThread.interrupt();
        }
        getActivity().findViewById(R.id.start_discover).setVisibility(View.INVISIBLE);
    }

    /**
     * This method performs simple socket connection
     * between client device and server one. On the server
     * side we could get that IP address.
     *
     * @return  a new thread that sends client's
     *          Wi-Fi Direct IP address to the server.
     */
    @NonNull
    private synchronized Thread sendIpAddress() {
        return new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress("192.168.49.1", 10000)), 5000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (!socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * This method accepts incoming socket connection
     * and gets remote socket address of the client.
     *
     * @return  a new thread that receives clients'
     *          Wi-Fi Direct IP addresses
     */
    @NonNull
    private synchronized Thread receiveIpAddress() {
        return new Thread(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                if (!serverSocket.isBound())
                    serverSocket.bind(new InetSocketAddress(10000), 1);

                if (serverSocket.isBound() && !serverSocket.isClosed()) {
                    String clientIP = serverSocket.accept().getRemoteSocketAddress().toString();
                    FileReceiverAsyncTask.setClientIpAddress(clientIP.substring(1, clientIP.indexOf(':')));
                }
            } catch (Exception ignored) {  } finally {
                try {
                    if (serverSocket != null && !serverSocket.isClosed())
                        serverSocket.close();
                } catch (Exception ignored) {
                }
            }
        });
    }

    private String getFileName(@NotNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try {
                Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    cursor.close();
                }
            } catch (NullPointerException e) {
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }
        return result;
    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        getView().setVisibility(View.GONE);
    }
}
