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

public class SockView extends View {

	private static String TAG = "SOCKVIEW";

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	private boolean local = true;

	private int centerX;
	private int centerY;

	private float x, y;
	private final Paint mPaint = new Paint();
	private int mSize = 20;

	//List of segments central co-ordinates
	ArrayList<Segment> segments;

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

	static class Segment {

		float x;
		float y;

		public Segment() {}

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

	//Shift all segments
	public void shift(int x, int y) {

		if (segments!=null) {    //todo why is fb returning empty socks?

			for (Segment s : segments) {
				s.shift(x, y);
			}
		}
	}


	public SockView(Context context, Sock sock) {
		super(context);
		segments = sock.getSegments();

		//todo - what else should be set?
		mPaint.setStyle(Paint.Style.FILL);

		sock.setWorldCenterX(worldCenterX);
		sock.setWorldCenterY(worldCenterY);

	}

	public SockView(Context context, int centerx, int centery) {
		super(context);

		this.centerX = centerx;
		this.centerY = centery;

		segments = new ArrayList<>();
		segments.add(new Segment(centerx, centerY));

		Log.i(TAG, "new sock" + segments);

		this.x = x ; this.y = y;
		mPaint.setStyle(Paint.Style.FILL);

		Random rnd = new Random();
		blue = rnd.nextInt(255);
		green = rnd.nextInt(255);
		red = rnd.nextInt(255);
	}


	//Add to end of segments (?)
	public void addSegmentToEnd(float x, float y) {
		segments.add(new Segment(x, y));         //add to end
		//Log.d(TAG, "Added segment to end " + +x + " " + y +  " " + segments);
	}


	public void addSegmentRelativeToHead(float xDiff, float yDiff) {

		Log.i(TAG, "Adding segment " + xDiff + " " + yDiff);

		//shift everything
		shift((int)xDiff, (int)yDiff);
		segments.add(0, new Segment(centerX, centerY));

	}

	public void removeLast() {
		segments.remove(segments.size() -1);
	}

	private int blue;
	private int red;
	private int green;

	@Override
	protected void onDraw(Canvas canvas) {
		Log.i(TAG, "drawing " + x + " " + y);

		if (segments != null) {            ///todo why are segments null? What is FB returning?
			for (Segment s : segments) {
				red = (red + 20) % 255;
				blue = (blue + 15) % 255;
				green = (green - 25) % 255;

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

