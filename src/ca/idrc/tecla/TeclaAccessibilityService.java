package ca.idrc.tecla;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	private final static boolean DEBUG = true;
	private static TeclaAccessibilityService sInstance;
	
	private AccessibilityNodeInfo mOriginalNode, mSelectedNode;
	
	private ArrayList<AccessibilityNodeInfo> mActiveNodes;
	private int mNodeIndex;
	public final static int DIRECTION_UP = 0;
	public final static int DIRECTION_LEFT = 1;
	public final static int DIRECTION_RIGHT = 2;
	public final static int DIRECTION_DOWN = 3;
	private final static int DIRECTION_UP_NORATIOCONSTRAINT = 4;
	private final static int DIRECTION_LEFT_NORATIOCONSTRAINT = 5;
	private final static int DIRECTION_RIGHT_NORATIOCONSTRAINT = 6;
	private final static int DIRECTION_DOWN_NORATIOCONSTRAINT = 7;
	private final static int DIRECTION_ANY = 8;
	
	private int mTouchMode;
	private TeclaAccessibilityOverlay mTeclaAccessibilityOverlay;
	private TeclaShieldControl mControl;
	
	// used for debugging 
	private final static int TOUCHED_TOPLEFT = 0;
	private final static int TOUCHED_TOPRIGHT = 1;
	private final static int TOUCHED_BOTTOMLEFT = 2;
	private final static int TOUCHED_BOTTOMRIGHT = 3;
	private final static int SCROLLED_NOWHERE = 0;
	private final static int SCROLLED_BACKWARD = 1;
	private final static int SCROLLED_FORWARD = 2;
	private int touchdown;
	private int touchup;
	private int scrollDirection;
	private AccessibilityNodeInfo scrollableNode;
	
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Log.d("TeclaA11y", "Tecla Accessibility Service Connected!");
		
		sInstance = this;
		mOriginalNode = null;
		mActiveNodes = new ArrayList<AccessibilityNodeInfo>();
		mTouchMode = 0;
		
		if (mTeclaAccessibilityOverlay == null) {
			mTeclaAccessibilityOverlay = new TeclaAccessibilityOverlay(this);
			mTeclaAccessibilityOverlay.show();
		}		
		mControl = new TeclaShieldControl(mTeclaAccessibilityOverlay.mControlView);
		
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
				if(mOriginalNode != null) mOriginalNode.recycle();
				mOriginalNode = node;				
				mNodeIndex = 0;
				searchAndUpdateNodes();
			} else if (event_type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {	
				mSelectedNode = findNeighbourNode(mSelectedNode, DIRECTION_ANY);
				if(mOriginalNode != null) mOriginalNode.recycle();
				mOriginalNode = node;				
				mNodeIndex = 0;
				searchAndUpdateNodes();
			} else if (event_type == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
				//searchAndUpdateNodes();
			} else if (event_type == AccessibilityEvent.TYPE_VIEW_SELECTED) {
				//searchAndUpdateNodes();
			} else if(event_type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
				/*
				searchAndUpdateNodes();
				mNodeIndex = 0;
				for (AccessibilityNodeInfo testnode: mActiveNodes) {
					if(scrollableNode.equals(testnode)) {
						Log.w("TeclaAlly", "found the matching scroll node!");
						return; 
					}
				}
				Log.w("TeclaAlly", "couldn't find the matching scroll node!");
				*/
			}
		} else {
			Log.e("TeclaA11y", "Node is null!");
		}
	}

	private void searchAndUpdateNodes() {
		searchActiveNodesBFS(mOriginalNode);
		if (mActiveNodes.size() > 0 ) {
			mSelectedNode =  mActiveNodes.get(mNodeIndex);
			TeclaAccessibilityOverlay.updateNodes(mOriginalNode, mSelectedNode);				
		}		
	}
	
	private AccessibilityNodeInfo searchActiveNodeBFS(AccessibilityNodeInfo node) {
		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
		q.add(node);
		while (!q.isEmpty()) {
			AccessibilityNodeInfo thisnode = q.poll();
			if(thisnode.isVisibleToUser() && thisnode.isClickable() && !thisnode.isScrollable()) {
				if(thisnode.isFocused() || thisnode.isSelected()) {
					return thisnode;
				}
			}
			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
		}
		return null;
	}
	
	private void searchActiveNodesBFS(AccessibilityNodeInfo node) {
		mActiveNodes.clear();
		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
		q.add(node);
		while (!q.isEmpty()) {
			AccessibilityNodeInfo thisnode = q.poll();
			if(thisnode == null) continue; 
			if(thisnode.isVisibleToUser() && thisnode.isClickable() && !thisnode.isScrollable()) {
			//if(thisnode.isFocused() || thisnode.isSelected()) {
					mActiveNodes.add(thisnode);
				//}
			}
			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
		}
	}
	
	public static void selectNode(AccessibilityNodeInfo refnode, int direction ) {
		AccessibilityNodeInfo node;
		switch (direction ) {
		case DIRECTION_UP:
			node = findNeighbourNode(refnode, DIRECTION_UP);
			if(node == null) node = findNeighbourNode(refnode, DIRECTION_UP_NORATIOCONSTRAINT);
			if(node != null) {
				TeclaAccessibilityService.sInstance.mSelectedNode = node;
				TeclaAccessibilityOverlay.updateNodes(TeclaAccessibilityService.sInstance.mOriginalNode, node);
			}
			break; 
		case DIRECTION_DOWN:
			node = findNeighbourNode(refnode, DIRECTION_DOWN);
			if(node == null) node = findNeighbourNode(refnode, DIRECTION_DOWN_NORATIOCONSTRAINT);
			if(node != null) {
				TeclaAccessibilityService.sInstance.mSelectedNode = node;
				TeclaAccessibilityOverlay.updateNodes(TeclaAccessibilityService.sInstance.mOriginalNode, node);
			}
			break; 
		case DIRECTION_LEFT:
			node = findNeighbourNode(refnode, DIRECTION_LEFT);
			if(node == null) node = findNeighbourNode(refnode, DIRECTION_LEFT_NORATIOCONSTRAINT);
			if(node != null) {
				TeclaAccessibilityService.sInstance.mSelectedNode = node;
				TeclaAccessibilityOverlay.updateNodes(TeclaAccessibilityService.sInstance.mOriginalNode, node);
			}
			break; 
		case DIRECTION_RIGHT:
			node = findNeighbourNode(refnode, DIRECTION_RIGHT);
			if(node == null) node = findNeighbourNode(refnode, DIRECTION_RIGHT_NORATIOCONSTRAINT);
			if(node != null) {
				TeclaAccessibilityService.sInstance.mSelectedNode = node;
				TeclaAccessibilityOverlay.updateNodes(TeclaAccessibilityService.sInstance.mOriginalNode, node);
			}
			break; 
		default: 
			break; 
		}
	}
	
	private static AccessibilityNodeInfo findNeighbourNode(AccessibilityNodeInfo refnode, int direction) {
		int r2_min = Integer.MAX_VALUE;
		int r2;
		double ratio;
		Rect refOutBounds = new Rect();
		refnode.getBoundsInScreen(refOutBounds);
		int x = refOutBounds.centerX();
		int y = refOutBounds.centerY();
		Rect outBounds = new Rect();
		AccessibilityNodeInfo result = null; 
		for (AccessibilityNodeInfo node: TeclaAccessibilityService.sInstance.mActiveNodes ) {
			if(refnode.equals(node)) continue; 
			node.getBoundsInScreen(outBounds);
			r2 = (x - outBounds.centerX())*(x - outBounds.centerX()) 
					+ (y - outBounds.centerY())*(y - outBounds.centerY());
			switch (direction ) {
			case DIRECTION_UP:
				ratio =(y - outBounds.centerY())/Math.sqrt(r2);
				if(ratio < Math.PI/4) continue; 
				break; 
			case DIRECTION_DOWN:
				ratio =(outBounds.centerY() - y)/Math.sqrt(r2);
				if(ratio < Math.PI/4) continue;
				break; 
			case DIRECTION_LEFT:
				ratio =(x - outBounds.centerX())/Math.sqrt(r2);
				if(ratio <= Math.PI/4) continue;
				break; 
			case DIRECTION_RIGHT:
				ratio =(outBounds.centerX() - x)/Math.sqrt(r2);
				if(ratio <= Math.PI/4) continue;
				break; 
			case DIRECTION_UP_NORATIOCONSTRAINT:
				if(y - outBounds.centerY() <= 0) continue; 
				break; 
			case DIRECTION_DOWN_NORATIOCONSTRAINT:
				if(outBounds.centerY() - y <= 0) continue;
				break; 
			case DIRECTION_LEFT_NORATIOCONSTRAINT:
				if(x - outBounds.centerX() <= 0) continue;
				break; 
			case DIRECTION_RIGHT_NORATIOCONSTRAINT:
				if(outBounds.centerX() - x <= 0) continue;
				break; 
			case DIRECTION_ANY:
				break; 
			default: 
				break; 
			}
			if(r2 < r2_min) {
				r2_min = r2;
				result = node; 
			}
		}
		return result;		
	}
	
	public static void clickActiveNode() {
		sInstance.mSelectedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	}
	
	public static void selectActiveNode(int index) {
		if(sInstance.mActiveNodes.size()==0) return; 
		sInstance.mNodeIndex = index;
		sInstance.mNodeIndex = Math.min(sInstance.mActiveNodes.size() - 1, sInstance.mNodeIndex + 1);
		sInstance.mNodeIndex = Math.max(0, sInstance.mNodeIndex - 1);
		TeclaAccessibilityOverlay.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
	}
	
	public static void selectPreviousActiveNode() {
		if(sInstance.mActiveNodes.size()==0) return; 
		sInstance.mNodeIndex = Math.max(0, sInstance.mNodeIndex - 1);
		TeclaAccessibilityOverlay.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
		
	}
	
	public static void selectNextActiveNode() {
		if(sInstance.mActiveNodes.size()==0) return;
		sInstance.mNodeIndex = Math.min(sInstance.mActiveNodes.size() - 1, sInstance.mNodeIndex + 1);
		TeclaAccessibilityOverlay.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
	}

	public static void scrollBackward() {
		boolean foundScrollableNode = false; 
		AccessibilityNodeInfo node = sInstance.mActiveNodes.get(sInstance.mNodeIndex);
		if(node == null) return; 
		while (!foundScrollableNode) {
			node = node.getParent();
			if(node == null) return; 
			if(node.isScrollable()) {
				sInstance.scrollableNode = node;
				sInstance.scrollDirection = SCROLLED_BACKWARD; 
				node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
				if(sInstance.mOriginalNode != null) sInstance.mOriginalNode.recycle();
				return; 
			}
		}
		sInstance.scrollDirection = SCROLLED_NOWHERE;
	}
	
	public static void scrollForward() {
		boolean foundScrollableNode = false; 
		AccessibilityNodeInfo node = sInstance.mActiveNodes.get(sInstance.mNodeIndex);
		if(node == null) return; 
		while (!foundScrollableNode) {
			node = node.getParent();
			if(node == null) return; 
			if(node.isScrollable()) {
				sInstance.scrollableNode = node;
				sInstance.scrollDirection = SCROLLED_FORWARD;
				node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
				if(sInstance.mOriginalNode != null) sInstance.mOriginalNode.recycle();
				return; 
			}
		}
		sInstance.scrollDirection = SCROLLED_NOWHERE;
		
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
					mControl.setPreviousSelected();
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPRIGHT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					mControl.setNextSelected();
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT) {
					if(mTouchMode == 1) {
						break; 
					}
					mControl.performAction(mSelectedNode);
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					if(mTouchMode == 1) {
						break; 
					}
					mControl.performAction(mSelectedNode);
					//Log.w("TeclaA11y", "Current node: " +  mActiveNodes.get(mActiveNodes.size()-1).toString());
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_BOTTOMLEFT && touchup==TeclaAccessibilityService.TOUCHED_TOPRIGHT) {
					// shut down   
					++mTouchMode;
					mTouchMode = mTouchMode%2;
					Log.w("TeclaA11y", "Touch mode = " + mTouchMode);
				} else if(touchdown==TeclaAccessibilityService.TOUCHED_TOPLEFT && touchup==TeclaAccessibilityService.TOUCHED_BOTTOMRIGHT) {
					// shut down  
					Log.w("TeclaA11y", "3-switch access: SHUTDOWN");
					shutdownInfrastructure();
				}
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
