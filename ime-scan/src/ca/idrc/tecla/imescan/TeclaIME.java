package ca.idrc.tecla.imescan;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
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
	
}
