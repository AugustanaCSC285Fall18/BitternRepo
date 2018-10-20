package dataModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

	public AnimalTrack getAnimal(String id) {
		for (AnimalTrack animal : tracks) {
			if (animal.getID().equals(id)) {
				return animal;
			}
		}
		return null;
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
				if (point.atSameTime(frameNum)) {
					pointsAtTime.add(point);
				}		
			}			
		}
		return pointsAtTime;
	}
	
	/*public void saveToFile(File saveFile) throws FileNotFoundException {
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
		data.getVideo();
		return data;
	}*/

}