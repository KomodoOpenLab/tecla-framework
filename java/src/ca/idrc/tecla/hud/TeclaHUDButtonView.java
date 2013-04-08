package ca.idrc.tecla.hud;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class TeclaHUDButtonView extends ImageButton {
	
    private final Paint mInnerPaint = new Paint();
    private final Paint mOuterPaint = new Paint();
    private int mWidth;
    private int mHeight;
    private int mInnerStrokeWidth;
    private int mOuterStrokeWidth;
	private Path mPath;

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
    	c.drawPath(mPath, mOuterPaint);
    	c.drawPath(mPath, mInnerPaint);
    }

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
		mHeight = h;

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

    	invalidate();
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
