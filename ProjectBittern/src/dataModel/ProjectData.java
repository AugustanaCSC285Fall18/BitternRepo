package dataModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
	
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class represents all the data from an animal tracking 
 * project
 * @author Group Bittern
 *
 */
public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;

	/**
	 * constructs a new ProjectData object 
	 * @param videoFilePath the file path of the video to track
	 * @throws FileNotFoundException if the video file does not load
	 */
	public ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}

	/**
	 * 
	 * @return this object's video
	 */
	public Video getVideo() {
		return video;
	}

	/**
	 * 
	 * @return this objects list of tracks (assignedTracks)
	 */
	public List<AnimalTrack> getTracks() {
		return tracks;
	}

	/**
	 * 
	 * @return this objects list of unassigned tracks
	 */
	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}

	/**
	 * searches within this object's unassignedSegments for AnimalTrack's that contain
	 * @param frameNum the given frame number
	 * @return true if this object's autoTracks (AnimalTracks with unassignedSegments) 
	 * have TimePoints at the given frame number
	 */
	public boolean containsAutoTracksAtTime(int frameNum) {
		for (AnimalTrack track : unassignedSegments) {
			if (containsPointAtTime(frameNum, track)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param frameNum the given frame number
	 * @param track the given AnimalTrack
	 * @return true if the given AnimalTrack contains TimePoints at the 
	 * given frame number
	 */
	private boolean containsPointAtTime(int frameNum, AnimalTrack track) {
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

	/**
	 * searches this object's list of unassigned segments for AnimalTracks that 
	 * contain the TimePoints at the given frame number
	 * @param frameNum the given frame number
	 * @return a list of AnimalTracks that contain TimePoints at the given time
	 */
	public List<AnimalTrack> getUnassignedSegmentsThatContainTime(int frameNum) {
		List<AnimalTrack> relevantTracks = new ArrayList<>();
		for (AnimalTrack track : unassignedSegments) {
			if (containsPointAtTime(frameNum, track)) {
				relevantTracks.add(track);
			}
		}
		return relevantTracks;
	}

	/**
	 * searches this object's tracks (assigned segments) for an AnimalTrack
	 * with the given ID
	 * @param id the given AnimalTrack ID/name
	 * @return the index of the AnimalTrack with the given ID or
	 * -1 if there is no such AnimalTrack
	 */
	public int getAnimalIndex(String id) {
		int index = 0;
		for (int i = 0; i < tracks.size(); i++) {
			if (tracks.get(i).getID().equals(id)) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * searches this object's tracks (assigned segments) for an AnimalTrack
	 * with the given ID
	 * @param id the given AnimalTrack ID/name
	 * @return the AnimalTrack with the given ID or null if there is
	 * no such AnimalTrack
	 */
	public AnimalTrack getAnimal(String id) {
		int index = getAnimalIndex(id);
		if (index >= 0) {
			return tracks.get(index);
		} 
		return null;
	}
	

	/**
	 * adds the given AnimalTrack to this object's tracks (assigned Segments)
	 * @param track the AnimalTrack to add
	 */
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

	/**
	 * removes the AnimalTrack with name <id>
	 * @param id the given AnimalTrack ID/name
	 */
	public void removeTrack(String id) {
		int index = getAnimalIndex(id);
		if (index >= 0) {
			tracks.remove(index);
		}
	}

	/**
	 * searches this object's list of autotracks and returns a list of TimePoints
	 * at the given frame number
	 * @param frameNum the given frame number
	 * @return a list of TimePoints within this object's unnassignedSegments
	 * at the given frame number
	 */
	// go through unassigned segments, finding each time point at frameNum
	public List<TimePoint> getUnassignedTimePointsAtTime(int frameNum) {
		List<TimePoint> pointsAtTime = new ArrayList<>();
		for (AnimalTrack track : unassignedSegments) {
			for (TimePoint point : track.getPositions()) {
				if (point.isAtSameTime(frameNum)) {
					pointsAtTime.add(point);
				}
			}
		}
		return pointsAtTime;
	}

	/**
	 * adds the given autotrack to this object's track with name <trackID>
	 * @param autoTrack the given autoTrack
	 * @param trackID the given AnimalTrack ID/name
	 */
	// check what happens with updatePoint when you remove added autoTracks
	public void addAutoTracks(AnimalTrack autoTrack, String trackID) {
		int index = getAnimalIndex(trackID);
		AnimalTrack track = tracks.get(index);
		track.add(autoTrack.getPositions());

		tracks.remove(track);
		tracks.add(index, track);
		unassignedSegments.remove(track);

	}

	/**
	 * removes the given autotrack from this object's track with name <trackID>
	 * @param autoTrack the given autoTrack
	 * @param trackID the given AnimalTrack ID/name
	 */
	public void removeAutoTrack(AnimalTrack autoTrack, String trackID) {
		int index = getAnimalIndex(trackID);
		AnimalTrack track = tracks.get(index);
		track.remove(autoTrack.getPositions());

		tracks.remove(track);
		tracks.add(index, track);
		unassignedSegments.add(autoTrack);

	}

	/**
	 * searches this object's unassignedSegments for the nearest AnimalTrack to the x 
	 * and y coordinates within the given time span
	 * @param x the given x coordinate
	 * @param y the given y coordinate
	 * @param startFrame the starting time (inclusive) 
	 * @param endFrame the ending time (inclusive)
	 * @return the nearest unassignedSegments' AnimalTrack to the x and y coordinates 
	 * within the given time span
	 */
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

		
	/**
	 * 
	 * @param saveFile
	 * @throws FileNotFoundException
	 */
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}
	
	/**
	 * 
	 * @return
	 */
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		return gson.toJson(this);
	}
	
	/**
	 * 
	 * @param loadFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}
	
	/**
	 * 
	 * @param jsonText
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}
	
}