package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.Persistence;
import ca.idrc.tecla.framework.ScanSpeedDialog;
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

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnSharedPreferenceChangeListener {

	private SettingsActivity sInstance;
	
	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;
	
	private OnboardingDialog mOnboardingDialog;
	private ScanSpeedDialog mScanSpeedDialog;
	
	Preference mScanSpeedPref;
	
	/** Onboarding variables **/
	private Button mOkButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sInstance = this;
		if (TeclaApp.getInstance().isTeclaIMERunning() && TeclaApp.getInstance().isTeclaA11yServiceRunning()) {
			init();
		} else {
			initOnboarding();
		}
	}
	
	private void init() {
		addPreferencesFromResource(R.xml.tecla_prefs);
		
		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
		mScanSpeedPref = findPreference(Persistence.PREF_SCAN_DELAY_INT);
		
		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);
		
		mScanSpeedPref.setOnPreferenceClickListener(this);		
		
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	}

	private void initOnboarding() {
		mOnboardingDialog = new OnboardingDialog(this);
		mOnboardingDialog.setContentView(R.layout.tecla_onboarding);
		mOnboardingDialog.setCancelable(false);
		mOkButton = (Button) mOnboardingDialog.findViewById(R.id.ok_button);
		mOkButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mOnboardingDialog.dismiss();
				sInstance.finish();
				
			}
		});
		mOnboardingDialog.show();
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

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
				this);
		super.onDestroy();
	}

}
