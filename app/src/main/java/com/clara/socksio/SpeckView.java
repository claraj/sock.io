package com.clara.socksio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * Created by clara on 9/22/16.
 */

class SpeckView extends View {

	//Random speck location

	private int size = 10;

	boolean eaten = false;

	Paint mPaint;

	int x;
	int y;

	public SpeckView(Context context, float centerX, float centerY, int radius) {
		super(context);


		radius = radius - size;  //create a margin
		Random rnd = new Random();

		x = radius - rnd.nextInt(radius*2);
		y = radius - rnd.nextInt(radius*2);

		Log.i(TAG, "x " + x + " y " + y + " radius " + radius);
		while (x*x + y*y > radius*radius) {
			Log.i(TAG, "Outside circle. recalc x " + x + " y " + y + " radius " + radius);
			x = radius - rnd.nextInt(radius*2);
			y = radius - rnd.nextInt(radius*2);
		}

		x += centerX;   //And shift to center
		y += centerY;

		mPaint = new Paint();

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);

		params.leftMargin = x;
		params.topMargin = y;

	}

	public void shift(int dx, int dy) {
		x = x - dx;
		y = y - dy;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setARGB(170, 255, 255, 255);  //white transparent
		canvas.drawCircle(x, y, size, mPaint);

	}

	@Override
	public String toString() {
		return "x=" + x + " y=" + y + " eaten? " + eaten;
	}

}
