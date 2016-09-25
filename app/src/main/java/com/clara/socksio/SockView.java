package com.clara.socksio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by admin on 9/22/16.
 */

public class SockView extends View implements CircleView {

	private static String TAG = "SOCKVIEW";

	private int centerX;
	private int centerY;

	private float x, y;
	private final Paint mPaint = new Paint();
	protected int mSize = 20;

	//List of segments central co-ordinates
	ArrayList<Segment> segments;

	@Override
	public int getCircleCenterX() {
		return (int)getHeadX();
	}

	@Override
	public int getCircleCenterY() {
		return (int)getHeadY();
	}

	public int getSize() {
		return mSize;}


	int worldCenterX;
	int worldCenterY;

	public int getWorldCenterY() {
		return worldCenterY;
	}

	public void setWorldCenterY(int worldCenterY) {
		this.worldCenterY = worldCenterY;
	}

	public int getWorldCenterX() {
		return worldCenterX;
	}

	public void setWorldCenterX(int worldCenterX) {
		this.worldCenterX = worldCenterX;
	}

	public float getHeadX() {
		return segments.get(0).x;
	}

	public float getHeadY() {
		return segments.get(0).y;
	}


	public Sock getSock(){
		return new Sock(segments, worldCenterX, worldCenterY);
	}

	static class Segment  implements CircleView{

		float x;
		float y;
		float size;

		public Segment() {}

		public Segment(float x, float y, float size) {
			this.x = x;
			this.y = y;
			this.size = size;
		}

		@Override
		public String toString(){
			return "[" + x + "," + y + "]";
		}

		public void shift(int dx, int dy) {
			x = x - dx;
			y = y - dy;
		}

		@Override
		public int getCircleCenterX() {
			return (int)x;
		}

		@Override
		public int getCircleCenterY() {
			return (int)y;
		}

		@Override
		public int getSize() {
			return (int)size;
		}
	}

	//Shift all segments
	public void shift(int x, int y) {

		for (Segment s : segments) {
			s.shift(x, y);
		}

	}


	public SockView(Context context, Sock sock) {
		super(context);
		segments = sock.getSegments();

		//todo - what else should be set?
		mPaint.setStyle(Paint.Style.FILL);

		//sock.setWorldCenterX(worldCenterX);
		//sock.setWorldCenterY(worldCenterY);

	}

	public SockView(Context context, int centerx, int centery) {
		super(context);

		this.centerX = centerx;
		this.centerY = centery;

		segments = new ArrayList<>();
		segments.add(new Segment(centerx, centery, mSize));

		Log.i(TAG, "new sock" + segments);

		this.x = x ; this.y = y;
		mPaint.setStyle(Paint.Style.FILL);

	}


	//Add to end of segments (?)
	public void addSegmentToEnd(float x, float y) {
		segments.add(new Segment(x, y, mSize));         //add to end
		//Log.d(TAG, "Added segment to end " + +x + " " + y +  " " + segments);
	}


	public void addSegmentRelativeToHead(float xDiff, float yDiff) {

		Log.i(TAG, "Adding segment " + xDiff + " " + yDiff);

		//shift everything
		shift((int)xDiff, (int)yDiff);
		segments.add(0, new Segment(centerX, centerY, mSize));

	}

	public void removeLast() {
		segments.remove(segments.size() -1);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		Log.i(TAG, "drawing " + x + " " + y);

		int red = 50;
		int blue = 100;

		if (segments != null) {            ///todo why are segments null? What is FB returning?
			for (Segment s : segments) {
				red = (red + 20) % 255;
				blue = (blue + 15) % 255;
				mPaint.setARGB(150, red, 0, blue);
				canvas.drawCircle(s.x, s.y, mSize, mPaint);
			}
		}

	}

	@Override
	public String toString() {
		return segments.toString();
	}

}

