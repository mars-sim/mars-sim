package org.mars_sim.msp.ui.javafx.nsmenufx;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RenameMenuItem extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		StackPane root = new StackPane();
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.requestFocus();
		primaryStage.show();

		// Get the toolkit
		MenuToolkit tk = MenuToolkit.toolkit();

		// Create the default Application menu
		Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("mars-sim");

		// Update the existing Application menu
		tk.setApplicationMenu(defaultApplicationMenu);

		// Since we now have a reference to the menu, we can rename items
		defaultApplicationMenu.getItems().get(1).setText("Hide all the otters");
	}

	public static void main(String[] args) {
		launch(args);
	}

}
