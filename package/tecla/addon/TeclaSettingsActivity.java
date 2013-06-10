package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.Persistence;
import ca.idrc.tecla.framework.ScanSpeedDialog;
import ca.idrc.tecla.framework.TeclaStatic;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.Button;

public class TeclaSettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	private final static String CLASS_TAG = "TeclaSettings";

	private TeclaSettingsActivity sInstance;

	private CheckBoxPreference mFullscreenMode;
	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;
	private CheckBoxPreference mPrefConnectToShield;
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
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mFullscreenMode = (CheckBoxPreference) findPreference(Persistence.PREF_FULLSCREEN_MODE);
		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
		mScanSpeedPref = findPreference(Persistence.PREF_SCAN_DELAY_INT);
		mFullscreenMode.setOnPreferenceChangeListener(sInstance);
		mScanSpeedPref.setOnPreferenceClickListener(sInstance);	
		mScanSpeedDialog = new ScanSpeedDialog(sInstance);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);

		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
		mPrefConnectToShield.setOnPreferenceClickListener(this);
		mPrefConnectToShield.setOnPreferenceChangeListener(this);
		mProgressDialog = new ProgressDialog(this);
		
		initOnboarding();
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {	
		if(pref.equals(mScanSpeedPref)) {
			mScanSpeedDialog.show();
			return true;
		}
		if(pref.equals(mPrefConnectToShield)) {
			if (mBluetoothAdapter == null) {
				showAlert(R.string.shield_connect_summary_BT_nosupport);
			} else if (!mBluetoothAdapter.isEnabled()) {
				registerReceiver(btReceiver, new IntentFilter(
						BluetoothAdapter.ACTION_STATE_CHANGED));
				startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
			}
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

	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND) && !mShieldFound) {
				BluetoothDevice dev = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
				if ((dev.getName() != null) && (
						dev.getName().startsWith(TeclaShieldService.SHIELD_PREFIX_2) ||
						dev.getName().startsWith(TeclaShieldService.SHIELD_PREFIX_3) )) {
					mShieldFound = true;
					mShieldAddress = dev.getAddress();
					mShieldName = dev.getName();
					TeclaStatic.logD(CLASS_TAG, "Found a Tecla Access Shield candidate");
					cancelDiscovery();
				}
			}

			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if (mShieldFound) {
					// Shield found, try to connect
					mProgressDialog.setOnCancelListener(null); //Don't do anything if dialog cancelled
					mProgressDialog.setOnKeyListener(new OnKeyListener() {

						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							return true; //Consume all keys once Shield is found (can't cancel with back key)
						}
						
					});
					mProgressDialog.setMessage(getString(R.string.connecting_tecla_shield) +
							" " + mShieldName);
					if(!TeclaShieldManager.connect(TeclaPrefs.this, mShieldAddress)) {
						// Could not connect to Shield
						dismissDialog();
						TeclaApp.getInstance().showToast(R.string.couldnt_connect_shield);
					}
				} else {
					// Shield not found
					dismissDialog();
					if (!mConnectionCancelled) TeclaApp.getInstance().showToast(R.string.no_shields_inrange);
					mPrefConnectToShield.setChecked(false);
					mPrefTempDisconnect.setChecked(false);
					mPrefTempDisconnect.setEnabled(false);
				}
			}

			if (intent.getAction().equals(TeclaShieldService.ACTION_SHIELD_CONNECTED)) {
				TeclaStatic.logD(CLASS_TAG, "Successfully started SEP");
				dismissDialog();
				TeclaApp.getInstance().showToast(R.string.shield_connected);
				mPrefTempDisconnect.setEnabled(true);
				mPrefMorse.setEnabled(true);
				mPrefPersistentKeyboard.setChecked(true);
			}

			if (intent.getAction().equals(TeclaShieldService.ACTION_SHIELD_DISCONNECTED)) {
				TeclaStatic.logD(CLASS_TAG, "SEP broadcast stopped");
				dismissDialog();
				mPrefTempDisconnect.setChecked(false);
				mPrefTempDisconnect.setEnabled(false);
			}
		}
	};
	
	BroadcastReceiver btReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			if (state == BluetoothAdapter.STATE_ON){
				TeclaStatic.logD(CLASS_TAG, "Bluetooth Turned On Successfully");
				mPrefConnectToShield.setChecked(true);
			}
		}
	};

	public void showAlert(int resid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(resid);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
