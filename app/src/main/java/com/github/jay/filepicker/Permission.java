package com.github.jay.filepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permission extends AppCompatActivity {
    Context context;
    Activity activity;
    ActivityResultLauncher<Intent> register;
    public Permission(Context context, ActivityResultLauncher<Intent> register) {
        this.context = context;
        activity = (Activity) context;
        this.register = register;
    }

    public static boolean canWriteStorage(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }
        else {
            return  (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        }
    }

    public static void takeStoragePermission(Context context,ActivityResultLauncher<Intent> register){
        Permission obj = new Permission(context,register);
        if (canWriteStorage(context)){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s", context.getPackageName())));
            register.launch(intent);
        }
        else {
            ActivityCompat.requestPermissions(obj.activity,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE

                    },101);
        }
    }




    // write settings permission

    public static boolean canWriteSettings(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(context);
        }
        return true;
    }

    public static void getWriteSettingPermission(Context context){
        Activity activity = (Activity) context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!canWriteSettings(context)){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:"+context.getPackageName()));
                activity.startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length >0){
            if (requestCode == 101){
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    takeStoragePermission(context,register);
                }
            }
        }
    }
}
