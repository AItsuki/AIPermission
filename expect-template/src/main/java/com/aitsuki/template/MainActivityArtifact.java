package com.aitsuki.template;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.aitsuki.aipermission.annotation.RequiresPermission;
import com.aitsuki.aipermission.annotation.ScanPermission;
import com.aitsuki.aipermission.annotation.UseStrategy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@ScanPermission
public class MainActivityArtifact extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_open_camera).setOnClickListener(v -> openCameraWithPermissions());
        findViewById(R.id.bt_get_location).setOnClickListener(v -> getLocationWithPermissions());
    }

    @RequiresPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void openCamera() {
        Toast.makeText(this, R.string.open_camera, Toast.LENGTH_SHORT).show();
    }

    @UseStrategy(Object.class)
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void getLocation() {
        Toast.makeText(this, R.string.get_location, Toast.LENGTH_SHORT).show();
    }

    void openCameraWithPermissions() {
//        MainActivityAIPermission.openCamera(this);
    }

    void getLocationWithPermissions() {
//        MainActivityAIPermission.getLocation(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        MainActivityAIPermission.handlePermissionResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        MainActivityAIPermission.handleActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
