package com.example.rotem.flaminghotgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Rotem on 23/03/2016.
 */
public class Sprite
{
    private int x=0 , y=0 , dx=0 , dy=0 , width=0 , height=0;
    private Bitmap bitmap;

    /**
     * constructor
     */
    public Sprite(int x, int y, int dx, int dy, int width, int height, Bitmap bitmap)
    {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.width = width;
        this.height = height;
        this.bitmap = bitmap;
    }

    /**
     * update the sprite
     * in this app the update is only on the y axis.
     */
    public void update()
    {
        y += height;

        if(y > height * MyStatic.WHEEL_VALUES_ARRAY[(MyStatic.NUMBER_OF_FRUITS_IN_ONE_WHEEL-1)]){
            y = height * MyStatic.WHEEL_VALUES_ARRAY[0] ;
        }
    }

    /**
     * onDraw paint the Image on the canvas
     * @param canvas
     */
    public void onDraw(Canvas canvas)
    {
        if(MainActivity.running)
            update();

        if(bitmap != null)
            canvas.drawBitmap(bitmap, x, y, null);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
