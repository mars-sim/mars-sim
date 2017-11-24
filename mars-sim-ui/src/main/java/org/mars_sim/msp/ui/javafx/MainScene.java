/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.1.0 2017-10-05
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import com.alee.laf.WebLookAndFeel;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.scene.GameScene;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXSlider.IndicatorPosition;

import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTimePicker;
import com.jfoenix.controls.JFXToggleButton;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

import org.controlsfx.control.MaskerPane;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

//import javafx.stage.Screen;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
//import javafx.geometry.Rectangle2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
//import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
//import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.MenuItem;
//import javafx.scene.control.ButtonType;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.HPos;

import javafx.util.Duration;
import jiconfont.icons.FontAwesome;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
//import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;

import java.awt.BorderLayout;
//import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.UpTimer;
import org.mars_sim.msp.ui.javafx.dashboard.DashboardController;
import org.mars_sim.msp.ui.javafx.demo.spinnerValueFactory.Spinner;
import org.mars_sim.msp.ui.javafx.demo.spinnerValueFactory.SpinnerValueFactory;
import org.mars_sim.msp.ui.javafx.quotation.QuotationPopup;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.tool.StartUpLocation;
import org.mars_sim.msp.ui.swing.tool.construction.ConstructionWizard;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.TransportWizard;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.MarsCalendarDisplay;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;

import static javafx.scene.input.KeyCode.*;
import static org.fxmisc.wellbehaved.event.EventPattern.*;
import static org.fxmisc.wellbehaved.event.InputMap.*;

/**
 * The MainScene class is the primary Stage for MSP. It is the container for
 * housing desktop swing node, javaFX UI, pull-down menu and icons for tools.
 */
@SuppressWarnings("restriction")
public class MainScene {

	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	public final static String CSS_PATH = "/fxui/css/theme/";

	public final static String ORANGE_CSS_JFX = "/fxui/css/tab/jfx_orange.css";
	public final static String BLUE_CSS_JFX = "/fxui/css/tab/jfx_blue.css";

	public final static String ORANGE_CSS_THEME = "/fxui/css/theme/nimrodskin.css";
	public final static String BLUE_CSS_THEME = "/fxui/css/theme/snowBlue.css";

	public static String OS = Simulation.OS.toLowerCase();
	// System.getProperty("os.name").toLowerCase();
	// e.g. 'linux', 'mac os x'

	private static final int TIME_DELAY = SettlementWindow.TIME_DELAY;

	private enum ThemeType {
	    System,
	    Nimbus,
	    Nimrod,
	    Weblaf
	}

	private ThemeType defaultThemeType = ThemeType.Weblaf;

	private static int defaultThemeColor = 0;

	public static final int DASHBOARD_TAB = 0;
	public static final int MAIN_TAB = 1;
	public static final int MAP_TAB = 2;
	public static final int HELP_TAB = 3;

	public static final int LOADING = 0;
	public static final int SAVING = 1;
	public static final int AUTOSAVING = 2;
	public static final int PAUSED = 3;

	public static final int DEFAULT_WIDTH = 1366; // 1920;//
	public static final int DEFAULT_HEIGHT = 768; // 1080;//
	public static final int TAB_PANEL_HEIGHT = 35;
	public static final int TITLE_BAR_HEIGHT = 25;

	private static final double ROTATION_CHANGE = Math.PI / 20D;

	private static final int DEFAULT_ZOOM = 10;
	
	// private static final String ROUND_BUTTONS_DIR = "/icons/round_buttons/";

	// private static final String PAUSE = "PAUSE";
	// private static final String ESC_TO_RESUME = "ESC to resume";
	// private static final String PAUSE_MSG = " [PAUSE]";// : ESC to resume]";
	private static final String LAST_SAVED = "Last Saved : ";
	//private static final String EARTH_DATE_TIME = " ";//EARTH : ";
	private static final String MARS_DATE_TIME = " ";//MARS : ";
	// private static final String UMST = " (UMST)";
	private static final String ONE_SPACE = " ";
	private static final String MONTH = "    Month : ";
	private static final String ORBIT = "Orbit : ";
	private static final String ADIR = "Adir";

	// private static final String BENCHMARK = "Benchmark :";
	private static final String UPTIME = "UpTime :";
	private static final String TPS = "Ticks/s :";
	private static final String SEC = "1 real sec :";
	private static final String TR = "Current TR :";
	private static final String DTR = "Default TR :";
	private static final String HZ = " Hz";

	private static final String SOLAR_LONGITUDE = "Solar Longitude : ";
	private static final String NOTE_MARS = " Note : Mars's now at ";
	private static final String APHELION = "aphelion ";
	private static final String PERIHELION = "perihelion ";

	private static final String NORTH = "Northern : ";
	private static final String SOUTH = "Southern : ";

	private static int theme = 6; // 6 is snow blue; 7 is the mud orange with nimrod
	public static int chatBoxHeight = 256;
	public static int LINUX_WIDTH = 270;
	public static int MACOS_WIDTH = 230;
	public static int WIN_WIDTH = 230;

	public static boolean menuBarVisible = false;
	static boolean isShowingDialog = false;

	private int screen_width = DEFAULT_WIDTH;
	private int screen_height = DEFAULT_HEIGHT;
	private int solCache = 0;

	public float musicSliderValue = AudioPlayer.DEFAULT_VOL * 100;
	public float soundEffectSliderValue = AudioPlayer.DEFAULT_VOL * 100;

	private float musicSliderCache = 0;
	private float effectSliderCache = 0;
	
	private double tpsCache;

	private boolean minimized = false;
	private boolean flag = true;
	private boolean isMainSceneDoneLoading = false;
	private boolean isFullScreenCache = false;
	public static boolean isFXGL = false;
	
	private DoubleProperty sceneWidth;// = new SimpleDoubleProperty(DEFAULt_WIDTH);//1366-40;
	private DoubleProperty sceneHeight;// = new SimpleDoubleProperty(DEFAULt_HEIGHT); //768-40;

	private volatile transient ExecutorService mainSceneExecutor;

	private String upTimeCache = "";
	private String themeSkin = "nimrod";
	private String title = null;
	private String dir = null;
	private String oldLastSaveStamp = null;

	private ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
	
	private GameScene gameScene;
	private Parent root;
	private StackPane rootStackPane, mainStackPane, dashboardStackPane, // monPane,
			mapStackPane, minimapStackPane, speedPane, soundPane, calendarPane, // farmPane,
			settlementBox, chatBoxPane, pausePane, savePane, sPane;

	private AnchorPane anchorPane, mapAnchorPane;
	private SwingNode desktopNode, mapNode, minimapNode;// , guideNode;// monNode, missionNode, resupplyNode, sciNode,
														// guideNode ;
	private Stage stage, loadingStage, savingStage;
	private Scene scene, savingScene;

	private File fileLocn = null;
	private Thread newSimThread;

	private JFXDatePicker datePickerFX;
	private JFXTimePicker timePickerFX;
	
	private IconNode marsTimeIcon;
	
	private HBox earthTimeBox;
	private HBox marsTimeBox;

	private Label marsTimeLabel;
	private Label lastSaveLabel;
	private Label tpsLabel;
	private Label upTimeLabel;
	private Label noteLabel;

	private Button marsTimeButton;

	private Text LSText, monthText, yearText, northText, southText;

	private IconNode soundIcon, marsNetIcon, speedIcon;// , farmIcon;

	private Blend blend;
	private VBox mapLabelBox, speedVBox, soundVBox;
	private Tab mainTab, dashboardTab;

	private Spinner<Integer> spinner;
	private JFXComboBox<Settlement> sBox;
	private JFXToggleButton cacheToggle, minimapToggle, mapToggle;
	private JFXSlider zoomSlider;

	private static JFXSlider musicSlider;
	private static JFXSlider soundEffectSlider; 
	
	private JFXButton soundBtn, marsNetBtn, rotateCWBtn, rotateCCWBtn, recenterBtn, speedBtn;
	private JFXPopup soundPopup, marsNetBox, marsCalendarPopup, simSpeedPopup;
	private JFXTabPane jfxTabPane;
	private JFXDialog exitDialog;
	private CheckBox musicMuteBox, soundEffectMuteBox;
	private ESCHandler esc = null;
	private Timeline timeline;

	private static ChatBox chatBox;
	private static MainDesktopPane desktop;
	private static MainSceneMenu menuBar;

	private static MarsNode marsNode;
	private static TransportWizard transportWizard;
	private static ConstructionWizard constructionWizard;

	private static QuotationPopup quote;

	private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock = sim.getMasterClock();
	private static EarthClock earthClock;
	private static MarsClock marsClock;

	private SettlementWindow settlementWindow;
	private NavigatorWindow navWin;
	private SettlementMapPanel mapPanel;

	private static AudioPlayer soundPlayer;
	private static MarsCalendarDisplay calendarDisplay;
	private static UpTimer uptimer;

	private static OrbitInfo orbitInfo;

	/**
	 * Constructor for MainScene
	 */
	public MainScene(int width, int height, GameScene gameScene) {
		screen_width = width;
		screen_height = height;
		this.gameScene = gameScene;
		sceneWidth = new SimpleDoubleProperty(width);
		sceneHeight = new SimpleDoubleProperty(height - TAB_PANEL_HEIGHT);

		isMainSceneDoneLoading = false;

		if (gameScene != null) {
			stage = ((Stage) gameScene.getRoot().getScene().getWindow());
			isFXGL = true;
		    //stage.initStyle(StageStyle.DECORATED);
		}
		else
			stage = new Stage();
		
		stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
		stage.setMinWidth(sceneWidth.get());
		stage.setMinHeight(sceneHeight.get());
		stage.setFullScreenExitHint(
				"Use Ctrl+F (or Meta+C in macOS) to toggle between either the Full Screen mode and the Window mode");
		stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		// Detect if a user hits the top-right close button
		stage.setOnCloseRequest(e -> {
			if (!isFXGL) {
		        Input input = FXGL.getInput();
				input.mockKeyPress(KeyCode.ESCAPE);
		        input.mockKeyRelease(KeyCode.ESCAPE);
			}
			else {
				dialogOnExit();
				e.consume();
			}
		});

		stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
		    	if (!t.equals(t1)) {
		    		minimized = t1;
		    	}
		    }
		});
		
		// Detect if a user hits ESC
		if (!isFXGL) {
			esc = new ESCHandler();
			setEscapeEventHandler(true, stage);
		}
	}

	/*
	 * Loads the rest of the methods in MainScene.
	 */
	public void finalizeMainScene() {

		Platform.runLater(() -> {
			prepareScene();
			initializeTheme();
			prepareOthers();
			
			// Call setMonitor() for screen detection and placing quotation pop at top right corner
			setMonitor(stage);

			//stage.centerOnScreen();
			stage.setTitle(Simulation.title);
			stage.setResizable(false);
			stage.show();
			stage.requestFocus();
			
			createSavingIndicator();

			openInitialWindows();
			hideWaitStage(MainScene.LOADING);
		});
	}

	/*
	 * Prepares the scene in the main scene
	 */
	public void prepareScene() {
		// logger.info("MainMenu's prepareScene() is on " +
		// Thread.currentThread().getName());
		UIConfig.INSTANCE.useUIDefault();
		// creates and initialize scene
		scene = initializeScene();
		// switch from the main menu's scene to the main scene's scene
		if (gameScene == null) {
			stage.setScene(scene);
		}

	}

	public void createLoadingIndicator() {
		// 2016-10-01 Added mainSceneExecutor for executing wait stages
		startMainSceneExecutor();
		createProgressCircle(LOADING);
	}

	public void createSavingIndicator() {
		createProgressCircle(AUTOSAVING);
		createProgressCircle(SAVING);
	}

	// 2015-12-28 Added setEscapeEventHandler()
	public void setEscapeEventHandler(boolean value, Stage stage) {
		if (value) {
			stage.addEventHandler(KeyEvent.KEY_PRESSED, esc);
		} else {
			stage.removeEventHandler(KeyEvent.KEY_PRESSED, esc);
		}
	}

	class ESCHandler implements EventHandler<KeyEvent> {

		public void handle(KeyEvent t) {
			if (t.getCode() == KeyCode.ESCAPE) {
				if (masterClock.isPaused()) {
					unpauseSimulation();
				} else {
					pauseSimulation(true);
				}
			}
		}
	}

	/**
	 * Sets up the UI theme and the two timers as a thread pool task
	 */
	public class MainSceneTask implements Runnable {
		public void run() {
			logger.info("MainScene's MainSceneTask is in " + Thread.currentThread().getName() + " Thread");
			// Set look and feel of UI.
			UIConfig.INSTANCE.useUIDefault();
		}
	}

	/**
	 * Prepares the transport wizard, construction wizard, autosave timer and earth
	 * timer
	 */
	public void prepareOthers() {
		// logger.info("MainScene's prepareOthers() is on " +
		// Thread.currentThread().getName() + " Thread");
		uptimer = masterClock.getUpTimer();
		startEarthTimer();
		transportWizard = new TransportWizard(this, desktop);
		constructionWizard = new ConstructionWizard(this, desktop);

	}

	/**
	 * Pauses sim and opens the transport wizard
	 * 
	 * @param buildingManager
	 */
	public synchronized void openTransportWizard(BuildingManager buildingManager) {
		// logger.info("MainScene's openTransportWizard() is on " +
		// Thread.currentThread().getName() + " Thread");
		// normally on pool-4-thread-3 Thread
		// Note: make sure pauseSimulation() doesn't interfere with
		// resupply.deliverOthers();
		// 2015-12-16 Track the current pause state
		Platform.runLater(() -> {
			// boolean previous = startPause(); ?
			pauseSimulation(false);
			transportWizard.deliverBuildings(buildingManager);
			unpauseSimulation();
			// endPause(previous);
		});
	}

	public TransportWizard getTransportWizard() {
		return transportWizard;
	}

	/**
	 * Pauses sim and opens the construction wizard
	 * 
	 * @param mission
	 */
	public void openConstructionWizard(BuildingConstructionMission mission) {
		Platform.runLater(() -> {
			// double previous = slowDownTimeRatio();
			pauseSimulation(false);
			constructionWizard.selectSite(mission);
			unpauseSimulation();
			// speedUpTimeRatio(previous);
		});
	}

	// 2015-12-16 Added getConstructionWizard()
	public ConstructionWizard getConstructionWizard() {
		return constructionWizard;
	}

	/**
	 * Setup key events using wellbehavedfx
	 */
	// 2016-11-14 Setup key events using wellbehavedfx
	public void setupKeyEvents() {
		InputMap<KeyEvent> f1 = consume(keyPressed(F1), e -> {
			jfxTabPane.getSelectionModel().select(MainScene.HELP_TAB);
			/*
			 * if (desktop.isToolWindowOpen(GuideWindow.NAME)) SwingUtilities.invokeLater(()
			 * -> desktop.closeToolWindow(GuideWindow.NAME)); else {
			 * //getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
			 * SwingUtilities.invokeLater(() -> desktop.openToolWindow(GuideWindow.NAME)); }
			 */
		});
		Nodes.addInputMap(rootStackPane, f1);

		InputMap<KeyEvent> f2 = consume(keyPressed(F2), e -> {
			if (desktop.isToolWindowOpen(SearchWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(SearchWindow.NAME));
			else {
				// getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(SearchWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f2);

		InputMap<KeyEvent> f3 = consume(keyPressed(F3), e -> {
			if (desktop.isToolWindowOpen(TimeWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(TimeWindow.NAME));
			else {
				// getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(TimeWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f3);

		InputMap<KeyEvent> f4 = consume(keyPressed(F4), e -> {
			if (desktop.isToolWindowOpen(MonitorWindow.NAME)) {
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(MonitorWindow.NAME));
				// rootAnchorPane.getChildren().remove(monPane);
			} else {

				// getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				// rootAnchorPane.getChildren().add(monPane);
				// AnchorPane.setRightAnchor(monPane, 0.0);
				// AnchorPane.setBottomAnchor(monPane, 0.0);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(MonitorWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f4);

		InputMap<KeyEvent> f5 = consume(keyPressed(F5), e -> {
			if (desktop.isToolWindowOpen(MissionWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(MissionWindow.NAME));
			else {
				// getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(MissionWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f5);

		InputMap<KeyEvent> f6 = consume(keyPressed(F6), e -> {
			if (desktop.isToolWindowOpen(ScienceWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(ScienceWindow.NAME));
			else {
				// getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(ScienceWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f6);

		InputMap<KeyEvent> f7 = consume(keyPressed(F7), e -> {
			if (desktop.isToolWindowOpen(ResupplyWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(ResupplyWindow.NAME));
			else {
				// getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(ResupplyWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f7);

		InputMap<KeyEvent> ctrlQ = consume(keyPressed(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)),
				e -> {
					popAQuote();
					mainStackPane.requestFocus();
				});
		Nodes.addInputMap(rootStackPane, ctrlQ);

		InputMap<KeyEvent> ctrlN = consume(keyPressed(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)),
				e -> {
					boolean isNotificationOn = !desktop.getEventTableModel().isNoFiring();
					if (isNotificationOn) {
						menuBar.getNotificationItem().setSelected(false);
						desktop.getEventTableModel().setNoFiring(true);
					} else {
						menuBar.getNotificationItem().setSelected(true);
						desktop.getEventTableModel().setNoFiring(false);
					}
				});
		Nodes.addInputMap(rootStackPane, ctrlN);

		InputMap<KeyEvent> ctrlF = consume(keyPressed(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)),
				e -> {
					boolean isFullScreen = stage.isFullScreen();
					if (!isFullScreen) {
						menuBar.getShowFullScreenItem().setSelected(true);
						if (!isFullScreenCache)
							stage.setFullScreen(true);
					} else {
						menuBar.getShowFullScreenItem().setSelected(false);
						if (isFullScreenCache)
							stage.setFullScreen(false);
					}
					isFullScreenCache = stage.isFullScreen();
				});
		Nodes.addInputMap(rootStackPane, ctrlF);

		InputMap<KeyEvent> ctrlUp = consume(keyPressed(new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN)),
				e -> {
					// soundPlayer.volumeUp();
					soundEffectSlider.setValue(soundEffectSlider.getValue() + .5);
					musicSlider.setValue(musicSlider.getValue() + .5);
					// soundSlider.setValue(convertVolume2Slider(soundPlayer.getVolume() +.05));
				});
		Nodes.addInputMap(rootStackPane, ctrlUp);

		InputMap<KeyEvent> ctrlDown = consume(
				keyPressed(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN)), e -> {
					// soundPlayer.volumeDown();
					soundEffectSlider.setValue(soundEffectSlider.getValue() - .5);
					musicSlider.setValue(musicSlider.getValue() - .5);
					// soundSlider.setValue(convertVolume2Slider(soundPlayer.getVolume() -.05));
				});
		Nodes.addInputMap(rootStackPane, ctrlDown);

		InputMap<KeyEvent> ctrlM = consume(keyPressed(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN)),
				e -> adjustVolume());
		Nodes.addInputMap(rootStackPane, ctrlM);

		InputMap<KeyEvent> ctrlS = consume(keyPressed(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)),
				e -> {
					saveSimulation(Simulation.SAVE_DEFAULT);
				});
		Nodes.addInputMap(rootStackPane, ctrlS);

		InputMap<KeyEvent> ctrlE = consume(keyPressed(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN)),
				e -> {
					saveSimulation(Simulation.SAVE_AS);
				});
		Nodes.addInputMap(rootStackPane, ctrlE);

		InputMap<KeyEvent> ctrlX = consume(keyPressed(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN)),
				e -> {
					if (!isFXGL) {
				        Input input = FXGL.getInput();
						input.mockKeyPress(KeyCode.ESCAPE);
				        input.mockKeyRelease(KeyCode.ESCAPE);
					}
					else {
						dialogOnExit();
						e.consume();
					}
				});
		Nodes.addInputMap(rootStackPane, ctrlX);

		InputMap<KeyEvent> ctrlT = consume(keyPressed(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)),
				e -> {
					if (OS.contains("linux")) {
						// if (theme == 6)
						setTheme(0);
						// else if (theme == 0)
						// setTheme(6);
					} else {
						if (theme == 0 || theme == 6) {
							setTheme(7);
						} else if (theme == 7) {
							setTheme(0);
						}
					}
				});
		Nodes.addInputMap(rootStackPane, ctrlT);

	}

	// Toggle the full screen mode off
	public void updateFullScreenMode() {
		menuBar.getShowFullScreenItem().setSelected(false);
	}

	public void createBlend() {

		blend = new Blend();
		blend.setMode(BlendMode.MULTIPLY);

		DropShadow ds = new DropShadow();
		ds.setColor(Color.rgb(254, 235, 66, 0.3));
		ds.setOffsetX(5);
		ds.setOffsetY(5);
		ds.setRadius(5);
		ds.setSpread(0.2);

		blend.setBottomInput(ds);

		DropShadow ds1 = new DropShadow();
		ds1.setColor(Color.web("#d68268")); // #d68268 is pinkish orange//f13a00"));
		ds1.setRadius(20);
		ds1.setSpread(0.2);

		Blend blend2 = new Blend();
		blend2.setMode(BlendMode.MULTIPLY);

		InnerShadow is = new InnerShadow();
		is.setColor(Color.web("#feeb42")); // #feeb42 is mid-pale yellow
		is.setRadius(9);
		is.setChoke(0.8);
		blend2.setBottomInput(is);

		InnerShadow is1 = new InnerShadow();
		is1.setColor(Color.web("#278206")); // # f13a00 is bright red // 278206 is dark green
		is1.setRadius(5);
		is1.setChoke(0.4);
		blend2.setTopInput(is1);

		Blend blend1 = new Blend();
		blend1.setMode(BlendMode.MULTIPLY);
		blend1.setBottomInput(ds1);
		blend1.setTopInput(blend2);

		blend.setTopInput(blend1);
	}

	public Text createTextHeader(String s) {
		DropShadow ds = new DropShadow();
		ds.setOffsetY(1.0f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));

		Text t = new Text();
		t.setEffect(ds);
		t.setCache(true);
		t.setX(10.0f);
		t.setY(270.0f);
		t.setFill(Color.DARKSLATEGREY);
		t.setText(s);
		t.setFont(Font.font(null, FontWeight.BOLD, 14));
		return t;
	}

	public Label createBlendLabel(String s) {
		Label header_label = new Label(s);
		// header_label.setEffect(blend);
		header_label.setStyle("-fx-text-fill: black;" + "-fx-font-size: 13px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: normal;");
		header_label.setPadding(new Insets(3, 0, 1, 10));
		return header_label;
	}

	public Text createBlendText(String s) {
		Text text = new Text(s);
		// text.setEffect(blend);
		text.setStyle("-fx-text-fill: black;" + "-fx-font-size: 11px;"
		// + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0
		// #000;"
				+ "-fx-font-weight: normal;");
		return text;
	}

	public void adjustVolume() {
		if (!masterClock.isPaused()) {
			
			musicMuteBox.setSelected(!musicMuteBox.isSelected());
			soundEffectMuteBox.setSelected(!soundEffectMuteBox.isSelected());
/*			
			if (musicMuteBox.isSelected()) {// || musicSlider.getValue() > 0) { //musicMuteBox.isSelected()) {//
				//musicMuteBox.setSelected(true);
				mute(false, true);
			} else
				//musicMuteBox.setSelected(false);
				unmute(false, true);
			
			if (soundEffectMuteBox.isSelected()) {// || soundEffectSlider.getValue() > 0) { //soundEffectMuteBox.isSelected()) {//
				//soundEffectMuteBox.setSelected(true);
				mute(true, false);
			} else
				//soundEffectMuteBox.setSelected(false);
				unmute(true, false);
*/			
		}
	}

	public void mute(boolean isEffect, boolean isMusic) {
		if (isMusic) {
			// mute it
			soundPlayer.mute(false, true);
			// save the slider value into the cache
			musicSliderCache = (float) musicSlider.getValue();
			// set the slider value to zero
			musicSlider.setValue(0);
			// check the music mute item in menuBar
			menuBar.getMusicMuteItem().setSelected(true);
		}
		if (isEffect) {
			// mute it
			soundPlayer.mute(true, false);
			// save the slider value into the cache
			effectSliderCache = (float) soundEffectSlider.getValue();
			// set the slider value to zero
			soundEffectSlider.setValue(0);
			// check the sound effect mute item in menuBar
			menuBar.getSoundEffectMuteItem().setSelected(true);
			
			
		}
		//soundPlayer.pause(isEffect, isMusic);
	}

	public void unmute(boolean isEffect, boolean isMusic) {
		if (isMusic) {
			// unmute it
			soundPlayer.unmute(false, true);
			// restore the slider value from the cache
			musicSlider.setValue(musicSliderCache);
			// uncheck the music mute item in menuBar
			menuBar.getMusicMuteItem().setSelected(false);
			
		}
		if (isEffect) {
			// unmute it
			soundPlayer.unmute(true, false);
			// restore the slider value from the cache
			soundEffectSlider.setValue(effectSliderCache);
			// uncheck the sound effect mute item in menuBar
			menuBar.getSoundEffectMuteItem().setSelected(false);
		}

		//soundPlayer.restore(isEffect, isMusic);

	}

	/**
	 * initializes the scene
	 *
	 * @return Scene
	 */
	@SuppressWarnings("unchecked")
	public Scene initializeScene() {
		IconFontFX.register(FontAwesome.getIconFont());

		createDesktopNode();
		// JFXToolbar toolbar = new JFXToolbar();

		// Load soundPlayer instance from desktop right after desktop has been
		// instantiated.
		soundPlayer = desktop.getSoundPlayer();
		// soundPlayer.setEffectVolume(sound_effect_volume);
		// soundPlayer.setMusicVolume(music_volume);

		// Setup root for embedding key events
		root = new Pane();// Group();

		marsNetBox = createFlyout();
		flag = false;

		// EffectUtilities.makeDraggable(flyout.getScene().getRoot().getStage(),
		// chatBox);
		// Create ControlFX's StatusBar
		// statusBar = createStatusBar();

		createBlend();

		createLastSaveBar();
		createMarsTimeBar();
		createEarthTimeBar();

		createSpeedPanel();
		createSoundPopup();
		// createFarmPopup();

		// Create menuBar
		menuBar = new MainSceneMenu(this, desktop);
		// Create Snackbar
		// createJFXSnackbar();
		// Create jfxTabPane
		createJFXTabs();

		pausePane = new StackPane();
		pausePane.setStyle("-fx-background-color:rgba(0,0,0,0.5);");
		pausePane.getChildren().add(createPausePaneContent());
		pausePane.setPrefSize(150, 150);

		if (OS.contains("mac")) {
			((MenuBar) menuBar).useSystemMenuBarProperty().set(true);
		}

		// AnchorPane.setBottomAnchor(jfxTabPane, 0.0);
		AnchorPane.setLeftAnchor(jfxTabPane, 0.0);
		AnchorPane.setRightAnchor(jfxTabPane, 0.0);
		AnchorPane.setTopAnchor(jfxTabPane, 0.0);

		// AnchorPane.setRightAnchor(badgeIcon, 5.0);
		// AnchorPane.setTopAnchor(badgeIcon, 0.0);

		if (OS.contains("win")) {
			AnchorPane.setTopAnchor(speedBtn, 3.0);
			AnchorPane.setTopAnchor(marsNetBtn, 3.0);
			AnchorPane.setTopAnchor(lastSaveLabel, 1.0);
			AnchorPane.setTopAnchor(soundBtn, 3.0);
			// AnchorPane.setTopAnchor(farmBtn, 3.0);
			AnchorPane.setTopAnchor(earthTimeBox, 0.0);
			AnchorPane.setTopAnchor(marsTimeBox, 0.0);
		}

		else if (OS.contains("linux")) {
			AnchorPane.setTopAnchor(speedBtn, 0.0);
			AnchorPane.setTopAnchor(marsNetBtn, 0.0);
			AnchorPane.setTopAnchor(lastSaveLabel, 1.0);
			AnchorPane.setTopAnchor(soundBtn, 0.0);
			// AnchorPane.setTopAnchor(farmBtn, 0.0);
			AnchorPane.setTopAnchor(earthTimeBox, 0.0);
			AnchorPane.setTopAnchor(marsTimeBox, 0.0);
		}

		else if (OS.contains("mac")) {
			AnchorPane.setTopAnchor(speedBtn, 0.0);
			AnchorPane.setTopAnchor(marsNetBtn, 0.0);
			AnchorPane.setTopAnchor(lastSaveLabel, 0.0);
			AnchorPane.setTopAnchor(soundBtn, 0.0);
			// AnchorPane.setTopAnchor(farmBtn, 0.0);
			AnchorPane.setTopAnchor(earthTimeBox, 0.0);
			AnchorPane.setTopAnchor(marsTimeBox, 0.0);
		}

		AnchorPane.setRightAnchor(speedBtn, 5.0);
		AnchorPane.setRightAnchor(marsNetBtn, 45.0);
		AnchorPane.setRightAnchor(soundBtn, 85.0);
		AnchorPane.setRightAnchor(marsTimeBox, sceneWidth.get() / 2);
		AnchorPane.setRightAnchor(earthTimeBox, sceneWidth.get() / 2 - earthTimeBox.getPrefWidth() - 30);
		AnchorPane.setRightAnchor(lastSaveLabel, 105.0);

		anchorPane = new AnchorPane();
		anchorPane.getChildren().addAll(jfxTabPane, marsNetBtn, speedBtn, lastSaveLabel, earthTimeBox, marsTimeBox,
				soundBtn);// , farmBtn);//badgeIcon,borderPane, timeBar, snackbar

		// Set up stackPane for anchoring the JFXDialog box and others
		rootStackPane = new StackPane(anchorPane);
		rootStackPane.setPrefWidth(sceneWidth.get());
		rootStackPane.setPrefHeight(sceneHeight.get());
		// rootStackPane.setBackground(new Background(new
		// BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

		if (gameScene != null) {
			scene = gameScene.getRoot().getScene();
			gameScene.addUINode(rootStackPane);
		}
		else {
			scene = new Scene(rootStackPane, sceneWidth.get(), sceneHeight.get(), Color.TRANSPARENT);// , Color.BROWN);
		}

		// pausePane.prefWidthProperty().bind(scene.widthProperty());
		// pausePane.prefHeightProperty().bind(scene.heightProperty());
		pausePane.setLayoutX((sceneWidth.get() - pausePane.getPrefWidth()) / 2D);
		pausePane.setLayoutY((sceneHeight.get() - pausePane.getPrefHeight()) / 2D);

		jfxTabPane.prefHeightProperty().bind(scene.heightProperty());// .subtract(TITLE_HEIGHT));
		jfxTabPane.prefWidthProperty().bind(scene.widthProperty());

		dashboardStackPane.prefHeightProperty().bind(scene.heightProperty());// .subtract(TITLE_HEIGHT));
		dashboardStackPane.prefWidthProperty().bind(scene.widthProperty());

		mainStackPane.prefHeightProperty().bind(scene.heightProperty());// .subtract(TITLE_HEIGHT));
		mainStackPane.prefWidthProperty().bind(scene.widthProperty());

		// anchorTabPane is within jfxTabPane
		mapAnchorPane.prefHeightProperty().bind(scene.heightProperty());// .subtract(TITLE_HEIGHT));
		mapAnchorPane.prefWidthProperty().bind(scene.widthProperty());

		// Setup key events using wellbehavedfx
		setupKeyEvents();

		return scene;
	}

	public void createEarthTimeBar() {

		if (masterClock == null)
			masterClock = sim.getMasterClock();

		if (earthClock == null)
			earthClock = masterClock.getEarthClock();

		datePickerFX = new JFXDatePicker();
		datePickerFX.setValue(earthClock.getLocalDate());
		datePickerFX.setEditable(false);
		//datePickerFX.setDefaultColor(Color.valueOf("#065185"));
		//datePickerFX.setOverLay(true);
		datePickerFX.setShowWeekNumbers(true);
		datePickerFX.setPromptText("Earth Date");
		datePickerFX.setId("earth-time");

		setQuickToolTip(datePickerFX, "Earth Date");
		
		timePickerFX = new JFXTimePicker();
		timePickerFX.setValue(earthClock.getLocalTime());
		timePickerFX.setIs24HourView(true);
		timePickerFX.setEditable(false);
		//timePickerFX.setDefaultColor(Color.valueOf("#065185"));
		//blueDatePicker.setOverLay(true);
		timePickerFX.setPromptText("Earth Time");
		timePickerFX.setId("earth-time");
		
		setQuickToolTip(timePickerFX, "Earth Time in UTC");
		
		HBox box = new HBox(5, datePickerFX, timePickerFX);

		earthTimeBox = new HBox(box);
		earthTimeBox.setId("earth-time-box");
		earthTimeBox.setAlignment(Pos.CENTER);

		
		if (OS.contains("linux")) {
			earthTimeBox.setPrefSize(50, 25); // 270
			datePickerFX.setPrefSize(25, 25);//160, 29);
			timePickerFX.setPrefSize(25, 25);//140, 29);
		} else if (OS.contains("mac")) {
			earthTimeBox.setPrefSize(50, 23);  // 230
			datePickerFX.setPrefSize(23, 23);
			timePickerFX.setPrefSize(23, 23);
		} else {
			earthTimeBox.setPrefSize(50, 25);
			datePickerFX.setPrefSize(25, 25);//130, 25);
			timePickerFX.setPrefSize(25, 25);//110, 25);
		}
		



	}

	/**
	 * Creates and returns the panel for simulation speed and time info
	 */
	public void createSpeedPanel() {
		spinner = new Spinner<Integer>();
		speedBtn = new JFXButton();
		// speedBtn.setStyle(value);
		// speedBtn.getStyleClass().add("menu-button");//"button-raised");
		speedIcon = new IconNode(FontAwesome.CLOCK_O);
		speedIcon.setIconSize(20);
		// speedIcon.setFill(Color.YELLOW);
		// speedIcon.setStroke(Color.WHITE);

		speedBtn.setMaxSize(20, 20);
		speedBtn.setGraphic(speedIcon);
		setQuickToolTip(speedBtn, "Click to open Speed Panel");
		speedBtn.setOnAction(e -> {
			int current = (int) masterClock.getTimeRatio();
			spinner.getValueFactory().setValue(current);

			if (simSpeedPopup.isShowing()) {
				simSpeedPopup.hide();// close();
			} else {
				simSpeedPopup.show(speedBtn, PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
			}
			e.consume();
		});

		speedPane = new StackPane();
		// speedPane.setEffect(blend);
		speedPane.getStyleClass().add("jfx-popup-container");
		speedPane.setAlignment(Pos.CENTER);
		speedPane.setPrefHeight(100);
		speedPane.setPrefWidth(200);// earthTimeButton.getPrefWidth());
		simSpeedPopup = new JFXPopup(speedPane);

		Text header_label = createTextHeader("SPEED PANEL");

		int default_ratio = (int) masterClock.getCalculatedTimeRatio();
		StringBuilder s0 = new StringBuilder();

		Label default_ratio_label0 = new Label(DTR);
		// time_ratio_label0.setEffect(blend);
		default_ratio_label0.setAlignment(Pos.CENTER_RIGHT);
		default_ratio_label0.setStyle("-fx-text-fill: #206982;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: normal;");
		default_ratio_label0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(default_ratio_label0, "The default time-ratio is the ratio of simulation time to real time"); //$NON-NLS-1$

		Label spinner_label0 = new Label(TR);
		// time_ratio_label0.setEffect(blend);
		spinner_label0.setAlignment(Pos.CENTER_RIGHT);
		spinner_label0.setStyle("-fx-text-fill: #206982;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: normal;");
		spinner_label0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(spinner_label0, "The current time-ratio is the ratio of simulation time to real time"); //$NON-NLS-1$

		Label default_ratio_label = new Label();
		// time_ratio_label.setEffect(blend);
		default_ratio_label.setStyle("-fx-text-fill: #206982;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: normal;");
		// default_ratio_label.setPadding(new Insets(1, 1, 1, 5));
		default_ratio_label.setAlignment(Pos.CENTER);
		// s0.append((int)initial_time_ratio).append(DEFAULT).append(default_ratio).append(CLOSE_PAR);
		s0.append(default_ratio);
		default_ratio_label.setText(s0.toString());
		setQuickToolTip(default_ratio_label, "e.g. if 128, then 1 real second equals 128 sim seconds"); //$NON-NLS-1$

		Label real_time_label0 = new Label(SEC);
		// real_time_label0.setEffect(blend);
		real_time_label0.setAlignment(Pos.CENTER_RIGHT);
		real_time_label0.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: italic;");
		real_time_label0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(real_time_label0, "the amount of simulation time per real second"); //$NON-NLS-1$

		Label real_time_label = new Label();
		// real_time_label.setEffect(blend);
		real_time_label.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: italic;");
		// real_time_label.setPadding(new Insets(1, 1, 1, 5));
		real_time_label.setAlignment(Pos.CENTER);
		setQuickToolTip(real_time_label,
				"e.g. 02m.08s means that 1 real second equals 2 real minutes & 8 real seconds"); //$NON-NLS-1$

		StringBuilder s1 = new StringBuilder();
		double ratio = masterClock.getTimeRatio();
		// String factor = String.format(Msg.getString("TimeWindow.timeFormat"), ratio);
		// //$NON-NLS-1$
		s1.append(masterClock.getTimeTruncated(ratio));
		real_time_label.setText(s1.toString());

		spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

		List<Integer> items = null;
		if (default_ratio == 16)
			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512);
		else if (default_ratio == 32)
			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512);// ,1024);
		else if (default_ratio == 64)
			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024);// , 2048);
		else if (default_ratio == 128)
			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048);// ,4096,8192);
		else // if (default_ratio == 256)
			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096);// ,8192);

		spinner.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(items));
		spinner.setMaxSize(95, 15);
		spinner.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 11px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight:bold;");
		// spinner.setAlignment(Pos.CENTER);
		spinner.getValueFactory().setValue(default_ratio);
		spinner.valueProperty().addListener((o, old_val, new_val) -> {

			if (old_val != new_val) {
				// newTimeRatio = value;
				int value = (int) new_val;
				// boolean previous = startPause();
				masterClock.setTimeRatio(value);
				// endPause(previous);

				StringBuilder s3 = new StringBuilder();
				s3.append(masterClock.getTimeTruncated(value));
				real_time_label.setText(s3.toString());

			}
		});

		Label tpsLabel0 = new Label(TPS);
		// TPSLabel0.setEffect(blend);
		tpsLabel0.setAlignment(Pos.CENTER_RIGHT);
		tpsLabel0.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: italic;");
		tpsLabel0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(tpsLabel0, "how often the simulation updates the changes"); //$NON-NLS-1$

		tpsLabel = new Label();
		// TPSLabel.setEffect(blend);
		tpsLabel.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: italic;");
		// TPSLabel.setPadding(new Insets(1, 1, 1, 5));
		tpsLabel.setAlignment(Pos.CENTER);
		tpsLabel.setText(masterClock.getPulsesPerSecond() + HZ);
		setQuickToolTip(tpsLabel, "e.g. 6.22 Hz means for each second, the simulation is updated 6.22 times"); //$NON-NLS-1$

		Label upTimeLabel0 = new Label(UPTIME);
		// upTimeLabel0.setEffect(blend);
		upTimeLabel0.setAlignment(Pos.CENTER_RIGHT);
		upTimeLabel0.setTextAlignment(TextAlignment.RIGHT);
		upTimeLabel0.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: italic;");
		upTimeLabel0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(upTimeLabel0, "how long the simulation has been up running"); //$NON-NLS-1$

		upTimeLabel = new Label();
		upTimeLabel.setAlignment(Pos.CENTER);
		// upTimeLabel.setEffect(blend);
		upTimeLabel.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 12px;"
				+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
				+ "-fx-font-weight: italic;");
		upTimeLabel.setPadding(new Insets(1, 1, 1, 2));
		if (uptimer != null)
			upTimeLabel.setText(uptimer.getUptime());
		setQuickToolTip(upTimeLabel, "e.g. 03m 05s means 3 minutes and 5 seconds"); //$NON-NLS-1$
		/*
		 * Label benchmarkLabel0 = new Label(BENCHMARK); //
		 * upTimeLabel0.setEffect(blend);
		 * benchmarkLabel0.setAlignment(Pos.CENTER_RIGHT);
		 * benchmarkLabel0.setTextAlignment(TextAlignment.RIGHT);
		 * benchmarkLabel0.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 12px;" +
		 * "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
		 * + "-fx-font-weight: italic;"); benchmarkLabel0.setPadding(new Insets(1, 1, 1,
		 * 2)); setQuickToolTip(benchmarkLabel0,
		 * "how well this machine perform in mars-sim \n (the lower the number the better the performance)"
		 * ); //$NON-NLS-1$
		 * 
		 * benchmarkLabel = new Label(); benchmarkLabel.setAlignment(Pos.CENTER); //
		 * upTimeLabel.setEffect(blend);
		 * benchmarkLabel.setStyle("-fx-text-fill: #065185;" + "-fx-font-size: 12px;" +
		 * "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
		 * + "-fx-font-weight: italic;"); benchmarkLabel.setPadding(new Insets(1, 1, 1,
		 * 2)); benchmarkLabel.setText(masterClock.getDiffCache() + "");
		 * setQuickToolTip(benchmarkLabel, "a real time metric of performance");
		 * //$NON-NLS-1$
		 */
		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("jfx-popup-container");
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(1, 1, 1, 1));
		gridPane.setHgap(1.0);
		gridPane.setVgap(1.0);

		ColumnConstraints right = new ColumnConstraints();
		right.setPrefWidth(120);// earthTimeButton.getPrefWidth() * .6);
		ColumnConstraints left = new ColumnConstraints();
		left.setPrefWidth(80);// earthTimeButton.getPrefWidth() * .4);

		GridPane.setConstraints(spinner, 1, 0);
		GridPane.setConstraints(default_ratio_label, 1, 1);
		GridPane.setConstraints(real_time_label, 1, 2);
		GridPane.setConstraints(tpsLabel, 1, 3);
		GridPane.setConstraints(upTimeLabel, 1, 4);
		// GridPane.setConstraints(benchmarkLabel, 1, 5);

		GridPane.setConstraints(spinner_label0, 0, 0);
		GridPane.setConstraints(default_ratio_label0, 0, 1);
		GridPane.setConstraints(real_time_label0, 0, 2);
		GridPane.setConstraints(tpsLabel0, 0, 3);
		GridPane.setConstraints(upTimeLabel0, 0, 4);
		// GridPane.setConstraints(benchmarkLabel0, 0, 5);

		GridPane.setHalignment(spinner, HPos.CENTER);
		GridPane.setHalignment(default_ratio_label, HPos.CENTER);
		GridPane.setHalignment(real_time_label, HPos.CENTER);
		GridPane.setHalignment(tpsLabel, HPos.CENTER);
		GridPane.setHalignment(upTimeLabel, HPos.CENTER);
		// GridPane.setHalignment(benchmarkLabel, HPos.CENTER);

		GridPane.setHalignment(spinner_label0, HPos.RIGHT);
		GridPane.setHalignment(default_ratio_label0, HPos.RIGHT);
		GridPane.setHalignment(real_time_label0, HPos.RIGHT);
		GridPane.setHalignment(tpsLabel0, HPos.RIGHT);
		GridPane.setHalignment(upTimeLabel0, HPos.RIGHT);
		// GridPane.setHalignment(benchmarkLabel0, HPos.RIGHT);

		gridPane.getColumnConstraints().addAll(left, right);
		gridPane.getChildren().addAll(spinner_label0, spinner, default_ratio_label0, default_ratio_label,
				real_time_label0, real_time_label, tpsLabel0, tpsLabel, upTimeLabel0, upTimeLabel);// , benchmarkLabel0,
		// benchmarkLabel);

		speedVBox = new VBox();
		speedVBox.getStyleClass().add("jfx-popup-container");
		speedVBox.setPadding(new Insets(2, 2, 2, 2));
		speedVBox.setAlignment(Pos.CENTER);
		speedVBox.getChildren().addAll(header_label, gridPane); // timeSliderBox
		speedPane.getChildren().addAll(speedVBox);

	}
	/*
	 * public String timeRatioString(int t) { String s = null; if (t < 10) s = "   "
	 * + t; else if (t < 100) s = "  " + t; else if (t < 1000) s = " " + t; else s =
	 * "" + t; return s; }
	 */
	/*
	 * public static Label createIconLabel(String iconName, int iconSize){ return
	 * LabelBuilder.create() .text(iconName) .styleClass("icons")
	 * .style("-fx-font-size: " + iconSize + "px;") .build(); }
	 */

	/**
	 * Creates and returns the sound popup box
	 */
	public void createSoundPopup() {
		soundBtn = new JFXButton();
		// soundBtn.getStyleClass().add("menu-button");//"button-raised");
		// Icon icon = new Icon("MUSIC");
		// icon.setCursor(Cursor.HAND);
		// icon.setStyle("-fx-background-color: orange;");
		// value.setPadding(new Insets(1));
		// Label bell = createIconLabel("\uf0a2", 15);
		// IconFontFX.register(FontAwesome.getIconFont());
		soundIcon = new IconNode(FontAwesome.MUSIC);// .BELL_O);
		soundIcon.setIconSize(20);
		// soundIcon.setFill(Color.YELLOW);
		// soundIcon.setStroke(Color.WHITE);

		soundBtn.setMaxSize(20, 20);
		soundBtn.setGraphic(soundIcon);
		setQuickToolTip(soundBtn, "Click to open Sound Panel");

		soundBtn.setOnAction(e -> {
			if (soundPopup.isShowing()) {
				soundPopup.hide();// close();
			} else {
				soundPopup.show(soundBtn, PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
			}
			e.consume();
		});

		soundPane = new StackPane();
		// soundPane.setEffect(blend);
		soundPane.getStyleClass().add("jfx-popup-container");
		soundPane.setAlignment(Pos.CENTER);
		soundPane.setPrefHeight(100);
		soundPane.setPrefWidth(300);

		soundPopup = new JFXPopup(soundPane);

		Text header_label = createTextHeader("SOUND PANEL");

		// Set up a settlement view zoom bar
		musicSlider = new JFXSlider();
		// soundSlider.setEffect(blend);
		musicSlider.getStyleClass().add("jfx-slider");
		// soundSlider.setEffect(blend);
		musicSlider.setPrefWidth(220);
		musicSlider.setPrefHeight(20);
		musicSlider.setPadding(new Insets(0, 15, 0, 15));

		musicSlider.setMin(0);
		musicSlider.setMax(100);
		musicSlider.setValue(musicSliderValue);// convertVolume2Slider(soundPlayer.getMusicGain()));
		musicSlider.setMajorTickUnit(20);
		// soundSlider.setMinorTickCount();
		musicSlider.setShowTickLabels(true);
		musicSlider.setShowTickMarks(true);
		musicSlider.setSnapToTicks(true);
		musicSlider.setBlockIncrement(5);
		musicSlider.setOrientation(Orientation.HORIZONTAL);
		musicSlider.setIndicatorPosition(IndicatorPosition.RIGHT);
		setQuickToolTip(musicSlider, "Adjust the background music volume"); //$NON-NLS-1$
		// detect dragging
		musicSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				if (old_val != new_val) {
					float sliderValue = new_val.floatValue();
					// Set to the new music volume in the sound player 
					soundPlayer.setMusicVolume((float) convertSlider2Volume(sliderValue));
					if (sliderValue <= 0) {
						// check the music mute box
						musicMuteBox.setSelected(true);
					} else {
						// uncheck the music mute box
						musicMuteBox.setSelected(false);
					}
				}
			}
		});
		// Background sound track
		Label trackLabel = createBlendLabel("Background Music");
		trackLabel.setPadding(new Insets(0, 0, 0, 0));

		musicMuteBox = new JFXCheckBox("mute");
		musicMuteBox.setStyle("-fx-background-color: linear-gradient(to bottom, -fx-base, derive(-fx-base,30%));"
				+ "-fx-font: bold 9pt 'Corbel';" + "-fx-text-fill: #654b00;");
		// cb.setPadding(new Insets(0,0,0,5));
		musicMuteBox.setAlignment(Pos.CENTER);
		
		musicMuteBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	//System.out.println("oldValue : " + oldValue + "   newValue : " + newValue);
		    	if (oldValue != newValue) {
			    	musicMuteBox.setSelected(newValue);
					if (!masterClock.isPaused()) {			
						if (musicMuteBox.isSelected()) {				
							mute(false, true);	
						} else {					
							unmute(false, true);			
						}
					}
		    	}
		    }
		});
/*		
		musicMuteBox.setOnAction(e -> {
			if (!masterClock.isPaused()) {
				if (musicMuteBox.isSelected()) {					
					mute(false, true);			
				} else {				
					unmute(false, true);			
				}
			} 
			e.consume();
		});
*/
		// Set up a settlement view zoom bar
		soundEffectSlider = new JFXSlider();
		// soundSlider.setEffect(blend);
		soundEffectSlider.getStyleClass().add("jfx-slider");
		// soundSlider.setEffect(blend);
		soundEffectSlider.setPrefWidth(220);
		soundEffectSlider.setPrefHeight(20);
		soundEffectSlider.setPadding(new Insets(0, 15, 0, 15));

		soundEffectSlider.setMin(0);
		soundEffectSlider.setMax(100);
		soundEffectSlider.setValue(soundEffectSliderValue);// convertVolume2Slider(soundPlayer.getEffectGain()));
		soundEffectSlider.setMajorTickUnit(20);
		// soundSlider.setMinorTickCount();
		soundEffectSlider.setShowTickLabels(true);
		soundEffectSlider.setShowTickMarks(true);
		soundEffectSlider.setSnapToTicks(true);
		soundEffectSlider.setBlockIncrement(5);
		soundEffectSlider.setOrientation(Orientation.HORIZONTAL);
		soundEffectSlider.setIndicatorPosition(IndicatorPosition.RIGHT);
		setQuickToolTip(soundEffectSlider, "Adjust the sound effect volume"); //$NON-NLS-1$
		// detect dragging
		soundEffectSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {

				if (old_val != new_val) {
					float sliderValue = new_val.floatValue();
					// Set to the new sound effect volume in the sound player 
					soundPlayer.setSoundVolume((float) convertSlider2Volume(sliderValue));
					
					if (sliderValue <= 0) {
						// check the sound effect mute box
						soundEffectMuteBox.setSelected(true);
					} else {
						// uncheck the sound effect mute box
						soundEffectMuteBox.setSelected(false);
					}
				}
			}
		});

		// Sound effect
		Label effectLabel = createBlendLabel("Sound Effect");
		effectLabel.setPadding(new Insets(0, 0, 0, 1));

		soundEffectMuteBox = new JFXCheckBox("mute");
		soundEffectMuteBox.setStyle("-fx-background-color: linear-gradient(to bottom, -fx-base, derive(-fx-base,30%));"
				+ "-fx-font: bold 9pt 'Corbel';" + "-fx-text-fill: #654b00;");
		// cb.setPadding(new Insets(0,0,0,5));
		soundEffectMuteBox.setAlignment(Pos.CENTER);
		
		soundEffectMuteBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	if (oldValue != newValue) {
			    	soundEffectMuteBox.setSelected(newValue);
					if (!masterClock.isPaused()) {			
						if (soundEffectMuteBox.isSelected()) {				
							mute(true, false);
						} else {					
							unmute(true, false);			
						}
					}
		    	}
		    }
		});
/*		
		soundEffectMuteBox.setOnAction(e -> {
			if (!masterClock.isPaused()) {			
				if (soundEffectMuteBox.isSelected()) {				
					mute(true, false);
				} else {					
					unmute(true, false);			
				}
			}
			e.consume();
		});
*/
		// Label empty = new Label();

		GridPane gridPane0 = new GridPane();
		gridPane0.getStyleClass().add("jfx-popup-container");
		gridPane0.setAlignment(Pos.CENTER);
		gridPane0.setPadding(new Insets(1, 1, 1, 1));
		gridPane0.setHgap(1.0);
		gridPane0.setVgap(1.0);

		GridPane gridPane1 = new GridPane();
		gridPane1.getStyleClass().add("jfx-popup-container");
		gridPane1.setAlignment(Pos.CENTER);
		gridPane1.setPadding(new Insets(1, 1, 1, 1));
		gridPane1.setHgap(1.0);
		gridPane1.setVgap(1.0);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPrefWidth(60);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPrefWidth(120);

		GridPane.setConstraints(trackLabel, 0, 0);
		GridPane.setConstraints(musicMuteBox, 1, 0);
		GridPane.setConstraints(effectLabel, 0, 0);
		GridPane.setConstraints(soundEffectMuteBox, 1, 0);

		GridPane.setHalignment(trackLabel, HPos.LEFT);
		GridPane.setHalignment(musicMuteBox, HPos.RIGHT);
		GridPane.setHalignment(effectLabel, HPos.LEFT);
		GridPane.setHalignment(soundEffectMuteBox, HPos.RIGHT);

		gridPane0.getColumnConstraints().addAll(c1, c0);
		gridPane0.getChildren().addAll(trackLabel, musicMuteBox);

		gridPane1.getColumnConstraints().addAll(c1, c0);
		gridPane1.getChildren().addAll(effectLabel, soundEffectMuteBox);

		soundVBox = new VBox();
		soundVBox.getStyleClass().add("jfx-popup-container");
		soundVBox.setPadding(new Insets(1, 1, 1, 1));
		soundVBox.setAlignment(Pos.CENTER);
		soundVBox.getChildren().addAll(header_label, gridPane0, musicSlider, gridPane1, soundEffectSlider);
		soundPane.getChildren().addAll(soundVBox);

	}

	/*
	 * public void createFarmPopup() {
	 * //logger.info("MainScene's createFarmPopup() is on " +
	 * Thread.currentThread().getName());
	 * 
	 * farmBtn = new JFXButton();
	 * //farmBtn.getStyleClass().add("menu-button");//"button-raised"); farmIcon =
	 * new IconNode(FontAwesome.LEAF); farmIcon.setIconSize(20);
	 * //soundIcon.setFill(Color.YELLOW); //soundIcon.setStroke(Color.WHITE);
	 * 
	 * farmBtn.setMaxSize(20, 20); farmBtn.setGraphic(farmIcon);
	 * setQuickToolTip(farmBtn, "Click to open Farming Panel");
	 * 
	 * farmBtn.setOnAction(e -> { if (farmPopup.isShowing()) {
	 * farmPopup.hide();//close(); } else { farmPopup.show(farmBtn,
	 * PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35); } });
	 * 
	 * Accordion acc = new Accordion(); ObservableList<Settlement> towns =
	 * sim.getUnitManager().getSettlementOList(); int num = towns.size();
	 * List<TitledPane> titledPanes = new ArrayList<>(); List<Pane> panes = new
	 * ArrayList<>();
	 * 
	 * for (Settlement s : towns) {
	 * 
	 * DragDrop dd = new DragDrop(); StackPane p = dd.createDragDropBox();
	 * panes.add(p); TitledPane tp = new TitledPane(s.getName(), p);
	 * titledPanes.add(tp);
	 * 
	 * p.getStyleClass().add("jfx-popup-container"); p.setAlignment(Pos.CENTER);
	 * p.setPrefHeight(75); p.setPrefWidth(250);
	 * 
	 * acc.getPanes().add(tp); }
	 * 
	 * farmPane = new StackPane(acc);
	 * 
	 * farmPopup = new JFXPopup(farmPane);
	 * 
	 * }
	 */

	public void createMarsTimeBar() {	
		
		marsTimeLabel = new Label();
		marsTimeLabel.setId("mars-time-label");
		marsTimeLabel.setAlignment(Pos.CENTER);
		
		setQuickToolTip(marsTimeLabel, "Martian Date and Time");	

		marsTimeButton = new Button();
		marsTimeButton.setPrefSize(20, 20);
		marsTimeButton.setId("mars-time-button");
		
		marsTimeIcon = new IconNode(FontAwesome.CALENDAR_O);
		marsTimeIcon.setIconSize(20);
		marsTimeIcon.setId("mars-time-icon");
		marsTimeButton.setGraphic(marsTimeIcon);
		
		marsTimeBox = new HBox(marsTimeButton, marsTimeLabel);
		marsTimeBox.setId("mars-time-box");
		marsTimeBox.setAlignment(Pos.CENTER);

		marsTimeButton.setMaxWidth(Double.MAX_VALUE);
		if (OS.contains("linux")) {
			marsTimeLabel.setPrefSize(LINUX_WIDTH, 29);
			marsTimeBox.setPrefSize(LINUX_WIDTH+25, 29);
		} else if (OS.contains("mac")) {
			marsTimeLabel.setPrefSize(MACOS_WIDTH, 30);
			marsTimeBox.setPrefSize(MACOS_WIDTH+20, 30);
		} else if (OS.contains("win")) {
			marsTimeLabel.setPrefSize(WIN_WIDTH, 35);
			marsTimeBox.setPrefSize(WIN_WIDTH+20, 35);
		}
		
		setQuickToolTip(marsTimeButton, "Click to open Martian calendar");	

		if (masterClock == null)
			masterClock = sim.getMasterClock();

		if (marsClock == null)
			marsClock = masterClock.getMarsClock();

		marsTimeButton.setOnAction(e -> {
			if (marsCalendarPopup.isShowing()) {
				marsCalendarPopup.hide();// close();
			} 
			else {
				marsCalendarPopup.show(marsTimeButton, PopupVPosition.TOP, PopupHPosition.LEFT, 0, 38);
			}
			e.consume();
		});

		
		calendarDisplay = new MarsCalendarDisplay(marsClock, desktop);

		SwingNode calNode = new SwingNode();
		calNode.setContent(calendarDisplay);

		StackPane calPane = new StackPane(calNode);
		calPane.getStyleClass().add("jfx-popup-container");
		calPane.setAlignment(Pos.CENTER);
		calPane.setPrefHeight(100);
		calPane.setPrefWidth(140);

		VBox calBox = new VBox();
		calBox.setPadding(new Insets(0, 3, 3, 3));
		calBox.setAlignment(Pos.BOTTOM_CENTER);
		calBox.getChildren().addAll(calPane);
		setQuickToolTip(calBox, "Martian Calendar showing all 4 weeks for current month");

		// Label header_label = createHeader("MARS CALENDAR");
		Text header_label = createTextHeader("MARS CALENDAR PANEL");

		monthText = createBlendText(MONTH + marsClock.getMonthName());
		yearText = createBlendText(ORBIT + marsClock.getOrbitString());

		setQuickToolTip(monthText, "the current Martian month. Each orbit has 24 months with either 27 or 28 Sols");
		setQuickToolTip(yearText, "the current Martian orbit (or year). "
				+ "Orbit 0015 coincides with Earth year 2043 CE"); 
		// The Martian year is referred to as an "orbit". Each orbit has 668.59 Martian sols and 
		// is 668.5921 Martian days long.
		HBox hBox = new HBox();
		hBox.setPadding(new Insets(2, 15, 2, 15));
		hBox.setAlignment(Pos.BOTTOM_CENTER);
		hBox.getChildren().addAll(yearText, monthText);

		Label LsLabel = new Label(SOLAR_LONGITUDE);
		setQuickToolTip(LsLabel, "Solar Longitude (L_s) is the Mars-Sun angle for determining the Martian season");

		orbitInfo = sim.getMars().getOrbitInfo();
		double L_s = orbitInfo.getL_s();
		LSText = createBlendText(Math.round(L_s * 100D) / 100D + Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		setQuickToolTip(LSText, "L_s in deg. For Northern hemisphere, "
				+ "Spring equinox at L_s=0; Winter solstice at L_s=270."
				+ "Summer solstice at L_s=90."																			
				+ "Autumn equinox at L_s = 180.");

		HBox LsBox = new HBox();
		LsBox.setPadding(new Insets(2, 2, 2, 2));
		LsBox.setAlignment(Pos.BOTTOM_CENTER);
		LsBox.getChildren().addAll(LsLabel, LSText);

		Label northLabel = new Label(NORTH);
		Label southLabel = new Label(SOUTH);
		northText = createBlendText(marsClock.getSeason(MarsClock.NORTHERN_HEMISPHERE));
		southText = createBlendText(marsClock.getSeason(MarsClock.SOUTHERN_HEMISPHERE));
		setQuickToolTip(northText, "the current season in the Northern hemisphere");
		setQuickToolTip(southText, "the current season in the Southern hemisphere");

		HBox northBox = new HBox();
		northBox.setPadding(new Insets(2, 2, 2, 2));
		northBox.setAlignment(Pos.BOTTOM_CENTER);
		northBox.getChildren().addAll(northLabel, northText);

		HBox southBox = new HBox();
		southBox.setPadding(new Insets(2, 2, 2, 2));
		southBox.setAlignment(Pos.BOTTOM_CENTER);
		southBox.getChildren().addAll(southLabel, southText);

		noteLabel = new Label();
		// noteText = new Text();
		// noteLabel.setEffect(blend);
		noteLabel.setPadding(new Insets(2, 5, 2, 0));
		noteLabel.setStyle("-fx-background-color: linear-gradient(to bottom, -fx-base, derive(-fx-base,30%));"
				+ "-fx-font-size: 12px;" + "-fx-text-fill: #654b00;");

		VBox vBox = new VBox();
		vBox.setPadding(new Insets(1, 5, 1, 5));
		vBox.setAlignment(Pos.CENTER);
		vBox.getChildren().addAll(header_label, hBox, calBox, LsBox, northBox, southBox, noteLabel);

		calendarPane = new StackPane(vBox);
		calendarPane.getStyleClass().add("jfx-popup-container");
		calendarPane.setAlignment(Pos.CENTER);
		calendarPane.setPrefHeight(170);
		calendarPane.setPrefWidth(180);
		calendarPane.setPadding(new Insets(5, 5, 5, 5));

		marsCalendarPopup = new JFXPopup(calendarPane);

	}

	public void createFXButtons() {

		minimapToggle = new JFXToggleButton();
		// pinButton.setTextFill(Paint.OPAQUE);
		minimapToggle.setText("Minimap Off");
		minimapToggle.setSelected(false);
		setQuickToolTip(minimapToggle, "Pin Minimap");
		minimapToggle.setOnAction(e -> {
			if (minimapToggle.isSelected()) {
				openMinimap();
			} else {
				closeMinimap();
				// minimapToggle.setText("Minimap Off");
				// desktop.closeToolWindow(NavigatorWindow.NAME);
				// mapAnchorPane.getChildren().remove(minimapStackPane);
				// minimapButton.setSelected(false);
			}

			// minimapButton.toFront();
			e.consume();
		});

		mapToggle = new JFXToggleButton();
		mapToggle.setText("Settlement Map Off");
		mapToggle.setSelected(false);
		setQuickToolTip(mapToggle, "Pin Settlement Map");
		mapToggle.setOnAction(e -> {
			if (mapToggle.isSelected()) {
				if (!desktop.isToolWindowOpen(SettlementWindow.NAME))
					openSettlementMap();
				if (desktop.isToolWindowOpen(NavigatorWindow.NAME)) {
					// pinButton.setSelected(true);
					// closeMinimap();
					openMinimap();
				}
			} else {
				closeSettlementMap();
			}

			// sMapButton.toFront();

		});

		cacheToggle = new JFXToggleButton();
		cacheToggle.setText("Map Cache Off");
		cacheToggle.setSelected(false);
		setQuickToolTip(cacheToggle, "Retain Settlement Map and Minimap after switching to another tab");
		cacheToggle.setOnAction(e -> {
			if (cacheToggle.isSelected()) {
				cacheToggle.setText("Map Cache On");
				if (!desktop.isToolWindowOpen(SettlementWindow.NAME))
					openSettlementMap();
				if (!desktop.isToolWindowOpen(NavigatorWindow.NAME)) {
					// pinButton.setSelected(true);
					openMinimap();
				} else
					minimapStackPane.toFront();

				minimapToggle.toFront();
				mapToggle.toFront();
			} else
				cacheToggle.setText("Map Cache Off");

			e.consume();
		});

		rotateCWBtn = new JFXButton();
		rotateCWBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.cw"))))); //$NON-NLS-1$
		// IconNode rotateCWIcon = new IconNode(FontAwesome.ar.ARROW_CIRCLE_O_RIGHT);
		// rotateCWIcon.setIconSize(30);
		// rotateCWBtn.setGraphic(rotateCWIcon);
		// Tooltip t0 = new
		// Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.clockwise"));
		// //$NON-NLS-1$
		// rotateCWBtn.setTooltip(t0);
		setQuickToolTip(rotateCWBtn, Msg.getString("SettlementTransparentPanel.tooltip.clockwise"));
		rotateCWBtn.setOnAction(e -> {
			mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE);
			e.consume();
		});

		rotateCCWBtn = new JFXButton();
		rotateCCWBtn
				.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.ccw"))))); //$NON-NLS-1$
		// IconNode rotateCCWIcon = new IconNode(FontAwesome.ARROW_CIRCLE_O_LEFT);
		// rotateCCWIcon.setIconSize(30);
		// rotateCCWBtn.setGraphic(rotateCCWIcon);
		// Tooltip t1 = new
		// Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise"));
		// //$NON-NLS-1$
		// rotateCCWBtn.setTooltip(t1);
		setQuickToolTip(rotateCCWBtn, Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise"));
		rotateCCWBtn.setOnAction(e -> {
			mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE);
			e.consume();
		});

		recenterBtn = new JFXButton();
		recenterBtn.setGraphic(
				new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.recenter"))))); //$NON-NLS-1$
		// IconNode recenterIcon = new IconNode(FontAwesome.ALIGN_CENTER);
		// recenterIcon.setIconSize(30);
		// recenterBtn.setGraphic(recenterIcon);
		// Tooltip t2 = new
		// Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.recenter"));
		// //$NON-NLS-1$
		// recenterBtn.setTooltip(t2);
		setQuickToolTip(recenterBtn, Msg.getString("SettlementTransparentPanel.tooltip.recenter"));
		recenterBtn.setOnAction(e -> {
			mapPanel.reCenter();
			zoomSlider.setValue(DEFAULT_ZOOM);
			e.consume();
		});

	}

	public void createFXZoomSlider() {
		// logger.info("MainScene's createFXZoomSlider() is on " +
		// Thread.currentThread().getName() + " Thread");

		// Set up a settlement view zoom bar
		zoomSlider = new JFXSlider();
		zoomSlider.getStyleClass().add("jfx-slider");
		// zoom.setMinHeight(100);
		// zoom.setMaxHeight(200);
		zoomSlider.prefHeightProperty().bind(mapStackPane.heightProperty().multiply(.3d));
		zoomSlider.setMin(1);
		zoomSlider.setMax(35);
		zoomSlider.setValue(DEFAULT_ZOOM);
		zoomSlider.setMajorTickUnit(34);	
		zoomSlider.setMinorTickCount(1);	
		zoomSlider.setShowTickLabels(true);
		zoomSlider.setShowTickMarks(true);
		zoomSlider.setSnapToTicks(false);
		zoomSlider.setBlockIncrement(.5);
		zoomSlider.setOrientation(Orientation.VERTICAL);
		zoomSlider.setIndicatorPosition(IndicatorPosition.RIGHT);

		setQuickToolTip(zoomSlider, Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		// detect dragging on zoom scroll bar
		zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				if (old_val != new_val) {
					// Change scale of map based on slider position.
					double sliderValue = new_val.doubleValue();
					//double d = SettlementMapPanel.DEFAULT_SCALE;
					double newScale = 0;
					if (sliderValue > 0) {
						newScale = sliderValue;//* SettlementTransparentPanel.ZOOM_CHANGE;
					} 
					//else if (sliderValue < 0) {
					//	newScale = 1 + sliderValue;//* SettlementTransparentPanel.ZOOM_CHANGE));
					//}
					mapPanel.setScale(newScale);
				}
			}
		});
	}

	public void createFXSettlementComboBox() {
		sBox = new JFXComboBox<>();
		// sBox.setAlignment(Pos.CENTER_RIGHT);
		// JFXListView<Settlement> list = new JFXListView<Settlement>();
		sBox.getStyleClass().add("jfx-combo-box");
		setQuickToolTip(sBox, Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		// ObservableList<Settlement> names = sim.getUnitManager().getSettlementOList();
		sBox.itemsProperty().setValue(sim.getUnitManager().getSettlementOList());
		sBox.setPromptText("Select a settlement to view");
		sBox.getSelectionModel().selectFirst();

		sBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != newValue) {
				SwingUtilities.invokeLater(() -> mapPanel.setSettlement((Settlement) newValue));
			}
		});

		settlementBox = new StackPane(sBox);
		settlementBox.setMaxSize(180, 30);
		settlementBox.setPrefSize(180, 30);
		settlementBox.setAlignment(Pos.CENTER_RIGHT);

	}

	public void changeSBox() {
		sBox.itemsProperty().setValue(sim.getUnitManager().getSettlementOList());
	}

	public void createFXMapLabelBox() {

		mapLabelBox = new VBox();
		mapLabelBox.setSpacing(5);
		mapLabelBox.setMaxSize(180, 150);
		mapLabelBox.setPrefSize(180, 150);
		// mapLabelBox.setAlignment(Pos.CENTER_RIGHT);

		JFXCheckBox box0 = new JFXCheckBox(Msg.getString("SettlementWindow.menu.daylightTracking"));
		JFXCheckBox box1 = new JFXCheckBox(Msg.getString("SettlementWindow.menu.buildings"));
		JFXCheckBox box2 = new JFXCheckBox(Msg.getString("SettlementWindow.menu.constructionSites"));
		JFXCheckBox box3 = new JFXCheckBox(Msg.getString("SettlementWindow.menu.people"));
		JFXCheckBox box4 = new JFXCheckBox(Msg.getString("SettlementWindow.menu.robots"));
		JFXCheckBox box5 = new JFXCheckBox(Msg.getString("SettlementWindow.menu.vehicles"));

		box0.getStyleClass().add("jfx-check-box");
		box1.getStyleClass().add("jfx-check-box");
		box2.getStyleClass().add("jfx-check-box");
		box3.getStyleClass().add("jfx-check-box");
		box4.getStyleClass().add("jfx-check-box");
		box5.getStyleClass().add("jfx-check-box");

		mapLabelBox.getChildren().addAll(box0, box1, box2, box3, box4, box5);

		box0.setSelected(mapPanel.isDaylightTrackingOn());
		box1.setSelected(mapPanel.isShowBuildingLabels());
		box2.setSelected(mapPanel.isShowConstructionLabels());
		box3.setSelected(mapPanel.isShowPersonLabels());
		box4.setSelected(mapPanel.isShowRobotLabels());
		box5.setSelected(mapPanel.isShowVehicleLabels());

		box0.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					box0.setSelected(newValue);
					mapPanel.setShowDayNightLayer(newValue);
				}
			}
		});

		box1.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					box1.setSelected(newValue);
					mapPanel.setShowBuildingLabels(newValue);
				}
			}
		});

		box2.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					box2.setSelected(newValue);
					mapPanel.setShowConstructionLabels(newValue);
				}
			}
		});

		box3.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					box3.setSelected(newValue);
					mapPanel.setShowPersonLabels(newValue);
				}
			}
		});

		box4.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					box4.setSelected(newValue);
					mapPanel.setShowRobotLabels(newValue);
				}
			}
		});

		box5.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
					box5.setSelected(newValue);
					mapPanel.setShowVehicleLabels(newValue);
				}
			}
		});
	}

	/**
	 * Creates the tab pane for housing a bunch of tabs
	 */
	@SuppressWarnings("restriction")
	public void createJFXTabs() {
		jfxTabPane = new JFXTabPane();

		mainStackPane = new StackPane();
		mainStackPane.getChildren().add(desktopNode);

		dashboardStackPane = new StackPane();

		dashboardTab = new Tab();
		dashboardTab.setText("Dashboard");
		dashboardTab.setContent(dashboardStackPane);

		Parent parent = null;
		DashboardController controller = null;
		FXMLLoader fxmlLoader = null;
		/*
		 * try { fxmlLoader = new FXMLLoader(); parent = (Parent)
		 * fxmlLoader.load(getClass().getResource("/fxui/fxml/dashboard/dashboard.fxml")
		 * ); //fxmlLoader.setController(controller); controller = (DashboardController)
		 * fxmlLoader.getController(); } catch (IOException e) { e.printStackTrace(); }
		 * 
		 */

		try {
			fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("/fxui/fxml/dashboard/dashboard.fxml")); // //
			parent = (Parent) fxmlLoader.load();
			controller = (DashboardController) fxmlLoader.getController();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// AnchorPane anchorPane = (AnchorPane)parent.lookup("#anchorPane");
		// anchorPane.setPrefSize(screen_width, screen_height);

		controller.setSize(screen_width, screen_height - TAB_PANEL_HEIGHT);

		dashboardStackPane.getChildren().add(parent);

		mainTab = new Tab();
		mainTab.setText("Main");
		mainTab.setContent(mainStackPane);

		// set up mapTab
		mapAnchorPane = new AnchorPane();
		mapAnchorPane.setStyle("-fx-background-color: black; ");

		Tab mapTab = new Tab();
		mapTab.setText("Map");
		mapTab.setContent(mapAnchorPane);

		navWin = (NavigatorWindow) desktop.getToolWindow(NavigatorWindow.NAME);

		minimapNode = new SwingNode();
		minimapStackPane = new StackPane(minimapNode);
		minimapNode.setContent(navWin);
		minimapStackPane.setStyle("-fx-background-color: black; ");
		minimapNode.setStyle("-fx-background-color: black; ");

		settlementWindow = (SettlementWindow) desktop.getToolWindow(SettlementWindow.NAME);
		mapPanel = settlementWindow.getMapPanel();

		mapNode = new SwingNode();
		mapStackPane = new StackPane(mapNode);
		mapNode.setContent(settlementWindow);
		mapStackPane.setStyle("-fx-background-color: black; ");
		mapNode.setStyle("-fx-background-color: black; ");

		createFXButtons();
		createFXSettlementComboBox();
		createFXZoomSlider();
		createFXMapLabelBox();

		// detect mouse wheel scrolling
		mapStackPane.setOnScroll(new EventHandler<ScrollEvent>() {
			public void handle(ScrollEvent event) {

				if (event.getDeltaY() == 0)
					return;

				double direction = event.getDeltaY();

				if (direction > 0) {
					// Move zoom slider down.
					if (zoomSlider.getValue() > zoomSlider.getMin())
						zoomSlider.setValue((zoomSlider.getValue() - 1));
				} else if (direction < 0) {
					// Move zoom slider up.
					if (zoomSlider.getValue() < zoomSlider.getMax())
						zoomSlider.setValue((zoomSlider.getValue() + 1));
				}
				// event.consume();
			}
		});

		BrowserJFX helpBrowser = desktop.getBrowserJFX();
		// StackPane guidePane = new StackPane(guideNode);
		StackPane guidePane = new StackPane(helpBrowser.getBorderPane());
		Tab guideTab = new Tab();
		guideTab.setText("Help");
		guideTab.setContent(guidePane);

		jfxTabPane.getTabs().addAll(dashboardTab, mainTab, mapTab, guideTab);

		jfxTabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {

			if (newTab == dashboardTab) {
				dashboardStackPane.requestFocus();
				closeMaps();
			}

			else if (newTab == mainTab) {
				mainStackPane.requestFocus();
				closeMaps();
			}

			else if (newTab == mapTab) {

				AnchorPane.setRightAnchor(cacheToggle, 25.0);
				AnchorPane.setTopAnchor(cacheToggle, 45.0); // 55.0

				AnchorPane.setLeftAnchor(minimapToggle, 10.0);
				AnchorPane.setTopAnchor(minimapToggle, 10.0); // 55.0

				AnchorPane.setRightAnchor(mapToggle, 15.0);
				AnchorPane.setTopAnchor(mapToggle, 10.0); // 55.0

				mapAnchorPane.getChildren().addAll(cacheToggle, minimapToggle, mapToggle);

				desktop.closeToolWindow(GuideWindow.NAME);

			}

			else if (newTab == guideTab) {

				if (!desktop.isToolWindowOpen(GuideWindow.NAME))
					desktop.openToolWindow(GuideWindow.NAME);

				closeMaps();

			}

			else {
				mapAnchorPane.getChildren().removeAll(cacheToggle, minimapToggle, mapToggle);
			}

		});

		// jfxTabPane.getSelectionModel().select(guideTab);

		// NOTE: if a tab is NOT selected, should close that tool as well to save cpu
		// utilization
		// this is done in ToolWindow's update(). It allows for up to 1 second of delay,
		// in case user open and close the same repeated.

	}

	public void openMinimap() {
		desktop.openToolWindow(NavigatorWindow.NAME);

		AnchorPane.setLeftAnchor(minimapStackPane, 3.0);
		AnchorPane.setTopAnchor(minimapStackPane, 0.0); // 45.0
		boolean flag = false;
		for (Node node : mapAnchorPane.getChildrenUnmodifiable()) {
			if (node == minimapStackPane) {
				flag = true;
				break;
			}
		}

		if (!flag)
			mapAnchorPane.getChildren().addAll(minimapStackPane);
		navWin.getGlobeDisplay().drawSphere();// updateDisplay();
		navWin.toFront();
		navWin.requestFocus();
		minimapStackPane.toFront();
		minimapToggle.setSelected(true);
		minimapToggle.setText("Minimap On");
		minimapToggle.toFront();

	}

	public void openSettlementMap() {

		mapStackPane.prefWidthProperty().unbind();
		mapStackPane.prefWidthProperty().bind(scene.widthProperty().subtract(1));

		desktop.openToolWindow(SettlementWindow.NAME);
		// mapNode.setContent(settlementWindow);

		AnchorPane.setRightAnchor(mapStackPane, 0.0);
		AnchorPane.setTopAnchor(mapStackPane, 0.0);

		AnchorPane.setRightAnchor(zoomSlider, 65.0);
		AnchorPane.setTopAnchor(zoomSlider, 350.0);// (mapNodePane.heightProperty().get() -
													// zoomSlider.heightProperty().get())*.4d);

		AnchorPane.setRightAnchor(rotateCWBtn, 110.0);
		AnchorPane.setTopAnchor(rotateCWBtn, 300.0);

		AnchorPane.setRightAnchor(rotateCCWBtn, 30.0);
		AnchorPane.setTopAnchor(rotateCCWBtn, 300.0);

		AnchorPane.setRightAnchor(recenterBtn, 70.0);
		AnchorPane.setTopAnchor(recenterBtn, 300.0);

		AnchorPane.setRightAnchor(settlementBox, 15.0);// anchorMapTabPane.widthProperty().get()/2D -
														// 110.0);//settlementBox.getWidth());
		AnchorPane.setTopAnchor(settlementBox, 100.0);

		AnchorPane.setRightAnchor(mapLabelBox, -10.0);
		AnchorPane.setTopAnchor(mapLabelBox, 140.0);

		boolean hasMap = false, hasZoom = false, hasButtons = false, hasSettlements = false, hasMapLabel = false;

		ObservableList<Node> nodes = mapAnchorPane.getChildrenUnmodifiable();

		for (Node node : nodes) {

			if (node == settlementBox) {
				hasSettlements = true;
			} else if (node == mapStackPane) {
				hasMap = true;
			} else if (node == zoomSlider) {
				hasZoom = true;
			} else if (node == recenterBtn || node == rotateCWBtn || node == rotateCCWBtn) {
				hasButtons = true;
			} else if (node == mapLabelBox)
				hasMapLabel = true;

		}

		if (!hasMap)
			mapAnchorPane.getChildren().addAll(mapStackPane);

		if (!hasSettlements)
			mapAnchorPane.getChildren().addAll(settlementBox);

		if (!hasMapLabel)
			mapAnchorPane.getChildren().addAll(mapLabelBox);

		if (!hasZoom)
			mapAnchorPane.getChildren().addAll(zoomSlider);

		if (!hasButtons)
			mapAnchorPane.getChildren().addAll(rotateCWBtn, rotateCCWBtn, recenterBtn);
		/*
		 * for (Node node : mapAnchorPane.getChildrenUnmodifiable()) { if (node ==
		 * cacheButton) { node.toFront(); } else if (node == minimapButton) {
		 * node.toFront(); } else if (node == sMapButton) { node.toFront(); } else if
		 * (node == settlementBox) { node.toFront(); } else if (node == mapLabelBox) {
		 * node.toFront(); } }
		 */
		mapLabelBox.toFront();
		settlementBox.toFront();
		mapToggle.toFront();
		cacheToggle.toFront();
		minimapToggle.toFront();

		mapToggle.setText("Settlement Map On");
		mapToggle.setSelected(true);

	}

	public boolean isMapOn() {
		return mapToggle.isSelected();
	}

	public boolean isMinimapOn() {
		return mapToggle.isSelected();
	}
	
	public void closeMinimap() {
		desktop.closeToolWindow(NavigatorWindow.NAME);
		Platform.runLater(() -> {
			// addNavWin();
			mapAnchorPane.getChildren().remove(minimapStackPane);
			minimapToggle.setSelected(false);
			minimapToggle.setText("Minimap Off");
			jfxTabPane.requestFocus();
		});
	}

	public void closeSettlementMap() {
		desktop.closeToolWindow(SettlementWindow.NAME);
		Platform.runLater(() -> {
			mapAnchorPane.getChildren().removeAll(mapStackPane, settlementBox, mapLabelBox, zoomSlider, rotateCWBtn,
					rotateCCWBtn, recenterBtn);
			mapToggle.setSelected(false);
			mapToggle.setText("Settlement Map Off");
			jfxTabPane.requestFocus();
		});
	}

	public void closeMaps() {
		mapAnchorPane.getChildren().removeAll(cacheToggle, minimapToggle, mapToggle);
		if (!isCacheButtonOn()) {
			desktop.closeToolWindow(SettlementWindow.NAME);
			desktop.closeToolWindow(NavigatorWindow.NAME);
			Platform.runLater(() -> {
				mapAnchorPane.getChildren().removeAll(minimapStackPane, mapStackPane, zoomSlider, rotateCWBtn,
						rotateCCWBtn, recenterBtn, settlementBox, mapLabelBox);
				minimapToggle.setSelected(false);
				minimapToggle.setText("Minimap Off");
				mapToggle.setSelected(false);
				mapToggle.setText("Settlement Map Off");
			});
		}
		jfxTabPane.requestFocus();
	}

	public boolean isCacheButtonOn() {
		if (cacheToggle.isSelected())
			return true;
		else
			return false;
	}

	/*
	 * Sets the theme skin after calling stage.show() at the start of the sim
	 */
	public void initializeTheme() {
		// logger.info("MainScene's initializeTheme()");
		// NOTE: it is mandatory to change the theme from 1 to 2 below at the start of
		// the sim
		// This avoids two display issues:
		// (1). the crash of Mars Navigator Tool when it was first loaded
		// (2). the inability of loading the tab icons of the Monitor Tool at the
		// beginning
		// Also, when clicking a tab at the first time, a NullPointerException results)
		// TODO: find out if it has to do with nimrodlf and/or JIDE-related
		/*
		 * if (OS.contains("linux")) { setTheme(0); } else { setTheme(7); }
		 */
/*		
		if (choice_theme == ThemeType.Nimrod)
			setTheme(0);
		else if (choice_theme == ThemeType.Nimbus)
			setTheme(0);
		else if (choice_theme == ThemeType.Web)
			setTheme(0);
*/		
		setTheme(defaultThemeColor);

		// SwingUtilities.invokeLater(() -> setLookAndFeel(NIMBUS_THEME));

		// logger.info("done with MainScene's initializeTheme()");
	}

	/*
	 * Sets the theme skin of the desktop
	 */
	public void setTheme(int theme) {

		String cssFile = CSS_PATH;
		
		if (menuBar != null && menuBar.getStylesheets() != null)
			menuBar.getStylesheets().clear();

		if (this.theme != theme) {
			this.theme = theme;

			if (theme == 0) { // snow blue
				// for numbus theme
				cssFile += "snowBlue.css";
				updateThemeColor(0, Color.rgb(0, 107, 184), Color.rgb(0, 107, 184), cssFile); // CADETBLUE //																						// Color.rgb(23,138,255)
				themeSkin = "snowBlue";
				// see https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/color.html

			} else if (theme == 1) { // olive green
				cssFile += "oliveskin.css";
				updateThemeColor(1, Color.GREEN, Color.PALEGREEN, cssFile); // DARKOLIVEGREEN
				themeSkin = "LightTabaco";

			} else if (theme == 2) { // burgundy red
				cssFile += "burgundyskin.css";
				updateThemeColor(2, Color.rgb(140, 0, 26), Color.YELLOW, cssFile); // ORANGERED
				themeSkin = "Burdeos";

			} else if (theme == 3) { // dark chocolate
				cssFile += "darkTabaco.css";
				updateThemeColor(3, Color.DARKGOLDENROD, Color.BROWN, cssFile);
				themeSkin = "DarkTabaco";

			} else if (theme == 4) { // grey
				cssFile += "darkGrey.css";
				updateThemeColor(4, Color.DARKSLATEGREY, Color.DARKGREY, cssFile);
				themeSkin = "DarkGrey";

			} else if (theme == 5) { // + purple
				cssFile += "nightViolet.css";
				updateThemeColor(5, Color.rgb(73, 55, 125), Color.rgb(73, 55, 125), cssFile); // DARKMAGENTA, SLATEBLUE
				themeSkin = "Night";

			} else if (theme == 6) { // + skyblue

				cssFile += "snowBlue.css";
				updateThemeColor(6, Color.rgb(0, 107, 184), Color.rgb(255, 255, 255), cssFile); 
				// (144, 208, 229) light blue , CADETBLUE (0,107,184), Color.rgb(23,138,255)
				themeSkin = "snowBlue";
			} else if (theme == 7) { // mud orange/standard

				cssFile += "nimrodskin.css";
				updateThemeColor(7, Color.rgb(156, 77, 0), Color.rgb(255, 255, 255), cssFile); // DARKORANGE, CORAL
				themeSkin = "nimrod";
			}

			SwingUtilities.invokeLater(() -> {
				if (OS.contains("linux")) {
					// Note: NIMROD theme lib doesn't work on linux
					setLookAndFeel(ThemeType.Nimbus);
				} 
				
				else {
					//if (theme == 0 || theme == 6)
						setLookAndFeel(defaultThemeType);
					//else
					//	setLookAndFeel(ThemeType.Nimrod);
				}
				
			});
			
		}

	}

	/**
	 * Sets the look and feel of the UI
	 * 
	 * @param choice
	 */
	// 2015-05-02 Edited setLookAndFeel()
	public void setLookAndFeel(ThemeType choice) {
		// logger.info("MainScene's setLookAndFeel() is on " +
		// Thread.currentThread().getName() + " Thread");
		boolean changed = false;
		if (choice == ThemeType.Weblaf) {
			try {
				// use the weblaf skin
				WebLookAndFeel.install();
				//UIManagers.initialize();
					        
				// need to load an uimanager
				//if (theme == 0 || theme == 6)
				//	UIManager.setLookAndFeel (NimbusLookAndFeel.class.getCanonicalName());
				//else if (theme == 7){
					NimRODTheme nt = new NimRODTheme(getClass().getClassLoader().getResource("theme/" + themeSkin + ".theme")); //
					NimRODLookAndFeel.setCurrentTheme(nt); // must be declared non-static or not
					// working if switching to a brand new .theme file 
					NimRODLookAndFeel nf = new NimRODLookAndFeel();
					nf.setCurrentTheme(nt); // must be declared non-static or not working if switching to a brand new .theme // file
					UIManager.setLookAndFeel(nf); 
				//}
				
				changed = true;
				
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		}
		
		else if (choice == ThemeType.System) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
				// themeSkin = "system";
				// System.out.println("found system");
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		}
		
		else if (choice == ThemeType.Nimrod) { 
			try {
				//String themeSkin = "snowBlue";
				NimRODTheme nt = new NimRODTheme(getClass().getClassLoader().getResource("theme/" + themeSkin + ".theme")); //
				NimRODLookAndFeel.setCurrentTheme(nt); // must be declared non-static or not
				// working if switching to a brand new .theme file 
				NimRODLookAndFeel nf = new NimRODLookAndFeel();
				nf.setCurrentTheme(nt); // must be declared non-static or not working if switching to a brand new .theme // file
				UIManager.setLookAndFeel(nf); 
				changed = true; //
				//System.out.println("found Nimrod");
		 
			} catch (Exception e) { 
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$ } }
			}
		 }
		 
		 else if (choice == ThemeType.Nimbus) {
			try {
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) {
						// Set Nimbus look & feel if found in JVM.

						// see https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/color.html

						// UIManager.put("nimbusBase", new Color(...));
						// UIManager.put("nimbusBlueGrey", new Color(...));
						// UIManager.put("control", new Color(...));

						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						// themeSkin = "nimbus";
						changed = true;
						// break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					// themeSkin = "metal";
					changed = true;
					// System.out.println("found metal");
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
			}
		}

		if (changed) {
			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateUnitWindowLF();
				// SwingUtilities.updateComponentTreeUI(desktop);
				// desktop.updateAnnouncementWindowLF();
				// desktop.updateTransportWizardLF();
			}
		}

	}

	/*
	 * Updates the theme colors of statusBar, swingPane and menuBar
	 */
	// 2015-08-29 Added updateThemeColor()
	public void updateThemeColor(int theme, Color txtColor, Color txtColor2, String cssFile) {
		mainStackPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		if (!OS.contains("mac"))
			menuBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

		// Note : menu bar color
		// orange theme : F4BA00
		// blue theme : 3291D2

		// String color = txtColor.replace("0x", "");

		jfxTabPane.getStylesheets().clear();

		setStylesheet(marsTimeBox, cssFile);
		setStylesheet(marsTimeButton, cssFile);
		setStylesheet(marsTimeLabel, cssFile);
		
		marsTimeIcon.getStyleClass().clear();
		marsTimeIcon.getStyleClass().add(getClass().getResource(cssFile).toExternalForm());
		
		setStylesheet(lastSaveLabel, cssFile);
		setStylesheet(cacheToggle, cssFile);
		setStylesheet(minimapToggle, cssFile);
		setStylesheet(mapToggle, cssFile);

		setStylesheet(settlementBox, cssFile);
		setStylesheet(mapLabelBox, cssFile);

		setStylesheet(speedPane, cssFile);
		setStylesheet(speedVBox, cssFile);
		setStylesheet(calendarPane, cssFile);
		setStylesheet(soundPane, cssFile);
		setStylesheet(soundVBox, cssFile);

		// setStylesheet(timeSlider, cssFile);
		setStylesheet(zoomSlider, cssFile);
		setStylesheet(musicSlider, cssFile);
		setStylesheet(soundEffectSlider, cssFile);

		datePickerFX.getStylesheets().clear();
		datePickerFX.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

//		if (cssFile.toLowerCase().contains("blue"))
//			datePickerFX.setDefaultColor(Color.valueOf("#065185"));
//		else
//			datePickerFX.setDefaultColor(Color.valueOf("#654b00"));

		timePickerFX.getStylesheets().clear();
		timePickerFX.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
//		if (cssFile.toLowerCase().contains("blue"))
//			timePickerFX.setDefaultColor(Color.valueOf("#065185"));
//		else
//			timePickerFX.setDefaultColor(Color.valueOf("#654b00"));
		
		if (settlementWindow == null) {
			settlementWindow = (SettlementWindow) (desktop.getToolWindow(SettlementWindow.NAME));
			if (settlementWindow != null) {
				settlementWindow.setTheme(txtColor);
				// settlementWindow.setStatusBarTheme(cssFile);
			}
		}

		else {
			settlementWindow.setTheme(txtColor);
			// settlementWindow.setStatusBarTheme(cssFile);
		}

		if (theme == 7) {
			rotateCCWBtn.setGraphic(
					new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.ccw_yellow"))))); //$NON-NLS-1$
			rotateCWBtn.setGraphic(
					new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.cw_yellow"))))); //$NON-NLS-1$
			recenterBtn.setGraphic(new ImageView(
					new Image(this.getClass().getResourceAsStream(Msg.getString("img.recenter_yellow"))))); //$NON-NLS-1$
			speedIcon.setFill(Color.YELLOW);
			speedBtn.setGraphic(speedIcon);
			marsNetIcon.setFill(Color.YELLOW);
			marsNetBtn.setGraphic(marsNetIcon);
			soundIcon.setFill(Color.YELLOW);
			soundBtn.setGraphic(soundIcon);
			// farmIcon.setFill(Color.YELLOW);
			// farmBtn.setGraphic(farmIcon);
			jfxTabPane.getStylesheets().add(getClass().getResource(ORANGE_CSS_JFX).toExternalForm());
			jfxTabPane.getStyleClass().add("jfx-tab-pane");
		}

		else {
			rotateCCWBtn.setGraphic(
					new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.ccw"))))); //$NON-NLS-1$
			rotateCWBtn
					.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.cw"))))); //$NON-NLS-1$
			recenterBtn.setGraphic(
					new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.recenter"))))); //$NON-NLS-1$
			speedIcon.setFill(Color.LAVENDER);
			speedBtn.setGraphic(speedIcon);
			marsNetIcon.setFill(Color.LAVENDER);
			marsNetBtn.setGraphic(marsNetIcon);
			soundIcon.setFill(Color.LAVENDER);
			soundBtn.setGraphic(soundIcon);
			// farmIcon.setFill(Color.LAVENDER);
			// farmBtn.setGraphic(farmIcon);
			jfxTabPane.getStylesheets().add(getClass().getResource(BLUE_CSS_JFX).toExternalForm()); 
			jfxTabPane.getStyleClass().add("jfx-tab-pane");
		}

		chatBox.update();

	}

	public void setStylesheet(Text t, String cssFile) {
		// t.getStyle().clear();
		t.setStyle(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(Node n, String cssFile) {
		//n.getStylesheets().clear();
		//n.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}
	
	public void setStylesheet(JFXSlider s, String cssFile) {
		s.getStylesheets().clear();
		s.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(Button b, String cssFile) {
		b.getStylesheets().clear();
		b.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(JFXButton b, String cssFile) {
		b.getStylesheets().clear();
		b.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(StackPane sp, String cssFile) {
		sp.getStylesheets().clear();
		sp.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(VBox vb, String cssFile) {
		vb.getStylesheets().clear();
		vb.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(JFXToggleButton b, String cssFile) {
		b.getStylesheets().clear();
		b.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(Label l, String cssFile) {
		l.getStylesheets().clear();
		l.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}

	/**
	 * Creates and starts the earth timer
	 */
	public void startEarthTimer() {
		// Set up earth time text update
		timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), ae -> updateTimeLabels()));
		// Note: Infinite Timeline might result in a memory leak if not stopped
		// properly.
		// All the objects with animated properties would not be garbage collected.
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();

	}

	/**
	 * Creates and returns a {@link Flyout}
	 * 
	 * @return a new {@link Flyout}
	 */
	public JFXPopup createFlyout() {
		marsNetBtn = new JFXButton();
		// marsNetBtn.getStyleClass().add("menu-button");//"button-raised");
		marsNetIcon = new IconNode(FontAwesome.COMMENTING_O);
		marsNetIcon.setIconSize(20);
		// marsNetButton.setPadding(new Insets(0, 0, 0, 0)); // Warning : this
		// significantly reduce the size of the button image
		setQuickToolTip(marsNetBtn, "Click to open MarsNet Chat Box");

		marsNetBox = new JFXPopup(createChatBox());
		marsNetBox.setOpacity(.9);

		marsNetBtn.setOnAction(e -> {
			if (!flag)
				chatBox.update();

			if (marsNetBox.isShowing()) {// .isVisible()) {
				marsNetBox.hide();// .close();
			} else {
				openChatBox();
			}
			e.consume();
		});

		return marsNetBox;
	}

	public void openChatBox() {
		try {
			TimeUnit.MILLISECONDS.sleep(200L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		chatBox.getAutoFillTextBox().getTextbox().clear();
		chatBox.getAutoFillTextBox().getTextbox().requestFocus();
		marsNetBox.show(marsNetBtn, PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
	}

	public JFXPopup getFlyout() {
		return marsNetBox;
	}

	/*
	 * Creates a chat box
	 * 
	 * @return StackPane
	 */
	public StackPane createChatBox() {
		chatBox = new ChatBox(this);
		chatBox.getAutoFillTextBox().getTextbox().requestFocus();
		chatBoxPane = new StackPane(chatBox);
		chatBoxPane.setMinHeight(chatBoxHeight);
		chatBoxPane.setPrefHeight(chatBoxHeight);
		chatBoxPane.setMaxHeight(chatBoxHeight);
		chatBoxPane.setPadding(new Insets(0, 0, 0, 0));
		return chatBoxPane;
	}

	public StackPane getChatBoxPane() {
		return chatBoxPane;
	}

	public ChatBox getChatBox() {
		return chatBox;
	}

	public void setChatBoxPaneHeight(double value) {
		chatBoxPane.setMinHeight(value);
		chatBoxPane.setPrefHeight(value);
		chatBoxPane.setMaxHeight(value);
	}

	/*
	 * Creates the time bar for MainScene
	 */
	public void createLastSaveBar() {
		// 2016-09-15 Added oldLastSaveStamp
		oldLastSaveStamp = sim.getLastSaveTimeStamp();
		oldLastSaveStamp = oldLastSaveStamp.replace("_", " ");

		lastSaveLabel = new Label();
		lastSaveLabel.setId("save-label");
		lastSaveLabel.setMaxWidth(Double.MAX_VALUE);
		if (OS.contains("linux")) {
			lastSaveLabel.setPrefWidth(LINUX_WIDTH);
			lastSaveLabel.setPrefSize(LINUX_WIDTH, 29);
		} else if (OS.contains("mac")) {
			lastSaveLabel.setPrefWidth(MACOS_WIDTH);
			lastSaveLabel.setPrefSize(MACOS_WIDTH, 28);
		} else {
			lastSaveLabel.setPrefWidth(WIN_WIDTH);
			lastSaveLabel.setPrefSize(WIN_WIDTH, 35);
		}

		lastSaveLabel.setAlignment(Pos.CENTER_RIGHT);
		lastSaveLabel.setTextAlignment(TextAlignment.RIGHT);
		lastSaveLabel.setText(LAST_SAVED + oldLastSaveStamp);

		setQuickToolTip(lastSaveLabel, "Last time the sim was saved");

	}

	/*
	 * Updates Earth and Mars time label in the earthTimeBar and marsTimeBar
	 */
	public void updateTimeLabels() {
		// int msol = (int)(masterClock.getTimeRatio());
		// if (msol % 10 == 0) {
		// Check to see if a background sound track is being played.
		if (!soundPlayer.isMusicMute())
		//if (musicSlider.getValue() > 0)
			soundPlayer.playRandomMusicTrack();
		// }

		if (simSpeedPopup.isShowing() || solCache == 0) {
			double tps = 0;
			if (gameScene != null)
				tps = masterClock.getFPS();
			else
				tps = Math.round(masterClock.getPulsesPerSecond() * 100.0) / 100.0;
			
			if (tpsCache != tps) {
				tpsCache = tps;
				tpsLabel.setText(tps + HZ);
			}
			String upt = uptimer.getUptime();
			if (!upTimeCache.equals(upt)) {
				upTimeCache = upt;
				upTimeLabel.setText(upt);
			}
			// benchmarkLabel.setText(masterClock.getDiffCache() + "");
		}

		int solOfMonth = marsClock.getSolOfMonth();
		if (solCache != solOfMonth) {
			solCache = solOfMonth;

			if (solOfMonth == 1) {
				String mn = marsClock.getMonthName();
				if (mn != null) {
					monthText.setText(MONTH + mn);
					if (mn.equals(ADIR)) {
						yearText.setText(ORBIT + marsClock.getOrbitString());
					}
				}
			}

		}

		if (marsCalendarPopup.isShowing() || solCache == 0) {
			calendarDisplay.update();

			double L_s = orbitInfo.getL_s();
			LSText.setText(Math.round(L_s * 100D) / 100D + Msg.getString("direction.degreeSign"));

			if (L_s > 68 && L_s < 72) {
				noteLabel.setText(NOTE_MARS + APHELION);

			} else if (L_s > 248 && L_s < 252) {
				noteLabel.setText(NOTE_MARS + PERIHELION);

			} else
				noteLabel.setEffect(null);

			northText.setText(marsClock.getSeason(MarsClock.NORTHERN_HEMISPHERE));
			southText.setText(marsClock.getSeason(MarsClock.SOUTHERN_HEMISPHERE));

		}

		StringBuilder m = new StringBuilder();
		m.append(MARS_DATE_TIME).append(marsClock.getDateString()).append(ONE_SPACE)
				.append(marsClock.getTrucatedTimeStringUMST());
		marsTimeLabel.setText(m.toString());

		//StringBuilder e = new StringBuilder();
		//e.append(EARTH_DATE_TIME).append(earthClock.getLT());//getTimeStampF0());
		//earthTime.setText(e.toString());
		
		datePickerFX.setValue(earthClock.getLocalDate());
		timePickerFX.setValue(earthClock.getLocalTime());

		// Check on whether autosave is due
		if (masterClock.getAutosave()) {
			// Trigger an autosave instance
			saveSimulation(Simulation.AUTOSAVE);
			masterClock.setAutosave(false);
		}

		// Check to see if the last save label needs to be updated
		if (sim.getJustSaved()) {
			String newLastSaveStamp = sim.getLastSaveTimeStamp();
			if (!oldLastSaveStamp.equals(newLastSaveStamp)) {
				sim.setJustSaved(false);
				oldLastSaveStamp = newLastSaveStamp.replace("_", " ");
				lastSaveLabel.setText(LAST_SAVED + oldLastSaveStamp);

			}
		}

	}

	/**
	 * Gets the main desktop panel.
	 * 
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

	public boolean isMainSceneDone() {
		return isMainSceneDoneLoading;
	}

	/**
	 * Create a new simulation.

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

	}
	 */
	
	/**
	 * Performs the process of creating a new simulation.

	private void newSimulationProcess() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Starting new sim");
		alert.setHeaderText(Msg.getString("MainScene.new.header"));
		alert.setContentText(Msg.getString("MainScene.new.content"));
		ButtonType buttonTypeOne = new ButtonType("Save on Exit");
		// ButtonType buttonTypeTwo = new ButtonType("End Sim");
		ButtonType buttonTypeCancel = new ButtonType("Back to Sim");// , ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne,
				// buttonTypeTwo,
				buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne) {
			saveOnExit();

		} else if (result.get() == buttonTypeCancel) {// !result.isPresent())
			return;
		}
	}
*/
	
	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 * 
	 * @param type
	 */
	public void saveSimulation(int type) {
		if (!masterClock.isPaused()) {
			// hideWaitStage(PAUSED);
			if (type == Simulation.SAVE_DEFAULT || type == Simulation.SAVE_AS)
				showWaitStage(SAVING);
			else
				showWaitStage(AUTOSAVING);

			saveExecutor.execute(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					saveSimulationProcess(type);
					while (masterClock.isSavingSimulation())
						TimeUnit.MILLISECONDS.sleep(200L);
					return null;
				}

				@Override
				protected void succeeded() {
					super.succeeded();
					hideWaitStage(SAVING);
				}
			});
			

		}
		// endPause(previous);
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 * 
	 * @param type

	public void saveSimulation(int type) {
		if (!masterClock.isPaused()) {
			// hideWaitStage(PAUSED);
			if (type == Simulation.SAVE_DEFAULT || type == Simulation.SAVE_AS)
				showWaitStage(SAVING);
			else
				showWaitStage(AUTOSAVING);

			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					saveSimulationProcess(type);
					while (masterClock.isSavingSimulation())
						TimeUnit.MILLISECONDS.sleep(200L);
					return null;
				}

				@Override
				protected void succeeded() {
					super.succeeded();
					hideWaitStage(SAVING);
				}
			};
			new Thread(task).start();

		}
		// endPause(previous);
	}
*/
	
	/**
	 * Performs the process of saving a simulation.
	 */
	private void saveSimulationProcess(int type) {
		// logger.info("MainScene's saveSimulationProcess() is on " +
		// Thread.currentThread().getName() + " Thread");
		fileLocn = null;
		dir = null;
		title = null;

		hideWaitStage(PAUSED);

		if (type == Simulation.AUTOSAVE) {
			dir = Simulation.AUTOSAVE_DIR;
			masterClock.setSaveSim(Simulation.AUTOSAVE, null);

		} else if (type == Simulation.SAVE_DEFAULT) {
			dir = Simulation.DEFAULT_DIR;
			masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);

		} else if (type == Simulation.SAVE_AS) {

			Platform.runLater(() -> {
				FileChooser chooser = new FileChooser();
				dir = Simulation.DEFAULT_DIR;
				File userDirectory = new File(dir);
				title = Msg.getString("MainScene.dialogSaveSim");
				chooser.setTitle(title); // $NON-NLS-1$
				chooser.setInitialDirectory(userDirectory);
				// Set extension filter
				FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter("Simulation files (*.sim)",
						"*.sim");
				FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("all files (*.*)", "*.*");
				chooser.getExtensionFilters().addAll(simFilter, allFilter);
				File selectedFile = chooser.showSaveDialog(stage);
				if (selectedFile != null)
					fileLocn = selectedFile;
				else {
					hideWaitStage(PAUSED);
					return;
				}

				showWaitStage(SAVING);

				saveExecutor.execute(new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						try {
							masterClock.setSaveSim(Simulation.SAVE_AS, fileLocn);

							while (masterClock.isSavingSimulation())
								TimeUnit.MILLISECONDS.sleep(200L);

						} catch (Exception e) {
							logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
							e.printStackTrace(System.err);
						}

						return null;
					}

					@Override
					protected void succeeded() {
						super.succeeded();
						hideWaitStage(SAVING);
					}
				});

			});
		}

	}

	public void startPausePopup() {
		if (gameScene == null) {
			// if (messagePopup.numPopups() < 1) {
			// Note: (NOT WORKING) popups.size() is always zero no matter what.
			Platform.runLater(() -> {
				// messagePopup.popAMessage(PAUSE, ESC_TO_RESUME, " ", stage, Pos.TOP_CENTER,
				// PNotification.PAUSE_ICON)
				boolean hasIt = false;
				for (Node node : rootStackPane.getChildren()) {// root.getChildrenUnmodifiable()) {
					if (node == pausePane) {
						hasIt = true;
						break;
					}
				}
				if (!hasIt) {
					pausePane.setLayoutX((scene.getWidth() - pausePane.getPrefWidth()) / 2D);
					pausePane.setLayoutY((scene.getHeight() - pausePane.getPrefHeight()) / 2D);
					// root.getChildrenUnmodifiable().add(pausePane);
					rootStackPane.getChildren().add(pausePane);
				}
			});
		}
	}

	public void stopPausePopup() {
		if (gameScene == null) {
			Platform.runLater(() -> {
				// messagePopup.stop()
				boolean hasIt = false;
				for (Node node : rootStackPane.getChildren()) {// root.getChildrenUnmodifiable()) {
					if (node == pausePane) {
						hasIt = true;
						break;
					}
				}
				if (hasIt)
					rootStackPane.getChildren().remove(pausePane);
				// root.getChildrenUnmodifiable().remove(pausePane);
			});
		}
	}

	/**
	 * Creates the pause box to be displayed on the root pane.
	 * 
	 * @return VBox
	 */
	// 2017-04-12 Add pause pane
	private VBox createPausePaneContent() {
		VBox vbox = new VBox();
		vbox.setPrefSize(150, 150);

		Label label = new Label("||");
		label.setAlignment(Pos.CENTER);
		label.setPadding(new Insets(10));
		label.setStyle("-fx-font-size: 48px; -fx-text-fill: cyan;");
		// label.setMaxWidth(250);
		label.setWrapText(true);

		Label label1 = new Label("ESC to resume");
		label1.setAlignment(Pos.CENTER);
		label1.setPadding(new Insets(2));
		label1.setStyle(" -fx-font: bold 11pt 'Corbel'; -fx-text-fill: cyan;");
		vbox.getChildren().addAll(label, label1);
		vbox.setAlignment(Pos.CENTER);

		return vbox;
	}

	/**
	 * Pauses the simulation.
	 */
	public void pauseSimulation(boolean showPane) {
		if (exitDialog == null || !exitDialog.isVisible()) {
			isShowingDialog = true;
			masterClock.setPaused(true, showPane);
			timeline.pause();
			if (showPane && !masterClock.isSavingSimulation())
				startPausePopup();
		}
	}

	/**
	 * Unpauses the simulation.
	 */
	public void unpauseSimulation() {
		isShowingDialog = false;
		masterClock.setPaused(false, true);
		timeline.play();
		stopPausePopup();
	}

	public boolean startPause() {
		boolean previous = masterClock.isPaused();
		if (!previous) {
			pauseSimulation(true);
		}
		// desktop.getTimeWindow().enablePauseButton(false);
		masterClock.setPaused(true, true);
		return previous;
	}

	public double slowDownTimeRatio() {
		double last_tr = masterClock.getTimeRatio();
		masterClock.setTimeRatio(1.0);
		return last_tr;
	}

	public void speedUpTimeRatio(double previous) {
		double now = masterClock.getTimeRatio();
		if (previous != 1.0) {
			if (now == 1.0) {
				masterClock.setTimeRatio(previous);
			}
		} else {
			if (now != 1.0) {
				masterClock.setTimeRatio(1.0);
			}
		}
		now = masterClock.getTimeRatio();
	}

	public void endPause(boolean previous) {
		boolean now = masterClock.isPaused();
		if (!previous) {
			if (now) {
				unpauseSimulation();
			}
		} else {
			if (!now) {
				unpauseSimulation();
			}
		}
		desktop.getTimeWindow().enablePauseButton(true);
	}

	/**
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves
	 * the main menu running
	 */
	private void endSim() {
		Simulation.instance().endSimulation();
		Simulation.instance().getSimExecutor().shutdownNow();
		mainSceneExecutor.shutdownNow();
		getDesktop().clearDesktop();
		timeline.stop();
		stage.close();
	}

	/**
	 * Exits the current simulation and the main menu.
	 */
	public void exitSimulation() {
		logger.info("Exiting the simulation. Bye!");
		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);
		masterClock.exitProgram();

	}

	public MainSceneMenu getMainSceneMenu() {
		return menuBar;
	}

	public Stage getStage() {
		return stage;
	}

	private void createDesktopNode() {
		// Create group to hold swingNode which in turns holds the Swing desktop
		desktopNode = new SwingNode();
		desktop = new MainDesktopPane(this);
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setSize(screen_width, screen_height);
		// Add main pane
		mainPane.add(desktop, BorderLayout.CENTER);
		SwingUtilities.invokeLater(() -> desktopNode.setContent(mainPane));
		desktopNode.requestFocus();
	}

	public SwingNode getDesktopNode() {
		return desktopNode;
	}

	/**
	 * Open the exit dialog box
	 */
	public void dialogOnExit() {
		if (!masterClock.isPaused()) {
			if (exitDialog == null) {
				isShowingDialog = true;

				Label l = createBlendLabel(Msg.getString("MainScene.exit.header"));
				l.setPadding(new Insets(10, 10, 10, 10));
				l.setFont(Font.font(null, FontWeight.BOLD, 14));

				HBox hb = new HBox();
				JFXButton b0 = new JFXButton("Save & Exit");
				b0.setStyle("-fx-background-color: white;");
				JFXButton b1 = new JFXButton("Exit");
				b1.setStyle("-fx-background-color: white;");
				JFXButton b2 = new JFXButton("Back");
				b2.setStyle("-fx-background-color: white;");
				// b0.setPadding(new Insets(2, 2, 2, 2));

				hb.getChildren().addAll(b0, b1, b2);
				HBox.setMargin(b0, new Insets(3, 3, 3, 3));
				HBox.setMargin(b1, new Insets(3, 3, 3, 3));
				HBox.setMargin(b2, new Insets(3, 3, 3, 3));

				VBox vb = new VBox();
				vb.setPadding(new Insets(5, 5, 5, 5));
				vb.getChildren().addAll(l, hb);
				StackPane sp = new StackPane(vb);
				sp.setStyle("-fx-background-color:rgba(0,0,0,0.1);");
				StackPane.setMargin(vb, new Insets(10, 10, 10, 10));

				exitDialog = new JFXDialog();
				exitDialog.setDialogContainer(rootStackPane);
				exitDialog.setContent(sp);
				exitDialog.show();

				b0.setOnAction(e -> {
					exitDialog.close();
					saveOnExit();
					e.consume();
				});

				b1.setOnAction(e -> {
					exitDialog.close();
					endSim();
					exitSimulation();
					Platform.exit();
					System.exit(0);
					e.consume();
				});

				b2.setOnAction(e -> {
					isShowingDialog = false;
					exitDialog.close();
					e.consume();
				});

			}

			else if (!exitDialog.isVisible()) {
				isShowingDialog = true;
				exitDialog.show();
			}
		}

	}

	/**
	 * Initiates the process of saving a simulation.
	 */
	public void saveOnExit() {
		showWaitStage(SAVING);
		desktop.getTimeWindow().enablePauseButton(false);
		// Save the simulation as default.sim
		masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);

		saveExecutor.execute(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);

					while (masterClock.isSavingSimulation())
						TimeUnit.MILLISECONDS.sleep(200L);

				} catch (Exception e) {
					logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
					e.printStackTrace(System.err);
				}
				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				hideWaitStage(SAVING);
				endSim();
				exitSimulation();
				Platform.exit();
				System.exit(0);
			}
		});

	}

	public void openInitialWindows() {
		if (OS.contains("mac")) {
			// SwingUtilities needed below for MacOSX
			SwingUtilities.invokeLater(() -> {
				desktop.openInitialWindows();
			});
		} else {
			desktop.openInitialWindows();
		}

		quote = new QuotationPopup(this);
		// popAQuote();

		isMainSceneDoneLoading = true;

	}

	public void popAQuote() {
		quote.popAQuote(stage);
	}

	public MarsNode getMarsNode() {
		return marsNode;
	}

	public static int getTheme() {
		return theme;
	}

	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	private MenuItem registerAction(MenuItem menuItem) {
		/*
		 * menuItem.setOnAction(new EventHandler<ActionEvent>() { public void
		 * handle(ActionEvent t) { // showPopup(borderPane, "You clicked the " +
		 * menuItem.getText() + " icon"); System.out.println("You clicked the " +
		 * menuItem.getText() + " icon"); ? } });
		 */
		menuItem.setOnAction(e -> {
			System.out.println("You clicked the " + menuItem.getText() + " icon");
			e.consume();
		});

		return menuItem;
	}

	/*
	 * Create the progress circle animation while waiting for loading the main scene
	 */
	public void createProgressCircle(int type) {

		if (type == LOADING) {
			ProgressIndicator indicator = new ProgressIndicator();
			indicator.setSkin(null);
			// indicator.setOpacity(.5);
			indicator.setStyle("-fx-background-color: transparent; ");
			StackPane stackPane = new StackPane();
			Rectangle2D rect = Screen.getPrimary().getBounds();
			int w = (int) rect.getWidth();
			int h = (int) rect.getHeight();
			//stackPane.setLayoutX(w/2);
			//stackPane.setLayoutY(h/2);
			stackPane.setTranslateX(w/2);
			stackPane.setTranslateY(h/2);
			// stackPane.setOpacity(0.5);
			stackPane.getChildren().add(indicator);
			StackPane.setAlignment(indicator, Pos.CENTER);
			stackPane.setBackground(Background.EMPTY);
			stackPane.setStyle(
					// "-fx-border-style: none; "
					"-fx-background-color: transparent; "
			// + "-fx-background-radius: 3px;"
			);
			Scene scene = new Scene(stackPane, 100, 100);
			scene.setFill(Color.TRANSPARENT);
			loadingStage = new Stage();
			//loadingStage.centerOnScreen();
			// loadingCircleStage.setOpacity(1);
			if (!isFXGL) setEscapeEventHandler(true, loadingStage);
			//loadingStage.initOwner(stage);
			loadingStage.initModality(Modality.WINDOW_MODAL); // Modality.NONE is by default if initModality() is NOT
																// specified.
			loadingStage.getIcons()
					.add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
			loadingStage.initStyle(StageStyle.TRANSPARENT);
			loadingStage.setScene(scene);
			loadingStage.hide();
		}

		else if (type == AUTOSAVING) {
			// ProgressIndicator indicator = new ProgressIndicator();
			MaskerPane mPane = new MaskerPane();
			mPane.setText("Autosaving");
			mPane.setSkin(null);
			// indicator.setOpacity(.5);
			mPane.setStyle("-fx-background-color: transparent; ");
			// indicator.setScaleX(1.5);
			// indicator.setScaleY(1.5);
			savePane = new StackPane();
			// stackPane.setOpacity(0.5);
			savePane.getChildren().add(mPane);
			StackPane.setAlignment(mPane, Pos.CENTER);
			savePane.setBackground(Background.EMPTY);
			savePane.setStyle(// "-fx-border-style: none; "
					"-fx-background-color: transparent; "
			// + "-fx-background-radius: 3px;"
			);

			savingStage = new Stage();
			savingStage.initOwner(stage);
			savingStage.initModality(Modality.WINDOW_MODAL); // Modality.NONE is by default if initModality() is NOT
																// specified.
			savingStage.getIcons()
					.add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
			savingStage.initStyle(StageStyle.TRANSPARENT);

			savingScene = new Scene(savePane);// , 150, 150);
			savingScene.setFill(Color.TRANSPARENT);
			savingStage.setScene(savingScene);
			savingStage.hide();

		}

		else if (type == SAVING) {
			// ProgressIndicator indicator = new ProgressIndicator();
			MaskerPane mPane = new MaskerPane();
			mPane.setText("Saving");
			mPane.setSkin(null);
			// indicator.setOpacity(.5);
			mPane.setStyle("-fx-background-color: transparent; ");
			// indicator.setScaleX(1.5);
			// indicator.setScaleY(1.5);
			sPane = new StackPane();
			// stackPane.setOpacity(0.5);
			sPane.getChildren().add(mPane);
			StackPane.setAlignment(mPane, Pos.CENTER);
			sPane.setBackground(Background.EMPTY);
			sPane.setStyle(// "-fx-border-style: none; "
					"-fx-background-color: transparent; "
			// + "-fx-background-radius: 3px;"
			);

		}

		//else if (type == PAUSED) {
			// messagePopup = new MessagePopup();
		//}

		else
			System.out.println("MainScene's createProgressCircle() : type is invalid");

	}

	/**
	 * Starts the wait stage in an executor thread
	 * @param type
	 */
	public void showWaitStage(int type) {
		if (type == AUTOSAVING) {
			savingScene.setRoot(savePane);
		} else if (type == SAVING) {
			savingScene.setRoot(sPane);
		}

		mainSceneExecutor.execute(new LoadWaitStageTask(type));
	}

	/*
	 * Set up a wait stage
	 * @param type
	 */
	class LoadWaitStageTask implements Runnable {
		int type;

		LoadWaitStageTask(int type) {
			this.type = type;
		}

		public void run() {
			// logger.info("LoadWaitStageTask is on " + Thread.currentThread().getName());
			Platform.runLater(() -> {
				// FXUtilities.runAndWait(() -> {}) does NOT work
				if (type == AUTOSAVING || type == SAVING) {
					stopPausePopup();
					setMonitor(savingStage);
					savingStage.setX((int) (stage.getX() + scene.getWidth() / 2 - 50));
					savingStage.setY((int) (stage.getY() + scene.getHeight() / 2 - 50));
					savingStage.show();
				} else if (type == LOADING) {
					setMonitor(loadingStage);
					loadingStage.show();
				} else if (type == PAUSED) {
					stopPausePopup();
					startPausePopup();
				}
			});
		}
	}

	public void hideWaitStage(int type) {
		// FXUtilities.runAndWait(() -> { // not working for loading sim
		if (type == AUTOSAVING || type == SAVING) {
			savingStage.hide();
		} else if (type == LOADING) {
			loadingStage.hide();
			loadingStage.close();
		} else if (type == PAUSED) {
			stopPausePopup();
		} else
			System.out.println("MainScene's hideWaitStage() : type is invalid");

	}

	/**
	 * Sets the stage properly on the monitor where the mouse pointer is
	 * @param stage
	 */
	public void setMonitor(Stage stage) {
		// TODO: how to run on the "Active monitor" as chosen by user ?
		// Note : "Active monitor is defined by whichever computer screen the mouse pointer is or where the command console that starts mars-sim.
		// By default, it runs on the primary monitor (aka monitor 0 as reported by windows os only.
		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762
		StartUpLocation startUpLoc = null;

		if (anchorPane == null) {
			StackPane pane = new StackPane();// starfield);
			pane.setPrefHeight(sceneWidth.get());
			pane.setPrefWidth(sceneHeight.get());
			// pane.prefHeightProperty().bind(scene.heightProperty());
			// pane.prefWidthProperty().bind(scene.widthProperty());

			startUpLoc = new StartUpLocation(pane.getPrefWidth(), pane.getPrefHeight());
		} else {
			startUpLoc = new StartUpLocation(scene.getWidth(), scene.getHeight());
		}

		double xPos = startUpLoc.getXPos();
		double yPos = startUpLoc.getYPos();
		// Set Only if X and Y are not zero and were computed correctly
		//if (xPos != 0 && yPos != 0) {
			stage.setX(xPos);
			stage.setY(yPos);
		//}
		
		//System.out.println("xPos : " + xPos);
		//System.out.println("yPos : " + yPos);

/*	
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		stage.setX(bounds.getMinX());
		stage.setY(bounds.getMinY());
		
		//stage.setWidth(bounds.getWidth());
		//stage.setHeight(bounds.getHeight());			

		System.out.println("bounds.getMinX() : " + bounds.getMinX());
		System.out.println("bounds.getMinY() : " + bounds.getMinY());
*/

		//stage.centerOnScreen(); // this will cause the stage to be pinned slight upward.
	}

	/**
	 * Starts the main scene executor thread
	 */
	private void startMainSceneExecutor() {
		// INFO: Simulation's startSimExecutor() is on JavaFX-Launcher Thread
		mainSceneExecutor = Executors.newSingleThreadExecutor();
	}

	public JFXTabPane getJFXTabPane() {
		return jfxTabPane;
	}

	public boolean isMapTabOpen() {
		if (jfxTabPane != null)
			return jfxTabPane.getSelectionModel().isSelected(MainScene.MAP_TAB);
		else
			return false;
	}

	public boolean isMainTabOpen() {
		if (jfxTabPane != null)
			return jfxTabPane.getSelectionModel().isSelected(MainScene.MAIN_TAB);
		else
			return false;
	}
	
	public Parent getRoot() {
		return root;
	}

	public Scene getScene() {
		return scene;
	}

	public JFXSlider getZoom() {
		return zoomSlider;
	}

	public JFXComboBox<Settlement> getSBox() {
		return sBox;
	};

	public Settlement getSettlement() {
		return sBox.getSelectionModel().getSelectedItem();
	}

	public void setSettlement(Settlement s) {
		Platform.runLater(() -> {
			openSettlementMap();
			sBox.getSelectionModel().select(s);
		});
	}
	
	/**
	 * Sets up the JavaFX's tooltip
	 * @param n Node
	 * @param s tooltip's hint text
	 */
	public void setQuickToolTip(Node n, String s) {
		Tooltip tt = new Tooltip(s);
		tt.getStyleClass().add("ttip");
		Tooltip.install(n, tt);
		tt.setOnShowing(ev -> tt.setText(s));
/*		
		

		n.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// position tooltip at bottom right of the node
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
*/
	}

	private float convertSlider2Volume(float y) {
		return y / 100f;
	}


	public JFXButton getMarsNetBtn() {
		return marsNetBtn;
	}

	public static void disableSound() {
		soundPlayer.enableMasterGain(false);
		if (musicSlider != null)
			musicSlider.setDisable(true);// .setValue(0);
		if (soundEffectSlider != null)
			soundEffectSlider.setDisable(true);// .setValue(0);

	}

	public void setScreenSize(int w, int h) {
		screen_width = w;
		screen_height = h;
	}

	public int getWidth() {
		return screen_width;
	}

	public int getHeight() {
		return (int) sceneHeight.get();// screen_height;
	}

	// public void setSound(float music_volume, float sound_effect_volume) {
	// this.music_volume = music_volume;
	// this.sound_effect_volume = sound_effect_volume;
	// }

	public float getMusic() {
		return musicSliderValue;
	}

	public float getSoundEffect() {
		return soundEffectSliderValue;
	}

	public boolean isMinimized() {
		return minimized;
	}
	
	public void destroy() {
		quote = null;
		// messagePopup = null;
		// topFlapBar = null;
		marsNetBox = null;
		marsNetBtn = null;
		chatBox = null;
		mapStackPane = null;
		mainStackPane = null;
		dashboardStackPane = null;
		root = null;
		rootStackPane = null;
		minimapStackPane = null;
		speedPane = null;
		soundPane = null;
		calendarPane = null;
		settlementBox = null;
		chatBoxPane = null;
		pausePane = null;
		savePane = null;
		sPane = null;

		anchorPane = null;
		newSimThread = null;
		stage = null;
		loadingStage = null;
		savingStage = null;
		timeline = null;
		// notificationPane = null;
		desktop.destroy();
		desktop = null;
		menuBar = null;
		marsNode = null;
		transportWizard = null;
		constructionWizard = null;
	}
}
