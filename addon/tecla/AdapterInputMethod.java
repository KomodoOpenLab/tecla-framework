package com.android.tecla;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.latin.LatinIME;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;

public class AdapterInputMethod {

	private static final String tag = "IMEAdapter";
	
	private static Keyboard sKeyboard = null;
	private static KeyboardView sKeyboardView = null;
	private static Key[] sKeys = null;
		
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
	
	public static boolean setKeyboardView(KeyboardView kbv) {
		sKeyboardView = kbv;
		if(kbv == null) {
			sKeyboard = null;
			sKeys = null;	
			return false;
		}
		sKeyboard = kbv.getKeyboard();
		if(sKeyboard == null || sKeyboard.mKeys == null) return false;
		sKeys = sortKeys(sKeyboard.mKeys);
		IMEStates.reset();
		return true;
	}
	
	private static Key[] sortKeys(Key[] keys) {
		Key[] sorted_keys = keys.clone();
		
		// sort rows
		for(int i=1; i<sorted_keys.length; ++i) {
			Key key = sorted_keys[i];
			boolean inserted = false;
			for(int j=i; j>0 && !inserted; --j) {
				if(key.mY >= sorted_keys[j-1].mY) {
					sorted_keys[j] = key; 
					inserted = true;
				} else if(j==1) {
					sorted_keys[j] = sorted_keys[j-1]; 
					sorted_keys[j-1] = key;
				} else {
					sorted_keys[j] = sorted_keys[j-1]; 
				}
			}
		}
		
		// sort columns
		int start_index = 0;
		int end_index = 0;
		while(start_index < sorted_keys.length) {
			while(end_index<sorted_keys.length ) {
				if(sorted_keys[start_index].mY != sorted_keys[end_index].mY) break;
				++end_index;
			}
			for(int i=start_index + 1; i<end_index; ++i) {
				Key key = sorted_keys[i];
				boolean inserted = false;
				for(int j=i; j>start_index && !inserted; --j) {
					if(key.mX >= sorted_keys[j-1].mX) {
						sorted_keys[j] = key; 
						inserted = true;
					} else if(j==start_index+1) {
						sorted_keys[j] = sorted_keys[j-1]; 
						sorted_keys[j-1] = key;
					} else {
						sorted_keys[j] = sorted_keys[j-1]; 
					}
				}
			}
			start_index = end_index;
		}
		
		return sorted_keys;
		
	}
	
	public static boolean isShowingKeyboard() {
		if(sKeyboardView == null) return false;
		return true;
	}
	
	public static void selectHighlighted() {
		if(sKeyboard != sKeyboardView.getKeyboard()) return;		
		int index = IMEStates.getCurrentKeyIndex();
		if(index < 0 || index >= sKeys.length) return;
		Key key = sKeys[index];
		LatinIME ime = (LatinIME)TeclaApp.ime;
		ime.onPressKey(key.mCode);
		ime.onReleaseKey(key.mCode, false);
		ime.onCodeInput(key.mCode, key.mX, key.mY);
		//TeclaIME.getInstance().getCurrentInputConnection()
		//	.commitText(String.valueOf((char)key.mCode), 1);		
	}

	public static void selectScanHighlighted() {
		IMEStates.click();
	}
	
	public static void cancelScanHighlighted() {
		// stub for later issue
	}

	public static void scanNext() {
		try {
			if(!IMEStates.sScanStateLock.tryLock(500, TimeUnit.MILLISECONDS)) return;
		} catch (InterruptedException e) {
			Log.e(tag, e.toString());
			e.printStackTrace();
		}

		if(sKeyboardView == null){
			IMEStates.sScanStateLock.unlock();
			return;
		}
		Keyboard keyboard = sKeyboardView.getKeyboard();
		if(sKeyboard != keyboard) {
			setKeyboardView(sKeyboardView);
			IMEStates.sState = IMEStates.SCAN_ROW;
		}
		
		switch(IMEStates.sState) {
		case(IMEStates.SCAN_STOPPED):	break;
		case(IMEStates.SCAN_ROW):		AdapterInputMethod.highlightNextRow();
										break;
		case(IMEStates.SCAN_COLUMN):	AdapterInputMethod.highlightNextKey();
										break;
		case(IMEStates.SCAN_CLICK):		IMEStates.sState = IMEStates.SCAN_ROW;
										highlightKey(IMEStates.getCurrentKeyIndex(), false);
										IMEStates.reset();
										AdapterInputMethod.highlightNextRow();		
										break;
		case(IMEStates.SCAN_WORDPREDICTION):if(!AdapterWordPrediction.highlightNext()) {
												IMEStates.sState = IMEStates.SCAN_ROW;
												IMEStates.reset();
												IMEStates.sCurrentRow = IMEStates.sRowCount;
												AdapterInputMethod.highlightNextRow();	
											}
											break;
		default:						break;
		}	
		IMEStates.sScanStateLock.unlock();
	}
	
	public static void scanPrevious() {
		switch(IMEStates.sState) {
		case(IMEStates.SCAN_STOPPED):	break;
		case(IMEStates.SCAN_ROW):		AdapterInputMethod.highlightPreviousRow();
										break;
		case(IMEStates.SCAN_COLUMN):	AdapterInputMethod.highlightPreviousKey();
										break;
		case(IMEStates.SCAN_CLICK):		IMEStates.sState = IMEStates.SCAN_ROW;
										IMEStates.reset();
										AdapterInputMethod.highlightPreviousRow();	
										break;
		default:						break;
		}		
		
	}

	public static void scanUp() {
		int index = IMEStates.getCurrentKeyIndex();
		Key key = sKeys[index];
		Key test_key;
		int closest_key_index = -1;
		float distance_sq, shortest_distance_sq;
		shortest_distance_sq = Float.MAX_VALUE;
		for(int i=0; i<sKeys.length; ++i) {
			if(i == index) continue;
			test_key = sKeys[i];
			if(test_key.mY < key.mY) {
				distance_sq = (key.mY - test_key.mY)*(key.mY - test_key.mY) + 
						(key.mX - test_key.mX)*(key.mX - test_key.mX); 
				if(distance_sq < shortest_distance_sq) {
					shortest_distance_sq = distance_sq;
					closest_key_index = i;
				}
			}
		}
		if(closest_key_index != -1) {
			AdapterInputMethod.highlightKey(index, false);
			IMEStates.setKey(closest_key_index);
			AdapterInputMethod.highlightKey(closest_key_index, true);
		}
		AdapterInputMethod.invalidateKeys();
	}

	public static void scanDown() {
		int index = IMEStates.getCurrentKeyIndex();
		Key key = sKeys[index];
		Key test_key;
		int closest_key_index = -1;
		float distance_sq, shortest_distance_sq;
		shortest_distance_sq = Float.MAX_VALUE;
		for(int i=0; i<sKeys.length; ++i) {
			if(i == index) continue;
			test_key = sKeys[i];
			if(test_key.mY > key.mY) {
				distance_sq = (key.mY - test_key.mY)*(key.mY - test_key.mY) + 
						(key.mX - test_key.mX)*(key.mX - test_key.mX); 
				if(distance_sq < shortest_distance_sq) {
					shortest_distance_sq = distance_sq;
					closest_key_index = i;
				}
			}
		}
		if(closest_key_index != -1) {
			AdapterInputMethod.highlightKey(index, false);
			IMEStates.setKey(closest_key_index);
			AdapterInputMethod.highlightKey(closest_key_index, true);
		}
		AdapterInputMethod.invalidateKeys();
	}

	public static void scanLeft() {
		int index = IMEStates.getCurrentKeyIndex();
		Key key = sKeys[index];
		Key test_key;
		int closest_key_index = -1;
		int distance;
		int shortest_distance = Integer.MAX_VALUE;
		for(int i=0; i<sKeys.length; ++i) {
			if(i == index) continue;
			test_key = sKeys[i];
			if(test_key.mY == key.mY && test_key.mX < key.mX) {
				distance = key.mX - test_key.mX;
				if(distance < shortest_distance) {
					shortest_distance = distance;
					closest_key_index = i;
				}
			}
		}
		if(closest_key_index != -1) {
			AdapterInputMethod.highlightKey(index, false);
			IMEStates.setKey(closest_key_index);
			AdapterInputMethod.highlightKey(closest_key_index, true);
		}
		AdapterInputMethod.invalidateKeys();
	}

	public static void scanRight() {
		int index = IMEStates.getCurrentKeyIndex();
		Key key = sKeys[index];
		Key test_key;
		int closest_key_index = -1;
		int distance;
		int shortest_distance = Integer.MAX_VALUE;
		for(int i=0; i<sKeys.length; ++i) {
			if(i == index) continue;
			test_key = sKeys[i];
			if(test_key.mY == key.mY && test_key.mX > key.mX) {
				distance = test_key.mX - key.mX;
				if(distance < shortest_distance) {
					shortest_distance = distance;
					closest_key_index = i;
				}
			}
		}
		if(closest_key_index != -1) {
			AdapterInputMethod.highlightKey(index, false);
			IMEStates.setKey(closest_key_index);
			AdapterInputMethod.highlightKey(closest_key_index, true);
		}
		AdapterInputMethod.invalidateKeys();
	}

	public static void stepOut() {
		if(IMEStates.sState != IMEStates.SCAN_ROW) {
			int index = IMEStates.getCurrentKeyIndex();
			AdapterInputMethod.highlightKey(index, false);
			IMEStates.sState = IMEStates.SCAN_ROW;
			IMEStates.reset();
			AdapterInputMethod.highlightKeys(IMEStates.getRowStart(0), 
					IMEStates.getRowEnd(0), true);
		}
	}
	
	
	private static void highlightKey(int key_index, boolean highlighted) {
		if(sKeys == null || key_index < 0 || key_index >= sKeys.length) return; 
        Key key = sKeys[key_index];
        if(highlighted) key.onPressed();
        else key.onReleased();
	}	
	
	private static void highlightKeys(int start_index, int end_index, boolean highlighted) {
		for(int i=start_index; i<=end_index; ++i) {
			highlightKey(i, highlighted);
		}			
	}

	private static void invalidateKeys() {
		Message msg = new Message();
		msg.what = AdapterInputMethod.REDRAW_KEYBOARD;
		sHandler.sendMessageDelayed(msg, 0);		
	}
	
	private static void highlightNextKey() {
		if(sKeyboard == null) return;
		if(IMEStates.getCurrentKeyIndex() > -1) 
			highlightKey(IMEStates.getCurrentKeyIndex(), false);
		else 
			highlightKeys(0, sKeys.length - 1, false);
		int nextkey = IMEStates.scanNextKey();
		if(nextkey > -1) 
			highlightKey(nextkey, true);
		else 
			highlightKeys(0, sKeys.length - 1, true);
		invalidateKeys();	
	}
	
	private static void highlightPreviousKey() {
		if(sKeyboard ==null) return;
		highlightKey(IMEStates.getCurrentKeyIndex(), false);
		highlightKey(IMEStates.scanPreviousKey(), true);
		invalidateKeys();		
	}
	
	private static void highlightNextRow() {
		if(sKeyboard ==null) return;
		IMEStates.scanNextRow();
		invalidateKeys();		
	}
	
	private static void highlightPreviousRow() {
		if(sKeyboard ==null) return;
		highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, false);
		IMEStates.scanPreviousRow();
		highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, true);
		invalidateKeys();		
	}
	
	private static class IMEStates {

		private static final int SCAN_STOPPED = 0xa0;
		private static final int SCAN_ROW = 0xa1;
		private static final int SCAN_COLUMN = 0xa2;
		private static final int SCAN_CLICK = 0xa3;
		private static final int SCAN_WORDPREDICTION = 0xa4;
		private static int sState = SCAN_CLICK;
		
		private static Lock sScanStateLock = new ReentrantLock();
		
		private static int sRowCount = 0;
		private static int sCurrentRow = -1;
		private static int sCurrentColumn = -1; 
		private static int sKeyStartIndex = 0;
		private static int sKeyEndIndex = 0;
		
		private static void reset() {
			if(sKeyboard == null) return;
			sRowCount = getRowCount(); 
			sCurrentRow = -1;
			sCurrentColumn = -1;
			sKeyStartIndex = getRowStart(0);
			sKeyEndIndex = getRowEnd(0);
		}

		private static void click() {
			try {
				if(!sScanStateLock.tryLock(500, TimeUnit.MILLISECONDS)) return;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.e(tag, e.toString());
				e.printStackTrace();
			}
			switch(sState) {
			case(SCAN_STOPPED):		sState = SCAN_ROW;
									if(TeclaApp.getInstance().persistence.isSelfScanningSelected()){
									ManagerAutoScan.start();}
									break;
			case(SCAN_ROW):			if(sCurrentRow == sRowCount) {
										sState = SCAN_WORDPREDICTION;
										AdapterWordPrediction.selectHighlighted();
									} else if(sCurrentRow == sRowCount + 1||sCurrentRow == -1) {
										//TeclaApp.ime.requestHideSelf(0);
										IMEStates.reset();
										TeclaApp.ime.hideWindow();
										TeclaApp.a11yservice.getHUD().hide();
										TeclaApp.a11yservice.getHUD().setPreviewHUD(false);
										TeclaApp.a11yservice.showFeedback();
										TeclaApp.persistence.setIMEShowing(false);
									} else {									
										sState = SCAN_COLUMN;
										highlightKeys(sKeyStartIndex, sKeyEndIndex, false);
									}
									ManagerAutoScan.resetTimer();
									scanNext();
									break;
			case(SCAN_COLUMN):		if(IMEStates.sCurrentColumn == -1) {
										sState = SCAN_ROW;
										ManagerAutoScan.resetTimer();
										highlightKeys(0, sKeys.length - 1, false);
										break;
									}
									sState = SCAN_CLICK;
									AdapterInputMethod.selectHighlighted();
									ManagerAutoScan.setExtendedTimer();
									break;
			case(SCAN_CLICK):		ManagerAutoScan.setExtendedTimer();
									if(getCurrentRowIndex() == getRowCount())
										AdapterWordPrediction.selectHighlighted();
									else
										AdapterInputMethod.selectHighlighted();
									break;
			case(SCAN_WORDPREDICTION):	if(!AdapterWordPrediction.selectHighlighted()) {
												sState = SCAN_CLICK;
										}
										ManagerAutoScan.setExtendedTimer();
										break;
			default:			break;
			}
			sScanStateLock.unlock();
		}
		
		private static int getCurrentKeyIndex() {
			return sCurrentColumn;
		}

		private static int getCurrentRowIndex() {
			return sCurrentRow;
		}
		
		private static boolean setKey(int index) {
			if(index < 0 || index >= sKeys.length) return false;
			sCurrentColumn = index;
			return true;
		}

		private static boolean setKeyRow(int index) {
			if(index < 0 || index >= IMEStates.sRowCount) return false;
			sCurrentRow = index;
			return true;
		}
		
		private static int scanNextKey() {
			if(sCurrentColumn == -1) sCurrentColumn = sKeyStartIndex;
			else {
				++sCurrentColumn;
				if(sCurrentColumn > sKeyEndIndex) sCurrentColumn = -1;
			}
			return sCurrentColumn;
		}
		
		private static int scanPreviousKey() {
			if(sCurrentColumn == -1) sCurrentColumn = sKeyEndIndex;
			else {
				--sCurrentColumn;
				if(sCurrentColumn < sKeyStartIndex) sCurrentColumn = -1;
			}
			return sCurrentColumn;
		}
		
		private static void scanNextRow() {
			if(IMEStates.sCurrentRow == IMEStates.sRowCount ) {
				AdapterWordPrediction.highlightNext();
			} else if(IMEStates.sCurrentRow == IMEStates.sRowCount + 1) {
				TeclaApp.a11yservice.getHUD().hide();
				TeclaApp.a11yservice.getHUD().setPreviewHUD(false);
			} else highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, false);
			++sCurrentRow;
			sCurrentRow %= sRowCount + 2;
			updateRowKeyIndices();
			if(IMEStates.sCurrentRow == IMEStates.sRowCount) {
				if(!AdapterWordPrediction.sSuggestionsViewGroup.isShown()) 
					++sCurrentRow;
				else
					AdapterWordPrediction.highlightNext();
			}
			if(IMEStates.sCurrentRow == IMEStates.sRowCount + 1) {
				TeclaApp.a11yservice.getHUD().setPreviewHUD(true);
				TeclaApp.a11yservice.getHUD().show();
			} else {
				TeclaApp.a11yservice.getHUD().hide();
				TeclaApp.a11yservice.getHUD().setPreviewHUD(false);
				highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, true);
				}
		}
		
		private static void scanPreviousRow() {
			if(IMEStates.sCurrentRow == IMEStates.sRowCount ) {
				AdapterWordPrediction.highlightNext();
			} else if(IMEStates.sCurrentRow == -1) {
				TeclaApp.a11yservice.getHUD().setPreviewHUD(true);
				TeclaApp.a11yservice.getHUD().show();
			} else highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, false);
			--sCurrentRow;
			if (sCurrentRow==-2){
				if(AdapterWordPrediction.sSuggestionsViewGroup.isShown())sCurrentRow = sRowCount;
				else sCurrentRow = sRowCount-1;}
			updateRowKeyIndices();
			if(IMEStates.sCurrentRow == IMEStates.sRowCount) {
				if(!AdapterWordPrediction.sSuggestionsViewGroup.isShown()) 
					--sCurrentRow;
				else
					AdapterWordPrediction.highlightNext();
			}
			if(IMEStates.sCurrentRow == -1) {
				TeclaApp.a11yservice.getHUD().setPreviewHUD(true);
				TeclaApp.a11yservice.getHUD().show();
			} else {
				TeclaApp.a11yservice.getHUD().hide();
				TeclaApp.a11yservice.getHUD().setPreviewHUD(false);
				highlightKeys(IMEStates.sKeyStartIndex, IMEStates.sKeyEndIndex, true);
				}
		}
		
		private static void updateRowKeyIndices() {
			sKeyStartIndex = getRowStart(sCurrentRow);
			sKeyEndIndex = getRowEnd(sCurrentRow);			
		}
		
		private static int getRowStart(int rowNumber) {
			if(sKeyboard == null || rowNumber == -1 || rowNumber >= sRowCount) return -1;
			int keyCounter = 0;
			if (rowNumber != 0) {
				Key[] keyList = sKeys;
				Key key;
				int rowCounter = 0;
				int prevCoord = keyList[0].mY;
				int thisCoord;
				while (rowCounter != rowNumber) {
					keyCounter++;
					key = keyList[keyCounter];
					thisCoord = key.mY;
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
			if(sKeyboard == null || rowNumber == -1 || rowNumber >= sRowCount) return -1;
			Key[] keyList = sKeys;
			int totalKeys = keyList.length;
			int keyCounter = 0;
			if (rowNumber == (getRowCount() - 1)) {
				keyCounter = totalKeys - 1;
			} else {
				Key key;
				int rowCounter = 0;
				int prevCoord = keyList[0].mY;
				int thisCoord;
				while (rowCounter <= rowNumber) {
					keyCounter++;
					key = keyList[keyCounter];
					thisCoord = key.mY;
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
			Key[] keyList = sKeys;
			Key key;
			int rowCounter = 0;
			int coord = 0;
			for(int i=0; i<keyList.length; ++i) {
				key = keyList[i];
				if (rowCounter == 0) {
					rowCounter++;
					coord = key.mY;
				}
				if (coord != key.mY) {
					rowCounter++;
					coord = key.mY;
				}
			}
			return rowCounter;
		}
			
	}
	
}
