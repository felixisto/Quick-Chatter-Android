package com.office.quickchatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.simple.SimpleFileSystem;
import com.office.quickchatter.navigation.PrimaryRouter;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    public final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    public final int PERMISSION_REQUEST_BLUETOOTH = 3;

    private Router _router;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tryAppStart();
    }

    @Override
    public void onBackPressed() {
        _router.navigateBack();
    }

    private void tryAppStart() {
        requestForReadPermission();
    }

    private void onAppReadyToStart() {
        _router = new PrimaryRouter(this);
    }

    // # Permissions

    private boolean isReadPermissionGranted() {
        if (Build.VERSION.SDK_INT < 16) {
            return true;
        }

        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isReadPermissionPermanentlyDenied() {
        if (Build.VERSION.SDK_INT < 16) {
            return false;
        }

        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED;
    }

    private void requestForReadPermission() {
        if (Build.VERSION.SDK_INT >= 16) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private boolean isWritePermissionGranted() {
        if (Build.VERSION.SDK_INT < 16) {
            return true;
        }

        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isWritePermissionPermanentlyDenied() {
        if (Build.VERSION.SDK_INT < 16) {
            return false;
        }

        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED;
    }

    private void requestForWritePermission() {
        if (Build.VERSION.SDK_INT >= 16) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private boolean isBluetoothPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isBluetoothPermissionPermanentlyDenied() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED;
    }

    private void requestForBluetoothPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_BLUETOOTH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (!isReadPermissionGranted()) {
                Toast toast = Toast.makeText(this, R.string.err_permissions_storage_fail, Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            // When read permission is granted, request for write permission up next
            if (!isWritePermissionGranted()) {
                requestForWritePermission();
                return;
            }

            // When write permission is granted, request bluetooth permission up next
            if (!isBluetoothPermissionGranted()) {
                requestForBluetoothPermission();
                return;
            }

            onAppReadyToStart();

            return;
        }

        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (!isWritePermissionGranted()) {
                Toast toast = Toast.makeText(this, R.string.err_permissions_storage_fail, Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            // When write permission is granted, request for bluetooth up next
            if (!isBluetoothPermissionGranted()) {
                requestForBluetoothPermission();
                return;
            }

            onAppReadyToStart();

            return;
        }

        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            if (isBluetoothPermissionGranted()) {
                onAppReadyToStart();
            } else {
                Toast toast = Toast.makeText(this, R.string.err_permissions_bluetooth_fail, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}
