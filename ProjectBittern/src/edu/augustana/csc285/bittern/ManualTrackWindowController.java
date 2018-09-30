package edu.augustana.csc285.bittern;

import java.awt.Point;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dataModel.AnimalTrack;
import dataModel.ExportData;
import dataModel.ProjectData;
import dataModel.TimePoint;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class ManualTrackWindowController {

	@FXML private BorderPane drawingBoard;
	@FXML private Button playButton;
	@FXML private Button exportButton;
	@FXML private Button backButton;
	@FXML private Button chicksButton;
	@FXML private ImageView videoView;
	@FXML private Label currentFrameLabel;
	@FXML private Slider sliderBar;
	@FXML private TextField nameField;
	@FXML private Button previousButton;
	@FXML private Button nextButton;

	private ProjectData project;
	private ScheduledExecutorService timer;
	private Point point;
	private Stage stage;
	private Stage popup;
	private GraphicsContext gc;
	private String name;
	private AnimalTrack track;

	@FXML
	public void initialize() {
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					project.getVideo().setCurrentFrameNum(arg2.intValue());
					displayFrame();
				}
			}
		});

		videoView.setOnMouseClicked((event) -> {
			point = new Point((int) event.getX(), (int) event.getY());
			System.out.println(point);
			track = new AnimalTrack(name);
			track.add(new TimePoint(point.getX(), point.getY(), project.getVideo().getCurrentFrameNum()));
			System.out.println(track.getPositions());
			
		});
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;
		popup = new Stage();
		popup.initOwner(stage);
		popup.setHeight(300);
		popup.setWidth(300);

		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
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
	public void handleExport() throws IOException {
		ExportData export = new ExportData(project);
		export.processData();
	}

	@FXML
	public void handleName() {
		name = nameField.getText();
	}

	@FXML
	public void handlePrevious() {
		jump(-1);
	}

	@FXML
	public void handleNext() {
		jump(1);
	}

	public void jump(int sth) {
		project.getVideo().setCurrentFrameNum((project.getVideo().getCurrentFrameNum() + sth * (int)project.getVideo().getFrameRate()));
		sliderBar.setValue(project.getVideo().getCurrentFrameNum());
		displayFrame();
	}

	public void displayFrame() {
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		videoView.setImage(curFrame);
		Platform.runLater(() -> {
			currentFrameLabel.setText("" + project.getVideo().getCurrentFrameNum());
		});
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

	public void setup(ProjectData project) {
		try {
			this.project = project;
			project.getVideo().setXPixelsPerCm(6.5);
			sliderBar.setMax(project.getVideo().getTotalNumFrames() - 1);
			sliderBar.setBlockIncrement(project.getVideo().getFrameRate());

			displayFrame();
			System.out.println(project.getVideo());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void handleDrawingBoard(MouseEvent event) {
		point = new Point((int) event.getX(), (int) event.getY());
		System.out.println("BorderPane Point: " + point);
		drawCircle(point);


	}

	public void drawCircle (Point p) {
		//Circle c = new Circle(p.getX(), p.getY(),5, Color.RED);
		//drawingBoard.getChildren().add(c);
	}

	// doesn't work until you start playing video
	@FXML
	public void getPoint() throws IOException {

	}

}
