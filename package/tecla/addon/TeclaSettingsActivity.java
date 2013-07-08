package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.ScanSpeedDialog;
import ca.idrc.tecla.framework.TeclaStatic;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class TeclaSettingsActivity extends Activity
	implements TeclaShieldActionListener {

	private static final String CLASS_TAG = "TeclaSettingsActivity2";

	private static TeclaSettingsActivity sInstance;
	private TeclaShieldConnect mTeclaShieldManager;
	
	private ScanSpeedDialog mScanSpeedDialog;
	private ProgressDialog mProgressDialog;

	public static TeclaShieldConnect getTeclaShieldConnect() {
		return sInstance.mTeclaShieldManager;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tecla_settings);
		
		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);

		mProgressDialog = new ProgressDialog(this);

		initOnboarding();
		TeclaApp.setSettingsActivityInstance(this);

		if(mTeclaShieldManager == null)
			mTeclaShieldManager = new TeclaShieldManager(this);
		
		sInstance = this;
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

	public void showDiscoveryDialog() {
		mProgressDialog.setMessage(getString(R.string.searching_for_shields));
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				mTeclaShieldManager.cancelDiscovery();
				TeclaStatic.logD(CLASS_TAG, "Tecla Shield discovery cancelled");
				TeclaApp.getInstance().showToast(R.string.shield_connection_cancelled);
				TeclaPreferenceFragment frag = (TeclaPreferenceFragment) 
						sInstance.getFragmentManager().findFragmentById(R.id.tecla_prefs_frag);
				frag.onCancelDiscoveryDialogUpdatePrefs();
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
		TeclaPreferenceFragment frag = (TeclaPreferenceFragment) 
				sInstance.getFragmentManager().findFragmentById(R.id.tecla_prefs_frag);
		frag.onResumeSettingsActivityUpdatePrefs();
	}
	
	@Override
	protected void onDestroy() {

		TeclaApp.setSettingsActivityInstance(null);
		super.onDestroy();
	}

	@Override
	public void onTeclaShieldFound() {
	}

	@Override
	public void onTeclaShieldDiscoveryFinished(boolean shieldFound, String shieldName) {
		if(shieldFound) {
			// Shield found, try to connect
			mProgressDialog.setOnCancelListener(null); //Don't do anything if dialog cancelled
			mProgressDialog.setOnKeyListener(new OnKeyListener() {

				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					return true; //Consume all keys once Shield is found (can't cancel with back key)
				}
				
			});
			mProgressDialog.setMessage(getString(R.string.connecting_tecla_shield) +
					" " + shieldName);
		} else {
			dismissDialog();
			
			TeclaPreferenceFragment frag = (TeclaPreferenceFragment) 
					sInstance.getFragmentManager().findFragmentById(R.id.tecla_prefs_frag);
			frag.onTeclaShieldDiscoveryFinishedUpdatePrefs();
		}
	}

	@Override
	public void onTeclaShieldConnected() {
		dismissDialog();
		
		TeclaPreferenceFragment frag = (TeclaPreferenceFragment) 
				sInstance.getFragmentManager().findFragmentById(R.id.tecla_prefs_frag);
		frag.onTeclaShieldConnectedUpdatePrefs();
	}

	@Override
	public void onTeclaShieldDisconnected() {
		dismissDialog();
		
		TeclaPreferenceFragment frag = (TeclaPreferenceFragment) 
				sInstance.getFragmentManager().findFragmentById(R.id.tecla_prefs_frag);
		frag.onTeclaShieldDisconnectedUpdatePrefs();
	}

	@Override
	public void dismissProgressDialog() {
		dismissDialog();
	}

	public void uncheckFullScreenMode() {
		TeclaPreferenceFragment frag = (TeclaPreferenceFragment) 
			sInstance.getFragmentManager().findFragmentById(R.id.tecla_prefs_frag);
		frag.uncheckFullScreenMode();
	}
}
