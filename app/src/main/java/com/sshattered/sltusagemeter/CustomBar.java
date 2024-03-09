package com.sshattered.sltusagemeter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class CustomBar extends View {

    private final Paint paint = new Paint();;
    private RectF rectF = new RectF();
    private int mainColor = 0;

    private float _total = 100, _day = 40, _night = 60;
    private float _package = 100;


    public CustomBar(Context context) {
        super(context);
        init(context);
    }

    public CustomBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CustomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        /*TypedArray typedArray = context.obtainStyledAttributes(R.styleable.CustomBar);
        try{
            //String title = typedArray.getString(R.styleable.CustomBar_mainTitle);
            mainColor = Color.parseColor(typedArray.getString(R.styleable.CustomBar_commonColor));
        }finally {
            typedArray.recycle();
        }*/
    }

    /**
     *
     * @param total
     * @param day
     * @param night
     */
    public void setVolumes(float total, float day, float night){
        _total = total;
        _day = day;
        _night = night;
    }

    /**
     * This function sets the total size of the package
     * @param pack
     */
    public void setPackage(float pack){
        _package = pack;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        paint.setColor(Color.parseColor("#212121"));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setDither(true);
        canvas.drawPaint(paint);

        float perc = (getHeight() - 100) / _package;
        float x1 = (float) getWidth() / 7;
        float x2 = 2 * (float) getWidth() / 7;
        float height = getHeight() - perc * _total;
        paint.setColor(Color.parseColor("#AD1457"));
        rectF.set(x1,  height, x2, getHeight());
        canvas.drawRect(rectF, paint);

        String tmp = String.format(Locale.ENGLISH,"%.1fGB", _total);
        paint.setColor(Color.WHITE);
        paint.setTextSize(30.0f);
        float len = paint.measureText(tmp);
        canvas.drawText(tmp, (x1+x2)/2 - len/2, height, paint);
        tmp = "Total";
        len = paint.measureText(tmp);
        canvas.drawText(tmp, (x1+x2)/2 - len/2, getHeight()-5, paint);

        x1 = 3 * (float) getWidth() / 7;
        x2 = 4 * (float) getWidth() / 7;
        height = getHeight() - perc * _day;
        paint.setColor(Color.parseColor("#1565C0"));
        rectF.set(x1, height, x2, getHeight());
        canvas.drawRect(rectF, paint);

        tmp = String.format(Locale.ENGLISH,"%.1fGB", _day);
        paint.setColor(Color.WHITE);
        paint.setTextSize(30.0f);
        len = paint.measureText(tmp);
        canvas.drawText(tmp, (x1+x2)/2 - len/2, height, paint);
        tmp = "Day";
        len = paint.measureText(tmp);
        canvas.drawText(tmp, (x1+x2)/2 - len/2, getHeight()-5, paint);

        x1 = 5 * (float) getWidth() / 7;
        x2 = 6 * (float) getWidth() / 7;
        height = getHeight() - perc * _night;
        paint.setColor(Color.parseColor("#D84315"));
        rectF.set(x1, height, x2, getHeight());
        canvas.drawRect(rectF, paint);

        tmp = String.format(Locale.ENGLISH,"%.1fGB", _night);
        paint.setColor(Color.WHITE);
        paint.setTextSize(30.0f);
        len = paint.measureText(tmp);
        canvas.drawText(tmp, (x1+x2)/2 - len/2, height, paint);
        tmp = "Night";
        len = paint.measureText(tmp);
        canvas.drawText(tmp, (x1+x2)/2 - len/2, getHeight()-5, paint);

        tmp = String.format(Locale.ENGLISH, "Package - %dGB", (int)_package);
        canvas.drawText(tmp, 0, 30, paint);

    }
}
