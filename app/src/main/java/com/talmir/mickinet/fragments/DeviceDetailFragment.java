package com.talmir.mickinet.fragments;

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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.ApkShareActivity;
import com.talmir.mickinet.helpers.FileReceiverAsyncTask;
import com.talmir.mickinet.helpers.IDeviceActionListener;
import com.talmir.mickinet.services.FileTransferService;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final int ACTION_TAKE_PICTURE_RESULT_CODE = 640;
    private static final int ACTION_TAKE_VIDEO_RESULT_CODE = 722;
    private static final int ACTION_CHOOSE_FILE_RESULT_CODE = 551;
    private static final int ACTION_CHOOSE_APP_RESULT_CODE = 290;

    private View mContentView = null;
    private static WifiP2pInfo info;
    public ProgressDialog progressDialog = null;
    private static int deviceType = -1;

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
                        }
                        else {
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

    private void chooseAction() {
        final android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(getActivity()).create();
        final View view = getActivity().getLayoutInflater().inflate(R.layout.choose_action, null);

        final RelativeLayout cameraAction = (RelativeLayout) view.findViewById(R.id.photo_camera_action);
        cameraAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), ACTION_TAKE_PICTURE_RESULT_CODE);
            }
        });

        final RelativeLayout videoAction = (RelativeLayout) view.findViewById(R.id.video_action);
        videoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE), ACTION_TAKE_VIDEO_RESULT_CODE);
            }
        });

        final RelativeLayout folderAction = (RelativeLayout) view.findViewById(R.id.file_action);
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

        final RelativeLayout chooseApp = (RelativeLayout) view.findViewById(R.id.remove_action);
        chooseApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivityForResult(new Intent(getActivity(), ApkShareActivity.class), ACTION_CHOOSE_APP_RESULT_CODE);
            }
        });
        dialog.setTitle("Choose action");
        dialog.setView(view);
        dialog.show();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACTION_TAKE_PICTURE_RESULT_CODE | ACTION_TAKE_VIDEO_RESULT_CODE | ACTION_CHOOSE_FILE_RESULT_CODE:
                // User has picked a file. Transfer it to group owner i.e peer using
                // FileTransferService.
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    Log.e("filename", getFileName(uri));
                    Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                    serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, getFileName(uri));
                    serviceIntent.putExtra(
                            FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            deviceType == 1 ? info.groupOwnerAddress.getHostAddress() :
                                    FileReceiverAsyncTask.getClientIpAddress()
                    );
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 4126);
                    getActivity().startService(serviceIntent);
                } else {
                    Toast t = Toast.makeText(getActivity(), "No file selected", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_CHOOSE_APP_RESULT_CODE:
                if (data != null && data.getExtras().getBoolean("share_apk")) {
                    String apk_dir = data.getExtras().getString("apk_dir");
                    String apk_name = data.getExtras().getString("apk_name");
                    if (apk_dir != null && apk_name != null) {
//                        Uri uri = Uri.parse();
                        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "file://" + apk_dir);
                        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_NAME, apk_name + ".apk");
                        serviceIntent.putExtra(
                                FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                deviceType == 1 ? info.groupOwnerAddress.getHostAddress() :
                                        FileReceiverAsyncTask.getClientIpAddress()
                        );
                        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 4126);
                        getActivity().startService(serviceIntent);
                    } else {
                        Log.e("apk_sharing", "something went wrong.");
                    }
                } else {
                    Toast t = Toast.makeText(getActivity(), "Select an application", Toast.LENGTH_LONG);
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

        new FileReceiverAsyncTask(getActivity()).execute();

        if (info.groupFormed && info.isGroupOwner) {
            deviceType = 0;
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            deviceType = 1;
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources().getString(R.string.client_text));
        }
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
