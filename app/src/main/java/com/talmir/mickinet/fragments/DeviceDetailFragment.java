package com.talmir.mickinet.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.activities.ApkShareActivity;
import com.talmir.mickinet.helpers.MixedUtils;
import com.talmir.mickinet.helpers.background.CrashReport;
import com.talmir.mickinet.helpers.background.IDeviceActionListener;
import com.talmir.mickinet.helpers.background.IP;
import com.talmir.mickinet.helpers.background.tasks.FileReceiver;
import com.talmir.mickinet.helpers.background.tasks.FileSender;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final int ACTION_TAKE_PICTURE_RESULT_CODE = 640;
    private static final int ACTION_TAKE_VIDEO_RESULT_CODE = 722;
    private static final int ACTION_CHOOSE_MEDIA_FILE_RESULT_CODE = 112;
    private static final int ACTION_CHOOSE_FILE_RESULT_CODE = 551;
    private static final int ACTION_CHOOSE_APP_RESULT_CODE = 290;

    private static WifiP2pInfo info;
    public static String getIpAddressByDeviceType() {
        return deviceType == 1 ? info.groupOwnerAddress.getHostAddress() : IP.getClientIpAddress();
    }

    public ProgressDialog progressDialog = null;

    // 0 - group owner (server), 1 - client
    private static int deviceType = -1;
    public int getDeviceType() { return deviceType; }

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
                Intent photoAction = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoAction.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(photoAction, ACTION_TAKE_PICTURE_RESULT_CODE);
            } else {
                if (IP.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), R.string.sorry_for_conection, Toast.LENGTH_LONG).show();
                else {
                    Intent photoAction = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoAction.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(photoAction, ACTION_TAKE_PICTURE_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.video_camera_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                Intent videoAction = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                videoAction.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(videoAction, ACTION_TAKE_VIDEO_RESULT_CODE);
            } else {
                if (IP.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), R.string.sorry_for_conection, Toast.LENGTH_LONG).show();
                else {
                    Intent videoAction = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    videoAction.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(videoAction, ACTION_TAKE_VIDEO_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.media_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                Intent mediaFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                mediaFileIntent.setType("audio/*");
                mediaFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                mediaFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(mediaFileIntent, ACTION_CHOOSE_MEDIA_FILE_RESULT_CODE);
            } else {
                if (IP.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), R.string.sorry_for_conection, Toast.LENGTH_LONG).show();
                else {
                    Intent mediaFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    mediaFileIntent.setType("audio/*");
                    mediaFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    mediaFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(mediaFileIntent, ACTION_CHOOSE_MEDIA_FILE_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.file_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(fileIntent, ACTION_CHOOSE_FILE_RESULT_CODE);
            } else {
                if (IP.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), R.string.sorry_for_conection, Toast.LENGTH_LONG).show();
                else {
                    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    fileIntent.setType("*/*");
                    fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(fileIntent, ACTION_CHOOSE_FILE_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.pick_app_action).setOnClickListener(v -> {
            if (deviceType == 1) {
                Intent getApp = new Intent(getActivity(), ApkShareActivity.class);
                getApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(getApp, ACTION_CHOOSE_APP_RESULT_CODE);
            } else {
                if (IP.getClientIpAddress().equals(""))
                    Toast.makeText(getActivity(), R.string.sorry_for_conection, Toast.LENGTH_LONG).show();
                else {
                    Intent getApp = new Intent(getActivity(), ApkShareActivity.class);
                    getApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(getApp, ACTION_CHOOSE_APP_RESULT_CODE);
                }
            }
        });

        mContentView.findViewById(R.id.disconnect_action).setOnClickListener(v -> ((IDeviceActionListener) getActivity()).disconnect());

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri;
        final String authority = getActivity().getApplicationContext().getPackageName() + ".provider";
        String[] params = new String[3];
        params[0] = deviceType == 1 ? info.groupOwnerAddress.getHostAddress() : IP.getClientIpAddress();
        final String path = "/storage/emulated/0/MickiNet/";

        switch (requestCode) {
            case ACTION_TAKE_PICTURE_RESULT_CODE:
                // User has taken a picture. Transfer it to group owner i.e peer using FileTransferService
                if (data != null && (uri = data.getData()) != null) {
                    params[1] = uri.toString();
                    params[2] = MixedUtils.getFileName(getActivity(), uri);
                    new FileSender(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    try {
                        Uri _uri_ = FileProvider.getUriForFile(getActivity(), authority, new File(params[2]));
                        MixedUtils.copyFileToDir(
                            getActivity(),
                            _uri_,
                            new File(path + "Photos/Sent/" + params[2])
                        );
                    } catch (IOException ignored) {
                    }
                } else {
                    Log.e("photo result", (data != null) + "");
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_TAKE_VIDEO_RESULT_CODE:
                // User has taken a video_camera. Transfer it to group owner i.e peer using FileTransferService.
                if (data != null && (uri = data.getData()) != null) {
                    params[1] = uri.toString();
                    params[2] = MixedUtils.getFileName(getActivity(), uri);
                    new FileSender(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    try {
                        Uri _uri_ = FileProvider.getUriForFile(
                            getActivity(),
                            authority,
                            new File(Objects.requireNonNull(MixedUtils.getFilePath(getActivity(), uri)))
                        );
                        MixedUtils.copyFileToDir(
                            getActivity(),
                            _uri_,
                            new File(path + "Videos/Sent/" + params[2])
                        );
                    } catch (IOException ignored) {
                    }
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_CHOOSE_MEDIA_FILE_RESULT_CODE:
                // User has picked a media file. Transfer it to group owner i.e peer using FileTransferService.
                if (data != null && (uri = data.getData()) != null) {
                    params[1] = uri.toString();
                    params[2] = MixedUtils.getFileName(getActivity(), uri);
                    try {
                        Uri _uri_ = FileProvider.getUriForFile(
                            getActivity(),
                            authority,
                            new File(Objects.requireNonNull(MixedUtils.getFilePath(getActivity(), uri)))
                        );
                        MixedUtils.copyFileToDir(
                            getActivity(),
                            _uri_,
                            new File(path + "Media/Sent/" + params[2])
                        );
                    } catch (IOException ignored) {
                    }
                    new FileSender(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Toast t = Toast.makeText(getActivity(), R.string.pick_a_file, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case ACTION_CHOOSE_FILE_RESULT_CODE:
                // User has picked a file. Transfer it to group owner i.e peer using FileTransferService.
                if (data != null && (uri = data.getData()) != null) {
                    params[1] = uri.toString();
                    params[2] = MixedUtils.getFileName(getActivity(), uri);
                    new FileSender(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    try {
                        String inner;
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(params[2].substring(params[2].lastIndexOf('.') + 1));
                        if (mimeType != null) {
                            if (mimeType.startsWith("image"))
                                inner = "Photos/Sent/";
                            else if (mimeType.startsWith("video"))
                                inner = "Videos/Sent/";
                            else if (mimeType.startsWith("music") || mimeType.startsWith("audio"))
                                inner = "Media/Sent/";
                            else if (mimeType.equals("application/vnd.android.package-archive"))
                                inner = "APKs/Sent/";
                            else
                                inner = "Others/Sent";
                        } else
                            inner = "Others/Sent";

                        Uri _uri_ = FileProvider.getUriForFile(
                            getActivity(),
                            authority,
                            new File(Objects.requireNonNull(MixedUtils.getFilePath(getActivity(), uri)))
                        );
                        MixedUtils.copyFileToDir(
                            getActivity(),
                            _uri_,
                            new File(path + inner + params[2])
                        );
                    } catch (IOException ignored) {
                    }
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
//                        Log.e("apk dir: ", params[1] + "/" + params[2]);
                        new FileSender(getActivity(), params).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        try {
                            MixedUtils.copyFileToDir(
                                getActivity(),
                                Uri.fromFile(new File(params[1] + "/" + params[2])),
                                new File(path + "APKs/Sent")
                            );
                        } catch (IOException ignored) {
                        }
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

        new FileReceiver(getActivity(), getView().findViewById(R.id.root)).execute();

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

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        Objects.requireNonNull(getView()).setVisibility(View.GONE);
    }
}
