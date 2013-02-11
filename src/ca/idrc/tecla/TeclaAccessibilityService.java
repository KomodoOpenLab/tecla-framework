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
	
	private AccessibilityNodeInfo mOriginalNode, mScanNode;
	private ArrayList<AccessibilityNodeInfo> mScanNodes, mScrollNodes;
	private int mScanNodeIndex;
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
				mOriginalNode = node;
				populateScanNodesBFS(node);
				mScanNodeIndex = 0;
				mScanNode = mScanNodes.get(0);
				TeclaAccessibilityOverlay.updateNodes(mOriginalNode, mScanNode);				
			} else if (event_type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {	
										
			} else if(event_type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
				
			}
		} else {
			Log.e("TeclaA11y", "Node is null!");
		}
	}

	// find the scan nodes with breadth first search 
	private void populateScanNodesBFS(AccessibilityNodeInfo node) {
		mScanNodes.clear();
		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
		q.add(node);
		while (!q.isEmpty()) {
			AccessibilityNodeInfo thisnode = q.poll();
			if(thisnode.isVisibleToUser() && thisnode.isClickable()) mScanNodes.add(thisnode);
			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
		}
		Log.w("TeclaA11y", "There are " + mScanNodes.size() + " elements in the scan node list.");
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
					Log.w("TeclaA11y", "3-switch access: scan node previous");
					if(--mScanNodeIndex < 0) mScanNodeIndex = 0;
					mScanNode = mScanNodes.get(mScanNodeIndex);
					TeclaAccessibilityOverlay.updateNodes(mOriginalNode, mScanNode);
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPRIGHT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					Log.w("TeclaA11y", "3-switch access: scan node next");
					if(++mScanNodeIndex >= mScanNodes.size())  mScanNodeIndex = mScanNodes.size() - 1;
					mScanNode = mScanNodes.get(mScanNodeIndex);
					TeclaAccessibilityOverlay.updateNodes(mOriginalNode, mScanNode);
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT) {
					Log.w("TeclaA11y", "3-switch access: click ");
					mScanNode.performAction(AccessibilityNodeInfo.ACTION_CLICK );
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					Log.w("TeclaA11y", "Current node: " + mScanNode.toString());
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					// shut down   
					Log.w("TeclaA11y", "3-switch access: SHUTDOWN");
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
