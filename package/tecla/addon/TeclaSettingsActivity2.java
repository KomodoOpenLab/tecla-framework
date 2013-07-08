package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.TeclaStatic;
import android.app.Activity;
import android.os.Bundle;

public class TeclaSettingsActivity2 extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TeclaStatic.logE("=====", "1");
		setContentView(R.layout.tecla_settings);
		TeclaStatic.logE("=====", "2");
	}

}
