package com.android.tecla.addon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import com.android.tecla.addon.SwitchEventProvider.LocalBinder;

import ca.idi.tecla.sdk.SwitchEvent;
import ca.idi.tecla.sdk.SEPManager;
import ca.idrc.tecla.framework.TeclaStatic;
import ca.idrc.tecla.highlighter.TeclaHighlighter;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	private final static String CLASS_TAG = "TeclaA11yService";

	public final static int DIRECTION_UP = 0;
	public final static int DIRECTION_LEFT = 1;
	public final static int DIRECTION_RIGHT = 2;
	public final static int DIRECTION_DOWN = 3;
	private final static int DIRECTION_ANY = 8;

	private static TeclaAccessibilityService sInstance;

	private Boolean register_receiver_called;

	private AccessibilityNodeInfo mOriginalNode, mPreviousOriginalNode;
	protected AccessibilityNodeInfo mSelectedNode;

	private ArrayList<AccessibilityNodeInfo> mActiveNodes;
	private int mNodeIndex;

	private TeclaVisualOverlay mVisualOverlay;
	private SingleSwitchTouchInterface mFullscreenSwitch;

	protected static ReentrantLock mActionLock;

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();

		TeclaStatic.logD(CLASS_TAG, "Service " + TeclaAccessibilityService.class.getName() + " connected");

		init();

	}

	private void init() {
		register_receiver_called = false;

		mOriginalNode = null;
		mActiveNodes = new ArrayList<AccessibilityNodeInfo>();
		mActionLock = new ReentrantLock();

		if(mVisualOverlay == null) {
			mVisualOverlay = new TeclaVisualOverlay(this);
			TeclaApp.setVisualOverlay(mVisualOverlay);
		}
		
		if (mFullscreenSwitch == null) {
			mFullscreenSwitch = new SingleSwitchTouchInterface(this);	
			TeclaApp.setFullscreenSwitch(mFullscreenSwitch);		
		}

		// Bind to SwitchEventProvider
		Intent intent = new Intent(this, SwitchEventProvider.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		registerReceiver(mReceiver, new IntentFilter(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED));
		register_receiver_called = true;
		SEPManager.start(this);

		sInstance = this;
		TeclaApp.setA11yserviceInstance(this);
	}
	
	public void hideFullscreenSwitch() {
		if (mFullscreenSwitch != null) {
			if (mFullscreenSwitch.isVisible()) {
				mFullscreenSwitch.hide();
			}
		}
	}

	public void showFullscreenSwitch() {
		if (mFullscreenSwitch != null) {
			if (!mFullscreenSwitch.isVisible()) {
				mFullscreenSwitch.show();
			}
		}
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (TeclaApp.getInstance().isSupportedIMERunning()) {
			if (mVisualOverlay.isVisible()) {
				int event_type = event.getEventType();
				TeclaStatic.logD(CLASS_TAG, AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());

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
//						mVisualOverlay.checkAndUpdateHUDHeight();
					} else if (event_type == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
						TeclaHighlighter.highlightNode(sInstance.mSelectedNode);
						if(mSelectedNode.getClassName().toString().contains("EditText"))
								TeclaApp.ime.showWindow(true);
						//searchAndUpdateNodes();
					} else if (event_type == AccessibilityEvent.TYPE_VIEW_SELECTED) {
						//searchAndUpdateNodes();
					} else if(event_type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
						mPreviousOriginalNode = mOriginalNode;
						mOriginalNode = node;				
						mNodeIndex = 0;
						searchAndUpdateNodes();
					} else if (event_type == AccessibilityEvent.TYPE_VIEW_CLICKED) {
						//searchAndUpdateNodes();
					}
				} else {
					TeclaStatic.logD(CLASS_TAG, "Node is null!");
				}
			}
		}
	}

	private void searchAndUpdateNodes() {
		//		TeclaHighlighter.clearHighlight();
		searchActiveNodesBFS(mOriginalNode);
		
		if (mActiveNodes.size() > 0 ) {
			mSelectedNode = findNeighbourNode(mSelectedNode, DIRECTION_ANY);
			if(mSelectedNode == null) mSelectedNode = mActiveNodes.get(0);
			TeclaHighlighter.highlightNode(mSelectedNode);
			mSelectedNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
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
		removeActiveParents();
	}
	
	private void removeActiveParents() {
		ArrayList<Rect> node_rects = new ArrayList<Rect>();
		AccessibilityNodeInfo node;
		Rect rect;
		int i;
		for(i=0; i<mActiveNodes.size(); ++i) {
			rect = new Rect();
			node = mActiveNodes.get(i);
			node.getBoundsInScreen(rect);
			node_rects.add(rect);
		}
		i=0;
		Rect rect2;
		while(i<node_rects.size()) {
			rect = node_rects.get(i);
			boolean removedANode = false;
			for(int j=0; j<node_rects.size(); ++j) {
				if(i==j) continue;
				rect2 = node_rects.get(j);
				if(rect.contains(rect2)) {
					node_rects.remove(i); 
					mActiveNodes.remove(i);
					removedANode = true;
					break;
				}
			}
			if(!removedANode) ++i;
		}
	}

	private void sortAccessibilityNodes(ArrayList<AccessibilityNodeInfo> nodes) {
		ArrayList<AccessibilityNodeInfo> sorted = new ArrayList<AccessibilityNodeInfo>();
		Rect bounds_unsorted_node = new Rect();
		Rect bounds_sorted_node = new Rect();
		boolean inserted = false; 
		for(AccessibilityNodeInfo node: nodes) {
			if(sorted.size() == 0) sorted.add(node);
			else {
				node.getBoundsInScreen(bounds_unsorted_node);
				inserted = false; 
				for (int i=0; i<sorted.size() && !inserted; ++i) {
					sorted.get(i).getBoundsInScreen(bounds_sorted_node);
					if(bounds_sorted_node.centerY() > bounds_unsorted_node.centerY()) {
						sorted.add(i, node);
						inserted = true;
					} else if (bounds_sorted_node.centerY() == bounds_unsorted_node.centerY()) {
						if(bounds_sorted_node.centerX() > bounds_unsorted_node.centerX()) {
							sorted.add(i, node);
							inserted = true;
						}
					}
				}
				if(!inserted) sorted.add(node);
			}
		}
		nodes.clear();
		nodes = sorted; 
	}

	public static void selectNode(int direction ) {
		selectNode(sInstance.mSelectedNode,  direction );
	}

	public static void selectNode(AccessibilityNodeInfo refnode, int direction ) {
		NodeSelectionThread thread = new NodeSelectionThread(refnode, direction);
		thread.start();	
	}

	private static AccessibilityNodeInfo findNeighbourNode(AccessibilityNodeInfo refnode, int direction) {
		int r2_min = Integer.MAX_VALUE;
		int r2 = 0;
		double ratio_min = Double.MAX_VALUE;
		double ratio = 0;
		double K = 2;
		Rect refOutBounds = new Rect();
		if(refnode == null) return null;
		refnode.getBoundsInScreen(refOutBounds);
		int x = refOutBounds.centerX();
		int y = refOutBounds.centerY();
		int dx, dy;
		Rect outBounds = new Rect();
		AccessibilityNodeInfo result = null; 
		for (AccessibilityNodeInfo node: sInstance.mActiveNodes ) {
			if(refnode.equals(node) && direction != DIRECTION_ANY) continue; 
			node.getBoundsInScreen(outBounds);
			dx = x - outBounds.centerX();
			dy = y - outBounds.centerY();
			r2 = dx*dx + dy*dy;
			switch (direction ) {
			case DIRECTION_UP:
				if(dy <= 0) continue;
				ratio = Math.round(Math.abs(dx/Math.sqrt(r2)*K));
				break;  
			case DIRECTION_DOWN:
				if(dy >= 0) continue;
				ratio = Math.round(Math.abs(dx/Math.sqrt(r2)*K));
				break;  
			case DIRECTION_LEFT:
				if(dx <= 0) continue;
				ratio = Math.round(Math.abs(dy/Math.sqrt(r2)*K));
				break; 
			case DIRECTION_RIGHT:
				if(dx >= 0) continue;
				ratio = Math.round(Math.abs(dy/Math.sqrt(r2)*K));
				break; 
			default: 
				break; 
			}
			if(ratio <= ratio_min) {
				if(ratio < ratio_min) {
					ratio_min = ratio;
					r2_min = r2;
					result = node;					
				} else if(r2 < r2_min) {
					r2_min = r2;
					result = node;						
				}
			}
		}
		if(ratio_min >= 0.95*K) result = null;
		return result;		
	}

	public static void clickActiveNode() {
		if(sInstance.mActiveNodes.size() == 0) return;
		if(sInstance.mSelectedNode == null) sInstance.mSelectedNode = sInstance.mActiveNodes.get(0); 
		sInstance.mSelectedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		if(sInstance.mVisualOverlay.isVisible()) 
			TeclaApp.overlay.clearHighlight();
	}

	//	public static void selectActiveNode(int index) {
	//		if(sInstance.mActiveNodes.size()==0) return; 
	//		sInstance.mNodeIndex = index;
	//		sInstance.mNodeIndex = Math.min(sInstance.mActiveNodes.size() - 1, sInstance.mNodeIndex + 1);
	//		sInstance.mNodeIndex = Math.max(0, sInstance.mNodeIndex - 1);
	//		TeclaHighlighter.updateNodes(sInstance.mActiveNodes.get(sInstance.mNodeIndex));
	//	}
	//
	//	public static void selectPreviousActiveNode() {
	//		if(sInstance.mActiveNodes.size()==0) return; 
	//		sInstance.mNodeIndex = Math.max(0, sInstance.mNodeIndex - 1);
	//		TeclaHighlighter.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
	//
	//	}
	//
	//	public static void selectNextActiveNode() {
	//		if(sInstance.mActiveNodes.size()==0) return;
	//		sInstance.mNodeIndex = Math.min(sInstance.mActiveNodes.size() - 1, sInstance.mNodeIndex + 1);
	//		TeclaHighlighter.updateNodes(sInstance.mOriginalNode, sInstance.mActiveNodes.get(sInstance.mNodeIndex));
	//	}
	//

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED)) {
				handleSwitchEvent(intent.getExtras());
			}
		}
	};

	private boolean isSwitchPressed = false;
	private String[] actions = null;
	private void handleSwitchEvent(Bundle extras) {
		TeclaStatic.logD(CLASS_TAG, "Received switch event.");
		SwitchEvent event = new SwitchEvent(extras);
		if (event.isAnyPressed()) {
			isSwitchPressed = true;
			actions = (String[]) extras.get(SwitchEvent.EXTRA_SWITCH_ACTIONS);
			if(TeclaApp.persistence.isInverseScanningEnabled()) {
				AutomaticScan.startAutoScan();
			}
		} else if(isSwitchPressed) { // on switch released
			isSwitchPressed = false;
			if(TeclaApp.persistence.isInverseScanningEnabled()) {
				if(IMEAdapter.isShowingKeyboard()) IMEAdapter.selectScanHighlighted();
				else TeclaHUDOverlay.selectScanHighlighted();
				AutomaticScan.stopAutoScan();
			} else {
				String action_tecla = actions[0];
				int max_node_index = mActiveNodes.size() - 1;
				switch(Integer.parseInt(action_tecla)) {

				case SwitchEvent.ACTION_NEXT:
					if(IMEAdapter.isShowingKeyboard()) IMEAdapter.scanNext();
					else mVisualOverlay.scanNext();
					break;
				case SwitchEvent.ACTION_PREV:
					if(IMEAdapter.isShowingKeyboard()) IMEAdapter.scanPrevious();
					else mVisualOverlay.scanPrevious();
					break;
				case SwitchEvent.ACTION_SELECT:
					if(IMEAdapter.isShowingKeyboard()) IMEAdapter.selectScanHighlighted();
					else TeclaHUDOverlay.selectScanHighlighted();				
					break;
				case SwitchEvent.ACTION_CANCEL:
					//TODO: Programmatic back key?
				default:
					break;
				}
				if(TeclaApp.persistence.isSelfScanningEnabled())
					AutomaticScan.setExtendedTimer();
			}
			
		}
	}

	@Override
	public void onInterrupt() {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdownInfrastructure();
	}

	/**
	 * Shuts down the infrastructure in case it has been initialized.
	 */
	public void shutdownInfrastructure() {	
		TeclaStatic.logD(CLASS_TAG, "Shutting down infrastructure...");
		if (mBound) unbindService(mConnection);
		SEPManager.stop(getApplicationContext());
		if (mVisualOverlay != null) {
			mVisualOverlay.hide();
		}
		
		if (mFullscreenSwitch != null) {
			if(mFullscreenSwitch.isVisible()) {
				mFullscreenSwitch.hide();
			}
		}
		if (register_receiver_called) {
			unregisterReceiver(mReceiver);
			register_receiver_called = false;
		}
	}

	protected static class NodeSelectionThread extends Thread {
		AccessibilityNodeInfo current_node;
		int direction; 
		public NodeSelectionThread(AccessibilityNodeInfo node, int dir) {
			current_node = node;
			direction = dir;
		}
		public void run() {
			AccessibilityNodeInfo node;
			if(direction == DIRECTION_UP
					&& isFirstScrollNode(current_node) 
					&& !isInsideParent(current_node)) {
				current_node.getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
				return;
			} 
			if(direction == DIRECTION_DOWN	
					&& isLastScrollNode(current_node) 
					&& !isInsideParent(current_node)) {
				current_node.getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
				return;
			} 
			mActionLock.lock();
			node = findNeighbourNode(current_node, direction);		
			if(node != null) {
				sInstance.mSelectedNode = node;
				if(node.getClassName().toString().contains("EditText")) {
					node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
				}
			}
			mActionLock.unlock(); 

			if(node != null)
				sInstance.mSelectedNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
			else {
				switch(direction) {
				case(DIRECTION_UP):
					TeclaApp.ime.sendKey(KeyEvent.KEYCODE_DPAD_UP);
					break;
				case(DIRECTION_DOWN):
					TeclaApp.ime.sendKey(KeyEvent.KEYCODE_DPAD_DOWN);
					break;
				case(DIRECTION_LEFT):
					TeclaApp.ime.sendKey(KeyEvent.KEYCODE_DPAD_LEFT);
					break;
				case(DIRECTION_RIGHT):
					TeclaApp.ime.sendKey(KeyEvent.KEYCODE_DPAD_RIGHT);
					break;					
				}
			}
		}
	}

	public static boolean hasScrollableParent(AccessibilityNodeInfo node) {
		if(node == null) return false;
		AccessibilityNodeInfo parent = node.getParent();
		if (parent != null) {
			if(!parent.isScrollable()) return false;
		}
		return true;
	}

	public static boolean isFirstScrollNode(AccessibilityNodeInfo node) {
		if(!hasScrollableParent(node)) return false;
		AccessibilityNodeInfo parent = node.getParent();
		AccessibilityNodeInfo  firstScrollNode = null;
		for(int i=0; i<parent.getChildCount(); ++i) {
			AccessibilityNodeInfo  aNode = parent.getChild(i);
			if(aNode.isVisibleToUser() && aNode.isClickable()) {
				firstScrollNode = aNode;
				break;
			}
		}

		return isSameNode(node, firstScrollNode);
	}

	public static boolean isLastScrollNode(AccessibilityNodeInfo node) {
		if(!hasScrollableParent(node)) return false;
		AccessibilityNodeInfo parent = node.getParent();
		AccessibilityNodeInfo  lastScrollNode = null;
		for(int i=parent.getChildCount()-1; i>=0; --i) {
			AccessibilityNodeInfo aNode = parent.getChild(i);
			if(aNode.isVisibleToUser() && aNode.isClickable()) {
				lastScrollNode = aNode;
				break;
			}
		}	
		return isSameNode(node, lastScrollNode);
	}

	public static boolean isSameNode(AccessibilityNodeInfo node1, AccessibilityNodeInfo node2) {
		if(node1 == null || node2 == null) return false;
		Rect node1_rect = new Rect(); 
		node1.getBoundsInScreen(node1_rect);	
		Rect node2_rect = new Rect(); 
		node2.getBoundsInScreen(node2_rect);	
		if(node1_rect.left == node2_rect.left
				&& node1_rect.right == node2_rect.right
				&& node1_rect.top == node2_rect.top
				&& node1_rect.bottom == node2_rect.bottom) 
			return true;
		return false;
	}
	
	public static boolean isInsideParent(AccessibilityNodeInfo node) {
		if(node == null) return false;
		AccessibilityNodeInfo parent = node.getParent();
		if(parent == null) return false;
		Rect node_rect = new Rect();
		Rect parent_rect = new Rect();
		node.getBoundsInScreen(node_rect);
		parent.getBoundsInScreen(parent_rect);
		if(parent_rect.contains(node_rect)) return true;
		return false;
	}

	public void sendGlobalBackAction() {
		sInstance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
	}

	public void sendGlobalHomeAction() {
		sInstance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);		
	}	

	public void sendGlobalNotificationAction() {
		sInstance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);		
	}	

	public void injectSwitchEvent(SwitchEvent event) {
		switch_event_provider.injectSwitchEvent(event);
	}

	public void injectSwitchEvent(int switchChanges, int switchStates) {
		switch_event_provider.injectSwitchEvent(switchChanges, switchStates);
	}

	SwitchEventProvider switch_event_provider;
	boolean mBound = false;

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			LocalBinder binder = (LocalBinder) service;
			switch_event_provider = binder.getService();
			mBound = true;
			TeclaStatic.logD(CLASS_TAG, "IME bound to SEP");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;

		}
	};
}
