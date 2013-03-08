package ca.idrc.tecla;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaShieldControl {
	private TeclaShieldControlView mView;
	private ReentrantLock mActionLock;

	public TeclaShieldControl(TeclaShieldControlView mView) {
		super();
		this.mView = mView;
		mActionLock = new ReentrantLock();
		setSelected("Up");
	}

    public TeclaShieldControlUnit getSelected () {
    	TeclaShieldControlUnit result = null; 
    	for (TeclaShieldControlUnit cu: mView.mControlUnits) {
    		if(cu.isSelected()) {
    			result = cu;
    			break; 
    		}
    	}
    	return result; 
    }
    
    public void performAction(AccessibilityNodeInfo node) {
    	TeclaAccessibilityService.NodeSelectionThread thread = null;    	
    	for (TeclaShieldControlUnit cu: mView.mControlUnits) {
    		if(cu.isSelected()) {
    			if(cu.mText.equals("Up"))     {
    				thread = new TeclaAccessibilityService.NodeSelectionThread(node, 
    						TeclaAccessibilityService.DIRECTION_UP,
    						mActionLock);
    			} else if(cu.mText.equals("Left"))     {
    				thread = new TeclaAccessibilityService.NodeSelectionThread(node, 
    						TeclaAccessibilityService.DIRECTION_LEFT,
    						mActionLock);
    			} else if(cu.mText.equals("Right"))     {
    				thread = new TeclaAccessibilityService.NodeSelectionThread(node, 
    						TeclaAccessibilityService.DIRECTION_RIGHT,
    						mActionLock);
    			} else if(cu.mText.equals("Down"))     {
    				thread = new TeclaAccessibilityService.NodeSelectionThread(node, 
    						TeclaAccessibilityService.DIRECTION_DOWN,
    						mActionLock);
    			} else if(cu.mText.equals("S"))     {
    				TeclaAccessibilityService.clickActiveNode();
    			}
				if(thread != null) thread.start();
    			break; 
    		}
    	}
    }
    
    public void setSelected(String text) {
    	for (TeclaShieldControlUnit cu: mView.mControlUnits) {
    		if(cu.mText.equals(text)) cu.setSelected(true);
    		else cu.setSelected(false);
    	}
    	mView.postInvalidate();
    }
    
    public void setPreviousSelected () {
    	TeclaShieldControlUnit previous = mView.mControlUnits.get(mView.mControlUnits.size() - 1);
    	for (TeclaShieldControlUnit cu: mView.mControlUnits) {
    		if(cu.isSelected()) {
    			cu.setSelected(false);
    			previous.setSelected(true);
    			mView.postInvalidate();
    			break;
    		}
    		else previous = cu;
    	}
    }

    public void setNextSelected () {
    	boolean select = false; 
    	for (TeclaShieldControlUnit cu: mView.mControlUnits) {
    		if(select) {
    			cu.setSelected(true);
    			mView.postInvalidate();
    			return; 
    		}
    		if(cu.isSelected()) {
    			cu.setSelected(false);
    			select = true; 
    		}
    	}
    	mView.mControlUnits.get(0).setSelected(true);
    	mView.postInvalidate();
    	
    }
    
	
}
