package ca.idrc.tecla;

import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityNode implements Comparable {

	AccessibilityNodeInfo mNode;
	
	public TeclaAccessibilityNode(AccessibilityNodeInfo node) {
		mNode = node;
	}

	@Override
	public int compareTo(Object obj) {
		
		return 0;
	}

}
