package com.android.tecla.addon;

import ca.idrc.tecla.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class TeclaPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.tecla_prefs);
	}

}
