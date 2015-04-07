/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.08 2015-03-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import static javafx.geometry.Orientation.VERTICAL;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.TransportWizard;

/**
 * The MainScene class is the primary Stage for MSP. It is the container for housing
 * desktop swing node, javaFX UI, pull-down menu and icons for tools.
 */
public class MainScene {

	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	private static int AUTOSAVE_EVERY_X_MINUTE = 10;
	private static final int TIME_DELAY = 940;

	// Categories of loading and saving simulation
	public static final int DEFAULT = 1;
	public static final int AUTOSAVE = 2;
	public static final int OTHER = 3; // load other file
	public static final int SAVE_AS = 3; // save as other file

    private StringProperty timeStamp;

    private int theme;
    private int memMax;
    private int memTotal;
    private int memUsed, memUsedCache;
    private int memFree;

    private boolean cleanUI = true;
	//private boolean useDefault;

	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;

    private Text timeText;
    private Text memUsedText;

    private Stage stage;

    private Tab swingTab;
    private Tab settlementTab;
    private TabPane tp;
    private Timeline timeline;
    private NotificationPane notificationPane;

    private MainDesktopPane desktop;
    //private MainWindow mainWindow;
    private MainSceneMenu menuBar;
    private MarsNode marsNode;
	private TransportWizard transportWizard;

	/**
	 * Constructor for MainScene
	 *@param stage
	 */
    public MainScene(Stage stage) {
         	this.stage = stage;
    }

	/**
	 * Creates the Main Scene
	 *@return Scene
	 */
    public Scene createMainScene() {

		// TODO: how to remove artifact and refresh the pull down menu and statusBar whenever clicking the maximize/iconify/restore button on top-right?

    	marsNode = new MarsNode(this, stage);

        Scene scene = init();

		// Load UI configuration.
		//if (!cleanUI) {
		//	UIConfig.INSTANCE.parseFile();
		//}

		// Set look and feel of UI.
		UIConfig.INSTANCE.useUIDefault();
		//setLookAndFeel(false);

        // Detect if a user hits ESC
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
              @Override
              public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ESCAPE) {
                 	// Toggle the full screen mode to OFF in the pull-down menu under setting
                	menuBar.exitFullScreen();
                	// close the MarsNet side panel
                	closeMarsNet();
                }
              }
          });

        theme = 1;
        scene.getStylesheets().add("/fxui/css/mainskin.css");
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.F11)) {
                	if (theme == 1) {
                		scene.getStylesheets().clear();
                		scene.getStylesheets().add(getClass().getResource("/fxui/css/themeskin.css").toExternalForm());
                		theme = 2;
                	}
                	else if (theme == 2) {
                		scene.getStylesheets().clear();
                		scene.getStylesheets().add(getClass().getResource("/fxui/css/settlementskin.css").toExternalForm());
                		theme = 3;
                	}
                	else if (theme == 3) {
                		scene.getStylesheets().clear();
                		scene.getStylesheets().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
                		theme = 1;
                	}
                }
            }
        });


		startAutosaveTimer();
        //desktop.openInitialWindows(); // doesn't work here
        startEarthTimer();

		// Detect if a user hits the top-right close button
		// TODO: determine if it is necessary to exit both the simulation stage and the Main Menu
		// Exit not just the stage but the simulation entirely
		stage.setOnCloseRequest(e -> {
			alertOnExit();
		});

		transportWizard = new TransportWizard(this);

        return scene;
    }

	public void openTransportWizard(BuildingManager buildingManager) {
		transportWizard.initialize(buildingManager);
		transportWizard.deliverBuildings();
	}

	public TransportWizard getTransportWizard() {
		return transportWizard;
	}

	/**
	 * Sets up the Main Scene
	 *@return Scene
	 */
	@SuppressWarnings("unchecked")
	public Scene init() {

        //ImageView bg1 = new ImageView();
        //bg1.setImage(new Image("/images/splash.png"));  // in lieu of the interactive Mars map
        //root.getChildren().add(bg1);

		// Create group to hold swingNode1 which holds the swing desktop
		StackPane swingPane = new StackPane();
		SwingNode swingNode = new SwingNode();
		createSwingNode(swingNode);
		swingPane.getChildren().add(swingNode);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		swingPane.setPrefWidth(primaryScreenBounds.getWidth());

		// Create ControlFX's StatusBar
		StatusBar statusBar = createStatusBar();
		VBox bottomBox = new VBox();
		bottomBox.getChildren().addAll(statusBar);

		// Create menuBar
        menuBar = new MainSceneMenu(this, getDesktop());
        menuBar.getStylesheets().addAll("/fxui/css/mainskin.css");

	    // Create BorderPane
		BorderPane borderPane = new BorderPane();
	    borderPane.setTop(menuBar);
	    //borderPane.setTop(toolbar);
	    borderPane.setBottom(bottomBox);
	    //borderPane.setStyle("-fx-background-color: palegorange");


	    //TabPane tpFX = new TabPane();
	    settlementTab = new Tab();
	    settlementTab.setClosable(false);
	    //tpFX.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
	    settlementTab.setText("Nodes");
	    //settlementTab.setContent(new Rectangle(200,200, Color.LIGHTSTEELBLUE));
	    settlementTab.setContent(marsNode.createPane("black"));
	    //tpFX.getTabs().add(settlementTab);

/*
	    // create a button to toggle floating.
	    final RadioButton floatControl = new RadioButton("Toggle floating");
	    floatControl.selectedProperty().addListener(new ChangeListener<Boolean>() {
	      @Override public void changed(ObservableValue<? extends Boolean> prop, Boolean wasSelected, Boolean isSelected) {
	        if (isSelected) {
	          tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
	        } else {
	          tabPane.getStyleClass().remove(TabPane.STYLE_CLASS_FLOATING);
	        }
	      }
	    });
*/
	    // layout the stage.
	    //VBox layout = new VBox(10);
	    //layout.getChildren().addAll(tabPane);
	    //VBox.setVgrow(tabPane, Priority.ALWAYS);
	    //layout.setStyle("-fx-padding: 10;");

	    tp = new TabPane();
	    tp.setSide(Side.RIGHT);
	    swingTab = new Tab();
	    swingTab.setClosable(false);
	    swingTab.setText("Classic");
	    swingTab.setContent(swingPane);

	    tp.getSelectionModel().select(swingTab);
	    tp.getTabs().addAll(swingTab,settlementTab);
/*
	    splitPane = new SplitPane();
	    //splitPane.setPrefWidth(100);
	    splitPane.setOrientation(Orientation.HORIZONTAL);
	    //splitPane.setMinWidth(Region.USE_PREF_SIZE);
	    splitPane.setDividerPositions(1.0f);
	    splitPane.getItems().addAll(swingPane, layout);
*/
		Node notificationNode = createNotificationPane();
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        notificationPane.setText("Breaking news for mars-simmers !!");
        notificationPane.hide();

	    //borderPane.setCenter(splitPane);
	    borderPane.setCenter(notificationNode);
	    StackPane rootStackPane = new StackPane(borderPane);

	    Scene scene = new Scene(rootStackPane, primaryScreenBounds.getWidth(), 640, Color.BROWN);

	    borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());

        return scene;
	}

	/**
	 * Creates and starts the earth timer
	 *@return Scene
	 */
	public void startEarthTimer() {
	    // Set up earth time text update
	    timeline = new Timeline(new KeyFrame(
	            Duration.millis(TIME_DELAY),
	            ae -> updateTimeText()));
	    timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
	    timeline.play();

	}



	public StatusBar createStatusBar() {
		StatusBar statusBar = new StatusBar();
		statusBar.setText(""); // needed for deleting the default text "OK"
	    //statusBar.setAlignment(Pos.BASELINE_RIGHT);
	    //statusBar.setStyle("-fx-background-color: gainsboro;");
        //statusBar.setAlignment(Pos.CENTER);
        //statusBar.setStyle("-fx-border-stylel:solid; -fx-border-width:2pt; -fx-border-color:grey; -fx-font: 14 arial; -fx-text-fill: white; -fx-base: #cce6ff;");
	    //statusBar.setMinHeight(memMaxText.getBoundsInLocal().getHeight() + 10);
	    //statusBar.setMijnWidth (memMaxText.getBoundsInLocal().getWidth()  + 10);


	    Button button1 = new Button(" [Memory] ");
        button1.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                new CornerRadii(2), new Insets(4))));
        statusBar.getRightItems().add(new Separator(VERTICAL));
	    statusBar.getRightItems().add(button1);

        memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
	    Text memMaxText = new Text(" Total Designated : " + memMax +  " MB ");
	    statusBar.getRightItems().add(memMaxText);

	    memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
	    memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
	    memUsed = memTotal - memFree;
	    memUsedText = new Text(" Currently Used : " + memUsed +  " MB ");
	    statusBar.getRightItems().add(memUsedText);
	    statusBar.getRightItems().add(new Separator(VERTICAL));

        MasterClock master = Simulation.instance().getMasterClock();
        if (master == null) {
            throw new IllegalStateException("master clock is null");
        }
        EarthClock earthclock = master.getEarthClock();
        if (earthclock == null) {
            throw new IllegalStateException("earthclock is null");
        }

	    timeText =  new Text(" Earth Time : " + timeStamp + "  ");
	    Button button2 = new Button(" [Clock] ");
        button2.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                new CornerRadii(2), new Insets(4))));
	    statusBar.getRightItems().add(button2);
	    statusBar.getRightItems().add(timeText);
	    statusBar.getRightItems().add(new Separator(VERTICAL));

		return statusBar;
	}

	public NotificationPane getNotificationPane() {
		return notificationPane;
	}

	public Node createNotificationPane() {

        //notificationPane = new NotificationPane(splitPane);
        notificationPane = new NotificationPane(tp);
        String imagePath = getClass().getResource("/notification/notification-pane-warning.png").toExternalForm();
        ImageView image = new ImageView(imagePath);
        notificationPane.setGraphic(image);
        notificationPane.getActions().addAll(new Action("Sync", ae -> {
                // do sync, then hide...
                notificationPane.hide();
        }));


        return notificationPane;
    }

		public String getSampleName() {
	        return "Notification Pane";
	    }


	    public String getControlStylesheetURL() {
	        return "/org/controlsfx/control/notificationpane.css";
	    }
/*
	    private void updateBar() {
	        boolean useDarkTheme = cbUseDarkTheme.isSelected();
	        if (useDarkTheme) {
	            notificationPane.setText("Hello World! Using the dark theme");
	            notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
	        } else {
	            notificationPane.setText("Hello World! Using the light theme");
	            notificationPane.getStyleClass().remove(NotificationPane.STYLE_CLASS_DARK);
	        }
	    }
*/
	public void updateTimeText() {

		String t = null;
		//try {
	        // Check if new simulation is being created or loaded from file.
	        if (!Simulation.isUpdating()) {

	            MasterClock master = Simulation.instance().getMasterClock();
	            if (master == null) {
	                throw new IllegalStateException("master clock is null");
	            }
	            EarthClock earthclock = master.getEarthClock();
	            if (earthclock == null) {
	                throw new IllegalStateException("earthclock is null");
	            }
	            t = earthclock.getTimeStamp();
	            //timeStamp = new SimpleStringProperty(earthclock.getTimeStamp());
	        }
	    //}
	   // catch (Exception ee) {
	    //    ee.printStackTrace(System.err);
	    //}
		timeText.setText(" Earth Time : " + t + "  ");
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
	    memUsed = memTotal - memFree;
	    //int mem = ( memUsedCache + memUsed ) /2;
	    if (memUsed > memUsedCache * 1.1 || memUsed < memUsedCache * 0.9) {
	    	memUsedText.setText(" Currently Used : " + memUsed +  " MB ");
	    }
    	memUsedCache = memUsed;

	}

	// 2015-01-07 Added startAutosaveTimer()
	public void startAutosaveTimer() {

	    Timeline timeline = new Timeline(new KeyFrame(
	            Duration.millis(1000*60*AUTOSAVE_EVERY_X_MINUTE),
	            ae -> saveSimulation(AUTOSAVE)));
	    timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
	    timeline.play();

    }


	/**
	 * Gets the main desktop panel.
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		//return mainWindow.getDesktop();
		return desktop;
	}

	/**
	 * Load a previously saved simulation.
	 */
	// 2015-01-25 Added autosave
	public void loadSimulation(int type) {
        //if (earthTimer != null)
        //    earthTimer.stop();
        //earthTimer = null;

		//timeline.stop(); // Note: no need to stop and restart at all

		if ((loadSimThread == null) || !loadSimThread.isAlive()) {
			loadSimThread = new Thread(Msg.getString("MainWindow.thread.loadSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						loadSimulationProcess(type);
					});
				}
			};
			loadSimThread.start();
		} else {
			loadSimThread.interrupt();
		}

	}


	/**
	 * Performs the process of loading a simulation.
	 * @param autosave, true if loading the autosave sim file
	 */
	public void loadSimulationProcess(int type) {
		String dir = null;
		String title = null;
		File fileLocn = null;

		if (type == DEFAULT) {
			dir = Simulation.DEFAULT_DIR;
		}

		else if (type == AUTOSAVE) {
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		}

		else if (type == OTHER) {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}

		if (type == AUTOSAVE || type == OTHER) {
			FileChooser chooser = new FileChooser();
			//chooser.setInitialFileName(dir);
			//Set to user directory or go to default if cannot access
			//String userDirectoryString = System.getProperty("user.home");
			File userDirectory = new File(dir);
			chooser.setInitialDirectory(userDirectory);
			chooser.setTitle(title); //$NON-NLS-1$

			 // Set extension filter
	        FileChooser.ExtensionFilter simFilter =
	                new FileChooser.ExtensionFilter("Simulation files (*.sim)", "*.sim");
			FileChooser.ExtensionFilter allFilter =
	                new FileChooser.ExtensionFilter("all files (*.*)", "*.*");

	        chooser.getExtensionFilters().addAll(simFilter, allFilter);

	        // Show open file dialog
			File selectedFile = chooser.showOpenDialog(stage);

			if (selectedFile != null)
				fileLocn = selectedFile;
			else
				return;
		}

		else if (type == DEFAULT) {

			fileLocn = null;
		}

		//fileLabel.setText(file.getPath());
		desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$
		desktop.clearDesktop();

		MasterClock clock = Simulation.instance().getMasterClock();
		clock.loadSimulation(fileLocn);

		while (clock.isLoadingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.waitInterrupt"), e); //$NON-NLS-1$
			}
		}


		try {
            desktop.resetDesktop();
            logger.info(" loadSimulationProcess() : desktop.resetDesktop()");
        }
        catch (Exception e) {
            // New simulation process should continue even if there's an exception in the UI.
            logger.severe(e.getMessage());
            e.printStackTrace(System.err);
        }

		// load UI config
		UIConfig.INSTANCE.parseFile();

		desktop.disposeAnnouncementWindow();

		// Open Guide tool after loading.
		desktop.openToolWindow(GuideWindow.NAME);

	}

	/**
	 * Create a new simulation.
	 */
	public void newSimulation() {
		if ((newSimThread == null) || !newSimThread.isAlive()) {
			newSimThread = new Thread(Msg.getString("MainWindow.thread.newSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						newSimulationProcess();
					});
				}
			};
			newSimThread.start();
		} else {
			newSimThread.interrupt();
		}

		// 2015-01-19 Added using delayLaunchTimer to launch earthTime
		//if (earthTimer == null) {
		//	//System.out.println(" newSimulation() : earthTimer == null");
		//	delayLaunchTimer = new Timer();
		//	int seconds = 1;
		//	delayLaunchTimer.schedule(new StatusBar(), seconds * 1000);
		//}
	}

	/**
	 * Ends the current simulation and close the JavaFX stage of MainScene
	 */
	private void endSim() {
		Simulation.instance().endSimulation();
		getDesktop().clearDesktop();
		//getDesktop().resetDesktop();
		stage.close();
	}

	/**
	 * Performs the process of creating a new simulation.
	 */
	private void newSimulationProcess() {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirm on New");
		alert.setHeaderText(Msg.getString("MainScene.new.header"));
		alert.setContentText(Msg.getString("MainScene.new.content"));
		ButtonType buttonTypeOne = new ButtonType("Save and End the Sim");
		ButtonType buttonTypeTwo = new ButtonType("End the Sim");
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne)	{
			saveOnExit();
			getDesktop().openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
		}
		else if (result.get() == buttonTypeTwo)	{
			getDesktop().openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
		}
/*
			getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.creatingNewSim")); //$NON-NLS-1$
			try {
				Simulation.stopSimulation();
				getDesktop().clearDesktop();
			    //if (earthTimer != null) {
                //    earthTimer.stop();
			    //}
                //earthTimer = null;
				//timeline.stop();

				Simulation.createNewSimulation();

				// Start the simulation.
				Simulation.instance().start();

			}
			catch (Exception e) {
			    // New simulation process should continue even if there's an exception in the UI.
			    logger.severe(e.getMessage());
			    e.printStackTrace(System.err);
			}

			SimulationConfig.loadConfig();

			// NOTE: cyclic dependency does not allow an instance of mainMenu to be referenced in MainScene
			// Therefore, ScenarioConfigEditorFX cannot be loaded from here
			//ScenarioConfigEditorFX editor = new ScenarioConfigEditorFX(mainMenu, SimulationConfig.instance());

			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				getDesktop().resetDesktop();
            }
            catch (Exception e) {
                // New simulation process should continue even if there's an exception in the UI.
                logger.severe(e.getMessage());
                e.printStackTrace(System.err);
            }

			//startEarthTimer();
			getDesktop().disposeAnnouncementWindow();

			// Open user guide tool.
			getDesktop().openToolWindow(GuideWindow.NAME);
            GuideWindow ourGuide = (GuideWindow) getDesktop().getToolWindow(GuideWindow.NAME);
            ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$
			}
*/

	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 * @param useDefault Should the user be allowed to override location?
	 */
	public void saveSimulation(int type) {
		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						saveSimulationProcess(type);
					});
				}
			};
			saveSimThread.start();
		} else {
			saveSimThread.interrupt();
		}
	}

	/**
	 * Performs the process of saving a simulation.
	 */
    // 2015-01-08 Added autosave
	private void saveSimulationProcess(int type) {
		File fileLocn = null;
		String dir = null;
		String title = null;
		// 2015-01-25 Added autosave
		if (type == AUTOSAVE) {
			dir = Simulation.AUTOSAVE_DIR;
			//title = Msg.getString("MainWindow.dialogAutosaveSim"); don't need
		}
		else if (type == DEFAULT || (type == SAVE_AS)) {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainScene.dialogSaveSim");
		}

		if (type == SAVE_AS) {
			FileChooser chooser = new FileChooser();
			File userDirectory = new File(dir);
			chooser.setTitle(title); //$NON-NLS-1$
			chooser.setInitialDirectory(userDirectory);
			 // Set extension filter
	        FileChooser.ExtensionFilter simFilter =
	                new FileChooser.ExtensionFilter("Simulation files (*.sim)", "*.sim");
			FileChooser.ExtensionFilter allFilter =
	                new FileChooser.ExtensionFilter("all files (*.*)", "*.*");
	        chooser.getExtensionFilters().addAll(simFilter, allFilter);
			File selectedFile = chooser.showSaveDialog(stage);
			if (selectedFile != null)
				fileLocn = selectedFile;
			else
				return;
		}

		MasterClock clock = Simulation.instance().getMasterClock();

		if (type == AUTOSAVE) {
			getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.autosavingSim")); //$NON-NLS-1$
			clock.autosaveSimulation(fileLocn);
		}
		else if (type == SAVE_AS || type == DEFAULT) {
			getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.savingSim")); //$NON-NLS-1$
			clock.saveSimulation(fileLocn);
		}

		while (clock.isSavingSimulation() || clock.isAutosavingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.sleepInterrupt"), e); //$NON-NLS-1$
			}
		}
		getDesktop().disposeAnnouncementWindow();
	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.pausingSim")); //$NON-NLS-1$
		Simulation.instance().getMasterClock().setPaused(true);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		Simulation.instance().getMasterClock().setPaused(false);
		getDesktop().disposeAnnouncementWindow();
	}

	/**
	 * Exit the simulation for running and exit.
	 */
	public void exitSimulation() {

		getDesktop().openAnnouncementWindow(Msg.getString("MainScene.exitSim"));

		logger.info("Exiting simulation");

		Simulation sim = Simulation.instance();
/*
		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);

		// Save the simulation.

		try {
			sim.getMasterClock().saveSimulation(null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
			e.printStackTrace(System.err);
		}
*/

		sim.getMasterClock().exitProgram();

	}

	/**
	 * Sets the look and feel of the UI
	 * @param nativeLookAndFeel true if native look and feel should be used.
	 */
	public void setLookAndFeel(boolean nativeLookAndFeel) {
		boolean changed = false;
		if (nativeLookAndFeel) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else {
			try {
				// Set Nimbus look & feel if found in JVM.
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) { //$NON-NLS-1$
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						changed = true;
						break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					changed = true;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
			}
		}

		if (changed) {
		//	SwingUtilities.updateComponentTreeUI(frame);
			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateAnnouncementWindowLF();
				//desktop.updateTransportWizardLF();
			}
		}
	}

	public MainSceneMenu getMainWindowFXMenu() {
		return menuBar;
	}


	public Stage getStage() {
		return stage;
	}

	private void createSwingNode(final SwingNode swingNode) {
		desktop = new MainDesktopPane(this);
		//mainWindow = new MainWindow(true, true);
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(desktop);
    		setLookAndFeel(false);
            //swingNode.setContent(mainWindow);
        });
    }

	public void closeMarsNet() {
		//splitPane.setDividerPositions(1.0f);
	    tp.getSelectionModel().select(swingTab);
	}

	public void openMarsNet() {
		//splitPane.setDividerPositions(0.8f);
	    tp.getSelectionModel().select(settlementTab);
	}


	/**
	 * Creates an Alert Dialog to confirm ending or exiting the simulation or MSP
	 */
    public void alertOnExit() {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirm on Exit");
		alert.setHeaderText(Msg.getString("MainScene.exit.header"));
		alert.setContentText(Msg.getString("MainScene.exit.content"));
		ButtonType buttonTypeOne = new ButtonType("Save and End");
		ButtonType buttonTypeTwo = new ButtonType("End the Sim");
		ButtonType buttonTypeThree = new ButtonType("Exit the MSP");
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne)	{
			saveOnExit();
			getDesktop().openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
		}
		else if (result.get() == buttonTypeTwo)		{
			getDesktop().openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
		}
		else if (result.get() == buttonTypeThree)
			exitSimulation();
    }

	/**
	 * Initiates the process of saving a simulation.
	 */
    public void saveOnExit() {
		getDesktop().openAnnouncementWindow(Msg.getString("MainScene.defaultSaveSim"));
		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);

		// Save the simulation.
		Simulation sim = Simulation.instance();
		try {
			sim.getMasterClock().saveSimulation(null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
			e.printStackTrace(System.err);
		}
    }



	//public void setMainMenu(MainMenu mainMenu) {
	//	this.mainMenu = mainMenu;
	//}


	public void destroy() {
		newSimThread = null;
		loadSimThread = null;
		saveSimThread = null;
	    timeText = null;
	    memUsedText = null;
	    stage = null;
	    swingTab = null;
	    settlementTab = null;
	    tp = null;
	    timeline = null;
	    notificationPane = null;
	    desktop.destroy();
	    desktop = null;
	    menuBar = null;
	    marsNode = null;
	}

}
