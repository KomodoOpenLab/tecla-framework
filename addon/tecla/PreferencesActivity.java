package com.android.tecla;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.ScanSpeedDialog;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class PreferencesActivity extends Activity {

	private static final String CLASS_TAG = "TeclaSettingsActivity2";

	private static PreferencesActivity sInstance;
	//private TeclaShieldConnect mTeclaShieldManager;
	private PreferencesFragment mPreferenceFragment;
	
	private ScanSpeedDialog mScanSpeedDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		
		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);

		initOnboarding();
		//TeclaApp.setSettingsActivityInstance(this);

//		if(mTeclaShieldManager == null)
//			mTeclaShieldManager = new TeclaShieldManager(this);
		
		mPreferenceFragment = (PreferencesFragment) getFragmentManager()
				.findFragmentById(R.id.tecla_prefs_frag);
		
		sInstance = this;
	}

	private void initOnboarding() {
		if (!TeclaStatic.isDefaultIMESupported(getApplicationContext()) ||
				!TeclaApp.getInstance().isAccessibilityServiceRunning()) {
			DialogOnboarding.createInstance(this, mOnboardingClickListener).show();
		} 
	}


	public void showScanSpeedDialog() {
		mScanSpeedDialog.show();

	}

	private View.OnClickListener mOnboardingClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch(id) {
			case R.id.ime_cancel_btn:
			case R.id.a11y_cancel_btn:
				DialogOnboarding.getInstance().dismiss();
				finish();
				break;
			case R.id.a11y_ok_btn:
				Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		initOnboarding();	
		mPreferenceFragment.onResumeSettingsActivityUpdatePrefs();
	}
	
	@Override
	protected void onDestroy() {

		//TeclaApp.setSettingsActivityInstance(null);
		super.onDestroy();
	}

//	@Override
//	public void onBluetoothActivation() {
//		TeclaApp.shieldmanager.discoverShield();
//		showDiscoveryDialog();
//	}

	public void uncheckFullScreenMode() {
		mPreferenceFragment.uncheckFullScreenMode();
	}

}
