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
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Handles drawing the screen reader cursor on-screen.
 */
public class TeclaShieldControlView extends View {

	protected ArrayList<TeclaShieldControlUnit> mControlUnits = new ArrayList<TeclaShieldControlUnit>();
	
    public TeclaShieldControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mControlUnits.add(new TeclaShieldControlUnit("Up", 200, 200));
        mControlUnits.add(new TeclaShieldControlUnit("Left", 100, 300));
        mControlUnits.add(new TeclaShieldControlUnit("Right", 300, 300));
        mControlUnits.add(new TeclaShieldControlUnit("Down", 200, 400));
        
        mControlUnits.add(new TeclaShieldControlUnit("B1", 500, 225));
        mControlUnits.add(new TeclaShieldControlUnit("B2", 500, 275));
        mControlUnits.add(new TeclaShieldControlUnit("B3", 500, 325));
        mControlUnits.add(new TeclaShieldControlUnit("B4", 500, 375));
        
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    public void onDraw(Canvas c) {
    	for (TeclaShieldControlUnit cu: mControlUnits) {
    		c.drawText(cu.mText, cu.mScreenLocation[0], cu.mScreenLocation[1], cu.mPaint);
    	}
    }
}