package ca.idrc.tecla.imescan;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public class TeclaIME extends InputMethodService {

	private static TeclaIME sInstance;
	private static IMEScanner sIMEScannner = null;

	@Override
	public void onCreate() {
		super.onCreate();		
		sInstance = this;
		sIMEScannner = new IMEScanner();
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
