package com.android.tecla.addon;

import android.os.Bundle;

public interface TeclaShieldActionListener {

	public void onTeclaShieldFound();
	public void onTeclaShieldDiscoveryFinished(boolean shieldFound, String shieldName);
	public void onTeclaShieldConnected();
	public void onTeclaShieldDisconnected();
	public void onBluetoothActivation();
	
	public void dismissProgressDialog();
}
