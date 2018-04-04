package com.rayhahah.qrcodedemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.rayhahah.qrcodedemo.zxing.app.CaptureActivity;

public class MainActivity extends AppCompatActivity implements PermissionManager.PermissionsResultListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionManager.requestPermission(this, "请求摄像头权限", 1, this, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
    }

    private static final int REQUEST_QRCODE = 0x01;

    public void onQRCodeClick(View view) {
        //启动二维码扫描的页面功能
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_QRCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_QRCODE) {
            switch (resultCode) {
                case CaptureActivity.RESULT_CODE_DECODE:
                case Activity.RESULT_OK:
                    String codeData = data.getStringExtra(CaptureActivity.EXTRA_DATA);
                    Toast.makeText(this, codeData, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted(int requestCode) {
        Toast.makeText(this, "申请权限成功", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPermissionDenied(int requestCode) {
        Toast.makeText(this, "申请权限失败", Toast.LENGTH_SHORT).show();

    }
}
