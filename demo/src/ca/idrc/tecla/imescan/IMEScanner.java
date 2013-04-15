package ca.idrc.tecla.imescan;

public class IMEScanner {

	private static InputCollection mInputCollection = new InputCollection();
	
	public static void activateInput(int id)  {
		for(GenericInput ts_input: mInputCollection.mInputs) {
			if(ts_input.checkID(id) ) {
				ts_input.activate();
				return;
			}
		}
	}
}
