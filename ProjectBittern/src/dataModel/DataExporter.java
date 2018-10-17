package dataModel;

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
				fWriter.append(getPositionsPerSecond(track, project.getVideo()));
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
	
	public static String getPositionsPerSecond(AnimalTrack tracks, Video video) {
		String output = "";
		for (int i = 0; i < tracks.getPositions().size(); i++) {
			int frameNum = tracks.getPositions().get(i).getFrameNum();
			output += "Time: " + video.getTime(frameNum) + ", Position: (" 
					+ (int) tracks.getPositions().get(i).getX() + ", " 
					+ (int) tracks.getPositions().get(i).getY() + ")\n";
		}
		return output;
	}
	
}