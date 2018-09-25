package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;
import java.io.File;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class SecondWindowController {
	private VideoCapture video = new VideoCapture();
	private Video chosenVideo;
	
	@FXML private ImageView myImageView;
	@FXML private Button confirmButton;
	@FXML private Button startFrameButton;
	@FXML private Button endFrameButton;
	@FXML private Slider sliderBar;
	@FXML private Label timeLabel;
	
	@FXML public void initialize() {
		File chosenFile = OpeningScreenController.getChosenFile();
		if (chosenFile != null) {
		video.open(chosenFile.getAbsolutePath());
			try {
				chosenVideo = new Video(chosenFile.getAbsolutePath());
			} catch (Exception e) {
				System.out.println("Wromg file type."); //have catch be user being sent to previous screen
			}
			
			sliderBar.setMax(chosenVideo.getTotalNumFrames());
			sliderBar.setBlockIncrement(chosenVideo.getFrameRate());
			timeLabel.setText("0");
			displayFrame();
		}
		
	}
		
	@FXML public void handleSlider() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					timeLabel.setText(Double.toString(arg2.doubleValue()/chosenVideo.getFrameRate()));
					video.set(Videoio.CAP_PROP_POS_FRAMES, arg2.intValue());
					displayFrame();
				}
			}
		});
	}
	
	@FXML public void handleStart() {
		chosenVideo.setStartFrameNum((int) sliderBar.getValue());
	}
	
	@FXML public void handleEnd() {
		chosenVideo.setEndFrameNum((int) sliderBar.getValue());
	}
	
	@FXML public void handleConfirm() {
		
	}
	
	public void displayFrame() {
		Mat frame = new Mat();
		video.read(frame);
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
}