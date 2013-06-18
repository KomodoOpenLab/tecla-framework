package com.android.tecla.addon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityNodeInfo;
import ca.idrc.tecla.highlighter.TeclaHighlighter;

public class TeclaVisualOverlay {

	private Context mContext;
	private TeclaHighlighter mHighlighter;
	private TeclaHUDOverlay mHUD;
	
	public TeclaVisualOverlay(Context context) {
		mContext = context;
		mHighlighter = new TeclaHighlighter(context);
		mHUD = new TeclaHUDOverlay(context);
	}
	
	public void show() {
		mHighlighter.show();
		mHUD.show();
	}
	
	public void hide() {
		mHighlighter.hide();
		mHUD.hide();
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
		show();
	}

	public void hidePreviewHUD() {
		mHUD.setPreviewHUD(false);
		hide();
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
