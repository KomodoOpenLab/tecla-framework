package com.android.tecla.keyboard;

import ca.idrc.tecla.framework.Persistence;
import ca.idrc.tecla.framework.TeclaStatic;

import android.app.Application;

public class TeclaApp extends Application {

	public static final String CLASS_TAG = "TeclaApp";

	public static Persistence persistence;
	public static TeclaIME ime;

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		init();
	}
	
	private void init() {
		TeclaStatic.logD(CLASS_TAG, "Application context created!");
		persistence = new Persistence(this);
	}
	
	public static void setIMEInstance (TeclaIME ime_instance) {
		ime = ime_instance;
		persistence.setIMERunning(true);
	}
	
}
