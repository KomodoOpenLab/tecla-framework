package ca.idrc.tecla;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class TeclaController extends SimpleOverlay {

	protected final TeclaShieldControlView mControlView;

	// used for touch-based debugging 
	private final static int TOUCHED_TOPLEFT = 0;
	private final static int TOUCHED_TOPRIGHT = 1;
	private final static int TOUCHED_BOTTOMLEFT = 2;
	private final static int TOUCHED_BOTTOMRIGHT = 3;
	private final static int SCROLLED_NOWHERE = 0;
	private final static int SCROLLED_BACKWARD = 1;
	private final static int SCROLLED_FORWARD = 2;
	
	private TeclaShieldControl mControl;
	private int touchdown;
	private int touchup;
	private int mTouchMode;

	public TeclaController(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		setParams(params);

		setContentView(R.layout.tecla_controller);

		mTouchMode = 0;
		mControlView = (TeclaShieldControlView) findViewById(R.id.tecla_control);
		mControl = new TeclaShieldControl(mControlView);
		getRootView().setOnTouchListener(mOverlayTouchListener);
	}

	private View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int width = v.getWidth();
			int height = v.getHeight();
			float x=event.getX();
			float y=event.getY();
			float logicalX=x/width;
			float logicalY=y/height;
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//Log.v("TeclaA11y", "Tecla Overlay Touch Down! " + Float.toString(logicalX) + " " + Float.toString(logicalY));
				
				if(logicalX<0.5 && logicalY<0.5) {
					touchdown = TOUCHED_TOPLEFT; 
				} else if(logicalX<0.5 && logicalY>=0.5) {
					touchdown = TOUCHED_BOTTOMLEFT;
				} else if(logicalX>=0.5 && logicalY<0.5) {
					touchdown = TOUCHED_TOPRIGHT;
				} else if(logicalX>=0.5 && logicalY>=0.5) {
					touchdown = TOUCHED_BOTTOMRIGHT;
				}
				break;
			case MotionEvent.ACTION_UP:
				//Log.v("TeclaA11y", "Tecla Overlay Touch Up! " + Float.toString(logicalX) + " " + Float.toString(logicalY));

				if(logicalX<0.5 && logicalY<0.5) {
					touchup = TOUCHED_TOPLEFT; 
				} else if(logicalX<0.5 && logicalY>=0.5) {
					touchup = TOUCHED_BOTTOMLEFT;
				} else if(logicalX>=0.5 && logicalY<0.5) {
					touchup = TOUCHED_TOPRIGHT;
				} else if(logicalX>=0.5 && logicalY>=0.5) {
					touchup = TOUCHED_BOTTOMRIGHT;
				}
				
				if(touchdown==TOUCHED_TOPLEFT && touchup==TOUCHED_TOPLEFT) {
					mControl.setPreviousSelected();
				} else if(touchdown==TOUCHED_TOPRIGHT && touchup==TOUCHED_TOPRIGHT) {
					mControl.setNextSelected();
				} else if(touchdown==TOUCHED_BOTTOMLEFT && touchup==TOUCHED_BOTTOMLEFT) {
					if(mTouchMode == 1) {
						break; 
					}
					//TODO: Implement perform action using A11yService singleton
//					mControl.performAction(mSelectedNode);
					//Log.w("TeclaA11y", mSelectedNode.toString());
				} else if(touchdown==TOUCHED_BOTTOMRIGHT && touchup==TOUCHED_BOTTOMRIGHT) {
					if(mTouchMode == 1) {
						break; 
					}
					//TODO: Implement perform action using singleton A11yService instance
//					mControl.performAction(mSelectedNode);
					//Log.w("TeclaA11y", "Current node: " +  mActiveNodes.get(mActiveNodes.size()-1).toString());
				} else if(touchdown==TOUCHED_TOPLEFT && touchup==TOUCHED_TOPRIGHT) {
					
				} else if(touchdown==TOUCHED_BOTTOMLEFT && touchup==TOUCHED_BOTTOMRIGHT) {
					
				} else if(touchdown==TOUCHED_BOTTOMLEFT && touchup==TOUCHED_TOPRIGHT) {
					// shut down   
					++mTouchMode;
					mTouchMode = mTouchMode%2;
					Log.w("TeclaA11y", "Touch mode = " + mTouchMode);
				} else if(touchdown==TOUCHED_TOPLEFT && touchup==TOUCHED_BOTTOMRIGHT) {
					// shut down  
					Log.w("TeclaA11y", "3-switch access: SHUTDOWN");
					TeclaAccessibilityService.getInstance().shutdownInfrastructure();
				}
				break;
			default:
				break;
			}
			return true;
		}
		
	};

}
