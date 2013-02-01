package ca.idrc.tecla;

import android.content.Context;
import android.view.WindowManager;

public class TeclaAccessibilityOverlay extends SimpleOverlay {

    private static TeclaAccessibilityOverlay sInstance;
    
	public TeclaAccessibilityOverlay(Context context) {
		super(context);
		
		 final WindowManager.LayoutParams params = getParams();
		 params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		 params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		 params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		 // params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		 setParams(params);
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
