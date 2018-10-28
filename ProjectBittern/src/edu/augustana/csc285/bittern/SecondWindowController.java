package edu.augustana.csc285.bittern;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dataModel.AnimalTrack;
import dataModel.DataExporter;
import dataModel.ProjectData;
import dataModel.TimePoint;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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
	private GraphicsContext videoGC;
	private GraphicsContext progressGC;
	private AnimalTrack currentTrack;
	private double frameWidthRatio; 
	
	public static final Color[] TRACK_COLORS = new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
			Color.MAGENTA, Color.BLUEVIOLET, Color.ORANGE };
	
	@FXML
	public void initialize() {
		sliderBar.valueProperty().addListener((obs, oldV, newV) -> displayFrame(newV.intValue()));
		setupClick();
			
	}
	
	public void initializeWithStage(Stage stage) {
		videoGC = videoCanvas.getGraphicsContext2D();
		videoCanvas.widthProperty().bind(paneHoldingVideoCanvas.widthProperty());
		videoCanvas.heightProperty().bind(paneHoldingVideoCanvas.heightProperty());
		videoCanvas.widthProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		videoCanvas.heightProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		
		progressGC = progressCanvas.getGraphicsContext2D();
		progressCanvas.widthProperty().bind(progressCanvas.getScene().widthProperty());
		progressCanvas.widthProperty().addListener(observable -> refillProgressCanvas());
	}

	public void setup(ProjectData project) {
		try {
			this.project = project;
			project.getVideo().resetToStart();
						
			chicksBox.getItems().clear();
			for (AnimalTrack track : project.getTracks()) {
				chicksBox.getItems().add(track.getID());
			}
			
			currentTrack = project.getTracks().get(0);
			chicksBox.setValue(currentTrack.getID());
			
			sliderBar.setMax(project.getVideo().getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(project.getVideo().getFrameRate());
			
			startFrameLabel.setText("" + project.getVideo().getTime(project.getVideo().getStartFrameNum()));
			endFrameLabel.setText("" + project.getVideo().getTime(project.getVideo().getEndFrameNum()));
						
			displayFrame(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public void setupClick() {
		videoCanvas.setOnMouseClicked((event) -> {
			if (project.getVideo().getArenaBounds().contains(new Point2D(event.getX(), event.getY()))
					&& project.getVideo().timeRelativelyWithinBounds()) {
				int curFrameNum = project.getVideo().getCurrentFrameNum();
				double scalingRatio = getImageScalingRatio();
				double unscaledX = event.getX() / scalingRatio;
				double unscaledY = event.getY() / scalingRatio;
				currentTrack.add(new TimePoint(unscaledX, unscaledY, curFrameNum));
				updateProgress(curFrameNum);
				jump(project.getVideo().getStepSize());
			}
		});	

	}

	//fix
	public void refillProgressCanvas() {
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
	
	public void updateProgress(int frameNumber) {
		double startWidth = frameNumber / frameWidthRatio - frameWidthRatio; //debug for ends
		progressGC.setFill(Color.GREEN);
		progressGC.fillRect(startWidth, 0, frameWidthRatio, progressCanvas.getHeight());
	}

	public void repaintCanvas() {
		if (project != null) {
			displayFrame(project.getVideo().getCurrentFrameNum()); 
		}
	}
	
	public void displayFrame(int frameNum) {
		sliderBar.setValue(frameNum);
		project.getVideo().setCurrentFrameNum(frameNum);
		findAutoTracks();
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		double scalingRatio = getImageScalingRatio();
		videoGC.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
		videoGC.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio, curFrame.getHeight() * scalingRatio);
		drawAssignedAnimalTracks(scalingRatio, frameNum);
		
		Platform.runLater(() -> {
			currentFrameLabel.setText(project.getVideo().getTime(project.getVideo().getCurrentFrameNum()));
		});
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

	@FXML 
	public void handleAddTrack() {
		if (tracksBox.getItems().size() != 0) {					
			AnimalTrack autoTrack = tracksBox.getValue();
			currentTrack.add(autoTrack.getPositions());
			usedTracksBox.getItems().add(autoTrack);
			project.getUnassignedSegments().remove(autoTrack); //hmmn
			tracksBox.getItems().remove(autoTrack);
			refillProgressCanvas();
			tracksBox.setStyle("");
		}
	}

	@FXML
	public void handleRemoveAutoTrack() {
		if (usedTracksBox.getItems().size() != 0) {
			AnimalTrack autoTrack = usedTracksBox.getValue();
			currentTrack.remove(autoTrack.getPositions());
			usedTracksBox.getItems().remove(autoTrack);
			project.getUnassignedSegments().add(autoTrack);
			refillProgressCanvas();
			findAutoTracks();
		}
	}

	@FXML
	public void handleChicksBox() {
		if (currentTrack != null) { 
			project.addTrack(currentTrack);
		}
		currentTrack = project.getAnimalTrackInTracks((String)chicksBox.getValue()); 
		sliderBar.setValue(project.getVideo().getStartFrameNum());
		refillProgressCanvas();
	}

	@FXML
	public void handleTracksBox() {
		double scalingRatio = getImageScalingRatio();
		if (tracksBox.getValue() != null) {
			videoGC.setFill(Color.DARKBLUE);
			for (TimePoint point : tracksBox.getValue().getPositions()) {
				videoGC.fillOval(point.getX() * scalingRatio - 1, point.getY() * scalingRatio - 1, 2, 2);
			}
		}
	}
	
	private void drawAssignedAnimalTracks(double scalingRatio, int frameNum) {
		Color trackColor = TRACK_COLORS[project.getAnimalIndex(currentTrack.getID()) % TRACK_COLORS.length];
		Color trackPrevColor = trackColor.deriveColor(0, 0.5, 1.5, 1.0); // subtler variant

		videoGC.setFill(trackPrevColor);
		for (TimePoint prevPt : currentTrack.getTimePointsWithinInterval(frameNum - 90, frameNum).getPositions()) {
			videoGC.fillOval(prevPt.getX() * scalingRatio - 3, prevPt.getY() * scalingRatio - 3, 7, 7);

		}
	}

	public void findAutoTracks() {
		tracksBox.getItems().removeAll(tracksBox.getItems());
		List<AnimalTrack> relevantTracks = 
				project.getUnassignedSegmentsThatContainTime(project.getVideo().getCurrentFrameNum());
		if (relevantTracks.size() > 0) {
			tracksBox.setStyle("-fx-background-color: rgb(0,255,0)");
			for (AnimalTrack track : relevantTracks) {
				tracksBox.getItems().add(track);
			}
		} else {
				tracksBox.setStyle("");
		}
		
	}
	
	private double getImageScalingRatio() {
		double widthRatio = videoCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = videoCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
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

	public void startVideo() {
		if (project.getVideo().isOpened()) {
			Runnable frameGrabber = new Runnable() {
				@Override
				public void run() {
					sliderBar.setValue(project.getVideo().getCurrentFrameNum());
				}
			};
	
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, (int) project.getVideo().getFrameRate(),
					TimeUnit.MILLISECONDS);
	
		}
	}

	@FXML
	public void handlePrevious() {
		jump(-project.getVideo().getStepSize());
	}

	@FXML
	public void handleNext() {
		jump(project.getVideo().getStepSize());
	}

	public void jump(int stepSize) {
		double frameNum = sliderBar.getValue() + stepSize * project.getVideo().getFrameRate();
		if (frameNum < project.getVideo().getEndFrameNum() + project.getVideo().getFrameRate()) {
			displayFrame((int)frameNum);
		}
	}
	
	@FXML public void menuFileExit() {
		Platform.exit();
	}
	
	/**
	 * Save to Json?
	 */
	@FXML public void menuFileSave() {
		//save method goes here @Dakota @Evan
	}
	
	@FXML public void menuHelpAbout() {
		//Say something about our team
	}
	
	@FXML public void menuHelpInstruction() {
		Alert calibrationInstruction = new Alert(AlertType.INFORMATION);
		calibrationInstruction.setTitle("Instructions for Calibration");
		calibrationInstruction.setHeaderText(null);
		calibrationInstruction.setContentText(
				"Click and drag your mouse to draw the space that the chicks will be" + " tracked within.");
		calibrationInstruction.showAndWait();
	}
	
}
