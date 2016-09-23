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
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;


/** TODO restarting not working properly - can restart +1 snake with +1 clock ticking. Also remove message.
 * TODO Boundaries! TODO sock fall off world
 * TODO Firebase - adversarial socks.
 * TODO specks need to be distributed around world
 * TODO adversarial tumble dryers.
 * TODO Dryers are getting stuck on the edge of the world. Prevailing direction needs to be set to point toward center.  Crop dryer png and/or get someone who can draw to draw it. Prefer washing machine :)
 * TODO handle rotation, instance state, stopping stuff on close.
 * TODO interface background, interface movesWithPlayer, has shift method
 * TODO int or float? Too many casts
 * TODO specks are not in the right place
 * TODO end message should be on top of game widgets
 * */

public class SockActivity extends AppCompatActivity {

	FrameLayout mFrame;
	TextView mGameOver;

	View.OnClickListener restartListener;

	private static String TAG = "SOCK ACTIVITY";

	private SockView mSock;
	private LinkedList<SpeckView> mSpecks;
	private WorldView mWorld;
	private LinkedList<DryerView> mDryers;

	private int speckCount = 200;
	private int dryerCount = 5;

	private long period = 100;
	private long maxDistanceMoved = 20;
	private float angle = 1;
	private float xMoveDist = 14f;
	private float yMoveDist = 14f;

	private float maxX;
	private float maxY;

	private int centerX;
	private int centerY;
	private int worldRadius = 500;

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

		//Add tumble dryers
		createLocalAdversaries();

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
		updateDryers();  //adversaries

		//** Stack overflow ftw http://stackoverflow.com/questions/10845172/android-running-a-method-periodically-using-postdelayed-call */
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "TICK");
				updateSock();
				updateSpecks();
				updateDryers();
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

	private void createLocalAdversaries() {

		//The specks put themselves in random places in a box.

		mDryers = new LinkedList<>();

		//Perhaps we want to decide where the dryers go?

		Random rnd = new Random();

		for (int d = 0 ; d < dryerCount ; d++) {

			//How big is the world?

			//Make random x, y TODO in circle

			int x = (worldRadius - rnd.nextInt(worldRadius*2));
			int y = (worldRadius - rnd.nextInt(worldRadius*2));

			//Pythagoras!
			while (x*x + y*y > worldRadius*worldRadius) {
				 x = (worldRadius - rnd.nextInt(worldRadius*2));
				 y = (worldRadius - rnd.nextInt(worldRadius*2));
			}

			x += centerX;   //And shift to center
			y += centerY;

			DryerView dryer = new DryerView(this, x, y);
			mFrame.addView(dryer);
			mDryers.add(dryer);

		}

		Log.i(TAG, "Dryers added : " + mDryers);
	}

	private void updateDryers() {
		//Move randomly

		for (DryerView dryer : mDryers) {
			dryer.shift((int)xMoveDist, (int)yMoveDist); //keep on screen  (?)
			dryer.wander(centerX, centerY, worldRadius);
			dryer.invalidate();
		}

		Log.i(TAG, "Dryers updated : " + mDryers);


	}


	private void makeSpecksAddToView() {

		mSpecks = new LinkedList<>();

		for (int s = 0 ; s < speckCount ; s++) {
			SpeckView speck = new SpeckView(this, centerX, centerY, worldRadius);
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


		int xdiff = Math.abs(mWorld.getCenterX() - (int) mSock.getHeadX());
		int ydiff = Math.abs(mWorld.getCenterY() - (int) mSock.getHeadY());

//				Log.i(TAG, "world x "+ mWorld.getCenterX() + " sock x " +  mSock.getHeadX() + " xdiff = " + (mWorld.getCenterX() - mSock.getHeadX()) + " xdiff2 " + (xdiff*xdiff)
//						+ " world y " +  mWorld.getCenterY() + " sock y " + mSock.getHeadY()  + "ydiff" + (mWorld.getCenterY() - mSock.getHeadY()) + " ydiff2 " + (ydiff*ydiff)
//						+ " world radius " + worldRadius + " rad2 " + worldRadius*worldRadius ) ;

		if (xdiff*xdiff + ydiff*ydiff > worldRadius*worldRadius) {
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

		if (dryerAteSock()) {
			mGameOver.setText("THE DRYER GOT YOU\nSCORE=" +score );
			return true;
		}

		Log.i(TAG, "game on");
		return false;
    }

	private boolean dryerAteSock() {

		//TODO!!
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

