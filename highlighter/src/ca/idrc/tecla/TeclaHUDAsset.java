package ca.idrc.tecla;

import android.graphics.Bitmap;


public class TeclaHUDAsset {
    public String mText;
    Bitmap mBmp;
	public int[] mScreenLocationOffset = new int[2];
    public float mAngleDegree;
    private boolean mSelected = false;
    public int mAlpha = 255;
    
	public TeclaHUDAsset(String text, Bitmap bmp, int dx, int dy, float angle) {
		mText = text;
		mBmp = bmp;
		mScreenLocationOffset[0] = dx;
		mScreenLocationOffset[1] = dy;
		mAngleDegree = angle;
	}
    
}
