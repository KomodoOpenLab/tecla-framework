package ca.idrc.tecla.imescan;

import java.util.ArrayList;

public class TeclaShieldSwitchCollection {

	ArrayList<GenericSwitch> mSwitches;

	public TeclaShieldSwitchCollection() {
		mSwitches = new ArrayList<GenericSwitch>();
		mSwitches.add(new TeclaShieldSwitch1(0x1234));
	}
	
	private class TeclaShieldSwitch1 extends GenericSwitch implements SwitchActivation {

		public TeclaShieldSwitch1(int id) {
			super(id);
		}

		@Override
		public void activate() {
			IMEAdapter.selectHighlighted();
			
		}
		
	}
}
