package com.clara.socksio;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by admin on 9/22/16.
 */

public class WorldView extends View implements CircleView{

	private Paint mBackgroundPaint;
	private Paint mBorderPaint;

	public int getCenterX() {
		return centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	private int centerX, centerY, radius;

	public WorldView(Context context, int x, int y, int radius) {
		super(context);
		mBackgroundPaint = new Paint();
		mBackgroundPaint.setStyle(Paint.Style.FILL); //border (?)
		mBackgroundPaint.setARGB(70, 0, 0, 0);

		mBorderPaint = new Paint();
		mBorderPaint.setStyle(Paint.Style.STROKE); //border (?)
		mBorderPaint.setARGB(255, 0, 0, 0);

		this.centerX = x;
		this.centerY = y;
		this.radius = radius;
	}

	public void shift(int x, int y) {
		centerX -= x;
		centerY -= y;
	}



	@Override
	public void onDraw(Canvas canvas) {

		//big circle!
		canvas.drawCircle(centerX, centerY, radius, mBackgroundPaint);
		canvas.drawCircle(centerX, centerY, radius, mBorderPaint);

	}

	@Override
	public int getCircleCenterX() {
		return getCenterX();
	}

	@Override
	public int getCircleCenterY() {
		return getCenterY();
	}

	@Override
	public int getSize() {
		return radius;
	}
}
