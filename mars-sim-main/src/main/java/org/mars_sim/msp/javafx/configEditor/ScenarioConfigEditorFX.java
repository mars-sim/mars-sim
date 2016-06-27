/**
 * Mars Simulation Project
 * ScenarioConfigEditorFX.java
 * @version 3.08 2015-04-17
 * @author Manny Kung
 */
package org.mars_sim.msp.javafx.configEditor;

import org.mars_sim.msp.javafx.MainMenu;
import org.mars_sim.msp.javafx.MarsProjectFX;
//import org.mars_sim.msp.javafx.WaitIndicator;
import org.mars_sim.msp.javafx.MainMenu.LoadSimulationTask;
//import org.mars_sim.msp.javafx.undecorator.Undecorator;
import insidefx.undecorator.Undecorator;
import insidefx.undecorator.UndecoratorScene;

import org.mars_sim.msp.networking.MultiplayerClient;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.configeditor.CrewEditor;
import org.mars_sim.msp.ui.swing.tool.StartUpLocation;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Node;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

import org.controlsfx.control.MaskerPane;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.networking.SettlementRegistry;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;


/**
 * ScenarioConfigEditorFX allows users to configure the types of settlements available at the start of the simulation.
 */
public class ScenarioConfigEditorFX {

	/** default logger. */
	private static Logger logger = Logger.getLogger(ScenarioConfigEditorFX.class.getName());

	private static final int HORIZONTAL_SIZE = 1024;

	// Data members.
	//private String TITLE = Msg.getString("SimulationConfigEditor.title");

	private int clientID = 0;

    double orgSceneX, orgSceneY;
    double orgTranslateX, orgTranslateY;

	private boolean hasError;
	private boolean hasSettlement;
	private boolean hasMSD, isCrewEditorOpen = false;
    private boolean isDone;

	private String playerName;
	private String gameMode;

	private JTableHeader header;
	private SettlementTableModel settlementTableModel;
	//private JTable settlementTable;
	private SettlementTable settlementTable;
	private JScrollPane settlementScrollPane;
	private TableCellEditor tableCellEditor;

	@FXML
	TabPane tabPane;
	   
	private Label errorLabel;
	private Button startButton;
	private Button addButton;
	private Button removeButton;
	private Button refreshDefaultButton;
	private Button crewButton;
	private Label titleLabel;
	private Label gameModeLabel;
	private Label clientIDLabel;
	private Label playerLabel;
	private TilePane titlePane;
	private VBox topVB;
	private BorderPane borderAll;
	private Parent parent;
	private SwingNode swingNode;
	private Stage stage;       
	private Stage cstage;// = new Stage();
	private Scene scene;

	private transient ThreadPoolExecutor executor;

	private SimulationConfig config;
	private MainMenu mainMenu;
	private CrewEditorFX crewEditorFX;
	private MarsProjectFX marsProjectFX;

	private MultiplayerClient multiplayerClient;
	private SettlementConfig settlementConfig;

	//private WaitIndicator waiti;
	
	private MainScene mainScene;

	private List<SettlementRegistry> settlementList;

	/**
	 * Constructor
	 * @param mainMenu
	 * @param config the simulation configuration.
	 */
	public ScenarioConfigEditorFX(MarsProjectFX marsProjectFX, MainMenu mainMenu) { //, SimulationConfig config) {
	    //logger.info("ScenarioConfigEditorFX's constructor is on " + Thread.currentThread().getName() );

		// Initialize data members.
		this.config = SimulationConfig.instance();
		this.mainMenu = mainMenu;
		this.marsProjectFX = marsProjectFX;
		this.mainScene = mainMenu.getMainScene();

	    hasError = false;

		settlementConfig = config.getSettlementConfiguration();

		if (mainMenu.getMultiplayerMode() != null) {
			//multiplayerClient = mainMenu.getMultiplayerMode().getMultiplayerClient();
			multiplayerClient = MultiplayerClient.getInstance();
			//multiplayerClient.sendRegister(); // not needed. already registered
			clientID = multiplayerClient.getClientID();
			playerName = multiplayerClient.getPlayerName();
			if (multiplayerClient.getNumSettlement() > 0)
				hasSettlement = true;
			//System.out.println("registrySize is " + registrySize);
			settlementList = multiplayerClient.getSettlementRegistryList();
			gameMode = "Simulation Mode : Multi-Player";
		}
		else {
			gameMode = "Simulation Mode : Single-Player";
			hasSettlement = false;
			playerName = "Default";
		}

	    createGUI();

	}

	@SuppressWarnings("restriction")
	public void createGUI() {
	   	Platform.setImplicitExit(false);
/*
		try {
			UIManager.setLookAndFeel(new NimRODLookAndFeel());
			}
	    catch(Exception ex){
			logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), ex); //$NON-NLS-1$
	    }
*/

		FXMLLoader fxmlLoader = null;

		try {
			fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("/fxui/fxml/ConfigEditorFX.fxml")); //ClientArea.fxml")); //
            fxmlLoader.setController(this);
			parent = (Parent) fxmlLoader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		Platform.runLater(() -> {

			stage = new Stage();
	 	   	//2016-02-07 Added calling setMonitor()
	 	   	mainMenu.setMonitor(stage);
			stage.setTitle("Mars Simulation Project - Configuration Editor");
		    stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));//toString()));
	        //stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));

		    Region root = (Region) parent;
		       // The Undecorator as a Scene
	        final UndecoratorScene undecoratorScene = new UndecoratorScene(stage, root);
	        
	        // Overrides defaults
	        undecoratorScene.addStylesheet("/fxui/css/app.css");
		    undecoratorScene.getStylesheets().add("/fxui/css/configEditorFXOrange.css");

	        // Enable fade transition
	        undecoratorScene.setFadeInTransition();

	        // Optional: Enable this node to drag the stage
	        // By default the root argument of Undecorator is set as draggable
	        //Node node = root.lookup("#draggableNode");  // Enable TabPane to drag the stage
	        //undecoratorScene.setAsStageDraggable(stage, tabPane);

	        /*
	         * Fade out transition on window closing request
	         */
	        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	            @Override
	            public void handle(WindowEvent we) {
	                we.consume();   // Do not hide yet
	                undecoratorScene.setFadeOutTransition();
	            }
	        });

		        
			AnchorPane anchorpane = null;
			if ( parent.lookup("#anchorRoot") == null)
				System.out.println("Warning: anchorRoot is not found");
			else
				anchorpane = ((AnchorPane) parent.lookup("#anchorRoot"));
			
		    // List should stretch as anchorpane is resized
		    BorderPane bp = createEditorFrame();
		    AnchorPane.setTopAnchor(bp, 5.0);
		    AnchorPane.setLeftAnchor(bp, 5.0);
		    AnchorPane.setRightAnchor(bp, 5.0);
		    anchorpane.getChildren().add(bp);

			//scene = new Scene(undecorator);
			//undecorator.setOnMousePressed(buttonOnMousePressedEventHandler);

			// Transparent scene and stage
		    //undecoratorScene.setFill(Color.TRANSPARENT); // needed to eliminate the white border

			//stage.initStyle(StageStyle.TRANSPARENT);
			//stage.setMinWidth(undecorator.getMinWidth());
			//stage.setMinHeight(undecorator.getMinHeight());
	        stage.centerOnScreen();
	        stage.setResizable(false);
	 	   	stage.setFullScreen(false);
	        stage.setScene(undecoratorScene);
			stage.sizeToScene();
	        stage.toFront();
	        stage.show();
		    

	    	stage.setOnCloseRequest(e -> {
				boolean isExit = mainMenu.getScreensSwitcher().exitDialog(stage);
				e.consume(); // need e.consume() in order to call setFadeOutTransition() below
				if (isExit) {
					 borderAll.setOpacity(0);
					 //undecorator.setFadeOutTransition();
					 if (crewEditorFX != null || isCrewEditorOpen)
						 crewEditorFX.getStage().close();
					 Platform.exit();
				}
			});

	    	
	    	
	    	
		});


	}


	//private Parent createEditor() {
	@SuppressWarnings("restriction")
	private BorderPane createEditorFrame() {
		//AnchorPane pane = new AnchorPane();
		borderAll = new BorderPane();
		//AnchorPane.setTopAnchor(borderAll, 50.0);
	    //AnchorPane.setLeftAnchor(borderAll, 50.0);
	    //AnchorPane.setRightAnchor(borderAll, 50.0);
		borderAll.setPadding(new Insets(10, 15, 10, 15));

		topVB = new VBox();
		topVB.setAlignment(Pos.CENTER);
		gameModeLabel = new Label(gameMode);
		gameModeLabel.setId("gameModeLabel");

		// Create the title label.
		if (multiplayerClient != null) {
			clientIDLabel = new Label("Client ID : " + clientID);
			playerLabel = new Label("Player : " + playerName);
		}
		else {
			clientIDLabel = new Label();
			playerLabel = new Label();
		}
		clientIDLabel.setId("clientIDLabel");
		playerLabel.setId("playerLabel");

		titleLabel = new Label(Msg.getString("SimulationConfigEditor.chooseSettlements")); //$NON-NLS-1$
		titleLabel.setId("titleLabel");
		//titleLabel.setPadding(new Insets(5, 10, 5, 10));
		//titlePane = new TilePane(Orientation.VERTICAL);
		titlePane = new TilePane(Orientation.HORIZONTAL);
		titlePane.setMaxWidth(600);
		titlePane.setPadding(new Insets(3, 3, 3, 3));
		titlePane.setHgap(2.0);
		titlePane.setVgap(2.0);
		//if (multiplayerClient != null) {
		//	titlePane.getChildren().addAll(clientIDLabel, titleLabel);
		//	clientIDLabel.setAlignment(Pos.TOP_LEFT);
		//}
		//else
		titlePane.getChildren().addAll(titleLabel);
		titlePane.setAlignment(Pos.TOP_LEFT);
		//titleLabel.setAlignment(Pos.CENTER);
		//gameModeLabel.setAlignment(Pos.TOP_LEFT);

		HBox topHB = new HBox(50);
		topHB.setPadding(new Insets(5, 10, 5, 10));
		topHB.setPrefWidth(400);
		topHB.getChildren().addAll(playerLabel, clientIDLabel);
		topHB.setAlignment(Pos.CENTER);
		topVB.getChildren().addAll(gameModeLabel, topHB, titleLabel);
		borderAll.setTop(topVB);

		// Create settlement scroll panel.
		//ScrollPane settlementScrollPane = new ScrollPane();
		//settlementScrollPane.setPreferredSize(new Dimension(585, 200));

		// Create settlement scroll panel.
		settlementScrollPane = new JScrollPane();
		settlementScrollPane.setPreferredSize(new Dimension(800, 200));
		settlementScrollPane.setSize(new Dimension(800, 200));
		//.add(settlementScrollPane, BorderLayout.CENTER);

		//TableView table = new TableView();
		//table.setEditable(true);
        //TableColumn col1 = new TableColumn("");
        //TableColumn col2 = new TableColumn("");
        //TableColumn col3 = new TableColumn("");
        //table.getColumns().addAll(col1, col2, col3);

		StackPane swingPane = new StackPane();
		swingPane.setMaxSize(HORIZONTAL_SIZE, 200);
		swingNode = new SwingNode();
		swingNode.setOpacity(.8);
		//swingNode.setBlendMode(BlendMode.SRC_OVER);
		createSwingNode(swingNode);
		swingPane.getChildren().add(swingNode);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		swingPane.setPrefWidth(primaryScreenBounds.getWidth());
		//swingPane.setMaxSize(Region.USE_COMPUTED_SIZE, 200);
		borderAll.setCenter(swingPane);

		// Create configuration button outer panel.
		BorderPane borderButtons = new BorderPane();
		borderAll.setLeft(borderButtons);

		// Create configuration button inner top panel.
		VBox vbTopLeft = new VBox();
		borderButtons.setTop(vbTopLeft);
		vbTopLeft.setSpacing(10);
		vbTopLeft.setPadding(new Insets(0, 10, 10, 10));

		// Create add settlement button.
		addButton = new Button(Msg.getString("SimulationConfigEditor.button.add")); //$NON-NLS-1$
		setMouseCursor(addButton);
		addButton.setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.add"))); //$NON-NLS-1$
		addButton.setOnAction((event) -> {
			addNewSettlement();
		});
		vbTopLeft.getChildren().add(addButton);

		// Create remove settlement button.
		removeButton = new Button(Msg.getString("SimulationConfigEditor.button.remove")); //$NON-NLS-1$
		setMouseCursor(removeButton);
		removeButton.setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.remove"))); //$NON-NLS-1$
		removeButton.setId("removeButton");
		removeButton.setOnAction((event) -> {
			boolean isYes = confirmDeleteDialog("Removing settlement", "Are you sure you want to do this?");
			if (isYes) {
				removeSelectedSettlements();
				//mainMenu.getStage().setIconified(true);
			}
		});
		vbTopLeft.getChildren().add(removeButton);

		// Create configuration button inner bottom panel.
		VBox vbCenter = new VBox();
		vbCenter.setSpacing(10);
		vbCenter.setPadding(new Insets(0, 10, 10, 10));
		borderButtons.setBottom(vbCenter);

/*
		// Create default button.
		defaultButton = new Button(Msg.getString("SimulationConfigEditor.button.default")); //$NON-NLS-1$
		//defaultButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.default")); //$NON-NLS-1$
		defaultButton.setOnAction((event) -> {
			if (multiplayerClient == null)
				setDefaultSettlements();
			else
				setExistingSettlements();
		});
		vbCenter.getChildren().add(defaultButton);
*/
		addButton.setMaxWidth(Double.MAX_VALUE);
		removeButton.setMaxWidth(Double.MAX_VALUE);
		//defaultButton.setMaxWidth(Double.MAX_VALUE);

		// Create bottom panel.
		BorderPane bottomPanel = new BorderPane();
		borderAll.setBottom(bottomPanel);

		// Create error label.
		errorLabel = new Label(" "); //$NON-NLS-1$
		//errorLabel.set//setColor(Color.RED);
		errorLabel.setStyle("-fx-font: 15 arial; -fx-color: red; -fx-base: #ff5400;");
		bottomPanel.setTop(errorLabel);

		// Create the bottom button panel.
		//HBox bottomButtonPanel = new HBox();
		//bottomPanel.setBottom(bottomButtonPanel);

		// Create refresh/defaultButton button.
		refreshDefaultButton = new Button(Msg.getString("SimulationConfigEditor.button.default")); //$NON-NLS-1$
		setMouseCursor(refreshDefaultButton);
		refreshDefaultButton.setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.default"))); //$NON-NLS-1$
		refreshDefaultButton.setOnAction((event) -> {
			if (multiplayerClient != null && hasSettlement) {
				boolean isYes = confirmDeleteDialog("Delete current settlement settings, reload the default templates and refresh other players' settlements", "Are you sure you want to do this?");
				if (isYes)
					setExistingSettlements();
			}
			else {
				boolean isYes = confirmDeleteDialog("Delete current settlement settings and reload the default templates", "Are you sure you want to do this?");
				if (isYes)
					setDefaultSettlements();
			}
			//mainMenu.getStage().setIconified(true);
		});
		//vbCenter.getChildren().add(defaultButton);

		// Create the start button.
		startButton = new Button("  " + Msg.getString("SimulationConfigEditor.button.newSim")+ "  "); //$NON-NLS-1$
		setMouseCursor(startButton);
		startButton.setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.newSim")));
		startButton.setId("startButton");
		// 2015-10-15 Made "Enter" key to work like the space bar for firing the button on focus
		startButton.defaultButtonProperty().bind(startButton.focusedProperty());
		startButton.requestFocus();
		startButton.setOnAction((event) -> {

			if (crewEditorFX != null) {
				if (!crewEditorFX.isGoodToGo()) {
					startButton.setDisable(true);
					//System.out.println("start button is set to disable");
					Alert alert = new Alert(AlertType.ERROR, "Please fix the invalid name(s) in the Crew Editor.");
					alert.initOwner(stage);
					alert.showAndWait();
					event.consume();
					return;
				}
				else{

				}
			}

			// Make sure any editing cell is completed, then check if error.
			if (tableCellEditor != null) {
				tableCellEditor.stopCellEditing();
			}

			if (!hasError) {
				
				//waiti = new WaitIndicator();
					
				setConfiguration();
		        //scene.setCursor(Cursor.WAIT); //Change cursor to wait style	  
		        
				cstage = new Stage();
				CompletableFuture<?> future = CompletableFuture
						.supplyAsync(() -> submitTask());
						//.thenAccept(lr -> waitLoading());	//loadProgress()); //
				
		    	//Platform.runLater(() -> {
					closeWindow();
				//});
					
/*					
				while (//Simulation.instance().getMasterClock() == null
						//&& 
						!mainMenu.getMainScene().isMainSceneDone()) {// || Simulation.instance().getMasterClock().isLoadingSimulation()) {
					System.out.print(".");
					//MainMenu : the master clock instance is not ready yet. Wait for another 1/2 secs");
					try {
						TimeUnit.MILLISECONDS.sleep(500L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				

				waiti.getStage().close();							
*/					
				//scene.setCursor(Cursor.DEFAULT); //Change cursor to default style
								
			} //end of if (!hasError)

		});

		// 2014-12-15 Added Edit Alpha Crew button.
		crewButton = new Button(Msg.getString("SimulationConfigEditor.button.crewEditor")); //$NON-NLS-1$
		setMouseCursor(crewButton);
		//alphaButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.crewEditor")); //$NON-NLS-1$
		crewButton.setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.crewEditor")));
		//alphaButton.setStyle("-fx-font: 16 arial; -fx-base: #cce6ff;");
		crewButton.setOnAction((event) -> {
			editCrewProfile("alpha");
		});
		//bottomButtonPanel.getChildren().add(alphaButton);

		TilePane tileButtons = new TilePane(Orientation.HORIZONTAL);
		tileButtons.setPadding(new Insets(5, 5, 5, 5));
		tileButtons.setHgap(50.0);
		tileButtons.setVgap(8.0);
		tileButtons.getChildren().addAll(refreshDefaultButton, startButton, crewButton);
		tileButtons.setAlignment(Pos.CENTER);
		bottomPanel.setBottom(tileButtons);

		//pane.getChildren().add(borderAll);
		return borderAll;
		//return borderAll;
	}

	public int loadProgress() {
    	Platform.runLater(() -> {
			waitLoading();
		});
		return 1;
	}
	
	public int submitTask() {
		Simulation.instance().getSimExecutor().submit(new SimulationTask());
		return 1;
	}
	
	/**
	 * Swaps the mouse cursor type between DEFAULT and HAND
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

	public class SimulationTask implements Runnable {
		public void run() {
			//logger.info("ScenarioConfigEditorFX's LoadSimulationTask's run() is on " + Thread.currentThread().getName() );
			//mainScene.createProgressCircle();
			mainScene.showLoadingStage();
			
			//boolean isDone = false;
			Simulation.createNewSimulation();
			//System.out.println("ScenarioConfigEditorFX : done calling Simulation.instance().createNewSimulation()");
			Simulation.instance().start(false);
			//System.out.println("ScenarioConfigEditorFX : done calling Simulation.instance().start()");
			Platform.runLater(() -> {
				mainMenu.prepareScene();
				mainMenu.prepareStage();
				//System.out.println("ScenarioConfigEditorFX : done calling prepareStage");
			});
			if (multiplayerClient != null)
				multiplayerClient.prepareListeners();
			
			//logger.info("ScenarioConfigEditorFX : done calling SimulationTask");

			//JmeCanvas jme = new JmeCanvas();
	    	//jme.setupJME();
			
		}
	}


	@SuppressWarnings("serial")
	private void createSwingNode(final SwingNode swingNode) {

        SwingUtilities.invokeLater(() -> {
    		settlementTableModel = new SettlementTableModel(this);
    		settlementTable = new SettlementTable(this, settlementTableModel);
        });
        swingNode.setContent(settlementScrollPane);

    }


	/**
	 * Adds a new settlement with default values.
	 */
	private void addNewSettlement() {
		SettlementInfo settlement = determineNewSettlementConfiguration();
		settlementTableModel.addSettlement(settlement);
		updateSettlementNames();
	}


	/**
	 * Removes the settlements selected on the table.
	 */
	private void removeSelectedSettlements() {
		settlementTableModel.removeSettlements(settlementTable.getSelectedRows());
		updateSettlementNames();
	}

	public void updateSettlementNames() {
		if (crewEditorFX != null) {
			crewEditorFX.updateSettlementNames();
		//} else if (!isCrewEditorOpen) {
		//	crewEditorFX.updateSettlementNames();
		}
	}

	/**
	 * Edits team roster.
	 */
	private void editCrewProfile(String crew) {
		if (crewEditorFX == null) {
			crewEditorFX = new CrewEditorFX(config, this);
			//System.out.println("new CrewEditorFX()");
		} else if (!isCrewEditorOpen) {
			//System.out.println("calling crewEditorFX.createGUI()");
			//crewEditorFX.createGUI();
			crewEditorFX.getStage().show();
		}
		else
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
		settlementTableModel.loadDefaultSettlements();
		updateSettlementNames();
	}

	/**
	 * Sets the existing settlements loaded from others client machine.
	 */
	private void setExistingSettlements() {
		settlementTableModel.loadExistingSettlements();
		updateSettlementNames();
	}

	/**
	 * Set the simulation configuration based on dialog choices.
	 */
	private void setConfiguration() {
		// Clear configuration settlements.
		settlementConfig.clearInitialSettlements();
		// Add configuration settlements from table data.
		for (int x = 0 ; x < settlementTableModel.getRowCount(); x++) {
			if (multiplayerClient != null) {
				if (hasSettlement && x < settlementList.size())
						; // do nothing to the existing settlements from other clients
				else {
					createSettlement(x);
				}
			}
			else {
				createSettlement(x);
			}
		}
	}

	/**
	 * Creates a settlement based from each row of choice
	 */
	private void createSettlement(int x) {

		String playerName = (String) settlementTableModel.getValueAt(x, SettlementTable.COLUMN_PLAYER_NAME);
		String name = (String) settlementTableModel.getValueAt(x, SettlementTable.COLUMN_SETTLEMENT_NAME);
		String template = (String) settlementTableModel.getValueAt(x, SettlementTable.COLUMN_TEMPLATE);
		String population = (String) settlementTableModel.getValueAt(x, SettlementTable.COLUMN_POPULATION);
		int populationNum = Integer.parseInt(population);
		//System.out.println("populationNum is " + populationNum);
		String numOfRobotsStr = (String) settlementTableModel.getValueAt(x, SettlementTable.COLUMN_BOTS);
		int numOfRobots = Integer.parseInt(numOfRobotsStr);
		//System.out.println("SimulationConfigEditor : numOfRobots is " + numOfRobots);
		String latitude = (String) settlementTableModel.getValueAt(x, SettlementTable.COLUMN_LATITUDE);
		String longitude = (String) settlementTableModel.getValueAt(x, SettlementTable.COLUMN_LONGITUDE);
		double lat = SettlementRegistry.convertLatLong2Double(latitude);
		double lo = SettlementRegistry.convertLatLong2Double(longitude);
/*
		Boolean hasMSD = (Boolean) settlementTableModel.getValueAt(x, SettlementTable.COLUMN_HAS_MSD);
		String maxMSDStr = null;
		int maxMSD = 0; // TODO: if hasMSD == 1, how do I load from settlementConfig
		//if (hasMSD) {
		//	maxMSDStr = Integer.toString(settlementConfig.getInitialSettlementMaxMSD(x));
		//	maxMSD = Integer.parseInt(maxMSDStr);
		//}
*/
		settlementConfig.addInitialSettlement(name, template, populationNum, numOfRobots, latitude, longitude, 0);

		//Send the newly created settlement to host server
		if (multiplayerClient != null) {
			// create an instance of SettlementRegistry
			SettlementRegistry newS = new SettlementRegistry(playerName, clientID, name, template, populationNum, numOfRobots, lat, lo);
			multiplayerClient.sendNew(newS);
			//settlementConfig.setMultiplayerClient(multiplayerClient);
		}

	}

	/**
	 * Close and dispose dialog window.
	 */
	private int waitLoading() {
      
        StackPane stackPane = new StackPane();

		MaskerPane indicator = new MaskerPane();
		indicator.setScaleX(1.2);
		indicator.setScaleY(1.2);

		stackPane.getChildren().add(indicator);
		StackPane.setAlignment(indicator, Pos.CENTER);
		stackPane.setBackground(Background.EMPTY);
		 
		//stage.hide();
		Scene scene = new Scene(stackPane);//, 200, 200);
		scene.setFill(Color.TRANSPARENT);
		
		//cstage = stage;
		cstage.initStyle (StageStyle.TRANSPARENT);
		cstage.setScene(scene);
			
		StartUpLocation startUpLoc = new StartUpLocation(borderAll.getPrefWidth(), borderAll.getPrefHeight());
        double xPos = startUpLoc.getXPos();
        double yPos = startUpLoc.getYPos();
  
        if (xPos != 0 && yPos != 0) {
            cstage.setX(xPos);
            cstage.setY(yPos);
            cstage.centerOnScreen();
            //System.out.println(" x : " + xPos + "   y : " + yPos);
        } else {
            cstage.centerOnScreen();
            //System.out.println("calling centerOnScreen()");
            //System.out.println(" x : " + xPos + "   y : " + yPos);
        }
        cstage.show();
        cstage.requestFocus();      
		stage.hide();
		return 1;
	}

	/**
	 * Close and dispose dialog window.
	 */
	private void closeWindow() {
		//cstage.hide();	
		//cstage.close();	
		stage.hide();
		stage.close();
	}


	/**
	 * Determines the configuration of a new settlement.
	 * @return settlement configuration.
	 */
	private SettlementInfo determineNewSettlementConfiguration() {
		SettlementInfo settlement = new SettlementInfo();
		settlement.playerName = playerName;
		settlement.name = determineNewSettlementName();
		settlement.template = determineNewSettlementTemplate();
		settlement.population = determineNewSettlementPopulation(settlement.template);
		settlement.numOfRobots = determineNewSettlementNumOfRobots(settlement.template);
		settlement.latitude = determineNewSettlementLatitude();
		settlement.longitude = determineNewSettlementLongitude();
		settlement.maxMSD = "0";
		// TODO: add maxMSD

		return settlement;
	}


	/**
	 * Determines a new settlement's name.
	 * @return name.
	 */
	private String determineNewSettlementName() {
		String result = null;

		// Try to find unique name in configured settlement name list.
		// Randomly shuffle settlement name list first.
		SettlementConfig settlementConfig = config.getSettlementConfiguration();
		List<String> settlementNames = settlementConfig.getSettlementNameList();
		Collections.shuffle(settlementNames);
		Iterator<String> i = settlementNames.iterator();
		while (i.hasNext()) {
			String name = i.next();

			// Make sure settlement name isn't already being used in table.
			boolean nameUsed = false;
			for (int x = 0; x < settlementTableModel.getRowCount(); x++) {
				if (name.equals(settlementTableModel.getValueAt(x, SettlementTable.COLUMN_SETTLEMENT_NAME))) {
					nameUsed = true;
				}
			}

			// TODO: check if the name is being used in the host server's settlement registry or not

			// If not being used already, use this settlement name.
			if (!nameUsed) {
				result = name;
				break;
			}
		}

		// If no name found, create numbered settlement name: "Settlement 1", "Settlement 2", etc.
		int count = 1;
		while (result == null) {
			String name = Msg.getString(
				"SimulationConfigEditor.settlement", //$NON-NLS-1$
				Integer.toString(count)
			);

			// Make sure settlement name isn't already being used in table.
			boolean nameUsed = false;
			for (int x = 0; x < settlementTableModel.getRowCount(); x++) {
				if (name.equals(settlementTableModel.getValueAt(x, SettlementTable.COLUMN_SETTLEMENT_NAME))) {
					nameUsed = true;
				}
			}

			// TODO: check if the name is being used in the host server's settlement registry or not

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
	 * @return template name.
	 */
	private String determineNewSettlementTemplate() {
		String result = null;

		SettlementConfig settlementConfig = config.getSettlementConfiguration();
		List<SettlementTemplate> templates = settlementConfig.getSettlementTemplates();
		if (templates.size() > 0) {
			int index = RandomUtil.getRandomInt(templates.size() - 1);
			result = templates.get(index).getTemplateName();
		}
		else logger.log(Level.WARNING, Msg.getString("SimulationConfigEditor.log.settlementTemplateNotFound")); //$NON-NLS-1$

		return result;
	}

	/**
	 * Determines the new settlement population.
	 * @param templateName the settlement template name.
	 * @return the new population number.
	 */
	public String determineNewSettlementPopulation(String templateName) {

		String result = "0"; //$NON-NLS-1$

		if (templateName != null) {
			SettlementConfig settlementConfig = config.getSettlementConfiguration();
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
	 * @param templateName the settlement template name.
	 * @return number of robots.
	 */
	public String determineNewSettlementNumOfRobots(String templateName) {

		String result = "0"; //$NON-NLS-1$

		if (templateName != null) {
			SettlementConfig settlementConfig = config.getSettlementConfiguration();
			Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
			while (i.hasNext()) {
				SettlementTemplate template = i.next();
				if (template.getTemplateName().equals(templateName)) {
					result = Integer.toString(template.getDefaultNumOfRobots());
					//System.out.println("SimulationConfigEditor : determineNewSettlementNumOfRobots() : result is " + result);
				}
			}
		}

		return result;
	}

	/**
	 * Determines a new settlement's latitude.
	 * @return latitude string.
	 */
	private String determineNewSettlementLatitude() {

		// TODO: check if there is an existing settlement with the same latitude (within 1 decimal places) at this location from the host server's settlement registry
		// note: d = 6779km. each one degree is 59.1579km. each .1 degree is 5.91579 km apart.
		// e.g. if an existing town is at (0.1, 0.1), one cannot "reuse" these coordinates again. He can only create a new town at (0.1, 0.1)

		double phi = Coordinates.getRandomLatitude();
		String formattedLatitude = Coordinates.getFormattedLatitudeString(phi);
		int degreeIndex = formattedLatitude.indexOf(Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		return
			formattedLatitude.substring(0, degreeIndex) + " " +
			formattedLatitude.substring(degreeIndex + 1, formattedLatitude.length())
		;
	}

	/**
	 * Determines a new settlement's longitude.
	 * @return longitude string.
	 */
	private String determineNewSettlementLongitude() {

		// TODO: check if there is an existing settlement with the same latitude (within 1 decimal places) at this location from the host server's settlement registry
		// note: d = 6779km. each one degree is 59.1579km. each .1 degree is 5.91579 km apart.
		// e.g. if an existing town is at (0.1, 0.1), one cannot "reuse" these coordinates again. He can only create a new town at (0.1, 0.1)

		double theta = Coordinates.getRandomLongitude();
		String formattedLongitude = Coordinates.getFormattedLongitudeString(theta);
		int degreeIndex = formattedLongitude.indexOf(Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		return
			formattedLongitude.substring(0, degreeIndex) + " " +
			formattedLongitude.substring(degreeIndex + 1, formattedLongitude.length())
		;
	}

	public boolean confirmDeleteDialog(String header, String text) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.initOwner(stage);
        dialog.setHeaderText(header);
        dialog.setContentText(text);
        dialog.getDialogPane().setPrefSize(300, 180);
        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");
        dialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
        final Optional<ButtonType> result = dialog.showAndWait();
        return result.get() == buttonTypeYes;
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

	public Button getRefreshDefaultButton() {
		return refreshDefaultButton;
	}

	public Button getStartButton() {
		return startButton;
	}

	public void disableStartButton() {
		startButton.setDisable(true);;
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

	public JScrollPane getSettlementScrollPane() {
		return settlementScrollPane;
	}

	public List<SettlementRegistry> getSettlementList() {
		return settlementList;
	}

	public SettlementTableModel getSettlementTableModel() {
		return settlementTableModel;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}
	
	public void destroy() {

		//header = null;
		//settlementTableModel = null;
		//settlementTable = null;
		//settlementScrollPane = null;
		//tableCellEditor = null;
		//crewEditorFX = null;
		//marsProjectFX = null;
		//multiplayerClient = null;
		//settlementConfig = null;
		//waiti = null;
		//settlementList.clear();	
		//settlementList = null;
		config  = null;
		mainMenu  = null;
		mainScene = null;
		crewEditorFX  = null;
		marsProjectFX  = null;
		multiplayerClient  = null;
		settlementConfig  = null;

	}

}