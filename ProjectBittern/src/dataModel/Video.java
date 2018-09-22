package dataModel;

//import java.awt.Rectangle;  // is this the right rectangle?
import javafx.scene.shape.Rectangle;

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
	//private double timeInSeconds;
	
	public Video(double frameRate, int totalNumFrames, String filePath) {
		
		this.frameRate = frameRate;
		this.totalNumFrames = totalNumFrames;
		this.filePath = filePath;
	//	this.timeInSeconds = totalNumFrames / frameRate;
		
	}

	public double getFrameRate() {
		return frameRate;
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

	public double getStartFrameNum() {
		return startFrameNum;
	}

	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public double getEndFrameNum() {
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
