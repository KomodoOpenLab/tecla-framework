package ca.idrc.tecla;

import android.content.Context;
import android.graphics.Color;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;

public class TeclaHighlighter extends SimpleOverlay {

    private static TeclaHighlighter sInstance;

    private final HighlightBoundsView mAnnounceBounds;
    private final HighlightBoundsView mBounds;
    
	public TeclaHighlighter(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;		
		setParams(params);
		
		setContentView(R.layout.tecla_highlighter);

		mAnnounceBounds = (HighlightBoundsView) findViewById(R.id.announce_bounds);
//		mAnnounceBounds.setHighlightColor(Color.argb(0xff, 0x21, 0xad, 0xe3));
		mAnnounceBounds.setHighlightColor(Color.WHITE);
		
		
		mBounds = (HighlightBoundsView) findViewById(R.id.bounds);
		mBounds.setHighlightColor(Color.argb(0xdd, 0x38, 0x38, 0x38));
	}

	@Override
	protected void onShow() {
		sInstance = this;
	}

	@Override
	protected void onHide() {
        sInstance = null;
        mBounds.clear();
        mAnnounceBounds.clear();
	}
	

    public static void removeInvalidNodes() {
        if (sInstance == null) {
            return;
        }

        sInstance.mBounds.removeInvalidNodes();
        sInstance.mBounds.postInvalidate();

        sInstance.mAnnounceBounds.removeInvalidNodes();
        sInstance.mAnnounceBounds.postInvalidate();
    }

    public static void updateNodes(AccessibilityNodeInfo source, AccessibilityNodeInfo announced) {
        if (sInstance == null) {
            return;
        }

        sInstance.mBounds.clear();
        if(source != null) {
            sInstance.mBounds.setStrokeWidth(10);
            sInstance.mBounds.add(announced);
            sInstance.mBounds.postInvalidate();        	
        }
        
        sInstance.mAnnounceBounds.clear();
        if(announced != null) {
            sInstance.mAnnounceBounds.setStrokeWidth(4);
            sInstance.mAnnounceBounds.add(announced);
            sInstance.mAnnounceBounds.postInvalidate();
        	
        }
    }
    
}
