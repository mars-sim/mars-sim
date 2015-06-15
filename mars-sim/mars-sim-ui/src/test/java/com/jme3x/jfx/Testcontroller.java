package com.jme3x.jfx;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class Testcontroller {
	@FXML
	private StackPane	rootObject;

	@FXML
	private WebView		website;

	@FXML
	public void initialize() {
		this.website.getEngine().load("http://acid3.acidtests.org/");
	}
}
