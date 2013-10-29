package com.android.tecla;

import ca.idrc.tecla.R;
import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import ca.idrc.tecla.framework.SimpleOverlay;
import ca.idi.tecla.sdk.*;

public class OverlaySwitch extends SimpleOverlay {

	/**
	 * Tag used for logging in the whole framework
	 */
	public static final String CLASS_TAG = "SingleSwitchTouchInterface";
	private static OverlaySwitch sInstance;

	public OverlaySwitch(Context context) {
		super(context);

		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		setParams(params);

		View rView = getRootView();
		rView.setBackgroundResource(R.drawable.screen_switch_background_normal);
		rView.setOnTouchListener(mOverlayTouchListener);
		
		if(!TeclaApp.persistence.isInverseScanningSelected()) 
			setLongClick(true);
		
	}

	@Override
	protected void onShow() {
		sInstance = this;
	}

	@Override
	protected void onHide() {
		sInstance = null;
	}

	/**
	 * Listener for full-screen switch actions
	 */
	private View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {
		
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				sInstance.getRootView().setBackgroundResource(R.drawable.screen_switch_background_pressed);
				TeclaApp.a11yservice.injectSwitchEvent(
						new SwitchEvent(SwitchEvent.MASK_SWITCH_E1, 0)); //Primary switch pressed
				// if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Fullscreen switch down!");
				break;
			case MotionEvent.ACTION_UP:
				sInstance.getRootView().setBackgroundResource(R.drawable.screen_switch_background_normal);
				TeclaApp.a11yservice.injectSwitchEvent(
						new SwitchEvent(0,0)); //Switches released
				// if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Fullscreen switch up!");
				break;
			default:
				break;
			}
			return false;
		}
	};

	public void setLongClick(boolean enabled) {
		View rView = getRootView();
		if(enabled) rView.setOnLongClickListener(mOverlayLongClickListener);
		else rView.setOnLongClickListener(null);
	}
	
	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			TeclaStatic.logV(CLASS_TAG, "Long clicked.  ");
			TeclaApp.a11yservice.disableScreenSwitch();
			return true;
		}
	};

}
