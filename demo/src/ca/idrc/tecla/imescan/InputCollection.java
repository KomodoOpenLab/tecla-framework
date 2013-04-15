package ca.idrc.tecla.imescan;

import java.util.ArrayList;

public class InputCollection {

	ArrayList<GenericInput> mInputs;

	public InputCollection() {
		mInputs = new ArrayList<GenericInput>();
		mInputs.add(new TeclaShieldInput1(0x1234));
	}
	
	private class TeclaShieldInput1 extends GenericInput implements InputActivation {

		public TeclaShieldInput1(int id) {
			super(id);
		}

		@Override
		public void activate() {
			IMEAdapter.sendCurrentKey();
			
		}
		
	}
}
