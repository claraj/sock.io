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


/** TODO restarting not working properly - can restart +1 snake with +1 clock ticking. Also remove message.
 * TODO Boundaries!
 * TODO Firebase - adversarial socks.
 * TODO adversarial tumble dryers
 * TODO handle rotation
 * */

public class SockActivity extends AppCompatActivity {

	FrameLayout mFrame;
	TextView mGameOver;

	View.OnClickListener restartListener;

	private static String TAG = "SOCK ACTIVITY";

	private SockView mSock;
	private LinkedList<SpeckView> mSpecks;
	private WorldView mWorld;

	private int speckCount = 20;

	private long period = 100;
	private long maxDistanceMoved = 20;
	private float angle = 1;
	private float xMoveDist = 14f;
	private float yMoveDist = 14f;

	private float maxX;
	private float maxY;

	private int centerX;
	private int centerY;
	private int worldRadius = 2000;

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

		//reset score, remove message
		score = 0;
		mGameOver.setVisibility(TextView.INVISIBLE);

		//remove listener from TextView
		mGameOver.setOnClickListener(null); // no more restarting!

		//remove old specks
		mFrame.removeView(mSock);

		if (mSpecks != null) {
			for (SpeckView speck : mSpecks) {
				mFrame.removeView(speck);
			}
		}

		//Create new specks...
		makeSpecksAddToView();
		//And new sock
		mSock = new SockView(SockActivity.this, centerX, centerY);
		mSock.addSegmentToEnd(50, 50);
		mSock.addSegmentToEnd(40, 40);

		mFrame.addView(mSock);

		//and add boundary

		mWorld = new WorldView(this, centerX, centerY, worldRadius);
		mFrame.addView(mWorld);

		updateSock();   // go!
		updateSpecks();

		//** Stack overflow ftw http://stackoverflow.com/questions/10845172/android-running-a-method-periodically-using-postdelayed-call */
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "TICK");
				updateSock();
				updateSpecks();
				if (!endGame()) {
					handler.postDelayed(this, period); //run again!
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

						if (xDelta < 0 ) { angle += Math.PI; };  //mathy fix

						//So, scaling to triangle with hypotenuse = maxDistanceMoved
						//    sin(angle) = opp / hyp =  OR  opp = xdistmove = sin(angle) * hyp
						//    cos(angle) = adj / hyp    OR  adj = ydistmove = cos(angle) * hyp
						xMoveDist = (float) Math.cos(angle) * maxDistanceMoved;
						yMoveDist = (float) Math.sin(angle) * maxDistanceMoved;

//						xMoveDist = -xMoveDist;
//						yMoveDist = -yMoveDist;


						Log.w(TAG, "Angle in rads " + angle + " headX " + sockHeadX + " headY " + sockHeadY + " xtouch " + touchX + " y touch " + touchY + " xdelta " + xDelta + " ydelta " + yDelta + " xmovedist " + xMoveDist + " ymovedist " + yMoveDist);

						break;
					}
				}

				return true;
			}
		});
	}



	private void makeSpecksAddToView() {

		mSpecks = new LinkedList<>();

		for (int s = 0 ; s < speckCount ; s++) {
			SpeckView speck = new SpeckView(this, maxX, maxY);
			mSpecks.add(speck);
			mFrame.addView(speck);
		}
		Log.i(TAG, "Added initial specks: " + mSpecks);

	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		//Mostly figuring out what size the screen is
		Log.i(TAG, mFrame.getMeasuredHeight() + " " + mFrame.getMeasuredWidth());

		maxX = mFrame.getWidth();
		maxY = mFrame.getHeight();

		centerX = (int) maxX / 2;
		centerY = (int) maxY / 2;

		//And start game.
		restart();
	}


    private boolean endGame() {

		//Check various ways the game can end

	    //sock off screen? This isn't possible with the snake centered and background scrolling.

		mGameOver.setVisibility(TextView.VISIBLE);
		mGameOver.setOnClickListener(restartListener);

		if (mSock.getHeadX() < 0 || mSock.getHeadY() < 0 || mSock.getHeadX() > maxX || mSock.getHeadY() > maxY) {
			Log.i(TAG, "Head is off screen " + mSock.getHeadX() +"  "+mSock.getHeadY());

			Log.i(TAG, "hit wall");

			mGameOver.setText("YOU HIT THE WALL, YOU LOSE\nSCORE = " + score);
			return true;
		}

		//Sock leaves border?  TODO this does not work. Fixme.

		Log.i(TAG, "world x "+ mWorld.getCenterX() + " sock x " +  mSock.getHeadX() + " world y " +  mWorld.getCenterY() + " sock y " + mSock.getHeadY() + " world radius " + worldRadius) ;

		if ( Math.abs(mWorld.getCenterX() - mSock.getHeadX()) > worldRadius && Math.abs( mWorld.getCenterY() - mSock.getHeadY() ) > worldRadius) {
			Log.i(TAG, "Sock leaves world");
			mGameOver.setText("YOU FELL OFF THE WORLD, YOU LOSE\nSCORE = " + score);
			return true;
		}


		//But if all specks eaten
		if (mSpecks.size() == 0) {
			//all specks eaten

			mGameOver.setText("ATE ALL THE SPECKS\n SCORE = " + score);
			Log.i(TAG, "eaten all specks");
			return true;
		}


		//TODO adversarial washing machines eat sock

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
			if (!speck.eaten) {
				temp.add(speck);
			}
		}

		mSpecks = temp;

		Log.i(TAG, "Cleared eaten specks. " + mSpecks);


		return specksEaten;

	}


	private boolean intersects(SpeckView speck, SockView sock) {

		int intersect = sock.getSize();

		int xdif = Math.abs((int)sock.getHeadX() - speck.x);
		int ydif = Math.abs((int)sock.getHeadY() - speck.y);

		if (xdif < intersect && ydif < intersect) {
			return true;
		}

		return  false;
	}


	private void updateSpecks() {
		for (SpeckView speck : mSpecks) {
			speck.shift((int)xMoveDist, (int)yMoveDist);
			speck.invalidate();
		}

		mWorld.shift((int)xMoveDist, (int)yMoveDist);
		mWorld.invalidate();

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

