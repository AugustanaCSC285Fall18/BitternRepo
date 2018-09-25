package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

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

	private VideoCapture video = new VideoCapture();

	private static Video chosenVideo;

	
	@FXML private ImageView myImageView;
	@FXML private Button confirmButton;
	@FXML private Button startFrameButton;
	@FXML private Button endFrameButton;
	@FXML private Slider sliderBar;
	@FXML private Label timeLabel;
	
	@FXML public void initialize() {
		File chosenFile = OpeningScreenController.getChosenFile();
		if (chosenFile != null) {

			try {
				chosenVideo = new Video(chosenFile.getAbsolutePath());
			} catch (Exception e) {
				System.out.println("Wromg file type."); //have catch be user being sent to previous screen
			}

			sliderBar.setMax(chosenVideo.getTotalNumFrames());
			chosenVideo.getVidCap().open(chosenFile.getAbsolutePath());
			sliderBar.setMax(chosenVideo.getTotalNumFrames()-1);
			sliderBar.setBlockIncrement(chosenVideo.getFrameRate());
			timeLabel.setText("0:00");
			displayFrame();
		}
		
	}
		
	@FXML public void handleSlider() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					DecimalFormat df = new DecimalFormat("00.00");
					timeLabel.setText(Double.toString(arg2.doubleValue()/chosenVideo.getFrameRate()));
					video.set(Videoio.CAP_PROP_POS_FRAMES, arg2.intValue()); // why
					double seconds = arg2.doubleValue()/chosenVideo.getFrameRate();
					int minutes = (int) seconds / 60;
					double remainingSeconds = seconds - (60 * minutes); 
					timeLabel.setText(minutes + ":" + df.format(remainingSeconds));
					chosenVideo.getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, arg2.intValue());
					displayFrame();
				}
			}
		});
	}
	
	@FXML public void handleStart() {
		chosenVideo.setStartFrameNum((int) sliderBar.getValue());
		chosenVideo.setCurrentFrameNum(chosenVideo.getStartFrameNum());
		chosenVideo.getVidCap().set(Videoio.CAP_PROP_POS_FRAMES, chosenVideo.getStartFrameNum());
	}
	
	@FXML public void handleEnd() {
		chosenVideo.setEndFrameNum((int) sliderBar.getValue());
	}
	
	@FXML public void handleConfirm() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("PlayVideoScreen.fxml"));
		AnchorPane root = (AnchorPane)loader.load();
			
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) confirmButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();		
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
	
	//https://stackoverflow.com/questions/14206768/how-to-check-if-a-string-is-numeric
	public boolean isNumerical(String strNum) {
	    try {
	        Double.parseDouble(strNum);
	    } catch (NumberFormatException e) {
	        return false;
	    }
	    return true;
	}
	
	public static Video getChosenVideo() {
		return chosenVideo;
	}

}