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
	
	@FXML
	public void handlePlay() {
	
	}

	
	 
}
