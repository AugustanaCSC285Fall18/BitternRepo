package dataModel;

//import org.opencv.core.Point;

import java.awt.Point;

public class TimePoint {
	//fields
	private Point pt;
	private int frameNum;
	
	//constructor
	public TimePoint(Point pt, int frameNum) {
		this.pt = pt;
		this.frameNum = frameNum;
	}
	

	public Point getPt() {
		return pt;
	}


	public int getFrameNum() {
		return frameNum;
	}

	
	
}
