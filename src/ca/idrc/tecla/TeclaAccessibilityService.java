package ca.idrc.tecla;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	private final static boolean DEBUG = true;
	
	private AccessibilityNodeInfo original, mScanWindow, mScanNode;
	private ArrayList<AccessibilityNodeInfo> mScanWindows, mScanNodes;
	private byte mScanWindowIndex, mScanNodeIndex;
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
		
		mScanWindows = new ArrayList<AccessibilityNodeInfo>();
		mScanNodes = new ArrayList<AccessibilityNodeInfo>();
		
		if (mTeclaAccessibilityOverlay == null) {
			mTeclaAccessibilityOverlay = new TeclaAccessibilityOverlay(this);
			mTeclaAccessibilityOverlay.show();
		}
		
		if(DEBUG) {
			mTeclaAccessibilityOverlay.getRootView().setOnTouchListener(mOverlayTouchListener);
			mTeclaAccessibilityOverlay.getRootView().setOnLongClickListener(mOverlayLongClickListener);
		}
		
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();
		Log.d("TeclaA11y", AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
		
		AccessibilityNodeInfo node = event.getSource();
		if (node != null) {
			if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {		
				original = node;
				populateScanWindowsBFS(node);
				mScanWindowIndex = 0;
				mScanWindow = mScanWindows.get(mScanWindowIndex);
				populateScanNodes(mScanWindow);
				mScanNodeIndex = 0;
				mScanNode = mScanNodes.get(0);
				TeclaAccessibilityOverlay.updateNodes(mScanWindow, mScanNode);
			}
		} else {
			Log.e("TeclaA11y", "Node is null!");
		}
	}

	// find the scan windows with breadth first search 
	private void populateScanWindowsBFS(AccessibilityNodeInfo node) {
		mScanWindows.clear();
		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
		q.add(node);
		while (!q.isEmpty()) {
			AccessibilityNodeInfo thisnode = q.poll();
			if(thisnode.getChildCount()>1) {
				AccessibilityNodeInfo child = thisnode.getChild(0);
				if(child.isClickable() && child.isVisibleToUser()) mScanWindows.add(thisnode);
			}
			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
		}
	}

	private void populateScanNodes(AccessibilityNodeInfo node) {
		mScanNodes.clear();
		for (int i=0; i<node.getChildCount(); ++i) {
			AccessibilityNodeInfo thisnode = node.getChild(i);
			if(thisnode.isClickable() && thisnode.isVisibleToUser()) mScanNodes.add(thisnode);
		}
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
					touchdown = TeclaAccessibilityService.TOUCHED_BOTTOMLEFT;
				} else if(logicalX>=0.5 && logicalY<0.5) {
					touchdown = TeclaAccessibilityService.TOUCHED_TOPRIGHT;
				} else if(logicalX>=0.5 && logicalY>=0.5) {
					touchdown = TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT;
				}
				break;
			case MotionEvent.ACTION_UP:
				Log.v("TeclaA11y", "Tecla Overlay Touch Up! " + Float.toString(logicalX) + " " + Float.toString(logicalY));

				if(logicalX<0.5 && logicalY<0.5) {
					touchup = TeclaAccessibilityService.TOUCHED_TOPLEFT; 
				} else if(logicalX<0.5 && logicalY>=0.5) {
					touchup = TeclaAccessibilityService.TOUCHED_BOTTOMLEFT;
				} else if(logicalX>=0.5 && logicalY<0.5) {
					touchup = TeclaAccessibilityService.TOUCHED_TOPRIGHT;
				} else if(logicalX>=0.5 && logicalY>=0.5) {
					touchup = TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT;
				}
				
				if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPLEFT) {
					// it's a left! 
					//Log.w("TeclaA11y", "6-switch access: LEFT");					
					//temp_node = original.findFocus(View.FOCUS_LEFT);
					
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPRIGHT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					// it's an up!  
					//Log.w("TeclaA11y", "6-switch access: UP");
					//temp_node = original.findFocus(View.FOCUS_UP);
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT) {
					// it's a down!  
					//Log.w("TeclaA11y", "6-switch access: DOWN");
					//temp_node = original.findFocus(View.FOCUS_DOWN);
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					// it's a right!  
					//Log.w("TeclaA11y", "6-switch access: RIGHT");
					//temp_node = original.findFocus(View.FOCUS_RIGHT);
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					// it's a send!
					//Log.w("TeclaA11y", "6-switch access: SEND");
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					// it's a cancel!  
					//Log.w("TeclaA11y", "6-switch access: CANCEL");
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					// shut down   
					//Log.w("TeclaA11y", "6-switch access: SHUTDOWN");
					shutdownInfrastructure();
				}
				/*
				if(temp_node != null) {
					original = temp_node;
					TeclaAccessibilityOverlay.updateNodes(original, null);
				}
				*/
				break;
			default:
				break;
			}
			return true;
		}
		
	};

	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			shutdownInfrastructure();
			return true;
		}
	};
	
	/**
	 * Shuts down the infrastructure in case it has been initialized.
	 */
	private void shutdownInfrastructure() {		
		if (mTeclaAccessibilityOverlay != null) {
			mTeclaAccessibilityOverlay.hide();
			mTeclaAccessibilityOverlay = null;
		}
	}
}
