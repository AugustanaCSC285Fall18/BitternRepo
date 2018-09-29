package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import autotracking.AutoTracker;
import dataModel.ProjectData;
import dataModel.Video;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class MainWindowController {

	@FXML private Label currentFrameLabel;
	@FXML private ImageView videoView;
	@FXML private Slider sliderBar;
	@FXML private Button startTimeButton;
	@FXML private Button endTimeButton;
	@FXML private Button playButton;
	@FXML private Button autoTrackButton;
	@FXML private ProgressBar progressAutoTrack;
	@FXML private Label startTimeLabel;
	@FXML private Label endTimeLabel;

	private Video chosenVideo;
	private ScheduledExecutorService timer;
	private AutoTracker autotracker;
	private ProjectData project;
	private Stage stage;

	public void createVideo(String chosenFileName) {
		try {
			chosenVideo = new Video(chosenFileName);

			sliderBar.setMin(chosenVideo.getStartFrameNum());
			sliderBar.setMax(chosenVideo.getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(chosenVideo.getFrameRate());

			startTimeLabel.setText("Start: " + chosenVideo.getStartFrameNum());
			endTimeLabel.setText("End: " + chosenVideo.getEndFrameNum());

			displayFrame();
			System.out.println(chosenVideo);
		} catch (Exception e) {
			System.out.println("File not found.");
		}
	}

	public void startVideo() {
		if (chosenVideo.isOpened()) {
			Runnable frameGrabber = new Runnable() {
				public void run() {
					sliderBar.setValue(chosenVideo.getCurrentFrameNum());
					displayFrame();
				}
			};

			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, (int) chosenVideo.getFrameRate(), TimeUnit.MILLISECONDS);

		}
	}

	public void displayFrame() {
		if (autotracker == null || !autotracker.isRunning()) {
			//chosenVideo.setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(chosenVideo.readFrame());
			videoView.setImage(curFrame);
			Platform.runLater(() -> {
				currentFrameLabel.setText("" + chosenVideo.getCurrentFrameNum());
			});
		}	
	}

	@FXML public void initialize() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					chosenVideo.setCurrentFrameNum(arg2.intValue());
					displayFrame(); 
				}
			}
		});
		
	}

	@FXML
	public void handleStart() {
		chosenVideo.setStartFrameNum(chosenVideo.getCurrentFrameNum());
		startTimeLabel.setText("Start: " + chosenVideo.getStartFrameNum());
		System.out.println(chosenVideo);
	}

	@FXML
	public void handleEnd() {
		chosenVideo.setEndFrameNum((int) sliderBar.getValue());
		endTimeLabel.setText("End: " + chosenVideo.getEndFrameNum());
	}


	@FXML
	public void handlePlay() throws InterruptedException {
		if (playButton.getText().equalsIgnoreCase("play video")) {
			playButton.setText("Pause Video");
			startVideo();
		} else {
			timer.shutdown();
			timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
			playButton.setText("Play Video");
		}
	}

	@FXML public void handleStartAutotracking() {

	}

}
