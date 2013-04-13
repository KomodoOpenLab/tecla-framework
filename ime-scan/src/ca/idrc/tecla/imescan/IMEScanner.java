package ca.idrc.tecla.imescan;

public class IMEScanner {

	private static TeclaShieldSwitchCollection mSwitchCollection = new TeclaShieldSwitchCollection();
	
	public static void activateSwitch(int id)  {
		for(TeclaShieldSwitch ts_switch: mSwitchCollection.mSwitches) {
			if(ts_switch.checkID(id) ) {
				ts_switch.activate();
				return;
			}
		}
	}
}
