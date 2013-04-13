package ca.idrc.tecla.imescan;

import java.util.ArrayList;

public class TeclaShieldSwitchCollection {

	ArrayList<TeclaShieldSwitch> mSwitches;

	public TeclaShieldSwitchCollection() {
		mSwitches = new ArrayList<TeclaShieldSwitch>();
		mSwitches.add(new TeclaShieldSwitch1(0x1234));
	}
	
}
