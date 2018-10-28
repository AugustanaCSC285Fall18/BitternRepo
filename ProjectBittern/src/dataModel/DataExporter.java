package dataModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataExporter {

	public static void exportToCSV(ProjectData project ) throws IOException {
		File projectFile = new File(project.getVideo().getFilePath());
		File output = new File("output." + projectFile.getName() + ".csv");
		FileWriter fWriter = new FileWriter(output);
		
		try {
			for (AnimalTrack track : project.getTracks()) {
				fWriter.append(track.getID());
				fWriter.append("\n");
				fWriter.append(getResult(track, project.getVideo()));
			}
			System.out.println("CSV file was created successfully !!!");	
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fWriter.flush();
				fWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}
		}
	}
	
	private static String getResult(AnimalTrack tracks, Video video) {
		String output = "";	
		List<TimePoint> timePoint = ProjectData.getCalibratedPosition(tracks, video);
		for (int i = 0; i < timePoint.size(); i++) {
			int frameNum = timePoint.get(i).getFrameNum();
			output += "Time: " + video.getTime(frameNum) + ", Position in centimeters: (" 
					+ (int) timePoint.get(i).getX() + ", " 
					+ (int) timePoint.get(i).getY() + ")\n";
			
		}
		return output;
	}

	public static List<TimePoint> getCalibratedPositions(AnimalTrack track, Video video, TimePoint origin) {
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