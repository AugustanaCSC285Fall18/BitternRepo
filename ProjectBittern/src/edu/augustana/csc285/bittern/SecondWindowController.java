package edu.augustana.csc285.bittern;

import java.io.File;
import java.io.FileNotFoundException;
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
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;

import javafx.scene.shape.Line;

import javafx.scene.shape.Rectangle;

import javafx.stage.FileChooser;

import javafx.stage.Stage;
import javafx.stage.Window;
import utils.UtilsForOpenCV;

/**
 * This class is responsible for coordinating the behaviors of the GUI controls
 * defined in "SecondWindow.fxml" to allow for manual tracking of animals in a
 * video
 * 
 * @author Group Bittern
 *
 */
public class SecondWindowController {

	@FXML
	private Pane paneHoldingVideoCanvas;
	@FXML
	private Button addTrackButton;
	@FXML
	private Button backButton;
	@FXML
	private Button exportButton;
	@FXML
	private Button nextButton;
	@FXML
	private Button playButton;
	@FXML
	private Button previousButton;
	@FXML
	private Button removeTrackButton;
	@FXML
	private Button showCurrentPathButton;
	@FXML
	private Canvas videoCanvas;
	@FXML
	private Canvas progressCanvas;
	@FXML
	private Label currentFrameLabel;
	@FXML
	private Label endFrameLabel;
	@FXML
	private Label startFrameLabel;
	@FXML
	private Slider sliderBar;
	@FXML
	private ComboBox<String> chicksBox;
	@FXML
	private ComboBox<AnimalTrack> tracksBox;
	@FXML
	private ComboBox<AnimalTrack> usedTracksBox;
	@FXML
	private MenuBar myMenuBar;

	private ProjectData project;
	private ScheduledExecutorService timer;
	private GraphicsContext videoGC;
	private GraphicsContext progressGC;
	private AnimalTrack currentTrack;
	private double frameWidthRatio;

	private Rectangle arenaBound;
	private Circle origin = new Circle();
	private Line xAxis = new Line();
	private Line yAxis = new Line();
	private File chosenFile;

	/**
	 * initializes this controller after its root element has been completely
	 * processed
	 */
	@FXML
	public void initialize() {
		sliderBar.valueProperty().addListener((obs, oldV, newV) -> displayFrame(newV.intValue()));
		setupClick();

	}

	/**
	 * sets up this class' canvases to resize whenever the scene's size is changed
	 * 
	 * @param stage the stage that holds this controller's scene
	 */
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

	/**
	 * sets up the project this controller will work with and adjusts the GUI
	 * controls to reflect the project's data
	 * 
	 * @param project the data this controller will work with
	 */
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

	/**
	 * sets up a click on the videoCanvas to add a TimePoint with the appropriate
	 * parameter to the current AnimalTrack and jump to the next second of the video
	 * with the progress canvas reflecting the added position
	 */
	private void setupClick() {
		videoCanvas.setOnMouseClicked((event) -> {
			double scalingRatio = getImageScalingRatio();
			double unscaledX = event.getX() / scalingRatio;
			double unscaledY = event.getY() / scalingRatio;
			int curFrameNum = project.getVideo().getCurrentFrameNum();

			if (project.getVideo().getArenaBounds().contains(new Point2D(unscaledX, unscaledY))
					&& project.getVideo().timeRelativelyWithinBounds()) {
				TimePoint point = new TimePoint(unscaledX, unscaledY, curFrameNum);
				currentTrack.add(point);
				updateProgress(curFrameNum);
				videoGC.setFill(Color.AQUA);
				videoGC.fillOval(event.getX() - 3, event.getY() -3 , 6, 6);
				try {
					Thread.sleep(125);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				jump(project.getVideo().getStepSize());
			}
		});

	}

	/**
	 * called when either the current AnimalTrack or the Scene's dimensions are
	 * changed, refilling the progress canvas to reflect these changes
	 */
	private void refillProgressCanvas() {
		frameWidthRatio = project.getVideo().getTotalNumFrames() / progressCanvas.getWidth();
		double startWidth = project.getVideo().getStartFrameNum() / frameWidthRatio;
		double endWidth = project.getVideo().getEndFrameNum() / frameWidthRatio;

		progressGC.setFill(Color.GRAY);
		progressGC.fillRect(0, 0, startWidth, progressCanvas.getHeight());
		progressGC.fillRect(endWidth, 0, progressCanvas.getWidth() - endWidth, progressCanvas.getHeight());

		progressGC.setFill(Color.RED);
		progressGC.fillRect(startWidth, 0, endWidth - startWidth, progressCanvas.getHeight());

		for (TimePoint position : currentTrack.getPositions()) {
			updateProgress(position.getFrameNum());
		}
	}

	/**
	 * refills the progress canvas to reflect an added TimePoint at the given
	 * frameNumber
	 * 
	 * @param frameNumber the time at which the point was added
	 */
	private void updateProgress(int frameNumber) {
		double startWidth = frameNumber / frameWidthRatio - frameWidthRatio; // debug for ends
		progressGC.setFill(Color.GREEN);
		progressGC.fillRect(startWidth, 0, frameWidthRatio, progressCanvas.getHeight());
	}

	/**
	 * called whenever the Scene's dimensions change and displays the video frame at
	 * that time
	 */
	private void repaintCanvas() {
		if (project != null) {
			displayFrame(project.getVideo().getCurrentFrameNum());
		}
	}

	/**
	 * sets the video's current frame to the given frame number, and displays the
	 * video image at that frameNumber
	 * 
	 * @param frameNum the time at which the video frame should be displayed
	 */
	private void displayFrame(int frameNum) {
		sliderBar.setValue(frameNum);
		project.getVideo().setCurrentFrameNum(frameNum);
		findAutoTracks();
		
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		double scalingRatio = getImageScalingRatio();
		videoGC.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
		videoGC.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio, curFrame.getHeight() * scalingRatio);
		
		drawCurrentPoint(scalingRatio, frameNum);
		Platform.runLater(() -> {
			currentFrameLabel.setText(project.getVideo().getTime(project.getVideo().getCurrentFrameNum()));
		});
	}

	/**
	 * loads and opens the previous window, and closes this controller's window
	 * 
	 * @throws IOException if an error occurs while loading "FirstWindow.fxml"
	 */
	@FXML
	private void handleBack() throws IOException {
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

	/**
	 * creates a .csv file with the relevant information from this project
	 * 
	 * @throws IOException if an error occurs while loading the project
	 */
	@FXML
	private void handleExport() throws IOException {
		DataExporter.exportToCSV(project);
	}

	/**
	 * adds the selected autorack's positions to the current AnimalTrack and adjust
	 * the controls to reflect this change
	 */
	@FXML
	private void handleAddAutoTrack() {
		if (tracksBox.getItems().size() != 0) {
			AnimalTrack autoTrack = tracksBox.getValue();
			currentTrack.add(autoTrack.getPositions());
			usedTracksBox.getItems().add(autoTrack);
			project.getUnassignedSegments().remove(autoTrack); // hmmn
			tracksBox.getItems().remove(autoTrack);
			refillProgressCanvas();
			tracksBox.setStyle("");
		}
	}

	/**
	 * removes the selected autotrack's position from the current AnimalTrack and
	 * adjusts the controls to reflect this change
	 */
	@FXML
	private void handleRemoveAutoTrack() {
		if (usedTracksBox.getItems().size() != 0) {
			AnimalTrack autoTrack = usedTracksBox.getValue();
			currentTrack.remove(autoTrack.getPositions());
			usedTracksBox.getItems().remove(autoTrack);
			project.getUnassignedSegments().add(autoTrack);
			refillProgressCanvas();
			findAutoTracks();
		}
	}

	/**
	 * sets the currentTrack as the ComboBox's value and adjusts the controls to
	 * reflect this change
	 */
	@FXML
	private void handleChicksBox() {
		if (currentTrack != null) {
			project.addTrack(currentTrack);
		}
		currentTrack = project.getAnimalTrackInTracks((String)chicksBox.getValue());
		sliderBar.setValue(project.getVideo().getStartFrameNum());
		refillProgressCanvas();
	}

	/**
	 * draws the selected autoTracks path on the video canvas
	 */
	@FXML
	private void handleTracksBox() {
		double scalingRatio = getImageScalingRatio();
		if (tracksBox.getValue() != null) {
			videoGC.setFill(Color.DARKBLUE);
			for (TimePoint point : tracksBox.getValue().getPositions()) {
				if (point.getFrameNum() >= project.getVideo().getCurrentFrameNum()) {
					videoGC.fillOval(point.getX() * scalingRatio - 1, point.getY() * scalingRatio - 1, 2, 2);
				}
			}
		}
	}
	
	@FXML
	private void handleShowCurrentPath() {
//		drawingGC.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
		drawTrackPath(currentTrack, (Color.color(Math.random(), Math.random(), Math.random())));
	}
	
	private void drawCurrentPoint(double scalingRatio, int frameNum) {
		TimePoint currentPoint = currentTrack.getTimePointAtTime(frameNum);
		if (currentPoint != null) {
			drawPoint(currentPoint, scalingRatio);
		}	
	}
	
	private void drawPoint(TimePoint point, double scalingRatio) {
		videoGC.setFill(Color.AQUA);
		videoGC.fillOval(point.getX() * scalingRatio - 3, 
				point.getY() * scalingRatio - 3, 7, 7);
	}
	
	private void drawTrackPath(AnimalTrack track, Color color) {
		videoGC.beginPath();
		videoGC.setLineWidth(2.0);
		videoGC.setStroke(color);
		
		for (int i = 0; i < track.getSize() - 1; i++) {
			videoGC.moveTo(track.getTimePointAtIndex(i).getX(), track.getTimePointAtIndex(i).getY());
			videoGC.lineTo(track.getTimePointAtIndex(i + 1).getX(), track.getTimePointAtIndex(i+ 1).getY());
			videoGC.stroke();
		}
	}

	/**
	 * checks for possible autoTracks at the current frame Number and when
	 * applicable, adds these tracks to the autoTrack ComboBox
	 */
	private void findAutoTracks() {
		tracksBox.getItems().removeAll(tracksBox.getItems());
		List<AnimalTrack> relevantTracks = project
				.getUnassignedSegmentsThatContainTime(project.getVideo().getCurrentFrameNum());
		if (relevantTracks.size() > 0) {
			tracksBox.setStyle("-fx-background-color: rgb(0,255,0)");
			for (AnimalTrack track : relevantTracks) {
				tracksBox.getItems().add(track);
			}
		} else {
			tracksBox.setStyle("");
		}

	}

	/**
	 * calculates the ratio of the video canvas' dimensions to the project video's
	 * dimensions
	 * 
	 * @return the ratio of the video canvas' dimensions to the project video's
	 *         dimensions
	 */
	private double getImageScalingRatio() {
		double widthRatio = videoCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = videoCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}

	/**
	 * plays or pauses the video depending on the play button's text
	 * 
	 * @throws InterruptedException thrown if timer is interrupted while awaiting
	 *                              termination
	 */
	@FXML
	private void handlePlay() throws InterruptedException {
		if (playButton.getText().equalsIgnoreCase("play")) {
			playButton.setText("Pause");
			playVideo();
		} else {
			timer.shutdown();
			timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
			playButton.setText("Play");
		}
	}

	/**
	 * plays the video from the current frame number until timer is shutdown
	 */
	private void playVideo() {
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

	/**
	 * displays the video frame at the configured "time step" before the current
	 * frame number
	 */
	@FXML
	private void handlePrevious() {
		jump(-project.getVideo().getStepSize());
	}

	/**
	 * displays the video frame at the configured "time step" after the current
	 * frame number
	 */
	@FXML
	private void handleNext() {
		jump(project.getVideo().getStepSize());
	}

	/**
	 * skips the given number of frames and displays the video at the resulting
	 * frame number
	 * 
	 * @param stepSize the number of frames (time) the video should skip through
	 */
	private void jump(int stepSize) {
		double frameNum = sliderBar.getValue() + stepSize * project.getVideo().getFrameRate();
		if (frameNum < project.getVideo().getEndFrameNum() + project.getVideo().getFrameRate()) {
			displayFrame((int) frameNum);
		}
	}

	// MENU HANDLING CODES

	/**
	 * Close the program
	 */
	@FXML
	private void menuFileExit() {
		Platform.exit();
	}

	/**
	 * Save to Json?
	 */
	@FXML
	private void menuFileSave() throws FileNotFoundException {
		File saveFile = new File(project.getVideo().getFilePath());
		File output = new File("output." + saveFile.getName() + ".txt");
		project.saveToFile(output);
	}

	/**
	 * menuFileOpen allows users to open their saved progress
	 * 
	 * @throws FileNotFoundException
	 */
	@FXML
	private void menuFileOpen() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Progress File");
		Window window = myMenuBar.getScene().getWindow();
		chosenFile = fileChooser.showOpenDialog(window);
		project = project.loadFromFile(chosenFile);

	}

	/**
	 * menuCalibrationToolShowCoordiateSystem shows coordinate system of calibration
	 * tool
	 */
	@FXML
	private void menuCalibrationToolShowCoordiateSystem() {
		origin.setCenterX(project.getVideo().getOrigin().getX() * getImageScalingRatio());
		origin.setCenterY(project.getVideo().getOrigin().getY() * getImageScalingRatio());
		origin.setRadius(5);
		origin.setStroke(Color.BLUE);
		
		xAxis.setStartX(project.getVideo().getXAxis().getStartX() * getImageScalingRatio());
		xAxis.setStartY(project.getVideo().getXAxis().getStartY() * getImageScalingRatio());
		xAxis.setEndX(project.getVideo().getXAxis().getEndX() * getImageScalingRatio());
		xAxis.setEndY(project.getVideo().getXAxis().getEndY() * getImageScalingRatio());
		
		yAxis.setStartX(project.getVideo().getYAxis().getStartX() * getImageScalingRatio());
		yAxis.setStartY(project.getVideo().getYAxis().getStartY() * getImageScalingRatio());
		yAxis.setEndX(project.getVideo().getYAxis().getEndX() * getImageScalingRatio());
		yAxis.setEndY(project.getVideo().getYAxis().getEndY() * getImageScalingRatio());
		
		xAxis.setStroke(Color.BLUE);
		xAxis.setStrokeWidth(3.0f);
		yAxis.setStroke(Color.BLUE);
		yAxis.setStrokeWidth(3.0f);

		paneHoldingVideoCanvas.getChildren().add(origin);
		paneHoldingVideoCanvas.getChildren().add(xAxis);
		paneHoldingVideoCanvas.getChildren().add(yAxis);

	}

	/**
	 * menuCalibrationToolHideCoordiateSystem hides coordinate system from this
	 * window
	 */
	@FXML
	private void menuCalibrationToolHideCoordiateSystem() {
		paneHoldingVideoCanvas.getChildren().remove(origin);
		paneHoldingVideoCanvas.getChildren().remove(yAxis);
		paneHoldingVideoCanvas.getChildren().remove(xAxis);

	}

	/**
	 * menuCalibrationToolShowArenaBound shows the arenabound of calibration tool
	 */

	@FXML
	private void menuCalibrationToolShowArenaBound() {
		arenaBound = new Rectangle((project.getVideo().getArenaBounds().getX() * getImageScalingRatio()),
				(project.getVideo().getArenaBounds().getY() * getImageScalingRatio()),
				(project.getVideo().getArenaBounds().getWidth() * getImageScalingRatio()),
				(project.getVideo().getArenaBounds().getHeight() * getImageScalingRatio()));

		arenaBound.setFill(null);
		arenaBound.setStroke(Color.RED);
		arenaBound.setStrokeWidth(5.0f);

		paneHoldingVideoCanvas.getChildren().add(arenaBound);

	}

	/**
	 * menuCalibrationToolHideArenaBound hides arenabounds from this window
	 */
	@FXML
	private void menuCalibrationToolHideArenaBound() {
		paneHoldingVideoCanvas.getChildren().remove(arenaBound);
	}

	/**
	 * Something about us
	 */
	@FXML
	private void menuHelpAbout() {
		Alert aboutUs = new Alert(AlertType.INFORMATION);
		aboutUs.setTitle("About");
		aboutUs.setHeaderText(null);
		aboutUs.setContentText("This program is designed by team Bittern ");
		aboutUs.showAndWait();
	}

	/**
	 * menuHelpInstruction pops
	 */
	@FXML
	private void menuHelpInstruction() {
		Alert calibrationInstruction = new Alert(AlertType.INFORMATION);
		calibrationInstruction.setTitle("Instructions for Calibration");
		calibrationInstruction.setHeaderText(null);
		calibrationInstruction.setContentText(
				"Click and drag your mouse to draw the space that the chicks will be" + " tracked within.");
		calibrationInstruction.showAndWait();
	}

}
