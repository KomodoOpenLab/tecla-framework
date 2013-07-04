package com.android.tecla.addon;

import ca.idrc.tecla.R;
import ca.idrc.tecla.framework.TeclaStatic;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class OnboardingDialog extends Dialog {
	
	public static final String CLASS_TAG = "OnboardingDialog";
	
	private static final int ONBOARD_SELECT_IME = 0;
	private static final int ONBOARD_ENABLE_A11Y = 1;
	private static final int ONBOARD_FINISHED = 2;
	
	private Context mContext;
	private TextView mImeWarnText;
	private TextView mA11yWarnText;
	private TextView mSuccessText;
	private ViewFlipper mOnboardingFlipper;

	private Button mImeOkBtn;
	private Button mImeCancelBtn;
	private Button mA11yOkBtn;
	private Button mA11yCancelBtn;
	private Button mFinalOkBtn;

	public OnboardingDialog(Context context) {
		super(context);
		mContext = context;
		setContentView(R.layout.tecla_onboarding);
		setCancelable(false);
		mImeOkBtn = (Button) findViewById(R.id.ime_ok_btn);
		mImeCancelBtn = (Button) findViewById(R.id.ime_cancel_btn);
		mA11yOkBtn = (Button) findViewById(R.id.a11y_ok_btn);
		mA11yCancelBtn = (Button) findViewById(R.id.a11y_cancel_btn);
		mFinalOkBtn = (Button) findViewById(R.id.success_btn);
		mImeOkBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TeclaApp.getInstance().pickIme();
			}
		});
		mFinalOkBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		init();
	}

	private void init() {
		
		setTitle(mContext.getString(R.string.tecla_next));

		mImeWarnText = (TextView) findViewById(R.id.ime_warning);
		mA11yWarnText = (TextView) findViewById(R.id.a11y_warning);
		mSuccessText = (TextView) findViewById(R.id.success_msg);
		
		mImeWarnText.setText(mContext.getString(R.string.onboarding_ime_warning));
		mA11yWarnText.setText(mContext.getString(R.string.onboarding_a11y_warning));
		mSuccessText.setText(mContext.getString(R.string.onboarding_success));
		
		mOnboardingFlipper = (ViewFlipper) findViewById(R.id.onboarding_flipper);
		
	}
	
	public void setExternalClickListener(View.OnClickListener listener) {
		mImeCancelBtn.setOnClickListener(listener);
		mA11yCancelBtn.setOnClickListener(listener);
		mA11yOkBtn.setOnClickListener(listener);
	}
		
	/* (non-Javadoc)
	 * @see android.app.Dialog#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			TeclaStatic.logD(CLASS_TAG, "Got focus!");
			switch (mOnboardingFlipper.getDisplayedChild()) {
			case ONBOARD_SELECT_IME:
				TeclaStatic.logD(CLASS_TAG, "Selecting IME!");
		    	if (TeclaStatic.isDefaultIMESupported(mContext)) {
		    		mOnboardingFlipper.showNext();
			    	if (TeclaApp.getInstance().isTeclaA11yServiceRunning()) {
						TeclaStatic.logD(CLASS_TAG, "Enabling A11y Service!");
			    		mOnboardingFlipper.showNext();
			    	}
		    	}
		    	break;
			case ONBOARD_ENABLE_A11Y:
		    	if (TeclaApp.getInstance().isTeclaA11yServiceRunning()) {
		    		mOnboardingFlipper.showNext();
		    	}
			}
		}
	}

}
