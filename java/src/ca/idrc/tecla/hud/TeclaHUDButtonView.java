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

public class TeclaHUDButtonView extends ImageButton {
	
	public final static byte POSITION_LEFT = 4;
	public final static byte POSITION_TOP_LEFT = 5;
	public final static byte POSITION_TOP = 6;
	public final static byte POSITION_TOP_RIGHT = 7;
	public final static byte POSITION_RIGHT = 8;
	public final static byte POSITION_BOTTOM_RIGHT = 9;
	public final static byte POSITION_BOTTOM = 10;
	public final static byte POSITION_BOTTOM_LEFT = 11;
	
    private final Paint mInnerPaint = new Paint();
    private final Paint mOuterPaint = new Paint();
    private int mWidth;
    private int mHeight;
    private int mStrokeWidth;
	private Path mPath;
	private Bitmap mBackground;
	private Canvas mBackgroundCanvas;
	private byte mPosition;

	public TeclaHUDButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
		init();
	}

	private void init() {
		
		mStrokeWidth = 1;
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
	
	public void setProperties(byte position, int stroke_width) {
		mPosition = position;
		mStrokeWidth = stroke_width;
		updateBackground();
		invalidate();
	}
	
	private void updateBackground () {
		if (mWidth > 0 && mHeight > 0) {
			
			mBackground = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
	    	mBackgroundCanvas = new Canvas(mBackground);
	    	
	    	int outer_stroke_width = 2 * mStrokeWidth;
	    	mInnerPaint.setStrokeWidth(mStrokeWidth);
	    	mOuterPaint.setStrokeWidth(outer_stroke_width);
	    	mPath = new Path();
	    	int left = outer_stroke_width;
	    	int top = outer_stroke_width;
	    	int right = mWidth - outer_stroke_width;
	    	int bottom = mHeight - outer_stroke_width;
			float pad;

			if (mPosition == POSITION_TOP_LEFT ||
					mPosition == POSITION_TOP_RIGHT ||
					mPosition == POSITION_BOTTOM_LEFT ||
					mPosition == POSITION_BOTTOM_RIGHT) {				
				pad = 0.28f * right;
				mPath.moveTo(left, bottom - pad);
		    	mPath.lineTo(right - pad, top);
		    	mPath.lineTo(right, bottom - pad);
		    	mPath.lineTo(right - pad, bottom);
		    	mPath.lineTo(left, bottom - pad);
		    	float background_rotation = 0.0f;
				switch(mPosition) {
				case POSITION_TOP_RIGHT:
					background_rotation = 90.0f;
			    	break;
				case POSITION_BOTTOM_LEFT:
					background_rotation = 270.0f;
			    	break;
				case POSITION_BOTTOM_RIGHT:
					background_rotation = 180.0f;
			    	break;
				}		    	
		    	Matrix m = new Matrix();
		    	m.setRotate(background_rotation, mWidth/2.0f, mHeight/2.0f);
		    	mPath.transform(m);
			} else {
				switch(mPosition) {
				case POSITION_LEFT:
					pad = 0.2f * right;
					mPath.moveTo(left, top);
			    	mPath.lineTo(right, pad);
			    	mPath.lineTo(right, bottom - pad);
			    	mPath.lineTo(left, bottom);
			    	mPath.lineTo(left, top);
			    	break;
				case POSITION_TOP:
					pad = 0.2f * bottom;
					mPath.moveTo(left, top);
			    	mPath.lineTo(right, top);
			    	mPath.lineTo(right - pad, bottom);
			    	mPath.lineTo(pad, bottom);
			    	mPath.lineTo(left, top);
			    	break;
				case POSITION_RIGHT:
					pad = 0.2f * right;
					mPath.moveTo(right, top);
			    	mPath.lineTo(right, bottom);
			    	mPath.lineTo(left, bottom - pad);
			    	mPath.lineTo(left, pad);
			    	mPath.lineTo(right, top);
			    	break;
				case POSITION_BOTTOM:
					pad = 0.2f * bottom;
					mPath.moveTo(left, bottom);
			    	mPath.lineTo(pad, top);
			    	mPath.lineTo(right - pad, top);
			    	mPath.lineTo(right, bottom);
			    	mPath.lineTo(left, bottom);
			    	break;
				}		    	
			}
	    	
	    	mBackgroundCanvas.drawPath(mPath, mOuterPaint);
	    	mBackgroundCanvas.drawPath(mPath, mInnerPaint);
	    	setBackground(new BitmapDrawable(getResources(), mBackground));
		}
	}
}
