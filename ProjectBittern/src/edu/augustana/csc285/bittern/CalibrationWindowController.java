package edu.augustana.csc285.bittern;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import dataModel.AnimalTrack;
import dataModel.ProjectData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
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
	private Button setX;
	@FXML
	private Button setY;
	@FXML
	private BorderPane drawingBoard;
	@FXML
	private TextField setActualLengthX;
	@FXML
	private TextField setActualLengthY;
	@FXML
	private ComboBox<Integer> stepBox;
	@FXML
	private ComboBox<String> chicksBox;
	@FXML
	private TextField nameField;

	private ArrayList<Point> calibration = new ArrayList();
	private ProjectData project;

	private double pixelLengthX;
	private double pixelLengthY;

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
		Point point = new Point((int) event.getX(), (int) event.getY());
		calibration.add(point);
		drawCircle(point);

	}

	@FXML
	public void handleSetX() {
		Point p1 = calibration.get(calibration.size() - 2);
		Point p2 = calibration.get(calibration.size() - 1);
		System.out.println(pixelLengthX);
		System.out.println("Point1: " + p1 + " Point 2: " + p2);
		drawLine(p1, p2);

	}

	@FXML
	public void handleSetY() {
		Point p1 = calibration.get(calibration.size() - 2);
		Point p2 = calibration.get(calibration.size() - 1);
		pixelLengthY = Point2D.distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		System.out.println(pixelLengthY);
		System.out.println("Point1: " + p1 + " Point 2: " + p2);
		drawLine(p1, p2);

	}

	@FXML
	public void handleSlider() {

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
		primary.setScene(nextScene);
		primary.show();

		OpeningWindowController controller = loader.getController();
		controller.setup(project.getVideo().getFilePath());
	}

	@FXML
	public void handleConfirm() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) confirmButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();
		System.out.println(project.getVideo());
		MainWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.setup(project);
	}

	@FXML
	public void initialize() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					project.getVideo().setCurrentFrameNum(arg2.intValue());
					displayFrame();
				}
			}
		});

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

	@FXML
	public void setActualLengthX() {
		int actualLengthX = Integer.parseInt(setActualLengthX.getText());
		ratioX = actualLengthX / pixelLengthX;
		System.out.println(ratioX);
	}

	@FXML
	public void setActualLengthY() {
		int actualLength = Integer.parseInt(setActualLengthY.getText());
		ratioY = actualLength / pixelLengthY;
		System.out.println(ratioY);
	}

	@FXML
	public void handleCalibration() {
		
		
//		drawingBoard.setOnMousePressed(new EventHandler<MouseEvent>() {
//			public void handle(MouseEvent event) {
//				drawingBoard.setMouseTransparent(true);
//				System.out.println("Event on Source: mouse pressed");
//				event.setDragDetect(true);
//			}
//		});
//
//		drawingBoard.setOnMouseReleased(new EventHandler<MouseEvent>() {
//			public void handle(MouseEvent event) {
//				System.out.println("Event on Source: mouse released");
//			}
//		});

	drawingBoard.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
			drawLine(new Point( (int) event.getX(),(int) event.getY()), new Point (100,200));
			System.out.println("Event on Source: mouse dragged");
			event.setDragDetect(false);
			}
	});
//
//		sourceFld.setOnDragDetected(new EventHandler<MouseEvent>() {
//			public void handle(MouseEvent event) {
//				sourceFld.startFullDrag();
//				writelog("Event on Source: drag detected");
//			}
//		});
//
//		// Add mouse event handlers for the target
//		targetFld.setOnMouseDragEntered(new EventHandler<MouseDragEvent>() {
//			public void handle(MouseDragEvent event) {
//				writelog("Event on Target: mouse dragged");
//			}
//		});
//
//		targetFld.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {
//			public void handle(MouseDragEvent event) {
//				writelog("Event on Target: mouse drag over");
//			}
//		});
//
//		targetFld.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {
//			public void handle(MouseDragEvent event) {
//				targetFld.setText(sourceFld.getSelectedText());
//				writelog("Event on Target: mouse drag released");
//			}
//		});
//
//		targetFld.setOnMouseDragExited(new EventHandler<MouseDragEvent>() {
//			public void handle(MouseDragEvent event) {
//				writelog("Event on Target: mouse drag exited");
//			}
//		});
//
	}

	public void setProject(ProjectData project) {
		this.project = project;
		project.getVideo().setCurrentFrameNum(0);
		displayFrame();
		System.out.println(project.getVideo());
	}
}