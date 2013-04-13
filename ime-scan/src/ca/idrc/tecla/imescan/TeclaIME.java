package ca.idrc.tecla.imescan;

import ca.idrc.tecla.touchinterface.SingleSwitchInterface;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public class TeclaIME extends InputMethodService {

	private static TeclaIME sInstance;
	private SingleSwitchInterface mSSI = null;

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
		if(mSSI == null) {
			mSSI = new SingleSwitchInterface(this);	
			mSSI.show();
		}		
		super.onStartInputView(info, restarting);
	}

	@Override
	public void onFinishInputView(boolean finishingInput) {
		if(mSSI != null) {
			mSSI.hide();
			mSSI = null;
		}
		super.onFinishInputView(finishingInput);
	}
	
	
}
