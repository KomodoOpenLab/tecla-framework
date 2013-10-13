package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.ScanSpeedDialog;
import ca.idrc.tecla.framework.TeclaStatic;
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

public class TeclaSettingsActivity extends Activity{

	private static final String CLASS_TAG = "TeclaSettingsActivity2";

	private static TeclaSettingsActivity sInstance;
	
	private static TeclaPreferenceFragment mPreferenceFragment;
	
	private ScanSpeedDialog mScanSpeedDialog;

	ProgressDialog mProgressDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tecla_settings);
		
		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);

		

		initOnboarding();
		TeclaApp.setSettingsActivityInstance(this);

		mProgressDialog = new ProgressDialog(this);
		
		mPreferenceFragment = (TeclaPreferenceFragment) getFragmentManager()
				.findFragmentById(R.id.tecla_prefs_frag);
		
		sInstance = this;
	}

	public static TeclaSettingsActivity getInstance() {
		return sInstance;
	}
	
	private void initOnboarding() {
		if (!TeclaStatic.isDefaultIMESupported(getApplicationContext()) ||
				!TeclaApp.getInstance().isTeclaA11yServiceRunning()) {
			OnboardingDialog.createInstance(this, mOnboardingClickListener).show();
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
				OnboardingDialog.getInstance().dismiss();
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

		TeclaApp.setSettingsActivityInstance(null);
		super.onDestroy();
	}

	public void uncheckFullScreenMode() {
		mPreferenceFragment.uncheckFullScreenMode();
	}
	
	public static TeclaPreferenceFragment getFragment(){
		return mPreferenceFragment;
	}
	
	public void showDiscoveryDialog() {
		mProgressDialog.setMessage(getString(R.string.searching_for_shields));
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				TeclaApp.getInstance().mTeclaShieldManager.cancelDiscovery();
				TeclaStatic.logD(CLASS_TAG, "Tecla Shield discovery cancelled");
				TeclaApp.getInstance().showToast(R.string.shield_connection_cancelled);
				TeclaSettingsActivity.getFragment().onCancelDiscoveryDialogUpdatePrefs();
			}
		});
		mProgressDialog.show();
	}
	
	
	/*
	 * Dismisses progress dialog without triggerint it's OnCancelListener
	 */
	public void dismissDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
	

}
