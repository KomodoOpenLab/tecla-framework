package ca.idrc.tecla.framework;

import ca.idrc.tecla.hud.TeclaHUDOverlay;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;

public class TeclaIME extends InputMethodService {
	
	@Override
	public void onCreate() {
		super.onCreate();
		TeclaHUDOverlay.sLatinIMEInstance = this;
		
	}    

	public void pressHomeKey() {
		sendDownUpKeyEvents(KeyEvent.KEYCODE_HOME);
	}

	public void pressBackKey() {
		sendDownUpKeyEvents(KeyEvent.KEYCODE_BACK);
	}
}
