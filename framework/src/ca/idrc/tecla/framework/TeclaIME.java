package ca.idrc.tecla.framework;

import android.inputmethodservice.InputMethodService;

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
