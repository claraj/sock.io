package com.clara.socksio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;


/** TODO restarting not working properly - can restart +1 snake
 * TODO Scroll background, instead of moving snake
 * TODO Firebase - adversarial socks.
 * */

public class SockActivity extends AppCompatActivity {

	FrameLayout mFrame;
	TextView mGameOver;

	View.OnClickListener restartListener;

	private static String TAG = "SOCK ACTIVITY";

	private SockView mSock;
	private LinkedList<SpeckView> mSpecks;

	private int speckCount = 20;

	private long period = 100;
	private long maxDistanceMoved = 20;
	private float angle = 1;
	private float xMoveDist = 14f;
	private float yMoveDist = 14f;

	private float maxX;
	private float maxY;

	private float score = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sock);

		mFrame = (FrameLayout) findViewById(R.id.fullscreen_content);
		mGameOver = (TextView) findViewById(R.id.game_over_msg);

		restartListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				restart();
			}
		};

	}

	private void restart() {

		score = 0;

		mGameOver.setVisibility(TextView.INVISIBLE);

		//remove listener

		mGameOver.setOnClickListener(null); // no more restarting!

		//remove old specks

		mFrame.removeView(mSock);

		if (mSpecks != null) {
			for (SpeckView speck : mSpecks) {
				mFrame.removeView(speck);
			}
		}

		mSpecks = null;

		makeSpecksAddtoView();

		mSock = new SockView(SockActivity.this, 70, 70);
		mSock.addSegmentToEnd(50, 50);
		mSock.addSegmentToEnd(40, 40);

		mFrame.addView(mSock);

		updateSock();   // go!

		//** Stack overflow ftw http://stackoverflow.com/questions/10845172/android-running-a-method-periodically-using-postdelayed-call */
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "TICK");
				updateSock();
				if (!endGame()) {
					handler.postDelayed(this, period);
				}
			}
		}, period);


		mFrame.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {

				Log.i(TAG, "touch event");

				Log.i(TAG, ""+ motionEvent.getActionMasked());

				Log.i(TAG, "X EVENT = " + motionEvent.getX() + " Y EVENT = " + motionEvent.getY());

				switch (motionEvent.getActionMasked()) {

					case MotionEvent.ACTION_DOWN:{
						Log.i(TAG, "action down");
					}

					case MotionEvent.ACTION_MOVE: {

						Log.i(TAG, "action move");

						//tell Sock to move   todo - only if moved more than a little bit.

						//mSock.addSegmentToStart(motionEvent.getX(), motionEvent.getY());

						//Where is touch relative to Sock head?

						float sockHeadX = mSock.getHeadX();
						float sockHeadY = mSock.getHeadY();

						float touchX = motionEvent.getX();
						float touchY = motionEvent.getY();

						float xDelta = touchX - sockHeadX;
						float yDelta = touchY - sockHeadY;

						//Angle is tan(angle) = opp/adj = xDelta / yDelta
						angle = (float) Math.atan(yDelta / xDelta);


						if (xDelta < 0 ) { angle += Math.PI; };

						//So, scaling to triangle with hypotenuse = maxDistanceMoved
						//    sin(angle) = opp / hyp =  OR  opp = xdistmove = sin(angle) * hyp
						//    cos(angle) = adj / hyp    OR  adj = ydistmove = cos(angle) * hyp
						xMoveDist = (float) Math.cos(angle) * maxDistanceMoved;
						yMoveDist = (float) Math.sin(angle) * maxDistanceMoved;


						Log.w(TAG, "Angle in rads " + angle + " headX " + sockHeadX + " headY " + sockHeadY + " xtouch " + touchX + " y touch " + touchY + " xdelta " + xDelta + " ydelta " + yDelta + " xmovedist " + xMoveDist + " ymovedist " + yMoveDist);

						break;
					}
				}

				return true;
			}
		});
	}



	private void makeSpecksAddtoView() {



		mSpecks = new LinkedList<>();

		for (int s = 0 ; s < speckCount ; s++) {

			SpeckView speck = new SpeckView(this, maxX, maxY);

			speck.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			mSpecks.add(speck);
			mFrame.addView(speck);

		}


		Log.i(TAG, "Added intital specks: " + mSpecks);

	}


	private class SpeckView extends View {

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

		@Override
		protected void onDraw(Canvas canvas) {
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setARGB(170, 255, 255, 255);  //white transparent

			canvas.drawCircle(x, y, size, mPaint);
//			canvas.drawCircle(0, 0, size, mPaint);


		}

		@Override
		public String toString() {
			return "x=" + x + " y=" + y + " eaten? " + eaten;
		}


//		@Override
//		public void onMeasure(int h, int w) {
//			setMeasuredDimension(size, size);
//		}

	}

	private class SockView extends View {
		public float getHeadX() {
			return segments.getFirst().x;
		}

		public float getHeadY() {
			return segments.getFirst().y;
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
		}

		LinkedList<Segment> segments;

		@Override
		public void setX(float x) {
			this.x = x;
		}

		@Override
		public void setY(float y) {
			this.y = y;
		}

		private float x, y;
		private final Paint mPaint = new Paint();
		private float mSize = 20;

		public SockView(Context context, float x, float y) {
			super(context);

			segments = new LinkedList<>();
			segments.add(new Segment(x, y));

			Log.i(TAG, "new sock" + segments);

			this.x = x ; this.y = y;
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setARGB(255, 255, 0, 255);  //magenta?

		}


		//Add to end of segments (?)
		public void addSegmentToEnd(float x, float y) {
			segments.add(new Segment(x, y));         //add to end
			Log.d(TAG, "Added segment to end " + +x + " " + y +  " " + segments);
		}

		//Add to start of segments (?)
		public void addSegmentToStart(float x, float y) {
			segments.addFirst(new Segment(x, y));         //add to start
		}

		public void addSegmentRelativeToHead(float xDiff, float yDiff) {

			Log.i(TAG, "Adding segment " + xDiff + " " + yDiff);

			//head is first segment
			Segment head = segments.get(0);
			segments.addFirst(new Segment( head.x + xDiff , head.y + yDiff));

		}

		public void removeLast() {
			segments.removeLast();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			Log.i(TAG, "drawing " + x + " " + y);

			int blue = 255, red=0;
			for (Segment s : segments) {
				red = (red+30) % 255;
				blue = (blue+40) % 255;

				mPaint.setARGB(255, red, 0, blue);
				canvas.drawCircle(s.x, s.y, mSize, mPaint);
				//Log.i(TAG, "blue = " + blue);
			}

		}

		@Override
		public String toString() {
			return segments.toString();
		}

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		Log.i(TAG, mFrame.getMeasuredHeight() + " " + mFrame.getMeasuredWidth());

		maxX = mFrame.getWidth();
		maxY = mFrame.getHeight();

		restart();
	}

    private boolean endGame() {
	    //sock off screen?

		mGameOver.setVisibility(TextView.VISIBLE);
		mGameOver.setOnClickListener(restartListener);


		if (mSock.getHeadX() < 0 || mSock.getHeadY() < 0 || mSock.getHeadX() > maxX || mSock.getHeadY() > maxY) {
			Log.i(TAG, "Head is off screen " + mSock.getHeadX() +"  "+mSock.getHeadY());

			Log.i(TAG, "hit wall");

			mGameOver.setText("YOU HIT THE WALL, YOU LOSER\nSCORE = " + score);
			return true;
		}

		if (mSpecks.size() == 0) {
			//all specks eaten

			mGameOver.setText("ATE ALL THE SPECKS, YOU LARDY THING\n SCORE = " + score);

			Log.i(TAG, "eaten all specks");



			return true;   //todo put back

		}

		Log.i(TAG, "game on");
		return false;
    }


	private int eatSpecks() {

		//which specks are 'eaten' by sock? Remove.

		int specksEaten = 0;

		for (SpeckView speck : mSpecks) {

			//under sock? remove
			if (intersects(speck, mSock)) {

				mFrame.removeView(speck);

				//speck.invalidate(); //?
				score++;
				specksEaten++;
				Log.i(TAG, "Eaten speck, score is " + score);
				speck.eaten = true;   //flag speck for removal
			}
		}

		Log.i(TAG, "Checking and removing specks. " + mSpecks);

		//TODO a better way? Filtering the list.

		LinkedList<SpeckView> temp = new LinkedList<>();
		for (SpeckView speck : mSpecks) {
			if (speck.eaten == false) {
				temp.add(speck);
			}
		}

		mSpecks = temp;

		Log.i(TAG, "Cleared eaten specks. " + mSpecks);


		return specksEaten;

	}


	private boolean intersects(SpeckView speck, SockView sock) {

		int intersect = 20;

		int xdif = Math.abs((int)sock.getHeadX() - speck.x);
		int ydif = Math.abs((int)sock.getHeadY() - speck.y);

		if (xdif < intersect && ydif < intersect) {
			return true;
		}

		return  false;


//
//
//
//		// <3 stack overflow http://stackoverflow.com/questions/26252710/detect-if-views-are-overlapping.
//
//		int[] firstPosition = new int[2];
//		int[] secondPosition = new int[2];
//
//		view1.getLocationOnScreen(firstPosition);
//		view2.getLocationOnScreen(secondPosition);  //plunks coords into the array argument.
//
//		Log.i(TAG, "View 1 (Speck?) " + firstPosition[0] + " " + firstPosition[1] + " " + view1.getMeasuredHeight() + "  " + view1.getHeight() +  " " + view1.getMeasuredWidth() + " " + view1.getWidth());
//		Log.i(TAG, "View 2 (Sock)" + secondPosition[0] + " " + secondPosition[1] + " " + view2.getMeasuredHeight() + "  " + view2.getHeight() +  " " + view2.getMeasuredWidth() + " " + view2.getWidth());
//
//
//		// Rect constructor parameters: left, top, right, bottom
//		Rect rectFirstView = new Rect(firstPosition[0], firstPosition[1],
//				firstPosition[0] + view1.getMeasuredWidth(), firstPosition[1] + view1.getMeasuredHeight());
//		Rect rectSecondView = new Rect(secondPosition[0], secondPosition[1],
//				secondPosition[0] + view2.getMeasuredWidth(), secondPosition[1] + view2.getMeasuredHeight());
//		boolean i = rectFirstView.intersect(rectSecondView);
//
//		Log.i(TAG, "intersects = "  +i);
//		//end stackoverflow code.
//		return i;
	}


	private void updateSock() {

			//Move sock by adding a new segment and removing last

			Log.i(TAG, "update sock");

			mSock.addSegmentRelativeToHead(xMoveDist, yMoveDist);

			int specksEaten = eatSpecks();

			if (specksEaten == 0) {
				mSock.removeLast();
			}

			mSock.invalidate();

			Log.i(TAG, mSock.toString());

		}

	}

