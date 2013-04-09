package ca.idrc.tecla.hud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class TeclaHUDCornerView extends ImageButton {
	
    private final Paint mInnerPaint = new Paint();
    private final Paint mOuterPaint = new Paint();
    private int mWidth;
    private int mHeight;
    private int mInnerStrokeWidth;
    private int mOuterStrokeWidth;
	private Path mPath;
	private Bitmap mBackground;
	private Canvas mBackgroundCanvas;
	private float mBackgroundRotation;

	public TeclaHUDCornerView(Context context, AttributeSet attrs) {
        super(context, attrs);
		init();
	}

	private void init() {
		
		mBackgroundRotation = 0.0f;
    	mInnerPaint.setColor(Color.argb(0xFF, 0x2F, 0xE6, 0xFF));
    	mOuterPaint.setColor(Color.argb(0xFF, 0x29, 0x58, 0x75));
    	mInnerPaint.setStyle(Paint.Style.STROKE);
    	mOuterPaint.setStyle(Paint.Style.STROKE);

	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mWidth = w;
		mHeight = h;

		updateBackground();		
    	invalidate();
    	
	}
	
	public void setBackgroundRotation(float degrees) {
		mBackgroundRotation = degrees;
		updateBackground();
		invalidate();
	}
	
	private void updateBackground () {
		if (mWidth > 0 && mHeight > 0) {
			mBackground = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
	    	mBackgroundCanvas = new Canvas(mBackground);
	    	mInnerStrokeWidth = (int) Math.round(0.01 * mWidth);
			mOuterStrokeWidth = 2 * mInnerStrokeWidth;
	    	mInnerPaint.setStrokeWidth(mInnerStrokeWidth);
	    	mOuterPaint.setStrokeWidth(2 * mOuterStrokeWidth);
	    	mPath = new Path();
	    	int left = mOuterStrokeWidth;
	    	int top = mOuterStrokeWidth;
	    	int width = mWidth - mOuterStrokeWidth;
	    	int height = mHeight - mOuterStrokeWidth;
			float pad = 0.28f * width;
			
			mPath.moveTo(left, height - pad);
	    	mPath.lineTo(width - pad, top);
	    	mPath.lineTo(width, height - pad);
	    	mPath.lineTo(width - pad, height);
	    	mPath.lineTo(left, height - pad);
	    	
	    	Matrix m = new Matrix();
	    	m.setRotate(mBackgroundRotation, mWidth/2.0f, mHeight/2.0f);
	    	mPath.transform(m);

	    	mBackgroundCanvas.drawPath(mPath, mOuterPaint);
	    	mBackgroundCanvas.drawPath(mPath, mInnerPaint);
	    	setBackground(new BitmapDrawable(getResources(), mBackground));
		}
	}
}
