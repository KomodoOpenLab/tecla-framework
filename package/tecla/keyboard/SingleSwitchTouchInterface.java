package com.android.tecla.keyboard;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import ca.idrc.tecla.framework.SimpleOverlay;
import ca.idrc.tecla.framework.TeclaStatic;

public class SingleSwitchTouchInterface extends SimpleOverlay {

	/**
	 * Tag used for logging in the whole framework
	 */
	public static final String CLASS_TAG = "SingleSwitchTouchInterface";
	private static SingleSwitchTouchInterface sInstance;

	public SingleSwitchTouchInterface(Context context) {
		super(context);

		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		setParams(params);

		View rView = getRootView();
		rView.setOnLongClickListener(mOverlayLongClickListener);
		rView.setOnClickListener(mOverlayClickListener);
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
			KeyguardManager kgMgr = 
					(KeyguardManager) sInstance.getContext().getSystemService(Context.KEYGUARD_SERVICE);
			boolean showing = kgMgr.inKeyguardRestrictedInputMode();
			
			if(showing) {
				KeyguardLock newKeyguardLock = kgMgr.newKeyguardLock(null);
				newKeyguardLock.disableKeyguard();
				/*WindowManager.LayoutParams params = sInstance.getParams();
				params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
				params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
				params.flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
				sInstance.setParams(params);*/
				Toast.makeText(sInstance.getContext().getApplicationContext(), "Unlocked screen", Toast.LENGTH_LONG).show();
			} else {
				if(IMEAdapter.isShowingKeyboard()) IMEAdapter.selectScanHighlighted();
				else TeclaHUDOverlay.selectScanHighlighted();				
			}
		
				
		}
	};	

	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			TeclaStatic.logV(CLASS_TAG, "Long clicked.  ");
			TeclaApp.persistence.setLongClicked(true);
			TeclaAccessibilityService.getInstance().shutdownInfrastructure();
			return true;
		}
	};

}
