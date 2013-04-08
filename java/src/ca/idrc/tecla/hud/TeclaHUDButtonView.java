package ca.idrc.tecla.hud;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

public class TeclaHUDButtonView extends ImageButton {
	
    private final Paint mInnerPaint = new Paint();
    private final Paint mOuterPaint = new Paint();
    private int mWidth;
    private int mHeight;
    private int mInnerStrokeWidth;
    private int mOuterStrokeWidth;
	private Rect mRect;

	public TeclaHUDButtonView(Context context) {
		super(context);
		
		init();
	}

	public TeclaHUDButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
		init();
	}

	private void init() {
		setBackground(null);

    	mInnerPaint.setColor(Color.argb(0xFF, 0x2F, 0xE6, 0xFF));
    	mOuterPaint.setColor(Color.argb(0xFF, 0x29, 0x58, 0x75));
    	mInnerPaint.setStyle(Paint.Style.STROKE);
    	mOuterPaint.setStyle(Paint.Style.STROKE);

	}
	
    @Override
    public void onDraw(Canvas c) {
    	c.drawRect(mRect, mOuterPaint);
    	c.drawRect(mRect, mInnerPaint);
    }

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
		mHeight = h;

    	Log.d("TeclaJB", "Width: " + mWidth + " Height: " + mHeight);

    	mInnerStrokeWidth = (int) Math.round(0.01 * mWidth);
		mOuterStrokeWidth = 2 * mInnerStrokeWidth;
    	mInnerPaint.setStrokeWidth(mInnerStrokeWidth);
    	mOuterPaint.setStrokeWidth(2 * mOuterStrokeWidth);
    	mRect = new Rect();
    	int left = (int) Math.round(mOuterStrokeWidth / 2.0);
    	int top = (int) Math.round(mOuterStrokeWidth / 2.0);
    	int width = mWidth - (int) Math.round(mOuterStrokeWidth / 2.0);
    	int height = mHeight - (int) Math.round(mOuterStrokeWidth / 2.0);
    	mRect.set(left, top, width, height);

		invalidate();
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
