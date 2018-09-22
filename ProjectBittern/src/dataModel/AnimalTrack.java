package dataModel;

import java.awt.Point;
import java.util.*;

public class AnimalTrack {
	//fields
	private String animalID;
	private List<TimePoint> positions;
	
	//constructor
	public AnimalTrack(String animalID, List<TimePoint> positions) {
		this.animalID = animalID;
		this.positions = positions;
	}
	
	
	public String getAnimalID() {
		return animalID;
	}


	public Point getPositionAtFrame(int frameNum) {
		return positions.get(frameNum).getPt();
	}

	
	
	
	
	
}
