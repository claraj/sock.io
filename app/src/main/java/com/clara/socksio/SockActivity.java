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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedList;
import java.util.Random;


/** TODO Firebase - adversarial socks.
 *
 *
 * TODO Dryers can still get off the edge of the world? Prevailing direction needs to be set to point toward center.  Crop dryer png and/or get someone who can draw to draw it. Prefer washing machine :)
 * TODO handle rotation, instance state, stopping stuff on close. Everything needs to be re-centered on rotation.
 *
 * CODE IS MESSY! Utility method to check whether a point is within a circle, for example
 * TODO interface background, interface movesWithPlayer, has shift method
 * TODO int or float? Too many casts
 * TODO app icon
 *
 *
 * TODO FIREBASE
 *
 * Remove dryers. Only for no internet play.
 * Specks can be are generated locally to each device.
 * Generate unique ID for this sock
 * Every clock tick, get location & quantity of other socks, and draw on screen
 * A sock needs a score; a list of segment x-y locations, segment size
 * Locally work out collisions
 * Game number for dividing socks into possible +1 game
 * Send message to server with new location OR if have died.
 * Check number of other socks to see if have won or not.
 *
 * Major config issues - how to clear data from DB? e.g. if apps crash or idle?
 * */

public class SockActivity extends AppCompatActivity {

	FrameLayout mFrame;
	TextView mGameOver;

	View.OnClickListener restartListener;

	private static String TAG = "SOCK ACTIVITY";

	private static SockView mSock;
	private static LinkedList<SpeckView> mSpecks;
	private static WorldView mWorld;
	private static LinkedList<DryerView> mDryers;

	private int speckCount = 200;
	private int dryerCount = 5;

	private long period = 100;
	private long maxDistanceMoved = 15;
	private float angle = 1;
	private float xMoveDist = 14f;
	private float yMoveDist = 14f;

	private float maxX;
	private float maxY;

	private int centerX;
	private int centerY;
	private int worldRadius = 1500;

	private float score = 0;
	private boolean mLocal = true;

	private DatabaseReference mSockDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sock);

		mFrame = (FrameLayout) findViewById(R.id.fullscreen_content);
		mGameOver = (TextView) findViewById(R.id.game_over_msg);

		//Internet connection?
		mLocal = !connectedToFirebase();

		String gameTypeMessage = mLocal ? "No connection to server. Battle the dryers" : "Sock VS Sock";
		Toast.makeText(this, gameTypeMessage, Toast.LENGTH_SHORT).show();

		restartListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				restart();
			}
		};

	}

	private boolean connectedToFirebase() {

		try {
			FirebaseAuth fbauth = FirebaseAuth.getInstance();

			Log.i("TAG", " current user : " + fbauth.getCurrentUser());

			FirebaseDatabase database = FirebaseDatabase.getInstance();

			Log.i(TAG, " database : " + database);

			DatabaseReference sockDatabase = database.getReference();

			return true;
		} catch (Exception e) {
			Log.e(TAG, "NOT CONNECTED ", e);
			return false; //todo presumably something will break if no connection. probably some more checks!
		}
	}

	private void restart() {

		//reset score, remove message - //TODO doesn't go away?
		score = 0;

		mGameOver.setVisibility(View.INVISIBLE);   //fixme ?
		mGameOver.setText("");  //hacky hack. why setVis not working?

		//remove listener from TextView
		mGameOver.setOnClickListener(null); // no more restarting!

		//remove old specks, sock, dryers, world...

		mFrame.removeView(mSock);
		if (mSpecks != null) {
			for (SpeckView speck : mSpecks) {
				mFrame.removeView(speck);
			}
		}

		if (mDryers != null) {
			for (DryerView dryer : mDryers) {
				mFrame.removeView(dryer);
			}
		}

		mFrame.removeView(mWorld);

		mWorld = new WorldView(this, centerX, centerY, worldRadius);
		mFrame.addView(mWorld);

		//Create new specks...
		makeSpecksAddToView();
		//And new sock


		mSock = new SockView(SockActivity.this, centerX, centerY);
		mSock.addSegmentToEnd(centerX+10, centerY+10);
		mSock.addSegmentToEnd(centerX+10, centerY+10);
		mFrame.addView(mSock);

		//Add tumble dryers
		createLocalAdversaries();


		//and add boundary


		updateSock();   // go!
		updateSpecks();
		updateDryers();  //adversaries

		//** Stack overflow ftw http://stackoverflow.com/questions/10845172/android-running-a-method-periodically-using-postdelayed-call */
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "TICK");

				if (mLocal) {

					updateSock();
					updateSpecks();
					updateDryers();

					if (!endLocalGame()) {
						handler.postDelayed(this, period); //run again!
					}
				}

				else {

					getStateFromFirebase();
					updateSock();
					updateSpecks();
					//updateDryers();
					sendNewStateToFirebase();

					if (!endFirebaseGame()) {
						handler.postDelayed(this, period); //run again!
					}

				}



			}
		}, period);

		mFrame.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {

				//Log.i(TAG, "touch event");

				//Log.i(TAG, ""+ motionEvent.getActionMasked());

				//Log.i(TAG, "X EVENT = " + motionEvent.getX() + " Y EVENT = " + motionEvent.getY());

				switch (motionEvent.getActionMasked()) {

					case MotionEvent.ACTION_DOWN:{
						//Log.i(TAG, "action down");
					}

					case MotionEvent.ACTION_MOVE: {

						//Log.i(TAG, "action move");

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


						//Log.w(TAG, "Angle in rads " + angle + " headX " + sockHeadX + " headY " + sockHeadY + " xtouch " + touchX + " y touch " + touchY + " xdelta " + xDelta + " ydelta " + yDelta + " xmovedist " + xMoveDist + " ymovedist " + yMoveDist);

						break;
					}
				}

				return true;
			}
		});
	}

	private void getStateFromFirebase() {
		//TODO get locations of all other socks and their scores

		mSockDatabase.push().setValue(mSock.getSock());   //I have no idea what i'm doing
		//seems we need a callback to identify when connected.
	}

	private void sendNewStateToFirebase() {
		//TODO where is this sock, and current score
		//Should be able to configure listener for data changed.
	}


	private boolean endFirebaseGame() {

		//TODO If all other socks dead OR we have died...

		mSockDatabase.removeValue();  //remove self.

		return false;

	}

	private void createLocalAdversaries() {

		//The specks put themselves in random places in a box.

		mDryers = new LinkedList<>();

		//Perhaps we want to decide where the dryers go?

		Random rnd = new Random();

		for (int d = 0 ; d < dryerCount ; d++) {

			//How big is the world?

			int radWithMargin = worldRadius - 100;  //todo measure view bitmap

			int x = (radWithMargin - rnd.nextInt(radWithMargin*2));
			int y = (radWithMargin - rnd.nextInt(radWithMargin*2));

			//Pythagoras!
			while (x*x + y*y > radWithMargin*radWithMargin) {
				 x = (radWithMargin - rnd.nextInt(radWithMargin*2));
				 y = (radWithMargin - rnd.nextInt(radWithMargin*2));
			}

			x += centerX;   //And shift to center
			y += centerY;

			DryerView dryer = new DryerView(this, x, y);
			mFrame.addView(dryer);
			mDryers.add(dryer);

		}

		//Log.i(TAG, "Dryers added : " + mDryers);
	}

	private void updateDryers() {
		//Move randomly

		for (DryerView dryer : mDryers) {
			dryer.shift((int)xMoveDist, (int)yMoveDist); //keep on screen  (?)
			dryer.wander(centerX, centerY, worldRadius);
			dryer.invalidate();
		}

		//Log.i(TAG, "Dryers updated : " + mDryers);


	}


	private void makeSpecksAddToView() {

		mSpecks = new LinkedList<>();

		for (int s = 0 ; s < speckCount ; s++) {
			SpeckView speck = new SpeckView(this, centerX, centerY, worldRadius);
			mSpecks.add(speck);
			mFrame.addView(speck);
		}
		//Log.i(TAG, "Added initial specks: " + mSpecks);

	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		//Mostly figuring out what size the screen is
		//Log.i(TAG, mFrame.getMeasuredHeight() + " " + mFrame.getMeasuredWidth());

		maxX = mFrame.getWidth();
		maxY = mFrame.getHeight();

		centerX = (int) maxX / 2;
		centerY = (int) maxY / 2;

		//And start game.
		restart();
	}


    private boolean endLocalGame() {

		//Check various ways the game can end

	    //sock off screen? This isn't possible with the snake centered and background scrolling.

		mGameOver.setVisibility(TextView.VISIBLE);
		mGameOver.bringToFront();
		mGameOver.setOnClickListener(restartListener);

		String gameOverText = "";
		boolean gameOver = false;

		if (mSock.getHeadX() < 0 || mSock.getHeadY() < 0 || mSock.getHeadX() > maxX || mSock.getHeadY() > maxY) {
			//Log.i(TAG, "Head is off screen " + mSock.getHeadX() +"  "+mSock.getHeadY());

			//Log.i(TAG, "hit wall");

			gameOverText = "YOU HIT THE WALL, YOU LOSE";
			gameOver = true;
		}


		int xdiff = Math.abs(mWorld.getCenterX() - (int) mSock.getHeadX());
		int ydiff = Math.abs(mWorld.getCenterY() - (int) mSock.getHeadY());

//				Log.i(TAG, "world x "+ mWorld.getCenterX() + " sock x " +  mSock.getHeadX() + " xdiff = " + (mWorld.getCenterX() - mSock.getHeadX()) + " xdiff2 " + (xdiff*xdiff)
//						+ " world y " +  mWorld.getCenterY() + " sock y " + mSock.getHeadY()  + "ydiff" + (mWorld.getCenterY() - mSock.getHeadY()) + " ydiff2 " + (ydiff*ydiff)
//						+ " world radius " + worldRadius + " rad2 " + worldRadius*worldRadius ) ;

		if (xdiff*xdiff + ydiff*ydiff > worldRadius*worldRadius) {
			Log.i(TAG, "Sock leaves world");
			gameOverText = "YOU FELL OFF THE WORLD";
			gameOver = true;

		}


		//But if all specks eaten
		if (mSpecks.size() == 0) {
			//all specks eaten

			gameOverText = "ATE ALL THE SPECKS!!";
			Log.i(TAG, "eaten all specks");
			gameOver = true;

		}


		//TODO adversarial washing machines eat sock

		if (eatenByDryer()) {
			gameOverText = "THE DRYER GOT YOU";
			gameOver = true;

		}

		if (gameOver) {
			gameOverText += "\n" ;
			gameOverText += "SCORE = " + (int) score;
			gameOverText += "\n" ;
			gameOverText += "* tap to replay *";
			mGameOver.setVisibility(View.VISIBLE);
			mGameOver.bringToFront();
			mGameOver.setText(gameOverText);
		}

		else {
			Log.i(TAG, "game on");
		}

		return gameOver;
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
				//Log.i(TAG, "Eaten speck, score is " + score);
				speck.eaten = true;   //flag speck for removal
			}
		}

		//Log.i(TAG, "Checking and removing specks. " + mSpecks);

		//TODO a better way? Filtering the list.

		LinkedList<SpeckView> temp = new LinkedList<>();
		for (SpeckView speck : mSpecks) {
			if (!speck.eaten) {
				temp.add(speck);
			}
		}

		mSpecks = temp;

		//Log.i(TAG, "Cleared eaten specks. " + mSpecks);


		return specksEaten;

	}

	private boolean eatenByDryer() {

		for (DryerView dryer : mDryers) {
			if (intersects(dryer, mSock)) {

				return true;
			}
		}

		return false;

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


	private boolean intersects(DryerView dryer, SockView sock) {

		int intersect = sock.getSize();  //RADIUS of sock head

		int sockHeadX = (int)sock.getHeadX();
		int sockHeadY = (int)sock.getHeadY();

		float dryerHeight = dryer.height();
		//float dryerMHeight = dryer.getMeasuredHeight();

		float dryerWidth = dryer.width();
		//float dryerMWidth = dryer.getMeasuredWidth();

		float dryerTopX = dryer.x();
		float dryerEndX = dryerTopX + dryerWidth;
		float dryerTopY = dryer.y();
		float dryerEndY = dryerTopY + dryerHeight;

//		Log.i(TAG, sockHeadX + " "
//				+ sockHeadY + " "
//				+ dryerTopX  + " "
//				+  dryerTopX  + " " +
//				dryerHeight  + " " +
//				 dryerWidth
//						+ " " +
//						dryerEndX  + " " +
//						dryerEndY
//				 );

		if (sockHeadX > dryerTopX && sockHeadX < dryerEndX &&
				sockHeadY > dryerTopY && sockHeadY < dryerEndY) {
			//Log.i(TAG, "dryer-sock collision");
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

			//Log.i(TAG, "update sock");

			mSock.addSegmentRelativeToHead(xMoveDist, yMoveDist);

			int specksEaten = eatSpecks();

			if (specksEaten == 0) {
				mSock.removeLast();
			}

			mSock.invalidate();

			//Log.i(TAG, mSock.toString());

		}

	}

