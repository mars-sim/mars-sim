package org.mars_sim.msp.ui.javafx.nsmenufx;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Skeleton for Standard Macintosh Application See:
 * "OS X Human Interface Guidelines"
 */
public class StandardMacApp extends Application {
	static final String appName = "Standard";
	static final String mainWindowTitle = "Main";
	static final String childWindowTitle = "Child";

	static long counter = 1;

	@Override
	public void start(Stage primaryStage) throws Exception {
		StackPane root = new StackPane();
		Button button = new Button();
		button.setText("Create new Stage");
		button.setOnAction(action -> createNewStage());
		root.getChildren().add(button);

		primaryStage.setScene(new Scene(root, 300, 200));
		primaryStage.requestFocus();
		primaryStage.setTitle(mainWindowTitle);
		primaryStage.show();

		MenuToolkit tk = MenuToolkit.toolkit();

		MenuBar bar = new MenuBar();

		// Application Menu
		// TBD: services menu
		Menu appMenu = new Menu(appName); // Name for appMenu can't be set at
											// Runtime
		MenuItem aboutItem = tk.createAboutMenuItem(appName);
		MenuItem prefsItem = new MenuItem("Preferences...");
		appMenu.getItems().addAll(aboutItem, new SeparatorMenuItem(), prefsItem, new SeparatorMenuItem(),
				tk.createHideMenuItem(appName), tk.createHideOthersMenuItem(), tk.createUnhideAllMenuItem(),
				new SeparatorMenuItem(), tk.createQuitMenuItem(appName));

		// File Menu (items TBD)
		Menu fileMenu = new Menu("File");
		MenuItem newItem = new MenuItem("New...");
		fileMenu.getItems().addAll(newItem, new SeparatorMenuItem(), tk.createCloseWindowMenuItem(),
				new SeparatorMenuItem(), new MenuItem("TBD"));

		// Edit (items TBD)
		Menu editMenu = new Menu("Edit");
		editMenu.getItems().addAll(new MenuItem("TBD"));

		// Format (items TBD)
		Menu formatMenu = new Menu("Format");
		formatMenu.getItems().addAll(new MenuItem("TBD"));

		// View Menu (items TBD)
		Menu viewMenu = new Menu("View");
		viewMenu.getItems().addAll(new MenuItem("TBD"));

		// Window Menu
		// TBD standard window menu items
		Menu windowMenu = new Menu("Window");
		windowMenu.getItems().addAll(tk.createMinimizeMenuItem(), tk.createZoomMenuItem(), tk.createCycleWindowsItem(),
				new SeparatorMenuItem(), tk.createBringAllToFrontItem());

		// Help Menu (items TBD)
		Menu helpMenu = new Menu("Help");
		helpMenu.getItems().addAll(new MenuItem("TBD"));

		bar.getMenus().addAll(appMenu, fileMenu, editMenu, formatMenu, viewMenu, windowMenu, helpMenu);

		tk.autoAddWindowMenuItems(windowMenu);
		tk.setGlobalMenuBar(bar);
	}

	private void createNewStage() {
		Stage stage = new Stage();
		stage.setScene(new Scene(new StackPane()));
		stage.setTitle(childWindowTitle + " " + (counter++));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
