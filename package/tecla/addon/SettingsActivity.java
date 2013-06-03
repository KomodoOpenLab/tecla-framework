package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.Persistence;
import ca.idrc.tecla.framework.ScanSpeedDialog;
import ca.idrc.tecla.framework.TeclaStatic;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnSharedPreferenceChangeListener {

	private SettingsActivity sInstance;

	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;

	private ScanSpeedDialog mScanSpeedDialog;

	Preference mScanSpeedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	private void init() {
		sInstance = this;
		
		addPreferencesFromResource(R.xml.tecla_prefs);

		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
		mScanSpeedPref = findPreference(Persistence.PREF_SCAN_DELAY_INT);

		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);

		mScanSpeedPref.setOnPreferenceClickListener(this);		

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		initOnboarding();
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {	
		if(pref.equals(mScanSpeedPref)) {
			mScanSpeedDialog.show();
		}
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Persistence.PREF_SELF_SCANNING)) {
			Persistence.getInstance().setSelfScanningEnabled(true);
			if (mPrefSelfScanning.isChecked()) {
				//TeclaApp.getInstance().startScanningTeclaIME();
			} else {
				Persistence.getInstance().setSelfScanningEnabled(false);
				//TeclaApp.getInstance().stopScanningTeclaIME();
				if (!mPrefInverseScanning.isChecked()) {
					/*mPrefFullScreenSwitch.setChecked(false);
					if (!mPrefConnectToShield.isChecked()) {
						mPrefTempDisconnect.setChecked(false);
						mPrefTempDisconnect.setEnabled(false);
					}*/
				}
			}
		}
		if (key.equals(Persistence.PREF_INVERSE_SCANNING)) {
			Persistence.getInstance().setInverseScanningEnabled(true);
			if (mPrefInverseScanning.isChecked()) {
				mPrefSelfScanning.setChecked(false);
				//TeclaApp.persistence.setInverseScanningChanged();
				//TeclaApp.persistence.setFullResetTimeout(Persistence.MAX_FULL_RESET_TIMEOUT);
			} else {
				Persistence.getInstance().setInverseScanningEnabled(false);
				//TeclaApp.getInstance().stopScanningTeclaIME();
				if (!mPrefSelfScanning.isChecked()) {
					//TeclaApp.persistence.setFullResetTimeout(Persistence.MIN_FULL_RESET_TIMEOUT);
					/*mPrefFullScreenSwitch.setChecked(false);
					if (!mPrefConnectToShield.isChecked()) {
						mPrefTempDisconnect.setChecked(false);
						mPrefTempDisconnect.setEnabled(false);
					}*/
				}
			}
		}

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
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
				this);
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
//			if (mOnboardingDialog != null) {
//				if (mOnboardingDialog.isShowing()) {
//					mOnboardingDialog.dismiss();
//				}
//				mOnboardingDialog = null;
//			}
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
