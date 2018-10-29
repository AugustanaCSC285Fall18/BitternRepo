package dataModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods that analyze and export relevant tracking 
 * information from a ProjectData object
 * @author Group Bittern
 *
 */
public class DataExporter {

	/**
	 * exports the given ProjectData object's tracking data into a .csv file
	 * @param project the ProjectData object to assess
	 * @throws IOException when constructing a FileWriter if the file cannot be opened
	 */
	public static void exportToCSV(ProjectData project) throws IOException {
		File projectFile = new File(project.getVideo().getFilePath());
		File output = new File("output." + projectFile.getName() + ".csv");
		FileWriter fWriter = new FileWriter(output);
		
		try {
			for (AnimalTrack track : project.getTracks()) {
				fWriter.append(track.getID());
				fWriter.append("\nTime, X Positions, Y Positions, Distance From Origin (cm)\n");
				fWriter.append(getResult(track, project.getVideo()));
			}
			fWriter.append("\n");
			fWriter.append(getTotalDistanceOutput(project.getTracks(), project.getVideo()));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fWriter.flush();
				fWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * returns the relevant tracking information from the given AnimalTrack
	 * @param track the AnimalTrack to get assess
	 * @param video the Video object containing the calibration information
	 * @return the relevant tracking information from the given AnimalTrack
	 */
	private static String getResult(AnimalTrack track, Video video) {
		double scaledOriginX = video.getOrigin().getX()/video.getXPixelsPerCm();
		double scaledOriginY = video.getOrigin().getY()/video.getYPixelsPerCm();
		TimePoint origin = new TimePoint(scaledOriginX, scaledOriginY, 0);
		
		List<TimePoint> adjustedPositions = getCalibratedPositions(track, video, origin);
		String output = "";	
		
		for (int i = 0; i < adjustedPositions.size(); i+= video.getStepSize()) {
			TimePoint currentPoint = adjustedPositions.get(i);
			output += "Time: " + video.getTime(currentPoint.getFrameNum()) + ", "
					+ (int) currentPoint.getX() + ", " 
					+ (int) currentPoint.getY() + ", "
					+ (int) currentPoint.getDistanceTo(origin) + "\n";
		}

		return output;
	}
	
	private static String getTotalDistanceOutput(List<AnimalTrack> tracks, Video video) {
		String output = "Chick Name, Total Distance\n";
		double totalDistance = 0;
		for (AnimalTrack track : tracks) {
			List<TimePoint> convertedPositions = getConvertedPositions(track.getPositions(),video);
			totalDistance = getTotalDistance(convertedPositions, video);
			output += track.getID() + "," + (int)totalDistance + "\n";
		}
		
		return output;
	}
	
	/**
	 * adjusts the given AnimalTrack's list of TimePoints relative to the given origin point
	 * @param track the AnimalTrack object containing the list of TimePoints to be adjusted
	 * @param video the Video object containing the pixelPerCm fields
	 * @param origin the origin TimePoint
	 * @return a list of the adjusted TimePoints
	 */
	private static List<TimePoint> getCalibratedPositions(AnimalTrack track, Video video, TimePoint origin) {
		List<TimePoint> calibratedTimePoints = new ArrayList<>();
		
		for (int i = 0; i < track.getPositions().size(); i++) {
			TimePoint currentPoint = getConvertedTimePoint(track.getPositions().get(i), video);
			currentPoint.setX(currentPoint.getX() - origin.getX());
			currentPoint.setY(currentPoint.getY() - origin.getY());
			calibratedTimePoints.add(currentPoint);
		}

		return calibratedTimePoints;
	}

	private static double getTotalDistance(List<TimePoint> adjustedPositions, Video video) {
		double totalDistance = 0;
		for (int i = 1; i < adjustedPositions.size(); i++) {
			TimePoint currentPoint = adjustedPositions.get(i);
			totalDistance += currentPoint.getDistanceTo(adjustedPositions.get(i-1));
		}
		return totalDistance;
	}

	private static TimePoint getConvertedTimePoint(TimePoint point, Video video) {
		double newX = point.getX() / video.getXPixelsPerCm();
		double newY = point.getY() / video.getYPixelsPerCm();
		TimePoint convertedPoint = new TimePoint(newX, newY, point.getFrameNum());
		return convertedPoint;
	}
	
	private static List<TimePoint> getConvertedPositions(List<TimePoint> positions, Video video) {
		List<TimePoint> convertedPositions = new ArrayList<>();
		for (TimePoint point : positions) {
			convertedPositions.add(getConvertedTimePoint(point, video));
		}
		return convertedPositions;
	}

}