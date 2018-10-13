package edu.augustana.csc285.bittern;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.opencv.core.Mat;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import dataModel.AnimalTrack;
import dataModel.ProjectData;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class MainWindowController implements AutoTrackListener {

	@FXML private Button autoTrackButton;
	@FXML private Button backButton;
	@FXML private Label currentFrameLabel;
	@FXML private Button endTimeButton;
	@FXML private Label endTimeLabel;
	@FXML private Button nextButton;
	@FXML private ProgressBar progressAutoTrack;
	@FXML private Slider sliderBar;
	@FXML private Button startTimeButton;
	@FXML private Label startTimeLabel;
	@FXML private ImageView videoView; 
	
	private AutoTracker autotracker;	
	private ProjectData project;
	
	public void displayFrame() {
		if (autotracker == null || !autotracker.isRunning()) {
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoView.setImage(curFrame);
			Platform.runLater(() -> {
				currentFrameLabel.setText("" 
						+ project.getVideo().getTime(project.getVideo().getCurrentFrameNum()));
			});
		}
	}

	@FXML
	public void handleBack() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("CalibrationWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) backButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();

		CalibrationWindowController controller = loader.getController();
		controller.initializeWithStage();
		controller.setProject(project);
	}

	
	@FXML
	public void handleNext() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ManualTrackWindow.fxml"));
		BorderPane root = (BorderPane)loader.load();
		
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) nextButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();		
		
		project.getVideo().resetToStart();
		System.out.println(project.getVideo());
		ManualTrackWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.setup(project);
	}
	
	
	@FXML
	public void handleEnd() {
		project.getVideo().setEndFrameNum((int) sliderBar.getValue());
		endTimeLabel.setText("End: " + project.getVideo().getTime(project.getVideo().getEndFrameNum()));
		project.getVideo().setCurrentFrameNum(project.getVideo().getStartFrameNum());
	}

	

	@FXML
	public void handleStart() {
		project.getVideo().setStartFrameNum(project.getVideo().getCurrentFrameNum());
		startTimeLabel.setText("Start: " + project.getVideo().getTime(project.getVideo().getStartFrameNum()));
		System.out.println(project.getVideo());
	}

	@FXML
	public void handleStartAutotracking() {
		if (autotracker == null || !autotracker.isRunning()) {

			autotracker = new AutoTracker();
			autotracker.addAutoTrackListener(this);
			autotracker.startAnalysis(project.getVideo());
			autoTrackButton.setText("CANCEL auto-tracking");

		} else {
			autotracker.cancelAnalysis();
			autoTrackButton.setText("Start auto-tracking");
		}
	}

	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		Platform.runLater(() -> {
			videoView.setImage(imgFrame);
			progressAutoTrack.setProgress(fractionComplete);
			sliderBar.setValue(frameNumber);
			currentFrameLabel.setText("" + frameNumber);
		});
	}

	public void initialize() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					project.getVideo().setCurrentFrameNum(arg2.intValue());
					displayFrame();
				}
			}
		});
	}

	public void initializeWithStage(Stage stage) {
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
	}

	public void setup(ProjectData project) {
		try {
			this.project = project;
			project.getVideo().setXPixelsPerCm(6.5);
			project.getVideo().setYPixelsPerCm(6.7);

			sliderBar.setMax(project.getVideo().getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(project.getVideo().getFrameRate());

			startTimeLabel.setText("Start: " + project.getVideo().getTime(project.getVideo().getStartFrameNum()));
			endTimeLabel.setText("End: " + project.getVideo().getTime(project.getVideo().getEndFrameNum()));

			displayFrame();
			System.out.println(project.getVideo());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track : trackedSegments) {
			System.out.println(track);
		}
		
		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			autoTrackButton.setText("Start auto-tracking");
		});
		autotracker.cancelAnalysis();
	}
	

}