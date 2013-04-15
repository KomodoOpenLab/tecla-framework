package ca.idrc.tecla.imescan;

public abstract class GenericSwitch {

	private int mID;
		
	public GenericSwitch(int id) {
		mID = id;
	}

	public boolean checkID(int id) {
		if(mID == id) return true;
		return false;
	}
	
	public abstract void activate();
}
