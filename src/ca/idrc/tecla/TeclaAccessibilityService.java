package ca.idrc.tecla;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	private TeclaAccessibilityOverlay mTeclaAccessibilityOverlay;
	
	// used for debugging 
	private final static int TOUCHED_TOPLEFT = 0;
	private final static int TOUCHED_TOPRIGHT = 1;
	private final static int TOUCHED_BOTTOMLEFT = 2;
	private final static int TOUCHED_BOTTOMRIGHT = 3;
	private int touchdown;
	private int touchup;
	
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Log.d("TeclaA11y", "Tecla Accessibility Service Connected!");

		if (mTeclaAccessibilityOverlay == null) {
			mTeclaAccessibilityOverlay = new TeclaAccessibilityOverlay(this);
			mTeclaAccessibilityOverlay.show();
		}
		
		mTeclaAccessibilityOverlay.getRootView().setOnTouchListener(mOverlayTouchListener);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();
		Log.d("TeclaA11y", AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
		
	}

	@Override
	public void onInterrupt() {
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int width = v.getWidth();
			int height = v.getHeight();
			float x=event.getX();
			float y=event.getY();
			float logicalX=x/width;
			float logicalY=y/height;
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.v("TeclaA11y", "Tecla Overlay Touch Down! " + Float.toString(logicalX) + " " + Float.toString(logicalY));
				
				if(logicalX<0.5 && logicalY<0.5) {
					touchdown = TeclaAccessibilityService.TOUCHED_TOPLEFT; 
				} else if(logicalX<0.5 && logicalY>=0.5) {
					touchdown = TeclaAccessibilityService.TOUCHED_TOPRIGHT;
				} else if(logicalX>=0.5 && logicalY<0.5) {
					touchdown = TeclaAccessibilityService.TOUCHED_BOTTOMLEFT;
				} else if(logicalX>=0.5 && logicalY>=0.5) {
					touchdown = TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT;
				}
				break;
			case MotionEvent.ACTION_UP:
				Log.v("TeclaA11y", "Tecla Overlay Touch Up! " + Float.toString(logicalX) + " " + Float.toString(logicalY));

				if(logicalX<0.5 && logicalY<0.5) {
					touchup = TeclaAccessibilityService.TOUCHED_TOPLEFT; 
				} else if(logicalX<0.5 && logicalY>=0.5) {
					touchup = TeclaAccessibilityService.TOUCHED_TOPRIGHT;
				} else if(logicalX>=0.5 && logicalY<0.5) {
					touchup = TeclaAccessibilityService.TOUCHED_BOTTOMLEFT;
				} else if(logicalX>=0.5 && logicalY>=0.5) {
					touchup = TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT;
				}
				
				if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPLEFT) {
					// it's a left!  
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPRIGHT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					// it's an up!  
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT) {
					// it's a down!  
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					// it's a right!  
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					// it's a send!
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					// it's a cancel!  
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					// shut down   
				}
				break;
			default:
				break;
			}
			return false;
		}
		
	};
}
