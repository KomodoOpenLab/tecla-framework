package ca.idrc.tecla.imescan;

import java.util.Iterator;
import java.util.List;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;

public class IMEAdapter {

	private static final String tag = "IMEAdapter";
	
	private static Keyboard sKeyboard = null;
	private static KeyboardView sKeyboardView = null;
	private static List<Key> sKeys = null;
		
	private static final int REDRAW_KEYBOARD = 0x22;
	
	private static Handler sHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case(REDRAW_KEYBOARD):	sKeyboardView.invalidateAllKeys();
									break;
			default:				break;
			}	
			super.handleMessage(msg);
		}
		
	};
	
	public static void setKeyboardView(KeyboardView kbv) {
		sKeyboardView = kbv;
		if(kbv == null) {
			sKeyboard = null;
			sKeys = null;	
			return;
		}
		sKeyboard = kbv.getKeyboard();
		sKeys = sKeyboard.getKeys();
		IMEStates.reset();
	}
	
	public static void selectHighlighted() {
		Key key = IMEStates.getCurrentKey();
		if(key == null) return; 
		TeclaIME.getInstance().sendDownUpKeyEvents(key.codes[0]);		
	}
	
	public static void scanNext() {
		
	}
	
	public static void scanPrevious() {
		
	}
	
	private static void highlightKey(int key_index, boolean highlighted) {
		if(sKeys == null || key_index < 0 || key_index >= sKeys.size()) return; 
        Key key = sKeys.get(key_index);
		key.pressed = highlighted;
	}
	
	
	private static void highlightKeys(int start_index, int end_index, boolean highlighted) {
		for(int i=start_index; i<=end_index; ++i) {
			highlightKey(i, highlighted);
		}			
	}

	private static void invalidateKeys() {
		Message msg = new Message();
		msg.what = IMEAdapter.REDRAW_KEYBOARD;
		sHandler.sendMessageDelayed(msg, 0);		
	}
	
	public static void highlightNextKey() {
		if(sKeyboard ==null) return;
		if(sCurrentKeyIndex == -1) {
			highlightKeys(sRowStartIndex, sRowEndIndex, false);
			sCurrentKeyIndex = sRowStartIndex;
		} else {
			highlightKey(sCurrentKeyIndex, false);
			++sCurrentKeyIndex;
			if(sCurrentKeyIndex > sRowEndIndex) sCurrentKeyIndex = -1;
		}
		highlightKey(sCurrentKeyIndex, true);
		invalidateKeys();	
	}
	
	public static void highlightPreviousKey() {
		if(sKeyboard ==null) return;
		if(sCurrentKeyIndex == -1) {
			highlightKeys(sRowStartIndex, sRowEndIndex, false);
			sCurrentKeyIndex = sRowEndIndex;
		} else {
			highlightKey(sCurrentKeyIndex, false);
			--sCurrentKeyIndex;
			if(sCurrentKeyIndex < sRowStartIndex) sCurrentKeyIndex = -1;
		}
		highlightKey(sCurrentKeyIndex, true);
		invalidateKeys();		
	}
	
	public static void highlightNextRow() {
		if(sKeyboard ==null) return;
		highlightKeys(sRowStartIndex, sRowEndIndex, false);
		++sCurrentRow;
		if(sCurrentRow >= IMEStates.sRowCount) sCurrentRow = -1;
		sRowStartIndex = getRowStart(sCurrentRow);
		sRowEndIndex = getRowEnd(sCurrentRow);
		sCurrentKeyIndex = -1;
		highlightKeys(sRowStartIndex, sRowEndIndex, true);
		invalidateKeys();		
	}
	
	public static void highlightPreviousRow() {
		if(sKeyboard ==null) return;
		highlightKeys(sRowStartIndex, sRowEndIndex, false);
		if(sCurrentRow < 0) sCurrentRow = IMEStates.sRowCount - 1;
		else --sCurrentRow;
		sRowStartIndex = getRowStart(sCurrentRow);
		sRowEndIndex = getRowEnd(sCurrentRow);
		highlightKeys(sRowStartIndex, sRowEndIndex, true);
		invalidateKeys();
		
	}
	
	private static int getRowStart(int rowNumber) {
		if(sKeyboard == null || rowNumber == -1) return -1;
		int keyCounter = 0;
		if (rowNumber != 0) {
			List<Key> keyList = sKeyboard.getKeys();
			Key key;
			int rowCounter = 0;
			int prevCoord = keyList.get(0).y;
			int thisCoord;
			while (rowCounter != rowNumber) {
				keyCounter++;
				key = keyList.get(keyCounter);
				thisCoord = key.y;
				if (thisCoord != prevCoord) {
					// Changed rows
					rowCounter++;
					prevCoord = thisCoord;
				}
			}
		}
		return keyCounter;
	}

	private static int getRowEnd(int rowNumber) {
		if(sKeyboard == null || rowNumber == -1) return -1;
		List<Key> keyList = sKeyboard.getKeys();
		int totalKeys = keyList.size();
		int keyCounter = 0;
		if (rowNumber == (getRowCount() - 1)) {
			keyCounter = totalKeys - 1;
		} else {
			Key key;
			int rowCounter = 0;
			int prevCoord = keyList.get(0).y;
			int thisCoord;
			while (rowCounter <= rowNumber) {
				keyCounter++;
				key = keyList.get(keyCounter);
				thisCoord = key.y;
				if (thisCoord != prevCoord) {
					// Changed rows
					rowCounter++;
					prevCoord = thisCoord;
				}
			}
			keyCounter--;
		}
		return keyCounter;
	}

	private static int getRowCount() {
		if(sKeyboard == null) return 0;
		List<Key> keyList = sKeyboard.getKeys();
		Key key;
		int rowCounter = 0;
		int coord = 0;
		for (Iterator<Key> i = keyList.iterator(); i.hasNext();) {
			key = i.next();
			if (rowCounter == 0) {
				rowCounter++;
				coord = key.y;
			}
			if (coord != key.y) {
				rowCounter++;
				coord = key.y;
			}
		}
		return rowCounter;
	}
	

	private static class IMEStates {

		private static final int SCAN_STOPPED = 0xa0;
		private static final int SCAN_ROW = 0xa1;
		private static final int SCAN_COLUMN = 0xa2;
		private static final int SCAN_CLICK = 0xa3;
		private static final int SCAN_CLICKED = 0xa4;
		private static int sState = SCAN_STOPPED;
		
		private static final int KEYPOINTER_NULL = -1;
		private static int sRowCount = 0;
		private static int sCurrentRow = KEYPOINTER_NULL;
		private static int sCurrentKeyIndex = KEYPOINTER_NULL; 
		private static int sRowStartIndex = KEYPOINTER_NULL;
		private static int sRowEndIndex = KEYPOINTER_NULL;
		
		private static void reset() {
			if(sKeyboard == null) return;
			sRowCount = getRowCount();
			sCurrentRow = KEYPOINTER_NULL;
			sCurrentKeyIndex = KEYPOINTER_NULL;
			sRowStartIndex = getRowStart(0);
			sRowEndIndex = getRowEnd(0);
		}
		
		private static Key getCurrentKey() {
			if(sKeyboard == null || sCurrentKeyIndex == KEYPOINTER_NULL) return null;
			return sKeys.get(sCurrentKeyIndex);
		}
		
		private static void click() {
			
		}
		
		private static int scanNextKey() {
			return -1;
		}
		
		private static int scanPreviousKey() {
			return  -1;
		}
		
		private static int scanNextRow() {
			return  -1;
		}
		
		private static int scanPreviousRow() {
			return  -1;
		}
		
		
	}

	
}
