package dataModel;

import java.io.File;
import java.util.*;

public class ProjectData {
	
	//fields
	private List<AnimalTrack> tracks;
	private Video video;
	
	//constructor
	public ProjectData(List<AnimalTrack> tracks, Video video) {
		this.tracks = tracks;
		this.video = video;
	}
	
	public void exportCSVFile(File outFile) {
		// export file
	}
	
	public void saveProject(File projectFile) {
		//save
	}
}
