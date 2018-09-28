package autotracking;

import java.util.List;

import org.opencv.core.Mat;

import dataModel.AnimalTrack;

public interface AutoTrackListener {

	public void handleTrackedFrame(Mat frame, int frameNumber, double percentTrackingComplete);
	public void trackingComplete(List<AnimalTrack> trackedSegments);
}
