package com.example.rotem.flaminghotgame;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Created by Rotem on 16/04/2016.
 */
public class WinningView extends View
{
    private int right = 0, top = 0, width = 0, height = 0, lineStroke1 = 0, lineStroke2 = 1,lineStroke3 = 2, stroke1 = 1, stroke2 = 1, stroke3 = 1, STROKE_1 = 6, STROKE_2 = 8, STROKE_3 = 10;

    private MyWinningLine[] myWinLines;
    private Point[] strokePairs;
    private int[] colors;
    private Paint paint;
    private Random rand;

    public WinningView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public WinningView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public WinningView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Initialize all the variables
     */
    private void init(Context context, AttributeSet attrs, int defStyle)
    {
        rand = new Random();
        paint = new Paint();

        strokePairs = new Point[MyStatic.BOARD_ROWS];

        strokePairs[0] = new Point(1,11);
        strokePairs[1] = new Point(2,9);
        strokePairs[2] = new Point(3,5);
        strokePairs[3] = new Point(4,3);

        colors = new int[MyStatic.BOARD_ROWS];

        colors[0] = Color.BLACK;
        colors[1] = Color.WHITE;
        colors[2] = Color.YELLOW;
        colors[3] = Color.MAGENTA;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if(MainActivity.isWinView && MainActivity.isWinningRunning)
        {
            lineStroke1 += stroke1;
            lineStroke2 += stroke2;
            lineStroke3 += stroke3;


            if(!( lineStroke1 >= 0 && lineStroke1 <= STROKE_1)) {
                stroke1 *= -1;
            }

            if(!( lineStroke2 >= 0 && lineStroke2 <= STROKE_2)) {
                stroke2 *= -1;
            }

            if(!( lineStroke3 >= 0 && lineStroke3 <= STROKE_3) ){
                stroke3 *= -1;
            }

            for(int i=0; i<myWinLines.length; i++)
            {
                if(myWinLines[i].isVisible())
                {
                    drawWinFrame(i, Color.WHITE, lineStroke3, canvas);
                    drawWinFrame(i, Color.YELLOW, lineStroke2, canvas);

                    if(myWinLines[i].type == MyStatic.FIVE_IN_ROW){
                        drawWinFrame(i, Color.MAGENTA, lineStroke1, canvas);
                    }
                    else if(myWinLines[i].type == MyStatic.FOUR_IN_ROW){
                        drawWinFrame(i, Color.GREEN, lineStroke1, canvas);
                    }
                    else if(myWinLines[i].type == MyStatic.THREE_IN_ROW){
                        drawWinFrame(i, Color.WHITE, lineStroke1, canvas);
                    }
                }
            }
        }
    }

    public void drawWinFrame(int row, int color, int stroke, Canvas canvas){

        paint.setColor(color);
        paint.setStrokeWidth(stroke);

        int tempStroke = stroke/2;

        canvas.drawLine(myWinLines[row].right + tempStroke, myWinLines[row].top, myWinLines[row].right + myWinLines[row].width - tempStroke, myWinLines[row].top, paint);
        canvas.drawLine(myWinLines[row].right + tempStroke, myWinLines[row].top + myWinLines[row].height, myWinLines[row].right + myWinLines[row].width - tempStroke , myWinLines[row].top + myWinLines[row].height, paint);
        canvas.drawLine(myWinLines[row].right, myWinLines[row].top - tempStroke, myWinLines[row].right, myWinLines[row].top + myWinLines[row].height + tempStroke , paint);
        canvas.drawLine(myWinLines[row].right + myWinLines[row].width, myWinLines[row].top - tempStroke, myWinLines[row].right + myWinLines[row].width, myWinLines[row].top + myWinLines[row].height + tempStroke, paint);

    }

    public void setFalseVisible(){

        if(myWinLines != null){
            for(int i=0; i< myWinLines.length; i++) {
                myWinLines[i].setVisible(false);
            }
            lineStroke1 = 0;
            lineStroke2 = 0;
            lineStroke3 = 0;
        }

    }

    public void setRowAndType(int row, int winType)
    {
        for(int i=0; i< myWinLines.length; i++){
            if(myWinLines[i].row == row && myWinLines[i].type== winType){
                myWinLines[i].setVisible(true);
            }
        }
    }

    public void initializeWinningObjectsArray()
    {
        myWinLines = new MyWinningLine[12];

        myWinLines[0] = new MyWinningLine( right, top, (width - (width/5)*2) , height, MyStatic.FIRST_ROW,  MyStatic.THREE_IN_ROW, Color.YELLOW, false);
        myWinLines[1] = new MyWinningLine( right, top, (width - width/5), height, MyStatic.FIRST_ROW, MyStatic.FOUR_IN_ROW, Color.YELLOW, false);
        myWinLines[2] = new MyWinningLine( right, top, width, height, MyStatic.FIRST_ROW, MyStatic.FIVE_IN_ROW, Color.YELLOW, false);

        myWinLines[3] = new MyWinningLine( right, top+height, (width - (width/5)*2) , height, MyStatic.SECOND_ROW,  MyStatic.THREE_IN_ROW, Color.YELLOW, false);
        myWinLines[4] = new MyWinningLine( right, top+height, (width - width/5), height, MyStatic.SECOND_ROW, MyStatic.FOUR_IN_ROW, Color.YELLOW, false);
        myWinLines[5] = new MyWinningLine( right, top+height, width, height, MyStatic.SECOND_ROW, MyStatic.FIVE_IN_ROW, Color.YELLOW, false);

        myWinLines[6] = new MyWinningLine( right, top+height*2, (width - (width/5)*2), height, MyStatic.THIRD_ROW,  MyStatic.THREE_IN_ROW, Color.YELLOW, false);
        myWinLines[7] = new MyWinningLine( right, top+height*2, (width - width/5), height, MyStatic.THIRD_ROW, MyStatic.FOUR_IN_ROW, Color.YELLOW, false);
        myWinLines[8] = new MyWinningLine( right, top+height*2, width, height, MyStatic.THIRD_ROW, MyStatic.FIVE_IN_ROW, Color.YELLOW, false);

        myWinLines[9]  = new MyWinningLine( right, top+height*3, (width - (width/5)*2), height, MyStatic.FOURTH_ROW,  MyStatic.THREE_IN_ROW, Color.YELLOW, false);
        myWinLines[10] = new MyWinningLine( right, top+height*3, (width - width/5), height, MyStatic.FOURTH_ROW, MyStatic.FOUR_IN_ROW, Color.YELLOW, false);
        myWinLines[11] = new MyWinningLine( right, top+height*3, width, height, MyStatic.FOURTH_ROW, MyStatic.FIVE_IN_ROW, Color.YELLOW, false);
    }

    /**
     * returns an integer that represent a random color
     *
     * @return Color.rgb(R, G, B);
     */
    private int MyRandomColor() {

        int R = rand.nextInt(256);
        int G = rand.nextInt(256);
        int B = rand.nextInt(256);

        return Color.rgb(R, G, B);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        right = getPaddingRight();
        top = getPaddingTop();
        width = w - (getPaddingLeft() + getPaddingRight());
        height = h - (getPaddingTop() + getPaddingBottom());

        height = height / 4;

        initializeWinningObjectsArray();
    }

    /**
     * private class that represent the winning views array
     */
    private class MyWinningLine
    {
        private int top = 0, right = 0, width = 0, height = 0, row, type = 0, color = 0;
        private  boolean visible = false;

        public MyWinningLine(int right, int top, int width, int height,int row, int type, int color, boolean visible) {
            this.right = right;
            this.top = top;
            this.width = width;
            this.type = type;
            this.row = row;
            this.height = height;
            this.color = color;
            this.visible = visible;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

    }

}