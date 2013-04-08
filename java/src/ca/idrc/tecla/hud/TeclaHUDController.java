package ca.idrc.tecla.hud;

import java.util.ArrayList;

import ca.idrc.tecla.R;
import ca.idrc.tecla.R.id;
import ca.idrc.tecla.R.layout;
import ca.idrc.tecla.framework.SimpleOverlay;
import ca.idrc.tecla.framework.TeclaIME;
import ca.idrc.tecla.highlighter.TeclaAccessibilityService;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class TeclaHUDController extends SimpleOverlay {

	private final static String tag = "TeclaHUDController";
	private static TeclaHUDController sInstance;
	public static TeclaIME sLatinIMEInstance = null;

	private ArrayList<ImageView> mHUDPad;
	private ArrayList<ImageView> mHUDPadHighlight;
	private ArrayList<ImageView> mHUDSymbol;
	private ArrayList<ImageView> mHUDSymbolHighlight;
	
	protected final static long SCAN_PERIOD = 1500;
	
	public TeclaHUDController(Context context) {
		super(context);
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		setParams(params);

		setContentView(R.layout.tecla_hud);

		getRootView().setOnLongClickListener(mOverlayLongClickListener);
		getRootView().setOnClickListener(mOverlayClickListener);
		
		mHUDPad = new ArrayList<ImageView>();
//		mHUDPad.add((ImageView)findViewById(R.id.imageView_border_up));
//		mHUDPad.add((ImageView)findViewById(R.id.imageView_border_upperright));
//		mHUDPad.add((ImageView)findViewById(R.id.imageView_border_right));
//		mHUDPad.add((ImageView)findViewById(R.id.imageView_border_lowerright));
//		mHUDPad.add((ImageView)findViewById(R.id.imageView_border_down));
//		mHUDPad.add((ImageView)findViewById(R.id.imageView_border_lowerleft));
//		mHUDPad.add((ImageView)findViewById(R.id.imageView_border_left));
//		mHUDPad.add((ImageView)findViewById(R.id.imageView_border_upperleft));
//
//		mHUDPadHighlight = new ArrayList<ImageView>();
//		mHUDPadHighlight.add((ImageView)findViewById(R.id.imageView_highlight_up));
//		mHUDPadHighlight.add((ImageView)findViewById(R.id.imageView_highlight_upperright));
//		mHUDPadHighlight.add((ImageView)findViewById(R.id.imageView_highlight_right));
//		mHUDPadHighlight.add((ImageView)findViewById(R.id.imageView_highlight_lowerright));
//		mHUDPadHighlight.add((ImageView)findViewById(R.id.imageView_highlight_down));
//		mHUDPadHighlight.add((ImageView)findViewById(R.id.imageView_highlight_lowerleft));
//		mHUDPadHighlight.add((ImageView)findViewById(R.id.imageView_highlight_left));
//		mHUDPadHighlight.add((ImageView)findViewById(R.id.imageView_highlight_upperleft));
//		
//		mHUDSymbol = new ArrayList<ImageView>();
//		mHUDSymbol.add((ImageView)findViewById(R.id.imageView_border_uparrow));
//		mHUDSymbol.add((ImageView)findViewById(R.id.imageView_border_select));
//		mHUDSymbol.add((ImageView)findViewById(R.id.imageView_border_rightarrow));
//		mHUDSymbol.add((ImageView)findViewById(R.id.imageView_border_forward));
//		mHUDSymbol.add((ImageView)findViewById(R.id.imageView_border_downarrow));
//		mHUDSymbol.add((ImageView)findViewById(R.id.imageView_border_back));
//		mHUDSymbol.add((ImageView)findViewById(R.id.imageView_border_leftarrow));
//		mHUDSymbol.add((ImageView)findViewById(R.id.imageView_border_home));
				
		mHUDSymbolHighlight = new ArrayList<ImageView>();
//		mHUDSymbolHighlight.add((ImageView)findViewById(R.id.imageView_highlight_uparrow));
//		mHUDSymbolHighlight.add((ImageView)findViewById(R.id.imageView_highlight_select));
//		mHUDSymbolHighlight.add((ImageView)findViewById(R.id.imageView_highlight_rightarrow));
//		mHUDSymbolHighlight.add((ImageView)findViewById(R.id.imageView_highlight_forward));
//		mHUDSymbolHighlight.add((ImageView)findViewById(R.id.imageView_highlight_downarrow));
//		mHUDSymbolHighlight.add((ImageView)findViewById(R.id.imageView_highlight_back));
//		mHUDSymbolHighlight.add((ImageView)findViewById(R.id.imageView_highlight_leftarrow));
//		mHUDSymbolHighlight.add((ImageView)findViewById(R.id.imageView_highlight_home));
		
        mAutoScanHandler.sleep(1000);
	}

	@Override
	protected void onShow() {
		sInstance = this;
	}

	@Override
	protected void onHide() {
        sInstance = null;
	}
	
	private View.OnClickListener mOverlayClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			scanTrigger();
			
		}
	};	

	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			Log.v("TeclaA11y", "Long clicked.  ");
			TeclaAccessibilityService.getInstance().shutdownInfrastructure();
			return true;
		}
	};

	protected void scanTrigger() {
		ImageView img = (ImageView)mHUDSymbolHighlight.get(mHUDSymbolHighlight.size()-1);
		int id = img.getId();
//		if(id == R.id.imageView_highlight_uparrow) {
//			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_UP);
//		} else if(id == R.id.imageView_highlight_select) {
//			TeclaAccessibilityService.clickActiveNode();
//		} else if(id == R.id.imageView_highlight_rightarrow) {
//			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_RIGHT);
//		} else if(id == R.id.imageView_highlight_forward) {
//			
//		} else if(id == R.id.imageView_highlight_downarrow) {
//			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_DOWN);
//		} else if(id == R.id.imageView_highlight_back) {
//			if(sLatinIMEInstance != null) {
//				Log.w(tag, "LatinIME is not null");
//				sLatinIMEInstance.pressBackKey();
//			} else Log.w(tag, "LatinIME is null");
//		} else if(id == R.id.imageView_highlight_rightarrow) {
//			TeclaAccessibilityService.selectNode(TeclaAccessibilityService.DIRECTION_RIGHT);
//		} else if(id == R.id.imageView_highlight_home) {
//			if(sLatinIMEInstance != null) {
//				Log.w(tag, "LatinIME is not null");
//				sLatinIMEInstance.pressHomeKey();
//			} else Log.w(tag, "LatinIME is null");
//		}
		mAutoScanHandler.sleep(SCAN_PERIOD);
	}
	protected void scanForward() {
		// update HUD graphics 
		
		ImageView img;
		
		if (mHUDPad.size() > 0) {
			mHUDPad.add((ImageView)mHUDPad.get(0));
			mHUDPad.remove(0);

			img = (ImageView)mHUDPadHighlight.get(mHUDPadHighlight.size()-1);
			img.setVisibility(View.INVISIBLE);
			img = (ImageView)mHUDPadHighlight.get(0);
			img.setVisibility(View.VISIBLE);
			mHUDPadHighlight.remove(0);
			mHUDPadHighlight.add(img);
			
			mHUDSymbol.add((ImageView)mHUDSymbol.get(0));
			mHUDSymbol.remove(0);

			img = (ImageView)mHUDSymbolHighlight.get(mHUDSymbolHighlight.size()-1);
			img.setVisibility(View.INVISIBLE);
			img = (ImageView)mHUDSymbolHighlight.get(0);
			img.setVisibility(View.VISIBLE);
			mHUDSymbolHighlight.remove(0);
			mHUDSymbolHighlight.add(img);		
			
			for(int i=0; i<mHUDPad.size()-1; ++i) {
				img = (ImageView)mHUDPad.get(i);
				img.setAlpha(1.0f/(mHUDPad.size()-1)*i);
				img = (ImageView)mHUDSymbol.get(i);
				img.setAlpha(1.0f/(mHUDSymbol.size()-1)*i);
			}
			
		}
		mAutoScanHandler.sleep(SCAN_PERIOD);
	}

	class AutoScanHandler extends Handler {
		public AutoScanHandler() {
			
		}

        @Override
        public void handleMessage(Message msg) {
        	TeclaHUDController.this.scanForward();
        }

        public void sleep(long delayMillis) {
                removeMessages(0);
                sendMessageDelayed(obtainMessage(0), delayMillis);
        }
	}
	AutoScanHandler mAutoScanHandler = new AutoScanHandler();

}
