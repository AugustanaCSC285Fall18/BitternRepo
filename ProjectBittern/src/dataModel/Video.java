package dataModel;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import edu.augustana.csc285.bittern.FirstWindowController;
import edu.augustana.csc285.bittern.SecondWindowController;



public class Video {
	
	private String filePath;
	private VideoCapture vidCap;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;
	
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private transient Point origin;
	private transient Line xAxis;
	private transient Line yAxis;
	private transient Rectangle arenaBounds; 
	private int stepSize;
	
	
		
	public Video(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
		//fill in some reasonable default/starting values for several fields
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames()-1;
		this.stepSize = 1;
		this.xPixelsPerCm = 6.5;
		this.yPixelsPerCm = 6.5;
		this.arenaBounds = new Rectangle(0,0,this.getFrameWidth(),this.getFrameHeight());
		this.origin = new Point(0,0);
		this.xAxis = new Line(this.origin.getX(), this.origin.getY(), this.getFrameWidth(), this.origin.getY());
		this.yAxis = new Line (this.origin.getX(), this.origin.getY(), this.origin.getX(), this.getFrameHeight());
		
	}
	
	/**
	 * @return the retangle arenaBounds of the video object
	 * 
	 */
	public Rectangle getArenaBounds() {
		return arenaBounds;
	}
	
	/**
	 * @return the average pixels per centimeter
	 */
	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm)/2;
	}
	

	public synchronized int getCurrentFrameNum() {
		return (int) vidCap.get(Videoio.CAP_PROP_POS_FRAMES);
	}
	

	public synchronized int getFrameWidth() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
	}

	public synchronized int getFrameHeight() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}
		
	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}
	
	public int getEndFrameNum() {
		return endFrameNum;
	}

	public String getFilePath() {
		return this.filePath;
	}

	/** 
	 * @return frames per second
	 */
	public synchronized double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
	}

	public int getStartFrameNum() {
		return startFrameNum;
	}

	public int getStepSize() {
		return stepSize;
	}

	//take out double decimals
	public String getTime(int frameNumber) {
		DecimalFormat df = new DecimalFormat("00.00");
		int seconds = (int) (frameNumber / this.getFrameRate());
		int minutes = seconds / 60;
		int remainingSeconds = (int) seconds - (60 * minutes);
		return minutes + ":" + df.format(remainingSeconds);
	}

	public synchronized int getTotalNumFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	public  double getXPixelsPerCm() {
		return xPixelsPerCm;
	}

	/**
	 * @return number of pixels per centimeters vertically
	 */
	public  double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	public void setArenaBounds(Rectangle rect) {
		this.arenaBounds = rect;
	}
	
	/**
	 * setXAxis 
	 * @param x
	 */
	public void setXAxis(Line x) {
		this.xAxis = x;
		
	}
	
	public void setYAxis(Line y) {
		this.yAxis = y ; 
	}
	
	public Line getXAxis() {
		return this.xAxis;
	}
	
	public Line getYAxis() {
		return this.yAxis;
	}


	public synchronized void setCurrentFrameNum(int currentFrameNum) {
		vidCap.set(Videoio.CAP_PROP_POS_FRAMES, currentFrameNum);
	}


	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}


	public void setEndFrameNum(int endFrameNum) {
		if (endFrameNum > startFrameNum) {
			this.endFrameNum = endFrameNum;
		} else {
			throw new IllegalArgumentException("End time cannot be less than the start time.");
		}
	}


	public void setStartFrameNum(int startFrameNum) {
		if (startFrameNum < endFrameNum) {
			this.startFrameNum = startFrameNum;
		} else {
			throw new IllegalArgumentException("The start time cannot be greater than the end time.");
		}
	}


	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
	}


	public void setXPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}


	public void setYPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}
	
	public void setOrigin(Point p) {
		this.origin.setLocation(p.getX(), p.getY());
	}
	
	public Point getOrigin() {
		return this.origin;
	}
	
	public double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}


	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}


	public synchronized boolean isOpened() {
		return vidCap.isOpened();
	}
	
	public synchronized Mat readFrame() {
		Mat frame = new Mat();
		vidCap.read(frame);
		return frame;
	}
	
	public void resetToStart() {
		this.setCurrentFrameNum(0);
	}
	
	public boolean timeWithinBounds() {
		if (this.getCurrentFrameNum() <= this.endFrameNum 
				&& this.getCurrentFrameNum() >= this.startFrameNum) {
			return true;
		}
		return false;
	}
	
	public boolean timeRelativelyWithinBounds() {
		return this.getCurrentFrameNum() - this.endFrameNum <= this.getFrameRate()
				&& this.startFrameNum - this.getCurrentFrameNum() <= this.getFrameRate();
	}

	@Override
	public String toString() {
		return "File Path: " + this.getFilePath() + "\nStart Frame Number: " + this.getStartFrameNum() 
				+ "\nEnd Frame Number: " + this.getEndFrameNum()
				+ "\nCurrent Frame Number: " + this.getCurrentFrameNum()
				+ "\nTotal Number Frames: " + this.getTotalNumFrames() 
				+ "\n";
	}
	
	
	
	/*
	 * ignore this for now, was trying something but realized that it's
	 * probably redundant, will fix later. - Dakota
	 */
	synchronized void connectVideoCapture() throws FileNotFoundException {
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
	}

}