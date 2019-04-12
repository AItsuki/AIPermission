package com.aitsuki.template;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import com.aitsuki.aipermission.annotation.RequiresPermission;
import com.aitsuki.aipermission.annotation.ScanPermission;
import com.aitsuki.aipermission.annotation.UseStrategy;
import com.aitsuki.template.strategy.LocationStrategy;

import androidx.appcompat.app.AppCompatActivity;

@ScanPermission
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_open_camera).setOnClickListener(v -> openCamera());
        findViewById(R.id.bt_get_location).setOnClickListener(v -> getLocation());
    }

    @RequiresPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void openCamera() {
        Toast.makeText(this, R.string.open_camera, Toast.LENGTH_SHORT).show();
    }

    @UseStrategy(LocationStrategy.class)
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void getLocation() {
        Toast.makeText(this, R.string.get_location, Toast.LENGTH_SHORT).show();
    }


}
