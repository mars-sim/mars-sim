/**
 * Mars Simulation Project
 * ScenarioConfigEditorFX.java
 * @version 3.1.0 2017-09-14
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.config;

import insidefx.undecorator.UndecoratorScene;

import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;
import org.mars_sim.msp.ui.javafx.networking.MultiplayerClient;
import org.mars_sim.network.SettlementRegistry;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Cursor;
import javafx.scene.Node;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * ScenarioConfigEditorFX allows users to configure the types of settlements
 * available at the start of the simulation.
 */
public class ScenarioConfigEditorFX {

	/** default logger. */
	private static Logger logger = Logger.getLogger(ScenarioConfigEditorFX.class.getName());
	// private static final char CVS_SEPARADOR = new
	// DecimalFormatSymbols().getPatternSeparator();
	// private static final int HORIZONTAL_SIZE = 1024;

	// Data members.
	private int clientID = 0;
	// private double orgSceneX, orgSceneY;
	// private double orgTranslateX, orgTranslateY;

	private boolean isExit = false;
	private boolean isShowingDialog = false;
	private boolean hasError;
	private boolean hasSettlement;
	private boolean isCrewEditorOpen = false;
	private boolean isCommanderMode;

	private String playerName;
	private String gameMode;

	private AnchorPane anchorpane;
	private StackPane stackPane;

	private JFXButton startButton;
	private JFXButton addButton;
	private JFXButton removeButton;
	private JFXButton undoButton;
	private JFXButton crewButton;

	private Label errorLabel;
	private Label titleLabel;
	// private Label gameModeLabel;
	private Label clientIDLabel;
	private Label playerLabel;

	private TilePane titlePane;
	private VBox topVB;
	private BorderPane borderAll;
	private Parent parent;

	private Stage stage;
//	private Stage cstage;

	private SimulationConfig config;
	private MainMenu mainMenu;
	private CrewEditorFX crewEditorFX;
	private MultiplayerClient multiplayerClient;
	private SettlementConfig settlementConfig;
	private MainScene mainScene;
	private TableView<?> tableView;
	private TableViewCombo tableViewCombo;

	private List<SettlementRegistry> settlementList;

	private Simulation sim = Simulation.instance();

	/**
	 * Constructor
	 * 
	 * @param mainMenu
	 * @param config   the simulation configuration.
	 */
	public ScenarioConfigEditorFX(MainMenu mainMenu, boolean isCommanderMode) { // {
		// logger.config("ScenarioConfigEditorFX's constructor is on " +
		// Thread.currentThread().getName() );

		// Initialize data members.
		this.config = SimulationConfig.instance();
		this.mainMenu = mainMenu;
		this.mainScene = mainMenu.getMainScene();
		this.isCommanderMode = isCommanderMode;

		hasError = false;

		settlementConfig = config.getSettlementConfiguration();

		if (mainMenu.getMultiplayerMode() != null) {
			// multiplayerClient =
			// mainMenu.getMultiplayerMode().getMultiplayerClient();
			multiplayerClient = MultiplayerClient.getInstance();
			// multiplayerClient.sendRegister(); // not needed. already
			// registered
			clientID = multiplayerClient.getClientID();
			playerName = multiplayerClient.getPlayerName();
			if (multiplayerClient.getNumSettlement() > 0)
				hasSettlement = true;
			settlementList = multiplayerClient.getSettlementRegistryList();
			gameMode = "Simulation Mode : Multi-Player";
		} else {
			gameMode = "Simulation Mode : Single-Player";
			hasSettlement = false;
			playerName = "Default";
		}

		createGUI();

	}

	public void createGUI() {
		Platform.setImplicitExit(false);
//		try { 
//			UIManager.setLookAndFeel(new NimRODLookAndFeel()); 
//		}
//		catch(Exception ex) { 
//			logger.log(Level.WARNING,
//			Msg.getString("MainWindow.log.lookAndFeelError"), ex); //$NON-NLS-1$
//		}
		FXMLLoader fxmlLoader = null;

		try {
			fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("/fxui/fxml/ConfigEditorFX.fxml")); // ClientArea.fxml")); //
																								// //
			fxmlLoader.setController(this);
			parent = (Parent) fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Platform.runLater(() -> {

			stage = new Stage();
			// 2016-02-07 Added calling setMonitor()
			mainMenu.setMonitor(stage);
			stage.setTitle("Mars Simulation Project - Configuration Editor");
			stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));// toString()));
			// stage.getIcons().add(new
			// Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));

			Region root = (Region) parent;// stackPane;
			// The Undecorator as a Scene
			final UndecoratorScene undecoratorScene = new UndecoratorScene(stage, root);

			// Overrides defaults
			undecoratorScene.addStylesheet("/fxui/css/config/undecorator.css");
			undecoratorScene.getStylesheets().add("/fxui/css/config/configEditorFXOrange.css");

			// Enable fade transition
			undecoratorScene.setFadeInTransition();

			// Optional: Enable this node to drag the stage
			// By default the root argument of Undecorator is set as draggable
			// Node node = root.lookup("#draggableNode"); // Enable TabPane to
			// drag the stage
			// undecoratorScene.setAsStageDraggable(stage, tabPane);

			/*
			 * Fade out transition on window closing request
			 */
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent we) {
					we.consume(); // Do not hide yet
					undecoratorScene.setFadeOutTransition();
				}
			});

			anchorpane = null;
			if (parent.lookup("#anchorRoot") == null)
				System.out.println("Warning: anchorRoot is not found");
			else
				anchorpane = ((AnchorPane) parent.lookup("#anchorRoot"));

			// List should stretch as anchorpane is resized
			BorderPane bp = createEditorFrame();
			stackPane = new StackPane(bp);
			stackPane.setAlignment(Pos.CENTER);
			AnchorPane.setTopAnchor(stackPane, 5.0);
			AnchorPane.setLeftAnchor(stackPane, 5.0);
			AnchorPane.setRightAnchor(stackPane, 5.0);
			anchorpane.getChildren().add(stackPane);

			// scene = new Scene(undecorator);
			// undecorator.setOnMousePressed(buttonOnMousePressedEventHandler);

			// Transparent scene and stage
			// undecoratorScene.setFill(Color.TRANSPARENT); // needed to
			// eliminate the white border

			// stage.initStyle(StageStyle.TRANSPARENT);
			// stage.setMinWidth(undecorator.getMinWidth());
			// stage.setMinHeight(undecorator.getMinHeight());
			stage.centerOnScreen();
			stage.setResizable(false);
			stage.setFullScreen(false);
			stage.setScene(undecoratorScene);
			stage.sizeToScene();
			stage.toFront();
			stage.show();

			stage.setOnCloseRequest(e -> {
				if (!isShowingDialog) {
					dialogOnExit(stackPane);
				}
				// need e.consume() in order to call setFadeOutTransition() below
				e.consume();

				if (isExit) {
					borderAll.setOpacity(0);
					// undecorator.setFadeOutTransition();
					closeWindow();
					Platform.exit();
				}
			});
			// bar.valueProperty().addListener(this::scrolled);
		});

	}

	private BorderPane createEditorFrame() {
		borderAll = new BorderPane();
		borderAll.setPadding(new Insets(0, 15, 0, 15));

		topVB = new VBox();
		topVB.setAlignment(Pos.CENTER);
		topVB.setPadding(new Insets(0, 5, 5, 5));
		// gameModeLabel = new Label(gameMode);
		// gameModeLabel.setId("gameModeLabel");

		// Create the title label.
		if (multiplayerClient != null) {
			clientIDLabel = new Label("Client ID : " + clientID);
			playerLabel = new Label("Player : " + playerName);
		} else {
			clientIDLabel = new Label();
			playerLabel = new Label();
		}
		clientIDLabel.setId("clientIDLabel");
		playerLabel.setId("playerLabel");

		titleLabel = new Label(Msg.getString("SimulationConfigEditor.chooseSettlements")); //$NON-NLS-1$
		titleLabel.setId("titleLabel");
		titleLabel.setAlignment(Pos.CENTER_LEFT);
		// titleLabel.setPadding(new Insets(5, 10, 5, 10));
		// titlePane = new TilePane(Orientation.VERTICAL);
		titlePane = new TilePane(Orientation.HORIZONTAL);
		titlePane.setMaxWidth(600);
		titlePane.setPadding(new Insets(3, 3, 3, 3));
		titlePane.setHgap(2.0);
		titlePane.setVgap(2.0);
		// if (multiplayerClient != null) {
		// titlePane.getChildren().addAll(clientIDLabel, titleLabel);
		// clientIDLabel.setAlignment(Pos.TOP_LEFT);
		// }
		// else
		titlePane.getChildren().addAll(titleLabel);
		titlePane.setAlignment(Pos.TOP_LEFT);
		// titleLabel.setAlignment(Pos.CENTER);
		// gameModeLabel.setAlignment(Pos.TOP_LEFT);

		HBox topHB = new HBox(50);
		topHB.setPadding(new Insets(5, 10, 5, 10));
		topHB.setPrefWidth(400);
		topHB.getChildren().addAll(playerLabel, clientIDLabel);
		topHB.setAlignment(Pos.CENTER);
		topVB.getChildren().addAll(topHB, titleLabel); // gameModeLabel
		borderAll.setTop(topVB);

		// 2016-08-09 Added TableViewCombo, TableView
		tableViewCombo = new TableViewCombo(this);
		tableView = tableViewCombo.createGUI();
		tableView.setMaxHeight(200);
		tableView.setPrefHeight(200);
		borderAll.setCenter(tableView);// bar);

		// SpreadsheetView ssv = settlementTableView.createGUI();
		// ssv.setMaxHeight(200);
		// ssv.setPrefHeight(200);
		// borderAll.setCenter(ssv);

		// Create configuration button outer panel.
		BorderPane borderButtons = new BorderPane();
		borderAll.setLeft(borderButtons);

		// Create configuration button inner top panel.
		VBox vbTopLeft = new VBox();
		borderButtons.setTop(vbTopLeft);
		vbTopLeft.setSpacing(10);
		vbTopLeft.setPadding(new Insets(5, 5, 5, 5));

		// Create add settlement button.
		// addButton = new JFXButton("+");//
		// Msg.getString("SimulationConfigEditor.button.add")); //$NON-NLS-1$
		addButton = new JFXButton();
		addButton.setGraphic(
				new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/button-add.png"))));
		setMouseCursor(addButton);
		addButton.getStyleClass().add("button-small");
		setQuickToolTip(addButton, Msg.getString("SimulationConfigEditor.tooltip.add")); //$NON-NLS-1$
		addButton.setOnAction((event) -> {
			addNewSettlement();
		});
		vbTopLeft.getChildren().addAll(new Label(), new Label());
		vbTopLeft.getChildren().add(addButton);

		// Create remove settlement button.
		removeButton = new JFXButton();// "-");// Msg.getString("SimulationConfigEditor.button.remove")); //$NON-NLS-1$
		removeButton.setGraphic(
				new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/button-delete.png"))));
		setMouseCursor(removeButton);
		setQuickToolTip(removeButton, Msg.getString("SimulationConfigEditor.tooltip.remove")); //$NON-NLS-1$
		// removeButton.setId("removeButton");
		removeButton.getStyleClass().add("button-small");

		removeButton.setOnAction((event) -> {
			// ObservableList list =
			// settlementTableView.getTableView().getSelectionModel().getSelectedIndices();
			int index = -1;
			index = tableViewCombo.getTableView().getSelectionModel().getSelectedIndex();
			// System.out.println("index is " + index);

			if (index > -1) {
				boolean isYes = confirmDeleteDialog("Removing settlement", "Are you sure you want to do this?");
				if (isYes) {
					// Object o =
					// settlementTableView.getTableView().getSelectionModel().getSelectedItem();
					// settlementTableView.getTableView().getSelectionModel().clearSelection();
					removeSelectedSettlements(index);
					// mainMenu.getStage().setIconified(true);
				}
			}
		});
		vbTopLeft.getChildren().add(removeButton);

		// Create refresh/defaultButton button.
		undoButton = new JFXButton();// Msg.getString("SimulationConfigEditor.button.default")); //$NON-NLS-1$
		undoButton.setGraphic(
				new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/red_undo_32.png"))));// button-undo.png"))));
		undoButton.getStyleClass().add("button-mid");// -sign");
		setMouseCursor(undoButton);
		setQuickToolTip(undoButton, Msg.getString("SimulationConfigEditor.tooltip.undo"));//$NON-NLS-1$

		undoButton.setOnAction((event) -> {
			if (multiplayerClient != null && hasSettlement) {
				boolean isYes = confirmDeleteDialog("Undo ALL changes", "Proceed ?");
				if (isYes)
					;// setExistingSettlements();
			} else {
				boolean isYes = confirmDeleteDialog("Undo ALL changes and refresh", "Proceed ?");
				if (isYes)
					setDefaultSettlements();
			}
			// mainMenu.getStage().setIconified(true);
		});
		// vbCenter.getChildren().add(defaultButton);
		// vbTopLeft.getChildren().add(refreshDefaultButton);

		// Added Edit Alpha Crew button.
		crewButton = new JFXButton();// Msg.getString("SimulationConfigEditor.button.crewEditor")); //$NON-NLS-1$
		crewButton
				.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/people32.png"))));
		setMouseCursor(crewButton);
		crewButton.getStyleClass().add("button-mid");// -sign");//raised");
		setQuickToolTip(crewButton, Msg.getString("SimulationConfigEditor.tooltip.crewEditor")); //$NON-NLS-1$

		// alphaButton.setStyle("-fx-font: 16 arial; -fx-base: #cce6ff;");
		crewButton.setOnAction((event) -> {
			editCrewProfile("alpha");
		});
		// bottomButtonPanel.getChildren().add(alphaButton);
		// vbTopLeft.getChildren().add(crewButton);

		// Create configuration button inner bottom panel.
		VBox vbCenter = new VBox();
		vbCenter.setSpacing(10);
		vbCenter.setPadding(new Insets(0, 10, 0, 10));
		borderButtons.setBottom(vbCenter);

		// Create bottom panel.
		BorderPane bottomPanel = new BorderPane();
		borderAll.setBottom(bottomPanel);

		// Create error label.
		errorLabel = new Label(" "); //$NON-NLS-1$
		errorLabel.setAlignment(Pos.CENTER);
		// errorLabel.set//setColor(Color.RED);
		errorLabel.setStyle("-fx-color: red; -fx-base: #ff5400;"); // -fx-font: 15 arial;
		bottomPanel.setTop(errorLabel);

		// Create the bottom button panel.
		// HBox bottomButtonPanel = new HBox();
		// bottomPanel.setBottom(bottomButtonPanel);

		// Create the start button.
		startButton = new JFXButton();// " " + Msg.getString("SimulationConfigEditor.button.newSim") + " ");
										// //$NON-NLS-1$
		// startButton = new JFXButton();
		// Icon value = new Icon("HEART");
		// value.setPadding(new Insets(10));
		startButton.setGraphic(
				new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/round_play_48.png"))));
		startButton.getStyleClass().add("button-large");
		setMouseCursor(startButton);
		setQuickToolTip(startButton, Msg.getString("SimulationConfigEditor.tooltip.newSim")); //$NON-NLS-1$
		startButton.setId("startButton");
		// Made "Enter" key to work like the space bar for firing the
		// button on focus
		startButton.defaultButtonProperty().bind(startButton.focusedProperty());
		startButton.requestFocus();
		startButton.setOnAction((event) -> {
			if (crewEditorFX != null) {
				if (!crewEditorFX.isGoodToGo()) {
					startButton.setDisable(true);
					// System.out.println("start button is set to disable");
					Alert alert = new Alert(AlertType.ERROR, "Please fix the invalid name(s) in the Crew Editor.");
					alert.initOwner(stage);
					alert.showAndWait();
					event.consume();
					return;
				} else {

				}
			}

			// Make sure any editing cell is completed, then check if error.
			// if (tableCellEditor != null) {
			// tableCellEditor.stopCellEditing();
			// }

			// checkForErrors();
			// System.out.println("hasError is " + hasError);

			if (!hasError) {
				// create wait indicators
				Platform.runLater(() -> {
					mainScene.createLoadingIndicator();
//					mainScene.showWaitStage(MainScene.LOADING);
				});

				setConfiguration();

				CompletableFuture.supplyAsync(() -> submitTask());

				// Lookup the thready service in the service registry
				// ThreadSynchronize threadSync =
				// ServiceUtils.getService(ThreadSynchronize.class).get();

				// CompletableFuture.runAsync(() -> submitTask()).thenAcceptAsync( (Consumer<?
				// super Void>) threadSync.wrap(() -> {
				// mainScene.createIndicator();
				// mainScene.showWaitStage(MainScene.LOADING);
				// }));

				// threadSync.scheduleExecution(200, runWait()).thenAcceptAsync( d ->
				// submitTask());

				closeWindow();

				// scene.setCursor(Cursor.DEFAULT); //Change cursor to default style

			} // end of if (!hasError)

		});

		addButton.setMaxWidth(Double.MAX_VALUE);
		removeButton.setMaxWidth(Double.MAX_VALUE);
		undoButton.setMaxWidth(Double.MAX_VALUE);
		crewButton.setMaxWidth(Double.MAX_VALUE);
		startButton.setMaxWidth(Double.MAX_VALUE);

		TilePane tileButtons = new TilePane(Orientation.HORIZONTAL);
		tileButtons.setPadding(new Insets(0, 5, 0, 5));
		tileButtons.setHgap(200.0);
		tileButtons.setVgap(3.0);
		tileButtons.getChildren().addAll(undoButton, startButton, crewButton);
		tileButtons.setAlignment(Pos.CENTER);

		bottomPanel.setBottom(tileButtons);

		// pane.getChildren().add(borderAll);
		return borderAll;
		// return borderAll;
	}

	public Runnable runWait() {
		sim.getSimExecutor().execute(new WaitTask());
		return null;
	}

	public class WaitTask implements Runnable {
		public void run() {
			mainScene.createLoadingIndicator();
//			mainScene.showWaitStage(MainScene.LOADING);
		}
	}

	public int submitTask() {
		sim.getSimExecutor().execute(new SimulationTask());
		return 1;
	}

	public class SimulationTask implements Runnable {
		public void run() {

			// if commander mode GUI hasn't been used, Start interactive terminal
//			if (!isCommanderMode) {
//				// Alert the user to see the interactive terminal 
//				logger.config("Please proceed to answering the question in the popped-up console.");
//				sim.getTerm().startCommanderMode();
//			}
			
			Simulation.createNewSimulation(-1);

			sim.start(false);

			Platform.runLater(() -> {
				mainScene.finalizeMainScene();
			});

			if (multiplayerClient != null)
				multiplayerClient.prepareListeners();

			// Load the menu choice
			sim.getTerm().loadTerminalMenu();
		}
	}

	/**
	 * Swaps the mouse cursor type between DEFAULT and HAND
	 * 
	 * @param node
	 */
	public void setMouseCursor(Node node) {
		node.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
			node.setCursor(Cursor.DEFAULT);
		});

		node.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
			node.setCursor(Cursor.HAND);
		});
	}

	/**
	 * Adds a new settlement with default values.
	 */
	private void addNewSettlement() {
		// SettlementInfo settlement = determineNewSettlementConfiguration();
		// settlementTableView.addSettlement(settlement);
		SettlementBase base = determineNewSettlementConfiguration();
		tableViewCombo.addSettlement(base);
		updateSettlementNames();
	}

	/**
	 * Removes the settlement selected on the table.
	 */
	private void removeSelectedSettlements(int i) {
		// settlementTableModel.removeSettlements(settlementTable.getSelectedRows());
		tableViewCombo.removeSettlement(i);
		updateSettlementNames();
	}

	public void updateSettlementNames() {
		if (crewEditorFX != null) {
			crewEditorFX.updateSettlementNames();
			// } else if (!isCrewEditorOpen) {
			// crewEditorFX.updateSettlementNames();
		}
	}

	/**
	 * Edits team roster.
	 */
	private void editCrewProfile(String crew) {
		if (crewEditorFX == null) {
			crewEditorFX = new CrewEditorFX(config, this);
			// System.out.println("new CrewEditorFX()");
		} else if (!isCrewEditorOpen) {
			// System.out.println("calling crewEditorFX.createGUI()");
			// crewEditorFX.createGUI();
			crewEditorFX.getStage().show();
		} else
			crewEditorFX.getStage().requestFocus();

	}

	public void setCrewEditorOpen(boolean value) {
		isCrewEditorOpen = value;
		startButton.setDisable(false);
	}

	/**
	 * Sets the default settlements from the loaded configuration.
	 */
	private void setDefaultSettlements() {
		// settlementTableModel.loadDefaultSettlements();
		// tableViewCombo.loadDefaultSettlements();
		tableView = tableViewCombo.createGUI();
		tableView.setMaxHeight(200);
		tableView.setPrefHeight(200);
		borderAll.setCenter(tableView);// bar);
		updateSettlementNames();
	}

	/**
	 * Set the simulation configuration based on dialog choices.
	 */
	private void setConfiguration() {
		// Clear configuration settlements.
		settlementConfig.clearInitialSettlements();
		// Add configuration settlements from table data.
		for (int x = 0; x < tableViewCombo.getRowCount(); x++) {
			if (multiplayerClient != null) {
				if (hasSettlement && x < settlementList.size())
					; // do nothing to the existing settlements from other clients
				else
					createSettlement(x);
			} else
				createSettlement(x);
		}
	}

	/**
	 * Creates a settlement
	 */
	private void createSettlement(int x) {

		String name = tableViewCombo.getAllData().get(x).getName().toString();
		String template = tableViewCombo.getAllData().get(x).getTemplate().toString();
		String population = tableViewCombo.getAllData().get(x).getSettler().toString();
		String robot = tableViewCombo.getAllData().get(x).getBot().toString();
		String sponsor = tableViewCombo.getAllData().get(x).getSponsor().toString();
		String latitude = tableViewCombo.getAllData().get(x).getLatitude().toString();
		String longitude = tableViewCombo.getAllData().get(x).getLongitude().toString();
		int populationNum = Integer.parseInt(population);
		int numOfRobots = Integer.parseInt(robot);
		double lat = SettlementRegistry.convertLatLong2Double(latitude);
		double lo = SettlementRegistry.convertLatLong2Double(longitude);

		settlementConfig.addInitialSettlement(name, template, populationNum, numOfRobots, sponsor, latitude, longitude);
		// Send the newly created settlement to host server
		if (multiplayerClient != null) {
			// create an instance of SettlementRegistry
			SettlementRegistry newS = new SettlementRegistry(playerName, clientID, name, template, populationNum,
					numOfRobots, sponsor, lat, lo);
			multiplayerClient.sendNew(newS);
		}
	}

//	/**
//	 * Close and dispose dialog window.
//	 */
//	private int waitLoading() {
//
//		StackPane stackPane = new StackPane();
//		MaskerPane indicator = new MaskerPane();
//		indicator.setScaleX(1.2);
//		indicator.setScaleY(1.2);
//		stackPane.getChildren().add(indicator);
//		StackPane.setAlignment(indicator, Pos.CENTER);
//		stackPane.setBackground(Background.EMPTY);
//
//		// stage.hide();
//		Scene scene = new Scene(stackPane);// , 200, 200);
//		scene.setFill(Color.TRANSPARENT);
//
//		// cstage = stage;
//		cstage.initStyle(StageStyle.TRANSPARENT);
//		cstage.setScene(scene);
//
//		StartUpLocation startUpLoc = new StartUpLocation(borderAll.getPrefWidth(), borderAll.getPrefHeight());
//		double xPos = startUpLoc.getXPos();
//		double yPos = startUpLoc.getYPos();
//
//		if (xPos != 0 && yPos != 0) {
//			cstage.setX(xPos);
//			cstage.setY(yPos);
//			cstage.centerOnScreen();
//			// System.out.println(" x : " + xPos + " y : " + yPos);
//		} else {
//			cstage.centerOnScreen();
//			// System.out.println("calling centerOnScreen()");
//			// System.out.println(" x : " + xPos + " y : " + yPos);
//		}
//		cstage.show();
//		cstage.requestFocus();
//		stage.hide();
//		return 1;
//	}

	/**
	 * Close and dispose dialog window.
	 */
	private void closeWindow() {
		if (crewEditorFX != null || isCrewEditorOpen) {// crewEditorFX.getStage() != null) {
			crewEditorFX.getStage().hide();
			crewEditorFX.getStage().close();
		}
		stage.hide();
		stage.close();

	}

	/**
	 * Determines the configuration of a new settlement.
	 * 
	 * @return SettlementBase configuration.
	 */
	private SettlementBase determineNewSettlementConfiguration() {
		String template = determineNewSettlementTemplate();
		String sponsor = determineNewSettlementSponsor();
		SettlementBase base = new SettlementBase(
				// playerName,
				determineNewSettlementName(sponsor), template, determineNewSettlementPopulation(template),
				determineNewSettlementNumOfRobots(template), sponsor, determineNewSettlementLatitude(),
				determineNewSettlementLongitude());

		return base;
	}

	/**
	 * Determines a new settlement's name.
	 * 
	 * @return name.
	 */
	private String determineNewSettlementName(String sponsor) {
		String result = null;

		// Try to find unique name in configured settlement name list.
		// Randomly shuffle settlement name list first.
		// SettlementConfig settlementConfig = config.getSettlementConfiguration();
		List<String> settlementNames = settlementConfig.getSettlementNameList(sponsor);
		Collections.shuffle(settlementNames);
		Iterator<String> i = settlementNames.iterator();
		while (i.hasNext()) {
			String name = i.next();
			// Make sure settlement name isn't already being used in table.
			boolean nameUsed = false;
			for (int x = 0; x < tableViewCombo.getRowCount(); x++) {
				// if (name.equals(settlementTableModel.getValueAt(x,
				// SettlementTable.COLUMN_SETTLEMENT_NAME))) {
				if (name.equals(tableViewCombo.getAllData().get(x).getName())) {
					nameUsed = true;
				}
			}

			// TODO: check if the name is being used in the host server's settlement
			// registry or not
			// If not being used already, use this settlement name.
			if (!nameUsed) {
				result = name;
				break;
			}
		}

		// If no name found, create numbered settlement name: "Settlement 1",
		// "Settlement 2", etc.
		int count = 1;
		while (result == null) {
			String name = Msg.getString("SimulationConfigEditor.settlement", //$NON-NLS-1$
					Integer.toString(count));

			// Make sure settlement name isn't already being used in table.
			boolean nameUsed = false;
			for (int x = 0; x < tableViewCombo.getRowCount(); x++) {
				// if (name.equals(settlementTableModel.getValueAt(x,
				// SettlementTable.COLUMN_SETTLEMENT_NAME))) {
				if (name.equals(tableViewCombo.getAllData().get(x).getName())) {
					nameUsed = true;
				}
			}

			// TODO: check if the name is being used in the host server's
			// settlement registry or not

			// If not being used already, use this settlement name.
			if (!nameUsed) {
				result = name;
			}
			count++;
		}

		return result;
	}

	/**
	 * Determines a new settlement's template.
	 * 
	 * @return template name.
	 */
	private String determineNewSettlementTemplate() {
		String result = null;

		// SettlementConfig settlementConfig = config.getSettlementConfiguration();
		List<SettlementTemplate> templates = settlementConfig.getSettlementTemplates();
		if (templates.size() > 0) {
			int index = RandomUtil.getRandomInt(templates.size() - 1);
			result = templates.get(index).getTemplateName();
		} else
			logger.log(Level.WARNING, Msg.getString("SimulationConfigEditor.log.settlementTemplateNotFound")); //$NON-NLS-1$

		return result;
	}

	/**
	 * Determines the new settlement population.
	 * 
	 * @param templateName the settlement template name.
	 * @return the new population number.
	 */
	public String determineNewSettlementPopulation(String templateName) {

		String result = "0"; //$NON-NLS-1$

		if (templateName != null) {
			// SettlementConfig settlementConfig = config.getSettlementConfiguration();
			Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
			while (i.hasNext()) {
				SettlementTemplate template = i.next();
				if (template.getTemplateName().equals(templateName)) {
					result = Integer.toString(template.getDefaultPopulation());
				}
			}
		}

		return result;
	}

	/**
	 * Determines the new settlement number of robots.
	 * 
	 * @param templateName the settlement template name.
	 * @return number of robots.
	 */
	public String determineNewSettlementNumOfRobots(String templateName) {
		String result = "0"; //$NON-NLS-1$
		if (templateName != null) {
			// SettlementConfig settlementConfig = config.getSettlementConfiguration();
			Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
			while (i.hasNext()) {
				SettlementTemplate template = i.next();
				if (template.getTemplateName().equals(templateName)) {
					result = Integer.toString(template.getDefaultNumOfRobots());
					// System.out.println("SimulationConfigEditor :
					// determineNewSettlementNumOfRobots() : result is " +
					// result);
				}
			}
		}

		return result;
	}

	/**
	 * Determines a new settlement's sponsor.
	 * 
	 * @return sponsor name.
	 */
	private String determineNewSettlementSponsor() {
		String result = "Mars Society (MS)";
		return result;
	}

	/**
	 * Determines a new settlement's latitude.
	 *
	 * @return latitude string.
	 */
	private String determineNewSettlementLatitude() {

		// TODO: check if there is an existing settlement with the same latitude
		// (within 1 decimal places) at this location from the host server's
		// settlement registry
		// note: d = 6779km. each one degree is 59.1579km. each .1 degree is
		// 5.91579 km apart.
		// e.g. if an existing town is at (0.1, 0.1), one cannot "reuse" these
		// coordinates again. He can only create a new town at (0.1, 0.1)

		double phi = Coordinates.getRandomLatitude();
		String formattedLatitude = Coordinates.getFormattedLatitudeString(phi);
		int degreeIndex = formattedLatitude.indexOf(Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		return formattedLatitude.substring(0, degreeIndex)// + " "
				+ formattedLatitude.substring(degreeIndex + 1, formattedLatitude.length());
	}

	/**
	 * Determines a new settlement's longitude.
	 *
	 * @return longitude string.
	 */
	private String determineNewSettlementLongitude() {

		// TODO: check if there is an existing settlement with the same latitude
		// (within 1 decimal places) at this location from the host server's
		// settlement registry
		// note: d = 6779km. each one degree is 59.1579km. each .1 degree is
		// 5.91579 km apart.
		// e.g. if an existing town is at (0.1, 0.1), one cannot "reuse" these
		// coordinates again. He can only create a new town at (0.1, 0.1)

		double theta = Coordinates.getRandomLongitude();
		String formattedLongitude = Coordinates.getFormattedLongitudeString(theta);
		int degreeIndex = formattedLongitude.indexOf(Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		return formattedLongitude.substring(0, degreeIndex)// + " "
				+ formattedLongitude.substring(degreeIndex + 1, formattedLongitude.length());
	}

	public boolean confirmDeleteDialog(String header, String text) {
		Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.initOwner(stage);
		dialog.setHeaderText(header);
		dialog.setContentText(text);
		dialog.getDialogPane().setPrefSize(300, 180);
		// ButtonType buttonTypeYes = new ButtonType("Yes");
		// ButtonType buttonTypeNo = new ButtonType("No");
		// dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
		dialog.getButtonTypes().clear();
		dialog.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
		// Deactivate Defaultbehavior for yes-Button:
		Button yesButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.YES);
		yesButton.setDefaultButton(false);
		// Activate Defaultbehavior for no-Button:
		Button noButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.NO);
		noButton.setDefaultButton(true);
		final Optional<ButtonType> result = dialog.showAndWait();
		// return result.get() == buttonTypeYes;
		return result.get() == ButtonType.YES;
	}

	public boolean getHasSettlement() {
		return hasSettlement;
	}

	public void setHasSettlement(boolean value) {
		hasSettlement = value;
	}

	public MultiplayerClient getMultiplayerClient() {
		return multiplayerClient;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String value) {
		playerName = value;
	}

	public Button getUndoButton() {
		return undoButton;
	}

	public Button getStartButton() {
		return startButton;
	}

	public void disableStartButton() {
		startButton.setDisable(true);
	}

	public Label getErrorLabel() {
		return errorLabel;
	}

	public Boolean getHasError() {
		return hasError;
	}

	public void setHasError(boolean value) {
		hasError = value;
	}

	/*
	 * public JScrollPane getSettlementScrollPane() { return settlementScrollPane; }
	 */
	public List<SettlementRegistry> getSettlementList() {
		return settlementList;
	}
	/*
	 * public SettlementTableModel getSettlementTableModel() { return
	 * settlementTableModel; }
	 */

	// public SettlementTableView getSettlementTableView() {
	// return tableViewCombo;
	// }

	public TableViewCombo getTableViewCombo() {
		return tableViewCombo;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

	public int getRowCount() {
		return tableViewCombo.getRowCount();
	}

	private ScrollBar getVerticalScrollbar(TableView<?> table) {
		ScrollBar result = null;
		for (Node n : table.lookupAll(".scroll-bar")) {
			if (n instanceof ScrollBar) {
				ScrollBar bar = (ScrollBar) n;
				if (bar.getOrientation().equals(Orientation.VERTICAL)) {
					result = bar;
				}
			}
		}
		return result;
	}

	void scrolled(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		double value = newValue.doubleValue();
		// System.out.println("Scrolled to " + value);
		ScrollBar bar = getVerticalScrollbar(tableView);
		if (value == bar.getMax()) {
			// System.out.println("Adding new persons.");
			// double targetValue = value * items.size();
			// addPersons();
			// bar.setValue(targetValue / items.size());
		}
	}

	/**
	 * Sets an edit-check error.
	 * 
	 * @param errorString the error description.
	 */
	private void setError(String errorString) {
		// Platform.runLater is needed to switch from Swing EDT to JavaFX Thread
		// Platform.runLater(() -> {
		if (!getHasError()) {
			setHasError(true);
			getErrorLabel().setText("\t\t" + errorString);
			// errorLabel.setStyle("-fx-font-color:red;");
			getErrorLabel().setTextFill(Color.RED);
			getStartButton().setDisable(true);
		}
		// });
	}

	/**
	 * Clears all edit-check errors.
	 */
	private void clearError() {
		// Platform.runLater is needed to switch from Swing EDT to JavaFX Thread
		// Platform.runLater(() -> {
		setHasError(false);
		getErrorLabel().setText(""); //$NON-NLS-1$
		getErrorLabel().setTextFill(Color.BLACK);
		getStartButton().setDisable(false);
		// });
	}

	/**
	 * Check for errors in table settlement values.
	 */
	public void checkForErrors() {
		// System.out.println("checkForErrors"); // runs only when a user click on a
		// cell
		// checkNumExistingSettlement();
		clearError();

		// TODO: in multiplayer mode, check to ensure the latitude/longitude has NOT
		// been chosen already in the table
		// by another settlement registered by the host server

		// TODO: incorporate checking for user locale and its decimal separation symbol
		// (. or ,)

		// TODO: use decimal separator as defined by user locale
		// char decimalPoint = format.getDecimalFormatSymbols().getDecimalSeparator();

		checkLatLong();

		Iterator<SettlementBase> i = tableViewCombo.getSettlementBase().iterator();
		while (i.hasNext()) {
			SettlementBase sb = i.next();

			// Check that settlement name is valid.
			if ((sb.getName().trim() == null) || (sb.getName().trim().isEmpty()) || (sb.getName().length() < 2)) {
				setError(Msg.getString("SimulationConfigEditor.error.nameMissing")); //$NON-NLS-1$
			}

			// Check if population is valid.
			if ((sb.getSettler().trim() == null) || (sb.getSettler().trim().isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.populationMissing")); //$NON-NLS-1$
			} else {
				try {
					int popInt = Integer.parseInt(sb.getSettler().trim());
					if (popInt < 0) {
						setError(Msg.getString("SimulationConfigEditor.error.populationTooFew")); //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					setError(Msg.getString("SimulationConfigEditor.error.populationInvalid")); //$NON-NLS-1$
					e.printStackTrace();
				}
			}

			// Check if number of robots is valid.
			if ((sb.getBot().trim() == null) || (sb.getBot().trim().isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsMissing")); //$NON-NLS-1$
			} else {
				try {
					int num = Integer.parseInt(sb.getBot().trim());
					if (num < 0) {
						setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsTooFew")); //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsInvalid")); //$NON-NLS-1$
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Checks for the validity of the latitude and longitude
	 */
	public void checkLatLong() {
		try {
			boolean repeated = false;
			int size = getRowCount();
			for (int x = 0; x < size; x++) {

				/// 2017-03-29 Add checking for comma as well
				boolean isDot_lat = false;
				boolean isComma_lat = false;
				boolean isDot_lon = false;
				boolean isComma_lon = false;

				// CHECK LATITUDE
				String latStr = tableViewCombo.getAllData().get(x).getLatitude().toString().trim().toUpperCase();
				;

				// check if it's empty or having a length less than 2 characters.
				if (latStr == null || latStr.length() < 2) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
					return;
				}

				// check if the last character is a digit, if digit, setError
				if (Character.isDigit(latStr.charAt(latStr.length() - 1))) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeEndWith", //$NON-NLS-1$
							Msg.getString("direction.northShort"), //$NON-NLS-1$
							Msg.getString("direction.southShort") //$NON-NLS-1$
					));
					return;
				}

				if (!latStr.endsWith(Msg.getString("direction.northShort"))
						&& !latStr.endsWith(Msg.getString("direction.southShort"))) { //$NON-NLS-1$ //$NON-NLS-2$
					setError(Msg.getString("SimulationConfigEditor.error.latitudeEndWith", //$NON-NLS-1$
							Msg.getString("direction.northShort"), //$NON-NLS-1$
							Msg.getString("direction.southShort") //$NON-NLS-1$
					));
				}

				// else {
				// String numLatitude = latStr.substring(0, latStr.length() - 1);
				// try {
				// double doubleLatitude = Double.parseDouble(numLatitude.trim());
				// if ((doubleLatitude < 0) || (doubleLatitude > 90)) {
				// setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith"));
				// //$NON-NLS-1$
				// return;
				// }
				// }
				// catch(NumberFormatException e) {
				// setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith"));
				// //$NON-NLS-1$
				// e.printStackTrace();
				// return;
				// }
				// }

				// check if the second from the last character is a digit or a letter, if a
				// letter, setError
				if (Character.isLetter(latStr.charAt(latStr.length() - 2))) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
					return;
				}

				// check if the second from the last character is a whitespace or not, if not
				// true, setError
				if (latStr.charAt(latStr.length() - 2) != ' ') {
					// System.out.println("has no whitespace");
					setError(Msg.getString("SimulationConfigEditor.error.latitudeMissingSpace")); //$NON-NLS-1$
					return;
				}

				if (latStr.length() > 2) {
					// check if the third from the last character is a digit or not, if not true,
					// setError
					if (!Character.isDigit(latStr.charAt(latStr.length() - 3))) {
						System.out.println("3rd from last");
						setError(Msg.getString("SimulationConfigEditor.error.latitudeBadFormat")); //$NON-NLS-1$
						return;
					}
				}
				// else {
				// setError(Msg.getString("SimulationConfigEditor.error.latitudeBadFormat"));
				// //$NON-NLS-1$
				// return;
				// }

				if (latStr.length() > 3) {
					/// 2017-03-29 Add checking for comma as well
					if (latStr.charAt(latStr.length() - 4) == '.')
						isDot_lat = true;
					if (latStr.charAt(latStr.length() - 4) == ',')
						isComma_lat = true;
					// check if the fourth from the last character is a whitespace or not, if not
					// true, setError
					if (!(isDot_lat || isComma_lat)) {
						System.out.println("4th from last : " + latStr.charAt(latStr.length() - 4));
						setError(Msg.getString("SimulationConfigEditor.error.latitudeBadFormat")); //$NON-NLS-1$
						return;
					}
				}

				if (latStr.length() > 4) {
					// check if the fifth from the last character is a digit or not, if not true,
					// setError
					if (!Character.isDigit(latStr.charAt(latStr.length() - 5))) {
						System.out.println("5th from last");
						setError(Msg.getString("SimulationConfigEditor.error.latitudeBadFormat")); //$NON-NLS-1$
						return;
					}
				}

				if (latStr.length() > 5) {
					// check if the sixth from the last character is a digit or not, if not true,
					// setError
					if (!Character.isDigit(latStr.charAt(latStr.length() - 6))) {
						System.out.println("6th from last");
						setError(Msg.getString("SimulationConfigEditor.error.latitudeBadFormat")); //$NON-NLS-1$
						return;
					}
				}

				// CHECK LONGITUDE

				String longStr = tableViewCombo.getAllData().get(x).getLongitude().toString().trim().toUpperCase();
				;

				if (longStr == null || longStr.length() < 2) {
					setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
					return;
				}

				// check if the last character is a digit or a letter, if digit, setError
				if (Character.isDigit(longStr.charAt(longStr.length() - 1))) {
					setError(Msg.getString("SimulationConfigEditor.error.longitudeEndWith", //$NON-NLS-1$
							Msg.getString("direction.eastShort"), //$NON-NLS-1$
							Msg.getString("direction.westShort") //$NON-NLS-1$
					));
					return;
				}

				// else {
				// String numLong = longStr.substring(0, longStr.length() - 1);
				// try {
				// double doubleLong = Double.parseDouble(numLong.trim());
				// if ((doubleLong < 0) || (doubleLong > 180)) {
				// setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith"));
				// //$NON-NLS-1$
				// return;
				// }
				// }
				// catch(NumberFormatException e) {
				// setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith"));
				// //$NON-NLS-1$
				// e.printStackTrace();
				// return;
				// }
				// }

				// check if the second from the last character is a digit or a letter, if a
				// letter, setError
				if (Character.isLetter(longStr.charAt(longStr.length() - 2))) {
					setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
					return;
				}

				// check if the second from the last character is a whitespace or not, if not
				// true, setError
				if (longStr.charAt(longStr.length() - 2) != ' ') {
					// System.out.println("has no whitespace");
					setError(Msg.getString("SimulationConfigEditor.error.longitudeMissingSpace")); //$NON-NLS-1$
					return;
				}

				// check if the third from the last character is a digit or not, if not true,
				// setError
				if (longStr.length() > 2) {
					if (!Character.isDigit(longStr.charAt(longStr.length() - 3))) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeBadFormat")); //$NON-NLS-1$
						return;
					}
				}

				if (longStr.length() > 3) {
					/// 2017-03-29 Add checking for comma as well
					if (longStr.charAt(longStr.length() - 4) == '.')
						isDot_lon = true;
					if (longStr.charAt(longStr.length() - 4) == ',')
						isComma_lon = true;
					// check if the fourth from the last character is a whitespace or not, if not
					// true, setError
					if (!(isDot_lon || isComma_lon)) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeBadFormat")); //$NON-NLS-1$
						return;
					}
				}

				if (longStr.length() > 4) {
					// check if the fifth from the last character is a digit or not, if not true,
					// setError
					if (!Character.isDigit(longStr.charAt(longStr.length() - 5))) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeBadFormat")); //$NON-NLS-1$
						return;
					}
				}

				// NOW COMPARE BETWEEN LATITUDES

				if (x + 1 < size) {

					// CHECK LATITUDES

					// if it has another settlement after this one
					String latNextStr = tableViewCombo.getAllData().get(x + 1).getLatitude().toString().trim()
							.toUpperCase();
					;

					if (latNextStr == null || latNextStr.length() < 2) {
						setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
						return;
					} else if (latStr.equals(latNextStr)) {
						repeated = true;
						break;
					}

					else {

						String numLat = latStr.substring(0, latStr.length() - 1);
						String numLatNext = latNextStr.substring(0, latNextStr.length() - 1);

						// System.out.println("isComma_lat : " + isComma_lat);

						/// 2017-03-29 Add checking for comma as well
						if (latNextStr.length() > 3) {
							if (latNextStr.charAt(latNextStr.length() - 4) == ',')
								isComma_lat = true;
						}

						if (isComma_lat) {
							numLat = numLat.replace(",", ".");
							numLatNext = numLatNext.replace(",", ".");
							// System.out.println("numLatNext : " + numLatNext);
						}

						try {

							double doubleLat = Double.parseDouble(numLat.trim());
							double doubleLatNext = Double.parseDouble(numLatNext.trim());

							if (doubleLat < 0 || doubleLat > 90 || doubleLatNext < 0 || doubleLatNext > 90) {
								setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith")); //$NON-NLS-1$
								return;
							}

							else if (doubleLatNext == 0 && doubleLat == 0) {
								repeated = true;
								break;

							}

							else if (doubleLatNext == doubleLat) {
								repeated = true;
								break;
							}
						}

						catch (NumberFormatException e) {
							setError(Msg.getString("SimulationConfigEditor.error.latitudeBadFormat")); //$NON-NLS-1$
							e.printStackTrace();
							return;
						}

					}

					// CHECK LONGITUDES

					String longNextStr = tableViewCombo.getAllData().get(x + 1).getLongitude().toString().trim()
							.toUpperCase();
					;

					if (longNextStr == null || longNextStr.length() < 2) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
						return;
					} else if (longStr.equals(longNextStr)) {
						repeated = true;
						break;
					}

					else {
						String numLong = longStr.substring(0, longStr.length() - 1);
						String numLongNext = longNextStr.substring(0, longNextStr.length() - 1);

						/// 2017-03-29 Add checking for comma as well
						if (longNextStr.length() > 3) {
							if (longNextStr.charAt(longNextStr.length() - 4) == ',')
								isComma_lon = true;
						}
						if (isComma_lon) {
							numLong = numLong.replace(",", ".");
							numLongNext = numLongNext.replace(",", ".");
						}

						try {

							double doubleLong = Double.parseDouble(numLong.trim());
							double doubleLongNext = Double.parseDouble(numLongNext.trim());

							if (doubleLong < 0 || doubleLong > 360 || doubleLongNext < 0 || doubleLongNext > 360) {
								setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith")); //$NON-NLS-1$
								return;
							}

							else if (doubleLongNext == 0 && doubleLong == 0) {
								repeated = true;
								break;

							}

							else if (doubleLongNext == doubleLong) {
								repeated = true;
								break;
							}
						}

						catch (NumberFormatException e) {
							setError(Msg.getString("SimulationConfigEditor.error.longitudeBadFormat")); //$NON-NLS-1$
							e.printStackTrace();
							return;
						}
					}
				}
			}

			if (repeated) {
				setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeRepeating")); //$NON-NLS-1$
				return;
			}

		} catch (NumberFormatException e) {
			setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the JavaFX's tooltip
	 * 
	 * @param node
	 * @param      tooltip's hint text
	 */
	public void setQuickToolTip(Node n, String s) {
		Tooltip tt = new Tooltip(s);
		tt.getStyleClass().add("ttip");

		n.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// Position the tooltip at bottom right of the node (see below for explanation)
				Point2D p = n.localToScreen(n.getLayoutBounds().getMaxX(), n.getLayoutBounds().getMaxY()); 
				tt.show(n, p.getX(), p.getY());
			}
		});
		n.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				tt.hide();
			}
		});

	}

	/**
	 * Open the exit dialog box
	 * 
	 * @param pane
	 */
	public void dialogOnExit(StackPane pane) {
		isShowingDialog = true;

		JFXButton b1 = new JFXButton("Exit");
		JFXButton b2 = new JFXButton("Back");

		StackPane sp = MainMenu.getExitDialogPane(b1, b2, null);

		JFXDialog exitDialog = new JFXDialog();
		exitDialog.setTransitionType(DialogTransition.TOP);
		exitDialog.setDialogContainer(pane);
		exitDialog.setContent(sp);
		exitDialog.show();

		b1.setOnAction(e -> {
			isExit = true;
			exitDialog.close();
			Platform.exit();
			System.exit(0);
		});

		b2.setOnAction(e -> {
			exitDialog.close();
			isShowingDialog = false;
			e.consume();
		});

	}

	public void destroy() {

		startButton = null;
		addButton = null;
		removeButton = null;
		undoButton = null;
		crewButton = null;

		config = null;
		mainMenu = null;
		mainScene = null;
		crewEditorFX = null;
		multiplayerClient = null;
		settlementConfig = null;

		errorLabel = null;
		titleLabel = null;

		clientIDLabel = null;
		playerLabel = null;
		titlePane = null;
		topVB = null;
		borderAll = null;
		parent = null;
		stage = null;
//		cstage = null;

		mainScene = null;
		tableView = null;
		tableViewCombo = null;
		settlementList = null;
	}

}