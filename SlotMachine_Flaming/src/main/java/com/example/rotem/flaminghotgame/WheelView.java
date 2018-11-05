package com.example.rotem.flaminghotgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Rotem on 04/04/2016.
 */
public class WheelView extends View
{
    private int right = 0, top = 0, width = 0, height = 0;
    private int imageWidth = 10, imageHeight = 20;
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private Bitmap[] bitmap, resizedBitmap;
    private BitmapFactory.Options options;
    private Sprite[] sprite;
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    public WheelView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Initialize all the variables
     */
    private void init(Context context, AttributeSet attrs, int defStyle)
    {

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        for(int i=0; i< MyStatic.NUMBER_OF_FRUITS_IN_ONE_WHEEL; i++){
            if(sprite != null) {
                sprite[i].onDraw(canvas);
            }
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        right = getPaddingRight();
        top = getPaddingTop();
        width = w - (getPaddingLeft() + getPaddingRight());
        height = h - (getPaddingTop() + getPaddingBottom());

        imageWidth = width;
        imageHeight = height/4;
    }
}
