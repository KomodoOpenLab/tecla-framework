package ca.idrc.tecla.hud;

import java.util.ArrayList;

import ca.idrc.tecla.R;
import ca.idrc.tecla.R.id;
import ca.idrc.tecla.R.layout;
import ca.idrc.tecla.framework.SimpleOverlay;
import ca.idrc.tecla.framework.TeclaStatic;
import ca.idrc.tecla.highlighter.TeclaAccessibilityService;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class TeclaHUDOverlay extends SimpleOverlay {

	/**
	 * Tag used for logging in the whole framework
	 */
	public static final String CLASS_TAG = "TeclaHUDOverlay";

	private final static byte HUD_BTN_TOP = 0;
	private final static byte HUD_BTN_TOPRIGHT = 1;
	private final static byte HUD_BTN_RIGHT = 2;
	private final static byte HUD_BTN_BOTTOMRIGHT = 3;
	private final static byte HUD_BTN_BOTTOM = 4;
	private final static byte HUD_BTN_BOTTOMLEFT = 5;
	private final static byte HUD_BTN_LEFT = 6;
	private final static byte HUD_BTN_TOPLEFT = 7;

	private final static float SIDE_WIDTH_PROPORTION = 0.5f;
	private final static float STROKE_WIDTH_PROPORTION = 0.018f;

	private float scan_alpha_max;
	private float scan_alpha_min;

	private Context mContext;
	private final WindowManager mWindowManager;
	private static TeclaHUDOverlay sInstance;
	//public static TeclaIME sLatinIMEInstance = null;

	private ArrayList<TeclaHUDButtonView> mHUDPad;
	private ArrayList<AnimatorSet> mHUDAnimators;
	private byte mScanIndex;

	protected final static long SCAN_PERIOD = 1500;

	public TeclaHUDOverlay(Context context) {
		super(context);

		mContext = context;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		scan_alpha_max = Float.parseFloat(mContext.getResources().getString(R.string.scan_alpha_max));
		scan_alpha_min = Float.parseFloat(mContext.getResources().getString(R.string.scan_alpha_min));
		
		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		setParams(params);

		setContentView(R.layout.tecla_hud);

		View rView = getRootView();

		rView.setOnLongClickListener(mOverlayLongClickListener);
		rView.setOnClickListener(mOverlayClickListener);

		findAllButtons();        
		fixHUDLayout();

		mScanIndex = 0;

		mHUDPad.get(mScanIndex).setAlpha(1.0f);
		for (int i = 1; i < mHUDPad.size(); i++) {
			mHUDPad.get(i).setAlpha(scan_alpha_max - ((i-1) * ((scan_alpha_max - scan_alpha_min) / (mHUDPad.size() - 1))));
		}

		mHUDAnimators = new ArrayList<AnimatorSet>();
		for (int i = 0; i < mHUDPad.size(); i++) {
			mHUDAnimators.add((AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.hud_alpha_animator));
			mHUDAnimators.get(i).setDuration(SCAN_PERIOD * mHUDPad.size());
			mHUDAnimators.get(i).setTarget(mHUDPad.get(i));
			if (i > 0) {
				mHUDAnimators.get(i).start();
			}
		}

		mAutoScanHandler.sleep(SCAN_PERIOD);
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
			TeclaStatic.logV(CLASS_TAG, "Long clicked.  ");
			TeclaAccessibilityService.getInstance().shutdownInfrastructure();
			return true;
		}
	};

	protected void scanTrigger() {
		/*		ImageView img = (ImageView)mHUDSymbolHighlight.get(mHUDSymbolHighlight.size()-1);
		int id = img.getId();*/
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

		// Move highlight out of previous button
		if (mHUDAnimators.get(mScanIndex).isRunning()) {
			mHUDAnimators.get(mScanIndex).cancel();
		}
		mHUDPad.get(mScanIndex).setHighlighted(false);
		AnimatorSet hud_animator = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.hud_alpha_animator);
		long duration = SCAN_PERIOD * mHUDPad.size();
		hud_animator.getChildAnimations().get(0).setDuration(Math.round(0.1 * duration));
		hud_animator.getChildAnimations().get(1).setDuration(Math.round(0.9 * duration));
		hud_animator.setTarget(mHUDPad.get(mScanIndex));
		mHUDAnimators.set(mScanIndex, hud_animator);
		mHUDAnimators.get(mScanIndex).start();
//		mHUDPad.get(mScanIndex).setAlpha(SCAN_ALPHA_LOW);
		// Proceed to highlight next button
		if (mScanIndex == mHUDPad.size()-1) {
			mScanIndex = 0;
		} else {
			mScanIndex++;
		}
		if (mHUDAnimators.get(mScanIndex).isRunning()) {
			mHUDAnimators.get(mScanIndex).cancel();
		}
		mHUDPad.get(mScanIndex).setHighlighted(true);
		mHUDPad.get(mScanIndex).setAlpha(1.0f);

		mAutoScanHandler.sleep(SCAN_PERIOD);
	}

	private void findAllButtons() {

		mHUDPad = new ArrayList<TeclaHUDButtonView>();
		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_top));
		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_topright));
		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_right));
		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_bottomright));
		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_bottom));
		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_bottomleft));
		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_left));
		mHUDPad.add((TeclaHUDButtonView) findViewById(R.id.hud_btn_topleft));

	}

	private void fixHUDLayout () {
		Display display = mWindowManager.getDefaultDisplay();
		Point display_size = new Point();
		display.getSize(display_size);
		int display_width = display_size.x;
		int display_height = display_size.y;

		int size_reference = 0;
		if (display_width <= display_height) { // Portrait (use width)
			size_reference = Math.round(display_width * 0.24f);
		} else { // Landscape (use height)
			size_reference = Math.round(display_height * 0.24f);
		}

		ArrayList<ViewGroup.LayoutParams> hudParams = new ArrayList<ViewGroup.LayoutParams>();
		for(TeclaHUDButtonView button : mHUDPad) {
			hudParams.add(button.getLayoutParams());
		}

		hudParams.get(HUD_BTN_TOPLEFT).width = size_reference;
		hudParams.get(HUD_BTN_TOPLEFT).height = size_reference;
		hudParams.get(HUD_BTN_TOPRIGHT).width = size_reference;
		hudParams.get(HUD_BTN_TOPRIGHT).height = size_reference;
		hudParams.get(HUD_BTN_BOTTOMLEFT).width = size_reference;
		hudParams.get(HUD_BTN_BOTTOMLEFT).height = size_reference;
		hudParams.get(HUD_BTN_BOTTOMRIGHT).width = size_reference;
		hudParams.get(HUD_BTN_BOTTOMRIGHT).height = size_reference;
		hudParams.get(HUD_BTN_LEFT).width = Math.round(SIDE_WIDTH_PROPORTION * size_reference);
		hudParams.get(HUD_BTN_LEFT).height = display_height - (2 * size_reference);
		hudParams.get(HUD_BTN_TOP).width = display_width - (2 * size_reference);
		hudParams.get(HUD_BTN_TOP).height = Math.round(SIDE_WIDTH_PROPORTION * size_reference);
		hudParams.get(HUD_BTN_RIGHT).width = Math.round(SIDE_WIDTH_PROPORTION * size_reference);
		hudParams.get(HUD_BTN_RIGHT).height = display_height - (2 * size_reference);
		hudParams.get(HUD_BTN_BOTTOM).width = display_width - (2 * size_reference);
		hudParams.get(HUD_BTN_BOTTOM).height = Math.round(SIDE_WIDTH_PROPORTION * size_reference);

		for (int i = 0; i < mHUDPad.size(); i++) {
			mHUDPad.get(i).setLayoutParams(hudParams.get(i));
		}

		int stroke_width = Math.round(STROKE_WIDTH_PROPORTION * size_reference);

		mHUDPad.get(HUD_BTN_TOPLEFT).setProperties(TeclaHUDButtonView.POSITION_TOPLEFT, stroke_width, false);
		mHUDPad.get(HUD_BTN_TOPRIGHT).setProperties(TeclaHUDButtonView.POSITION_TOPRIGHT, stroke_width, false);
		mHUDPad.get(HUD_BTN_BOTTOMLEFT).setProperties(TeclaHUDButtonView.POSITION_BOTTOMLEFT, stroke_width, false);
		mHUDPad.get(HUD_BTN_BOTTOMRIGHT).setProperties(TeclaHUDButtonView.POSITION_BOTTOMRIGHT, stroke_width, false);
		mHUDPad.get(HUD_BTN_LEFT).setProperties(TeclaHUDButtonView.POSITION_LEFT, stroke_width, false);
		mHUDPad.get(HUD_BTN_TOP).setProperties(TeclaHUDButtonView.POSITION_TOP, stroke_width, true);
		mHUDPad.get(HUD_BTN_RIGHT).setProperties(TeclaHUDButtonView.POSITION_RIGHT, stroke_width, false);
		mHUDPad.get(HUD_BTN_BOTTOM).setProperties(TeclaHUDButtonView.POSITION_BOTTOM, stroke_width, false);
	}

	class AutoScanHandler extends Handler {
		public AutoScanHandler() {

		}

		@Override
		public void handleMessage(Message msg) {
			TeclaHUDOverlay.this.scanForward();
		}

		public void sleep(long delayMillis) {
			removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	}
	AutoScanHandler mAutoScanHandler = new AutoScanHandler();

}
