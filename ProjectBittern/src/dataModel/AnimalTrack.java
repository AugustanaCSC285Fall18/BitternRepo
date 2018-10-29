package dataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the positions at certain time of an animal, with a given name
 * @author danielleosazuwa16
 *
 */
public class AnimalTrack {
	private String animalID;
	
	private List<TimePoint> positions;
	
	/**
	 * constructs an AnimalTrack
	 * @param id the AnimalTrack's name
	 */
	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}
	
	/**
	 * 
	 * @return the AnimalTrack's name
	 */
	public String getID() {
		return animalID;
	}
	
	/**
	 * 
	 * @return the size of this AnimalTrack's list of positions
	 */
	public int getSize() {
		return positions.size();
	}
	
	/**
	 * 
	 * @return this AnimalTrack's last position
	 */
	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size()-1);
	}

	/**
	 * 
	 * @return this AnimalTrack's list of TimePoints
	 */
	public List<TimePoint> getPositions() {
		return this.positions;
	}

	/**
	 * 
	 * @param index an index in this AnimalTrack's list of positions
	 * @return this AnimalTrack's position at the given index
	 */
	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}

	/**
	 * 
	 * @param frameNum the given frame number
	 * @return this AnimalTrack's position at the given frame number, 
	 * or null if there is no such TimePoint in the list
	 */
	public TimePoint getTimePointAtTime(int frameNum) {
		for (TimePoint point : positions) {
			if (point.getFrameNum() == frameNum) {
				return point;
			}
		}
		return null;
	}

	/**
	 * searches through this AnimalTrack's list of position for a TimePoint at the given 
	 * frame number
	 * @param frameNum the given frame number
	 * @return the index of this AnimalTrack's TimePoint at the given frame number, or -1
	 * is there is no such TimePoint in the list
	 */
	public int indexOfPointAt(int frameNum) {
		int min = 0;
		int max = positions.size();
		while (min <= max) {
			int mid = (min + max) / 2;
			if (positions.get(mid).getFrameNum() < frameNum) {
				min = mid + 1;
			} else if (positions.get(mid).getFrameNum() > frameNum) {
				max = mid - 1;
			} else {
				return mid; 
			}
		}
		return -(min + 1);
	} 

	/**
	 * adds the given TimePoint to this AnimalTrack's list of positions
	 * @param point the TimePoint to add to this AnimalTrack's list of positions
	 */
	public void add(TimePoint point) {
		if (this.containsPointAtTime(point.getFrameNum()) & positions.size() != 0) {
			updateTimePoint(point);
		} else {
			positions.add(point);
		}
		Collections.sort(positions);
	}
	
	/**
	 * adds the given list of TimePoint's to this AnimalTrack's positions
	 * @param points the list of TimePoints to add to this AnimalTrack's positions
	 */
	public void add(List<TimePoint> points) {
		for (TimePoint point : points) {
			add(point);
		}
	}
			
	/**
	 * removes the given TimePoint from this AnimalTrack's positions
	 * @param point the TimePoint to remove
	 */
	public void remove(TimePoint point) {
		positions.remove(point);
		Collections.sort(positions);
	}
	
	/**
	 * removes the given list of TimePoint's from this AnimalTrack's positions
	 * @param points the given list of TimePoints to remove from this AnimalTrack's positions
	 */
	public void remove(List<TimePoint> points) {
		for (TimePoint point : points) {
			remove(point);
		}
	}
	
	/**
	 * 
	 * @param frameNum the given frame number
	 * @return true is this AnimalTrack contains a point at the given frame number
	 */
	public boolean containsPointAtTime(int frameNum) {
		for (TimePoint point : positions) {
			if (point.isAtSameTime(frameNum)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * searches this AnimalTrack for TimePoints within the given interval
	 * @param startFrameNum the starting time (inclusive)
	 * @param endFrameNum the ending time (inclusive)
	 * @return all time points in that time interval
	 */
	public AnimalTrack getTimePointsWithinInterval(int startFrameNum, int endFrameNum) {
		AnimalTrack pointsInInterval = new AnimalTrack("Points");
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() >= startFrameNum && pt.getFrameNum() <= endFrameNum) {
				pointsInInterval.add(pt);
			}
		}
		return pointsInInterval;
	}
	
	/**
	 * searches this AnimalTrack's positions for the most recent TimePoint from the given frame number
	 * @param frameNum the given frame number 
	 * @param frameRate the span to check for points
	 * @return the most recent TimePoint or null if this AnimalTrack's positions is empty
	 */
	public TimePoint getMostRecentPoint(int frameNum, double frameRate) {
		AnimalTrack pointsInInterval = getTimePointsWithinInterval((int) Math.round(frameNum - frameRate), 
				(int) Math.round(frameNum + frameRate));
		TimePoint closestPoint = null;

		if (pointsInInterval.getSize() != 0) {
			closestPoint = pointsInInterval.getTimePointAtIndex(0);
			for (TimePoint point : pointsInInterval.getPositions()) {
				if (Math.abs(point.getTimeDiffFrom(frameNum)) 
						< Math.abs(closestPoint.getTimeDiffFrom(frameNum))) {
					closestPoint = point;
				}
			}
		}

		return closestPoint; 
	}
	
	/**
	 * searches this AnimalTrack's positions for the closest TimePoint to the given TimePoint
	 * @param other the given TimePoint
	 * @return this AnimalTrack's closest TimePoint or null if this AnimalTrack's list of 
	 * positions is empty
	 */
	public TimePoint getClosestPoint(TimePoint other) {
		if (positions.size() == 0) {
			return null;
		}
		
		TimePoint closestPoint = positions.get(0);
		for (int i = 1; i < positions.size(); i++) {
			if (positions.get(i).getDistanceTo(other) < closestPoint.getDistanceTo(other)) {
				closestPoint = positions.get(i);
			}
		}	
		return closestPoint;
	}
	
	/**
	 * updates this AnimalTrack's TimePoint at the same time as the given TimePoint
	 * @param newPoint the given TimePoint
	 */
	public void updateTimePoint(TimePoint newPoint) {
		TimePoint oldPoint = getTimePointAtTime(newPoint.getFrameNum());
		if (oldPoint != null) {
			oldPoint.setX(newPoint.getX());
			oldPoint.setY(newPoint.getY());
		} else {
			positions.add(newPoint);
		}
	}
	
	/**
	 * a string representation of this AnimalTrack
	 */
	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size() 
		+" start=" + startFrame + " end=" + endFrame +"]" ; 
	}

}
