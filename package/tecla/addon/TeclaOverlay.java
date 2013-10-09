package com.android.tecla.addon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityNodeInfo;
import ca.idrc.tecla.framework.TeclaStatic;
import ca.idrc.tecla.highlighter.TeclaHighlighter;

public class TeclaOverlay {

	/**
	 * Tag used for logging in the whole framework
	 */
	public static final String CLASS_TAG = "TeclaOverlay";

	private Context mContext;
	private TeclaHighlighter mHighlighter;
	private TeclaHUD mHUD;
	
	public TeclaOverlay(Context context) {
		mContext = context;
		mHighlighter = new TeclaHighlighter(context);
		mHUD = new TeclaHUD(context);
		TeclaStatic.logD(CLASS_TAG, "Overlay created");
	}
	
	public void showAll() {
		showHighlighter();
		showHUD();
	}
	
	public void hideAll() {
		hideHUD();
		hideHighlighter();
	}
	
	public void showHUD() {
		mHUD.show();
	}
	
	private void showHighlighter() {
		mHighlighter.show();
	}
	
	public void hideHUD() {
		mHUD.hide();
	}
	
	private void hideHighlighter() {
		mHighlighter.hide();
	}
	
	public boolean isVisible() {
		return mHighlighter.isVisible() && mHUD.isVisible();
	}
	
	public boolean isPreview() {
		return mHUD.isPreview();
	}
	
	public void scanNextHUDButton() {
		mHUD.scanNext();
	}

	public void showPreviewHUD() {
		mHUD.setPreviewHUD(true);
		showAll();
	}

	public void hidePreviewHUD() {
		mHUD.setPreviewHUD(false);
		hideAll();
	}

	public void highlightNode(AccessibilityNodeInfo selectedNode) {
		mHighlighter.highlightNode(selectedNode);	
	}
	
	public void clearHighlight() {
		mHighlighter.clearHighlight();
	}
	
	public void scanNext() {
		mHUD.scanNext();
	}
	
	public void scanPrevious() {
		mHUD.scanPrevious();
	}

//	public void checkAndUpdateHUDHeight() {
//		if(mHUDController.isVisible())
//			mHUDController.didStatusBarVisibilityChange();
//	}
}
