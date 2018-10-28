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
		TimePoint origin = new TimePoint(video.getOrigin().getX() / video.getXPixelsPerCm(),
				video.getOrigin().getY() / video.getYPixelsPerCm(), 0) ;
		
		List<TimePoint> positions = getCalibratedPositions(tracks, video, origin);
		for (int i = 0; i < positions.size(); i++) {
			TimePoint currentPoint = positions.get(i);
			
			output += "Time: " + video.getTime(currentPoint.getFrameNum()) 
					+ ", Position in centimeters: (" 
					+ (int) currentPoint.getX() + ", " 
					+ (int) currentPoint.getY() + ")\nDistance from origin: "
					+ currentPoint.getDistanceTo(origin) + " cm\n";
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