package ca.idrc.tecla;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaHUDController extends SimpleOverlay {

	protected final TeclaHUD mHUD;
	
	public TeclaHUDController(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		setParams(params);

		setContentView(R.layout.tecla_controller);

		mHUD = (TeclaHUD) findViewById(R.id.teclaHUD_control);
		getRootView().setOnTouchListener(mOverlayTouchListener);
		setSelected("Up");
	}

    public TeclaHUDAsset getSelected () {
    	TeclaHUDAsset result = null; 
    	for (TeclaHUDAsset asset: mHUD.mHUDAssets) {
    		if(asset.isSelected()) {
    			result = asset;
    			break; 
    		}
    	}
    	return result; 
    }
    
    public void setSelected(String text) {
    	for (TeclaHUDAsset asset: mHUD.mHUDAssets) {
    		if(asset.mText.equals(text)) asset.setSelected(true);
    		else asset.setSelected(false);
    	}
    	mHUD.postInvalidate();
    }

    public void performAction(AccessibilityNodeInfo node) {
    	for (TeclaHUDAsset asset: mHUD.mHUDAssets) {
    		if(asset.isSelected()) {
    			if(asset.mText.equals("Up"))     {
    				TeclaAccessibilityService.selectNode(node, TeclaAccessibilityService.DIRECTION_UP);
    			} else if(asset.mText.equals("Left"))     {
    				TeclaAccessibilityService.selectNode(node, TeclaAccessibilityService.DIRECTION_LEFT);
    			} else if(asset.mText.equals("Right"))     {
    				TeclaAccessibilityService.selectNode(node, TeclaAccessibilityService.DIRECTION_RIGHT);
    			} else if(asset.mText.equals("Down"))     {
    				TeclaAccessibilityService.selectNode(node, TeclaAccessibilityService.DIRECTION_DOWN);
    			} else if(asset.mText.equals("S"))     {
    				TeclaAccessibilityService.clickActiveNode();
    			}
    			break; 
    		}
    	}
    }
    
    public void setPreviousSelected () {
    	TeclaHUDAsset previous = mHUD.mHUDAssets.get(mHUD.mHUDAssets.size() - 1);
    	for (TeclaHUDAsset asset: mHUD.mHUDAssets) {
    		if(asset.isSelected()) {
    			asset.setSelected(false);
    			previous.setSelected(true);
    			mHUD.postInvalidate();
    			break;
    		}
    		else previous = asset;
    	}
    }

    public void setNextSelected () {
    	boolean select = false; 
    	for (TeclaHUDAsset asset: mHUD.mHUDAssets) {
    		if(select) {
    			asset.setSelected(true);
    			mHUD.postInvalidate();
    			return; 
    		}
    		if(asset.isSelected()) {
    			asset.setSelected(false);
    			select = true; 
    		}
    	}
    	mHUD.mHUDAssets.get(0).setSelected(true);
    	mHUD.postInvalidate();
    	
    }
    
	private View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//Log.v("TeclaA11y", "Tecla Overlay Touch Down! " + Float.toString(logicalX) + " " + Float.toString(logicalY));
				break;
			case MotionEvent.ACTION_UP:
				//Log.v("TeclaA11y", "Tecla Overlay Touch Up! " + Float.toString(logicalX) + " " + Float.toString(logicalY));
				performAction(TeclaAccessibilityService.sInstance.mSelectedNode);
			default:
				break;
			}
			return true;
		}
		
	};
}
