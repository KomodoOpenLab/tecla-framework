package com.android.tecla.addon;

import ca.idrc.tecla.framework.TeclaStatic;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.view.KeyEvent;

public class SPPManager {
	
	private final static String CLASS_TAG = "SPPManager";

	/**
	 * Intent string used to start and stop the switch event
	 * provider service. {@link #EXTRA_SHIELD_ADDRESS}
	 * must be provided to start the service.
	 */
	private static final String SHIELD_SERVICE = "com.android.tecla.addon.TECLA_SHIELD_SERVICE";
	private static final String SHIELD_SERVICE_CLASS = "com.android.tecla.addon.TeclaShieldService";
	
	/**
	 * Tecla Shield MAC Address to connect to.
	 */
	public static final String EXTRA_SHIELD_ADDRESS = "ca.idi.tecla.sdk.extra.SHIELD_ADDRESS";

	private static final int REQUEST_ENABLE_BT = 1;
	
	private Context mContext;
	private BluetoothAdapter mBluetoothAdapter;
	private ProgressDialog mProgressDialog;
	private String mShieldAddress, mShieldName;
	private boolean mShieldFound, mConnectionCancelled;

	
	public SPPManager(Context context) {
		mContext = context;
		init();
	}
	
	private void init() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			
		}
		mProgressDialog = new ProgressDialog(mContext);
	}
	

	private void discoverShield() {
		mShieldFound = false;
		mConnectionCancelled = false;
		cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
		showDiscoveryDialog();
	}
	
	private void cancelDiscovery() {
		if (mBluetoothAdapter != null && 
				mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	private void showDiscoveryDialog() {
		mProgressDialog.setMessage("Searching for Tecla Shields. Please wait…");
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				cancelDiscovery();
				TeclaStatic.logD(CLASS_TAG, "Tecla Shield discovery cancelled");
				TeclaApp.getInstance().showToast("Connection to Tecla Shield cancelled");
				mConnectionCancelled = true;
				// TODO: uncheck shield connection preference
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
	
	/**
	 * Start the Switch Event Provider and attempt a connection with a Tecla Shield with the address provided
	 */
	public boolean shieldConnect() {
		Intent shieldIntent = new Intent(SHIELD_SERVICE);
		shieldIntent.putExtra(EXTRA_SHIELD_ADDRESS, mShieldAddress);
		return mContext.startService(shieldIntent) == null? false:true;
	}

	public boolean shieldDisconnect(Context context) {
		Intent shieldIntent = new Intent(SHIELD_SERVICE);
		return context.stopService(shieldIntent);
	}

	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND) 
					&& !mShieldFound) {
				BluetoothDevice dev = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
				if ((dev.getName() != null) && (
						dev.getName().startsWith("TeklaShield") ||
						dev.getName().startsWith("TeclaShield") )) {
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
					mProgressDialog.setMessage("Connecting to Tecla Shield" +
							" " + mShieldName);
					if(!shieldConnect()) {
						// Could not connect to Shield
						dismissDialog();
						TeclaApp.getInstance().showToast("Could not connect to Tecla Shield");
					}
				} else {
					// Shield not found
					dismissDialog();
					if (!mConnectionCancelled) TeclaApp.getInstance().showToast("No Tecla Shields in range");
				
					// TODO: uncheck shield connection preference
				}
			}

//			if (intent.getAction().equals(TeclaShieldService.ACTION_SHIELD_CONNECTED)) {
//				TeclaStatic.logD(CLASS_TAG, "Successfully started SEP");
//				dismissDialog();
//				TeclaApp.getInstance().showToast(R.string.shield_connected);
//				mPrefTempDisconnect.setEnabled(true);
//				mPrefMorse.setEnabled(true);
//				mPrefPersistentKeyboard.setChecked(true);
//			}
//
//			if (intent.getAction().equals(TeclaShieldService.ACTION_SHIELD_DISCONNECTED)) {
//				TeclaStatic.logD(CLASS_TAG, "SEP broadcast stopped");
//				dismissDialog();
//				mPrefTempDisconnect.setChecked(false);
//				mPrefTempDisconnect.setEnabled(false);
//			}
		}
	};
	
}
