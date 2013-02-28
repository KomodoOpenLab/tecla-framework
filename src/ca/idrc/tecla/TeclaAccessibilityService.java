package ca.idrc.tecla;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	private final static boolean DEBUG = false;
	
	private static TeclaAccessibilityService sInstance;
	
	private AccessibilityNodeInfo mOriginalNode, mPreviousOriginalNode, mSelectedNode;
	
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
	
	private TeclaHighlighter mTeclaHighlighter;
	private TeclaController mTeclaController;
	
	public static TeclaAccessibilityService getInstance() {
		return sInstance;
	}
	
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Log.d("TeclaA11y", "Tecla Accessibility Service Connected!");
		
		sInstance = this;
		mOriginalNode = null;
		mActiveNodes = new ArrayList<AccessibilityNodeInfo>();
		
		if (mTeclaHighlighter == null) {
			mTeclaHighlighter = new TeclaHighlighter(this);
			mTeclaHighlighter.show();
		}
		
		if(DEBUG) {
			mTeclaController = new TeclaController(this);
			mTeclaController.getRootView().setOnLongClickListener(mOverlayLongClickListener);
			mTeclaController.show();
		}
		
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();
		Log.d("TeclaA11y", AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
		
		AccessibilityNodeInfo node = event.getSource();
		if (node != null) {
			if (event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
				mPreviousOriginalNode = mOriginalNode;
				mOriginalNode = node;				
				mNodeIndex = 0;
				searchAndUpdateNodes();
			} else if (event_type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {	
				mPreviousOriginalNode = mOriginalNode;
				mOriginalNode = node;				
				mNodeIndex = 0;
				searchAndUpdateNodes();
			} else if (event_type == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
				//searchAndUpdateNodes();
			} else if (event_type == AccessibilityEvent.TYPE_VIEW_SELECTED) {
				//searchAndUpdateNodes();
			} else if(event_type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
				mPreviousOriginalNode = mOriginalNode;
				mOriginalNode = node;				
				mNodeIndex = 0;
				searchAndUpdateNodes();
			}
		} else {
			Log.e("TeclaA11y", "Node is null!");
		}
	}

	private void searchAndUpdateNodes() {
		searchActiveNodesBFS(mOriginalNode);
		if (mActiveNodes.size() > 0 ) {
			mSelectedNode = findNeighbourNode(mSelectedNode, DIRECTION_ANY);
			if(mSelectedNode == null) mSelectedNode = mActiveNodes.get(0);
			TeclaHighlighter.updateNodes(mSelectedNode.getParent(), mSelectedNode);	
			if(mPreviousOriginalNode != null) mPreviousOriginalNode.recycle();
		}		
	}
	
	private void searchActiveNodesBFS(AccessibilityNodeInfo node) {
		mActiveNodes.clear();
		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
		q.add(node);
		while (!q.isEmpty()) {
			AccessibilityNodeInfo thisnode = q.poll();
			if(thisnode == null) continue;
			if(thisnode.isVisibleToUser() && thisnode.isClickable() 
					&& !thisnode.isScrollable()) {
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
				TeclaHighlighter.updateNodes(node.getParent(), node);
			}
			break; 
		case DIRECTION_DOWN:
			node = findNeighbourNode(refnode, DIRECTION_DOWN);
			if(node == null) node = findNeighbourNode(refnode, DIRECTION_DOWN_NORATIOCONSTRAINT);
			if(node != null) {
				TeclaAccessibilityService.sInstance.mSelectedNode = node;
				TeclaHighlighter.updateNodes(node.getParent(), node);
			}
			break; 
		case DIRECTION_LEFT:
			node = findNeighbourNode(refnode, DIRECTION_LEFT);
			if(node == null) node = findNeighbourNode(refnode, DIRECTION_LEFT_NORATIOCONSTRAINT);
			if(node != null) {
				TeclaAccessibilityService.sInstance.mSelectedNode = node;
				TeclaHighlighter.updateNodes(node.getParent(), node);
			}
			break; 
		case DIRECTION_RIGHT:
			node = findNeighbourNode(refnode, DIRECTION_RIGHT);
			if(node == null) node = findNeighbourNode(refnode, DIRECTION_RIGHT_NORATIOCONSTRAINT);
			if(node != null) {
				TeclaAccessibilityService.sInstance.mSelectedNode = node;
				TeclaHighlighter.updateNodes(node.getParent(), node);
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
		if(refnode == null) return null;
		refnode.getBoundsInScreen(refOutBounds);
		int x = refOutBounds.centerX();
		int y = refOutBounds.centerY();
		Rect outBounds = new Rect();
		AccessibilityNodeInfo result = null; 
		for (AccessibilityNodeInfo node: TeclaAccessibilityService.sInstance.mActiveNodes ) {
			if(refnode.equals(node) && direction != DIRECTION_ANY) continue; 
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
		TeclaHighlighter.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
	}
	
	public static void selectPreviousActiveNode() {
		if(sInstance.mActiveNodes.size()==0) return; 
		sInstance.mNodeIndex = Math.max(0, sInstance.mNodeIndex - 1);
		TeclaHighlighter.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
		
	}
	
	public static void selectNextActiveNode() {
		if(sInstance.mActiveNodes.size()==0) return;
		sInstance.mNodeIndex = Math.min(sInstance.mActiveNodes.size() - 1, sInstance.mNodeIndex + 1);
		TeclaHighlighter.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
	}
	
	@Override
	public void onInterrupt() {
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdownInfrastructure();
	}

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
	public void shutdownInfrastructure() {		
		if (mTeclaController != null) {
			mTeclaController.hide();
			mTeclaController = null;
		}
		if (mTeclaHighlighter != null) {
			mTeclaHighlighter.hide();
			mTeclaHighlighter = null;
		}
	}
}
