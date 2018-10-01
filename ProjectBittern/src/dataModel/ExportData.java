package dataModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportData {

	private ProjectData project;
	private FileWriter fWriter;
	private BufferedWriter bWriter;
	private File output;

	public ExportData(ProjectData project) throws IOException {
		this.project = project;
		File projectFile = new File(this.project.getVideo().getFilePath());
		this.output = new File("output." + projectFile.getName() + ".csv");
		fWriter = new FileWriter(output);
		bWriter = new BufferedWriter(fWriter);
	}

	public void processData() throws IOException {
		try {
			for (int i = 0; i < this.project.getTracks().size(); i++) {
				fWriter.append(this.project.getTracks().get(i).getID());
				fWriter.append(this.project.getTracks().get(i).getPositions());
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
	
	public void readData() {
		
	}
	
	
}
