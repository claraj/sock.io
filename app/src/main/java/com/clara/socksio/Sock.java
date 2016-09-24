package com.clara.socksio;

import java.util.ArrayList;

/**
 * Created by admin on 9/23/16.
 */

public class Sock {

	ArrayList<SockView.Segment> segments;

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


	public Sock() {}  //empty constructor needed?

	public ArrayList<SockView.Segment> getSegments() {
		return segments;
	}

	public void setSegments(ArrayList<SockView.Segment> segments) {
		this.segments = segments;
	}


	public Sock(ArrayList<SockView.Segment> segments, int worldCenterX, int worldCenterY) {
		this.segments = segments;
		this.worldCenterX = worldCenterX;
		this.worldCenterY = worldCenterY;


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
