package dataModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class AnimalTrack {
	private String animalID;
	
	private List<TimePoint> positions;
	
	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}
	
	public void add(TimePoint pt) {
		positions.add(pt);
	}
	
	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}
	
	public String getID() {
		return animalID;
	}
	/**
	 * Returns the TimePoint at the specified time, or null
	 * @param frameNum
	 * @return
	 */
	
	public TimePoint getTimePointAtTime(int frameNum) {
		//TODO: This method's implementation is inefficient [linear search is O(N)]
		//      Replace this with binary search (O(log n)] or use a Map for fast access
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() == frameNum) {
				return pt;
			}
		}
		return null;
	}
	
	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size()-1);
	}
	
	//print a String of positions per time
	public String getPositions() {
		String output = "";
		for (int i = 0; i < positions.size(); i++) {
			output += "Time: " + positions.get(i).getFrameNum() + ", Position: (" 
					+ (int) positions.get(i).getX() + ", " + (int) positions.get(i).getY() + ")\n";
		}
		return output;
	}
	
	public String getPositionsBySecond(Video video) {
		String output = "";
		for (int i = 0; i < positions.size(); i++) {
			int frameNum = positions.get(i).getFrameNum();
			output += "Time: " + getTime(frameNum, video) + ", Position: (" 
					+ (int) positions.get(i).getX() + ", " + (int) positions.get(i).getY() + ")\n";
		}
		return output;
	}
	
	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size()+" start=" + startFrame + " end=" + endFrame +"]" ; 
	}
	
	public String getTime(int frameNumber, Video video) {
		DecimalFormat df = new DecimalFormat("00.00");
		int seconds = (int) (frameNumber / video.getFrameRate());
		int minutes = seconds / 60;
		int remainingSeconds = (int) seconds - (60 * minutes);
		return minutes + ":" + df.format(remainingSeconds);
	} 
	
	
}
