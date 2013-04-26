package com.android.tecla.keyboard;

import android.view.ViewGroup;

public class WordPredictionAdapter {

	private static int[] sSuggestionsViewIndices = {0,2,4};
	private static ViewGroup sSuggestionsViewGroup;
	
	public static void setSuggestionsViewGroup(ViewGroup vg) {
		sSuggestionsViewGroup = vg;
	}
	
	public static void scanNext() {
		
	}
	
	public static void scanPrevious() {
		
	}
	
	public static void selectHighlighted() {
		
	}
	
	private static class WordPredictionStates {
		private static final int WPSCAN_NONE = 0x7777;
		private static final int WPSCAN_SUGGESTIONS = 0x8888;
		private static final int WPSCAN_MORESUGGESTIONS = 0x9999;
		private static final int WPSCAN_CLICK = 0xaaaa;
		private static int sState = WPSCAN_NONE;
		
		private static int sCurrentIndex;
		
		private static void reset() {
			
		}
		
		private static void click() {
			switch(sState) {
			case(WPSCAN_NONE):				sState = WPSCAN_SUGGESTIONS;
											break;
			case(WPSCAN_SUGGESTIONS):		break;
			case(WPSCAN_MORESUGGESTIONS):	break;
			case(WPSCAN_CLICK):				break;
			default:						break;
			}
		}
		
		
	}
}
