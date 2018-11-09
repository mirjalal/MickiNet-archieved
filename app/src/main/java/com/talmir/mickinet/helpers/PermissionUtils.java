package com.talmir.mickinet.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;

public final class PermissionUtils {

    public static final String[] CAMERA_PERMISSIONS = { Manifest.permission.CAMERA };

    public static final String[] STORAGE_PERMISSION = { Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private static boolean hasPermission(Context context, String permission) {
        return Build.VERSION.SDK_INT < M || (PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(permission));
    }

    @RequiresApi(api = M)
    public static boolean canAccessStorage(Context context) {
        return (hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) && hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    @RequiresApi(api = M)
    public static boolean canAccessCamera(Context context) {
        return hasPermission(context, Manifest.permission.CAMERA);
    }

    @RequiresApi(api = O)
    public static boolean canAccessLocation(Context context) {
        return (hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) && hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION));
    }
}
