package com.android.tecla.addon;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.latin.LatinIME;

public class LatinIMEAdapter implements TeclaIMEAdapter {

	private static final String CLASS_TAG = "LatinIMEAdapter";
	
	private enum IMEState {
		STOPPED,
		ROW,
		COLUMN,
		CLICK,
		WORDPREDICTION
	}
	private IMEState mState = IMEState.STOPPED;

	private int mRowCount, mCurrentRow, mCurrentColumn, mKeyStartIndex, mKeyEndIndex;
	
	private Keyboard mKeyboard;
	private KeyboardView mKeyboardView;
	private static Key[] mKeys;
		
	private static final int REDRAW_KEYBOARD = 0x22;
	
	private Lock mScanStateLock;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case(REDRAW_KEYBOARD):	mKeyboardView.invalidateAllKeys();
									break;
			default:				break;
			}	
			super.handleMessage(msg);
		}
	};
	
	private static TeclaIMEAdapter sTeclaIMEAdapter;
	protected static TeclaIMEAdapter getIMEAdapter() {
		if(sTeclaIMEAdapter == null)
			sTeclaIMEAdapter = new LatinIMEAdapter();
		return sTeclaIMEAdapter;
	}
	
	private LatinIMEAdapter() {

		mRowCount = 0;
		mCurrentRow = -1;
		mCurrentColumn = -1; 
		mKeyStartIndex = -1;
		mKeyEndIndex = -1;
		
		mKeyboard = null;
		mKeyboardView = null;
		mKeys = null;
		
		mScanStateLock = new ReentrantLock();
	}
	
	@Override
	public void scanDown() {
		int index = getCurrentKeyIndex();
		Key key = mKeys[index];
		Key test_key;
		int closest_key_index = -1;
		float distance_sq, shortest_distance_sq;
		shortest_distance_sq = Float.MAX_VALUE;
		for(int i=0; i<mKeys.length; ++i) {
			if(i == index) continue;
			test_key = mKeys[i];
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
			highlightKey(index, false);
			setKey(closest_key_index);
			highlightKey(closest_key_index, true);
		}
		invalidateKeys();
	}

	@Override
	public void scanLeft() {
		int index = getCurrentKeyIndex();
		Key key = mKeys[index];
		Key test_key;
		int closest_key_index = -1;
		int distance;
		int shortest_distance = Integer.MAX_VALUE;
		for(int i=0; i<mKeys.length; ++i) {
			if(i == index) continue;
			test_key = mKeys[i];
			if(test_key.mY == key.mY && test_key.mX < key.mX) {
				distance = key.mX - test_key.mX;
				if(distance < shortest_distance) {
					shortest_distance = distance;
					closest_key_index = i;
				}
			}
		}
		if(closest_key_index != -1) {
			highlightKey(index, false);
			setKey(closest_key_index);
			highlightKey(closest_key_index, true);
		}
		invalidateKeys();
	}

	@Override
	public void scanRight() {
		int index = getCurrentKeyIndex();
		Key key = mKeys[index];
		Key test_key;
		int closest_key_index = -1;
		int distance;
		int shortest_distance = Integer.MAX_VALUE;
		for(int i=0; i<mKeys.length; ++i) {
			if(i == index) continue;
			test_key = mKeys[i];
			if(test_key.mY == key.mY && test_key.mX > key.mX) {
				distance = test_key.mX - key.mX;
				if(distance < shortest_distance) {
					shortest_distance = distance;
					closest_key_index = i;
				}
			}
		}
		if(closest_key_index != -1) {
			highlightKey(index, false);
			setKey(closest_key_index);
			highlightKey(closest_key_index, true);
		}
		invalidateKeys();
	}

	@Override
	public void scanUp() {
		int index = getCurrentKeyIndex();
		Key key = mKeys[index];
		Key test_key;
		int closest_key_index = -1;
		float distance_sq, shortest_distance_sq;
		shortest_distance_sq = Float.MAX_VALUE;
		for(int i=0; i<mKeys.length; ++i) {
			if(i == index) continue;
			test_key = mKeys[i];
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
			highlightKey(index, false);
			setKey(closest_key_index);
			highlightKey(closest_key_index, true);
		}
		invalidateKeys();
	}

	@Override
	public void scanNext() {
		try {
			if(!mScanStateLock.tryLock(500, TimeUnit.MILLISECONDS)) return;
		} catch (InterruptedException e) {
			Log.e(CLASS_TAG, e.toString());
			e.printStackTrace();
		}

		if(mKeyboardView == null){
			mScanStateLock.unlock();
			return;
		}
		Keyboard keyboard = mKeyboardView.getKeyboard();
		if(mKeyboard != keyboard) {
			setKeyboardView(mKeyboardView);
			mState = IMEState.ROW;
		}
		
		switch(mState) {
		case STOPPED:	break;
		case ROW:		highlightNextRow();
										break;
		case COLUMN:	highlightNextKey();
										break;
		case CLICK:		mState = IMEState.ROW;
						highlightKey(getCurrentKeyIndex(), false);
						reset();
						highlightNextRow();		
						break;
		case WORDPREDICTION:	if(!WordPredictionAdapter.highlightNext()) {
									mState = IMEState.ROW;
									reset();
									mCurrentRow = mRowCount;
									highlightNextRow();	
								}
								break;
		default:		break;
		}	
		mScanStateLock.unlock();
	}

	@Override
	public void scanPrevious() {
		switch(mState) {
		case STOPPED:	break;
		case ROW:		highlightPreviousRow();
										break;
		case COLUMN:	highlightPreviousKey();
		case CLICK:		mState = IMEState.ROW;
						reset();
						highlightPreviousRow();	
						break;
		default:		break;
		}	
	}

	@Override
	public void stepOut() {
		if(mState != IMEState.ROW) {
			int index = getCurrentKeyIndex();
			highlightKey(index, false);
			mState = IMEState.ROW;
			reset();
			highlightKeys(getRowStart(0), 
			getRowEnd(0), true);
		}
	}

	@Override
	public void selectScanHighlighted() {
		try {
			if(!mScanStateLock.tryLock(500, TimeUnit.MILLISECONDS)) return;
		} catch (InterruptedException e) {
			Log.e(CLASS_TAG, e.toString());
			e.printStackTrace();
		}
		
		switch(mState) {
		case STOPPED:	mState = IMEState.ROW;
						AutomaticScan.startAutoScan();
						break;
		case ROW:		if(mCurrentRow == mRowCount) {
							mState = IMEState.WORDPREDICTION;
							WordPredictionAdapter.selectHighlighted();
						} else if(mCurrentRow == mRowCount + 1) {
							//TeclaApp.ime.requestHideSelf(0);
							TeclaApp.ime.hideWindow();
							TeclaApp.overlay.hidePreviewHUD();
							TeclaApp.overlay.show();
						} else {									
							mState = IMEState.COLUMN;
							highlightKeys(mKeyStartIndex, mKeyEndIndex, false);
						}
						AutomaticScan.resetTimer();
						break;
		case COLUMN:	if(mCurrentColumn == -1) {
							mState = IMEState.ROW;
							AutomaticScan.resetTimer();
							highlightKeys(0, mKeys.length - 1, false);
							break;
						}
						mState = IMEState.CLICK;
						selectHighlighted();
						AutomaticScan.setExtendedTimer();
						break;
		case CLICK:		AutomaticScan.setExtendedTimer();
						if(getCurrentRowIndex() == getRowCount())
							WordPredictionAdapter.selectHighlighted();
						else
							selectHighlighted();
						break;
		case WORDPREDICTION:	if(!WordPredictionAdapter.selectHighlighted()) {
									mState = IMEState.CLICK;
								}
								AutomaticScan.setExtendedTimer();
								break;
		default:		break;
		}
		mScanStateLock.unlock();
	}

	@Override
	public boolean isShowingKeyboard() {
		if(mKeyboardView == null) 
			return false;
		return true;
	}

	@Override
	public boolean setKeyboardView(KeyboardView kbv) {
		mKeyboardView = kbv;
		if(kbv == null) {
			mKeyboard = null;
			mKeys = null;	
			return false;
		}
		mKeyboard = kbv.getKeyboard();
		if(mKeyboard == null || mKeyboard.mKeys == null) return false;
		mKeys = sortKeys(mKeyboard.mKeys);
		reset();
		return true;
	}

	private Key[] sortKeys(Key[] keys) {
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

	private void selectHighlighted() {
		if(mKeyboard != mKeyboardView.getKeyboard()) return;		
		int index = getCurrentKeyIndex();
		if(index < 0 || index >= mKeys.length) return;
		Key key = mKeys[index];
		LatinIME ime = (LatinIME)TeclaApp.ime;
		ime.onPressKey(key.mCode);
		ime.onReleaseKey(key.mCode, false);
		ime.onCodeInput(key.mCode, key.mX, key.mY);		
	}

	private void reset() {
		if(mKeyboard == null) return;
		mRowCount = getRowCount();
		mCurrentRow = -1;
		mCurrentColumn = -1;
		mKeyStartIndex = getRowStart(0);
		mKeyEndIndex = getRowEnd(0);
	}

	private int getRowStart(int rowNumber) {
		if(mKeyboard == null || rowNumber == -1 || rowNumber >= mRowCount) return -1;
		int keyCounter = 0;
		if (rowNumber != 0) {
			Key[] keyList = mKeys;
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

	private int getRowEnd(int rowNumber) {
		if(mKeyboard == null || rowNumber == -1 || rowNumber >= mRowCount) return -1;
		Key[] keyList = mKeys;
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

	private int getRowCount() {
		if(mKeyboard == null) return 0;
		Key[] keyList = mKeys;
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
	
	private int getCurrentKeyIndex() {
		return mCurrentColumn;
	}

	private int getCurrentRowIndex() {
		return mCurrentRow;
	}
	
	private boolean setKey(int index) {
		if(index < 0 || index >= mKeys.length) return false;
		mCurrentColumn = index;
		return true;
	}

	private boolean setKeyRow(int index) {
		if(index < 0 || index >= mRowCount) return false;
		mCurrentRow = index;
		return true;
	}
	
	private int scanNextKey() {
		if(mCurrentColumn == -1) mCurrentColumn = mKeyStartIndex;
		else {
			++mCurrentColumn;
			if(mCurrentColumn > mKeyEndIndex) mCurrentColumn = -1;
		}
		return mCurrentColumn;
	}
	
	private int scanPreviousKey() {
		if(mCurrentColumn == -1) mCurrentColumn = mKeyEndIndex;
		else {
			--mCurrentColumn;
			if(mCurrentColumn < mKeyStartIndex) mCurrentColumn = -1;
		}
		return mCurrentColumn;
	}
	
	private void scanNextRow() {
		if(mCurrentRow == mRowCount ) {
			WordPredictionAdapter.highlightNext();
		} else if(mCurrentRow == mRowCount + 1) {
			TeclaApp.overlay.hidePreviewHUD();
		} else highlightKeys(mKeyStartIndex, mKeyEndIndex, false);
		++mCurrentRow;
		mCurrentRow %= mRowCount + 2;
		updateRowKeyIndices();
		if(mCurrentRow == mRowCount) {
			if(!WordPredictionAdapter.sSuggestionsViewGroup.isShown()) 
				++mCurrentRow;
			else
				WordPredictionAdapter.highlightNext();
		}
		if(mCurrentRow == mRowCount + 1) {
			TeclaApp.overlay.showPreviewHUD();
		} else highlightKeys(mKeyStartIndex, mKeyEndIndex, true);
	}
	
	private void scanPreviousRow() {
		if(mCurrentRow == -1) mCurrentRow = mRowCount;
		else --mCurrentRow;
		updateRowKeyIndices();
	}
	
	private void updateRowKeyIndices() {
		mKeyStartIndex = getRowStart(mCurrentRow);
		mKeyEndIndex = getRowEnd(mCurrentRow);			
	}

	private void highlightKey(int key_index, boolean highlighted) {
		if(mKeys == null || key_index < 0 || key_index >= mKeys.length) return; 
        Key key = mKeys[key_index];
        if(highlighted) key.onPressed();
        else key.onReleased();
	}	
	
	private void highlightKeys(int start_index, int end_index, boolean highlighted) {
		for(int i=start_index; i<=end_index; ++i) {
			highlightKey(i, highlighted);
		}			
	}

	private void invalidateKeys() {
		Message msg = new Message();
		msg.what = REDRAW_KEYBOARD;
		mHandler.sendMessageDelayed(msg, 0);		
	}
	
	private void highlightNextKey() {
		if(mKeyboard == null) return;
		if(getCurrentKeyIndex() > -1) 
			highlightKey(getCurrentKeyIndex(), false);
		else 
			highlightKeys(0, mKeys.length - 1, false);
		int nextkey = scanNextKey();
		if(nextkey > -1) 
			highlightKey(nextkey, true);
		else 
			highlightKeys(0, mKeys.length - 1, true);
		invalidateKeys();	
	}
	
	private void highlightPreviousKey() {
		if(mKeyboard ==null) return;
		highlightKey(getCurrentKeyIndex(), false);
		highlightKey(scanPreviousKey(), true);
		invalidateKeys();		
	}
	
	private void highlightNextRow() {
		if(mKeyboard ==null) return;
		scanNextRow();
		invalidateKeys();		
	}
	
	private void highlightPreviousRow() {
		if(mKeyboard ==null) return;
		highlightKeys(mKeyStartIndex, mKeyEndIndex, false);
		scanPreviousRow();
		highlightKeys(mKeyStartIndex, mKeyEndIndex, true);
		invalidateKeys();		
	}	
}
