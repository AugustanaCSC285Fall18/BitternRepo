package dataModel;

import java.awt.Point;

/**
 * This class represents a point at a frame number 
 * @author Group Bittern
 *
 */
public class TimePoint implements Comparable<TimePoint> {
	private double x;     // location
	private double y;      
	private int frameNum; // time (measured in frames)
	
	/**
	 * constructs a TimePoint object
	 * @param point the given java.awt point
	 * @param frameNum the given frame number
	 */
	public TimePoint(Point point, int frameNum) {
		this(point.getX(), point.getY(), frameNum);
	}
	
	/**
	 * constructs a TimePoint object 
	 * @param x the given x coordinate
	 * @param y the given y coordinate
	 * @param frameNum the given frame number
	 */
	public TimePoint(double x, double y, int frameNum) {
		this.x = x;
		this.y = y;
		this.frameNum = frameNum;
	}
	
	/**
	 * sets this TimePoint's x coordinate to the given value
	 * @param x the x coordinate to set this TimePoint's to
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * sets this TimePoint's y coordinate to the given value
	 * @param y the y coordinate to set this TimePoint's to
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * 
	 * @return this TimePoint's x coordinate
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * 
	 * @return this TimePoint's y coordinate
	 */
	public double getY() {
		return y;
	}

	/**
	 * 
	 * @return this TimePoint's frame number
	 */
	public int getFrameNum() {
		return frameNum;
	}

	/**
	 * 
	 * @return this TimePoint's opencv point
	 */
	public org.opencv.core.Point getPointOpenCV() {
		return new org.opencv.core.Point(x,y);
	}

	/**
	 * 
	 * @return this TimePoint's java.awt point
	 */
	public java.awt.Point getPointAWT() {
		return new java.awt.Point((int)x,(int)y);
	}

	/**
	 * returns a string representation of this TimePoint
	 */
	@Override
	public String toString() {
		return String.format("(%.1f,%.1f@T=%d)",x,y,frameNum);
	}

	/**
	 * 
	 * @param other the TimePoint to compare to
	 * @return the distance between this TimePoint and the given TimePoint
	 */
	public double getDistanceTo(TimePoint other) {
		double dx = other.x-x;
		double dy = other.y-y;
		return Math.sqrt(dx*dx+dy*dy);
	}

	/**
	 * @param other - the otherTimePoint to compare with
	 * @return the difference between this TimePoint's frame number
	 * and the given TimePoint's (negative if the other TimePoint is later)
	 */
	public int getTimeDiffAfter(TimePoint other) {
		return getTimeDiffFrom(other.getFrameNum());
	}

	/**
	 * 
	 * @param frameNum the frame number to compare to
	 * @return the difference between this TimePoint's frame number
	 * and the given frame number
	 */
	public int getTimeDiffFrom(int frameNum) {
		return this.frameNum - frameNum;
	}
	
	/**
	 * @param frameNum the frame number to compare to
	 * @return true is this TimePoint's frame number is equal to the given frame number
	 */
	public boolean isAtSameTime(int frameNum) {
		return (this.getFrameNum() == frameNum);
	}
	
	/**
	 * compares this object to the given TimePoint based on the time (frame number).
	 */
	@Override
	public int compareTo(TimePoint other) {		
		return this.getTimeDiffAfter(other);
	}

	/**
	 * returns true if the given Object is equal to this object
	 */
	public boolean equals(Object object) {
		if (object instanceof TimePoint) {
			TimePoint other = (TimePoint) object;
			return (this.x == other.getX() && this.y == other.getY()
					&& isAtSameTime(other.getFrameNum()));
		} else {
			return false;
		}
	}
}
