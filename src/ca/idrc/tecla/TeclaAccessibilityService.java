package ca.idrc.tecla;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaAccessibilityService extends AccessibilityService {

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Log.d("TeclaA11y", "Tecla Accessibility Service Connected!");
		
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int event_type = event.getEventType();
		Log.d("TeclaA11y", AccessibilityEvent.eventTypeToString(event_type) + ": " + event.getText());
		
	}

	@Override
	public void onInterrupt() {
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
