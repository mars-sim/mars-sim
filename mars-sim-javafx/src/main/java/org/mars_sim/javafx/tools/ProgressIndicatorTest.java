package org.mars_sim.javafx.tools;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ProgressIndicatorTest extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setScene(new Scene(createRoot(), 300, 200));
	    primaryStage.show();
	}

	private Parent createRoot() {
		StackPane stackPane = new StackPane();

		BorderPane controlsPane = new BorderPane();
		controlsPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		stackPane.getChildren().add(controlsPane);
		controlsPane.setCenter(new TableView<Void>());

		ProgressIndicator indicator = new ProgressIndicator();
		indicator.setMaxSize(120, 120);
		stackPane.getChildren().add(indicator);
		StackPane.setAlignment(indicator, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(indicator, new Insets(20));

		return stackPane;
	}

	public static void main(String[] args) {
		launch(args);
	}
}