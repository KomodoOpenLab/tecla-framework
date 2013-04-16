package ca.idrc.tecla.imescan;

import android.os.Handler;
import android.os.Message;

public class AutomaticScan {
	private static final String tag = "AutomaticScan";
	
	private static final int TICK = 0x33;
	
	private static int sScanDelay = 1000;
	private static int sActivateDelay = 1500;
	
	private static Handler sHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == TICK) {
			}
			
			super.handleMessage(msg);
		}
		
	};
	
	private static void scan() {
		IMEAdapter.scanNext();
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, sScanDelay);		
	}
	
	public static void startAutoScan() {
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, sScanDelay);
	}
	
	public static void stopAutoScan() {
		sHandler.removeMessages(TICK);
	}
	
}
