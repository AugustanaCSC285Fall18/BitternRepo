package dataModel;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class AnimalTrack {
	private String animalID;
	
	private List<TimePoint> positions;
	
	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}
	
	public String getID() {
		return animalID;
	}
	

	public int getSize() {
		return positions.size();
	}
	
	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size()-1);
	}

	public List<TimePoint> getPositions() {
		return this.positions;
	}

	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}

	public TimePoint getTimePointAtTime(int frameNum) {
		/*Collections.sort(positions);
		int index = indexOfPointAt(frameNum);
		if (index >= 0) {
			return positions.get(index);
		} else {
			return null;
		}*/
		
		for (TimePoint point : positions) {
			if (point.getFrameNum() == frameNum) {
				return point;
			}
		}
		return null;
	}

	//a mess
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

	//fix
	public void add(TimePoint point) {
		if (this.containsPointAtTime(point.getFrameNum()) & positions.size() != 0) {
			updateTimePoint(point);
		} else {
			positions.add(point);
		}
		Collections.sort(positions);
	}
	
	public void add(List<TimePoint> points) {
		for (TimePoint point : points) {
			add(point);
		}
	}
			
	public void remove(TimePoint point) {
		positions.remove(point);
		Collections.sort(positions);
	}
	
	public void remove(List<TimePoint> points) {
		for (TimePoint point : points) {
			remove(point);
		}
	}
	
	public boolean containsPointAtTime(int frameNum) {
		for (TimePoint position : positions) {
			if (position.atSameTime(frameNum)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param startFrameNum - the starting time (inclusive)
	 * @param endFrameNum   - the ending time (inclusive)
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
	
	public void updateTimePoint(TimePoint newPoint) {
		TimePoint oldPoint = getTimePointAtTime(newPoint.getFrameNum());
		if (oldPoint != null) {
			oldPoint.setX(newPoint.getX());
			oldPoint.setY(newPoint.getY());
		} else {
			positions.add(newPoint);
		}
	}
	
	
	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size() 
		+" start=" + startFrame + " end=" + endFrame +"]" ; 
	}

}
