package ca.idrc.tecla;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaHUDController extends SimpleOverlay {

	protected final TeclaHUD mHUD;
	private static TeclaHUDController sInstance;
	
	public TeclaHUDController(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		setParams(params);

		setContentView(R.layout.tecla_controller);

		mHUD = (TeclaHUD) findViewById(R.id.teclaHUD_control);
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
	
	private View.OnClickListener mOverlayClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mHUD.scanTrigger();
			
		}
	};	

	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			Log.v("TeclaA11y", "Long clicked.  ");
			TeclaAccessibilityService.getInstance().shutdownInfrastructure();
			return true;
		}
	};

}
