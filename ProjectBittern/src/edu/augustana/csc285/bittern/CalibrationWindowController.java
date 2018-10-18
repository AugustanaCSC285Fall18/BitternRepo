package edu.augustana.csc285.bittern;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import dataModel.AnimalTrack;
import dataModel.ProjectData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;
//import javafx.scene.shape.LineBuilder;

public class CalibrationWindowController {

	@FXML
	private Button backButton;
	@FXML
	private Button confirmButton;
	@FXML
	private Slider sliderBar;
	@FXML
	private ImageView videoView;
	@FXML
	private BorderPane drawingBoard;
	@FXML
	private ComboBox<Integer> stepBox;
	@FXML
	private ComboBox<String> chicksBox;
	@FXML
	private TextField nameField;
	@FXML
	private Label showActualLengthX;
	@FXML
	private Label showActualLengthY;

	private Line mouseDragLine;
	private Rectangle mouseDragRect;
	private ProjectData project;

	private double pixelLengthX;
	private double pixelLengthY;
	private int actualLengthX;
	private int actualLengthY;

	private Point startPoint;

	public static double ratioX;
	public static double ratioY;

	public void createProject(String filePath) {
		try {
			project = new ProjectData(filePath);
			project.getVideo().setXPixelsPerCm(6.5);
			project.getVideo().setYPixelsPerCm(6.7);

			sliderBar.setMax(project.getVideo().getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(project.getVideo().getFrameRate());
			displayFrame();
			System.out.println(project.getVideo());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayFrame() {
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		videoView.setImage(curFrame);
	}

	@FXML
	public void getPoint() throws IOException {
		videoView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {

			}
		});
	}

	@FXML
	public void handleDrawingBoard(MouseEvent event) {
		// Point point = new Point((int) event.getX(), (int) event.getY());

	}

	@FXML
	public void handleSlider() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					project.getVideo().setCurrentFrameNum(arg2.intValue());
					displayFrame();
				}
			}
		});
	}

	public void drawCircle(Point p) {
		Circle c = new Circle(p.getX(), p.getY(), 5, Color.RED);
		drawingBoard.getChildren().add(c);
	}

	public void drawLine(Point p1, Point p2) {
		Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		line.setStroke(Color.RED);
		line.setStrokeWidth(5.0f);
		drawingBoard.getChildren().add(line);

	}

	@FXML
	private void handleBack() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("OpeningWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) backButton.getScene().getWindow();
		primary.setTitle("Bittern Tracker");
		primary.setScene(nextScene);
		primary.show();

		OpeningWindowController controller = loader.getController();
		controller.setup(project.getVideo().getFilePath());
	}

	@FXML
	public void handleConfirm() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("AutoTrackWindowController.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) confirmButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.setTitle("Auto Tracking Window");
		primary.show();
		
		System.out.println(project.getVideo());
		AutoTrackWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.setup(project);
	}

	@FXML
	public void initialize() {
//		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
//			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
//				if (sliderBar.isValueChanging()) {
//					project.getVideo().setCurrentFrameNum(arg2.intValue());
//					displayFrame();
//				}
//			}
//		});

		stepBox.getItems().addAll(1, 2, 3, 4, 5);

	}

	@FXML
	public void handleName() {
		String name = nameField.getText();
		project.getTracks().add(new AnimalTrack(name));
		chicksBox.getItems().add(name);
		nameField.setText("");
	}

	@FXML
	public void handleStepBox() {
		project.getVideo().setStepSize(stepBox.getValue());
	}

	public void initializeWithStage() {
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
	}

	public void setActualLengthX(String inputX) {
		actualLengthX = Integer.parseInt(inputX);
		ratioX = actualLengthX / pixelLengthX;
		System.out.println(ratioX);
	}

	public void setActualLengthY(String inputY) {
		actualLengthY = Integer.parseInt(inputY);
		ratioY = actualLengthY / pixelLengthY;
		System.out.println(ratioY);
	}

	@FXML
	public void handleMousePressed(MouseEvent event) {
//		System.out.println("pressed: " + event.getX() + " " + event.getY());
//		if (mouseDragLine != null) {
//			drawingBoard.getChildren().remove(mouseDragLine);
//		}
//		mouseDragLine = new Line(event.getX(), event.getY(), event.getX(), event.getY());
//		mouseDragLine.setStroke(Color.RED);
//		mouseDragLine.setStrokeWidth(5.0f);
//		drawingBoard.getChildren().add(mouseDragLine);

		if (mouseDragRect != null) {
			drawingBoard.getChildren().remove(mouseDragRect);
		}
		startPoint = new Point((int) event.getX(), (int) event.getY());
		mouseDragRect = new Rectangle(startPoint.getX(), startPoint.getY(), 1, 1);
		mouseDragRect.setStroke(Color.RED);
		mouseDragRect.setStrokeWidth(5.0f);
		drawingBoard.getChildren().add(mouseDragRect);

	}

	@FXML
	public void handleMouseDragged(MouseEvent event) {
		System.out.println("dragged: " + event.getX() + " " + event.getY());
		// mouseDragLine.setEndX(event.getX());
		// mouseDragLine.setEndY(event.getY());

		mouseDragRect.setWidth(Math.abs(event.getX() - startPoint.getX()));
		mouseDragRect.setHeight(Math.abs(event.getY() - startPoint.getY()));
	}

	@FXML
	public void handleMouseReleased(MouseEvent event) {
		ArrayList<String> choices = new ArrayList();
		choices.add("Vertical");
		choices.add("Horizon");

		ChoiceDialog<String> dialog = new ChoiceDialog<>("", choices);
		dialog.setHeaderText("Set up actual length");
		dialog.showAndWait();
		dialog.setHeaderText(null);

		while (dialog.getResult().equals("")) {
			Alert alert = new Alert(AlertType.INFORMATION, "You must choose something :)");
			alert.setHeaderText(null);
			alert.showAndWait();
			dialog.showAndWait();
		}

		if (dialog.getResult().equals("Vertical")) {
			askForYValue();
		} else if (dialog.getResult().equals("Horizon")) {
			askForXValue();
		}

		System.out.println("released: " + event.getX() + " " + event.getY());
	}

	public void setProject(ProjectData project) {
		this.project = project;

		if (!(project.getTracks() == null)) {
			for (AnimalTrack track : project.getTracks()) {
				chicksBox.getItems().add(track.getID());
			}
		}

		project.getVideo().setCurrentFrameNum(0);
		displayFrame();
		System.out.println(project.getVideo());
	}

	public void askForXValue() {
		TextInputDialog horizontalValue = new TextInputDialog("cm");
		horizontalValue.setHeaderText("Set up horizontal length");
		horizontalValue.setContentText("Please enter actual horizontal length:");
		horizontalValue.showAndWait();
		pixelLengthX = Point2D.distance(mouseDragLine.getStartX(), mouseDragLine.getStartY(), mouseDragLine.getEndX(),
				mouseDragLine.getEndY());
		setActualLengthX(horizontalValue.getResult());
		showActualLengthX.setText("Actual Horizontal Length: " + actualLengthX + " cm");
	}

	public void askForYValue() {
		TextInputDialog verticalValue = new TextInputDialog("cm");
		verticalValue.setHeaderText("Set up vertical length");
		verticalValue.setContentText("Please enter actual vertical length:");
		verticalValue.showAndWait();
		pixelLengthY = Point2D.distance(mouseDragLine.getStartX(), mouseDragLine.getStartY(), mouseDragLine.getEndX(),
				mouseDragLine.getEndY());
		setActualLengthY(verticalValue.getResult());
		showActualLengthY.setText("Actual Vertical Length: " + actualLengthY + " cm");

	}

}