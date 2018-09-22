package dataModel;

import java.awt.Rectangle;  // is this the right rectangle?

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
	
	public Video(double frameRate, double xPixelsPerCm, double yPixelsPerCm, int totalNumFrames, String filePath,
			int startFrameNum, int endFrameNum, Rectangle arenaBounds) {
		
		this.frameRate = frameRate;
		this.xPixelsPerCm = xPixelsPerCm;
		this.yPixelsPerCm = yPixelsPerCm;
		this.totalNumFrames = totalNumFrames;
		this.filePath = filePath;
		this.startFrameNum = startFrameNum;
		this.endFrameNum = endFrameNum;
		this.arenaBounds = arenaBounds;
	}

	public double getDurationInSeconds() {
		return totalNumFrames / frameRate;
	}

	public double getxPixelsPerCm() {
		return xPixelsPerCm;
	}

	
	public double getyPixelsPerCm() {
		return yPixelsPerCm;
	}

	

	public int getTotalNumFrames() {
		return totalNumFrames;
	}

	

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getStartFrameNum() {
		return startFrameNum;
	}

	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}
}
