package dataModel;

import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {

	private String filePath;
	private VideoCapture video;
	private int startFrameNum;
	private int endFrameNum;

	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds;

	public Video(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		this.video = new VideoCapture(filePath);
		video.open(filePath);
		if (!video.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
	}

	public String getFilePath() {
		return this.filePath;
	}

	public double getFrameRate() {
		return video.get(Videoio.CAP_PROP_FPS);
	}

	public int getTotalNumFrames() {
		return (int) video.get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	public VideoCapture getVideo() {
		return this.video;
	}

	public int getStartFrameNum() {
		return startFrameNum;
	}

	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public double getXPixelsPerCm() {
		return xPixelsPerCm;
	}

	public void setXPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	public double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	public void setYPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}
	
	/*public String toString() {
		System.out.println();
	}*/

}
