package edu.augustana.csc285.bittern;

import java.awt.Point;
import java.io.IOException;
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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class FirstWindowController implements AutoTrackListener {

	@FXML private Pane paneHoldingCanvas;
	@FXML private BorderPane drawingBoard;
	@FXML private Button backButton;
	@FXML private Button confirmButton;
	@FXML private Button originButton;
	@FXML private Button setAcutalLengthButton;
	@FXML private Button startTimeButton;
	@FXML private Button instruction;
	@FXML private Button autoTrackButton;
	@FXML private Button endTimeButton;
	@FXML private Button nextButton;
	@FXML private Slider sliderBar;
	@FXML private Canvas videoCanvas;
	@FXML private ComboBox<Integer> stepBox;
	@FXML private ComboBox<String> chicksBox;
	@FXML private Label showActualLengthX;
	@FXML private Label showActualLengthY;
	@FXML private Label currentFrameLabel;
	@FXML private Label endTimeLabel;
	@FXML private Label startTimeLabel;
	@FXML private ProgressBar progressAutoTrack;
	@FXML private TextField nameField;
	
	private AutoTracker autotracker;
	private GraphicsContext videoGC;
	private ProjectData project;
	private Rectangle mouseDragRect;
	private Point startPoint;
	private Circle origin;
	private boolean isSettingOrigin = false;
	
	@FXML
	public void initialize() {
		stepBox.getItems().addAll(1, 2, 3, 4, 5);
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					project.getVideo().setCurrentFrameNum(arg2.intValue());
					displayFrame();
				}
			}
		});
	}
	
	public void initializeWithStage(Stage stage) {
		videoGC = videoCanvas.getGraphicsContext2D();
		videoCanvas.widthProperty().bind(paneHoldingCanvas.widthProperty());
		videoCanvas.heightProperty().bind(paneHoldingCanvas.heightProperty());
		videoCanvas.widthProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		videoCanvas.heightProperty().addListener((obs, oldV, newV) -> repaintCanvas());
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void repaintCanvas() {
		if (project != null) {
			displayFrame(); 
		}
	}
	
	public void displayFrame() {
		if (autotracker == null || !autotracker.isRunning()) {
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoGC.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
			double scalingRatio = getImageScalingRatio();
			videoGC.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio, curFrame.getHeight() * scalingRatio);
		}
		currentFrameLabel.setText(String.format("%05d", project.getVideo().getCurrentFrameNum()));
	}
	
	
	
	private double getImageScalingRatio() {
		double widthRatio = videoCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = videoCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}
	
	@FXML
	public void handleBack() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("OpeningWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) backButton.getScene().getWindow();
		primary.setTitle("Setup Window");
		primary.setScene(nextScene);
		primary.show();

		CalibrationWindowController controller = loader.getController();
		controller.setProject(project);
	}
	
	@FXML
	public void handleNext() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("SecondWindow.fxml"));
		BorderPane root = (BorderPane)loader.load();
		
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) nextButton.getScene().getWindow();
		primary.setTitle("Manual Track Window");
		primary.setScene(nextScene);
		primary.show();		
		
		ManualTrackWindowController controller = loader.getController();
		controller.setup(project);
		controller.initializeWithStage(primary);
		
	}
	
	@FXML
	public void handleEnd() {
		project.getVideo().setEndFrameNum((int) sliderBar.getValue());
		endTimeLabel.setText("End: " + project.getVideo().getTime(project.getVideo().getEndFrameNum()));
	}

	@FXML
	public void handleStart() {
		project.getVideo().setStartFrameNum(project.getVideo().getCurrentFrameNum());
		startTimeLabel.setText("Start: " + project.getVideo().getTime(project.getVideo().getStartFrameNum()));
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
			double scalingRatio = getImageScalingRatio();
			videoGC.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
			videoGC.drawImage(imgFrame, 0, 0, imgFrame.getWidth() * scalingRatio, imgFrame.getHeight() * scalingRatio);
			progressAutoTrack.setProgress(fractionComplete);
			sliderBar.setValue(frameNumber);
			currentFrameLabel.setText("" + project.getVideo().getTime(frameNumber));
		});
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
	}
}
