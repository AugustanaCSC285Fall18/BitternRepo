package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
import javafx.stage.Stage;

public class SecondWindowController {

	private Video chosenVideo;

	@FXML private ImageView myImageView;
	@FXML private Button confirmButton;
	@FXML private Button startFrameButton;
	@FXML private Button endFrameButton;
	@FXML private Slider sliderBar;
	@FXML private Label timeLabel;
	@FXML private Label startTimeLabel;
	@FXML private Label endTimeLabel;

	@FXML
	public void handleSlider() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					chosenVideo.setCurrentFrameNum(arg2.intValue());
					timeLabel.setText(chosenVideo.getTime(arg2.intValue()));
					displayFrame();
				}
			}
		});
	}

	@FXML
	public void handleStart() {
		chosenVideo.setStartFrameNum(chosenVideo.getCurrentFrameNum());
		startTimeLabel.setText("Start time: " + chosenVideo.getTime(chosenVideo.getStartFrameNum()));
		System.out.println(chosenVideo);
	}

	@FXML
	public void handleEnd() {
		chosenVideo.setEndFrameNum((int) sliderBar.getValue());
		endTimeLabel.setText("End Time: " + chosenVideo.getTime(chosenVideo.getEndFrameNum()));
	}

	@FXML
	public void handleConfirm() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("PlayVideoScreen.fxml"));
		AnchorPane root = (AnchorPane) loader.load();
		
		PlayVideoController pvc = loader.getController();
		pvc.setUpVideo(chosenVideo);
		
		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) confirmButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();
	}

	public void displayFrame() {
		Mat frame = new Mat();
		chosenVideo.readFrame(frame);
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", frame, buffer);
		Image currentFrameImage = new Image(new ByteArrayInputStream(buffer.toArray()));
		Platform.runLater(new Runnable() {
			public void run() {
				myImageView.setImage(currentFrameImage);
			}
		});
	}


	public void createVideo(String chosenFileName) {
		try {
			chosenVideo = new Video(chosenFileName);
			sliderBar.setMin(1);
			sliderBar.setMax(chosenVideo.getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(chosenVideo.getFrameRate());
			timeLabel.setText("0:00");
			startTimeLabel.setText("Start Time: " + chosenVideo.getTime(0));
			endTimeLabel.setText("End Time: " + chosenVideo.getTime(chosenVideo.getTotalNumFrames()));
			displayFrame();
			System.out.println(chosenVideo);
		} catch (Exception e) {
			System.out.println("File not found.");
		}
	}
	
}