package edu.augustana.csc285.bittern;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import dataModel.AnimalTrack;
import dataModel.ProjectData;
import dataModel.Video;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class MainWindowController implements AutoTrackListener {

	@FXML private Label currentFrameLabel;
	@FXML private ImageView videoView;
	@FXML private Slider sliderBar;
	@FXML private Button startTimeButton;
	@FXML private Button endTimeButton;
	@FXML private Button playButton;
	@FXML private Button autoTrackButton;
	@FXML private ProgressBar progressAutoTrack;
	@FXML private Label startTimeLabel;
	@FXML private Label endTimeLabel;

	private ScheduledExecutorService timer;
	private AutoTracker autotracker;
	private ProjectData project;
	private Stage stage;
	
	public void createVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			Video chosenVideo = project.getVideo(); //why
			
			project.getVideo().setXPixelsPerCm(6.5); 
			project.getVideo().setYPixelsPerCm(6.7);
			
			sliderBar.setMax(chosenVideo.getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(chosenVideo.getFrameRate());

			startTimeLabel.setText("Start: " + chosenVideo.getStartFrameNum());
			endTimeLabel.setText("End: " + chosenVideo.getEndFrameNum());

			displayFrame();
			System.out.println(chosenVideo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startVideo() {
		if (project.getVideo().isOpened()) {
			Runnable frameGrabber = new Runnable() {
				public void run() {
					sliderBar.setValue(project.getVideo().getCurrentFrameNum());
					displayFrame();
				}
			};

			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, (int) project.getVideo().getFrameRate(), TimeUnit.MILLISECONDS);

		}
	}

	public void displayFrame() {
		if (autotracker == null || !autotracker.isRunning()) {
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoView.setImage(curFrame);
			Platform.runLater(() -> {
				currentFrameLabel.setText("" + project.getVideo().getCurrentFrameNum());
			});
		}	
	}

	@FXML public void initialize() {
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
		this.stage = stage;
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());  
	}

	@FXML
	public void handleStart() {
		project.getVideo().setStartFrameNum(project.getVideo().getCurrentFrameNum());
		startTimeLabel.setText("Start: " + project.getVideo().getStartFrameNum());
		System.out.println(project.getVideo());
	}

	@FXML
	public void handleEnd() {
		project.getVideo().setEndFrameNum((int) sliderBar.getValue());
		endTimeLabel.setText("End: " + project.getVideo().getEndFrameNum());
	}


	@FXML
	public void handlePlay() throws InterruptedException {
		if (playButton.getText().equalsIgnoreCase("play video")) {
			playButton.setText("Pause Video");
			startVideo();
		} else {
			timer.shutdown();
			timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
			playButton.setText("Play Video");
		}
	}

	@FXML public void handleStartAutotracking() {
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
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track: trackedSegments) {
			System.out.println(track);
			//System.out.println("  " + track.getPositions());
		}
		Platform.runLater(() -> { 
			progressAutoTrack.setProgress(1.0);
			autoTrackButton.setText("Start auto-tracking");
		});	
		autotracker.cancelAnalysis();
	}
	
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		Platform.runLater(() -> { 
			videoView.setImage(imgFrame);
			progressAutoTrack.setProgress(fractionComplete);
			sliderBar.setValue(frameNumber);
			currentFrameLabel.setText("" + frameNumber);
		});		
	}

	//doesn't work until you start playing video
	@FXML
	public void getPoint() throws IOException {
		videoView.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
		        System.out.println(event.getSceneX());
		        System.out.println(event.getSceneY());
		    }
		});
		

	}
	

}
