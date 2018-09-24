package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	
	private VideoCapture video = new VideoCapture();

	public void handleBrowse() {
		
		File chosenFile = OpeningScreenController.getChosenFile();
		if (chosenFile != null) {
			video.open(chosenFile.getAbsolutePath());
			sliderBar.setMax(video.get(Videoio.CV_CAP_PROP_FRAME_COUNT)-1);
			displayFrame();
		} 

	}
	
	@FXML
	public void handlePlay() {
	
	}

	@FXML 
	public void handleSlider() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					video.set(Videoio.CAP_PROP_POS_FRAMES, arg2.intValue());
					displayFrame();
				}
			}
		});
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
	 
}
