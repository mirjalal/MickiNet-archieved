package com.talmir.mickinet.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import com.talmir.mickinet.helpers.background.FileSenderAsyncTask;
import com.talmir.mickinet.helpers.background.IDeviceActionListener;
import com.talmir.mickinet.helpers.background.IP;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final int ACTION_CHOOSE_APP_RESULT_CODE = 290;
    private static final int ACTION_CHOOSE_FILE_RESULT_CODE = 551;
    private static final int ACTION_TAKE_PICTURE_RESULT_CODE = 640;
    private static final int ACTION_TAKE_VIDEO_RESULT_CODE = 722;

    private static WifiP2pInfo info;
    public ProgressDialog progressDialog = null;

    // 0 - group owner (server), 1 - client
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

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mContentView = inflater.inflate(R.layout.fragment_device_detail, null);

        mContentView.findViewById(R.id.photo_camera_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), ACTION_TAKE_PICTURE_RESULT_CODE);
            } else {
                if (IP.getClientIpAddress().equals(""))
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
                if (IP.getClientIpAddress().equals(""))
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
                if (IP.getClientIpAddress().equals(""))
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
                if (IP.getClientIpAddress().equals(""))
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

        String[] params = new String[3];
        params[0] = deviceType == 1 ? info.groupOwnerAddress.getHostAddress() : IP.getClientIpAddress();

        switch (requestCode) {
            case ACTION_TAKE_PICTURE_RESULT_CODE:
                // User has taken a picture. Transfer it to group owner i.e peer using FileTransferService
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    params[1] = uri.toString();
                    params[2] = getFileName(uri);
                    new FileSenderAsyncTask(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    params[1] = uri.toString();
                    params[2] = getFileName(uri);
                    new FileSenderAsyncTask(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    params[1] = uri.toString();
                    params[2] = getFileName(uri);
                    new FileSenderAsyncTask(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_CHOOSE_APP_RESULT_CODE:
                if (data != null && Objects.requireNonNull(data.getExtras()).getBoolean("share_apk")) {
                    String apk_dir = data.getExtras().getString("apk_dir");
                    String apk_name = data.getExtras().getString("apk_name");
                    if (apk_dir != null && apk_name != null) {
                        params[1] = "file://" + apk_dir;
                        params[2] = apk_name + ".apk";
                        new FileSenderAsyncTask(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        Objects.requireNonNull(getView()).setVisibility(View.VISIBLE);

        new FileReceiverAsyncTask(getActivity(), getView()).execute();

        if (info.groupFormed && info.isGroupOwner) {
            Thread ipReceiverThread = IP.receiveIpAddress();
            ipReceiverThread.setPriority(10);
            ipReceiverThread.start();

            deviceType = 0;

            if (!ipReceiverThread.isInterrupted())
                ipReceiverThread.interrupt();
        } else if (info.groupFormed) {
            Thread ipSenderThread = IP.sendIpAddress();
            ipSenderThread.setPriority(10);
            ipSenderThread.start();

            deviceType = 1;

            if (!ipSenderThread.isInterrupted())
                ipSenderThread.interrupt();
        }
        getActivity().findViewById(R.id.start_discover).setVisibility(View.INVISIBLE);
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
            } catch (NullPointerException ignored) {  }
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
        Objects.requireNonNull(getView()).setVisibility(View.GONE);
    }
}
