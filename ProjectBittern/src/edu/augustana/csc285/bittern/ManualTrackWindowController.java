package edu.augustana.csc285.bittern;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class ManualTrackWindowController {

	@FXML private StackPane stackPane;
	@FXML private Canvas drawingCanvas;
	@FXML private Canvas progressCanvas;
	@FXML private Button playButton;
	@FXML private Button exportButton;
	@FXML private Button backButton;
	@FXML private ImageView videoView;
	@FXML private Label currentFrameLabel;
	@FXML private Label endFrameLabel;
	@FXML private Label startFrameLabel;
	@FXML private Slider sliderBar;
	@FXML private Button previousButton;
	@FXML private Button nextButton;
	@FXML private ComboBox<String> chicksBox;
	@FXML private ComboBox<AnimalTrack> tracksBox;
	

	private ProjectData project;
	private ScheduledExecutorService timer;
	private TimePoint currentTimePoint;
	private GraphicsContext drawingGC;
	private GraphicsContext progressGC;
	private AnimalTrack currentTrack;
	private double frameWidthRatio; 
	private double startWidth;
	private double endWidth;

	@FXML
	public void initialize() {
		
		setupSlider();
		setupClick();
		setupCanvas();
				
	}

	public void initializeWithStage(Stage stage) {
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());

		drawingCanvas.widthProperty().bind(videoView.fitWidthProperty());
		drawingCanvas.heightProperty().bind(videoView.fitHeightProperty());
		
		progressCanvas.widthProperty().bind(videoView.getScene().widthProperty());
		progressCanvas.widthProperty().addListener(observable -> refillCanvas());
	}

	public void setupCanvas() {
		drawingGC = drawingCanvas.getGraphicsContext2D();
		progressGC = progressCanvas.getGraphicsContext2D();
		drawingGC.setFill(Color.CYAN);				
	}


	public void setupClick() {
		drawingCanvas.setOnMouseClicked((event) -> {
			currentTimePoint = new TimePoint(event.getX(), event.getY(), 
					project.getVideo().getCurrentFrameNum());
			/*if (project.getVideo().getArenaBounds().contains(currentTimePoint.getPointAWT())
					&& project.getVideo().timeWithinBounds()) {*/ //until calibration works
			if (project.getVideo().timeWithinBounds()) {
				drawingGC.fillOval(event.getX() - 3, event.getY() - 3, 6, 6); //debug for edges of the arena
				currentTrack.add(currentTimePoint);
				//updateCanvas(project.getVideo().getCurrentFrameNum());
				
				if (project.containsAutoTracksAtTime(currentTimePoint.getFrameNum())) {
					tracksBox.setPromptText("Posible Autotracks!");
					handleTracksBox();
				}
			}
			jump(1); //when do we jump?
		});
	}

	
	public void suggestAutoTracks() {
		
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
		FXMLLoader loader = new FXMLLoader(getClass().getResource("AutoTrackWindowController.fxml"));
		BorderPane root = (BorderPane) loader.load();

		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		Stage primary = (Stage) backButton.getScene().getWindow();
		primary.setTitle("Auto Tracking Window");
		primary.setScene(nextScene);
		primary.show();

		AutoTrackWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.setup(project);
	}
	
	@FXML
	public void handleTracksBox() {
		for (AnimalTrack track : project.getUnassignedSegmentsThatContainTime(project.getVideo().getCurrentFrameNum())) {
			tracksBox.getItems().add(track);
		}
	}

	@FXML
	public void handleChicksBox() {
		project.getTracks().add(currentTrack);
		currentTrack = project.getTracks().get(project.getAnimalIndex(chicksBox.getValue()));
		sliderBar.setValue(project.getVideo().getStartFrameNum());
		refillCanvas();
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
		System.out.println(videoView.getFitWidth() + " = " + drawingCanvas.getWidth());
		frameWidthRatio = project.getVideo().getTotalNumFrames() / progressCanvas.getWidth();
		startWidth = project.getVideo().getStartFrameNum() / frameWidthRatio;
		endWidth = project.getVideo().getEndFrameNum() / frameWidthRatio;
		
		progressGC.setFill(Color.GRAY);
		progressGC.fillRect(0, progressCanvas.getLayoutY(), startWidth, progressCanvas.getHeight());
		progressGC.fillRect(endWidth, progressCanvas.getLayoutY(), progressCanvas.getWidth() - endWidth, 
				progressCanvas.getHeight());
		
		progressGC.setFill(Color.RED);
		progressGC.fillRect(startWidth, progressCanvas.getLayoutY(), endWidth - startWidth,
				progressCanvas.getHeight());

		for (TimePoint position : currentTrack.getPositions()) {
			updateCanvas(position.getFrameNum());
		}
	}
	
	public void updateCanvas(int frameNumber) {
		startWidth = frameNumber / frameWidthRatio - frameWidthRatio; //debug for ends
		progressGC.setFill(Color.GREEN);
		progressGC.clearRect(startWidth, progressCanvas.getLayoutX(), frameWidthRatio, progressCanvas.getHeight());
		progressGC.fillRect(startWidth, progressCanvas.getLayoutX(), frameWidthRatio, progressCanvas.getHeight());
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