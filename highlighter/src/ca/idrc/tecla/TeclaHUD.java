package ca.idrc.tecla;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class TeclaHUD extends View {

	protected ArrayList<TeclaHUDAsset> mHUDAssets = new ArrayList<TeclaHUDAsset>();
	private int[] mCenterLocation = new int[2];
	
	private Matrix matrix = new Matrix();
	private Paint paint = new Paint();
	private Point size = new Point();

    public TeclaHUD(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mCenterLocation[0] = size.x/2;
        mCenterLocation[1] = size.y/2;
    
        Bitmap bmp;
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
        mHUDAssets.add(new TeclaHUDAsset("Up", bmp, 0, 0, 0));        
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas c) {
    	for (TeclaHUDAsset t: mHUDAssets) {
    		matrix.setRotate(t.mAngleDegree, 
    				mCenterLocation[0]+t.mScreenLocationOffset[0]+t.mBmp.getWidth()/2, 
    				mCenterLocation[1]+t.mScreenLocationOffset[1]+t.mBmp.getHeight()/2);
    		paint.setAlpha(t.mAlpha);
    		c.drawBitmap(t.mBmp, matrix, paint);
    	}
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getSize(size);
        mCenterLocation[0] = size.x/2;
        mCenterLocation[1] = size.y/2;
    
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
