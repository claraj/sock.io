package com.clara.socksio;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class FirebaseInteraction {

	private static final String TAG = "FIREBASE INTERACTIONS";

	private String sockKey;

	private Sock mSock;

//	private List<Sock> mEnemySocks;

	private HashMap<String, Sock> mEnemySocks;

	private ChildEventListener mSockChildListener;
	private ValueEventListener mSockValueListener;

	private DatabaseReference mSockDatabaseRef;

	private ServerDataReadyListener dataReadyListener;

	private final String ALL_SOCKS_KEY = "all_socks";

	public FirebaseInteraction(ServerDataReadyListener listener) {

		this.dataReadyListener = listener;

		//Set up data listeners
		//TODO get locations of all other socks and their scores

		//https://firebase.google.com/docs/database/android/save-data

		mEnemySocks = new HashMap<>();

		//Log.i(TAG, "push sock?" + mSock);

		//Make a query, and give it a listener

		FirebaseAuth fbauth = FirebaseAuth.getInstance();
		Log.i("TAG", " current user : " + fbauth.getCurrentUser());   //should be null, allow anon connections
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		Log.i(TAG, " database : " + database);
		mSockDatabaseRef = database.getReference();

		Log.i(TAG, " database reference : " + mSockDatabaseRef);
	}


	public void getDataAboutOtherSocks() {

		mSockChildListener = new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				//deal with data here
				//new sock joined game (?)
				Log.i(TAG, "Child added key is " + s + " value is " + dataSnapshot.getValue());
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {
				//and here - socks moved
				Log.i(TAG, "Child changed " + s + dataSnapshot.getValue());

			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {
				//sock left game
				Log.i(TAG, "Child removed " + dataSnapshot.getValue());

			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {
				//probably don't care
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				//and should probably deal with this
				Log.i(TAG, "error " + databaseError);


			}
		};


		//Query allOtherSnakes = mSockDatabaseRef.child(ALL_SOCKS_KEY);
		//allOtherSnakes.addChildEventListener(mSockChildListener);       //do we need?


		Query allOtherSnakesData = mSockDatabaseRef.child(ALL_SOCKS_KEY);

		allOtherSnakesData.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {

				Log.i(TAG, "value event " + dataSnapshot);


				if (mEnemySocks == null) {
					mEnemySocks = new HashMap<String, Sock>();
				}

				//mEnemySocks.add(dataSnapshot.getValue(Sock.class));

				long children = dataSnapshot.getChildrenCount();

//				for (long child = 0 ; 0 < children ; child++ ) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {

					//Log.i(TAG, "DataSnapshot, child of " + ALL_SOCKS_KEY + ": "  + ds);

					Sock enemy = ds.getValue(Sock.class);   //move into if statement
					String enemyKey = ds.getKey();

					if (!enemyKey.equals(sockKey)){
						mEnemySocks.put(enemyKey, enemy);
						//Log.i(TAG, "Adding/updating enemy in enemy sock list: " + enemy);

					} else {
						//Log.i(TAG, "This is me, not adding: " + enemy);
					}

				//TODO remove dead socks

				}

				Log.i(TAG, "all other socks, notifying listener " + mEnemySocks);
				dataReadyListener.serverDataAvailable();


			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});


	//	Log.i(TAG, "push sock?" + mSock);

		//Make a query, and give it a listener



	}

	public void setSock(Sock sock) {
		mSock = sock;

		DatabaseReference ref = mSockDatabaseRef.child(ALL_SOCKS_KEY).push();
		String key = ref.getKey();
		ref.setValue(sock);   //add new sock.

		sockKey = key;
		Log.i(TAG, "Set/add sock result from fb = " + sockKey + " " + sock);

	}

	HashMap<String, Sock> getEnemySocks() {
		return mEnemySocks;
	}


	void removeSelfFromFirebase() {
		//TODO
		mSockDatabaseRef.child(sockKey).removeValue();
	}


	 void sendNewStateToFirebase(Sock sock) {

		//TODO where is this sock, and current score
		 //todo uh, have a reference to the SockActivity sock?

		 Log.i(TAG, "send new state to firebase " + sock);

		mSockDatabaseRef.child(ALL_SOCKS_KEY).child(sockKey).setValue(sock);   //todo  need to update ??

	}


	 boolean connectedToFirebase() {

		 return (mSockDatabaseRef != null) ;   //did we end up with a db reference?
//		try {
//
//			FirebaseAuth fbauth = FirebaseAuth.getInstance();
//			Log.i("TAG", " current user : " + fbauth.getCurrentUser());   //should be null, allow anon connections
//			FirebaseDatabase database = FirebaseDatabase.getInstance();
//			Log.i(TAG, " database : " + database);
//			mSockDatabaseRef = database.getReference();
//
//			Log.i(TAG, " database reference : " + mSockDatabaseRef);
//
//			//HelloSock ss = new HelloSock();
//			//mSockDatabaseRef.child("all_socks").push().setValue(ss);   //I have no idea what i'm doing
//
//			//Get all other socks
//
//			return true;
//
//		} catch (Exception e) {
//
//			Log.e(TAG, "NOT CONNECTED ", e);    //But this is ok, if we have no network connection. Just play local game.
//												//for development, need to differentiate between issue connecting to Firebase, and plain old no internet.
//			return false;
//
//			//todo presumably something will break if no connection. probably some more checks!
//			//todo this async on the server end?? Still need to do more error handling.
//
//		}
	}


	interface ServerDataReadyListener {
		void serverDataAvailable();
	}

}
