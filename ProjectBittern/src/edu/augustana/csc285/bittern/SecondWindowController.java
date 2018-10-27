package edu.augustana.csc285.bittern;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dataModel.AnimalTrack;
import dataModel.DataExporter;
import dataModel.ProjectData;
import dataModel.TimePoint;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class SecondWindowController {

	@FXML private Pane paneHoldingVideoCanvas;
	@FXML private Button addTrackButton;
	@FXML private Button backButton;
	@FXML private Button exportButton;
	@FXML private Button nextButton;
	@FXML private Button playButton;
	@FXML private Button previousButton;
	@FXML private Button removeTrackButton;
	@FXML private Button showCurrentPathButton;
	@FXML private Canvas videoCanvas;
	@FXML private Canvas progressCanvas;
	@FXML private Label currentFrameLabel;
	@FXML private Label endFrameLabel;
	@FXML private Label startFrameLabel;
	@FXML private Slider sliderBar;
	@FXML private ComboBox<String> chicksBox;
	@FXML private ComboBox<AnimalTrack> tracksBox;
	@FXML private ComboBox<AnimalTrack> usedTracksBox;
	
	private ProjectData project;
	private ScheduledExecutorService timer;
	private TimePoint currentTimePoint;
	private GraphicsContext videoGC;
	private GraphicsContext progressGC;
	private AnimalTrack currentTrack;
	private double frameWidthRatio; 
	
	@FXML
	public void initialize() {
		sliderBar.valueProperty().addListener((obs, oldV, newV) -> displayFrame(newV.intValue()));
		setupClick();
			
	}
	
	public void setupClick() {
		videoCanvas.setOnMouseClicked((event) -> {
			if (project.getVideo().getArenaBounds().contains(currentTimePoint.getPoint2D())
					&& project.getVideo().timeWithinBounds()) {
				
				double scalingRatio = getImageScalingRatio();
				double unscaledX = event.getX() / scalingRatio;
				double unscaledY = event.getY() / scalingRatio;
				currentTimePoint = new TimePoint(unscaledX, unscaledY, 
						project.getVideo().getCurrentFrameNum());
				currentTrack.add(currentTimePoint);
				drawPoint(currentTimePoint);
				updateProgress(project.getVideo().getCurrentFrameNum());
				jump(1);
			} 
		});	

	}
	
	public void initializeWithStage(Stage stage) {
		videoGC = videoCanvas.getGraphicsContext2D();
		videoCanvas.widthProperty().bind(paneHoldingVideoCanvas.widthProperty());
		videoCanvas.heightProperty().bind(paneHoldingVideoCanvas.heightProperty());
		videoCanvas.widthProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		videoCanvas.heightProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		
		progressGC = progressCanvas.getGraphicsContext2D();
		progressCanvas.widthProperty().bind(progressCanvas.getScene().widthProperty());
		progressCanvas.widthProperty().addListener(observable -> refillCanvas());
	}

	public void setup(ProjectData project) {
		try {
			this.project = project;
			project.getVideo().resetToStart();
			sliderBar.setMax(project.getVideo().getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(project.getVideo().getFrameRate());
			
			startFrameLabel.setText("" + project.getVideo().getTime(project.getVideo().getStartFrameNum()));
			endFrameLabel.setText("" + project.getVideo().getTime(project.getVideo().getEndFrameNum()));
			
			//remove conditional before we turn project in
			if (project.getTracks().size() > 0) {
				for (AnimalTrack track : project.getTracks()) {
					chicksBox.getItems().add(track.getID());
				}
				chicksBox.setValue(project.getTracks().get(0).getID());
				currentTrack = project.getTracks().get(0);
			} 			
			displayFrame(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public void repaintCanvas() {
		if (project != null) {
			displayFrame((int) sliderBar.getValue()); 
		}
	}

	public void displayFrame(int frameNum) {
		project.getVideo().setCurrentFrameNum(frameNum);
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		double scalingRatio = getImageScalingRatio();
		videoGC.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
		videoGC.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio, curFrame.getHeight() * scalingRatio);
		drawAssignedAnimalTracks(scalingRatio, frameNum);
		currentFrameLabel.setText(String.format("%05d", frameNum));
	}

	public void drawPoint(TimePoint point) {
		if (point != null) { //rethink this conditional
			videoGC.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
			videoGC.setFill(Color.CYAN);
			videoGC.fillOval(point.getX()-3, point.getY(), 6, 6);	
		}
	}
	
	//fix
	public void refillCanvas() {
		frameWidthRatio = project.getVideo().getTotalNumFrames() / progressCanvas.getWidth();
		double startWidth = project.getVideo().getStartFrameNum() / frameWidthRatio;
		double endWidth = project.getVideo().getEndFrameNum() / frameWidthRatio;

		progressGC.setFill(Color.GRAY);
		progressGC.fillRect(0, 0, startWidth, progressCanvas.getHeight());
		progressGC.fillRect(endWidth, 0, progressCanvas.getWidth() - endWidth, 
				progressCanvas.getHeight());
		
		progressGC.setFill(Color.RED);
		progressGC.fillRect(startWidth, 0, endWidth - startWidth,
				progressCanvas.getHeight());

		for (TimePoint position : currentTrack.getPositions()) {
			updateProgress(position.getFrameNum());
		}
	}
	
	private double getImageScalingRatio() {
		double widthRatio = videoCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = videoCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}
	
	public void findAutoTracks() {
		tracksBox.getItems().removeAll(tracksBox.getItems());
		if (project.getUnassignedSegmentsThatContainTime(project.getVideo().getCurrentFrameNum()).size() > 0) {
			tracksBox.setStyle("-fx-background-color: rgb(0,255,0)");
		} else {
			tracksBox.setStyle("-fx-background-color: lightgrey");
		}
		for (AnimalTrack track : project.getUnassignedSegmentsThatContainTime(project.getVideo().getCurrentFrameNum())) {
			tracksBox.getItems().add(track);
		}
		tracksBox.setPromptText("Auto Tracks");
	}
	
	@FXML 
	public void handleAddTrack() {
		if (tracksBox.getItems().size() != 0) {
			project.addAutoTracks(tracksBox.getValue(), currentTrack.getID());
			usedTracksBox.getItems().add(tracksBox.getValue());
			tracksBox.getItems().removeAll(tracksBox.getItems());
			tracksBox.setStyle("-fx-background-color: lightgrey");
			tracksBox.setPromptText("AutoTracks");
		}
	}
	
	@FXML
	public void handleRemoveTrack() {
		if (usedTracksBox.getItems().size() != 0) {
			project.removeAutoTrack(usedTracksBox.getValue(), currentTrack.getID());
			usedTracksBox.getItems().remove(usedTracksBox.getValue());
			usedTracksBox.setPromptText("Used Tracks");
		}
	}

	@FXML
	public void handleTracksBox() {
		if (tracksBox.getValue() != null) {
			videoGC.setFill(Color.color(Math.random(), Math.random(), Math.random()));
			for (TimePoint point : tracksBox.getValue().getPositions()) {
				videoGC.fillOval(point.getX() - 3, point.getY() - 3, 6, 6);
			}
		}
	}
	
	@FXML
	public void handleChicksBox() {
		if (currentTrack != null) { //rethink using this conditional
			project.addTrack(currentTrack);
		}
		currentTrack = project.getTracks().get(project.getAnimalIndex(chicksBox.getValue()));
		sliderBar.setValue(project.getVideo().getStartFrameNum());
		refillCanvas();
	}	

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
	public void handleNext() {
		jump(project.getVideo().getStepSize());
	}
	
	@FXML
	public void handlePrevious() {
		jump(-project.getVideo().getStepSize());
	}
	
	public void jump(int stepSize) {
		double frameNum = project.getVideo().getCurrentFrameNum() 
				+ stepSize * project.getVideo().getFrameRate();
		if (frameNum < project.getVideo().getEndFrameNum()) {
			project.getVideo().setCurrentFrameNum((int)frameNum);
			displayFrame((int)frameNum);
		}
	}
	
	public void updateProgress(int frameNumber) {
		double startWidth = frameNumber / frameWidthRatio - frameWidthRatio; //debug for ends
		progressGC.setFill(Color.GREEN);
		progressGC.fillRect(startWidth, 0, frameWidthRatio, progressCanvas.getHeight());
	}
	
	public void startVideo() {
		if (project.getVideo().isOpened()) {
			Runnable frameGrabber = new Runnable() {
				@Override
				public void run() {
					sliderBar.setValue(project.getVideo().getCurrentFrameNum());
					displayFrame((int)sliderBar.getValue());
				}
			};

			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, (int) project.getVideo().getFrameRate(),
					TimeUnit.MILLISECONDS);

		}
	}
	
	@FXML
	public void handleBack() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("FirstWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) backButton.getScene().getWindow();
		primary.setTitle("Setup Window");
		primary.setScene(nextScene);
		primary.show();

		FirstWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.setup(project);
	}
	
	@FXML
	public void handleExport() throws IOException {
		DataExporter.exportToCSV(project);
	}
	
	private void drawAssignedAnimalTracks(double scalingRatio, int frameNum) {
		for (int i = 0; i < project.getTracks().size(); i++) {
			AnimalTrack track = project.getTracks().get(i);
			Color trackColor = Color.color(Math.random(), Math.random(), Math.random());
			Color trackPrevColor = trackColor.deriveColor(0, 0.5, 1.5, 1.0); // subtler variant
			videoGC.setFill(trackPrevColor);
			for (TimePoint prevPt : track.getTimePointsWithinInterval(frameNum - 90, frameNum).getPositions()) {
				videoGC.fillOval(prevPt.getX() * scalingRatio - 3, prevPt.getY() * scalingRatio - 3, 7, 7);
			}
			TimePoint currPt = track.getTimePointAtTime(frameNum);
			if (currPt != null) {
				videoGC.setFill(trackColor);
				videoGC.fillOval(currPt.getX() * scalingRatio - 7, currPt.getY() * scalingRatio - 7, 15, 15);
			}
		}
	}
	
}
