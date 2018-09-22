package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class SecondWindowController {
	
	@FXML private ImageView videoView;
	
	@FXML private VideoCapture vidCap = new VideoCapture();
	
	@FXML private Button setStart;
	@FXML private Button setEnd;
	@FXML private Button confirm;
	
	@FXML private TextField startTime;
	@FXML private TextField endTime;
	
	@FXML public void initialize() {	
	}
	
	
	
	
	
	
//	public void video() {
//		Mat frame = new Mat();
//		vidCap.read(frame);
//		MatOfByte buffer = new MatOfByte();
//		Imgcodecs.imencode(".png", frame, buffer);
//		Image toShow = new Image(new ByteArrayInputStream(buffer.toArray()));
//		Platform.runLater(new Runnable() {
//			@Override public void run() { 
//				videoView.setImage(toShow); }
//		});
//	}
	
	
}
