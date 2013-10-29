package com.android.tecla;

import com.android.tecla.ServiceShield.ShieldServiceBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.Intent;
import android.os.IBinder;

public class ManagerShield {
	
	
	private final static String CLASS_TAG = "TeclaShieldManager";

	/**
	 * Intent string used to start and stop the switch event
	 * provider service. {@link #EXTRA_SHIELD_ADDRESS}
	 * must be provided to start the service.
	 */
	//private static final String SHIELD_SERVICE = "com.android.tecla.TECLA_SHIELD_SERVICE";
	
	/**
	 * Tecla Shield MAC Address to connect to.
	 */
	public static final String EXTRA_SHIELD_ADDRESS = "ca.idi.tecla.sdk.extra.SHIELD_ADDRESS";

	//private static final int REQUEST_ENABLE_BT = 1;

	private ServiceShield mTeclaShieldService;
	private Boolean mBound = false;
	private Context mContext;

	public ManagerShield(Context context) {
		init(context);
//		init();
	}

//	private void init() {
	private void init(Context context) {
		mContext = context;
		bindToTeclaShieldService();

//		//mShieldListener = activity;
//		//mContext.registerReceiver(mReceiver, new IntentFilter(TeclaShieldService.ACTION_SHIELD_CONNECTED));
//		//mContext.registerReceiver(mReceiver, new IntentFilter(TeclaShieldService.ACTION_SHIELD_DISCONNECTED));
	}
//	
//	/*
//	 * Stops the SEP if it is running
//	 */
//	public void stopShieldService() {
//		//if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
//		//	return;
//		if (TeclaApp.getInstance().isShieldServiceRunning(mContext)) {
//			disconnect(mContext.getApplicationContext());
//		}
//	}
//	
	/**
	 * Start the Tecla Shield Service to attempt a connection
	 * with the last known Tecla Shield
	 */
	public void connect(OnConnectionAttemptListener listener) {
		if (mBound == true) {
			mTeclaShieldService.discover(listener);
		} else {
			listener.onConnetionFailed(ERROR_SERVICE_NOT_BOUND);
		}
	}

	public void disconnect() {
		if (mBound == true) {
			mTeclaShieldService.stopShieldService();
		}
	}

	private void bindToTeclaShieldService() {
		TeclaStatic.logD(CLASS_TAG, "Attempting to bind to Tecla Shield Service...");

		// Bind to TeclaShieldService
		Intent intent = new Intent(mContext, ServiceShield.class);
		mContext.bindService(intent, mTSSConnection, Context.BIND_AUTO_CREATE);

		//mHandler = new Handler();
		//mIsBroadcasting = false;
		//mPrevSwitchStates = SwitchEvent.SWITCH_STATES_DEFAULT;
		//mServiceStarted = false;

		//mShyCounter = 0;
		//mBoldCounter = 0;
		//mIsBold = false;
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mTSSConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			ShieldServiceBinder binder = (ShieldServiceBinder) service;
			mTeclaShieldService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;

		}
	};

//	// All intents will be processed here
//	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//
//			if (intent.getAction().equals(TeclaShieldService.ACTION_SHIELD_CONNECTED)) {
//				TeclaStatic.logD(CLASS_TAG, "Successfully started SEP");
//				TeclaApp.getInstance().showToast(R.string.shield_connected);
//				onTeclaShieldConnected();
//			}
//
//			if (intent.getAction().equals(TeclaShieldService.ACTION_SHIELD_DISCONNECTED)) {
//				TeclaStatic.logD(CLASS_TAG, "SEP broadcast stopped");
//				onTeclaShieldDisconnected();
//			}
//			if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
//				onBluetoothActivation();
//			}
//		}
//	};

//	@Override
//	public void onTeclaShieldFound() {
//		
//	}
//
//	@Override
//	public void onTeclaShieldDiscoveryFinished(boolean shieldFound,
//			String shieldName) {
//		if(shieldFound) {
//			// Shield found, try to connect
//			mProgressDialog.setOnCancelListener(null); //Don't do anything if dialog cancelled
//			mProgressDialog.setOnKeyListener(new OnKeyListener() {
//
//				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//					return true; //Consume all keys once Shield is found (can't cancel with back key)
//				}
//				
//			});
//			mProgressDialog.setMessage(getString(R.string.connecting_tecla_shield) +
//					" " + shieldName);
//		} else {
//			dismissDialog();
//			
//			mPreferenceFragment.onTeclaShieldDiscoveryFinishedUpdatePrefs();
//		}
//	}

//	@Override
//	public void onTeclaShieldConnected() {
//		TeclaApp.getInstance().turnHUDon();
//		IMEAdapter.setKeyboardView(null);
////		mPrefMorse.setEnabled(true);
////		mPrefPersistentKeyboard.setChecked(true);
//		mPreferenceFragment.onTeclaShieldConnectedUpdatePrefs();
//	}

//	@Override
//	public void onTeclaShieldDisconnected() {
//		dismissDialog();
//		TeclaApp.getInstance().turnHUDoff();		
//		mPreferenceFragment.onTeclaShieldDisconnectedUpdatePrefs();
//
//	}
//
//	@Override
//	public void onBluetoothActivation() {
//		
//	}
//
//	@Override
//	public void dismissProgressDialog() {
//		
//	}

	/** Callback definitions! **/
	public final static int ERROR_BT_NOT_SUPPORTED = -1;
	public final static int ERROR_SHIELD_NOT_FOUND = -2;
	public final static int ERROR_SERVICE_NOT_BOUND = -3;

	public static interface OnConnectionAttemptListener {
		public abstract void onConnetionEstablished();
		public abstract void onShieldFound(String shield_name);
		public abstract void onConnetionFailed(int error);
	}
		
}
