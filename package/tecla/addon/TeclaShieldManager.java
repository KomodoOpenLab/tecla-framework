package com.android.tecla.addon;

import ca.idrc.tecla.framework.TeclaStatic;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class TeclaShieldManager implements TeclaShieldConnect {
	
	private final static String CLASS_TAG = "TeclaShieldManager";

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
	private String mShieldAddress, mShieldName;
	private boolean mShieldFound, mConnectionCancelled;

	private Handler mSettingsActivityHandler;

	public BluetoothAdapter getBluetoothAdapter() {
		if(mBluetoothAdapter == null)
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		return mBluetoothAdapter;
	}
	
	public TeclaShieldManager(Context context, Handler handler) {
		mSettingsActivityHandler = handler;
		mContext = context;
		init();
	}
	
	private void init() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//Tecla Access Intents & Intent Filters
		mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		mContext.registerReceiver(mReceiver, new IntentFilter(TeclaShieldService.ACTION_SHIELD_CONNECTED));
		mContext.registerReceiver(mReceiver, new IntentFilter(TeclaShieldService.ACTION_SHIELD_DISCONNECTED));

	}
	

	public boolean discoverShield() {
		if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
			return false;
		mShieldFound = false;
		mConnectionCancelled = false;
		cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
		return true;
	}

	/*
	 * Stops the SEP if it is running
	 */
	public void stopShieldService() {
		if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
			return;
		if (isShieldServiceRunning(mContext.getApplicationContext())) {
			disconnect(mContext.getApplicationContext());
		}
	}
	
	public void cancelDiscovery() {
		if (mBluetoothAdapter != null && 
				mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	/**
	 * Start the Switch Event Provider and attempt a connection with the last known Tecla Shield
	 */
	public boolean connect(Context context) {
		return connect(context, null);
	}

	/**
	 * Start the Switch Event Provider and attempt a connection with a Tecla Shield with the address provided
	 */
	public boolean connect(Context context, String shieldAddress) {
		Intent shieldIntent = new Intent(SHIELD_SERVICE);
		shieldIntent.putExtra(EXTRA_SHIELD_ADDRESS, shieldAddress);
		return context.startService(shieldIntent) == null? false:true;
	}

	public boolean disconnect(Context context) {
		Intent shieldIntent = new Intent(SHIELD_SERVICE);
		return context.stopService(shieldIntent);
	}

	public boolean isShieldServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Log.d("Tecla SDK", service.service.getClassName().toString());
	    	if (SHIELD_SERVICE_CLASS.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
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
				Message msg = new Message();
				if (mShieldFound) {
					Bundle bundle = new Bundle();
					bundle.putString(TeclaSettingsActivity.SHIELD_NAME_KEY, mShieldName);
					bundle.putString(TeclaSettingsActivity.SHIELD_ADDRESS_KEY, mShieldAddress);
					msg.what = TeclaSettingsActivity.ACTION_DISCOVERY_FINISHED_SHIELD_FOUND;
					msg.obj = bundle;
					mSettingsActivityHandler.sendMessage(msg);
				} else {
					msg.what = TeclaSettingsActivity.ACTION_DISCOVERY_FINISHED_SHIELD_NOT_FOUND;
					mSettingsActivityHandler.sendMessage(msg);
					if (!mConnectionCancelled) TeclaApp.getInstance().showToast("No Tecla Shields in range");
				
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
