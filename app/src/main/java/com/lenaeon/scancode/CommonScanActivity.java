/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lenaeon.scancode;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.lenaeon.scancode.zxing.utils.Constant;
import com.lenaeon.scancode.zxing.ScanListener;
import com.lenaeon.scancode.zxing.ScanManager;
import com.lenaeon.scancode.zxing.decode.DecodeThread;
import com.lenaeon.scancode.zxing.decode.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * 二维码扫描使用
 *
 * @author 刘红亮  2015年4月29日  下午5:49:45
 *         代码调用顺序
 *         CaptureActivity → CaptureActivityHandler → CameraManager → PreviewCallback → DecodeHandler → CaptureActivityHandler  → CaptureActivity
 */
public final class CommonScanActivity extends Activity implements ScanListener, View.OnClickListener {
    static final String TAG = CommonScanActivity.class.getSimpleName();

    SurfaceView scanPreview = null;
    View scanContainer;
    View scanCropView;
    ImageView scanLine;
    ScanManager scanManager;
    TextView iv_light;
    TextView qrcode_g_gallery;
    TextView qrcode_ic_back;
    final int PHOTOREQUESTCODE = 1111;
    boolean lightflag;

    @Bind(R.id.service_register_rescan)
    Button rescan;
    @Bind(R.id.scan_image)
    ImageView scan_image;
    @Bind(R.id.authorize_return)
    ImageView authorize_return;
    @Bind(R.id.authorize_light)
    ImageView authorize_light;
    private int scanMode;//扫描模型（条形，二维码，全部）

    @Bind(R.id.common_title_TV_center)
    TextView title;
    @Bind(R.id.scan_hint)
    TextView scan_hint;
    @Bind(R.id.tv_scan_result)
    TextView tv_scan_result;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 设置全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scan_code);
        ButterKnife.bind(this);
        scanMode = getIntent().getIntExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
        initView();
    }

    void initView() {
        switch (scanMode) {
            case DecodeThread.BARCODE_MODE:
                title.setText(R.string.scan_barcode_title);
                scan_hint.setText(R.string.scan_barcode_hint);
                break;
            case DecodeThread.QRCODE_MODE:
                title.setText(R.string.scan_qrcode_title);
                scan_hint.setText(R.string.scan_qrcode_hint);
                break;
            case DecodeThread.ALL_MODE:
                title.setText(R.string.scan_allcode_title);
                scan_hint.setText(R.string.scan_allcode_hint);
                break;
        }

        scanContainer = findViewById(R.id.capture_container);

        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanCropView = findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        qrcode_g_gallery = (TextView) findViewById(R.id.qrcode_g_gallery);
        qrcode_g_gallery.setOnClickListener(this);
        qrcode_ic_back = (TextView) findViewById(R.id.qrcode_ic_back);
        qrcode_ic_back.setOnClickListener(this);
        iv_light = (TextView) findViewById(R.id.iv_light);
        iv_light.setOnClickListener(this);
        rescan.setOnClickListener(this);
        authorize_return.setOnClickListener(this);
        authorize_light.setOnClickListener(this);
        //构造出扫描管理器
        scanManager = new ScanManager(this, scanPreview, scanContainer, scanCropView, scanLine, scanMode, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        scanManager.onResume();
        //rescan.setVisibility(View.INVISIBLE);
        //rescan.setBackgroundColor(Color.parseColor("#828282"));

        rescan.setBackgroundResource(R.drawable.rescan_shape_button_off);
        rescan.setClickable(false);
        rescan.setText(rescan.isClickable() ? R.string.scan_continue : R.string.scan_running);

        scan_image.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanManager.onPause();
    }

    /**
     *
     */
    public void scanResult(Result rawResult, Bundle bundle) {
        //扫描成功后，扫描器不会再连续扫描，如需连续扫描，调用reScan()方法。
        //scanManager.reScan();
//		Toast.makeText(that, "result="+rawResult.getText(), Toast.LENGTH_LONG).show();

        if (!scanManager.isScanning() && bundle.getInt("type") == R.id.decode_camera) {
            // 如果当前不是在扫描状态
            // 设置再次扫描按钮出现
            rescan.setBackgroundResource(R.drawable.rescan_shape_button);
            rescan.setClickable(true);
            rescan.setText(rescan.isClickable() ? R.string.scan_continue : R.string.scan_running);

            Bitmap barcode = null;
            byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
            if (compressedBitmap != null) {
                barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
            }

            scan_image.setImageBitmap(barcode);
            scan_image.setVisibility(View.VISIBLE);
        }
        tv_scan_result.setVisibility(View.VISIBLE);
        tv_scan_result.setText(rawResult.getText());
    }

    @Override
    public void scanError(Exception e) {
        //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        //rescan.setVisibility(View.VISIBLE);

        tv_scan_result.setVisibility(View.VISIBLE);
        tv_scan_result.setText(e.getMessage());
        //相机扫描出错时
        if (e.getMessage() != null && e.getMessage().startsWith("相机")) {
            scanPreview.setVisibility(View.INVISIBLE);
        }
    }

    void startScan() {
        if (rescan.isClickable()) {
            scan_image.setVisibility(View.GONE);
            rescan.setBackgroundResource(R.drawable.rescan_shape_button_off);
            rescan.setClickable(false);
            rescan.setText(rescan.isClickable() ? R.string.scan_continue : R.string.scan_running);
            scanManager.reScan();
        }
    }

    public void showPictures(int requestCode) {
        //显示相册选择对话框
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_PICK);
        }
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String photo_path;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTOREQUESTCODE:
                    Uri uri = data.getData();
                    if (!TextUtils.isEmpty(uri.getAuthority())) {
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
                        if (null == cursor) {
                            Toast.makeText(this, "无法找到选择的图片", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        cursor.moveToFirst();
                        int colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        photo_path = cursor.getString(colum_index);
                        if (photo_path == null) {
                            photo_path = Utils.getPath(getApplicationContext(), uri);
                        }
                        cursor.close();
                    } else {
                        photo_path = data.getData().getPath();
                    }
                    // 识别相册图片
                    scanManager.scanningImage(photo_path);

                    //显示相册图片
                    //Bitmap bitmap = BitmapFactory.decodeFile(photo_path);
                    //scan_image.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.qrcode_g_gallery:
                showPictures(PHOTOREQUESTCODE);
                break;
            case R.id.iv_light:
                scanManager.beepManager.playBeepSoundAndVibrate();
                break;
            case R.id.authorize_light:
                lightflag = !lightflag;
                int imageSource = (lightflag ? R.drawable.flash_on : R.drawable.flash_off);
                authorize_light.setImageResource(imageSource);
                scanManager.switchLight();
                break;
            case R.id.qrcode_ic_back:
                finish();
                break;
            case R.id.service_register_rescan://再次开启扫描
                startScan();
                break;
            case R.id.authorize_return:
                finish();
                break;
            default:
                break;
        }
    }

/*    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();

        if (action ==KeyEvent.KEYCODE_VOLUME_DOWN) {
            tv_scan_result.setVisibility(View.VISIBLE);
            tv_scan_result.setText("+++++++++ACTION_DOWN++++++"+ count++);
            return true;
        }

        if (action== KeyEvent.KEYCODE_VOLUME_UP) {
            tv_scan_result.setVisibility(View.VISIBLE);
            tv_scan_result.setText("+++++ACTION_UP++++++++++");
            return true;
        }

        return super.dispatchKeyEvent(event);
    }*/

/*    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                startScan();
                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
                scanManager.switchLight();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/
}