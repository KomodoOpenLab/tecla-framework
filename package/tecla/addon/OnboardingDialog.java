package com.android.tecla.addon;

import ca.idrc.tecla.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class OnboardingDialog extends Dialog {
	
	private OnboardingDialog sInstance;
	private Context mContext;
	private TextView mMessage;
	private Button mButton;
	
	public OnboardingDialog(Context context) {
		super(context);
		mContext = context;
		sInstance = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		init();
	}

	private void init() {
		
		String title = mContext.getString(R.string.tecla_next);
		String warn1 = mContext.getString(R.string.ime_warning_1);
		String warn2 = mContext.getString(R.string.ime_warning_2);
		String warn3 = mContext.getString(R.string.ime_warning_3);
		setTitle(title);

		mMessage = (TextView) findViewById(R.id.ime_warning);
		mButton = (Button) findViewById(R.id.ok_button);
		
		mMessage.setText(warn1 + " " + title + ". " +
				warn2 + " " + title + ", " + warn3);
	}
	
}
