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
	protected ArrayList<TeclaHUDAsset> mHUDScanAssets = new ArrayList<TeclaHUDAsset>();
	
	private byte mState = 0;
	protected final static byte STATE_UP = 0;
	protected final static byte STATE_RIGHT = 1;
	protected final static byte STATE_DOWN = 2;
	protected final static byte STATE_LEFT = 3;
	protected final static byte STATE_OK = 4;
	//protected final static byte STATE_BACK = 5;
	//protected final static byte STATE_HOME = 6;
	
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
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_background);
        mHUDAssets.add(new TeclaHUDAsset("DPad Background", bmp, 0, 0, 0));  
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_center);
        mHUDAssets.add(new TeclaHUDAsset("DPad Center", bmp, 0, 0, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.side_button_ok_symbol);
        mHUDAssets.add(new TeclaHUDAsset("OK Symbol", bmp, 0, 0, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.side_button_background);
        mHUDAssets.add(new TeclaHUDAsset("Left Side Button Background", bmp, -180, 320, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.right_side_button_background);
        mHUDAssets.add(new TeclaHUDAsset("Right Side Button Background", bmp, 180, 320, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.side_button_back_symbol);
        mHUDAssets.add(new TeclaHUDAsset("Back Symbol", bmp, -180, 340, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.side_button_home_symbol);
        mHUDAssets.add(new TeclaHUDAsset("Home Symbol", bmp, 180, 340, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
        mHUDAssets.add(new TeclaHUDAsset("Up Arrow", bmp, 0, -250, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
        mHUDAssets.add(new TeclaHUDAsset("Down Arrow", bmp, 0, 250, 180)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
        mHUDAssets.add(new TeclaHUDAsset("Left Arrow", bmp, -250, 0, -90)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow);
        mHUDAssets.add(new TeclaHUDAsset("Right Arrow", bmp, 250, 0, 90));
        
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_arrow_highlighted);
        mHUDScanAssets.add(new TeclaHUDAsset("Arrow Highlight", bmp, 0, 0, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_highlight_background);
        mHUDScanAssets.add(new TeclaHUDAsset("DPad Highlight Background", bmp, 0, 0, 0));  
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_highlight_border);
        mHUDScanAssets.add(new TeclaHUDAsset("DPad Highlight Border", bmp, 0, 0, 0)); 
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpad_center_highlight_border);
        mHUDScanAssets.add(new TeclaHUDAsset("DPad Center Highlight", bmp, 0, 0, 0));  
        
        
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas c) {
    	for (TeclaHUDAsset t: mHUDAssets) {
    		matrix.setRotate(t.mAngleDegree, t.mBmp.getWidth()/2, t.mBmp.getHeight()/2);
    		matrix.postTranslate(mCenterLocation[0]+t.mScreenLocationOffset[0]-t.mBmp.getWidth()/2, 
    				mCenterLocation[1]+t.mScreenLocationOffset[1]-t.mBmp.getHeight()/2);
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
