package com.lenaeon.scancode.zxing.view;

import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

/**
 * Created by Administrator on 2017/05/26.
 */

public class MyResultPointCallback implements ResultPointCallback {

    private final MyImageView myImageView;

    public MyResultPointCallback(MyImageView myImageView) {
        this.myImageView = myImageView;
    }

    public void foundPossibleResultPoint(ResultPoint point) {
        //myImageView.addPossibleResultPoint(point);
    }
}
