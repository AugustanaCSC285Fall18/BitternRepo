package dataModel;

import java.awt.Rectangle;

public class Video {
	//fields
	private double frameRate;
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private int totalNumFrames;
	private String filePath;
	private int startFrameNum;
	private int endFrameNum;
	private Rectangle arenaBounds;
	
	//constructor coming soon 
	
	
	public double getDurationInSeconds() {
		return totalNumFrames / frameRate;
	}
}
