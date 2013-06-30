package com.android.tecla.addon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TeclaShieldService extends Service {

	private static final String CLASS_TAG = "TeclaShieldService";
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
