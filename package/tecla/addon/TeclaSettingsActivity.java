package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.Persistence;
import ca.idrc.tecla.framework.ScanSpeedDialog;
import ca.idrc.tecla.framework.TeclaStatic;
import android.accessibilityservice.AccessibilityService;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

public class TeclaSettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, /*OnSharedPreferenceChangeListener,*/ OnPreferenceChangeListener {

	private final static String CLASS_TAG = "TeclaSettings";

	private static TeclaSettingsActivity sInstance;

	private TeclaShieldConnect mTeclaShieldManager;
	
	private CheckBoxPreference mFullscreenMode;
	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;
	private CheckBoxPreference mPrefConnectToShield;
	Preference mScanSpeedPref;
	private ScanSpeedDialog mScanSpeedDialog;
	private ProgressDialog mProgressDialog;

	public static final int ACTION_DISCOVERY_FINISHED_SHIELD_FOUND = 0x1111;
	public static final int ACTION_DISCOVERY_FINISHED_SHIELD_NOT_FOUND = 0x2222;
	public static final String SHIELD_NAME_KEY = "ShieldName";
	public static final String SHIELD_ADDRESS_KEY = "ShieldAddress";
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(ACTION_DISCOVERY_FINISHED_SHIELD_FOUND == msg.what) {
				// Shield found, try to connect
				mProgressDialog.setOnCancelListener(null); //Don't do anything if dialog cancelled
				mProgressDialog.setOnKeyListener(new OnKeyListener() {

					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						return true; //Consume all keys once Shield is found (can't cancel with back key)
					}
					
				});
				Bundle bundle = (Bundle) msg.obj;
				String shieldName = bundle.getString(SHIELD_NAME_KEY, "");
				String shieldAddress = bundle.getString(SHIELD_ADDRESS_KEY, "");
				mProgressDialog.setMessage("Connecting to Tecla Shield" +
						" " + shieldName);
				if(!mTeclaShieldManager.connect(sInstance.getApplicationContext(), shieldAddress)) {
					// Could not connect to Shield
					dismissDialog();
					TeclaApp.getInstance().showToast("Could not connect to Tecla Shield");
				}
			} else if(ACTION_DISCOVERY_FINISHED_SHIELD_NOT_FOUND == msg.what) {
				dismissDialog();
				sInstance.mPrefConnectToShield.setChecked(false);
			}
			super.handleMessage(msg);
		}
		
	};
	
	public static TeclaShieldConnect getTeclaShieldConnect() {
		return sInstance.mTeclaShieldManager;
	}
	
//	private CheckBoxPreference mPrefHUD;
//	private CheckBoxPreference mPrefSingleSwitchOverlay;
//	private CheckBoxPreference mPrefHUDSelfScanning;

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
		mPrefSelfScanning.setOnPreferenceChangeListener(sInstance);
		mPrefInverseScanning.setOnPreferenceChangeListener(sInstance);
		mScanSpeedPref.setOnPreferenceClickListener(sInstance);	

		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
		
		mScanSpeedDialog = new ScanSpeedDialog(sInstance);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);

		mProgressDialog = new ProgressDialog(this);
		
//		mPrefHUD = (CheckBoxPreference) findPreference(Persistence.PREF_HUD);
//		mPrefHUD.setChecked(TeclaApp.persistence.isHUDRunning());
//		mPrefSingleSwitchOverlay = (CheckBoxPreference) findPreference(Persistence.PREF_SINGLESWITCH_OVERLAY);
//		mPrefHUDSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_HUD_SELF_SCANNING);
//		mPrefSingleSwitchOverlay.setEnabled(mPrefHUD.isChecked());
//		mPrefHUDSelfScanning.setEnabled(mPrefHUD.isChecked());
//		mPrefSingleSwitchOverlay.setChecked(TeclaApp.persistence.isSingleSwitchOverlayEnabled());
//		mPrefHUDSelfScanning.setChecked(TeclaApp.persistence.isSelfScanningEnabled());
		
		//getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		initOnboarding();
		
		if(mTeclaShieldManager == null)
			mTeclaShieldManager = new TeclaShieldManager(this, mHandler);

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
				mPrefSelfScanning.setChecked(true);
			} else {
				TeclaApp.getInstance().turnFullscreenOff();
				mPrefSelfScanning.setChecked(false);
			}
			return true;
		}
		if(pref.equals(mPrefSelfScanning)) {
			TeclaStatic.logD(CLASS_TAG, "Self scanning preference changed!");
			if (newValue.toString().equals("true")) {
				TeclaApp.persistence.setSelfScanningEnabled(true);
				if(TeclaApp.persistence.isFullscreenEnabled() )
					AutomaticScan.startAutoScan();
			} else {
				TeclaApp.persistence.setSelfScanningEnabled(false);
				if(TeclaApp.persistence.isFullscreenEnabled() )
					AutomaticScan.stopAutoScan();
			}
			return true;
		}
		if(pref.equals(mPrefInverseScanning)) {
			TeclaStatic.logD(CLASS_TAG, "Inverse scanning preference changed!");
			if (newValue.toString().equals("true")) {
				TeclaApp.persistence.setInverseScanningEnabled(true);
				TeclaApp.setFullscreenSwitchLongClick(false);
				if(TeclaApp.persistence.isFullscreenEnabled() 
						&& TeclaApp.persistence.isSelfScanningEnabled()) {
					AutomaticScan.stopAutoScan();
				}
			} else {
				TeclaApp.persistence.setInverseScanningEnabled(false);
				TeclaApp.setFullscreenSwitchLongClick(true);
				if(TeclaApp.persistence.isFullscreenEnabled() 
						&& TeclaApp.persistence.isSelfScanningEnabled()) {
					AutomaticScan.startAutoScan();
				}
			}
			return true;
		}
		if(pref.equals(mPrefConnectToShield)) {
			TeclaStatic.logD(CLASS_TAG, "Connect to shield preference changed!");
			if (newValue.toString().equals("true")) {
				if(!mTeclaShieldManager.discoverShield())
					mPrefConnectToShield.setChecked(false);
				else
					showDiscoveryDialog();
			} else {
				mTeclaShieldManager.stopShieldService();
			}
			return true;
		}
		return false;
	}

	private void showDiscoveryDialog() {
		mProgressDialog.setMessage("Searching for Tecla Shields. Please wait…");
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				mTeclaShieldManager.cancelDiscovery();
				TeclaStatic.logD(CLASS_TAG, "Tecla Shield discovery cancelled");
				TeclaApp.getInstance().showToast("Connection to Tecla Shield cancelled");
				//mConnectionCancelled = true;
				mPrefConnectToShield.setChecked(false);
				
			}
		});
		mProgressDialog.show();
	}

	/*
	 * Dismisses progress dialog without triggerint it's OnCancelListener
	 */
	private void dismissDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
	
	/** FIXME: DO NOT USE onSharedPreferenceChanged FOR PROCESSING PREFERENCES!!! THIS METHOD IS NOT APPROPRIATE!!! USE onPreferenceChange INSTEAD!!!**/
//	@Override
//	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
//			String key) {
//		if (key.equals(Persistence.PREF_SELF_SCANNING)) {
//			Persistence.getInstance().setSelfScanningEnabled(true);
//			if (mPrefSelfScanning.isChecked()) {
//				//TeclaApp.getInstance().startScanningTeclaIME();
//			} else {
//				Persistence.getInstance().setSelfScanningEnabled(false);
//				//TeclaApp.getInstance().stopScanningTeclaIME();
//				if (!mPrefInverseScanning.isChecked()) {
//					/*mPrefFullScreenSwitch.setChecked(false);
//					if (!mPrefConnectToShield.isChecked()) {
//						mPrefTempDisconnect.setChecked(false);
//						mPrefTempDisconnect.setEnabled(false);
//					}*/
//				}
//			}
//			
//		} else if (key.equals(Persistence.PREF_INVERSE_SCANNING)) {
//			Persistence.getInstance().setInverseScanningEnabled(true);
//			if (mPrefInverseScanning.isChecked()) {
//				mPrefSelfScanning.setChecked(false);
//				//TeclaApp.persistence.setInverseScanningChanged();
//				//TeclaApp.persistence.setFullResetTimeout(Persistence.MAX_FULL_RESET_TIMEOUT);
//			} else {
//				Persistence.getInstance().setInverseScanningEnabled(false);
//				//TeclaApp.getInstance().stopScanningTeclaIME();
//				if (!mPrefSelfScanning.isChecked()) {
//					//TeclaApp.persistence.setFullResetTimeout(Persistence.MIN_FULL_RESET_TIMEOUT);
//					/*mPrefFullScreenSwitch.setChecked(false);
//					if (!mPrefConnectToShield.isChecked()) {
//						mPrefTempDisconnect.setChecked(false);
//						mPrefTempDisconnect.setEnabled(false);
//					}*/
//				}
//			}
//			
//		}
////		else if (key.equals(Persistence.PREF_HUD)) {
////			if(!TeclaApp.getInstance().isTeclaA11yServiceRunning()
////					|| !TeclaApp.persistence.isHUDRunning()) {
////				mPrefHUD.setChecked(false);
////				return;
////			}
////			TeclaApp.persistence.setHUDRunning(mPrefHUD.isChecked());
////			if(mPrefHUD.isChecked()) {
////				if(!TeclaApp.a11yservice.mTeclaHUDController.isVisible()) {
////					TeclaApp.a11yservice.mTeclaHUDController.show();
////				}
////				mPrefSingleSwitchOverlay.setEnabled(true);
////				mPrefHUDSelfScanning.setEnabled(true);
////				TeclaApp.persistence.setSingleSwitchOverlayEnabled(true);
////				TeclaApp.a11yservice.mTouchInterface.show();
////				mPrefSingleSwitchOverlay.setChecked(true);
////				TeclaApp.persistence.setHUDSelfScanningEnabled(mPrefHUDSelfScanning.isChecked());
////				TeclaApp.a11yservice.mTeclaHUDController.mAutoScanHandler.sleep(
////						TeclaApp.persistence.getScanDelay());
////				mPrefHUDSelfScanning.setChecked(true);				
////			} else {
////				if(TeclaApp.persistence.isHUDRunning()) {
////					TeclaApp.persistence.setHUDRunning(false);
////					TeclaApp.a11yservice.mTeclaHUDController.hide();
////				}
////				if(TeclaApp.persistence.isSingleSwitchOverlayEnabled()) {
////					TeclaApp.persistence.setSingleSwitchOverlayEnabled(false);
////					mPrefSingleSwitchOverlay.setChecked(false);
////					TeclaApp.a11yservice.mTouchInterface.hide();
////				}
////				if(TeclaApp.persistence.isHUDSelfScanningEnabled()) {
////					TeclaApp.persistence.setHUDSelfScanningEnabled(false);
////					mPrefHUDSelfScanning.setChecked(false);
////					TeclaApp.a11yservice.mTeclaHUDController.mAutoScanHandler.removeMessages(0);
////				}
////				mPrefSingleSwitchOverlay.setEnabled(false);
////				mPrefHUDSelfScanning.setEnabled(false);
////			}
////			
////		} else if (key.equals(Persistence.PREF_SINGLESWITCH_OVERLAY)) {
////			if(!TeclaApp.getInstance().isTeclaA11yServiceRunning()
////					|| !TeclaApp.persistence.isHUDRunning()
////					|| !TeclaApp.a11yservice.mTeclaHUDController.isVisible()) {
////				mPrefSingleSwitchOverlay.setChecked(false);
////				return;
////			}			
////			TeclaApp.persistence.setSingleSwitchOverlayEnabled(mPrefSingleSwitchOverlay.isChecked());
////			if(mPrefSingleSwitchOverlay.isChecked()) {
////				TeclaApp.a11yservice.mTouchInterface.show();
////			} else {
////				TeclaApp.a11yservice.mTouchInterface.hide();
////			}
////			
////		} else if (key.equals(Persistence.PREF_HUD_SELF_SCANNING)) {
////			TeclaApp.persistence.setHUDSelfScanningEnabled(mPrefHUDSelfScanning.isChecked());
////			if(mPrefHUDSelfScanning.isChecked()) {
////				TeclaApp.a11yservice.mTeclaHUDController.mAutoScanHandler.sleep(
////						TeclaApp.persistence.getScanDelay());
////			} else {
////				TeclaApp.a11yservice.mTeclaHUDController.mAutoScanHandler.removeMessages(0);				
////			}
////			
////		}
//
//	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		initOnboarding();	
		updatePreferences();
	}
	
	private void updatePreferences() {
		mFullscreenMode.setChecked(TeclaApp.persistence.isFullscreenEnabled());
		mPrefSelfScanning.setChecked(TeclaApp.persistence.isSelfScanningEnabled());
		mPrefInverseScanning.setChecked(TeclaApp.persistence.isInverseScanningEnabled());;
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
