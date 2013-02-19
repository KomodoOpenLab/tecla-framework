package ca.idrc.tecla;

public class TeclaShieldControl {
	private TeclaShieldControlView mView;

	public TeclaShieldControl(TeclaShieldControlView mView) {
		super();
		this.mView = mView;
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
    
    public void performAction() {
    	for (TeclaShieldControlUnit cu: mView.mControlUnits) {
    		if(cu.isSelected()) {
    			if(cu.mText.equals("Up"))     {
    				TeclaAccessibilityService.selectPreviousActiveNode();
    			} else if(cu.mText.equals("Left"))     {
    				TeclaAccessibilityService.scrollBackward();
    			} else if(cu.mText.equals("Right"))     {
    				TeclaAccessibilityService.scrollForward();
    			} else if(cu.mText.equals("Down"))     {
    				TeclaAccessibilityService.selectNextActiveNode();
    			} else if(cu.mText.equals("B1"))     {
    				TeclaAccessibilityService.clickActiveNode();
    			} else if(cu.mText.equals("B2"))     {
    				
    			} else if(cu.mText.equals("B3"))     {
    				
    			} else if(cu.mText.equals("B4"))     {
    				
    			}
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
