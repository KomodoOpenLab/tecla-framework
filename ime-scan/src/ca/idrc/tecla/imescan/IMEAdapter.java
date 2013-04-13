package ca.idrc.tecla.imescan;

import java.util.Iterator;
import java.util.List;

import com.example.android.softkeyboard.R;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IMEAdapter {

	private static Keyboard sKeyboard = null;
	private static KeyboardView sKeyboardView = null;
	private static List<Key> sKeys = null;
	private static int sRowCount = 0;
	private static int sCurrentRow =-1;
	private static int sCurrentKeyIndex = -1; 
	private static int sRowStartIndex = -1;
	private static int sRowEndIndex = -1;
		
	private static final int REDRAW_KEYBOARD = 0x22;
	
	private static Handler sHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			sKeyboardView.invalidateAllKeys();			
			super.handleMessage(msg);
		}
		
	};
	
	public static void setKeyboardView(KeyboardView kbv) {
		sKeyboardView = kbv;
		if(kbv == null) {
			sKeyboard = null;
			sKeys = null;
			sRowCount = 0;
			sCurrentRow = -1;
			sCurrentKeyIndex = -1;
			sRowStartIndex = -1;
			sRowEndIndex = -1;		
			return;
		}
		sKeyboard = kbv.getKeyboard();
		sKeys = sKeyboard.getKeys();
		sRowCount = getRowCount();
		sCurrentRow = 0;
		sCurrentKeyIndex = 0;
		sRowStartIndex = getRowStart(0);
		sRowEndIndex = getRowEnd(0);
	}
	
	private static void highlightKey(int key_index, boolean highlighted) {
		if(key_index<sRowStartIndex || key_index>sRowEndIndex) return;
        Key key = sKeys.get(key_index);
		key.pressed = highlighted;
	}
	
	
	private static void highlightKeys(int start_index, int end_index, boolean highlighted) {
		if(start_index==-1 || end_index==-1) return;
		for(int i=start_index; i<=end_index; ++i) {
			highlightKey(i, highlighted);
		}			
	}

	private static void invalidateKeys() {
		Message msg = new Message();
		msg.what = IMEAdapter.REDRAW_KEYBOARD;
		sHandler.dispatchMessage(msg);		
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
		if(sCurrentRow >= sRowCount) sCurrentRow = -1;
		sRowStartIndex = getRowStart(sCurrentRow);
		sRowEndIndex = getRowEnd(sCurrentRow);
		sCurrentKeyIndex = -1;
		highlightKeys(sRowStartIndex, sRowEndIndex, true);
		invalidateKeys();		
	}
	
	public static void highlightPreviousRow() {
		if(sKeyboard ==null) return;
		highlightKeys(sRowStartIndex, sRowEndIndex, false);
		if(sCurrentRow < 0) sCurrentRow = sRowCount - 1;
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
	
}
