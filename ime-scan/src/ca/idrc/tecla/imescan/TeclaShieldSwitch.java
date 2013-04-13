package ca.idrc.tecla.imescan;

public abstract class TeclaShieldSwitch {

	private int mID;
		
	public TeclaShieldSwitch(int id) {
		mID = id;
	}

	public boolean checkID(int id) {
		if(mID == id) return true;
		return false;
	}
	
	public abstract void activate();
}
