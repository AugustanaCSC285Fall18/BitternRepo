package dataModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//import datamodel.ProjectData;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

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

	public boolean containsAutoTracksAtTime(int frameNum) {
		for (AnimalTrack track : unassignedSegments) {
			if (containsPointAtTime(frameNum, track)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsPointAtTime(int frameNum, AnimalTrack track) {
		int quotient = (int) (frameNum / video.getFrameRate());
		int startFrame = (int) (quotient * video.getFrameRate()) + 1;
		int endFrame = (int) ((quotient + 1) * video.getFrameRate());
		for (int i = startFrame; i <= endFrame; i++) {
			if (track.containsPointAtTime(i)) {
				return true;
			}
		}
		return false;
	}

	public List<AnimalTrack> getUnassignedSegmentsThatContainTime(int frameNum) {
		List<AnimalTrack> relevantTracks = new ArrayList<>();
		for (AnimalTrack track : unassignedSegments) {
			if (containsPointAtTime(frameNum, track)) {
				relevantTracks.add(track);
			}
		}
		return relevantTracks;
	}

	public int getAnimalIndex(String id) {
		int index = 0;
		for (int i = 0; i < tracks.size(); i++) {
			if (tracks.get(i).getID().equals(id)) {
				return index;
			}
		}
		return -1;
	}

	public AnimalTrack getAnimal(String id) {
		for (AnimalTrack animal : tracks) {
			if (animal.getID().equals(id)) {
				return animal;
			}
		}
		return null;
	}

	public void addTrack(AnimalTrack track) {
		int index = getAnimalIndex(track.getID());
		if (index >= 0) {
			tracks.remove(track);
			tracks.add(index, track);
			;
		} else {
			tracks.add(track);
		}
	}

	public void removeTrack(String id) {
		int index = getAnimalIndex(id);
		if (index >= 0) {
			tracks.remove(index);
		}
	}

	// go through unassigned segments, finding each time point at frameNum
	public List<TimePoint> getUnassignedTimePointsAtTime(int frameNum) {
		List<TimePoint> pointsAtTime = new ArrayList<>();
		for (AnimalTrack track : unassignedSegments) {
			for (TimePoint point : track.getPositions()) {
				if (point.atSameTime(frameNum)) {
					pointsAtTime.add(point);
				}
			}
		}
		return pointsAtTime;
	}

	// check what happens with updatePoint when you remove added autoTracks
	public void addAutoTracks(AnimalTrack autoTrack, String trackID) {
		int index = getAnimalIndex(trackID);
		AnimalTrack track = tracks.get(index);
		track.add(autoTrack.getPositions());

		tracks.remove(track);
		tracks.add(index, track);
		unassignedSegments.remove(track);

	}

	public void removeAutoTrack(AnimalTrack autoTrack, String trackID) {
		int index = getAnimalIndex(trackID);
		AnimalTrack track = tracks.get(index);
		track.remove(autoTrack.getPositions());

		tracks.remove(track);
		tracks.add(index, track);
		unassignedSegments.add(autoTrack);

	}

	public AnimalTrack getNearestUnassignedSegment(double x, double y, int startFrame, int endFrame) {
		TimePoint other = new TimePoint(x, y, 0);
		AnimalTrack closestTrack = null;
		double minDistance = Integer.MAX_VALUE;
		TimePoint closestPoint;

		for (AnimalTrack track : unassignedSegments) {
			closestPoint = track.getTimePointsWithinInterval(startFrame, endFrame).getClosestPoint(other);
			if (closestPoint != null && closestPoint.getDistanceTo(other) < minDistance) {
				closestTrack = track;
				minDistance = closestTrack.getTimePointsWithinInterval(startFrame, endFrame).getClosestPoint(other)
						.getDistanceTo(other);
			}
		}

		return closestTrack;
	}

	public static List<TimePoint> getCalibratedPosition(AnimalTrack track, Video video) {

		List<TimePoint> calibratedTimePoint = track.getPositions();
		for (int i = 0; i < track.getPositions().size(); i++) {
			if (track.getPositions().get(i).getX() > video.getOrigin().getX()
					&& track.getPositions().get(i).getX() > video.getOrigin().getX()) {
				calibratedTimePoint.get(i).setX(Math.abs(track.getPositions().get(i).getX() - video.getOrigin().getX())
						/ video.getXPixelsPerCm());
				calibratedTimePoint.get(i).setY(Math.abs(video.getOrigin().getY() - track.getPositions().get(i).getX())
						/ video.getYPixelsPerCm());
			} else if (track.getPositions().get(i).getX() > video.getOrigin().getX()
					&& track.getPositions().get(i).getY() > video.getOrigin().getY()) {
				calibratedTimePoint.get(i).setX(Math.abs(track.getPositions().get(i).getX() - video.getOrigin().getX())
						/ video.getXPixelsPerCm());
				calibratedTimePoint.get(i).setY(-Math.abs(video.getOrigin().getY() - track.getPositions().get(i).getX())
						/ video.getYPixelsPerCm());
			} else if (track.getPositions().get(i).getX() < video.getOrigin().getX()
					&& track.getPositions().get(i).getX() < video.getOrigin().getX()) {
				calibratedTimePoint.get(i).setX(-Math.abs(track.getPositions().get(i).getX() - video.getOrigin().getX())
						/ video.getXPixelsPerCm());
				calibratedTimePoint.get(i).setY(Math.abs(video.getOrigin().getY() - track.getPositions().get(i).getX())
						/ video.getYPixelsPerCm());
			} else {
				calibratedTimePoint.get(i).setX(-Math.abs(track.getPositions().get(i).getX() - video.getOrigin().getX())
						/ video.getXPixelsPerCm());
				calibratedTimePoint.get(i).setY(-Math.abs(video.getOrigin().getY() - track.getPositions().get(i).getX())
						/ video.getYPixelsPerCm());
			}

		}

		return calibratedTimePoint;
	}

	public AnimalTrack getAnimalTrackInTracks(String id) {
		for (AnimalTrack animal : tracks) {
			if (animal.getID().equals(id)) {
				return animal;
			}
		}
		return null;
	}
/**
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}

	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}

	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}

	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}
**/
}