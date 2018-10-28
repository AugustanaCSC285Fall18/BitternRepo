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
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class FirstWindowController implements AutoTrackListener {

	@FXML
	private Pane paneHoldingVideoCanvas;
	@FXML
	private Button backButton;
	@FXML
	private Button confirmButton;
	@FXML
	private Button startTimeButton;
	@FXML
	private Button autoTrackButton;
	@FXML
	private Button endTimeButton;
	@FXML
	private Button nextButton;
	@FXML
	private Slider sliderBar;
	@FXML
	private Canvas videoCanvas;
	@FXML
	private ComboBox<Integer> stepBox;
	@FXML
	private ComboBox<String> chicksBox;
	@FXML
	private Label showActualLengthX;
	@FXML
	private Label showActualLengthY;
	@FXML
	private Label currentFrameLabel;
	@FXML
	private Label endTimeLabel;
	@FXML
	private Label startTimeLabel;
	@FXML
	private ProgressBar progressAutoTrack;
	@FXML
	private TextField nameField;

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

	@FXML
	public void initialize() {
		nextButton.setDisable(true);
		stepBox.getItems().addAll(1, 2, 3, 4, 5);
		sliderBar.valueProperty().addListener((obs, oldV, newV) -> displayFrame(newV.intValue()));
		System.out.println(videoCanvas.getHeight() + " " + videoCanvas.getWidth() + " " + videoCanvas.getLayoutX() + " "
				+ videoCanvas.getLayoutY());
		System.out.println(paneHoldingVideoCanvas.getHeight() + " " + paneHoldingVideoCanvas.getWidth() + " "
				+ paneHoldingVideoCanvas.getLayoutX() + " " + paneHoldingVideoCanvas.getLayoutY());
	}

	public void initializeWithStage(Stage stage) {
		videoGC = videoCanvas.getGraphicsContext2D();
		videoCanvas.widthProperty().bind(paneHoldingVideoCanvas.widthProperty());
		videoCanvas.heightProperty().bind(paneHoldingVideoCanvas.heightProperty());
		videoCanvas.widthProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		videoCanvas.heightProperty().addListener((obs, oldV, newV) -> repaintCanvas());

		// Still cant work
		paneHoldingVideoCanvas.widthProperty().addListener((obs, oldV, newV) -> repaintCanvas());
		paneHoldingVideoCanvas.heightProperty().addListener((obs, oldV, newV) -> repaintCanvas());

		// remove debugging code
		System.out.println("Video Canvas: " + videoCanvas.getHeight() + " " + videoCanvas.getWidth() + " "
				+ videoCanvas.getLayoutX() + " " + videoCanvas.getLayoutY());
		System.out.println("Pane: " + paneHoldingVideoCanvas.getHeight() + " " + paneHoldingVideoCanvas.getWidth() + " "
				+ paneHoldingVideoCanvas.getLayoutX() + " " + paneHoldingVideoCanvas.getLayoutY());
	}

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

	public void repaintCanvas() {
		if (project != null) {
			displayFrame((int) sliderBar.getValue());
		}
	}

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

	private double getImageScalingRatio() {
		double widthRatio = videoCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = videoCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);

	}

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

	@FXML
	public void handleRemoveChickButton() {
		String trackID = chicksBox.getValue();
		chicksBox.getItems().remove(trackID);
		project.removeTrack(trackID);
		if (chicksBox.getItems().size() == 0) {
			nextButton.setDisable(true);
			// add code to show prompt text
		}

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

		OpeningWindowController controller = loader.getController();
		controller.setProject(project);
	}

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

	@FXML
	public void handleEnd() {
		project.getVideo().setEndFrameNum((int) sliderBar.getValue());
		endTimeLabel.setText(project.getVideo().getTime(project.getVideo().getEndFrameNum()));
	}

	@FXML
	public void handleStart() {
		project.getVideo().setStartFrameNum(project.getVideo().getCurrentFrameNum());
		startTimeLabel.setText(project.getVideo().getTime(project.getVideo().getStartFrameNum()));
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

		isAbleToSetOrigin = false;
		isAbleToSetArena = false;
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
			currentFrameLabel.setText(project.getVideo().getTime(frameNumber));
		});
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track : project.getUnassignedSegments()) {
			System.out.println(track);
		}

		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			autoTrackButton.setText("Start auto-tracking");
		});
	}

	@FXML
	public void handleMouseDragged(MouseEvent event) {
		if (isAbleToSetArena) {
			mouseDragRect.setWidth(Math.abs(event.getX() - startPoint.getX()));
			mouseDragRect.setHeight(Math.abs(event.getY() - startPoint.getY()));
		}
	}

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
			//set arenabound of the video
			project.getVideo().setArenaBounds(new Rectangle(mouseDragRect.getX() / getImageScalingRatio(),
			mouseDragRect.getY() / getImageScalingRatio()));
			
			//make it show up on the screen 
			paneHoldingVideoCanvas.getChildren().add(mouseDragRect);
		} else if (isAbleToSetOrigin) {
			if (origin != null) {
				paneHoldingVideoCanvas.getChildren().remove(origin);
				paneHoldingVideoCanvas.getChildren().remove(xAxis);
				paneHoldingVideoCanvas.getChildren().remove(yAxis);
			}
			origin = new Circle(event.getX(), event.getY(), 5, Color.BLUE);
			setUpAxis();
			
			//set the origin, xAxis and yAxis of the Video
			project.getVideo().setOrigin(new Point((int) (origin.getCenterX() / getImageScalingRatio()),
			(int) (origin.getCenterY() / getImageScalingRatio())));
			project.getVideo().setXAxis(new Line (xAxis.getStartX()/getImageScalingRatio(), xAxis.getStartY()/getImageScalingRatio() , xAxis.getEndX()/getImageScalingRatio(), 
					xAxis.getEndY()/getImageScalingRatio()));
			project.getVideo().setYAxis(new Line (yAxis.getStartX()/getImageScalingRatio(), yAxis.getStartY()/getImageScalingRatio() , yAxis.getEndX()/getImageScalingRatio(), 
					yAxis.getEndY()/getImageScalingRatio()));			
			
			
			//make it show up on the screen 
			paneHoldingVideoCanvas.getChildren().add(origin);
			paneHoldingVideoCanvas.getChildren().add(xAxis);
			paneHoldingVideoCanvas.getChildren().add(yAxis);
		}
	}

	public void setUpAxis() {
		xAxis = new Line(0, origin.getCenterY(), videoCanvas.getWidth(), origin.getCenterY());
		yAxis = new Line(origin.getCenterX(), 0, origin.getCenterX(), videoCanvas.getHeight());
		xAxis.setStroke(Color.BLUE);
		yAxis.setStroke(Color.BLUE);
		xAxis.setStrokeWidth(3.0f);
		yAxis.setStrokeWidth(3.0f);

	}

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
	 * menuFileSave saves users' progress
	 */
	@FXML
	public void menuFileSave() throws FileNotFoundException {
		File saveFile = new File(project.getVideo().getFilePath());
		File output = new File("output." + saveFile.getName() + ".txt");
		project.saveToFile(output);
	}

	@FXML
	public void menuFileOpen() {
		// open method goes here
	}

	// CALIBRATION TOOL

	/*
	 * menuCalibrationSetArenaBounds
	 */
	@FXML
	public void menuCalibrationSetArenaBounds() {
		isAbleToSetArena = true;
		isAbleToSetOrigin = false;

	}

	/**
	 * 
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
	 * 
	 */
	@FXML
	public void menuHelpAbout() {
		Alert aboutUs = new Alert(AlertType.INFORMATION);
		aboutUs.setTitle("About Us");
		aboutUs.setHeaderText(null);
		aboutUs.setContentText(" ");
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
