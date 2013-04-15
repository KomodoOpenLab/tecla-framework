package ca.idrc.tecla.imescan;

import android.os.Handler;
import android.os.Message;

public class AutomaticScan {
	private static final String tag = "AutomaticScan";
	
	private static final int TICK = 0x33;
	
	private static final int SCAN_STOPPED = 0xa0;
	private static final int SCAN_ROW = 0xa1;
	private static final int SCAN_COLUMN = 0xa2;
	private static final int SCAN_CLICK = 0xa3;
	private static final int SCAN_CLICKED = 0xa4;
	private static int sState = SCAN_STOPPED;
	

	private static int sScanDelay = 1000;
	private static int sActivateDelay = 1500;
	
	private static Handler sHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == TICK) {
				switch(sState) {
				case SCAN_ROW:		IMEAdapter.highlightNextRow();
									sHandler.sendMessageDelayed(msg, sScanDelay);
									break;
				case SCAN_COLUMN:	IMEAdapter.highlightNextKey();
									sHandler.sendMessageDelayed(msg, sScanDelay);
									break;
				case SCAN_CLICK:	IMEAdapter.sendCurrentKey();
									sState = SCAN_CLICKED;
									sHandler.sendMessageDelayed(msg, sActivateDelay);
									break;
				case SCAN_CLICKED:	sState = SCAN_ROW;
									IMEAdapter.reset();
									sHandler.sendMessageDelayed(msg, sScanDelay);
									break;
				default:			break;
						
				}
			} else if(msg.what == SCAN_CLICKED) {
				
			}
			
			super.handleMessage(msg);
		}
		
	};
	
	public static void startScanning() {
		sState = SCAN_ROW;
		Message msg = new Message();
		msg.what = TICK;
		sHandler.sendMessageDelayed(msg, sScanDelay);
	}
	
	public static void stopScanning() {
		sHandler.removeMessages(TICK);
	}
	
	public static void click() {
		stopScanning();
		Message msg = new Message();
		msg.what = TICK;
		switch(sState) {
		case SCAN_ROW:		sState = SCAN_COLUMN;
							break;
		case SCAN_COLUMN:	sState = SCAN_CLICK;
							break;
		case SCAN_CLICK:	return; // impossibly fast
		case SCAN_CLICKED:	sState = SCAN_CLICK;
							break;
		default:			break;
		}
		sHandler.sendMessageDelayed(msg, 0);
		
	}
}
