package com.example.probook455.telephone;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 345;
    private Boolean shouldShowPermissionExplanation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView versionView;
        versionView = findViewById(R.id.versionTextView);
        versionView.setText(String.format("%s: %s", getResources().getString(R.string.version), BuildConfig.VERSION_NAME));
        getImei();
    }

    private void showImei()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)
            return;
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TextView imeiView = findViewById(R.id.imeiTextView);
        String imei = tm.getDeviceId();
        imeiView.setText(String.format("%s: %s", getResources().getString(R.string.imei), imei));
    }

    private void showPermissionExplanation() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.explanation);
        dialogBuilder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] { Manifest.permission.READ_PHONE_STATE },
                        PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        });
        dialogBuilder.show();
    }

    protected void getImei() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                showPermissionExplanation();
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PERMISSIONS_REQUEST_READ_PHONE_STATE);
                shouldShowPermissionExplanation = true;
            }
        }
        else {
            showImei();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImei();
                }
                else if (shouldShowPermissionExplanation) {
                    showPermissionExplanation();
                    shouldShowPermissionExplanation = false;
                }
            }
        }
    }
}
