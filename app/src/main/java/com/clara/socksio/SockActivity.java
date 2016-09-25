package com.clara.socksio;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;


/**
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
 * TODO is a new SockView - and list of enemy SockViews - being created every tick? (?) Drawing is not working.
 *
 * TODO other forms of animation. Would something else be appropriate?
 *
 *
 * TODO FIREBASE
 *
 * todo sync the local data with firebase data more efficiently. Should not create new SockViews every tick. Should update existing sockviews.
 *
 * todo deal correctly with no data connection
 * Startup needs work. First test if connection is available. If so, offer local vs. server play
 * (Should also handle loss of connection in the middle of game)
 * todo deal correctly with no other players available, fall back to no data connection
 * todo delete sock on death (ok) but make sure restart sock has default length
 *
 * todo identify collisions (sorta working? TEST)
 * todo dryer deathmatch. Any part of dryer touch sock = death.
 *
 * Remove dryers. Only for no internet play.
 * Specks can be are generated locally to each device.
 * Generate unique ID for this sock
 * Every clock tick, get location & quantity of other socks, and draw on screen
 * A sock needs a score; a list of segment x-y locations, segment size
 * Locally work out collisions
 * TODO Game number for dividing socks into possible +1 game
 * Send message to server with new location OR if have died.
 * Check number of other socks to see if have won or not.
 *
 * Major config issues - how to clear data from DB? e.g. if apps crash or idle?
 * */

public class SockActivity extends AppCompatActivity implements FirebaseInteraction.ServerDataReadyListener{

	private static final String ALL_SOCKS = "sock_data";
	FrameLayout mFrame;
	TextView mGameOver;

	boolean serverGameRunning = false;

	View.OnClickListener restartListener;

	private static String TAG = "SOCK ACTIVITY";

	private static SockView mSock;
	private static LinkedList<SpeckView> mSpecks;
	private static WorldView mWorld;
	private static LinkedList<DryerView> mDryers;

	private int speckCount = 200;
	private int dryerCount = 5;

	private long period = 150;            // time between ms
	private long maxDistanceMoved = 15;   //How far a sock can move in one tick
	private float angle = 1;             //initial angle
	private float xMoveDist = 14f;    //Amount moved in last clock tick
	private float yMoveDist = 14f;    //Amount moved in last clock tick

	private float maxX;
	private float maxY;

	private int centerX;
	private int centerY;
	private int worldRadius = 1500;

	private int score = 0;			//this sock's score
	private boolean mLocal = true;

	View.OnTouchListener mTouchListener;

	FirebaseInteraction mFirebase;

	private HashMap<String, Sock> enemySocks;      // FB keys and sock objects
	private ArrayList<SockView> enemySockViews;    // and corresponding SockViews


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sock);

		mFrame = (FrameLayout) findViewById(R.id.fullscreen_content);
		mGameOver = (TextView) findViewById(R.id.game_over_msg);

		//Internet connection?
		mFirebase = new FirebaseInteraction(this);
		mLocal = !mFirebase.connectedToFirebase();

		String gameTypeMessage = mLocal ? "No connection to server. Battle the dryers" : "Sock VS Sock";
		Toast.makeText(this, gameTypeMessage, Toast.LENGTH_SHORT).show();

		restartListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				restart();
			}
		};

		mTouchListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {

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

						//Log.w(TAG, "Angle in rads " + angle + " headX " + sockHeadX + " headY " + sockHeadY + " xtouch " + touchX + " y touch " + touchY + " xdelta " + xDelta + " ydelta " + yDelta + " xmovedist " + xMoveDist + " ymovedist " + yMoveDist);

						break;
					}
				}

				return true;
			}
		};

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		Log.i(TAG, "on Window Focus Changed");

		//Figuring out what size the screen is
		//Log.i(TAG, mFrame.getMeasuredHeight() + " " + mFrame.getMeasuredWidth());

		//AND starting game

		maxX = mFrame.getWidth();
		maxY = mFrame.getHeight();

		centerX = (int) maxX / 2;
		centerY = (int) maxY / 2;

		//And new sock

		Log.i(TAG, "Adding sock");


		if (mSock != null) { mFrame.removeView(mSock); }
		mSock = new SockView(SockActivity.this, centerX, centerY);
		mSock.addSegmentToEnd(centerX+10, centerY+10);
		mSock.addSegmentToEnd(centerX+20, centerY+20);    //TODO init possibly not in the exact center to avoid collisions?
		mFirebase.setSock(mSock.getSock());
		mFirebase.getDataAboutOtherSocks();   //This is what calls the callback serverDataAvailable


		//And start game.
		if (mLocal) {
			restart();
		}
	}


	@Override
	public void serverDataAvailable() {
		//Start server game

		Log.i(TAG, "Server game running? " + serverGameRunning);
		if (serverGameRunning == false) {

			mFrame.addView(mSock);
			mSock.bringToFront();
			restart();
			serverGameRunning = true;
		}
	}


	private void restart() {


		Log.i(TAG, "Start game");

		//reset score, remove message - //TODO doesn't go away?
		score = 0;

		mGameOver.setVisibility(View.INVISIBLE);   //fixme ?
		mGameOver.setText("");  //hacky hack. why setVis not working?

		//remove listener from TextView
		mGameOver.setOnClickListener(null); // no more restarting!

		/** remove old game components - specks, enemy socks, dryers, world... **/

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

		/* Add new game components - specks, enemys, dryers, world... */

		//Create and add new world
		mWorld = new WorldView(this, centerX, centerY, worldRadius);
		mFrame.addView(mWorld);

		//Create new specks...
		makeSpecksAddToView();

		if (mLocal) {
			createLocalAdversaries();
			updateDryers();
		}

		else {
			//Otherwise, add enemy socks
			updateEnemySocks();
		}


		updateSock();   // go!
		updateSpecks();     //shift specks so looks like background scrolls

		mSock.bringToFront();  //Move this player's sock to front

		//** Stack overflow ftw http://stackoverflow.com/questions/10845172/android-running-a-method-periodically-using-postdelayed-call */
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "TICK");

//				Log.i(TAG, "world coords xMovDist = " + xMoveDist + " ymoved "  + yMoveDist + " centerX " + centerX + " centerY " + centerY +
//					" center of world at x "  + mWorld.getCenterX() + ", y " + mWorld.getCenterY());


				if (mLocal) {
					updateSock();
					updateSpecks();
					updateDryers();
					if (!endLocalGame()) {
						handler.postDelayed(this, period); //run again!
					} else {
//						mSock = new SockView(SockActivity.this, centerX, centerY);
//						mSock.addSegmentToEnd(centerX+10, centerY+10);
//						mSock.addSegmentToEnd(centerX+20, centerY+20);    //TODO init possibly not in the exact center to avoid collisions?
					}
				}

				else {
					updateSock();
					updateSpecks();
					updateEnemySocks();
					mSock.getSock().setScore(score);
					mFirebase.sendNewStateToFirebase(mSock.getSock());
					if (!endFirebaseGame()) {
						handler.postDelayed(this, period); //run again!
					} else {
						//game over, remove self from server
						mFirebase.removeSelfFromFirebase();
//						mSock = new SockView(SockActivity.this, centerX, centerY);
//						mSock.addSegmentToEnd(centerX+10, centerY+10);
//						mSock.addSegmentToEnd(centerX+20, centerY+20);    //TODO init possibly not in the exact center to avoid collisions?
//						mFirebase.setSock(mSock.getSock());
					}
				}
			}
		}, period);

		mFrame.setOnTouchListener(mTouchListener);
	}

	private void updateEnemySocks() {
		//todo

		enemySocks = mFirebase.getEnemySocks();


		if (enemySockViews != null) {
			for (SockView sock : enemySockViews) {
				mFrame.removeView(sock);
			}
		}

		enemySockViews = new ArrayList<>();


		if (mFirebase.getEnemySocks() != null) {
			Log.i(TAG, "There are currently this many enemies" + enemySocks.size());

			for (String enemySockKey : enemySocks.keySet()) {

				Sock enemySock = enemySocks.get(enemySockKey);

				Log.i(TAG, "Enemy Sock: " + enemySockKey + " sock: "  + enemySock);

				Log.i(TAG, "Enemy sock world center is (1) " + enemySock.getWorldCenterX() + " " + enemySock.getWorldCenterY());

				SockView enemySockView = new SockView(this, enemySock);

				Log.i(TAG, "Enemy Sock View: " + enemySockView);

				enemySockView.shift(enemySock.getWorldCenterX(), enemySock.getWorldCenterY());   //TODO got to shift relative to world center??


				//BuildConfig.DEBUG


				int playerXdiff =  -mWorld.getCenterX();
				int playerYdiff =  -mWorld.getCenterY();

				Log.i(TAG, "World, player sock: x" + mSock.worldCenterX + " y " + mSock.worldCenterY + " w x " + mWorld.getCenterX() + " w y " + mWorld.getCenterY());

				Log.i(TAG, "Enemy Sock View after shift 1 : " + enemySockView);



				enemySockView.shift( playerXdiff, playerYdiff ); //keep on screen  (?)


				Log.i(TAG, "Enemy sock after shift 2 : " + enemySockView);

				//Assert.fail("Enemy socks need to update correctly");

				//The coordinates in this sock's segments are going to start at world center. So a sock needs to store the center offset.

				mFrame.addView(enemySockView);
				enemySockViews.add(enemySockView);

				 //todo update more efficiently. Making a new View for each sock is not very efficient.

			}
		}

		else {
			Log.i(TAG, "no enemy socks returned from firebase interaction");
		}

	}


	private boolean endFirebaseGame() {

		//TODO If all other socks dead OR we have died...

		mGameOver.setVisibility(TextView.VISIBLE);
		mGameOver.bringToFront();
		mGameOver.setOnClickListener(restartListener);
		String gameOverText = "";
		boolean gameOver = false;


		Log.i(TAG, "Checking if server game ended " + enemySocks);

		/* No more other socks? */

		//if (enemySocks != null ) {
			if (enemySocks.size() == 0) {

				//You win! Todo bring on dryers for dryer deathmatch
				gameOver = true;
				gameOverText = "YOU ARE THE WINNER";
				Log.i(TAG, "No more other socks");

			}
//		} else {
//			Log.i(TAG, "ENEMY SOCK LIST IS NULL");
//		}

		if (collidedWithEnemySock()) {

			gameOver = true;
			gameOverText = "YOU WERE DESTROYED BY OTHER SOCK";
			Log.i(TAG, "destroyed by other sock");
		}


		/* Fell off world? */

		int xdiff = Math.abs(mWorld.getCenterX() - (int) mSock.getHeadX());
		int ydiff = Math.abs(mWorld.getCenterY() - (int) mSock.getHeadY());

		Log.i(TAG, "world x "+ mWorld.getCenterX() + " sock x " +  mSock.getHeadX() + " xdiff = " + (mWorld.getCenterX() - mSock.getHeadX()) + " xdiff2 " + (xdiff*xdiff)
				+ " world y " +  mWorld.getCenterY() + " sock y " + mSock.getHeadY()  + "ydiff" + (mWorld.getCenterY() - mSock.getHeadY()) + " ydiff2 " + (ydiff*ydiff)
				+ " world radius " + worldRadius + " rad2 " + worldRadius*worldRadius ) ;

		if (xdiff*xdiff + ydiff*ydiff > worldRadius*worldRadius) {
			Log.i(TAG, "Sock leaves world");
			gameOverText = "YOU FELL OFF THE WORLD";
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

		return gameOver;

	}

	private boolean collidedWithEnemySock() {

		for (SockView enemySockView : enemySockViews) {

			//Check all segments
			for (SockView.Segment s : enemySockView.segments)  {
				if (intersects(s, mSock)) {
					return true;
				}
			}
		}

		return false;

	}

	private boolean endLocalGame() {

		//Check various ways the game can end

		//sock off screen? This isn't possible with the sock centered and background scrolling.

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

		Log.i(TAG, "world x "+ mWorld.getCenterX() + " sock x " +  mSock.getHeadX() + " xdiff = " + (mWorld.getCenterX() - mSock.getHeadX()) + " xdiff2 " + (xdiff*xdiff)
				+ " world y " +  mWorld.getCenterY() + " sock y " + mSock.getHeadY()  + "ydiff" + (mWorld.getCenterY() - mSock.getHeadY()) + " ydiff2 " + (ydiff*ydiff)
				+ " world radius " + worldRadius + " rad2 " + worldRadius*worldRadius ) ;


		if (!intersects(mWorld, mSock)) {
			//fell off world
			gameOverText = "YOU FELL OFF THE WORLD";
		}


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



	private int eatSpecks() {

		//which specks are 'eaten' by sock? Remove.

		int specksEaten = 0;

		for (SpeckView speck : mSpecks) {

			//under sock? remove
			if (intersects(speck, mSock)) {

				mFrame.removeView(speck);
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


	//todo test me!
	private boolean intersects(CircleView view1, CircleView view2) {

		int intersect = view1.getSize() + view2.getSize();

		int xdif = Math.abs(view1.getCircleCenterX() - view2.getCircleCenterX());
		int ydif = Math.abs(view1.getCircleCenterX() - view2.getCircleCenterY());

//		if (xdif < intersect && ydif < intersect) {
//			return true;
//		}

		if ( xdif * xdif + ydif * ydif < intersect) {
			return true;
		}

		return  false;
	}


	private boolean intersects(SockView.Segment s, SockView sock) {

		int intersect = sock.getSize();

		int xdif = Math.abs((int)sock.getHeadX() - (int)s.x);
		int ydif = Math.abs((int)sock.getHeadY() - (int)s.y);

		if (xdif < intersect && ydif < intersect) {
			return true;
		}

		return  false;
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

			mSock.addSegmentRelativeToHead(xMoveDist, yMoveDist);

			int specksEaten = eatSpecks();

			if (specksEaten == 0) {
				mSock.removeLast();
			}

			mSock.setWorldCenterX(mWorld.getCenterX());
			mSock.setWorldCenterY(mWorld.getCenterY());

			mSock.invalidate();

		Log.i(TAG, "updated sock, " + mSock);

		//Log.i(TAG, mSock.toString());

		}

	@Override
	public void onPause() {
		super.onPause();
		//remove from server
		//Stop game ! //tODO !!! Stop clock ticks
		mFirebase.removeSelfFromFirebase();
	}

}

