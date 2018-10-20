package dataModel;

import java.util.ArrayList;
import java.util.Arrays;
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
		Collections.sort(positions);
		int index = indexOfPointAt(frameNum);
		if (index >= 0) {
			return positions.get(index);
		} else {
			return null;
		}
	}

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

	public void add(List<TimePoint> points) {
		for (TimePoint point : points) {
			add(point);
		}
	}
	
	public void add(TimePoint point) {
		Collections.sort(positions);
		if (this.containsPointAtTime(point.getFrameNum())) {
			updateTimePoint(point);
		} else {
			positions.add(point);
		}
	}
	
	public boolean containsPointAtTime(int frameNum) {
		int index = indexOfPointAt(frameNum);
		if (index >= 0) {
			return true;
		} else {
			return false;
		}
	}

	public void updateTimePoint(TimePoint newPoint) {
		positions.remove(indexOfPointAt(newPoint.getFrameNum()));
		positions.add(newPoint);
	}
	
	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size() 
			+" start=" + startFrame + " end=" + endFrame +"]" ; 
	}
	
}
