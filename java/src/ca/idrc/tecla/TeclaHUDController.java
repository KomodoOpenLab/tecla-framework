package ca.idrc.tecla;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaHUDController extends SimpleOverlay {

	private static TeclaHUDController sInstance;

	protected final static long SCAN_PERIOD = 1500;
	private byte mState;
	protected final static byte TOTAL_STATES = 5;
	protected final static byte STATE_UP = 0;
	protected final static byte STATE_SELECT = 1;
	protected final static byte STATE_RIGHT = 2;
	protected final static byte STATE_FORWARD = 3;
	protected final static byte STATE_DOWN = 4;
	protected final static byte STATE_BACK = 5;
	protected final static byte STATE_LEFT = 6;
	protected final static byte STATE_HOME = 7;
	
	public TeclaHUDController(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		setParams(params);

		setContentView(R.layout.tecla_controller);

		getRootView().setOnLongClickListener(mOverlayLongClickListener);
		getRootView().setOnClickListener(mOverlayClickListener);

        mState = STATE_UP;
        mAutoScanHandler.sleep(1000);
	}

	@Override
	protected void onShow() {
		sInstance = this;
	}

	@Override
	protected void onHide() {
        sInstance = null;
	}
	
	private View.OnClickListener mOverlayClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			scanTrigger();
			
		}
	};	

	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			Log.v("TeclaA11y", "Long clicked.  ");
			TeclaAccessibilityService.getInstance().shutdownInfrastructure();
			return true;
		}
	};

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
		case(STATE_SELECT):
			TeclaAccessibilityService.clickActiveNode();
			break; 
		default: 
			break; 
		}
		mAutoScanHandler.sleep(SCAN_PERIOD);
	}
	protected void scanForward() {
		++mState;
		mState %= TOTAL_STATES;
		
		// update HUD graphics 
		switch (mState) {
		case(STATE_UP):
			break; 
		case(STATE_SELECT):
			break; 
		case(STATE_RIGHT):
			break; 
		case(STATE_FORWARD):
			break; 
		case(STATE_DOWN):
			break; 
		case(STATE_BACK):
			break; 
		case(STATE_LEFT):
			break; 
		case(STATE_HOME):
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
        	TeclaHUDController.this.scanForward();
        }

        public void sleep(long delayMillis) {
                removeMessages(0);
                sendMessageDelayed(obtainMessage(0), delayMillis);
        }
	}
	AutoScanHandler mAutoScanHandler = new AutoScanHandler();

}
