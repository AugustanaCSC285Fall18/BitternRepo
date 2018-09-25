package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.Videoio;

import dataModel.Video;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PlayVideoController {
 
	@FXML private ImageView myImageView;
	@FXML private Slider sliderBar;
	@FXML private Button playButton;
	@FXML private Label timeLabel;
	private Video chosenVideo = SecondWindowController.getChosenVideo();
	private ScheduledExecutorService timer;
	
	@FXML public void initialize() {
		chosenVideo.getVidCap().open(chosenVideo.getFilePath());
		displayFrame();
	}
	
	@FXML
	public void handlePlay() {
		if(playButton.getText().equalsIgnoreCase("play")) {
			playButton.setText("Pause");
			startVideo();
		} else {
			timer.shutdown();
			playButton.setText("Play");
		}
		
	}
	
	@FXML 
	public void handleSlider() {
		
	}
	
	public void startVideo() {
		
		if (chosenVideo.getVidCap().isOpened()) {
			chosenVideo.getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, chosenVideo.getCurrentFrameNum());
			Runnable frameGrabber = new Runnable() {
				public void run() {
					chosenVideo.setCurrentFrameNum((int) chosenVideo.getVidCap().get(Videoio.CAP_PROP_POS_FRAMES));
					displayFrame();
				}
			};
			
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0,(int) chosenVideo.getFrameRate(), TimeUnit.MILLISECONDS);
			
		}
	}
	
	public void displayFrame() {
		Mat frame = new Mat();
		chosenVideo.getVidCap().read(frame);
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", frame, buffer);
		Image currentFrameImage = new Image(new ByteArrayInputStream(buffer.toArray()));
		Platform.runLater(new Runnable() {
			public void run() {
				myImageView.setImage(currentFrameImage);
			}
		});
	}
	
	
}
