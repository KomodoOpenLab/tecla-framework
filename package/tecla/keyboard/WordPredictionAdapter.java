package com.android.tecla.keyboard;

import java.util.ArrayList;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class WordPredictionAdapter {

	private static final int[] SUGGESTIONSVIEWINDICES = {0,2,4};
	private static final int BACKGROUND_HIGHLIGHT_COLOR = Color.BLUE;
	private static final int BACKGROUND_NORMAL_COLOR = Color.DKGRAY;
	public static final String tag = "WordPredictionAdapter";
	
	public static ViewGroup sSuggestionsViewGroup = null;
	private static ArrayList<View> sWordChoices = new ArrayList<View>(); 
	
	public static void setSuggestionsViewGroup(ViewGroup vg) {
		sSuggestionsViewGroup = vg;
	}
	
	private static void populateWordChoices() {
		sWordChoices.clear();
		for(int i=0; i<sSuggestionsViewGroup.getChildCount(); i+=2) {
			sWordChoices.add(sSuggestionsViewGroup.getChildAt(i));
		}
	}
	
	public static boolean selectHighlighted() {
		if(sSuggestionsViewGroup == null) return false;
		if(sSuggestionsViewGroup.getVisibility() != ViewGroup.VISIBLE) return false;
		WordPredictionStates.click();
		return WordPredictionStates.sState != WordPredictionStates.WPSCAN_NONE;
	}

	public static boolean highlightNext() {
		if(sSuggestionsViewGroup == null) return false;
		if(sSuggestionsViewGroup.getVisibility() != ViewGroup.VISIBLE) return false;
		WordPredictionStates.scanNext();
		if(WordPredictionStates.sState == WordPredictionStates.WPSCAN_NONE) return false;
		else return true;
	}

	public static void highlightPrevious() {
		if(sSuggestionsViewGroup == null) return;
		if(sSuggestionsViewGroup.getVisibility() != ViewGroup.VISIBLE) return;
		WordPredictionStates.scanPrevious();
	}
	
	private static void highlightSuggestion(int index, boolean highlight) {
		if(index < 0 || index >= sWordChoices.size()) return;
		View view = sWordChoices.get(index);
		if(highlight) {
			view.setBackgroundColor(BACKGROUND_HIGHLIGHT_COLOR);
		} else {
			view.setBackgroundColor(BACKGROUND_NORMAL_COLOR);
		}
	}
	
	private static void highlightSuggestions(boolean highlight) {
		for(int i=0; i<sWordChoices.size(); ++i) {
			highlightSuggestion(i, highlight);
		}
	}
	
	private static void invalidateKeys() {
		sSuggestionsViewGroup.invalidate();
	}
	
	private static class WordPredictionStates {
		private static final int WPSCAN_NONE = 0x66;
		private static final int WPSCAN_HIGHLIGHTED = 0x77;
		private static final int WPSCAN_SUGGESTIONS = 0x88;
		private static final int WPSCAN_MORESUGGESTIONS = 0x99;
		private static final int WPSCAN_CLICK = 0xaa;
		private static int sState = WPSCAN_NONE;
		
		private static int sCurrentIndex = -1;
		
		private static void reset() {
			sState = WPSCAN_HIGHLIGHTED;
			sCurrentIndex = -1;
		}
		
		private static void scanNext() {
			switch(sState) {
			case(WPSCAN_NONE):		populateWordChoices();
									highlightSuggestions(true);
									sState = WPSCAN_HIGHLIGHTED;
									break;
			case(WPSCAN_HIGHLIGHTED):		populateWordChoices();
											highlightSuggestions(false);
											sState = WPSCAN_NONE;
											break;
			case(WPSCAN_SUGGESTIONS):		populateWordChoices();
											highlightSuggestion(sCurrentIndex++, false);
											sCurrentIndex %= SUGGESTIONSVIEWINDICES.length + 1;
											highlightSuggestion(sCurrentIndex, true);
											break;
			case(WPSCAN_CLICK):		populateWordChoices();
									highlightSuggestion(sCurrentIndex, false);
									sState = WPSCAN_NONE;
									break;
			default:				break;
			}
			invalidateKeys();
		}
		
		private static void scanPrevious() {
			switch(sState) {
			case(WPSCAN_NONE):		highlightSuggestion(0, true);
									highlightSuggestion(1, true);
									highlightSuggestion(2, true);
									sState = WPSCAN_HIGHLIGHTED;
									break;
			case(WPSCAN_HIGHLIGHTED):		highlightSuggestion(0, false);
											highlightSuggestion(1, false);
											highlightSuggestion(2, false);
											sState = WPSCAN_NONE;
											break;
			case(WPSCAN_SUGGESTIONS):		highlightSuggestion(sCurrentIndex--, false);
											if (sCurrentIndex < 0)
												sCurrentIndex = SUGGESTIONSVIEWINDICES.length;
											highlightSuggestion(sCurrentIndex, true);
			default:						break;
			}
		}
		
		private static void click() {
			switch(sState) {
			case(WPSCAN_NONE):				break;
			case(WPSCAN_HIGHLIGHTED):		populateWordChoices();
											highlightSuggestions(false);
											sState = WPSCAN_SUGGESTIONS;
											sCurrentIndex = 0;
											highlightSuggestion(sCurrentIndex, true);
											invalidateKeys();
											break;
			case(WPSCAN_SUGGESTIONS):		highlightSuggestion(sCurrentIndex, false);
											invalidateKeys();
											sState = WPSCAN_NONE;
											if(sCurrentIndex<0 || sCurrentIndex>=sWordChoices.size()) break;
											View view = sWordChoices.get(sCurrentIndex);
											view.callOnClick();
											break;
			default:						break;
			}
		}
		
		
	}
}
