package com.clara.socksio.actors;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.clara.socksio.actors.CircleView;

import java.util.Random;

/**
 * Created by clara on 9/22/16.
 */

public class SpeckView extends View implements CircleView {

	//Draw at random location

	private int size = 10;

	public boolean eaten = false;

	Paint mPaint;

	int x;
	int y;

	public SpeckView(Context context, float centerX, float centerY, int radius) {
		super(context);

		radius = radius - size;  //create a margin
		Random rnd = new Random();

		x = radius - rnd.nextInt(radius*2);
		y = radius - rnd.nextInt(radius*2);

		//Log.i(TAG, "x " + x + " y " + y + " radius " + radius);
		while (x*x + y*y > radius*radius) {
			//Log.i(TAG, "Outside circle. recalc x " + x + " y " + y + " radius " + radius);
			x = radius - rnd.nextInt(radius*2);
			y = radius - rnd.nextInt(radius*2);
		}

		x += centerX;   //And shift to center
		y += centerY;

		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setARGB(170, 255, 255, 255);  //white, slightly transparent

	}

	public void shift(int dx, int dy) {
		x = x - dx;
		y = y - dy;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(x, y, size, mPaint);
	}

	@Override
	public String toString() {
		return "x=" + x + " y=" + y + " eaten? " + eaten;
	}


	@Override
	public int getCircleCenterX() {
		return x;
	}

	@Override
	public int getCircleCenterY() {
		return y;
	}

	@Override
	public int getSize() {
		return size;
	}
}
