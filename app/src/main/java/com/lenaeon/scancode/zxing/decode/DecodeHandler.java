/*
 * Copyright (C) 2010 ZXing authors
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

package com.lenaeon.scancode.zxing.decode;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.lenaeon.scancode.R;
import com.lenaeon.scancode.zxing.ScanManager;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class DecodeHandler extends Handler {

    final ScanManager scanManager;
    final MultiFormatReader multiFormatReader;
    public boolean running = true;

    public DecodeHandler(ScanManager scanManager, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.scanManager = scanManager;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case R.id.decode:
                //解码摄像头返回的图片信息
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    void decode(byte[] data, int width, int height) {
        Size size = scanManager.getCameraManager().getPreviewSize();

        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < size.height; y++) {
            for (int x = 0; x < size.width; x++)
                rotatedData[x * size.height + size.height - y - 1] = data[x + y * size.width];
        }

        // 宽高也要调整
        int tmp = size.width;
        size.width = size.height;
        size.height = tmp;

        Result rawResult = null;
        // 构造基于平面的YUV亮度源，即包含二维码区域的数据源
        PlanarYUVLuminanceSource source = buildLuminanceSource(rotatedData, size.width, size.height);
        if (source != null) {
            // 构造二值图像比特流，使用HybridBinarizer算法解析数据源
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                // 采用MultiFormatReader解析图像，可以解析多种数据格式
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        Handler handler = scanManager.getHandler();
        if (rawResult != null) {
            // Don't log the barcode contents for security.
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
                Bundle bundle = new Bundle();
                //bundleThumbnail(source, bundle);
                bundleOriginal(source, bundle);
                message.setData(bundle);

                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_failed);
                message.sendToTarget();
            }
        }

    }

    /**
     * 建立在适当的luminancesource对象工厂方法
     * <p>
     * 预览缓冲区的格式，被描述为camera.parameters。
     *
     * @param data   数据预览框。
     * @param width  宽度图像的宽度。
     * @param height 高度图像的高度。
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     * @返回 planaryuvluminancesource 实例。
     * A factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = scanManager.getCropRect();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        // 高解码效率和速度，采用了裁剪无用区域的方式
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
        // 直接返回整幅图像的数据，而不计算聚焦框大小
        //return new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
    }


    // 获取灰度缩略图
    static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        //Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());

    }


    // 获取彩色缩略图
    static void bundleOriginal(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        //Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
    }

}
