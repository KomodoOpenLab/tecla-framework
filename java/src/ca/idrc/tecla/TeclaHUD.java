package ca.idrc.tecla;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class TeclaHUD extends View {
	
	protected final static long SCAN_PERIOD = 1500;
	private byte mState;
	protected final static byte TOTAL_STATES = 5;
	protected final static byte STATE_UP = 0;
	protected final static byte STATE_RIGHT = 1;
	protected final static byte STATE_DOWN = 2;
	protected final static byte STATE_LEFT = 3;
	protected final static byte STATE_OK = 4;
	//protected final static byte STATE_BACK = 5;
	//protected final static byte STATE_HOME = 6;
	
    public TeclaHUD(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mState = STATE_UP;
        
        mAutoScanHandler.sleep(1000);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas c) {
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {    
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	protected void scanForward() {
		++mState;
		mState %= TOTAL_STATES;
		this.invalidate();
		mAutoScanHandler.sleep(SCAN_PERIOD);
	}

	protected void scanTrigger() {
		switch (mState) {
		case(STATE_UP):
			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_UP);
			break; 
		case(STATE_RIGHT):
			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_RIGHT);
			break; 
		case(STATE_DOWN):
			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_DOWN);
			break; 
		case(STATE_LEFT):
			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_LEFT);
			break; 
		case(STATE_OK):
			TeclaAccessibilityService.clickActiveNode();
			break; 
		default: 
			break; 
		}
		mAutoScanHandler.sleep(SCAN_PERIOD);
	}
	
	class AutoScanHandler extends Handler {
		public AutoScanHandler() {
			
		}

        @Override
        public void handleMessage(Message msg) {
        	TeclaHUD.this.scanForward();
        }

        public void sleep(long delayMillis) {
                removeMessages(0);
                sendMessageDelayed(obtainMessage(0), delayMillis);
        }
	}
	AutoScanHandler mAutoScanHandler = new AutoScanHandler();
	 
}
