package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import dataModel.Video;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PlayVideoController {

	@FXML
	private Label timeLabel;
	@FXML
	private AnchorPane wrapPane;
	@FXML
	private ImageView myImageView;
	@FXML
	private Slider sliderBar;
	@FXML
	private Button playButton;
	@FXML
	private Label currentTimeLabel;
	@FXML
	private Label endTimeLabel;
	@FXML
	private Button animalButton;
	@FXML
	private Button autoTrackButton;

	private Video chosenVideo;
	private ScheduledExecutorService timer;

	@FXML
	public void handlePlay() throws InterruptedException {
		if (playButton.getText().equalsIgnoreCase("play")) {
			playButton.setText("Pause");
			startVideo();
		} else {
			timer.shutdown();
			timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
			playButton.setText("Play");
		}

	}

	@FXML
	public void handleSlider() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					chosenVideo.setCurrentFrameNum(arg2.intValue());
					timer.shutdown();
					/*
					 * try { timer.awaitTermination(1000, TimeUnit.MILLISECONDS); } catch
					 * (InterruptedException e) { e.printStackTrace(); }
					 */
					displayFrame();
					System.out.println("Slider moved " + arg2);
				}
				startVideo();
			}
		});
	}

	public void startVideo() {
		if (chosenVideo.isOpened()) {
			Runnable frameGrabber = new Runnable() {
				public void run() {
					if (chosenVideo.getCurrentFrameNum() <= chosenVideo.getEndFrameNum()) {
						sliderBar.setValue(chosenVideo.getCurrentFrameNum());
						// currentTimeLabel.setText(getTime(chosenVideo.getCurrentFrameNum()));
						displayFrame();
					}
				}
			};

			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, (int) chosenVideo.getFrameRate(), TimeUnit.MILLISECONDS);

		}
	}

	public void displayFrame() {
		Mat frame = chosenVideo.readFrame();
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", frame, buffer);
		Image currentFrameImage = new Image(new ByteArrayInputStream(buffer.toArray()));
		Platform.runLater(new Runnable() {
			public void run() {
				myImageView.setImage(currentFrameImage);
			}
		});
	}

	public void setUpVideo(Video chosenVideo) {
		this.chosenVideo = chosenVideo;
		chosenVideo.setCurrentFrameNum(chosenVideo.getStartFrameNum());
		System.out.println(chosenVideo);

		sliderBar.setMin(chosenVideo.getStartFrameNum());
		sliderBar.setMax(chosenVideo.getEndFrameNum());
		sliderBar.setBlockIncrement(chosenVideo.getFrameRate());

		currentTimeLabel.setText(getTime(chosenVideo.getStartFrameNum()));
		endTimeLabel.setText(getTime(chosenVideo.getEndFrameNum()));

		displayFrame();
	}

	public String getTime(double frameNumber) {
		DecimalFormat df = new DecimalFormat("00.00");
		double seconds = frameNumber / chosenVideo.getFrameRate();
		int minutes = (int) seconds / 60;
		double remainingSeconds = seconds - (60 * minutes);
		return minutes + ":" + df.format(remainingSeconds);
	}

	@FXML
	public void handleAnimalButton() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("NewAnimalWindow.fxml"));
		AnchorPane root = (AnchorPane) loader.load();

		Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) animalButton.getScene().getWindow();
		primary.setScene(scene);
		primary.show();



	}
	

	@FXML
	public void handleAutoTrackButton() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("AutoTrackWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) autoTrackButton.getScene().getWindow();
		primary.setScene(scene);
		primary.show();

	}

}
