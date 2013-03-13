package ca.idrc.tecla;

import android.graphics.Bitmap;


public class TeclaHUDAsset {
    public String mText;
    Bitmap mBmp;
	public int[] mScreenLocationOffset = new int[2];
    public float mAngleDegree;
    private boolean mSelected = false;
    public int mAlpha;
    
	public TeclaHUDAsset(String text, Bitmap bmp, int dx, int dy, float angle, int alpha) {
		mText = text;
		mBmp = bmp;
		mScreenLocationOffset[0] = dx;
		mScreenLocationOffset[1] = dy;
		mAngleDegree = angle;
		mAlpha = alpha;
	}

	public void setSelected(boolean b) {
		mSelected = b;
	}
	
	public boolean isSelected() {
		return mSelected; 
	}
	
}
