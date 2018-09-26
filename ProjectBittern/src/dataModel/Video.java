package dataModel;

import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {

	private String filePath;
	private VideoCapture vidCap;
	private int startFrameNum;
	private int endFrameNum;

	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds;

	public Video(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		vidCap = new VideoCapture(filePath);
		vidCap.open(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
		startFrameNum = 0;
		endFrameNum = this.getTotalNumFrames() - 1;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
	}

	public int getTotalNumFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	public int getStartFrameNum() {
		return startFrameNum;
	}

	public void setStartFrameNum(int startFrameNum) {
		if (startFrameNum < endFrameNum) {
			this.startFrameNum = startFrameNum;
		} else {
			throw new IllegalArgumentException("The start time cannot be greater than the end time.");
		}
		
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		if (endFrameNum > startFrameNum) {
			this.endFrameNum = endFrameNum;
		} else {
			throw new IllegalArgumentException("End time cannot be less than the start time.");
		}
	}

	public double getXPixelsPerCm() {
		return xPixelsPerCm;
	}

	public void setXPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	public double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	public void setYPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}
	
	public void setCurrentFrameNum(int currentFrameNum) {
		if (currentFrameNum >= 0 && currentFrameNum <= getTotalNumFrames()) {
			vidCap.set(Videoio.CAP_PROP_POS_FRAMES, currentFrameNum);
		} else {
			throw new IllegalArgumentException("Input out of range");
		}
	}
	
	public int getCurrentFrameNum() {
		return (int) vidCap.get(Videoio.CAP_PROP_POS_FRAMES);
	}

	public void resetToStart() {
		setCurrentFrameNum(0);
	}
	
	public void readFrame(Mat frame) {
		vidCap.read(frame);
	}
	
	public boolean isOpened() {
		return vidCap.isOpened();
	}
	
	@Override
	public String toString() {
		return "File Path: " + this.getFilePath() + "\nStart Frame Number: " + this.getStartFrameNum() 
				+ "\nEnd Frame Number: " + this.getEndFrameNum()
				+ "\nCurrent Frame Number: " + this.getCurrentFrameNum()
				+ "\nTotal Number Frames: " + this.getTotalNumFrames() 
				+ "\n";
	}
}
