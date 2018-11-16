package com.talmir.mickinet.helpers;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author miri
 * @since 8/24/2018
 */
public final class MixedUtils {
    public static String getFileName(@NotNull Activity activity, @NotNull Uri uri) {
//        String result = null;
//        if (uri.getScheme().equals("content")) {
//            try {
//                Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                    cursor.close();
//                }
//            } catch (NullPointerException ignored) {  }
//        }
//        if (result == null) {
//            result = uri.getPath();
//            int cut = result.lastIndexOf('/');
//            if (cut != -1)
//                result = result.substring(cut + 1);
//        }
//        return result;
        Cursor returnCursor = activity.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    public static String getRealPathFromUri(@NotNull Context context, Uri uri) {
        String path = "";
        Cursor cursor = context.getContentResolver().query(uri, new String[] { "_data" }, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex("_data");
            path = cursor.getString(idx);
            cursor.close();
        }
        return path;
    }
    
    public static String getFilePath(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            File file = new File(Objects.requireNonNull(uri.getPath()));//create path from uri
            final String[] split = file.getPath().split(":");//split the path.
            return split[1]; // return the file path
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String[] split = DocumentsContract.getDocumentId(uri).split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                    split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null)) {
                int column_index;
                if (cursor != null) {
                    column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst())
                        return cursor.getString(column_index);
                }
            } catch (Exception ignored) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
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

    public static void mimeTypeChecker() {
    
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
