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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
        scanManager = new ScanManager(this, scanPreview, scanContainer, scanCropView,scanLine, scanMode, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        scanManager.onResume();
        //rescan.setVisibility(View.INVISIBLE);
        //rescan.setBackgroundColor(Color.parseColor("#828282"));
        rescan.setBackgroundResource(R.drawable.rescan_shape_button_off);
        rescan.setClickable(false);

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

        if (!scanManager.isScanning()) { //如果当前不是在扫描状态
            //设置再次扫描按钮出现
            //rescan.setVisibility(View.VISIBLE);
            scan_image.setVisibility(View.VISIBLE);

            Bitmap barcode = null;
            byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
            if (compressedBitmap != null) {
                barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
            }

            scan_image.setImageBitmap(barcode);
            saveBmp(barcode);
            saveBitmap(barcode);
        }
        //rescan.setVisibility(View.VISIBLE);
        rescan.setBackgroundResource(R.drawable.rescan_shape_button);
        rescan.setClickable(true);

        scan_image.setVisibility(View.VISIBLE);
        tv_scan_result.setVisibility(View.VISIBLE);
        tv_scan_result.setText(rawResult.getText());
    }

    void startScan() {
//        if (rescan.getVisibility() == View.VISIBLE) {
//            rescan.setVisibility(View.INVISIBLE);
//            scan_image.setVisibility(View.GONE);
//            scanManager.reScan();
//        }
        rescan.setBackgroundResource(R.drawable.rescan_shape_button_off);
        rescan.setClickable(false);

        scan_image.setVisibility(View.GONE);
        scanManager.reScan();
    }

    @Override
    public void scanError(Exception e) {
        //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        //rescan.setVisibility(View.VISIBLE);
        rescan.setBackgroundResource(R.drawable.rescan_shape_button);
        rescan.setClickable(true);

        scan_image.setVisibility(View.VISIBLE);
        tv_scan_result.setVisibility(View.VISIBLE);
        tv_scan_result.setText(e.getMessage());
        //相机扫描出错时
        if (e.getMessage() != null && e.getMessage().startsWith("相机")) {
            scanPreview.setVisibility(View.INVISIBLE);
        }
    }

    public void showPictures(int requestCode) {

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_PICK);
        }

        //Intent intent = new Intent(Intent.ACTION_PICK);
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
                    scanManager.scanningImage(photo_path);
                    //显示相册图片
                    Bitmap bitmap = BitmapFactory.decodeFile(photo_path);
                    scan_image.setImageBitmap(bitmap);

//                    String[] proj = {MediaStore.Images.Media.DATA};
//                    Cursor cursor = this.getContentResolver().query(data.getData(), proj, null, null, null);
//                    if (cursor.moveToFirst()) {
//                        int colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                        photo_path = cursor.getString(colum_index);
//                        if (photo_path == null) {
//                            photo_path = Utils.getPath(getApplicationContext(), data.getData());
//                        }
//                        scanManager.scanningImage(photo_path);
//                    }
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
            case R.id.authorize_light:
                if (!lightflag) {
                    lightflag = true;
                    authorize_light.setImageResource(R.drawable.flash_on);
                } else {
                    lightflag = false;
                    authorize_light.setImageResource(R.drawable.flash_off);
                }
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

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        int action = event.getAction();
//
//        if (action ==KeyEvent.KEYCODE_VOLUME_DOWN) {
//            tv_scan_result.setVisibility(View.VISIBLE);
//            tv_scan_result.setText("+++++++++ACTION_DOWN++++++"+ count++);
//            return true;
//        }
//
//        if (action== KeyEvent.KEYCODE_VOLUME_UP) {
//            tv_scan_result.setVisibility(View.VISIBLE);
//            tv_scan_result.setText("+++++ACTION_UP++++++++++");
//            return true;
//        }
//
//        return super.dispatchKeyEvent(event);
//    }

    @Override
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
    }

    /**
     * 将Bitmap存为 .bmp格式图片
     * @param bitmap
     */
    private void saveBmp(Bitmap bitmap) {
        if (bitmap == null)
            return;
        // 位图大小
        int nBmpWidth = bitmap.getWidth();
        int nBmpHeight = bitmap.getHeight();
        // 图像数据大小
        int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);
        try {
            // 存储文件名
            String filename = "/sdcard/test.bmp";
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileos = new FileOutputStream(filename);
            // bmp文件头
            int bfType = 0x4d42;
            long bfSize = 14 + 40 + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            long bfOffBits = 14 + 40;
            // 保存bmp文件头
            writeWord(fileos, bfType);
            writeDword(fileos, bfSize);
            writeWord(fileos, bfReserved1);
            writeWord(fileos, bfReserved2);
            writeDword(fileos, bfOffBits);
            // bmp信息头
            long biSize = 40L;
            long biWidth = nBmpWidth;
            long biHeight = nBmpHeight;
            int biPlanes = 1;
            int biBitCount = 24;
            long biCompression = 0L;
            long biSizeImage = 0L;
            long biXpelsPerMeter = 0L;
            long biYPelsPerMeter = 0L;
            long biClrUsed = 0L;
            long biClrImportant = 0L;
            // 保存bmp信息头
            writeDword(fileos, biSize);
            writeLong(fileos, biWidth);
            writeLong(fileos, biHeight);
            writeWord(fileos, biPlanes);
            writeWord(fileos, biBitCount);
            writeDword(fileos, biCompression);
            writeDword(fileos, biSizeImage);
            writeLong(fileos, biXpelsPerMeter);
            writeLong(fileos, biYPelsPerMeter);
            writeDword(fileos, biClrUsed);
            writeDword(fileos, biClrImportant);
            // 像素扫描
            byte bmpData[] = new byte[bufferSize];
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3) {
                    int clr = bitmap.getPixel(wRow, nCol);
                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
                }

            fileos.write(bmpData);
            fileos.flush();
            fileos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeWord(FileOutputStream stream, int value) throws IOException {
        byte[] b = new byte[2];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        stream.write(b);
    }

    protected void writeDword(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    protected void writeLong(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    public void saveBitmap(Bitmap bitmap) {
        Log.e(TAG, "保存图片");
        File f = new File("/sdcard/", "test1.bmp");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}