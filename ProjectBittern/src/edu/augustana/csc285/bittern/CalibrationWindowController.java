package edu.augustana.csc285.bittern;

import java.awt.Point;
import java.io.IOException;

import dataModel.ProjectData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class CalibrationWindowController {

	@FXML private Button backButton;
	@FXML private Button confirmButton;
	@FXML private Button lengthButton;
	@FXML private ComboBox<String> chicksComboBox;
	@FXML private Label lengthLabel;
	@FXML private TextField lengthTextField;
	@FXML private TextField widthTextField;
	@FXML private Slider sliderBar;
	@FXML private ImageView videoView;
	@FXML private Button widthButton;
	private Point point;
	private ProjectData project;
	

	@FXML private Label widthLabel;

	public void createProject(String filePath) {
		try {
			project = new ProjectData(filePath);			
			project.getVideo().setXPixelsPerCm(6.5); 
			project.getVideo().setYPixelsPerCm(6.7);

			sliderBar.setMax(project.getVideo().getTotalNumFrames()-1);
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
				point = new Point((int)event.getX(), (int)event.getY());
				System.out.println(point);
			}
		});
	}

	@FXML 
	private void handleBack() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("OpeningWindow.fxml"));
		BorderPane root = (BorderPane)loader.load();
		
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) backButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();		
	
		OpeningWindowController controller = loader.getController();
		controller.setup(project.getVideo().getFilePath());
	}

	@FXML
	public void handleComboBox() {
		
	}
	
	@FXML 
	public void handleConfirm() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		BorderPane root = (BorderPane)loader.load();
		
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) confirmButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();		
	
		MainWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.setup(project);
	}
	
	@FXML 
	public void handleLengthButton() {

	}
	
	
	@FXML 
	public void handleWidthButton() {

	}
		
	@FXML 
	public void initialize() {
		chicksComboBox.getItems().addAll("1", "2", "3");
		
		sliderBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderBar.isValueChanging()) {
					project.getVideo().setCurrentFrameNum(arg2.intValue());
					displayFrame(); 
				}
			}
		});
	}
	
	public void initializeWithStage() {
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
	}
	
	@FXML
	public void setActualLength() {
		
	}
	
	@FXML
	public void setActualWidth() {
		
	}
	
	public void setProject(ProjectData project) {
		this.project = project;
		project.getVideo().setCurrentFrameNum(0);
		displayFrame();
		System.out.println(project.getVideo());
	}
}
