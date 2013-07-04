package com.android.tecla.addon;

import com.android.inputmethod.keyboard.KeyboardView;

public interface TeclaIMEAdapter {

	public void scanDown();
	public void scanLeft();
	public void scanRight();
	public void scanUp();
	
	public void scanNext();
	public void scanPrevious();
	public void stepOut();
	public void selectScanHighlighted();
	
	public boolean isShowingKeyboard();
	
	public boolean setKeyboardView(KeyboardView kbv);
	
}
