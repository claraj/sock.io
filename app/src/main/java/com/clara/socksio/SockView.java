package com.clara.socksio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.util.LinkedList;

/**
 * Created by admin on 9/22/16.
 */

public class SockView extends View {

	private static String TAG = "SOCKVIEW";

	private int centerX;
	private int centerY;

	private float x, y;
	private final Paint mPaint = new Paint();
	private int mSize = 20;


	public float getHeadX() {
		return segments.getFirst().x;
	}

	public float getHeadY() {
		return segments.getFirst().y;
	}

	public int getSize() {
		return mSize;
	}

	//List of segments central co-ordinates

	private class Segment {
		float x;
		float y;

		public Segment(float x, float y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString(){
			return "[" + x + "," + y + "]";
		}

		public void shift(int dx, int dy) {
			x = x - dx;
			y = y - dy;
		}
	}

	LinkedList<Segment> segments;

	public void shift(int x, int y) {
		for (Segment s : segments) {
			s.shift(x, y);
		}
	}


	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}



	public SockView(Context context, int centerx, int centery) {
		super(context);

		this.centerX = centerx;
		this.centerY = centery;

		segments = new LinkedList<>();
		segments.add(new Segment(centerx, centerY));

		Log.i(TAG, "new sock" + segments);

		this.x = x ; this.y = y;
		mPaint.setStyle(Paint.Style.FILL);
	}


	//Add to end of segments (?)
	public void addSegmentToEnd(float x, float y) {
		segments.add(new Segment(x, y));         //add to end
		Log.d(TAG, "Added segment to end " + +x + " " + y +  " " + segments);
	}

//	//Add to start of segments (?)
//	public void addSegmentToStart(float x, float y) {
//		segments.addFirst(new Segment(x, y));         //add to start
//	}

	public void addSegmentRelativeToHead(float xDiff, float yDiff) {

		Log.i(TAG, "Adding segment " + xDiff + " " + yDiff);

		//shift everything

		shift((int)xDiff, (int)yDiff);
		segments.addFirst(new Segment(centerX, centerY));

//
//		//head is first segment
//		Segment head = segments.get(0);
//		segments.addFirst(new Segment( head.x + xDiff , head.y + yDiff));

	}

	public void removeLast() {
		segments.removeLast();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.i(TAG, "drawing " + x + " " + y);

		int blue = 255, red=0;

		for (Segment s : segments) {
			red = (red+20) % 255;
			blue = (blue+15) % 255;

			mPaint.setARGB(150, red, 0, blue);
			canvas.drawCircle(s.x, s.y, mSize, mPaint);
			//Log.i(TAG, "blue = " + blue);
		}

	}

	@Override
	public String toString() {
		return segments.toString();
	}

}

