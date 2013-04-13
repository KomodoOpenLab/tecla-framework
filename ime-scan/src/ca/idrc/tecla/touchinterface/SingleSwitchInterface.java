package ca.idrc.tecla.touchinterface;

import com.example.android.softkeyboard.R;

import android.content.Context;
import android.view.WindowManager;

public class SingleSwitchInterface extends SimpleOverlay {

    private static SingleSwitchInterface sInstance;

	public SingleSwitchInterface(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		// params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;		
		setParams(params);
		
		setContentView(R.layout.switch_interface);
	}

	@Override
	protected void onShow() {
		sInstance = this;
	}

	@Override
	protected void onHide() {
        sInstance = null;
	}
	

    public static void removeInvalidNodes() {
        if (sInstance == null) {
            return;
        }
    }
}
