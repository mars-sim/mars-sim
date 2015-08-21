/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.08 2015-05-26
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import static javafx.geometry.Orientation.VERTICAL;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.eclipse.fx.ui.controls.tabpane.DndTabPane;
import org.eclipse.fx.ui.controls.tabpane.DndTabPaneFactory;
import org.eclipse.fx.ui.controls.tabpane.DndTabPaneFactory.FeedbackType;
import org.eclipse.fx.ui.controls.tabpane.skin.DnDTabPaneSkin;

import java.io.File;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
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
import javafx.event.ActionEvent;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.swing.AbstractAction;
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

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;
import com.sibvisions.rad.ui.javafx.ext.mdi.FXDesktopPane;

/**
 * The MainScene class is the primary Stage for MSP. It is the container for
 * housing desktop swing node, javaFX UI, pull-down menu and icons for tools.
 */
public class MainScene {

	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	private static int AUTOSAVE_EVERY_X_MINUTE = 10;
	private static final int TIME_DELAY = 980;

	// Categories of loading and saving simulation
	public static final int DEFAULT = 1;
	public static final int AUTOSAVE = 2;
	public static final int OTHER = 3; // load other file
	public static final int SAVE_AS = 3; // save as other file

	private static int theme = 1; // 7 is the standard nimrod theme
	private int memMax;
	private int memTotal;
	private int memUsed, memUsedCache;
	private int memFree;
	private int processCpuLoad;
	private int systemCpuLoad;


	private StringProperty timeStamp;

	private String lookAndFeelTheme = "nimrod";

	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;

	private Text timeText;
	private Text memUsedText;
	private Text memMaxText;
	private Text processCpuLoadText;
	private Text systemCpuLoadText;
	private Button memBtn, clkBtn;

	private Stage stage;
	private Scene scene;

	private Tab swingTab;
	private Tab nodeTab;

	private DndTabPane dndTabPane;
	private FXDesktopPane fxDesktopPane;

	private Timeline timeline, autosaveTimeline;
	private static NotificationPane notificationPane;

	private static MainDesktopPane desktop;
	private MainSceneMenu menuBar;
	private MarsNode marsNode;
	private TransportWizard transportWizard;
	private StackPane rootStackPane;
	private SwingNode swingNode;
	@SuppressWarnings("restriction")
	private OperatingSystemMXBean osBean;

	//static {
   //     Font.loadFont(MainScene.class.getResource("/fxui/fonts/fontawesome-webfont.ttf").toExternalForm(), 10);
    //}

	/**
	 * Constructor for MainScene
	 *
	 * @param stage
	 */
	public MainScene(Stage stage) {
		//logger.info("MainScene's constructor() is on " + Thread.currentThread().getName() + " Thread");
		this.stage = stage;
	}

	/**
	 * Prepares the Main Scene, sets up LookAndFeel UI, starts two timers,
	 * prepares Transport Wizard
	 */
	public void prepareMainScene() {
		//logger.info("MainScene's prepareMainScene() is in " + Thread.currentThread().getName() + " Thread");
		Simulation.instance().getSimExecutor().submit(new MainSceneTask());
	}

	/**
	 * Sets up the UI theme and the two timers as a thread pool task
	 */
	public class MainSceneTask implements Runnable {
		public void run() {
			//logger.info("MainScene's MainSceneTask is in " + Thread.currentThread().getName() + " Thread");
			// Load UI configuration.
			// if (!cleanUI) {
			// UIConfig.INSTANCE.parseFile();
			// }

			// Set look and feel of UI.
			UIConfig.INSTANCE.useUIDefault();

			SwingUtilities.invokeLater(() -> {
				setLookAndFeel(1);
			} );

			//startAutosaveTimer();
			// desktop.openInitialWindows(); // doesn't work here
			//startEarthTimer();
			// System.out.println("done running the two timers");
			// System.out.println("done running createMainScene()");
		}
	}

	public void prepareOthers() {
		//logger.info("MainScene's prepareOthers() is on " + Thread.currentThread().getName() + " Thread");
		transportWizard = new TransportWizard(this, desktop);
		openInitialWindows();
		startAutosaveTimer();
		startEarthTimer();
		//logger.info("done with MainScene's prepareOthers()");
	}

	public void openTransportWizard(BuildingManager buildingManager) {
		transportWizard.initialize(buildingManager);
		transportWizard.deliverBuildings();
	}

	public TransportWizard getTransportWizard() {
		return transportWizard;
	}

	/**
	 * initializes the scene
	 *
	 * @return Scene
	 */
	@SuppressWarnings("unchecked")
	public Scene initializeScene() {
		//logger.info("MainScene's initializeScene() is on " + Thread.currentThread().getName() + " Thread");
		marsNode = new MarsNode(this, stage);

		// Detect if a user hits the top-right close button
		// TODO: determine if it is necessary to exit both the simulation stage
		// and the Main Menu
		// Exit not just the stage but the simulation entirely
		stage.setOnCloseRequest(e -> {
			alertOnExit();
		} );

		// Detect if a user hits ESC
		stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ESCAPE) {
					// Toggle the full screen mode to OFF in the pull-down menu
					// under setting
					menuBar.exitFullScreen();
					// close the MarsNet side panel
					openSwingTab();
				}
			}
		});

		// ImageView bg1 = new ImageView();
		// bg1.setImage(new Image("/images/splash.png")); // in lieu of the
		// interactive Mars map
		// root.getChildren().add(bg1);

		// Create group to hold swingNode1 which holds the swing desktop
		StackPane swingPane = new StackPane();
		swingNode = new SwingNode();
		createSwingNode();
		swingPane.getChildren().add(swingNode);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		swingPane.setPrefWidth(primaryScreenBounds.getWidth());

		// Create ControlFX's StatusBar
		StatusBar statusBar = createStatusBar();
		VBox bottomBox = new VBox();
		bottomBox.getChildren().addAll(statusBar);

		// Create menuBar
		menuBar = new MainSceneMenu(this, desktop);
		menuBar.getStylesheets().addAll("/fxui/css/mainskin.css");

		// Create BorderPane
		BorderPane borderPane = new BorderPane();
		borderPane.setTop(menuBar);
		// borderPane.setTop(toolbar);
		borderPane.setBottom(bottomBox);
		// borderPane.setStyle("-fx-background-color: palegorange");

		// 2015-05-26 Create fxDesktopPane
		fxDesktopPane = marsNode.createFXDesktopPane();

		// 2015-05-26 Create the dndTabPane.
		dndTabPane = new DndTabPane();
		StackPane containerPane = new StackPane(dndTabPane);

		// We need to create the skin manually, could also be your custom skin.
		DnDTabPaneSkin skin = new DnDTabPaneSkin(dndTabPane);

		// Setup the dragging.
		DndTabPaneFactory.setup(FeedbackType.MARKER, containerPane, skin);

		// Set the skin.
		dndTabPane.setSkin(skin);
		dndTabPane.setSide(Side.RIGHT);

		// Create nodeTab
		nodeTab = new Tab();
		nodeTab.setClosable(false);
		nodeTab.setText("JavaFX UI");
		nodeTab.setContent(fxDesktopPane);

		/*
		 * // create a button to toggle floating. final RadioButton floatControl
		 * = new RadioButton("Toggle floating");
		 * floatControl.selectedProperty().addListener(new
		 * ChangeListener<Boolean>() {
		 *
		 * @Override public void changed(ObservableValue<? extends Boolean>
		 * prop, Boolean wasSelected, Boolean isSelected) { if (isSelected) {
		 * tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING); } else {
		 * tabPane.getStyleClass().remove(TabPane.STYLE_CLASS_FLOATING); } } });
		 */

		// Create swing tab to hold classic UI
		swingTab = new Tab();
		swingTab.setClosable(false);
		swingTab.setText("Classic UI");
		swingTab.setContent(swingPane);

		// Set to select the swing tab at the start of simulation
		dndTabPane.getSelectionModel().select(swingTab);
		dndTabPane.getTabs().addAll(swingTab, nodeTab);

		Node notificationNode = createNotificationPane();

		borderPane.setCenter(notificationNode);

		rootStackPane = new StackPane(borderPane);

		Scene scene = new Scene(rootStackPane, primaryScreenBounds.getWidth(), 640, Color.BROWN);
		borderPane.prefHeightProperty().bind(scene.heightProperty());
		borderPane.prefWidthProperty().bind(scene.widthProperty());

		rootStackPane.getStylesheets().add("/fxui/css/mainskin.css");
		rootStackPane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (keyEvent.getCode() == KeyCode.T && keyEvent.isControlDown()) {
					changeTheme();
					SwingUtilities.invokeLater(() -> {
						setLookAndFeel(1);
						swingNode.setContent(desktop);
					} );
				}
			}
		});

		// System.out.println("done running initializeScene()");

		return scene;
	}

	public static void notifyThemeChange(String text) {
		if (desktop != null) {
			// desktop.refreshTheme();
			//System.out.println("deskop != null , calling notifyThemeChange()");
			// if (notificationPane != null) {
			// notificationPane.setText("Skin is set to " + text);
			notificationPane.show("Skin is set to " + text);
			// }
		}
	}

	public void initializeTheme() {
		logger.info("MainScene's initializeTheme()");
		setLookAndFeel(1);
		changeTheme();

/*
		theme = 1;
		rootStackPane.getStylesheets().clear();
		rootStackPane.getStylesheets().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
		notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
		memUsedText.setFill(Color.ORANGE);
		memMaxText.setFill(Color.ORANGE);
		timeText.setFill(Color.ORANGE);
		memBtn.setTextFill(Color.LIGHTSALMON);
		clkBtn.setTextFill(Color.LIGHTSALMON);
		lookAndFeelTheme = "nimrod";
*/
		logger.info("done with MainScene's initializeTheme()");
	}

	public void changeTheme() {
		logger.info("MainScene's changeTheme()");
		if (theme == 1) {
			rootStackPane.getStylesheets().clear();
			theme = 2;
			rootStackPane.getStylesheets().add(getClass().getResource("/fxui/css/oliveskin.css").toExternalForm());
			notificationPane.getStyleClass().remove(NotificationPane.STYLE_CLASS_DARK);
			notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/oliveskin.css").toExternalForm());
			memUsedText.setFill(Color.GREEN);
			memMaxText.setFill(Color.GREEN);
			timeText.setFill(Color.GREEN);
			memBtn.setTextFill(Color.PALEGREEN);
			clkBtn.setTextFill(Color.PALEGREEN);
			lookAndFeelTheme = "LightTabaco";
		} else if (theme == 2) {
			rootStackPane.getStylesheets().clear();
			theme = 3;
			rootStackPane.getStylesheets().add(getClass().getResource("/fxui/css/burgundyskin.css").toExternalForm());
			notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
			notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/burgundyskin.css").toExternalForm());
			memUsedText.setFill(Color.ORANGERED);
			memMaxText.setFill(Color.ORANGERED);
			timeText.setFill(Color.ORANGERED);
			memBtn.setTextFill(Color.YELLOW);
			clkBtn.setTextFill(Color.YELLOW);
			lookAndFeelTheme = "Burdeos";
		} else if (theme == 3) { // dark olive
			rootStackPane.getStylesheets().clear();
			theme = 4;
			rootStackPane.getStylesheets().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
			notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
			memUsedText.setFill(Color.LIGHTCYAN);
			memMaxText.setFill(Color.LIGHTCYAN);
			timeText.setFill(Color.LIGHTCYAN);
			memBtn.setTextFill(Color.DARKOLIVEGREEN);
			clkBtn.setTextFill(Color.DARKOLIVEGREEN);
			lookAndFeelTheme = "DarkTabaco";
		} else if (theme == 4) {
			// rootStackPane.getStylesheets().clear();
			theme = 5;
			// rootStackPane.getStylesheets().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
			memUsedText.setFill(Color.BLANCHEDALMOND);
			memMaxText.setFill(Color.BLANCHEDALMOND);
			timeText.setFill(Color.BLANCHEDALMOND);
			memBtn.setTextFill(Color.GREY);
			clkBtn.setTextFill(Color.GREY);
			lookAndFeelTheme = "DarkGrey";
		} else if (theme == 5) { // + purple
			// rootStackPane.getStylesheets().clear();
			theme = 6;
			// rootStackPane.getStylesheets().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
			memUsedText.setFill(Color.PALEGOLDENROD);
			memMaxText.setFill(Color.PALEGOLDENROD);
			timeText.setFill(Color.PALEGOLDENROD);
			memBtn.setTextFill(Color.BLUEVIOLET);
			clkBtn.setTextFill(Color.BLUEVIOLET);
			lookAndFeelTheme = "Night";
		} else if (theme == 6) { // + skyblue
			// rootStackPane.getStylesheets().clear();
			theme = 7;
			// rootStackPane.getStylesheets().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
			notificationPane.getStyleClass().remove(NotificationPane.STYLE_CLASS_DARK);
			memUsedText.setFill(Color.CADETBLUE);
			memMaxText.setFill(Color.CADETBLUE);
			timeText.setFill(Color.CADETBLUE);
			memBtn.setTextFill(Color.LIGHTBLUE);
			clkBtn.setTextFill(Color.LIGHTBLUE);
			lookAndFeelTheme = "Snow";
		} else if (theme == 7) {
			rootStackPane.getStylesheets().clear();
			theme = 1;
			rootStackPane.getStylesheets().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
			memUsedText.setFill(Color.ORANGE);
			memMaxText.setFill(Color.ORANGE);
			timeText.setFill(Color.ORANGE);
			memBtn.setTextFill(Color.LIGHTSALMON);
			clkBtn.setTextFill(Color.LIGHTSALMON);
			lookAndFeelTheme = "nimrod";
		}

		logger.info("done with MainScene's changeTheme()");
	}

	/**
	 * Creates and starts the earth timer
	 *
	 * @return Scene
	 */
	public void startEarthTimer() {
		// Set up earth time text update
		timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), ae -> updateTimeText()));
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();

	}

	public StatusBar createStatusBar() {
		StatusBar statusBar = new StatusBar();
		statusBar.setText(""); // needed for deleting the default text "OK"
		// statusBar.setAlignment(Pos.BASELINE_RIGHT);
		// statusBar.setStyle("-fx-background-color: gainsboro;");
		// statusBar.setAlignment(Pos.CENTER);
		// statusBar.setStyle("-fx-border-stylel:solid; -fx-border-width:2pt;
		// -fx-border-color:grey; -fx-font: 14 arial; -fx-text-fill: white;
		// -fx-base: #cce6ff;");
		// statusBar.setMinHeight(memMaxText.getBoundsInLocal().getHeight() +
		// 10);
		// statusBar.setMijnWidth (memMaxText.getBoundsInLocal().getWidth() +
		// 10);

		osBean = ManagementFactory.getPlatformMXBean(
				com.sun.management.OperatingSystemMXBean.class);

		processCpuLoad = (int) (osBean.getProcessCpuLoad() * 100D);
		processCpuLoadText = new Text(" Process CPU Load : " + processCpuLoad + " % ");
		processCpuLoadText.setFill(Color.GREY);
		statusBar.getRightItems().add(new Separator(VERTICAL));
		statusBar.getRightItems().add(processCpuLoadText);

		systemCpuLoad = (int) (osBean.getSystemCpuLoad() * 100D);
		systemCpuLoadText = new Text(" System CPU Load : " + systemCpuLoad + " % ");
		systemCpuLoadText.setFill(Color.GREY);
		statusBar.getRightItems().add(new Separator(VERTICAL));
		statusBar.getRightItems().add(systemCpuLoadText);

		memBtn = new Button(" [Memory] ");
		memBtn.setBackground(new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(2), new Insets(1))));
		memBtn.setTextFill(Color.ORANGE);
		statusBar.getRightItems().add(new Separator(VERTICAL));
		statusBar.getRightItems().add(memBtn);

		memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
		memMaxText = new Text(" Total Designated : " + memMax + " MB ");
		memMaxText.setFill(Color.GREY);
		statusBar.getRightItems().add(memMaxText);

		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
		memUsed = memTotal - memFree;
		memUsedText = new Text(" Currently Used : " + memUsed + " MB ");
		memUsedText.setId("mem-text");
		// memUsedText.setStyle("-fx-text-inner-color: orange;");
		memUsedText.setFill(Color.GREY);
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

		timeText = new Text(" Earth Time : " + timeStamp + "  ");
		// timeText.setStyle("-fx-text-inner-color: orange;");
		timeText.setId("time-text");
		timeText.setFill(Color.GREY);

		clkBtn = new Button(" [Clock] ");
		clkBtn.setTextFill(Color.ORANGE);
		clkBtn.setBackground(new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(2), new Insets(1))));
		statusBar.getRightItems().add(clkBtn);
		statusBar.getRightItems().add(timeText);
		statusBar.getRightItems().add(new Separator(VERTICAL));

		return statusBar;
	}

	public NotificationPane getNotificationPane() {
		return notificationPane;
	}

	public Node createNotificationPane() {

		notificationPane = new NotificationPane(dndTabPane);
		String imagePath = getClass().getResource("/notification/notification-pane-warning.png").toExternalForm();
		ImageView image = new ImageView(imagePath);
		notificationPane.setGraphic(image);
		notificationPane.getActions().addAll(new Action("Close", ae -> {
			// do sync, then hide...
			notificationPane.hide();
		} ));

		notificationPane.setShowFromTop(false);
		// notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
		// notificationPane.setText("Breaking news for mars-simmers !!");
		// notificationPane.hide();

		return notificationPane;
	}

	public String getSampleName() {
		return "Notification Pane";
	}

	public String getControlStylesheetURL() {
		return "/org/controlsfx/control/notificationpane.css";
	}

	public void updateTimeText() {

		String t = null;
		// try {
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
			// timeStamp = new SimpleStringProperty(earthclock.getTimeStamp());
		}
		// }
		// catch (Exception ee) {
		// ee.printStackTrace(System.err);
		// }
		timeText.setText(" Earth Time : " + t + "  ");
		// timeText.setStyle("-fx-text-inner-color: orange;");
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
		memUsed = memTotal - memFree;
		// int mem = ( memUsedCache + memUsed ) /2;
		if (memUsed > memUsedCache * 1.1 || memUsed < memUsedCache * 0.9) {
			memUsedText.setText(" Currently Used : " + memUsed + " MB ");
			// memUsedText.setStyle("-fx-text-inner-color: orange;");
		}
		memUsedCache = memUsed;

		processCpuLoad = (int) (osBean.getProcessCpuLoad() * 100D);
		processCpuLoadText.setText(" Process CPU Load : " + processCpuLoad + " % ");
		//processCpuLoadText.setFill(Color.GREY);


		systemCpuLoad = (int) (osBean.getSystemCpuLoad() * 100D);
		systemCpuLoadText.setText(" System CPU Load : " + systemCpuLoad + " % ");
		//systemCpuLoadText.setFill(Color.GREY);

	}

	// 2015-01-07 Added startAutosaveTimer()
	public void startAutosaveTimer() {

		autosaveTimeline = new Timeline(
				new KeyFrame(Duration.seconds(60 * AUTOSAVE_EVERY_X_MINUTE),
						ae -> saveSimulation(AUTOSAVE)));
		autosaveTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		autosaveTimeline.play();

	}

	/**
	 * Gets the timeline instance of the autosave timer.
	 * @return autosaveTimeline
	 */
	public Timeline getAutosaveTimeline() {
		return autosaveTimeline;
	}

	/**
	 * Gets the main desktop panel.
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		// return mainWindow.getDesktop();
		return desktop;
	}

	/**
	 * Load a previously saved simulation.
	 * @param type
	 */
	// 2015-01-25 Added autosave
	public void loadSimulation(int type) {
		logger.info("MainScene's loadSimulation() is on " + Thread.currentThread().getName() + " Thread");

		// if (earthTimer != null)
		// earthTimer.stop();
		// earthTimer = null;

		// timeline.stop(); // Note: no need to stop and restart at all

		if ((loadSimThread == null) || !loadSimThread.isAlive()) {
			loadSimThread = new Thread(Msg.getString("MainWindow.thread.loadSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						loadSimulationProcess(type);
					} );
				}
			};
			loadSimThread.start();
		} else {
			loadSimThread.interrupt();
		}
	}

	/**
	 * Performs the process of loading a simulation.
	 * @param type
	 */
	public void loadSimulationProcess(int type) {
		logger.info("MainScene's loadSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");
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
			// chooser.setInitialFileName(dir);
			// Set to user directory or go to default if cannot access
			// String userDirectoryString = System.getProperty("user.home");
			File userDirectory = new File(dir);
			chooser.setInitialDirectory(userDirectory);
			chooser.setTitle(title); // $NON-NLS-1$

			// Set extension filter
			FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter("Simulation files (*.sim)",
					"*.sim");
			FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("all files (*.*)", "*.*");

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

		// fileLabel.setText(file.getPath());
		desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$
		desktop.clearDesktop();

		MasterClock masterClock = Simulation.instance().getMasterClock();
		Simulation.instance().getClockScheduler().submit(masterClock.getClockThreadTask());

		while (masterClock.isLoadingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.waitInterrupt"), e); //$NON-NLS-1$
			}
		}

		// Simulation.instance().getExecutorServiceThread().submit(new
		// LoadSimulationTask(fileLocn));

		try {
			desktop.resetDesktop();
			logger.info(" loadSimulationProcess() : desktop.resetDesktop()");
		} catch (Exception e) {
			// New simulation process should continue even if there's an
			// exception in the UI.
			logger.severe(e.getMessage());
			e.printStackTrace(System.err);
		}

		// load UI config
		UIConfig.INSTANCE.parseFile();

		desktop.disposeAnnouncementWindow();
		// Open Guide tool after loading.
		//desktop.openToolWindow(GuideWindow.NAME);

	}


	/**
	 * Create a new simulation.
	 */
	public void newSimulation() {
		logger.info("MainScene's newSimulation() is on " + Thread.currentThread().getName() + " Thread");

		if ((newSimThread == null) || !newSimThread.isAlive()) {
			newSimThread = new Thread(Msg.getString("MainWindow.thread.newSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						newSimulationProcess();
					} );
				}
			};
			newSimThread.start();
		} else {
			newSimThread.interrupt();
		}

	}

	/**
	 * Ends the current simulation and close the JavaFX stage of MainScene
	 */
	private void endSim() {
		logger.info("MainScene's endSim() is on " + Thread.currentThread().getName() + " Thread");
		Simulation.instance().endSimulation();
		Simulation.instance().getSimExecutor().shutdownNow();

		// Simulation.instance().destroyOldSimulation();
		getDesktop().clearDesktop();
		// getDesktop().resetDesktop();
		// Simulation.instance().getMasterClock().exitProgram();
		stage.close();
		// Simulation.instance().endMasterClock();
		Simulation.instance().startSimExecutor();
	}

	/**
	 * Performs the process of creating a new simulation.
	 */
	private void newSimulationProcess() {
		logger.info("MainScene's newSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirm on New");
		alert.setHeaderText(Msg.getString("MainScene.new.header"));
		alert.setContentText(Msg.getString("MainScene.new.content"));
		ButtonType buttonTypeOne = new ButtonType("Save and End the Sim");
		ButtonType buttonTypeTwo = new ButtonType("End the Sim");
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne) {
			saveOnExit();
			desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
		} else if (result.get() == buttonTypeTwo) {
			desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
		}
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 *
	 * @param useDefault
	 *            Should the user be allowed to override location?
	 */
	public void saveSimulation(int type) {
		logger.info("MainScene's saveSimulation() is on " + Thread.currentThread().getName() + " Thread");
		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					Platform.runLater(() -> {
						saveSimulationProcess(type);
					} );
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
		logger.info("MainScene's saveSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");
		File fileLocn = null;
		String dir = null;
		String title = null;
		// 2015-01-25 Added autosave
		if (type == AUTOSAVE) {
			dir = Simulation.AUTOSAVE_DIR;
			// title = Msg.getString("MainWindow.dialogAutosaveSim"); don't need
		} else if (type == DEFAULT || (type == SAVE_AS)) {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainScene.dialogSaveSim");
		}

		if (type == SAVE_AS) {
			FileChooser chooser = new FileChooser();
			File userDirectory = new File(dir);
			chooser.setTitle(title); // $NON-NLS-1$
			chooser.setInitialDirectory(userDirectory);
			// Set extension filter
			FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter("Simulation files (*.sim)",
					"*.sim");
			//FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("all files (*.*)", "*.*");
			chooser.getExtensionFilters().add(simFilter); // , allFilter);
			File selectedFile = chooser.showSaveDialog(stage);
			if (selectedFile != null)
				fileLocn = selectedFile; // + Simulation.DEFAULT_EXTENSION;
			else
				return;
		}

		MasterClock clock = Simulation.instance().getMasterClock();

		if (type == AUTOSAVE) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.autosavingSim")); //$NON-NLS-1$
			clock.autosaveSimulation();
		} else if (type == SAVE_AS || type == DEFAULT) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.savingSim")); //$NON-NLS-1$
			clock.saveSimulation(fileLocn);
		}

		while (clock.isSavingSimulation() || clock.isAutosavingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.sleepInterrupt"), e); //$NON-NLS-1$
			}
		}
		desktop.disposeAnnouncementWindow();
	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		desktop.openAnnouncementWindow(Msg.getString("MainWindow.pausingSim")); //$NON-NLS-1$
		autosaveTimeline.pause();
		Simulation.instance().getMasterClock().setPaused(true);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		autosaveTimeline.play();
		Simulation.instance().getMasterClock().setPaused(false);
		desktop.disposeAnnouncementWindow();
	}

	/**
	 * Exit the simulation for running and exit.
	 */
	public void exitSimulation() {
		logger.info("MainScene's exitSimulation() is on " + Thread.currentThread().getName() + " Thread");
		desktop.openAnnouncementWindow(Msg.getString("MainScene.exitSim"));

		logger.info("Exiting simulation");

		Simulation sim = Simulation.instance();
		/*
		 * // Save the UI configuration. UIConfig.INSTANCE.saveFile(this);
		 *
		 * // Save the simulation.
		 *
		 * try { sim.getMasterClock().saveSimulation(null); } catch (Exception
		 * e) { logger.log(Level.SEVERE,
		 * Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
		 * e.printStackTrace(System.err); }
		 */

		sim.getMasterClock().exitProgram();

	}

	/**
	 * Sets the look and feel of the UI
	 *
	 * @param nativeLookAndFeel
	 *            true if native look and feel should be used.
	 */
	// 2015-05-02 Edited setLookAndFeel()
	public void setLookAndFeel(int choice) {
		logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
		boolean changed = false;
		// String currentTheme =
		// UIManager.getLookAndFeel().getClass().getName();
		// System.out.println("CurrentTheme is " + currentTheme);
		if (choice == 0) { // theme == "nativeLookAndFeel"
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
				lookAndFeelTheme = "system";
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else if (choice == 1) { // theme == "nimRODLookAndFeel"
			try {

				if (lookAndFeelTheme.equals("nimrod"))
					// Use default theme
					try {
						UIManager.setLookAndFeel(new NimRODLookAndFeel());
					} catch (Exception e) {
						e.printStackTrace();
					}

				else {
					/*
					 * //TODO: let user customize theme in future NimRODTheme nt
					 * = new NimRODTheme(); nt.setPrimary1(new
					 * java.awt.Color(10,10,10)); nt.setPrimary2(new
					 * java.awt.Color(20,20,20)); nt.setPrimary3(new
					 * java.awt.Color(30,30,30)); NimRODLookAndFeel NimRODLF =
					 * new NimRODLookAndFeel(); NimRODLF.setCurrentTheme( nt);
					 */
					NimRODTheme nt = new NimRODTheme(
							getClass().getClassLoader().getResource("theme/" + lookAndFeelTheme + ".theme"));
					NimRODLookAndFeel nf = new NimRODLookAndFeel();
					nf.setCurrentTheme(nt);
					UIManager.setLookAndFeel(nf);
					changed = true;
				}

			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else if (choice == 2) {
			try {
				// Set Nimbus look & feel if found in JVM.
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) { //$NON-NLS-1$
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						lookAndFeelTheme = "nimbus";
						changed = true;
						break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					lookAndFeelTheme = "metal";
					changed = true;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
			}
		}

		if (changed) {
			SwingUtilities.updateComponentTreeUI(desktop);
			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateUnitWindowLF();
				desktop.updateAnnouncementWindowLF();
				// desktop.updateTransportWizardLF();
			}
		}

		logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
	}

	public MainSceneMenu getMainSceneMenu() {
		return menuBar;
	}

	public Stage getStage() {
		return stage;
	}

	private void createSwingNode() {
		desktop = new MainDesktopPane(this);
		SwingUtilities.invokeLater(() -> {
			setLookAndFeel(1);
			swingNode.setContent(desktop);
		} );
		// desktop.openInitialWindows();
	}

	public SwingNode getSwingNode() {
		return swingNode;
	}

	public void openSwingTab() {
		// splitPane.setDividerPositions(1.0f);
		dndTabPane.getSelectionModel().select(swingTab);
	}

	public void openMarsNet() {
		// splitPane.setDividerPositions(0.8f);
		dndTabPane.getSelectionModel().select(nodeTab);
	}

	/**
	 * Creates an Alert Dialog to confirm ending or exiting the simulation or
	 * MSP
	 */
	public void alertOnExit() {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirm on Exit");
		alert.initOwner(stage);
		alert.setHeaderText(Msg.getString("MainScene.exit.header"));
		alert.setContentText(Msg.getString("MainScene.exit.content"));
		ButtonType buttonTypeOne = new ButtonType("Save and End");
		ButtonType buttonTypeTwo = new ButtonType("End the Sim");
		ButtonType buttonTypeThree = new ButtonType("Exit the MSP");
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne) {
			desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			saveOnExit();
			endSim();
		} else if (result.get() == buttonTypeTwo) {
			desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
		} else if (result.get() == buttonTypeThree)
			exitSimulation();
		else
			return;

	}

	/**
	 * Initiates the process of saving a simulation.
	 */
	public void saveOnExit() {
		logger.info("MainScene's saveOnExit() is on " + Thread.currentThread().getName() + " Thread");

		desktop.openAnnouncementWindow(Msg.getString("MainScene.defaultSaveSim"));
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

	// public void setMainMenu(MainMenu mainMenu) {
	// this.mainMenu = mainMenu;
	// }

	public void openInitialWindows() {
		logger.info("MainScene's openInitialWindows() is on " + Thread.currentThread().getName() + " Thread");
		//SwingUtilities.invokeLater(() -> {
			desktop.openInitialWindows();
		//});
	}

	public MarsNode getMarsNode() {
		return marsNode;
	}

	public static int getTheme() {
		return theme;
	}

	public Scene getScene() {
		return scene;
	}

	public void destroy() {
		newSimThread = null;
		loadSimThread = null;
		saveSimThread = null;
		timeText = null;
		memUsedText = null;
		stage = null;
		swingTab = null;
		nodeTab = null;
		dndTabPane = null;
		timeline = null;
		notificationPane = null;
		desktop.destroy();
		desktop = null;
		menuBar = null;
		marsNode = null;
	}

}
