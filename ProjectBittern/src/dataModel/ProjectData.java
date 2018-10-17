package dataModel;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;

	public ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}

	public Video getVideo() {
		return video;
	}

	public List<AnimalTrack> getTracks() {
		return tracks;
	}

	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}
	
	//a second lies within a range of frameNumbers
	//so we need to check for points at the range of frameNumbers the inputed time represents
	public boolean containsAutoTracksAtTime(int frameNum) {
		for (AnimalTrack track : unassignedSegments) {
			int quotient = (int) (frameNum / video.getFrameRate());
			int startFrame = (int) (quotient * video.getFrameRate()) + 1;
			int endFrame = (int) ((quotient + 1) * video.getFrameRate());
			for (int i = startFrame; i <= endFrame; i++) {
				if (track.containsPointAtTime(i)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<AnimalTrack> getUnassignedSegmentsThatContainTime(int frameNum) {
		List<AnimalTrack> applicableTracks = new ArrayList<>();
		for (AnimalTrack track : unassignedSegments) {
			if (track.containsPointAtTime(frameNum)) {
				applicableTracks.add(track);
			}
		}
		return applicableTracks;
	}
	
	// go through unassigned segments, finding each time point at frameNum
	public List<TimePoint> getUnassignedTimePointsAtTime(int frameNum) {
		List<TimePoint> pointsAtTime = new ArrayList<>();
		for (AnimalTrack track : unassignedSegments) {
			for (TimePoint point : track.getPositions()) {
				if (point.sameTime(frameNum)) {
					pointsAtTime.add(point);
				}
			}
			
		}
		return pointsAtTime;
	}
		
	public AnimalTrack getAnimal(String id) {
		for (AnimalTrack animal : tracks) {
			if (animal.getID().equals(id)) {
				return animal;
			}
		}
		return null;
	}

}