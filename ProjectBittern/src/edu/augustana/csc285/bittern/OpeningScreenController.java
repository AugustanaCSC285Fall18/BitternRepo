package edu.augustana.csc285.bittern;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class OpeningScreenController {

	@FXML private Button browseButton;
	@FXML private Label nameLabel;
	@FXML private Label fileNameLabel;
	@FXML private Button confirmButton;
	private File chosenFile;
	
	@FXML public void handleBrowse() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		Window mainWindow = browseButton.getScene().getWindow();
		chosenFile = fileChooser.showOpenDialog(mainWindow);
		fileNameLabel.setText("You selected " + chosenFile.getAbsolutePath());	
	 }
	
	@FXML public void handleConfirm() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		BorderPane root = (BorderPane)loader.load();
		loader.setRoot(root);
		
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) confirmButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.show();		
	
		MainWindowController controller = loader.getController();
		controller.initializeWithStage(primary);
		controller.createVideo(chosenFile.getAbsolutePath());
	}
}
