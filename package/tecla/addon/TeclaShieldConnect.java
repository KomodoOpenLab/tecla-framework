package com.android.tecla.addon;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

public interface TeclaShieldConnect {

	public BluetoothAdapter getBluetoothAdapter();
	public boolean discoverShield();
	public void cancelDiscovery();
	public void stopShieldService();

	public boolean connect(Context context);
	public boolean connect(Context context, String shieldAddress);
	public boolean disconnect(Context context);
	public boolean isShieldServiceRunning(Context context);

}
