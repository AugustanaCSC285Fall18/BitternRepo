package edu.augustana.csc285.bittern;

import java.awt.Point;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.MatOfPoint;

import autotracking.AutoTracker;
import autotracking.DetectedShape;
import dataModel.AnimalTrack;
import dataModel.DataExporter;
import dataModel.ProjectData;
import dataModel.TimePoint;
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
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class ManualTrackWindowController {

	@FXML private BorderPane drawingBoard;
	@FXML private Button playButton;
	@FXML private Button exportButton;
	@FXML private Button backButton;
	@FXML private ImageView videoView;
	@FXML private Label currentFrameLabel;
	@FXML private Slider sliderBar;
	@FXML private Button previousButton;
	@FXML private Button nextButton;
	@FXML private ComboBox<String> chicksBox;
	@FXML private Canvas progressCanvas;

	private AutoTracker autoTracker;
	private Stage popup;
	private ProjectData project;
	private ScheduledExecutorService timer;
	private TimePoint currentTimePoint;
	private Point point;
	private Stage stage;
	private GraphicsContext gc;
	private String name;
	private AnimalTrack track;
	private double frameWidthRatio; //refactor

	@FXML
	public void initialize() {
		
		setupSlider();
		videoView.setOnMouseClicked((event) -> {
			setupClick(event);
		});
		setupCanvas();
				
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;
		
		popup = new Stage();
		popup.initOwner(stage);
		popup.setHeight(100);
		popup.setWidth(300);
		
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
		progressCanvas.widthProperty().bind(videoView.getScene().widthProperty());
		progressCanvas.widthProperty().addListener(observable -> refillCanvas());
	}

	public void setupCanvas() {
		gc = progressCanvas.getGraphicsContext2D();
		gc.setFill(Color.GRAY);
		gc.fillRect(0, progressCanvas.getLayoutY(), progressCanvas.getWidth(), progressCanvas.getHeight());
		
	}

	//need to refactor this method
	public void setupClick(MouseEvent event) {
		popup.close();
		if (chicksBox.getValue() == null) {
			popup.show();
		} else {
			currentTimePoint = new TimePoint(event.getX(), event.getY(), project.getVideo().getCurrentFrameNum());

			if (project.getVideo().getArenaBounds().contains(currentTimePoint.getPointAWT())) {
				track.getPositions().add(currentTimePoint);	
				updateCanvas(currentTimePoint.getFrameNum());
				jump(1);
			}

			
		}

	}
	
	public void sth() {
		for (int i = 0; i < track.getPositions().size(); i++) {
			if (track.getPositions().get(i).getFrameNum() == currentTimePoint.getFrameNum()) {
				TimePoint pnt = track.getPositions().get(i);
				track.getPositions().remove(pnt);
				track.getPositions().add(i, currentTimePoint);
			} else {
				track.getPositions().add(currentTimePoint);
			}
			updateCanvas(currentTimePoint.getFrameNum());
			jump(1);
		}
		
	}
	
	public void check() {
		for (AnimalTrack track : project.getUnassignedSegments()) {
			MatOfPoint contour = new MatOfPoint();
			DetectedShape shape = new DetectedShape(contour);

			for (TimePoint position : track.getPositions()) {
				if (position.getFrameNum() == currentTimePoint.getFrameNum() 
						&& shape.getArea() >= autoTracker.getMinShapePixelArea() 
						&& shape.getArea() <= autoTracker.getMaxShapePixelArea()) {
					suggestAutoTracks(shape);
				}
			}
		}
	}

	//how we connect auto to manual tracking
	public void suggestAutoTracks(DetectedShape shape) {
		System.out.println("Possible track");
	}

	public void setup(ProjectData project, AutoTracker autoTracker) {
		try {
			this.project = project;
			this.autoTracker = autoTracker;
			//project.getVideo().setXPixelsPerCm(6.5);
			project.getVideo().resetToStart();
			sliderBar.setMax(project.getVideo().getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(project.getVideo().getFrameRate());
			
			frameWidthRatio = project.getVideo().getTotalNumFrames() / progressCanvas.getWidth();
			System.out.println("Frame Width Ratio: " + frameWidthRatio + " width: " + progressCanvas.getWidth());
			
			if (!(project.getTracks() == null)) {
				for (AnimalTrack track : project.getTracks()) {
					chicksBox.getItems().add(track.getID());
				}
			}
			
			System.out.println(project.getVideo());
			displayFrame();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public void setupSlider() {
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

	@FXML
	public void handleBack() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) backButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();

		MainWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.setup(project);
	}

	@FXML
	public void handleChicksBox() {
		track = project.getAnimal(chicksBox.getValue());
	}

	@FXML
	public void handleDrawingBoard(MouseEvent event) {
		point = new Point((int) event.getX(), (int) event.getY());
		System.out.println("BorderPane Point: " + point);
		//drawCircle(point);
	}

	@FXML
	public void handleExport() throws IOException {
		DataExporter.exportToCSV(project);
	}

	@FXML
	public void handleNext() {
		jump(project.getVideo().getStepSize());
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
	public void handlePrevious() {
		jump(-project.getVideo().getStepSize());
	}


	public void displayFrame() {
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		videoView.setImage(curFrame);
		Platform.runLater(() -> {
			currentFrameLabel.setText("" 
					+ project.getVideo().getTime(project.getVideo().getCurrentFrameNum()));
		});
		
	}

	public void drawCircle (Point p) {
		Circle c = new Circle(p.getX(), p.getY(),5, Color.RED);
		drawingBoard.getChildren().add(c);
	}

	public void jump(int stepSize) {
		double frameNum = project.getVideo().getCurrentFrameNum() 
				+ stepSize * project.getVideo().getFrameRate();
		if (frameNum < project.getVideo().getEndFrameNum()) {
			project.getVideo().setCurrentFrameNum((int)frameNum);
			sliderBar.setValue(project.getVideo().getCurrentFrameNum());
			displayFrame();
		}
	
	}

	public void refillCanvas() {
		frameWidthRatio = project.getVideo().getTotalNumFrames() / progressCanvas.getWidth();
	
		gc.fillRect(0, 0, progressCanvas.getWidth(), progressCanvas.getHeight());
				
		for (AnimalTrack track : project.getTracks()) {
			for (int i = 0; i < track.getPositions().size(); i++) {
				updateCanvas(track.getTimePointAtIndex(i).getFrameNum());
			}
		}
	}
	
	public void updateCanvas(int frameNumber) {
		double x = frameNumber / frameWidthRatio - frameWidthRatio;
		double y = progressCanvas.getLayoutY();
		double width = frameWidthRatio;
		double height = progressCanvas.getHeight();
		
		gc.clearRect(x, y, width, height);
		gc.setFill(Color.GREEN);
		gc.fillRect(x, y, width, height);
	}

	public void startVideo() {
		if (project.getVideo().isOpened()) {
			Runnable frameGrabber = new Runnable() {
				@Override
				public void run() {
					sliderBar.setValue(project.getVideo().getCurrentFrameNum());
					displayFrame();
				}
			};

			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, (int) project.getVideo().getFrameRate(),
					TimeUnit.MILLISECONDS);

		}
	}

}