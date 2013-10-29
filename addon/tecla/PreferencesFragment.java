package com.android.tecla;

import com.android.tecla.ManagerShield.OnConnectionAttemptListener;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.Persistence;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;

public class PreferencesFragment extends PreferenceFragment
implements OnPreferenceClickListener
, OnPreferenceChangeListener  {

	private final static String CLASS_TAG = "TeclaPreferenceFragment";

	private static PreferencesFragment sInstance;

	private ProgressDialog mProgressDialog;
	private OnConnectionAttemptListener mOnConnectionAttemptListener;
	private boolean mConnectionCancelled;

	private CheckBoxPreference mFullscreenMode;
	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;
	private CheckBoxPreference mPrefConnectToShield;
	private CheckBoxPreference mPrefTempDisconnect;
	Preference mScanSpeedPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		sInstance = this;

		addPreferencesFromResource(R.xml.preferences);

		mFullscreenMode = (CheckBoxPreference) findPreference(Persistence.PREF_FULLSCREEN_MODE);
		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
		mScanSpeedPref = findPreference(Persistence.PREF_SCAN_DELAY_INT);
		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
		mPrefTempDisconnect = (CheckBoxPreference) findPreference(Persistence.PREF_TEMP_SHIELD_DISCONNECT);

		mFullscreenMode.setOnPreferenceChangeListener(sInstance);
		mPrefSelfScanning.setOnPreferenceChangeListener(sInstance);
		mPrefInverseScanning.setOnPreferenceChangeListener(sInstance);
		mScanSpeedPref.setOnPreferenceClickListener(sInstance);
		mPrefConnectToShield.setOnPreferenceChangeListener(sInstance);
		mPrefTempDisconnect.setOnPreferenceChangeListener(sInstance);

		mProgressDialog = new ProgressDialog((PreferencesActivity)getActivity());
		mOnConnectionAttemptListener = new OnConnectionAttemptListener() {

			@Override
			public void onShieldFound(String shield_name) {
				// Shield found, try to connect
				mProgressDialog.setOnCancelListener(null); //Don't do anything if dialog cancelled
				mProgressDialog.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						return true; //Consume all keys once Shield is found (can't cancel with back key)
					}

				});
				mProgressDialog.setMessage(getString(R.string.connecting_tecla_shield) +
						" " + shield_name);
			}

			@Override
			public void onConnetionFailed(int error) {
				switch(error) {
				case ManagerShield.ERROR_BT_NOT_SUPPORTED:
				case ManagerShield.ERROR_SERVICE_NOT_BOUND:
				case ManagerShield.ERROR_SHIELD_NOT_FOUND:
					TeclaStatic.logE(CLASS_TAG, getString(R.string.couldnt_connect_shield));
				}
				dismissProgressDialog();
				mPrefConnectToShield.setChecked(false);
			}

			@Override
			public void onConnetionEstablished() {
				dismissProgressDialog();
				//TeclaApp.a11yservice.showFeedback();
			}
		};

	}

	@Override
	public boolean onPreferenceClick(Preference pref) {	
		if(pref.equals(mScanSpeedPref)) {
			((PreferencesActivity)getActivity()).showScanSpeedDialog();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange(android.preference.Preference, java.lang.Object)
	 */
	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		if(pref.equals(mFullscreenMode)) {
			if (newValue.toString().equals("true")) {
				TeclaStatic.logD(CLASS_TAG, "User wants to enable full screen mode!");
				TeclaApp.a11yservice.enableScreenSwitch();
				//mPrefSelfScanning.setChecked(true);
			} else {
				TeclaStatic.logD(CLASS_TAG, "User wants to disable full screen mode!");
				//TeclaApp.getInstance().turnFullscreenOff();
				TeclaApp.a11yservice.disableScreenSwitch();
				//mPrefSelfScanning.setChecked(false);
			}
			return true;
		}
		if(pref.equals(mPrefSelfScanning)) {
			TeclaStatic.logD(CLASS_TAG, "Self scanning preference changed!");
			if (newValue.toString().equals("true")) {
				TeclaApp.persistence.setSelfScanningSelected(true);
				//if(TeclaApp.persistence.isFullscreenEnabled())
				ManagerAutoScan.start();
			} else {
				TeclaApp.persistence.setSelfScanningSelected(false);
				//if(TeclaApp.persistence.isFullscreenEnabled() )
				ManagerAutoScan.stop();
			}
			return true;
		}
		if(pref.equals(mPrefInverseScanning)) {
			TeclaStatic.logD(CLASS_TAG, "Inverse scanning preference changed!");
			if (newValue.toString().equals("true")) {
				TeclaApp.persistence.setInverseScanningSelected(true);
				TeclaApp.a11yservice.setFullscreenSwitchLongClick(false);
				if(TeclaApp.persistence.isScreenSwitchSelected() 
						&& TeclaApp.persistence.isSelfScanningSelected()) {
					ManagerAutoScan.stop();
				}
			} else {
				TeclaApp.persistence.setInverseScanningSelected(false);
				TeclaApp.a11yservice.setFullscreenSwitchLongClick(true);
				if(TeclaApp.persistence.isScreenSwitchSelected() 
						&& TeclaApp.persistence.isSelfScanningSelected()) {
					ManagerAutoScan.start();
				}
			}
			return true;
		}
		if(pref.equals(mPrefConnectToShield)) {
			if (newValue.toString().equals("true")) {
				TeclaStatic.logD(CLASS_TAG, "User requested Shield connection!");
				//mConnectionCancelled = false;

				showDiscoveryDialog();
				TeclaApp.getShieldManager().connect(mOnConnectionAttemptListener);

				//				if (!TeclaApp.shieldmanager.getBluetoothAdapter().isEnabled()) {
				//					startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
				//				}else{	
				//				
				//					if(!TeclaApp.shieldmanager.discoverShield())
				//						mPrefConnectToShield.setChecked(false);
				//					else
				//						((TeclaSettingsActivity)getActivity()).showDiscoveryDialog();
				//				}
			} else {
				TeclaStatic.logD(CLASS_TAG, "User wants to cancel Shield connection!");
				TeclaApp.getShieldManager().disconnect();
				dismissProgressDialog();
				if (!mFullscreenMode.isChecked()) {
					mPrefTempDisconnect.setChecked(false);
					mPrefTempDisconnect.setEnabled(false);
					mPrefSelfScanning.setChecked(false);
					mPrefInverseScanning.setChecked(false);
					//					mPrefPersistentKeyboard.setChecked(false);
				}
			}
			return true;
		}
		if(pref.equals(mPrefTempDisconnect)) {
			TeclaStatic.logD(CLASS_TAG, "Temp shield disconnect preference changed!");
			if (newValue.toString().equals("true")) {
				mPrefConnectToShield.setEnabled(false);
				TeclaApp.getShieldManager().disconnect();
				Handler mHandler = new Handler();
				Runnable mReconnect = new Runnable() {

					public void run() {
						TeclaStatic.logD(CLASS_TAG, "Re-enabling discovery");
						showDiscoveryDialog();
						TeclaApp.getShieldManager().connect(mOnConnectionAttemptListener);
						mPrefConnectToShield.setEnabled(true);
					}
				};

				// See if the handler was posted
				if(mHandler.postDelayed(mReconnect, 90000))	// 90 second delay
				{
					TeclaStatic.logD(CLASS_TAG, "Posted Runnable");
				}
				else
				{
					TeclaStatic.logD(CLASS_TAG, "Could not post Runnable");
				}
			}
			return true;
		}
		return false;
	}

	//	private void onTeclaShieldDiscoveryFinishedUpdatePrefs() {
	//		mPrefConnectToShield.setChecked(false);
	//		mPrefTempDisconnect.setChecked(false);
	//		mPrefTempDisconnect.setEnabled(false);
	///*		if (!mConnectionCancelled) 
	//			TeclaApp.getInstance().showToast(R.string.no_shields_inrange);
	//*/	}

	//	private void onTeclaShieldConnectedUpdatePrefs() {
	//		mPrefTempDisconnect.setEnabled(true);
	//		mPrefConnectToShield.setChecked(true);
	//		//		mPrefMorse.setEnabled(true);
	//		//		mPrefPersistentKeyboard.setChecked(true);
	//	}
	//
	//	private void onTeclaShieldDisconnectedUpdatePrefs() {
	//		mPrefTempDisconnect.setChecked(false);
	//		mPrefTempDisconnect.setEnabled(false);
	//	}
	//
/*	private void showTeclaOverlay() {
		TeclaApp.a11yservice.getOverlay().showAll();
	}
*/
	private void showDiscoveryDialog() {
		mProgressDialog.setMessage(getString(R.string.searching_for_shields));
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				TeclaApp.getShieldManager().disconnect();
				onCancelDiscoveryDialogUpdatePrefs();
			}
		});
		mProgressDialog.show();
	}

	/*
	 * Dismisses progress dialog without triggerint it's OnCancelListener
	 */
	private void dismissProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	private void onCancelDiscoveryDialogUpdatePrefs() {
		mConnectionCancelled = true;
		mPrefConnectToShield.setChecked(false);
		mPrefTempDisconnect.setChecked(false);
		mPrefTempDisconnect.setEnabled(false);
	}

	public void onResumeSettingsActivityUpdatePrefs() {
		mFullscreenMode.setChecked(TeclaApp.persistence.isScreenSwitchSelected());
		mPrefSelfScanning.setChecked(TeclaApp.persistence.isSelfScanningSelected());
		mPrefInverseScanning.setChecked(TeclaApp.persistence.isInverseScanningSelected());;
	}

	public void uncheckFullScreenMode() {
		if(!TeclaApp.persistence.isScreenSwitchSelected()) {
			mFullscreenMode.setChecked(false);
		}
	}

}
