package com.talmir.mickinet.fragments;

import com.google.firebase.crash.FirebaseCrash;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.ApkShareActivity;
import com.talmir.mickinet.helpers.background.FileReceiverAsyncTask;
import com.talmir.mickinet.helpers.background.IDeviceActionListener;
import com.talmir.mickinet.services.FileTransferService;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final int ACTION_CHOOSE_APP_RESULT_CODE = 290;
    private static final int ACTION_CHOOSE_FILE_RESULT_CODE = 551;
    private static final int ACTION_TAKE_PICTURE_RESULT_CODE = 640;
    private static final int ACTION_TAKE_VIDEO_RESULT_CODE = 722;

    private View mContentView = null;
    private static WifiP2pInfo info;
    public ProgressDialog progressDialog = null;
    private static int deviceType = -1;

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
        mContentView = inflater.inflate(R.layout.fragment_device_detail, null);
        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((IDeviceActionListener) getActivity()).disconnect();
                    }
                });
        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (deviceType == 1) {
                            chooseAction();
                        } else {
                            sendIpAddress();
                            if (FileReceiverAsyncTask.getClientIpAddress().equals(""))
                                Toast.makeText(getActivity(), "Sorry! You'll be able to send files after a successful connection.", Toast.LENGTH_LONG).show();
                            else {
                                chooseAction();
                            }
                        }
                    }
                });
        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACTION_TAKE_PICTURE_RESULT_CODE:
                // User has taken a picture. Transfer it to group owner i.e peer using FileTransferService
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                    serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, getFileName(uri));
                    serviceIntent.putExtra(
                            FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            deviceType == 1 ? info.groupOwnerAddress.getHostAddress() : FileReceiverAsyncTask.getClientIpAddress()
                    );
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 4126);
                    getActivity().startService(serviceIntent);
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_TAKE_VIDEO_RESULT_CODE:
                // User has taken a video. Transfer it to group owner i.e peer using
                // FileTransferService.
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                    serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, getFileName(uri));
                    serviceIntent.putExtra(
                            FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            deviceType == 1 ? info.groupOwnerAddress.getHostAddress() : FileReceiverAsyncTask.getClientIpAddress()
                    );
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 4126);
                    getActivity().startService(serviceIntent);
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_CHOOSE_FILE_RESULT_CODE:
                // User has picked a file. Transfer it to group owner i.e peer using
                // FileTransferService.
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                    serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, getFileName(uri));
                    serviceIntent.putExtra(
                            FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            deviceType == 1 ? info.groupOwnerAddress.getHostAddress() : FileReceiverAsyncTask.getClientIpAddress()
                    );
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 4126);
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
                        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "file://" + apk_dir);
                        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, apk_name + ".apk");
                        serviceIntent.putExtra(
                                FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                deviceType == 1 ? info.groupOwnerAddress.getHostAddress() : FileReceiverAsyncTask.getClientIpAddress()
                        );
                        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 4126);
                        getActivity().startService(serviceIntent);
                    } else {
                        FirebaseCrash.log("DeviceDetailFragment");
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
        this.getView().setVisibility(View.VISIBLE);

        new FileReceiverAsyncTask(getActivity(), this.getView()).execute();

        if (info.groupFormed && info.isGroupOwner) {
            Thread ipReceiverThread = receiveIpAddress();
            ipReceiverThread.setPriority(10);
            ipReceiverThread.start();

            deviceType = 0;
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);

            if (!ipReceiverThread.isInterrupted())
                ipReceiverThread.interrupt();
        } else if (info.groupFormed) {
            Thread ipSenderThread = sendIpAddress();
            ipSenderThread.setPriority(10);
            ipSenderThread.start();

            // The other device acts as the client. In this case, we enable the
            // get file button.
            deviceType = 1;

            // TODO: create listener to receive other connected clients' detail (IP, MAC, and other details of a new connected device)

            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources().getString(R.string.client_text));

            if (!ipSenderThread.isInterrupted())
                ipSenderThread.interrupt();
        }
    }

    @NonNull
    private synchronized Thread sendIpAddress() {
        return new Thread(new Runnable() {
            public void run () {
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
            }
        });
    }

    @NonNull
    private synchronized Thread receiveIpAddress() {
        return new Thread(new Runnable() {
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    if (!serverSocket.isBound())
                        serverSocket.bind(new InetSocketAddress(10000), 1);

                    if (serverSocket != null && serverSocket.isBound() && !serverSocket.isClosed()) {
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
            }
        });
    }

    private void chooseAction() {
        final android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(getActivity()).create();
        final View view = getActivity().getLayoutInflater().inflate(R.layout.choose_action, null);

        final RelativeLayout photoCameraAction = (RelativeLayout) view.findViewById(R.id.photo_camera_action);
        ImageView photoCameraActionImageView = (ImageView) photoCameraAction.getChildAt(0);
        photoCameraActionImageView.setImageResource(R.drawable.ic_photo_camera_action);
        photoCameraAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), ACTION_TAKE_PICTURE_RESULT_CODE);
            }
        });

        final RelativeLayout videoCameraAction = (RelativeLayout) view.findViewById(R.id.video_camera_action);
        ImageView videoCameraActionImageView = (ImageView) videoCameraAction.getChildAt(0);
        videoCameraActionImageView.setImageResource(R.drawable.ic_video_camera_action);
        videoCameraAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE), ACTION_TAKE_VIDEO_RESULT_CODE);
            }
        });

        final RelativeLayout folderAction = (RelativeLayout) view.findViewById(R.id.file_action);
        ImageView folderActionImageView = (ImageView) folderAction.getChildAt(0);
        folderActionImageView.setImageResource(R.drawable.ic_folder_action);
        folderAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(fileIntent, ACTION_CHOOSE_FILE_RESULT_CODE);
            }
        });

        final RelativeLayout chooseAppAction = (RelativeLayout) view.findViewById(R.id.pick_app_action);
        ImageView chooseAppActionImageView = (ImageView) chooseAppAction.getChildAt(0);
        chooseAppActionImageView.setImageResource(R.drawable.ic_pick_apk_action);
        chooseAppAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivityForResult(new Intent(getActivity(), ApkShareActivity.class), ACTION_CHOOSE_APP_RESULT_CODE);
            }
        });
        dialog.setTitle(R.string.choose_action);
        dialog.setView(view);
        dialog.show();
    }

    private String getFileName(Uri uri) {
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
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
//        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
//        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
//        view.setText(device.deviceAddress);
//        view = (TextView) mContentView.findViewById(R.id.device_info);
//        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        this.getView().setVisibility(View.GONE);
    }
}
