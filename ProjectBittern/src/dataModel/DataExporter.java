package dataModel;

import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataExporter {

	public static void exportToCSV(ProjectData project ) throws IOException {
		File projectFile = new File(project.getVideo().getFilePath());
		File output = new File("output." + projectFile.getName() + ".csv");
		FileWriter fWriter = new FileWriter(output);
		
		try {
			for (AnimalTrack track : project.getTracks()) {
				fWriter.append(track.getID());
				fWriter.append("\n");
				fWriter.append(getPixelPositionsPerSecond(track, project.getVideo()));
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
	
	private static String getPixelPositionsPerSecond(AnimalTrack tracks, Video video) {
		String output = "";
		for (int i = 0; i < tracks.getSize(); i++) {
			int frameNum = tracks.getTimePointAtIndex(i).getFrameNum();
			TimePoint origin = new TimePoint(video.getOrigin().getX(), video.getOrigin().getY(), 0);
			output += "Time: " + video.getTime(frameNum) + ", Position: (" 
					+ (int) tracks.getTimePointAtIndex(i).getX() + ", " 
					+ (int) tracks.getTimePointAtIndex(i).getY() + ")\n"
					+ ", Distance to the Origin: " 
					+ (int) tracks.getTimePointAtIndex(i).getDistanceTo(origin) + " cm\n";
			
		}
		return output;
	}
	
	

	
	
}