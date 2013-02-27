/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ca.idrc.tecla;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Handles drawing the screen reader cursor on-screen.
 */
public class TeclaShieldControlView extends View {

	protected ArrayList<TeclaShieldControlUnit> mControlUnits = new ArrayList<TeclaShieldControlUnit>();
	private int[] mCenterLocation = new int[2];
	
    public TeclaShieldControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mCenterLocation[0] = size.x/2;
        mCenterLocation[1] = size.y/2;
    
        mControlUnits.add(new TeclaShieldControlUnit("Up", 0, -100, Paint.Align.CENTER));
        mControlUnits.add(new TeclaShieldControlUnit("Left", -100, 0, Paint.Align.RIGHT));
        mControlUnits.add(new TeclaShieldControlUnit("Right", 100, 0, Paint.Align.LEFT));
        mControlUnits.add(new TeclaShieldControlUnit("Down", 0, 100, Paint.Align.CENTER));
        
        mControlUnits.add(new TeclaShieldControlUnit("S", 0, 0, Paint.Align.CENTER));
        
        mControlUnits.add(new TeclaShieldControlUnit("Back", -100, 200, Paint.Align.CENTER));
        mControlUnits.add(new TeclaShieldControlUnit("Home", 100, 200, Paint.Align.CENTER));
        mControlUnits.add(new TeclaShieldControlUnit("SB", -100, 300, Paint.Align.CENTER));
        mControlUnits.add(new TeclaShieldControlUnit("SF", 100, 300, Paint.Align.CENTER));
        
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    public void onDraw(Canvas c) {
    	for (TeclaShieldControlUnit cu: mControlUnits) {
    		c.drawText(cu.mText, mCenterLocation[0] + cu.mScreenLocationOffset[0], 
    				mCenterLocation[1] + cu.mScreenLocationOffset[1], cu.mPaint);
    	}
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mCenterLocation[0] = size.x/2;
        mCenterLocation[1] = size.y/2;
    
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}