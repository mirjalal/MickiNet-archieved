package com.talmir.mickinet.helpers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author miri
 * @since 8/24/2018
 */
public final class MixedUtils {
    public static String getFileName(@NotNull Activity activity, @NotNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try {
                Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
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

    public static String getRealPathFromUri(@NotNull Context context, Uri uri) {
        String path = "";
        if (context.getContentResolver() != null) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    public static void copyFileToDir(Context c, Uri src, File dst) throws IOException {
        try (InputStream in = c.getContentResolver().openInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                assert in != null;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
            }
        }
    }

    public static void createNestedFolders() {
        File rootDir = new File("/storage/emulated/0/MickiNet/");
        rootDir.mkdirs();

        File p = new File(rootDir, "/Photos/");
        p.mkdirs();
        new File(p, "/Sent/").mkdirs();
        new File(p, "/Received/").mkdirs();

        File v = new File(rootDir, "/Videos/");
        v.mkdirs();
        new File(v, "/Sent/").mkdirs();
        new File(v, "/Received/").mkdirs();

        File m = new File(rootDir, "/Media/");
        m.mkdirs();
        new File(m, "/Sent/").mkdirs();
        new File(m, "/Received/").mkdirs();

        File a = new File(rootDir, "/APKs/");
        a.mkdirs();
        new File(a, "/Sent/").mkdirs();
        new File(a, "/Received/").mkdirs();

        File f = new File(rootDir, "/Others/");
        f.mkdirs();
        new File(f, "/Sent/").mkdirs();
        new File(f, "/Received/").mkdirs();
    }

    public static String getInnerFolder(String mimeType) {
        String inner="";
        return inner;
    }
}
