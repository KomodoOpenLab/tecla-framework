package com.android.tecla.addon;

import android.content.Context;
import ca.idrc.tecla.highlighter.TeclaHighlighter;

public class TeclaVisualOverlay {

	TeclaHighlighter mHighlighter;
	TeclaHUDOverlay mHUDController;
	
	public TeclaVisualOverlay(Context context) {
		mHighlighter = new TeclaHighlighter(context);
		mHUDController = new TeclaHUDOverlay(context);
	}
	
	public void show() {
		mHighlighter.show();
		mHUDController.show();
	}
	
	public void hide() {
		mHighlighter.hide();
		mHUDController.hide();
	}
	
	public boolean isVisible() {
		return mHighlighter.isVisible() && mHUDController.isVisible();
	}
	
	public boolean isPreview() {
		return mHUDController.isPreview();
	}
}
