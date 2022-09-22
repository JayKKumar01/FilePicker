package com.github.jay.filepicker;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    Context context;

    public FileUtil(Context context) {
        this.context = context;
    }

    public static String uriToString(Uri uri){
        String str = null;
        try {
            str = URLDecoder.decode(uri.toString(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static List<String> rootPaths(Context context){
        List<String> list = new ArrayList<>();
        File[] folders = context.getExternalCacheDirs();
        if (folders == null){
            return list;
        }
        for (File file: folders){
            String str = file.getPath();
            list.add(str.replace(str.substring(str.indexOf("Android")),""));
        }
        return list;
    }

    public static String getVideoPath(Context context,Uri uri){

        if(isExternalStorageDocument(uri)){
            return getPathFromExtSD(uri);
        }

        if(isMediaDocument(uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            if (split.length <2){
                return null;
            }
            String type = split[0];

            Uri contentUri = null;

            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            if (contentUri == null){
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            }


            String selection = "_id=?";
            String[] selectionArgs = new String[] {
                    split[1]
            };


            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
        if (isDownloadsDocument(uri)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final String id;
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, new String[] {
                            MediaStore.MediaColumns.DISPLAY_NAME
                    }, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String fileName = cursor.getString(0);
                        String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                        if (fileExists(path)) {
                            return path;
                        }
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }
            else {
                final String id = DocumentsContract.getDocumentId(uri);

                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                Uri contentUri = null;
                try {
                    contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                if (contentUri != null)
                    return getDataColumn(context, contentUri, null, null);
            }
        }

        return null;

    }


    private static String getPathFromExtSD(Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] pathData = docId.split(":");
        if (pathData.length <2){
            return null;
        }
        final String type = pathData[0];
        final String relativePath = File.separator + pathData[1];
        String fullPath = "";


//        Log.d(TAG, "MEDIA EXTSD TYPE: " + type);
//        Log.d(TAG, "Relative path: " + relativePath);
        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }
        else{
            fullPath = "/storage/"+type+relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }
        if ("home".equalsIgnoreCase(type)) {
            fullPath = "/storage/emulated/0/Documents" + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return null;
    }

    public static boolean fileExists(String filePath) {
        if (filePath == null){
            return false;
        }
        File file = new File(filePath);

        return file.exists();
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

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }
}
