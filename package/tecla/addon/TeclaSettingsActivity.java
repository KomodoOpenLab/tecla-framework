package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.Persistence;
import ca.idrc.tecla.framework.ScanSpeedDialog;
import ca.idrc.tecla.framework.TeclaStatic;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;

public class TeclaSettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, /*OnSharedPreferenceChangeListener,*/ OnPreferenceChangeListener {

	private final static String CLASS_TAG = "TeclaSettings";

	private TeclaSettingsActivity sInstance;

	private CheckBoxPreference mFullscreenMode;
	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;
	private CheckBoxPreference mPrefConnectToShield;
	private CheckBoxPreference mPrefTempDisconnect;
	Preference mScanSpeedPref;
	private ScanSpeedDialog mScanSpeedDialog;
	private ProgressDialog mProgressDialog;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mShieldFound, mConnectionCancelled;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	private void init() {
		sInstance = this;
		
		addPreferencesFromResource(R.xml.tecla_prefs);

		mFullscreenMode = (CheckBoxPreference) findPreference(Persistence.PREF_FULLSCREEN_MODE);
		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
		mScanSpeedPref = findPreference(Persistence.PREF_SCAN_DELAY_INT);
		mFullscreenMode.setOnPreferenceChangeListener(sInstance);
		mScanSpeedPref.setOnPreferenceClickListener(sInstance);	
		mScanSpeedDialog = new ScanSpeedDialog(sInstance);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);

		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
		mPrefTempDisconnect = (CheckBoxPreference) findPreference(Persistence.PREF_TEMP_SHIELD_DISCONNECT);
		
		initOnboarding();
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {	
		if(pref.equals(mScanSpeedPref)) {
			mScanSpeedDialog.show();
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
			TeclaStatic.logD(CLASS_TAG, "FullscreenMode pressed!");
			if (newValue.toString().equals("true")) {
				TeclaApp.getInstance().turnFullscreenOn();
			} else {
				TeclaApp.getInstance().turnFullscreenOff();
			}
			return true;
		}
		if(pref.equals(mPrefConnectToShield)) {
			if (newValue.toString().equals("true")) {
				// Connect to shield
				discoverShield();
			} else {
				// FIXME: Tecla Access - Find out how to disconnect
				// switch event provider without breaking
				// connection with other potential clients.
				// Should perhaps use Binding?
				dismissDialog();
				if (!mFullscreenMode.isChecked()) {
					mPrefTempDisconnect.setChecked(false);
					mPrefTempDisconnect.setEnabled(false);
					mPrefSelfScanning.setChecked(false);
					mPrefInverseScanning.setChecked(false);
				}
				stopShieldService();
			}
			return true;
		}
		return false;
	}
	
	/*
	 * Dismisses progress dialog without triggerint it's OnCancelListener
	 */
	private void dismissDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	private void discoverShield() {
		mShieldFound = false;
		mConnectionCancelled = false;
		cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
		showDiscoveryDialog();
	}

	/*
	 * Stops the SEP if it is running
	 */
	private void stopShieldService() {
		if (TeclaShieldManager.isRunning(getApplicationContext())) {
			TeclaShieldManager.disconnect(getApplicationContext());
		}
	}
	
	private void cancelDiscovery() {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
			// Triggers ACTION_DISCOVERY_FINISHED on mReceiver.onReceive
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	private void showDiscoveryDialog() {
		mProgressDialog.setMessage(getString(R.string.searching_for_shields));
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				cancelDiscovery();
				TeclaStatic.logD(CLASS_TAG, CLASS_TAG + "Tecla Shield discovery cancelled");
				TeclaApp.getInstance().showToast(R.string.shield_connection_cancelled);
				mConnectionCancelled = true;
				mPrefTempDisconnect.setChecked(false);
				mPrefTempDisconnect.setEnabled(false);
				//Since we have cancelled the discovery the check state needs to be reset
				//(triggers onSharedPreferenceChanged)
				//mPrefConnectToShield.setChecked(false);
			}
		});
		mProgressDialog.show();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		initOnboarding();
	}
	
	@Override
	protected void onDestroy() {
//		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	/** Onboarding variables & methods **/
	private OnboardingDialog mOnboardingDialog;

	private Button mImeOkBtn;
	private Button mImeCancelBtn;
	private Button mA11yOkBtn;
	private Button mA11yCancelBtn;
	private Button mFinalOkBtn;

	private void initOnboarding() {
		if (!TeclaStatic.isDefaultIMESupported(getApplicationContext()) ||
				!TeclaApp.getInstance().isTeclaA11yServiceRunning()) {
			if (mOnboardingDialog != null) {
				if (mOnboardingDialog.isShowing()) {
					mOnboardingDialog.dismiss();
				}
				mOnboardingDialog = null;
			}
			mOnboardingDialog = new OnboardingDialog(this);
			mOnboardingDialog.setContentView(R.layout.tecla_onboarding);
			mOnboardingDialog.setCancelable(false);
			mImeOkBtn = (Button) mOnboardingDialog.findViewById(R.id.ime_ok_btn);
			mImeCancelBtn = (Button) mOnboardingDialog.findViewById(R.id.ime_cancel_btn);
			mA11yOkBtn = (Button) mOnboardingDialog.findViewById(R.id.a11y_ok_btn);
			mA11yCancelBtn = (Button) mOnboardingDialog.findViewById(R.id.a11y_cancel_btn);
			mFinalOkBtn = (Button) mOnboardingDialog.findViewById(R.id.success_btn);
			mImeCancelBtn.setOnClickListener(mCancelOnboardingClickListener);
			mA11yCancelBtn.setOnClickListener(mCancelOnboardingClickListener);
			mImeOkBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					TeclaApp.getInstance().pickIme();
				}
			});
			mA11yOkBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
					startActivity(intent);
				}
			});
			mFinalOkBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mOnboardingDialog.dismiss();
				}
			});
			mOnboardingDialog.show();
		} else {
		}
	}

	private View.OnClickListener mCancelOnboardingClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mOnboardingDialog.dismiss();
			sInstance.finish();
		}
	};

}
