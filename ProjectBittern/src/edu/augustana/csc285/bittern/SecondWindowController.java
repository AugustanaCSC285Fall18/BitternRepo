package edu.augustana.csc285.bittern;

import java.io.ByteArrayInputStream;

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
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class SecondWindowController {
	private VideoCapture video = new VideoCapture();
	
	@FXML private ImageView myImageView;
	@FXML private Button confirmButton;
	@FXML private TextField startTimeField;
	@FXML private TextField endTimeField;
	@FXML private Slider sliderBar;
	
	@FXML public void initialize() {
		if (OpeningScreenController.getChosenFile() != null) {
			video.open(OpeningScreenController.getChosenFile().getAbsolutePath());
			sliderBar.setMax(video.get(Videoio.CV_CAP_PROP_FRAME_COUNT)-1);
			displayFrame();
		}
		
	}
		
	@FXML public void handleSlider() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					video.set(Videoio.CAP_PROP_POS_FRAMES, arg2.intValue());
					displayFrame();
				}
			}
		});
	}
	
	@FXML public void handleStart() {
		
	}
	
	@FXML public void handleEnd() {
		
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
}