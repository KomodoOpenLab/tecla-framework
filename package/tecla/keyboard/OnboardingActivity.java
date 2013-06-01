package com.android.tecla.keyboard;

import ca.idrc.tecla.R;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class OnboardingActivity extends Activity {
	
	private static Activity sInstance;
	private TextView mMessage;
	private Button mButton;
	private Resources mResources;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		init();
	}

	private void init() {
		setContentView(R.layout.tecla_onboarding);
		
		sInstance = this;
		
		mResources = getResources();
		String app_name = (String) mResources.getText(R.string.app_name);
		
		mMessage = (TextView) findViewById(R.id.ime_warning);
		mButton = (Button) findViewById(R.id.ok_button);
		
		mMessage.setText(mResources.getText(R.string.ime_warning_1) +
				" " + app_name + ". " +
				mResources.getText(R.string.ime_warning_2) + 
				" " + app_name + ", " +
				mResources.getText(R.string.ime_warning_3));
		mButton.setOnClickListener(mOkListener);
	}
	
	private View.OnClickListener mOkListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			sInstance.finish();
		}
	};
	
}
