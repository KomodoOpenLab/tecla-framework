package com.android.tecla.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public class TeclaIME extends InputMethodService {

	private static TeclaIME sInstance;

	@Override
	public void onCreate() {
		super.onCreate();		
		sInstance = this;
	}
	
	public static TeclaIME getInstance() {
		return sInstance;
	}

	@Override
	public void onStartInputView(EditorInfo info, boolean restarting) {
		super.onStartInputView(info, restarting);
	}

	@Override
	public void onFinishInputView(boolean finishingInput) {
		IMEAdapter.setKeyboardView(null);
		super.onFinishInputView(finishingInput);
	}
	
	
}
