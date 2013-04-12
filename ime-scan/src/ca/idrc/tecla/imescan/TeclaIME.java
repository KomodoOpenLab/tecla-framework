package ca.idrc.tecla.imescan;

import android.inputmethodservice.InputMethodService;

public class TeclaIME extends InputMethodService {

	private static TeclaIME sInstancce;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstancce = this;
	}
	
	
	
}
