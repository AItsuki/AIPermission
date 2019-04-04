package com.aitsuki.aipermission.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aitsuki.aipermission.annotation.RequirePermissions;
import com.aitsuki.aipermission.annotation.ScanActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

@ScanActivity
public class MainActivity extends AppCompatActivity {

    private TextView tv_location;
    private Button bt_get_location;

    private LocationManager locationManager;
    private boolean startLocation;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                tv_location.setText(location.toString());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_location = findViewById(R.id.tv_location);
        bt_get_location = findViewById(R.id.bt_get_location);
        // test fragment require permission
        String def = getString(R.string.app_name);
        findViewById(R.id.bt_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("camera");
                if (fragment == null) {
                    fragment = new CameraFragment();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_container, fragment, "camera")
                        .addToBackStack(null)
                        .commit();
            }
        });

//         test activity require permission
        bt_get_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startLocation) {
                    stopLocation();
                } else {
                    startLocation();
                }
            }
        });
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    @RequirePermissions(permissions = {Manifest.permission.ACCESS_FINE_LOCATION},
            rationaleId = R.string.location_permission_rationale)
    void startLocation() {
        startLocation = true;
        tv_location.setText("正在获取GPS……");
        bt_get_location.setText("停止定位");
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            locationListener.onLocationChanged(lastKnownLocation);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    void stopLocation() {
        startLocation = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        locationManager.removeUpdates(locationListener);
        tv_location.setText("");
        bt_get_location.setText("获取位置");
    }
}
