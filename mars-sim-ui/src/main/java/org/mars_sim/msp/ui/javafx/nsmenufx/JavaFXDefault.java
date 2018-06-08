package org.mars_sim.msp.ui.javafx.nsmenufx;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Locale;

public class JavaFXDefault extends Application {
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		MenuToolkit tk = MenuToolkit.toolkit(Locale.getDefault());
		tk.setApplicationMenu(tk.createDefaultApplicationMenu("mars-sim"));

		MenuBar menuBar = new MenuBar();
		menuBar.useSystemMenuBarProperty().set(true);

		Menu menu = new Menu("File");

		Menu help = new Menu("Help");
		
		MenuItem newItem = new MenuItem("New");
		
		menu.getItems().add(newItem);

		menuBar.getMenus().addAll(menu, help);

		primaryStage.setScene(new Scene(new Pane(menuBar)));
		primaryStage.setTitle("mars-sim");
		primaryStage.show();
	}
}
