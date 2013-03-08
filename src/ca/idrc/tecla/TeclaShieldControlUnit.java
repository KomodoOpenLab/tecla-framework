package ca.idrc.tecla;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class TeclaShieldControlUnit {
	public int[] mScreenLocationOffset = new int[2];
	public final Paint mPaint = new Paint();
    private int mColor, mColorSelected, mColorNotSelected;
    public String mText;
    private boolean mSelected = false;
    
	public TeclaShieldControlUnit(String text, int x, int y, Paint.Align align) {
		mText = text; 
		mScreenLocationOffset[0] = x;
		mScreenLocationOffset[1] = y;
		
		float[] hsv = new float[3];
		Color.colorToHSV(Color.GRAY, hsv);
		hsv[2] = 0.3f;
		mColorNotSelected = Color.HSVToColor(hsv);
		Color.colorToHSV(Color.GREEN, hsv);
		hsv[2] = 1f;
		mColorSelected = Color.HSVToColor(hsv);
		
		mColor = mColorNotSelected;
		mPaint.setStyle(Style.FILL);
        mPaint.setTextSize(80);
        mPaint.setTextAlign(align);
        mPaint.setColor(mColor);
		
	}
    
	public void setSelected(boolean b) {
		mSelected = b;
		if(!b) mColor = mColorNotSelected;
		else mColor = mColorSelected;
		mPaint.setColor(mColor);
	}
	
	public boolean isSelected() {
		return mSelected; 
	}
	
}