package ca.idrc.tecla.imescan;

public class TeclaShieldSwitch1 extends TeclaShieldSwitch implements SwitchActivation {

	
	public TeclaShieldSwitch1(int id) {
		super(id);
	}

	@Override
	public void activate() {
		IMEAdapter.sendCurrentKey();
	}

}
