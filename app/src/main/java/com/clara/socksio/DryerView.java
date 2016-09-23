package com.clara.socksio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.Random;

/**
 * Created by admin on 9/23/16.
 */

public class DryerView extends View {


	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}

	private int x;
	private int y;
	private int MAX_MOVE = 30;

	private float prevailingDirection;
	Paint mPaint;

	Bitmap dryerBitmap;

	public DryerView(Context context, int x, int y) {
		super(context);
		this.x=x ; this.y = y;
		mPaint = new Paint();
		prevailingDirection = new Random().nextFloat() * 2 * (float) Math.PI;
		Drawable dryer = getResources().getDrawable(R.drawable.evildryer);   //new version requires theme argument, which can be null.
		dryerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.evildryer);

	}

	public void shift(int xdif, int ydif) {
		x-=xdif;
		y-=ydif;
	}

	public void wander(int worldCenterX, int worldCenterY, int radius) {
		//move a random amount in prevailingDirection within circle centered x, y, radius
		//adjust prevailingDirection a random amount

		//Log.i(TAG, "")
		Random rnd = new Random();
		int distance = rnd.nextInt(MAX_MOVE);

		x = x + (int)( Math.sin(prevailingDirection) * distance);
		y = y + (int)( Math.cos(prevailingDirection) * distance);

		prevailingDirection = prevailingDirection + (rnd.nextFloat() / 2);  //move just a little, between 0 and 0.5 rads ~ 12th of a circle

		//If hit edge, reverse by adding Pi to prevailing direction

		int xdiff = worldCenterX - x;
		int ydiff = worldCenterY - y;

		//Does this fall off the world?
		if ( xdiff*xdiff + ydiff*ydiff > radius*radius) {

			prevailingDirection = (prevailingDirection + (float)Math.PI)  % ((float) Math.PI);

		}

	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawBitmap(dryerBitmap, x, y, mPaint);

	}


	@Override
	public String toString() {
		return "DryerView{" +
				"x=" + x +
				", y=" + y +
				", prevailingDirection=" + prevailingDirection +
				'}';
	}
}
