package com.android.tecla.addon;

import android.os.Handler;
import android.os.Message;

public class AutoScanManager {
	private static final String tag = "AutomaticScan";
	
	private static final int TICK = 0x33;
	
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
		if(TeclaApp.a11yservice.getOverlay().isVisible()
				&& !TeclaApp.a11yservice.getOverlay().isPreview()) {
			TeclaApp.a11yservice.getOverlay().scanNextHUDButton();
		} else if(TeclaApp.getInstance().isSupportedIMERunning()) {
			IMEAdapter.scanNext();			
		}
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, TeclaApp.persistence.getScanDelay());		
	}
	
	public static void start() {
		if(sIsScsanning) return;
		sIsScsanning = true;
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, TeclaApp.persistence.getScanDelay());
	}
	
	public static void stop() {
		if(!sIsScsanning) return;
		sHandler.removeMessages(TICK);
		sIsScsanning = false;
	}
	
	public static void resetTimer() {
		if(!sIsScsanning) return;
		sHandler.removeMessages(TICK);
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, TeclaApp.persistence.getScanDelay());
	}

	public static void setExtendedTimer() {
		if(!sIsScsanning) return;
		sHandler.removeMessages(TICK);
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, TeclaApp.persistence.getScanDelay()*3/2);
	}	
}
