package com.clara.socksio;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by admin on 9/22/16.
 */

public class WorldView extends View {

	private Paint mPaint;
	private int centerX, centerY, radius;

	public WorldView(Context context, int x, int y, int radius) {
		super(context);
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL); //border (?)
		mPaint.setARGB(100, 0, 0, 0);
		this.centerX = x;
		this.centerY = y;
		this.radius = radius;

	}

	@Override
	public void onDraw(Canvas canvas) {

		//big circle!
		canvas.drawCircle(centerX, centerY, radius, mPaint);

	}
}
