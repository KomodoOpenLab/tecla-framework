package ca.idrc.tecla.touchinterface;

import ca.idrc.tecla.imescan.IMEScanner;

import com.example.android.softkeyboard.R;

import android.content.Context;
import android.view.View;
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
		
		getRootView().setOnLongClickListener(mOverlayLongClickListener);
		getRootView().setOnClickListener(mOverlayClickListener);
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
    
	private View.OnClickListener mOverlayClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			IMEScanner.activateInput(0x1234);
		}
	};	


	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			return true;
		}
	};

}
