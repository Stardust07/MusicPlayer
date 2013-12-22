package com.hustunique.musicplayer;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Lrc extends TextView{
	private float width=480;        //�����ͼ���  
    private float height=240;       //�����ͼ�߶�  
    private Paint paint;
    private Paint paintHL;  
    private float textHeight = 40; 
    public static int index=-1;    
    public static ArrayList<Long> time = PlayActivity.time;
    public static HashMap<Long, String> lrcMap = PlayActivity.lrcMap;
    
	public Lrc(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public Lrc(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        init();  
    }  
	
	private void init() {  
        setFocusable(true);     //���ÿɶԽ�  
        //��������  
        paintHL = new Paint();  
        paintHL.setAntiAlias(true);    //���ÿ���ݣ����������۱���  
        paintHL.setTextAlign(Paint.Align.CENTER);
          
        //�Ǹ�������  
        paint = new Paint();  
        paint.setAntiAlias(true);  
        paint.setTextAlign(Paint.Align.CENTER);
    }  
	
	@Override  
    protected void onDraw(Canvas canvas) {  
        if(canvas == null) {  
            return;  
        }  
          
        paintHL.setColor(Color.RED);  
        paint.setColor(Color.BLACK);  
          
        paintHL.setTextSize(24);  
        paintHL.setTypeface(Typeface.SERIF);  
          
        paint.setTextSize(20);  
        paint.setTypeface(Typeface.DEFAULT);  
        try {  
            setText("");  
            canvas.drawText(lrcMap.get(time.get(index)), width / 2, height / 2, paintHL);  
              
            float tempY = height / 2; 
            //��������֮ǰ�ľ���  
            for(int i = index - 1; i >= 0; i--) {  
                //��������  
                tempY = tempY - textHeight;  
                canvas.drawText(lrcMap.get(time.get(i)), width / 2, tempY, paint);  
            }  
            tempY = height / 2;  
            //��������֮��ľ���  
            for(int i = index + 1; i < lrcMap.size()-1; i++) {  
                //��������  
                tempY = tempY + textHeight;  
                canvas.drawText(lrcMap.get(time.get(i)), width / 2, tempY, paint);  
            }   
        } catch (Exception e) {  
        	canvas.drawText("δ�ҵ����", width / 2, height / 2, paintHL);  
        }  
        super.onDraw(canvas);
    }
	
	public static void setIndex(long process) {
		if(process > time.get(index)){
			index++;
		}
	}
}
