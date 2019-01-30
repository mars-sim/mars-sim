package org.mars_sim.msp.ui.javafx.nsmenufx;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SampleMenuBar extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		StackPane root = new StackPane();
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.requestFocus();
		primaryStage.show();

		MenuToolkit tk = MenuToolkit.toolkit();

		MenuBar bar = new MenuBar();

		MenuItem item1 = new MenuItem("Item1");
		MenuItem item2 = new MenuItem("Item2");
		MenuItem item3 = new MenuItem("Mute");
		item3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Muted");
			}
		});

		MenuItem quit = tk.createQuitMenuItem("mars-sim");

		Menu menu2 = new Menu("Menu2");
		menu2.getItems().add(item2);
		
		Menu menu1 = new Menu("Menu1");
		menu1.getItems().addAll(item1, menu2, quit);

		Menu file = new Menu("File");
		file.getItems().addAll(item3);

		bar.getMenus().addAll(menu1, file);

		tk.setMenuBar(primaryStage, bar);

	}

	public static void main(String[] args) {
		launch(args);
	}

}
