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
				fWriter.append("\n");
				fWriter.append("Time, X Positions, Y Positions, Distance From Origin (cm)");
				fWriter.append("\n");
				fWriter.append(getResult(track, project.getVideo()));
			}
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
			TimePoint currentPoint = track.getPositions().get(i);
			double newX = currentPoint.getX() / video.getXPixelsPerCm();
			double newY = currentPoint.getY() / video.getYPixelsPerCm();
				
			newX -= origin.getX();
			newY -= origin.getY();
			calibratedTimePoints.add(new TimePoint(newX,newY,currentPoint.getFrameNum()));
		}

		return calibratedTimePoints;
	}

	
}