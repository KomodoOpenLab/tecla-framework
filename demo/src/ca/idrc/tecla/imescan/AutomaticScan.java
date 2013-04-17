package ca.idrc.tecla.imescan;

import android.os.Handler;
import android.os.Message;

public class AutomaticScan {
	private static final String tag = "AutomaticScan";
	
	private static final int TICK = 0x33;
	
	private static int sScanDelay = 1000;
	private static boolean sIsScsanning = false;
	
	private static Handler sHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == TICK) {
				tick();
			}
			
			super.handleMessage(msg);
		}
		
	};
	
	private static void tick() {
		sHandler.removeMessages(TICK);
		IMEAdapter.scanNext();
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, sScanDelay);		
	}
	
	public static void startAutoScan() {
		if(sIsScsanning) return;
		sIsScsanning = true;
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, sScanDelay);
	}
	
	public static void stopAutoScan() {
		if(!sIsScsanning) return;
		sHandler.removeMessages(TICK);
		sIsScsanning = false;
	}
	
	public static void resetTimer() {
		if(!sIsScsanning) return;
		sHandler.removeMessages(TICK);
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, sScanDelay);
	}

	public static void setExtendedTimer() {
		if(!sIsScsanning) return;
		sHandler.removeMessages(TICK);
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, sScanDelay*3/2);
	}
	
	
	
}
