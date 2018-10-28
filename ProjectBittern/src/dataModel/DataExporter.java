package dataModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
				fWriter.append(getPositionInCM(track, project.getVideo()));
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
	
	private static String getPositionInCM(AnimalTrack tracks, Video video) {
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
	
	
	
	

	
	
}