package com.clara.socksio;

import com.clara.socksio.actors.SockView;

import java.util.ArrayList;

/**
 *  Sock represents objects stored in Firebase.
 */

public class Sock {

	//Segments, as used by each sock to draw itself in its own world. Must be shifted to draw sock relative to other socks.
	ArrayList<SockView.Segment> segments;

	// x-coords of center of world.
	int worldCenterX;
	int worldCenterY;

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int score;
	public int size;


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


	public Sock() {}  //empty constructor needed for firebase

	public ArrayList<SockView.Segment> getSegments() {
		return segments;
	}

	public void setSegments(ArrayList<SockView.Segment> segments) {
		this.segments = segments;
	}


	public Sock(ArrayList<SockView.Segment> segments, int worldCenterX, int worldCenterY, int size) {
		this.segments = segments;
		this.worldCenterX = worldCenterX;
		this.worldCenterY = worldCenterY;
		this.size = size;


	}

	public boolean isThatUs(Sock otherSock) {

		return this.segments.equals(otherSock.segments);
	}

	@Override
	public String toString(){

		if (segments == null) {
			return "Sock with no segments";
		} else {
			return "A sock with segments: " + segments.toString() + " World center x " + worldCenterX + " , y "  + worldCenterY;
		}
	}

}
