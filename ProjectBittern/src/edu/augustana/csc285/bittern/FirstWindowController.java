package edu.augustana.csc285.bittern;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opencv.core.Mat;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import dataModel.AnimalTrack;
import dataModel.ProjectData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
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
 * defined in "FirstWindow.fxml" to allow for automatic tracking of animals in a video
 * @author Group Bittern
 *
 */
public class FirstWindowController implements AutoTrackListener {

	@FXML private Pane paneHoldingVideoCanvas;
	@FXML private Button backButton;
	@FXML private Button confirmButton;
	@FXML private Button startTimeButton;
	@FXML private Button autoTrackButton;
	@FXML private Button endTimeButton;
	@FXML private Button nextButton;
	@FXML private Button setEmptyFrameButton;
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
	@FXML private MenuBar myMenuBar;

	private AutoTracker autotracker;
	private GraphicsContext videoGC;
	private ProjectData project;
	private Rectangle mouseDragRect;
	private Point startPoint;

	private Circle origin;
	private Line xAxis;
	private Line yAxis;
	private boolean isAbleToSetArena = false;

	private boolean isAbleToSetOrigin = false;

	
	/**
	 * initializes this controller after its root element has been completely processed
	 */
	@FXML
	public void initialize() {
		nextButton.setDisable(true);
		stepBox.getItems().addAll(1, 2, 3, 4, 5);
		sliderBar.valueProperty().addListener((obs, oldV, newV) -> displayFrame(newV.intValue()));
	}

	/**
	 * sets up this class' canvases to resize whenever the scene's size is changed
	 * @param stage the stage that holds this controller's scene
	 */
	public void initializeWithStage(Stage stage) {
		videoGC = videoCanvas.getGraphicsContext2D();
		videoCanvas.widthProperty().bind(paneHoldingVideoCanvas.widthProperty());
		videoCanvas.heightProperty().bind(paneHoldingVideoCanvas.heightProperty());
		videoCanvas.widthProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		videoCanvas.heightProperty().addListener((obs, oldV, newV) -> repaintCanvas());
	}

	/**
	 * sets up the project this controller will work with and adjusts the GUI controls
	 * to reflect the project's data
	 * @param project the data this controller will work with
	 */
	public void setup(ProjectData project) {
		try {
			this.project = project;

			chicksBox.getItems().clear();
			if (project.getTracks().size() > 0) {
				nextButton.setDisable(false);
				for (AnimalTrack track : project.getTracks()) {
					chicksBox.getItems().add(track.getID());
				}
			}
			sliderBar.setMax(project.getVideo().getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(project.getVideo().getFrameRate());

			startTimeLabel.setText(project.getVideo().getTime(project.getVideo().getStartFrameNum()));
			endTimeLabel.setText(project.getVideo().getTime(project.getVideo().getEndFrameNum()));

			displayFrame(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * called whenever the Scene's dimensions change and displays the video frame at that time
	 */
	public void repaintCanvas() {
		if (project != null) {
			displayFrame((int) sliderBar.getValue());
		}
	}

	/**
	 * sets the given frame number as the video's current frame, and displays the video image 
	 * at the given frameNumber
	 * @param frameNum the time at which the video frame should be displayed
	 */
	public void displayFrame(int frameNum) {
		if (autotracker == null || !autotracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			double scalingRatio = getImageScalingRatio();
			videoGC.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
			videoGC.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio, curFrame.getHeight() * scalingRatio);
		}
		currentFrameLabel.setText(String.format("%05d", project.getVideo().getCurrentFrameNum()));
	}

	/**
	 * calculates the ratio of the video canvas' dimensions to the project video's dimensions
	 * @return the ratio of the video canvas' dimensions to the project video's dimensions
	 */
	private double getImageScalingRatio() {
		double widthRatio = videoCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = videoCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);

	}

	/**
	 * request an AnimalTrack name from the user and adds a new AnimalTrack
	 * with that name to the project's list of tracks
	 */
	@FXML
	public void handleAddChickButton() {
		String suggestedInput = "Chick #" + (chicksBox.getItems().size() + 1);
		TextInputDialog dialog = new TextInputDialog(suggestedInput);
		dialog.setTitle("Add Chick:");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter Chick Name:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			String chickName = result.get();
			project.getTracks().add(new AnimalTrack(chickName));
			chicksBox.getItems().add(chickName);
			chicksBox.getSelectionModel().select(chickName);
		}

		nextButton.setDisable(false);

	}

	/**
	 * removes AnimalTrack with name <ComboBox value> from the projects list of tracks
	 * and the chicks ComboBox
	 */
	@FXML
	public void handleRemoveChickButton() {
		String trackID = chicksBox.getValue();
		chicksBox.getItems().remove(trackID);
		project.removeTrack(trackID);
		if (chicksBox.getItems().size() == 0) {
			nextButton.setDisable(true);
		}

	}

	/**
	 * loads and opens the previous window, and closes this controller's window
	 * @throws IOException if an error occurs while loading "OpeningWindow.fxml" 
	 */
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

		OpeningWindowController controller = loader.getController();
		controller.setProject(project);
	}

	/**
	 * loads and opens the next window, and closes this controller's window
	 * @throws IOException if an error occurs while loading "SecondWindow.fxml" 
	 */
	@FXML
	public void handleNext() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("SecondWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) nextButton.getScene().getWindow();
		primary.setTitle("Manual Track Window");
		primary.setScene(nextScene);
		primary.show();

		SecondWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.setup(project);
	}

	/**
	 * sets the end time for tracking as the slider's value
	 */
	@FXML
	public void handleEnd() {
		project.getVideo().setEndFrameNum((int) sliderBar.getValue());
		endTimeLabel.setText(project.getVideo().getTime(project.getVideo().getEndFrameNum()));
	}

	/**
	 * sets the start time for tracking as the slider's value
	 */
	@FXML
	public void handleStart() {
		project.getVideo().setStartFrameNum(project.getVideo().getCurrentFrameNum());
		startTimeLabel.setText(project.getVideo().getTime(project.getVideo().getStartFrameNum()));
	}

	/**
	 * starts the autotracking process
	 * @throws InterruptedException if the ExecutorService is interrupted while waiting
	 * for termination
	 */
	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
			autotracker = new AutoTracker();
			autotracker.addAutoTrackListener(this);
			autotracker.startAnalysis(project.getVideo());
			autoTrackButton.setText("CANCEL auto-tracking");
		} else {
			autotracker.cancelAnalysis();
			autoTrackButton.setText("Start auto-tracking");
		}

		isAbleToSetOrigin = false;
		isAbleToSetArena = false;
	}

	@FXML
	public void handleSetEmptyImage() {
		project.getVideo().setEmptyFrameNum((int)sliderBar.getValue());
	}
	
	/**
	 * adjust the controls to show that the given frame has been tracked
	 * @param frame
	 * @param frameNumber the frame number that has been tracked
	 * @param fractionComplete the fraction of the time interval that has been tracked
	 */
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		Platform.runLater(() -> {
			double scalingRatio = getImageScalingRatio();
			videoGC.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
			videoGC.drawImage(imgFrame, 0, 0, imgFrame.getWidth() * scalingRatio, imgFrame.getHeight() * scalingRatio);
			progressAutoTrack.setProgress(fractionComplete);
			sliderBar.setValue(frameNumber);
			currentFrameLabel.setText(project.getVideo().getTime(frameNumber));
		});
	}

	/**
	 * adds all the autoTracks to the projects unassignedSegments array list 
	 * while adjusting the controls to show that tracking is complete
	 * @param trackedSegments the list of autotracks
	 */
	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			autoTrackButton.setText("Start auto-tracking");
		});
	}

	/**
	 * 
	 * @param event
	 */
	@FXML
	public void handleMouseDragged(MouseEvent event) {
		if (isAbleToSetArena) {
			mouseDragRect.setWidth(Math.abs(event.getX() - startPoint.getX()));
			mouseDragRect.setHeight(Math.abs(event.getY() - startPoint.getY()));
		}
	}
	
	/**
	 * 
	 * @param event
	 */
	@FXML
	public void handleMouseReleased(MouseEvent event) {
		// set arenabound of the video
		project.getVideo().setArenaBounds(new Rectangle(mouseDragRect.getX() / getImageScalingRatio(),
				mouseDragRect.getY() / getImageScalingRatio(), mouseDragRect.getWidth() / getImageScalingRatio(),
				mouseDragRect.getHeight() / getImageScalingRatio()));

	}

	/**
	 * Listen to mouse pressed event on videoCanvas
	 * @param event - mouse Press
	 */
	@FXML
	public void handleMousePressed(MouseEvent event) {
		if (isAbleToSetArena) {
			if (mouseDragRect != null) {
				paneHoldingVideoCanvas.getChildren().remove(mouseDragRect);
			}
			startPoint = new Point((int) event.getX(), (int) event.getY());
			mouseDragRect = new Rectangle(startPoint.getX(), startPoint.getY(), 1, 1);
			mouseDragRect.setFill(null);
			mouseDragRect.setStroke(Color.RED);
			mouseDragRect.setStrokeWidth(5.0f);
			// make it show up on the screen
			paneHoldingVideoCanvas.getChildren().add(mouseDragRect);

		} else if (isAbleToSetOrigin) {
			if (origin != null) {
				paneHoldingVideoCanvas.getChildren().remove(origin);
				paneHoldingVideoCanvas.getChildren().remove(xAxis);
				paneHoldingVideoCanvas.getChildren().remove(yAxis);
			}
			origin = new Circle(event.getX(), event.getY(), 5, Color.BLUE);
			setUpAxis();

			// set the origin, xAxis and yAxis of the Video
			project.getVideo().setOrigin(new Point((int) (origin.getCenterX() / getImageScalingRatio()),
					(int) (origin.getCenterY() / getImageScalingRatio())));
			project.getVideo()
					.setXAxis(new Line(xAxis.getStartX() / getImageScalingRatio(),
							xAxis.getStartY() / getImageScalingRatio(), xAxis.getEndX() / getImageScalingRatio(),
							xAxis.getEndY() / getImageScalingRatio()));
			project.getVideo()
					.setYAxis(new Line(yAxis.getStartX() / getImageScalingRatio(),
							yAxis.getStartY() / getImageScalingRatio(), yAxis.getEndX() / getImageScalingRatio(),
							yAxis.getEndY() / getImageScalingRatio()));

			// make it show up on the screen
			paneHoldingVideoCanvas.getChildren().add(origin);
			paneHoldingVideoCanvas.getChildren().add(xAxis);
			paneHoldingVideoCanvas.getChildren().add(yAxis);
		}
	}


	/**
	 * Set up coordinate system 
	 */
	public void setUpAxis() {
		xAxis = new Line(0, origin.getCenterY(), videoCanvas.getWidth(), origin.getCenterY());
		yAxis = new Line(origin.getCenterX(), 0, origin.getCenterX(), videoCanvas.getHeight());
		xAxis.setStroke(Color.BLUE);
		yAxis.setStroke(Color.BLUE);
		xAxis.setStrokeWidth(3.0f);
		yAxis.setStrokeWidth(3.0f);

	}

	/**
	 * sets the video's time step value to the ComboBox's value
	 */
	@FXML
	public void handleStepBox() {
		project.getVideo().setStepSize(stepBox.getValue());
	}

	/**
	 * askForXValue prompts users for actual horizontal length
	 */
	public void askForXValue() {
		TextInputDialog horizontalValue = new TextInputDialog("cm");
		horizontalValue.setHeaderText("Set up horizontal length");
		horizontalValue.setContentText("Please enter actual horizontal length:");
		horizontalValue.showAndWait();

		int actualLengthX = Integer.parseInt(horizontalValue.getResult());
		double pixelLength = mouseDragRect.getWidth();
		project.getVideo().setXPixelsPerCm(pixelLength / actualLengthX);
		showActualLengthX.setText("Actual Horizontal Length: " + actualLengthX + " cm");

		// remove check
		System.out.println("Pixel length X: " + pixelLength);
		System.out.println("Ratio X: " + pixelLength + "/" + actualLengthX + "=" + pixelLength / actualLengthX);
		System.out.println("Pixel per cm X: " + project.getVideo().getXPixelsPerCm());

	}

	/**
	 * askForYValue prompts users for actual vertical length
	 */
	public void askForYValue() {
		TextInputDialog verticalValue = new TextInputDialog("cm");
		verticalValue.setHeaderText("Set up vertical length");
		verticalValue.setContentText("Please enter actual vertical length:");
		verticalValue.showAndWait();

		int actualLengthY = Integer.parseInt(verticalValue.getResult());
		double pixelLength = mouseDragRect.getHeight();
		project.getVideo().setYPixelsPerCm(pixelLength / actualLengthY);
		showActualLengthY.setText("Actual Vertical Length: " + actualLengthY + " cm");

		// remove check
		System.out.println("Pixel length Y: " + pixelLength);
		System.out.println("Ratio Y: " + pixelLength + "/" + actualLengthY + "=" + pixelLength / actualLengthY);
		System.out.println("Pixel per cm Y: " + project.getVideo().getYPixelsPerCm());

	}

	// MENU EVENTS HANDLING

	// File
	/**
	 * menuFileExit closes the program
	 */
	@FXML
	public void menuFileExit() {
		Platform.exit();
	}

	/**
	 * Save the progress 
	 * @throws FileNotFoundException
	 */
	@FXML
	public void menuFileSave() throws FileNotFoundException {
		try {
			File saveFile = new File(project.getVideo().getFilePath());
			File output = new File("output." + saveFile.getName() + ".json");
			project.saveToFile(output);
			System.out.println("File was saved successfully!");
		} catch (Exception e) {
			System.out.println("File was not saved successfully!");
			e.printStackTrace();
		}

	}

	/**
	 * Open saved progresss
	 * @throws FileNotFoundException
	 */
	@FXML
	public void menuFileOpen() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Progress File");
		Window window = myMenuBar.getScene().getWindow();
		File chosenFile = fileChooser.showOpenDialog(window);
		project = project.loadFromFile(chosenFile);

	}

	/*
	 * menuCalibrationSetArenaBounds
	 */
	@FXML
	public void menuCalibrationSetArenaBounds() {
		isAbleToSetArena = true;
		isAbleToSetOrigin = false;

	}

	/**
	 * Asks users for actual length
	 */
	@FXML
	public void menuCalibrationSetActualLengths() {
		isAbleToSetArena = false;
		isAbleToSetOrigin = false;
		if (mouseDragRect != null) {
			project.getVideo().setArenaBounds(mouseDragRect);

			ArrayList<String> choices = new ArrayList();
			choices.add("Vertical");
			choices.add("Horizon");

			ChoiceDialog<String> dialog = new ChoiceDialog<>("", choices);
			dialog.setHeaderText("Set up actual length");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				if (dialog.getResult().equals("Vertical")) {
					askForYValue();
				} else if (dialog.getResult().equals("Horizon")) {
					askForXValue();
				} else {
					dialog.close();
				}
			}
		}

	}

	/**
	 * Enables setting orgin
	 */
	@FXML
	public void menuCalibrationSetOrgin() {
		isAbleToSetOrigin = true;
		isAbleToSetArena = false;
	}

	/**
	 * something about us
	 */
	@FXML
	public void menuHelpAbout() {
		Alert aboutUs = new Alert(AlertType.INFORMATION);
		aboutUs.setTitle("About");
		aboutUs.setHeaderText(null);
		aboutUs.setContentText("This program is designed by team Bittern ");
		aboutUs.showAndWait();
	}

	/**
	 * Provide instruction for Calibration
	 */
	@FXML
	public void menuHelpInstruction() {
		Alert calibrationInstruction = new Alert(AlertType.INFORMATION);
		calibrationInstruction.setTitle("Instructions for Calibration");
		calibrationInstruction.setHeaderText(null);
		calibrationInstruction.setContentText(
				"Click and drag your mouse to draw the space that the chicks will be" + " tracked within.");
		calibrationInstruction.showAndWait();
	}

}
