package ca.idrc.tecla.imescan;

public abstract class GenericInput {

	private int mID;
		
	public GenericInput(int id) {
		mID = id;
	}

	public boolean checkID(int id) {
		if(mID == id) return true;
		return false;
	}
	
	public abstract void activate();
}
