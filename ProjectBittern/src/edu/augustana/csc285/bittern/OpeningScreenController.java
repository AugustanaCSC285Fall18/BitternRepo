package edu.augustana.csc285.bittern;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class OpeningScreenController {

	@FXML private Button browseButton;
	@FXML private Label nameLabel;
	@FXML private Label fileNameLabel;
	@FXML private Button confirmButton;
	
	@FXML public void handleBrowse() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		Window mainWindow = browseButton.getScene().getWindow();
		File chosenFile = fileChooser.showOpenDialog(mainWindow);
		fileNameLabel.setText("You selected " + chosenFile.getAbsolutePath());
	 }
	
	@FXML public void handleConfirm() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("SecondWindow.fxml"));
		AnchorPane root = (AnchorPane)loader.load();
		
		//SecondWindowController confirmController = loader.getController();	
		
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) confirmButton.getScene().getWindow();
		primary.setScene(nextScene);
		
		//Stage stage = new Stage();
		//stage.setScene(nextScene);
		primary.show();		
		//confirmButton.getScene().getWindow().hide();
		
		//primaryStage.setScene(scene);
		//primaryStage.show();
	}
}
