package com.clara.socksio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Random;

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

	public SpeckView(Context context, float maxX, float maxY) {
		super(context);

		Random rnd = new Random();
		x = rnd.nextInt((int)maxX);
		y = rnd.nextInt((int)maxY);
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
