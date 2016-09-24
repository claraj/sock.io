package com.clara.socksio;

import java.util.List;

/**
 * Created by admin on 9/23/16.
 */

public class Sock {

	List<SockView.Segment> segments;

	public List<SockView.Segment> getSegments() {
		return segments;
	}

	public void setSegments(List<SockView.Segment> segments) {
		this.segments = segments;
	}


	public Sock(List<SockView.Segment> segments) {
		this.segments = segments;

	}

}
