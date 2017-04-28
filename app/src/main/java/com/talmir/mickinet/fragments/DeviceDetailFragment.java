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
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.ApkShareActivity;
import com.talmir.mickinet.helpers.FileReceiverAsyncTask;
import com.talmir.mickinet.helpers.IDeviceActionListener;
import com.talmir.mickinet.services.FileTransferService;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final int CHOOSE_FILE_RESULT_CODE = 20;
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
//                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // for Camera app

//                            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                            fileIntent.setType("*/*");
//                            fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

//                            Intent contactsIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                            contactsIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);

//                            Intent apkShareIntent = ;
                            startActivity(new Intent(getActivity(), ApkShareActivity.class));

//                            Intent chooserIntent = Intent.createChooser(fileIntent, "Pick file");
//                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{ takePictureIntent, apkShareIntent });
//                            startActivityForResult(chooserIntent, CHOOSE_FILE_RESULT_CODE);
                        }
                        else {
                            if (FileReceiverAsyncTask.getClientIpAddress().equals(""))
                            Toast.makeText(getActivity(), "Sorry! You'll be able to send files after a successful connection.", Toast.LENGTH_LONG).show();
                            else {
                                // Allow user to pick an image from Gallery or other registered apps
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("*/*");
                                startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                            }
                        }
                    }
                });
        return mContentView;
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
            case CHOOSE_FILE_RESULT_CODE:
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
