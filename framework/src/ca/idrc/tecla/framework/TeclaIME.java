package ca.idrc.tecla.framework;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;

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
	
	public void pressBackKey() {
		sendDownUpKeyEvents(KeyEvent.KEYCODE_BACK);
	}
}
