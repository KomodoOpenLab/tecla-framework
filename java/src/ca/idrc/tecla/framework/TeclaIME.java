package ca.idrc.tecla.framework;

import ca.idrc.tecla.TeclaHUDController;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;

public class TeclaIME extends InputMethodService {
	
	@Override
	public void onCreate() {
		super.onCreate();
		TeclaHUDController.sLatinIMEInstance = this;
		
	}    
	
	public void pressBackKey() {
		sendDownUpKeyEvents(KeyEvent.KEYCODE_BACK);
	}
}
