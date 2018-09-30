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
	
	//fix name
	public void processData() throws IOException {
		bWriter.write("Time, Point");
		bWriter.newLine();
		
		for (int i = 1; i < project.getTracks().size(); i++) {
			bWriter.write(project.getTracks().get(i).getPositions());
			bWriter.newLine();
		}
		
		bWriter.close();
		fWriter.close();
	}

}
