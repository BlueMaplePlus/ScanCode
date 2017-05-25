package com.lenaeon.scancode.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.google.zxing.ResultPoint;

import java.util.Collection;


/**
 * 作者：王敏 on 2015/8/21 17:31
 * 类说明：画出扫描框的四个脚的脚边框，也可以直接用一张图片代替
 */
public class MyImageView extends ImageView {

    private Context context;

/*    private static final int OPAQUE = 0xFF;
    private int resultPointColor;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;*/

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

/*Resources resources = getResources();
        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new HashSet<ResultPoint>(5);*/
    }

    public MyImageView(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 106, 98));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(t(5));

        canvas.drawLine(0, 0, 0, t(18), paint);
        canvas.drawLine(0, 0, t(18), 0, paint);

        canvas.drawLine(0, height - t(18), 0, height, paint);
        canvas.drawLine(0, height, t(18), height, paint);

        canvas.drawLine(width - t(18), 0, width, 0, paint);
        canvas.drawLine(width, 0, width, t(18), paint);

        canvas.drawLine(width, height - t(18), width, height, paint);
        canvas.drawLine(width - t(18), height, width, height, paint);

        Rect frame = new Rect(0, 0, width, height);

      /*  Collection<ResultPoint> currentPossible = possibleResultPoints;
        Collection<ResultPoint> currentLast = lastPossibleResultPoints;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new HashSet<ResultPoint>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(OPAQUE);
            paint.setColor(resultPointColor);
            for (ResultPoint point : currentPossible) {
                canvas.drawCircle(frame.left + point.getX(), frame.top
                        + point.getY(), 6.0f, paint);
            }
        }
        if (currentLast != null) {
            paint.setAlpha(OPAQUE / 2);
            paint.setColor(resultPointColor);
            for (ResultPoint point : currentLast) {
                canvas.drawCircle(frame.left + point.getX(), frame.top
                        + point.getY(), 3.0f, paint);
            }
        }*/

    }

    public int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public int t(float dpVal) {
        return dp2px(dpVal);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //setMeasuredDimension(t(248),t(248));
    }

/*    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
        //this.setBackgroundResource(R.drawable.rescan_shape_button);
        this.refreshDrawableState();
    }*/
}
