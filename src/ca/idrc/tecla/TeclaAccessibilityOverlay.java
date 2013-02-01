package ca.idrc.tecla;

import android.content.Context;

public class TeclaAccessibilityOverlay extends SimpleOverlay {

    private static TeclaAccessibilityOverlay sInstance;
    
	public TeclaAccessibilityOverlay(Context context) {
		super(context);
	}

	@Override
	protected void onShow() {
		sInstance = this;
	}

	@Override
	protected void onHide() {
        sInstance = null;
	}
	

}
