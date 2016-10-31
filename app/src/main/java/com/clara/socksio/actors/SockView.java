package com.clara.socksio.actors;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.clara.socksio.Sock;
import com.clara.socksio.actors.CircleView;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by clara on 9/22/16.
 */

public class SockView extends View implements CircleView {

	private static String TAG = "SOCKVIEW";

	private int centerX;
	private int centerY;

	private float x, y;
	private final Paint mPaint = new Paint();
	protected int mSize = 20;

	//List of segments central co-ordinates
	public ArrayList<Segment> segments;

	private int mScore;

	public void reset () {
		mScore = 0;
		Segment s0 = segments.get(0);
		Segment s1 = segments.get(1);
		Segment s2 = segments.get(2);  //hacky!

		segments.clear();
		segments.add(s0);
		segments.add(s1);
		segments.add(s2);



	}

	@Override
	public int getCircleCenterX() {
		return (int)getHeadX();
	}

	@Override
	public int getCircleCenterY() {
		return (int)getHeadY();
	}

	public int getSize() {
		return mSize;
	}


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
		return new Sock(segments, worldCenterX, worldCenterY, mSize);
	}

	@IgnoreExtraProperties
	public static class Segment  implements CircleView{

		float x;
		float y;
		float size;

		public Segment() {}   //needed by firebase

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

		@Exclude
		@Override
		public int getCircleCenterX() {
			return (int)x;
		}

		@Exclude    //Firebase ignore
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
		mSize = sock.size;
		mScore = sock.getScore();

		setStartColors();

	}


	public SockView(Context context, int centerx, int centery) {
		super(context);

		this.centerX = centerx;
		this.centerY = centery;

		segments = new ArrayList<>();
		segments.add(new Segment(centerx, centery, mSize));

		Log.i(TAG, "new sock" + segments);

		setStartColors();

		//this.x = x ; this.y = y;
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


	/// todo create a selection of nice start colors and pick one at random from that.
	//Also would like to avoid colors similar to the background
	private void setStartColors() {
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());

		redStart = rnd.nextInt(255);
		blueStart = rnd.nextInt(255);
		greenStart = rnd.nextInt(255);

		Log.d(TAG, "Start colors r: " + redStart + " g: " + greenStart + " b: " + blueStart);
	}

	int redStart;
	int blueStart;
	int greenStart;

	@Override
	protected void onDraw(Canvas canvas) {
		Log.i(TAG, "drawing " + x + " " + y);

		int red = redStart;
		int blue = blueStart;
		int green = greenStart;      //cycle just red and blue to keep colors within similar palette

		if (segments != null) {
			for (Segment s : segments) {
				red = (red + 20) % 255;
				blue = (blue + 15) % 255;

				mPaint.setARGB(150, red, green, blue);
				canvas.drawCircle(s.x, s.y, mSize, mPaint);
			}
		}

	}

	@Override
	public String toString() {
		return segments.toString();
	}

}

