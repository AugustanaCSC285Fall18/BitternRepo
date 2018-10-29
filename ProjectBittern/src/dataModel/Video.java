package dataModel;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * This class represents a video
 * @author Group Bittern
 *
 */
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
	
	
	/**
	 * constructs a Video object from the given file path
	 * @param filePath the file path of the video to be constructed
	 * @throws FileNotFoundException if the video file does not open
	 */
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
	

	/**
	 * 
	 * @return the video's current frame number
	 */
	public synchronized int getCurrentFrameNum() {
		return (int) vidCap.get(Videoio.CAP_PROP_POS_FRAMES);
	}
	
	/**
	 * 
	 * @return the video's frame width
	 */
	public synchronized int getFrameWidth() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
	}

	/**
	 * 
	 * @return the video's frame height
	 */
	public synchronized int getFrameHeight() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}
		
	/**
	 * 
	 * @return the frame of the video that is free of chicks/animals 
	 */
	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}
	
	/**
	 * 
	 * @return the end frame for tracking in this video
	 */
	public int getEndFrameNum() {
		return endFrameNum;
	}

	/**
	 * 
	 * @return this video's file path
	 */
	public String getFilePath() {
		return this.filePath;
	}

	/** 
	 * @return frames per second
	 */
	public synchronized double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
	}

	/**
	 * 
	 * @return this video's start frame for tracking
	 */
	public int getStartFrameNum() {
		return startFrameNum;
	}

	/**
	 * 
	 * @return the number of frames to skip when time stepping
	 */
	public int getStepSize() {
		return stepSize;
	}

	/**
	 * 
	 * @param frameNumber the given frame number
	 * @return the given frame number converted to time
	 */
	public String getTime(int frameNumber) {
		DecimalFormat df = new DecimalFormat("00.00");
		int seconds = (int) (frameNumber / this.getFrameRate());
		int minutes = seconds / 60;
		int remainingSeconds = (int) seconds - (60 * minutes);
		return minutes + ":" + df.format(remainingSeconds);
	}

	/**
	 * 
	 * @return the total number of frames within this video object
	 */
	public synchronized int getTotalNumFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	/**
	 * 
	 * @return the ratio of pixels to centimeters horizontally
	 */
	public  double getXPixelsPerCm() {
		return xPixelsPerCm;
	}

	/**
	 * @return number of pixels per centimeters vertically
	 */
	public  double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	/**
	 * sets this video's arena bounds to the given rectangle
	 * @param rect the given rectangle
	 */
	public void setArenaBounds(Rectangle rect) {
		this.arenaBounds = rect;
	}
	
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

	/**
	 * sets this video's current frame number to the given frame number
	 * @param currentFrameNum the given frame number
	 */
	public synchronized void setCurrentFrameNum(int currentFrameNum) {
		vidCap.set(Videoio.CAP_PROP_POS_FRAMES, currentFrameNum);
	}

	/**
	 * set's this video's empty frame number to the given value
	 * @param emptyFrameNum the given frame number
	 */
	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}

	/**
	 * sets this video's start frame number to the given value
	 * @param endFrameNum the given frame number
	 */
	public void setEndFrameNum(int endFrameNum) {
		if (endFrameNum > startFrameNum) {
			this.endFrameNum = endFrameNum;
		} else {
			throw new IllegalArgumentException("End time cannot be less than the start time.");
		}
	}

	/**
	 * sets this video's start frame number to the given value
	 * @param startFrameNum the given frame number
	 */
	public void setStartFrameNum(int startFrameNum) {
		if (startFrameNum < endFrameNum) {
			this.startFrameNum = startFrameNum;
		} else {
			throw new IllegalArgumentException("The start time cannot be greater than the end time.");
		}
	}

	/**
	 * sets this video's step size to the given value
	 * @param stepSize the number of frames to jump with each "step"
	 */
	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
	}

	/**
	 * sets this video's horizontal ratio of pixels to centimeters to the given ratio
	 * @param xPixelsPerCm the given ratio
	 */
	public void setXPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	/**
	 * sets this video's vertical ratio of pixels to centimeters to the given ratio
	 * @param yPixelsPerCm the given ratio
	 */
	public void setYPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}
	
	/**
	 * sets this video's origin point to the given point
	 * @param p the given point
	 */
	public void setOrigin(Point p) {
		this.origin.setLocation(p.getX(), p.getY());
	}
	
	/**
	 * 
	 * @return this video's origin point
	 */
	public Point getOrigin() {
		return this.origin;
	}
	
	private double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}


	private int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}

	/**
	 * 
	 * @return true if this video's VideoCapture object is opened
	 */
	public synchronized boolean isOpened() {
		return vidCap.isOpened();
	}
	
	/**
	 * 
	 * @return a Mat that is read by the VideoCapture object?
	 */
	public synchronized Mat readFrame() {
		Mat frame = new Mat();
		vidCap.read(frame);
		return frame;
	}
	
	/**
	 * sets this video's current frame number to zero
	 */
	public void resetToStart() {
		this.setCurrentFrameNum(0);
	}
	
	/**
	 * 
	 * @return true id this object's current frame number is relatively within
	 * the bounds of it's start and end frame numbers
	 */
	public boolean timeRelativelyWithinBounds() {
		return this.getCurrentFrameNum() - this.endFrameNum <= this.getFrameRate()
				&& this.startFrameNum - this.getCurrentFrameNum() <= this.getFrameRate();
	}

	/**
	 * returns a string representation of this object
	 */
	@Override
	public String toString() {
		return "File Path: " + this.getFilePath() + "\nStart Frame Number: " + this.getStartFrameNum() 
				+ "\nEnd Frame Number: " + this.getEndFrameNum()
				+ "\nCurrent Frame Number: " + this.getCurrentFrameNum()
				+ "\nTotal Number Frames: " + this.getTotalNumFrames() 
				+ "\n";
	}
}