package edu.augustana.csc285.bittern;

import dataModel.ProjectData;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SuggestAutoTrackWindowController {

	@FXML private Button playButton;
	private ManualTrackWindowController mwc;
	
	public void setup(ProjectData project, ManualTrackWindowController mwc) {
		this.mwc = mwc;
	}
	
	@FXML
	public void handleButton() throws InterruptedException {
		mwc.handlePlay();
	}
}
