package com.android.tecla;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import com.android.tecla.ServiceSwitchEventProvider.SwitchEventProviderBinder;

import ca.idi.tecla.sdk.SwitchEvent;
import ca.idi.tecla.sdk.SEPManager;
import ca.idrc.tecla.R;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ServiceAccessibility extends AccessibilityService {

	private final static String CLASS_TAG = "TeclaA11yService";

	private final static String EDITTEXT_CLASSNAME = "android.widget.EditText";
	
	public final static int DIRECTION_UP = 0;
	public final static int DIRECTION_LEFT = 1;
	public final static int DIRECTION_RIGHT = 2;
	public final static int DIRECTION_DOWN = 3;
	private final static int DIRECTION_ANY = 8;

	private static ServiceAccessibility sInstance;

	private Boolean register_receiver_called;

	private AccessibilityNodeInfo mOriginalNode, mPreviousOriginalNode;
	protected AccessibilityNodeInfo mSelectedNode;

	private ArrayList<AccessibilityNodeInfo> mActiveNodes;
	private int mNodeIndex;

	private OverlayHUD mHUD;
	private OverlayHighlighter mHighlighter;
	private OverlaySwitch mSwitch;

	protected static ReentrantLock mActionLock;
	
	private final static String MAP_VIEW = "android.view.View";
	// For later use for custom actions 
	//private final static String WEB_VIEW = "Web View";
	
	@Override
	public void onCreate() {
		TeclaStatic.logD(CLASS_TAG, "Service created");

		init();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();

		TeclaStatic.logD(CLASS_TAG, "Service connected");

	}

	private void init() {
		sInstance = this;
		register_receiver_called = false;

		mActiveNodes = new ArrayList<AccessibilityNodeInfo>();
		mActionLock = new ReentrantLock();

		mHighlighter = new OverlayHighlighter(this);
		mHUD = new OverlayHUD(this);
		
		if (mSwitch == null) {
			mSwitch = new OverlaySwitch(this);
			//TeclaApp.setFullscreenSwitch(mFullscreenSwitch);		
		}

		// Bind to SwitchEventProvider
		Intent intent = new Intent(this, ServiceSwitchEventProvider.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		registerReceiver(mReceiver, new IntentFilter(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED));
		register_receiver_called = true;
		
		SEPManager.start(this);

		//mOverlayHighlighter.show();
		//mOverlayHUD.show();
		//mFullscreenSwitch.show();
		//mTeclaOverlay.hide();
		//mFullscreenSwitch.hide();
		
		TeclaApp.setA11yserviceInstance(this);
	}
	
	public OverlayHUD getHUD() {
		return mHUD;
	}
	
	public OverlayHighlighter getHighlighter() {
		return mHighlighter;
	}
	
	public void setFullscreenSwitchLongClick(boolean enabled) {
		if(mSwitch != null)
			mSwitch.setLongClick(enabled);
	}
	
	private void showHighlighter() {
		if (mHighlighter != null) {
			if (!mHighlighter.isVisible()) {
				mHighlighter.show();
			}
		}
	}
	
	private void hideHighlighter() {
		if (mHighlighter != null) {
			if (mHighlighter.isVisible()) {
				mHighlighter.hide();
			}
		}
	}
	
	private void showHUD() {
		if (mHUD != null) {
			if (!mHUD.isVisible()) {
				mHUD.show();
			}
		}
	}
	
	private void hideHUD() {
		if (mHUD != null) {
			if (mHUD.isVisible()) {
				mHUD.hide();
			}
		}
	}
	
	public void showSwitch() {
		if (mSwitch != null) {
			if (!mSwitch.isVisible()) {
				mSwitch.show();
			}
		}
	}
	
	public void hideSwitch() {
		if (mSwitch != null) {
			if (mSwitch.isVisible()) {
				mSwitch.hide();
			}
		}
	}
	
	private void showAll() {
		showFeedback();
		showSwitch();
	}
	
	public boolean isFeedbackVisible() {
		return (mHighlighter.isVisible() && mHUD.isVisible());
	}
	
	public void enableScreenSwitch() {
		TeclaApp.persistence.setSelfScanningSelected(true);
		if(!TeclaApp.persistence.isInverseScanningSelected())
			ManagerAutoScan.start();
		showAll();
		sendGlobalHomeAction();
		TeclaApp.persistence.setScreenSwitchSelected(true);
	}

	public void disableScreenSwitch() {
		mSwitch.getRootView().setBackgroundResource(R.drawable.screen_switch_background_normal);
		mSwitch.getRootView().invalidate();
		if (TeclaApp.ime != null) TeclaApp.ime.hideWindow();
		TeclaApp.persistence.setScreenSwitchSelected(false);
		mSwitch.hide();
		hideFeedback();
	}

	public void showFeedback() {
		showHighlighter();
		showHUD();
	}
	
	public void hideFeedback() {
		ManagerAutoScan.stop();
		hideHighlighter();
		hideHUD();
	}
	
/*	public void turnFullscreenOn() {
		TeclaApp.persistence.setSelfScanningEnabled(true);
		if(!TeclaApp.persistence.isInverseScanningEnabled())
			AutoScanManager.start();
		showAll();
		//showFullscreenSwitch();
		sendGlobalHomeAction();
		//TeclaApp.persistence.setFullscreenEnabled(true);
	}
	
	public void turnFullscreenOff() {
		TeclaApp.a11yservice.hideFullscreenSwitch();
		TeclaApp.persistence.setSelfScanningEnabled(false);
		AutoScanManager.stop();				
		//TeclaApp.overlay.hideAll();
		TeclaApp.persistence.setFullscreenEnabled(false);
//		if(TeclaApp.settingsactivity != null) {
//			TeclaApp.settingsactivity.uncheckFullScreenMode();
//		}
	}
	
*/
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (TeclaApp.getInstance().isSupportedIMERunning()) {
			if (isFeedbackVisible()) {
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
						AccessibilityNodeInfo selectednode = findSelectedNode();
						if(selectednode != null && selectednode.getParent().isScrollable()) {
							mSelectedNode = selectednode;
							mHighlighter.highlightNode(mSelectedNode);
						}
//						mVisualOverlay.checkAndUpdateHUDHeight();
					} else if (event_type == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
						mSelectedNode = node;
						mHighlighter.highlightNode(mSelectedNode);
						if(mSelectedNode.getClassName().toString().contains(EDITTEXT_CLASSNAME))
								TeclaApp.ime.showWindow(true);
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
					mSelectedNode=sInstance.getRootInActiveWindow();
					TeclaStatic.logD(CLASS_TAG, "Node is null!");
				}
			}
		}
	}

	private AccessibilityNodeInfo findSelectedNode() {
		AccessibilityNodeInfo result = null;
		for (AccessibilityNodeInfo node: mActiveNodes) {
			if(node.isSelected()) {
				if(result == null)
					result = node;
				else {
					Rect node_rect = new Rect();
					Rect result_rect = new Rect();
					node.getBoundsInScreen(node_rect);
					result.getBoundsInScreen(result_rect);
					if(node_rect.contains(result_rect))
						result = node;
				}
			}
		}
		return result;
	}
		
	private AccessibilityNodeInfo mFocusedNode;
	private void searchAndUpdateNodes() {
		//		TeclaHighlighter.clearHighlight();
		searchActiveNodesBFS(mOriginalNode);
		
		if (mActiveNodes.size() > 0 ) {
			mSelectedNode = findNeighbourNode(mSelectedNode, DIRECTION_ANY);
			if(mSelectedNode == null) mSelectedNode = mActiveNodes.get(0);
			if(mFocusedNode != null) {
				mSelectedNode = mFocusedNode;
			}
			mHighlighter.highlightNode(mSelectedNode);
			if(mPreviousOriginalNode != null) 
				mPreviousOriginalNode.recycle();
		}
	}

	private void searchActiveNodesBFS(AccessibilityNodeInfo node) {
		mActiveNodes.clear();
		mFocusedNode = null;
		Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
		q.add(node);
		while (!q.isEmpty()) {
			AccessibilityNodeInfo thisnode = q.poll();
			if(thisnode == null) continue;
			if(isActive(thisnode) && !thisnode.isScrollable()) {
				mActiveNodes.add(thisnode);
				if(thisnode.isFocused())
					mFocusedNode = thisnode;
			}
			for (int i=0; i<thisnode.getChildCount(); ++i) q.add(thisnode.getChild(i));
		}
		//removeActiveParents();
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

	public void selectNode(int direction ) {
		selectNode(sInstance.mSelectedNode,  direction );
	}

	public void selectNode(AccessibilityNodeInfo refnode, int direction ) {
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

	public void clickActiveNode() {
		if(sInstance.mActiveNodes.size() == 0) return;
		if(sInstance.mSelectedNode == null) sInstance.mSelectedNode = sInstance.mActiveNodes.get(0);
		
		// Use to find out view type for custom actions
		//Log.i("NODE TO STRING"," " + sInstance.mSelectedNode.toString());
		
		sInstance.mSelectedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		if(isFeedbackVisible()) 
			mHighlighter.clearHighlight();
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
			if(TeclaApp.persistence.isInverseScanningSelected()) {
				ManagerAutoScan.start();
			}
		} else if(isSwitchPressed) { // on switch released
			isSwitchPressed = false;
			if(TeclaApp.persistence.isInverseScanningSelected()) {
				if(AdapterInputMethod.isShowingKeyboard()) AdapterInputMethod.selectScanHighlighted();
				else selectHighlighted();
				ManagerAutoScan.stop();
			} else {
				String action_tecla = actions[0];
				int max_node_index = mActiveNodes.size() - 1;
				switch(Integer.parseInt(action_tecla)) {

				case SwitchEvent.ACTION_NEXT:
					if(AdapterInputMethod.isShowingKeyboard()) AdapterInputMethod.scanNext();
					else mHUD.scanNext();
					break;
				case SwitchEvent.ACTION_PREV:
					if(AdapterInputMethod.isShowingKeyboard()) AdapterInputMethod.scanPrevious();
					else mHUD.scanPrevious();
					break;
				case SwitchEvent.ACTION_SELECT:
					if(AdapterInputMethod.isShowingKeyboard()) AdapterInputMethod.selectScanHighlighted();
					else selectHighlighted();				
					break;
				case SwitchEvent.ACTION_CANCEL:
					//TODO: Programmatic back key?
				default:
					break;
				}
				if(TeclaApp.persistence.isSelfScanningSelected())
					ManagerAutoScan.setExtendedTimer();
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

	private void selectHighlighted() {
		AccessibilityNodeInfo node = TeclaApp.a11yservice.mSelectedNode;
		AccessibilityNodeInfo parent = null;
		if(node != null) parent = node.getParent();
		int actions = 0;
		if(parent != null) actions = node.getParent().getActions();
		
		if(mHUD.getPage() == 0) {
			switch (mHUD.getIndex()){
			case OverlayHUD.HUD_BTN_TOP:
				if(isActiveScrollNode(node) 
						&& (actions & AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) 
						== AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
					parent.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
				} else
					TeclaApp.a11yservice.selectNode(ServiceAccessibility.DIRECTION_UP);
				break;
			case OverlayHUD.HUD_BTN_BOTTOM:
				if(isActiveScrollNode(node)
						&& (actions & AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) 
						== AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
					node.getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
				} else 
					TeclaApp.a11yservice.selectNode(ServiceAccessibility.DIRECTION_DOWN);
				break;
			case OverlayHUD.HUD_BTN_LEFT:
				TeclaApp.a11yservice.selectNode(ServiceAccessibility.DIRECTION_LEFT);
				break;
			case OverlayHUD.HUD_BTN_RIGHT:
				TeclaApp.a11yservice.selectNode(ServiceAccessibility.DIRECTION_RIGHT);
				break;
			case OverlayHUD.HUD_BTN_TOPRIGHT:
				TeclaApp.a11yservice.clickActiveNode();
				break;
			case OverlayHUD.HUD_BTN_BOTTOMLEFT:
				TeclaApp.a11yservice.sendGlobalBackAction();
				/*if(Persistence.isDefaultIME(mContext) && TeclaApp.persistence.isIMERunning()) {
					TeclaStatic.logI(CLASS_TAG, "LatinIME is active");
					TeclaApp.ime.pressBackKey();
				} else TeclaStatic.logW(CLASS_TAG, "LatinIME is not active!");*/
				break;
			case OverlayHUD.HUD_BTN_TOPLEFT:
				TeclaApp.a11yservice.sendGlobalNotificationAction();
				/*if(Persistence.isDefaultIME(mContext) && TeclaApp.persistence.isIMERunning()) {
					TeclaStatic.logI(CLASS_TAG, "LatinIME is active");
					TeclaApp.ime.pressHomeKey();
				} else TeclaStatic.logW(CLASS_TAG, "LatinIME is not active!");*/
				break;
			case OverlayHUD.HUD_BTN_BOTTOMRIGHT:
				mHUD.turnPage();
				break;
			}
		} else if(mHUD.getPage() == 1) {
			switch (mHUD.getIndex()){
			case OverlayHUD.HUD_BTN_TOP:
			case OverlayHUD.HUD_BTN_BOTTOM:
			case OverlayHUD.HUD_BTN_LEFT:
			case OverlayHUD.HUD_BTN_RIGHT:
			case OverlayHUD.HUD_BTN_TOPRIGHT:
			case OverlayHUD.HUD_BTN_BOTTOMLEFT:
				break;
			case OverlayHUD.HUD_BTN_TOPLEFT:
				TeclaApp.a11yservice.sendGlobalHomeAction();
				break;
			case OverlayHUD.HUD_BTN_BOTTOMRIGHT:
				mHUD.turnPage();
				break;
			}
		}
		
		if(TeclaApp.persistence.isSelfScanningSelected())
			ManagerAutoScan.resetTimer();

	}
	
	/**
	 * Shuts down the infrastructure in case it has been initialized.
	 */
	public void shutdownInfrastructure() {	
		TeclaStatic.logD(CLASS_TAG, "Shutting down infrastructure...");
		if (mBound) unbindService(mConnection);
		SEPManager.stop(getApplicationContext());
		hideFeedback();
		
		if (mSwitch != null) {
			if(mSwitch.isVisible()) {
				mSwitch.hide();
			}
		}
		if (register_receiver_called) {
			unregisterReceiver(mReceiver);
			register_receiver_called = false;
		}
	}

	protected class NodeSelectionThread extends Thread {
		AccessibilityNodeInfo current_node;
		int direction; 
		public NodeSelectionThread(AccessibilityNodeInfo node, int dir) {
			current_node = node;
			direction = dir;
		}
		public void run() {
			if(hasScrollableParent(current_node)) {
				navigateWithDPad(direction);
				return;
			}
			AccessibilityNodeInfo node;
			mActionLock.lock();
			node = findNeighbourNode(current_node, direction);
			
			if(node != null) {
				if (sInstance.mSelectedNode.toString().contains(MAP_VIEW)){
					navigateWithDPad(direction);
				}else{
					mHighlighter.highlightNode(node);
				if(node.isFocusable()) 
					node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
				sInstance.mSelectedNode = node;
				}
				
			} else {
				navigateWithDPad(direction);
			}
			mActionLock.unlock(); 
		}
	}

	private static void navigateWithDPad(int direction) {
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
	
	public static boolean hasScrollableParent(AccessibilityNodeInfo node) {
		if(node == null) return false;
		AccessibilityNodeInfo parent = node.getParent();
		if (parent != null) {
			if(!parent.isScrollable()) return false;
		}
		return true;
	}

//	public boolean isFirstActiveScrollNode(AccessibilityNodeInfo node) {
//		if(!hasScrollableParent(node)) return false;
//		AccessibilityNodeInfo parent = node.getParent();
//		AccessibilityNodeInfo  activeNode = null;
//		for(int i=0; i < parent.getChildCount()-1; ++i) {
//			AccessibilityNodeInfo  aNode = parent.getChild(i);
//			if(isActive(aNode)) {
//				activeNode = aNode;
//				break;
//			}
//		}
//
//		return isSameNode(node, activeNode);
//	}

	public boolean isActiveScrollNode(AccessibilityNodeInfo node) {
		if(node == null) return false;
		return (hasScrollableParent(node) && isActive(node))? true:false;
	}

//	public boolean isLastActiveScrollNode(AccessibilityNodeInfo node) {
//		if(!hasScrollableParent(node)) return false;
//		AccessibilityNodeInfo parent = node.getParent();
//		AccessibilityNodeInfo  lastScrollNode = null;
//		for(int i=parent.getChildCount()-1; i>=0; --i) {
//			AccessibilityNodeInfo aNode = parent.getChild(i);
//			if(isActive(aNode)) {
//				lastScrollNode = aNode;
//				break;
//			}
//		}	
//		return isSameNode(node, lastScrollNode);
//	}
	
	private boolean isActive(AccessibilityNodeInfo node) {
		return (node.isVisibleToUser() && node.isClickable())? true:false;
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

	ServiceSwitchEventProvider switch_event_provider;
	boolean mBound = false;

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			SwitchEventProviderBinder binder = (SwitchEventProviderBinder) service;
			switch_event_provider = binder.getService();
			mBound = true;
			TeclaStatic.logD(CLASS_TAG, "Ally service bound to SEP");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;

		}
	};

}
