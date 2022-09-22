package com.github.jay.filepicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    FilePicker filePicker;
    ActivityResultLauncher<Intent> registerStoragePermission = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // take permission first
        Permission.takeStoragePermission(this,registerStoragePermission);


        filePicker = new FilePicker(this);
        filePicker.setTitle("Choose Video");
        filePicker.setExtensions(new String[]{".mp4",".mkv"});
        filePicker.setOnclickListener(new FilePicker.SelectListener() {
            @Override
            public void onClicked(File file) {
                Toast.makeText(MainActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
            }
        });
        filePicker.setOnCancelListener(new FilePicker.CancelListener() {
            @Override
            public void onCancelled() {
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void show(View view) {
        if (!Permission.canWriteStorage(this)){
            Permission.takeStoragePermission(this,registerStoragePermission);
            return;
        }
        filePicker.show();
    }
}