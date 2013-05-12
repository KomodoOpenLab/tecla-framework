package ca.idrc.tecla.framework;

import ca.idrc.tecla.R;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener  {

	ScanSpeedDialog mScanSpeedDialog;
	
	Preference mScanSpeedPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.tecla_prefs);
		init();
	}

	private void init() {
		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.scan_speed_dialog);
		mScanSpeedPref = this.findPreference("scan_speed");
		mScanSpeedPref.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {	
		if(pref.equals(mScanSpeedPref)) {
			mScanSpeedDialog.show();
		}
		return false;
	}

}
