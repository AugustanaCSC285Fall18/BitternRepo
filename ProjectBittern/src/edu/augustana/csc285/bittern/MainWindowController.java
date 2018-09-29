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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class MainWindowController implements AutoTrackListener {

	@FXML
	private Button autoTrackButton;
	private AutoTracker autotracker;
	@FXML
	private Label currentFrameLabel;
	@FXML
	private Button endTimeButton;
	@FXML
	private Label endTimeLabel;
	@FXML
	private Button playButton;
	@FXML
	private ProgressBar progressAutoTrack;
	private ProjectData project;
	@FXML
	private Slider sliderBar;
	// private Stage stage;

	@FXML
	private Button startTimeButton;
	@FXML
	private Label startTimeLabel;
	private ScheduledExecutorService timer;
	@FXML
	private ImageView videoView;

	public void createVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			// Video chosenVideo = project.getVideo();

			project.getVideo().setXPixelsPerCm(6.5);
			project.getVideo().setYPixelsPerCm(6.7);

			sliderBar.setMax(project.getVideo().getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(project.getVideo().getFrameRate());

			startTimeLabel.setText("Start: " + project.getVideo().getStartFrameNum());
			endTimeLabel.setText("End: " + project.getVideo().getEndFrameNum());

			displayFrame();
			System.out.println(project.getVideo());
		} catch (Exception e) {
			e.printStackTrace();
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

	// doesn't work until you start playing video
	@FXML
	public void getPoint() throws IOException {
		// videoView.setOnMouseClicked(new EventHandler<MouseEvent>() {
		// @Override
		// public void handle(MouseEvent event) {
		// System.out.println(event.getSceneX());
		// System.out.println(event.getSceneY());
		// }
		// });

		Image i[] = new Image [project.getVideo().getTotalNumFrames()];
		for(int i = 1; i < project.getC)

		videoView.setOnMouseClicked(event -> {
			double x = event.getX();
			double y = event.getY();
			System.out.println(x + " " + y );
			WritableImage wi = new WritableImage(i[0].getPixelReader(), (int) i[0].getWidth(), (int) i[0].getHeight());
			PixelWriter pw = wi.getPixelWriter();
			pw.setColor((int) x, (int) y, new Color(0, 0, 0, 1));
			i[0] = wi;
			videoView.setImage(i[0]);

		});

	}

	@FXML
	public void handleEnd() {
		project.getVideo().setEndFrameNum((int) sliderBar.getValue());
		endTimeLabel.setText("End: " + project.getVideo().getEndFrameNum());
		// Note: without the following line after the user clicks endTimeButton,
		// if they play video, the video starts from the endTime frame.
		// line is currently a band-aid
		project.getVideo().setCurrentFrameNum(project.getVideo().getStartFrameNum());
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

	@FXML
	public void handleStart() {
		project.getVideo().setStartFrameNum(project.getVideo().getCurrentFrameNum());
		startTimeLabel.setText("Start: " + project.getVideo().getStartFrameNum());
		System.out.println(project.getVideo());
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
	}

	public void initializeWithStage(Stage stage) {
		// Stage stage = stage;
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());

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
			this.timer.scheduleAtFixedRate(frameGrabber, 0, (int) project.getVideo().getFrameRate(),
					TimeUnit.MILLISECONDS);

		}
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track : trackedSegments) {
			System.out.println(track);
			// System.out.println(" " + track.getPositions());
		}
		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			autoTrackButton.setText("Start auto-tracking");
		});
		autotracker.cancelAnalysis();
	}

}
