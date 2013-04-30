package ca.idrc.tecla.framework;

import android.content.Context;
import android.provider.Settings;

public class Persistence {

	private static final String IME_ID = "com.android.inputmethod.latin/.LatinIME";

	private boolean is_ime_running;
	private boolean is_ime_showing;

	public Persistence(Context context) {
		is_ime_running = false;
		is_ime_showing = false;
	}

	public void setIMERunning(boolean is_showing) {
		is_ime_running = is_showing;
	}

	public boolean isIMERunning() {
		return is_ime_running;
	}

	public void setIMEShowing(boolean is_showing) {
		is_ime_showing = is_showing;
	}

	public boolean isIMEShowing() {
		return is_ime_showing;
	}

	public static Boolean isDefaultIME(Context context) {
		String ime_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		if (ime_id.equals(IME_ID)) return true;
		return false;
	}

}
