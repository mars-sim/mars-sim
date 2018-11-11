/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.1.0 2017-10-05
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import javafx.scene.Parent;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockUtils;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.UpTimer;
import org.mars_sim.msp.ui.javafx.dashboard.DashboardController;
import org.mars_sim.msp.ui.javafx.dotMatrix.DotMatrix;
import org.mars_sim.msp.ui.javafx.dotMatrix.DotMatrixBuilder;
import org.mars_sim.msp.ui.javafx.dotMatrix.MatrixFont8x8;
import org.mars_sim.msp.ui.javafx.dotMatrix.DotMatrix.DotShape;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;
import org.mars_sim.msp.ui.javafx.quotation.QuotationPopup;
import org.mars_sim.msp.ui.javafx.tools.DraggableNode;
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

import jiconfont.icons.FontAwesome;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

import com.alee.laf.WebLookAndFeel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.UIManagers;
//import com.alee.managers.UIManagers;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.input.ActionType;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.InputMapping;
import com.almasb.fxgl.input.OnUserAction;
import com.almasb.fxgl.scene.GameScene;
import com.almasb.fxgl.ui.InGameWindow;

import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
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
import com.jfoenix.controls.JFXToolbar;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

import org.controlsfx.control.MaskerPane;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import static org.fxmisc.wellbehaved.event.EventPattern.*;
import static org.fxmisc.wellbehaved.event.InputMap.*;

/**
 * The MainScene class is the primary Stage for MSP. It is the container for
 * housing desktop swing node, javaFX UI, pull-down menu and icons for tools.
 */
public class MainScene implements ClockListener {

	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	public final static String THEME_PATH = "/fxui/css/theme/";

	public final static String SPINNER_CSS = "/fxui/css/spinner/spinner.css";

	public final static String JFX_ORANGE_CSS = "/fxui/css/tab/jfx_orange.css";
	public final static String JFX_BLUE_CSS = "/fxui/css/tab/jfx_blue.css";

	public final static String ORANGE_CSS_THEME = THEME_PATH + "nimrodskin.css";
	public final static String BLUE_CSS_THEME = THEME_PATH + "snowBlue.css";

	public final static String BREAKING_NEWS = "Breaking News: ";
	public final static String HEALTH_NEWS = "Health News: ";
	public final static String MISSION_REPORTS = "Mission Reports: ";
	public final static String HAZARD = "Safety News: ";

	public static String OS = Simulation.OS.toLowerCase();
	// System.getProperty("os.name").toLowerCase();
	// e.g. 'linux', 'mac os x'

	private static final int TIME_DELAY = SettlementWindow.TIME_DELAY;

	private static final double TITLE_HEIGHT = 0;//45.0;
	
	private static final int LIME = DotMatrix.convertToInt(Color.LIME);
	private static final int RED = DotMatrix.convertToInt(Color.RED);
	private static final int ORANGE = DotMatrix.convertToInt(Color.ORANGE);
	private static final int YELLOW = DotMatrix.convertToInt(Color.YELLOW);
	private static final int BLACK = DotMatrix.convertToInt(Color.BLACK);

	public enum ThemeType {
		System, Nimbus, Nimrod, Weblaf
	}

	public static ThemeType defaultThemeType = ThemeType.Weblaf;

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
	
	public static int chatBoxHeight = 256;
	public static int LINUX_WIDTH = 270+10;
	public static int MACOS_WIDTH = 230+10;
	public static int WIN_WIDTH = 230+10;
	
	public static int spacing = 0;

	public static boolean isFXGL = false;
	public static boolean menuBarVisible = false;

	static boolean isShowingDialog = false;
	
	private static final int DEFAULT_ZOOM = 10;
	
	private static int defaultThemeColor = 0;
	private static int theme = 6; // 6 is snow blue; 7 is the mud orange with nimrod

	private static final double ROTATION_CHANGE = Math.PI / 20D;

	private static final String LAST_SAVED = "Last Saved : ";
	// private static final String EARTH_DATE_TIME = " ";//EARTH : ";
	private static final String MARS_DATE_TIME = " ";// MARS : ";
	// private static final String UMST = " (UMST)";
	private static final String COLON = ":";
//	private static final String ONE_SPACE = " ";
	private static final String MONTH = "    Month : ";
	private static final String ORBIT = "Orbit : ";
	private static final String ADIR = "Adir";

	// private static final String BENCHMARK = "Benchmark :";
	private static final String UPTIME = "UpTime :";
	private static final String TPS = "# Ticks/s :";
	private static final String SEC = "One Real Sec :";
	private static final String TR = "Time Ratio :";
	private static final String DTR = "Default TR :";
	private static final String HZ = " Hz";
	private static final String REFRESH = "Refresh Rate :";

	private static final String SOLAR_LONGITUDE = "Areocentric Ls : ";//"Solar Longitude : ";
	private static final String NOTE_MARS = " Note : Mars's now at ";
	private static final String APHELION = "aphelion ";
	private static final String PERIHELION = "perihelion ";

	private static final String NORTH = "Northern Hemi. : ";
	private static final String SOUTH = "Southern Hemi. : ";

	private static final String LABEL_CSS_STYLE = "-fx-background-color:transparent; -fx-text-fill: white;"
			+ "-fx-font-size: 11px;" + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
			+ "-fx-font-weight:bold; -fx-text-alignment: center; -fx-alignment: CENTER;";

	private static final String PANE_CSS = "-fx-background-radius: 10; -fx-background-color:transparent;";//"jfx-popup-container; -fx-background-radius: 10; -fx-background-color:transparent;";

	private int screen_width = DEFAULT_WIDTH;
	private int screen_height = DEFAULT_HEIGHT;
	private int solCache = 0;

	// For DotMatrix billboard
	private int textLength;
	private int textLengthInPixel;
	private int offset;
	private int x;

	public double musicSliderValue = AudioPlayer.DEFAULT_VOL * 100;
	public double soundEffectSliderValue = AudioPlayer.DEFAULT_VOL * 100;

	private double musicSliderCache = 0;
	private double effectSliderCache = 0;

	private double refreshCache;
	
	private long lastTimerCall;

	private DoubleProperty musicProperty = new SimpleDoubleProperty(musicSliderValue);
	private DoubleProperty soundEffectProperty = new SimpleDoubleProperty(soundEffectSliderValue);

	private double tpsCache;

	private boolean minimized = false;
	private boolean flag = true;
	private boolean isMainSceneDoneLoading = false;
	private boolean isFullScreenCache = false;
	
	private boolean lastMusicMuteBoxSetting;
	private boolean lastSoundEffectMuteBoxSetting;

	private DoubleProperty sceneWidth;// = new SimpleDoubleProperty(DEFAULt_WIDTH);//1366-40;
	private DoubleProperty sceneHeight;// = new SimpleDoubleProperty(DEFAULt_HEIGHT); //768-40;

	private volatile transient ExecutorService mainSceneExecutor;

	private String newsHeader;
	private String messageCache;
	private String upTimeCache = "";
	private String themeSkin = "nimrod";
	private String title = null;
	private String dir = null;
	private String oldLastSaveStamp = null;

	private DotMatrix matrix;

	private ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

	private DraggableNode dragNode;
	private GameScene gameScene;
	private Parent root;
//	private StackPane sMapToolPane;
	private StackPane rootStackPane;
	private StackPane mainStackPane;
	private StackPane dashboardStackPane; // monPane,
	private StackPane sMapStackPane;
	private Group minimapGroup;
	private StackPane speedPane;
	private StackPane soundPane;
	private StackPane calendarPane; // farmPane,
	private StackPane settlementBox;
	private StackPane chatBoxPane;
	private StackPane pausePane;
	private StackPane asPane;
	private StackPane sPane;
	private StackPane billboard;

	private AnchorPane stageAnchorPane;
	private AnchorPane mapsAnchorPane;
	private AnchorPane mapToolAnchorPane;
	private SwingNode desktopNode;
	private SwingNode mapNode;
	private SwingNode minimapNode;
	// guideNode, monNode, missionNode, resupplyNode, sciNode, guideNode ;

//	private CustomStage customStage;
	private Stage stage;
	private Stage savingStage;
//	private SceneManager sceneManager;
	private Scene scene;
	private Scene savingScene;

	private File fileLocn = null;

	private JFXDatePicker datePickerFX;
	private JFXTimePicker timePickerFX;

	private IconNode marsTimeIcon;

	private HBox earthTimeBox;
	private HBox marsTimeBox;

	private static Label effectLabel;
	private static Label trackLabel;

	private Label marsTimeLabel;
	private Label lastSaveLabel;
	private Label tpsLabel;
	private Label realTimeLabel;
	private Label upTimeLabel;
	private Label noteLabel;
	private Label refreshLabel;

	private Button marsTimeButton;

	private Text LSText;
	private Text monthText;
	private Text yearText;
	private Text northText;
	private Text southText;
	private Text radiusText;

	private IconNode soundIcon;
	private IconNode marsNetIcon;
	private IconNode speedIcon;// , farmIcon;

	private DropShadow borderGlow;
	private Blend blend;
	private VBox mapLabelBox;
	private VBox speedVBox;
	private static VBox soundVBox;
	private VBox toolbarBox;
	private Tab mainTab;
	private Tab dashboardTab;

	private Spinner<Integer> timeRatioSpinner;
	private JFXComboBox<Settlement> sBox;
	private JFXToggleButton cacheToggle;
	private JFXToggleButton minimapToggle;
	private JFXToggleButton sMapToggle;
	private JFXSlider zoomSlider;
	private JFXToolbar toolbar;

	private static JFXSlider musicSlider;
	private static JFXSlider soundEffectSlider;

	private JFXButton soundBtn;
	private JFXButton marsNetBtn;
	private JFXButton rotateCWBtn;
	private JFXButton rotateCCWBtn;
	private JFXButton recenterBtn;
	private JFXButton speedBtn;

	private JFXPopup soundPopup;
	private JFXPopup marsNetBox;
	private JFXPopup marsCalendarPopup;
	private JFXPopup simSpeedPopup;

	private JFXTabPane tabPane;

	private JFXDialog exitDialog;

	private static CheckBox musicMuteBox;
	private static CheckBox soundEffectMuteBox;

	private ESCHandler esc = null;
	private Timeline timeLabeltimer;

	private static ChatBox chatBox;
	private static MainDesktopPane desktop;
	private static MainSceneMenu menuBar;

	private static MarsNode marsNode;
	private static TransportWizard transportWizard;
	private static ConstructionWizard constructionWizard;

	private static QuotationPopup quote;

	private static Simulation sim = Simulation.instance();
	private static UnitManager unitManager = sim.getUnitManager();

	private static MasterClock masterClock = sim.getMasterClock();
	private static EarthClock earthClock;
	private static MarsClock marsClock;

	private static MarsClock lastNewsClock;
	
	private SettlementWindow settlementWindow;
	private NavigatorWindow navWin;
	private SettlementMapPanel mapPanel;

	private static AudioPlayer soundPlayer;
	private static MarsCalendarDisplay calendarDisplay;
	private static UpTimer uptimer;

	private AnimationTimer billboardTimer;

	private static OrbitInfo orbitInfo;

	private DecimalFormat df = new DecimalFormat("0.000");

//	private static FXGraphics2D g2;
//	
//	private static Canvas mapCanvas;
	
	/**
	 * Constructor for MainScene
	 */
	public MainScene(int width, int height, GameScene gameScene) {
		
		screen_width = width;
		screen_height = height;
		this.gameScene = gameScene;
		sceneWidth = new SimpleDoubleProperty(DEFAULT_WIDTH);
		sceneHeight = new SimpleDoubleProperty(DEFAULT_HEIGHT);// - TAB_PANEL_HEIGHT);

		isMainSceneDoneLoading = false;

		if (gameScene != null) {
			stage = ((Stage) gameScene.getRoot().getScene().getWindow());
			isFXGL = true;
			spacing = 10;
			// stage.initStyle(StageStyle.DECORATED);
		} else {
			stage = new Stage();			
//			String title = Simulation.title;
//			try {
//				stage = new CustomStageBuilder()
//					.setIcon("/icons/lander_hab64.png")             						     
//				    .setTitleColor("#F4A576")      //Color of title text
//				    .setWindowColor("#5675FA")              //Color of the window
//				    .setDimensions(sceneWidth.get(), sceneHeight.get(), width, height)
//	                .setWindowTitle(title, HorizontalPos.RIGHT, HorizontalPos.LEFT)    
//				    .build();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			
		}

		stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
		stage.setMinWidth(sceneWidth.get());
		stage.setMinHeight(sceneHeight.get());
		
		// Enable Full Screen
		stage.setFullScreenExitHint(
				"Use Ctrl+F (or Meta+F in macOS) to toggle between Full-Screen and the Window mode");
		stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		
		// Detect if a user hits the top-right close button
		stage.setOnCloseRequest(e -> {
			if (isFXGL) {
				Input input = FXGL.getInput();
				input.mockKeyPress(KeyCode.ESCAPE);
				input.mockKeyRelease(KeyCode.ESCAPE);
				endSim();
				exitSimulation();
				Platform.exit();
				System.exit(0);
			} else {
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
				if (billboardTimer != null) {
					if (t1)
						billboardTimer.stop();
					else
						billboardTimer.start();
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

		// SwingUtilities.invokeLater(() -> {
		WebLookAndFeel.install();
		UIManagers.initialize();
		// });

		Platform.runLater(() -> {

			prepareScene();
			initializeTheme();
			prepareOthers();
			
			// Call setMonitor() for screen detection
			setMonitor(stage);
			stage.centerOnScreen();
			stage.setTitle(Simulation.title);
			stage.setResizable(false);
			stage.show();
			stage.requestFocus();

			createSavingIndicator();
			openInitialWindows();
//			hideWaitStage(MainScene.LOADING);

		});
		
		// Add MainScene to MasterClock's clock listener
		if (masterClock == null)
			masterClock = sim.getMasterClock();			
		masterClock.addClockListener(this);

	}

	/*
	 * Prepares the scene in the main scene
	 */
	public void prepareScene() {
		// logger.config("MainMenu's prepareScene() is on " +
		// Thread.currentThread().getName());
		// TODO: Need to revamp the use of UIConfig
		UIConfig.INSTANCE.useUIDefault();
		// Create swing node desktop 
		createDesktopNode();
		// Create menuBar
		menuBar = new MainSceneMenu(this, desktop);
		// Creates and initialize scene
		scene = initializeScene();
		// Switch from the main menu's scene to the main scene's scene
		if (gameScene == null) {	
			stage.setScene(scene);
//			sceneManager = CustomStage.getDefaultSceneManager();
//			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxui/fxml/main/Scene.fxml"));
//			try {
//				sceneManager.addScene("s0", loader.load(), loader.getController());
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			Node scene = manager.getScene("Scene"); //gets Scene1.fxml view
//			manager.getController("Scene"); //gets the Controller of Scene1.fxml
		}

		// Load soundPlayer from desktop 
		soundPlayer = desktop.getSoundPlayer();
		// Setup key events using wellbehavedfx
		setupKeyEvents();
	}

	public void createLoadingIndicator() {
		// Added mainSceneExecutor for executing wait stages
		startMainSceneExecutor();
//		createProgressCircle(LOADING);
	}

	public void createSavingIndicator() {
		createProgressCircle(AUTOSAVING);
		createProgressCircle(SAVING);
	}

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
					masterClock.setPaused(false, true);
				} else {
					masterClock.setPaused(true, true);
				}
			}
		}
	}

	/**
	 * Sets up the UI theme and the two timers as a thread pool task
	 */
	public class MainSceneTask implements Runnable {
		public void run() {
			logger.config("MainScene's MainSceneTask is in " + Thread.currentThread().getName() + " Thread");
			// Set look and feel of UI.
			UIConfig.INSTANCE.useUIDefault();
		}
	}

	/**
	 * Prepares the transport wizard, construction wizard, autosave timer and earth
	 * timer
	 */
	public void prepareOthers() {
		// logger.config("MainScene's prepareOthers() is on " +
		// Thread.currentThread().getName() + " Thread");
		uptimer = masterClock.getUpTimer();
		startEarthTimer();
		transportWizard = new TransportWizard(this, desktop);
		constructionWizard = new ConstructionWizard(this, desktop);

//		if (soundPlayer.isSoundDisabled()) {
//			logger.log(Level.SEVERE, "Disabling the sound UI in MainScene.");
//			disableSound();		
//		}
	}

	/**
	 * Pauses sim and opens the transport wizard
	 * 
	 * @param buildingManager
	 */
	public synchronized void openTransportWizard(BuildingManager buildingManager) {
		// logger.config("MainScene's openTransportWizard() is on " +
		// Thread.currentThread().getName() + " Thread");
		// normally on pool-4-thread-3 Thread
		// Note: make sure pauseSimulation() doesn't interfere with
		// resupply.deliverOthers();
		// Track the current pause state
		Platform.runLater(() -> {
			// boolean previous = startPause(); ?
			pauseSimulation(false);
			transportWizard.deliverBuildings(buildingManager);
			masterClock.setPaused(false, true);
//			unpauseSimulation();
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
			masterClock.setPaused(false, true);
//			unpauseSimulation();
			// speedUpTimeRatio(previous);
		});
	}


	public ConstructionWizard getConstructionWizard() {
		return constructionWizard;
	}

	/**
	 * Setup key events using wellbehavedfx
	 */
	public void setupKeyEvents() {
		InputMap<KeyEvent> f1 = consume(keyPressed(KeyCode.F1), e -> {
			tabPane.getSelectionModel().select(MainScene.HELP_TAB);
		});
		Nodes.addInputMap(rootStackPane, f1);

		InputMap<KeyEvent> f2 = consume(keyPressed(KeyCode.F2), e -> {
			if (desktop.isToolWindowOpen(SearchWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(SearchWindow.NAME));
			else {
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(SearchWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f2);

		InputMap<KeyEvent> f3 = consume(keyPressed(KeyCode.F3), e -> {
			if (desktop.isToolWindowOpen(TimeWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(TimeWindow.NAME));
			else {
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(TimeWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f3);

		InputMap<KeyEvent> f4 = consume(keyPressed(KeyCode.F4), e -> {
			if (desktop.isToolWindowOpen(MonitorWindow.NAME)) {
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(MonitorWindow.NAME));
			} else {
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(MonitorWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f4);

		InputMap<KeyEvent> f5 = consume(keyPressed(KeyCode.F5), e -> {
			if (desktop.isToolWindowOpen(MissionWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(MissionWindow.NAME));
			else {
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(MissionWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f5);

		InputMap<KeyEvent> f6 = consume(keyPressed(KeyCode.F6), e -> {
			if (desktop.isToolWindowOpen(ScienceWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(ScienceWindow.NAME));
			else {
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(ScienceWindow.NAME));
			}
		});
		Nodes.addInputMap(rootStackPane, f6);

		InputMap<KeyEvent> f7 = consume(keyPressed(KeyCode.F7), e -> {
			if (desktop.isToolWindowOpen(ResupplyWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(ResupplyWindow.NAME));
			else {
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
				e -> toggleSound());
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
					if (isFXGL) {
						Input input = FXGL.getInput();
						input.mockKeyPress(KeyCode.ESCAPE);
						input.mockKeyRelease(KeyCode.ESCAPE);
						e.consume();
					} else {
						dialogOnExit();
						e.consume();
					}
				});
		Nodes.addInputMap(rootStackPane, ctrlX);

		InputMap<KeyEvent> ctrlT = consume(keyPressed(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)),
				e -> {
					if (theme == 0 || theme == 6) {
						setTheme(7);
					} else if (theme == 7) {
						setTheme(0);
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
		Text t = new Text();
		t.setCache(true);
		t.setX(10.0f);
		t.setY(270.0f);
		t.setFill(Color.WHITE);//.LIGHTBLUE);// .ORANGE);//.DARKSLATEGREY);
		t.setText(s);
		t.setFont(Font.font(null, FontWeight.BOLD, 14));
		return t;
	}

	public Text createBlackTextHeader(String s) {
		Text t = new Text();
		t.setCache(true);
		t.setX(10.0f);
		t.setY(270.0f);
		t.setFill(Color.BLACK);//.LIGHTBLUE);// .ORANGE);//.DARKSLATEGREY);
		t.setText(s);
		t.setFont(Font.font(null, FontWeight.BOLD, 14));
		return t;
	}
	
	public Label createBlendLabel(String s) {
		Label header_label = new Label(s);
		header_label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-background-color:transparent;"
				+ "-fx-font-weight: bold;");
		header_label.setPadding(new Insets(3, 0, 1, 2));
		return header_label;
	}

	public Text createBlendText(String s) {
		Text text = new Text(s);
		text.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-background-color:transparent;"
				+ "-fx-font-weight: normal;");
		return text;
	}

	/**
	 * Toggle the music mute box and sound effect mute box
	 */
	public void toggleSound() {
		if (!masterClock.isPaused()) {
			musicMuteBox.setSelected(!musicMuteBox.isSelected());
			soundEffectMuteBox.setSelected(!soundEffectMuteBox.isSelected());
		}
	}

	/**
	 * Mute various sound control in the main scene
	 * 
	 * @param isEffect
	 * @param isMusic
	 */
	public void muteControls(boolean isEffect, boolean isMusic) {
//		System.out.println("MainScene's mute(" + isEffect + ", " + isMusic + ")");
		if (isMusic) {
			// mute the music mute box						
			//musicMuteBox.setSelected(true);
			// mute the sound player
			//soundPlayer.mutePlayer(false, true);
			// save the music slider value into the cache
			musicSliderCache = musicSlider.getValue();
			// set the music slider value to zero
			musicSlider.setValue(0);
			// check the music mute item in menuBar
			menuBar.getMusicMuteItem().setSelected(true);
		}
		
		if (isEffect) {
			// mute the sound effect mute box			
			//soundEffectMuteBox.setSelected(true);
			// mute the sound player
			//soundPlayer.mutePlayer(true, false);
			// save the sound effect slider value into the cache
			effectSliderCache = soundEffectSlider.getValue();
			// set the sound effect slider value to zero
			soundEffectSlider.setValue(0);
			// check the sound effect mute item in menuBar
			menuBar.getSoundEffectMuteItem().setSelected(true);

		}
	}

	/**
	 * Unmute various sound control in the main scene
	 * 
	 * @param isEffect
	 * @param isMusic
	 */
	public void unmuteControls(boolean isEffect, boolean isMusic) {
//		System.out.println("MainScene's unmute(" + isEffect + ", " + isMusic + ")");
		if (isMusic) {
			// mute the music mute box						
			//musicMuteBox.setSelected(false);
			// unmute the sound player
			//soundPlayer.unmutePlayer(false, true);
			// restore the slider value from the cache
			musicSlider.setValue(musicSliderCache);
			// uncheck the music mute item in menuBar
			menuBar.getMusicMuteItem().setSelected(false);
			
			//soundPlayer.setMusicVolume(convertSlider2Volume(musicSlider.getValue()));
			
			// Play the music track			
			if (!soundPlayer.isSoundDisabled() 
					&& !MainMenu.isSoundDisabled()
					&& !musicMuteBox.isSelected()
					&& musicSlider.getValue() > 0)
						soundPlayer.resumeMusic();//.playRandomMusicTrack();

		}
		
		if (isEffect) {
			// mute the sound effect mute box			
			//soundEffectMuteBox.setSelected(true);
			// unmute the sound player
			//soundPlayer.unmutePlayer(true, false);
			// restore the slider value from the cache
			soundEffectSlider.setValue(effectSliderCache);
			// uncheck the sound effect mute item in menuBar
			menuBar.getSoundEffectMuteItem().setSelected(false);
			
			//soundPlayer.setSoundVolume(convertSlider2Volume(soundEffectSlider.getValue()));
		}
	}

	/**
	 * initializes the scene
	 *
	 * @return Scene
	 */
	public Scene initializeScene() {
		IconFontFX.register(FontAwesome.getIconFont());
		// Setup root for embedding key events
		root = new Pane();
		// Create marsnet flyout 
		marsNetBox = createFlyout();
		flag = false;
		// EffectUtilities.makeDraggable(flyout.getScene().getRoot().getStage(),chatBox);
		// Create ControlFX's StatusBar
		// statusBar = createStatusBar();
		createBlend();
		createLastSaveBar();
		createMarsTimeBar();
		createEarthTimeBar();
		createSpeedPanel();
		createSoundPopup();
		// createFarmPopup();
		// Create Snackbar
		// createJFXSnackbar();
		createJFXTabs();
		createMapToolBox();
		
		// Create pause pane
		pausePane = new StackPane();
		pausePane.setStyle("-fx-background-color:rgba(0,0,0,0.5);");
		pausePane.getChildren().add(createPausePaneContent());
		pausePane.setPrefSize(150, 150);

		if (OS.contains("mac")) {
			((MenuBar) menuBar).useSystemMenuBarProperty().set(true);
		}

		// AnchorPane.setBottomAnchor(jfxTabPane, 0.0);
		AnchorPane.setLeftAnchor(tabPane, 0.0);
		//AnchorPane.setRightAnchor(tabPane, 0.0);
		AnchorPane.setTopAnchor(tabPane, TITLE_HEIGHT);

		// AnchorPane.setRightAnchor(badgeIcon, 5.0);
		// AnchorPane.setTopAnchor(badgeIcon, 0.0);

		if (OS.contains("win")) {
			AnchorPane.setTopAnchor(speedBtn, 3.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(marsNetBtn, 3.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(lastSaveLabel, 1.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(soundBtn, 3.0 + TITLE_HEIGHT);
			// AnchorPane.setTopAnchor(farmBtn, 3.0);
			AnchorPane.setTopAnchor(earthTimeBox, 0.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(marsTimeBox, 0.0 + TITLE_HEIGHT);
		}

		else if (OS.contains("linux")) {
			AnchorPane.setTopAnchor(speedBtn, 0.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(marsNetBtn, 0.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(lastSaveLabel, 1.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(soundBtn, 0.0 + TITLE_HEIGHT);
			// AnchorPane.setTopAnchor(farmBtn, 0.0);
			AnchorPane.setTopAnchor(earthTimeBox, 0.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(marsTimeBox, 0.0 + TITLE_HEIGHT);
		}

		else if (OS.contains("mac")) {
			AnchorPane.setTopAnchor(speedBtn, 0.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(marsNetBtn, 0.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(lastSaveLabel, 0.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(soundBtn, 0.0 + TITLE_HEIGHT);
			// AnchorPane.setTopAnchor(farmBtn, 0.0);
			AnchorPane.setTopAnchor(earthTimeBox, 0.0 + TITLE_HEIGHT);
			AnchorPane.setTopAnchor(marsTimeBox, 0.0 + TITLE_HEIGHT);
		}

		AnchorPane.setRightAnchor(speedBtn, 5.0 + spacing);
		AnchorPane.setRightAnchor(marsNetBtn, 45.0 + spacing);
		AnchorPane.setRightAnchor(soundBtn, 85.0 + spacing);
		AnchorPane.setRightAnchor(marsTimeBox, sceneWidth.get() / 2);
		AnchorPane.setRightAnchor(earthTimeBox, sceneWidth.get() / 2 - earthTimeBox.getPrefWidth() - 30);
		AnchorPane.setRightAnchor(lastSaveLabel, 105.0 + spacing);
		
		createBillboard();

		stageAnchorPane = new AnchorPane();
		// anchorPane.setStyle("-fx-background-color: black; ");
		stageAnchorPane.getChildren().addAll(tabPane, marsNetBtn, speedBtn, lastSaveLabel, earthTimeBox, marsTimeBox,
				soundBtn, dragNode);// , farmBtn);//badgeIcon,borderPane, timeBar, snackbar

		// Set up stackPane for anchoring the JFXDialog box and others
		rootStackPane = new StackPane(stageAnchorPane);

		if (gameScene != null) {
			scene = gameScene.getRoot().getScene();
			gameScene.addUINode(rootStackPane);
		}
		else {
			scene = new Scene(rootStackPane, sceneWidth.get() + spacing, sceneHeight.get(), Color.TRANSPARENT);// , Color.BROWN);
//			scene = sceneManager.getScene("s0");	
//			stage.changeScene(rootStackPane);
		}

		pausePane.setLayoutX((sceneWidth.get() - pausePane.getPrefWidth()) / 2D);
		pausePane.setLayoutY((sceneHeight.get() - pausePane.getPrefHeight()) / 2D);

		stageAnchorPane.prefHeightProperty().bind(rootStackPane.heightProperty());
		stageAnchorPane.prefWidthProperty().bind(rootStackPane.widthProperty());
		
		rootStackPane.prefHeightProperty().bind(stage.heightProperty());
		rootStackPane.prefWidthProperty().bind(stage.widthProperty());

		tabPane.prefHeightProperty().bind(stageAnchorPane.heightProperty());
		tabPane.prefWidthProperty().bind(stageAnchorPane.widthProperty().subtract(spacing));

		dashboardStackPane.prefHeightProperty().bind(stageAnchorPane.heightProperty().subtract(120));
		dashboardStackPane.prefWidthProperty().bind(stageAnchorPane.widthProperty());

		mainStackPane.prefHeightProperty().bind(stageAnchorPane.heightProperty());//.subtract(30));
		mainStackPane.prefWidthProperty().bind(stageAnchorPane.widthProperty());

		// anchorTabPane is within jfxTabPane
		mapsAnchorPane.prefHeightProperty().bind(stageAnchorPane.heightProperty());//.subtract(30));
		mapsAnchorPane.prefWidthProperty().bind(stageAnchorPane.widthProperty());
		
		// Set the location of the billboard node
		dragNode.setLayoutX(sceneWidth.get() + spacing - 925 - 60);//320);
		dragNode.setLayoutY(sceneHeight.get() - 90); // 70 pixels from bottom
		
		// Setup key events using wellbehavedfx
		setupKeyEvents();

		return scene;
	}

	/**
	 * Creates the earth time bar
	 */
	public void createEarthTimeBar() {

		if (masterClock == null)
			masterClock = sim.getMasterClock();

		if (earthClock == null)
			earthClock = masterClock.getEarthClock();

		datePickerFX = new JFXDatePicker();
		datePickerFX.setValue(earthClock.getLocalDate());
		datePickerFX.setEditable(false);
		// datePickerFX.setDefaultColor(Color.valueOf("#065185"));
		// datePickerFX.setOverLay(true);
		datePickerFX.setShowWeekNumbers(true);
		datePickerFX.setPromptText("Earth Date");
		datePickerFX.setId("earth-time");

		setQuickToolTip(datePickerFX, "Earth Date");

		timePickerFX = new JFXTimePicker();
		timePickerFX.setValue(earthClock.getLocalTime());
		timePickerFX.setIs24HourView(true);
		timePickerFX.setEditable(false);
		// timePickerFX.setDefaultColor(Color.valueOf("#065185"));
		// blueDatePicker.setOverLay(true);
		timePickerFX.setPromptText("Earth Time");
		timePickerFX.setId("earth-time");

		setQuickToolTip(timePickerFX, "Earth Time in UTC");

		HBox box = new HBox(5, datePickerFX, timePickerFX);

		earthTimeBox = new HBox(box);
		earthTimeBox.setId("earth-time-box");
		earthTimeBox.setAlignment(Pos.CENTER);

		if (OS.contains("linux")) {
			earthTimeBox.setPrefSize(50, 25); // 270
			datePickerFX.setPrefSize(25, 25);// 160, 29);
			timePickerFX.setPrefSize(25, 25);// 140, 29);
		} else if (OS.contains("mac")) {
			earthTimeBox.setPrefSize(50, 23); // 230
			datePickerFX.setPrefSize(23, 23);
			timePickerFX.setPrefSize(23, 23);
		} else {
			earthTimeBox.setPrefSize(50, 25);
			datePickerFX.setPrefSize(25, 25);// 130, 25);
			timePickerFX.setPrefSize(25, 25);// 110, 25);
		}

	}

	/**
	 * Creates and returns the panel for simulation speed and time info
	 */
	public void createSpeedPanel() {

		speedBtn = new JFXButton();
		// speedBtn.setStyle(value);
		// speedBtn.getStyleClass().add("menu-button");//"button-raised");
		speedIcon = new IconNode(FontAwesome.CLOCK_O);
		speedIcon.setIconSize(20);
		// speedIcon.setStroke(Color.WHITE);

		speedBtn.setMaxSize(20, 20);
		speedBtn.setGraphic(speedIcon);
		setQuickToolTip(speedBtn, "Click to open Speed Panel");
		speedBtn.setOnAction(e -> {
			int current = (int) masterClock.getTimeRatio();
			timeRatioSpinner.getValueFactory().setValue(current);

			if (simSpeedPopup.isShowing()) {
				simSpeedPopup.hide();// close();
			} else {
				simSpeedPopup.show(speedBtn, PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
			}
			e.consume();
		});

		speedPane = new StackPane();
		speedPane.setBackground(
				new Background(new BackgroundFill(Color.rgb(54, 54, 54), CornerRadii.EMPTY, Insets.EMPTY)));

		speedPane.getStyleClass().add("jfx-popup-container; -fx-text-fill: white;");// -fx-background-radius: 10;");

		speedPane.setAlignment(Pos.CENTER);
		speedPane.setPrefHeight(100);
		speedPane.setPrefWidth(250);
		simSpeedPopup = new JFXPopup(speedPane);

		Text header_label = createTextHeader("SPEED PANEL");

		Label defaultRatioLabel0 = createLabelLeft(DTR,
				"The default time-ratio is the default ratio of simulation time to real time.");
		Label defaultRatioLabel = createLabelRight("e.g. 128x means 1 real sec corresponds to 128 sim secs.");
		int defaultRatioInt = (int) masterClock.getCalculatedTimeRatio();
		StringBuilder s0 = new StringBuilder();
		s0.append(defaultRatioInt);
		defaultRatioLabel.setText(s0.toString());

		Label realTimeLabel0 = createLabelLeft(SEC, "The amount of simulation time per real second.");
		realTimeLabel = createLabelRight(
				"e.g. 02m.08s means every real-world sec corresponds to 2 mins and 8 secs of simulation time.");
		StringBuilder s1 = new StringBuilder();
		double ratio = masterClock.getTimeRatio();
		s1.append(ClockUtils.getTimeTruncated(ratio));
		realTimeLabel.setText(s1.toString());

		Label spinnerLabel0 = createLabelLeft(TR,
				"The current time-ratio is the ratio of simulation time to real time.");

		timeRatioSpinner = new Spinner<Integer>();
		// timeRatioSpinner.getStyleClass().clear();
		timeRatioSpinner.getStyleClass().add(getClass().getResource(SPINNER_CSS).toExternalForm());
		//timeRatioSpinner.setStyle("spinner");
		timeRatioSpinner.getStyleClass().add("spinner");
		timeRatioSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		// Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL
		// Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL
		// Spinner.STYLE_CLASS_ARROWS_ON_LEFT_HORIZONTAL
		// Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL
		// Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL

//		List<Integer> items = null;
//		if (default_ratio == 16)
//			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512);
//		else if (default_ratio == 32)
//			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512);// ,1024);
//		else if (default_ratio == 64)
//			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024);// , 2048);
//		else if (default_ratio == 128)
//			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048);// ,4096,8192);
//		else // if (default_ratio == 256)
//			items = FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096);// ,8192);
		
		List<Integer> items = null;
		if (defaultRatioInt <= 4)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128);//, 256);
		if (defaultRatioInt == 8)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256);//, 512);
		if (defaultRatioInt == 16)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512);//, 1024);
		else if (defaultRatioInt == 32)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024);//, 2048);
		else if (defaultRatioInt == 64)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048);//, 4096);
		else if (defaultRatioInt == 128)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096);//, 8192);
		else if (defaultRatioInt == 256)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192);//, 16384);
		else if (defaultRatioInt == 512)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384);//, 32768);
		else if (defaultRatioInt >= 1024)
			items = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768);//, 65536);


		timeRatioSpinner.setValueFactory(
				new SpinnerValueFactory.ListSpinnerValueFactory<Integer>(FXCollections.observableArrayList(items)));
		// spinner.setValueFactory((SpinnerValueFactory<Integer>) items);
		timeRatioSpinner.setMaxSize(100, 15);
		// timeRatioSpinner.setStyle(LABEL_CSS_STYLE);
		// spinner.setAlignment(Pos.CENTER);
		timeRatioSpinner.getValueFactory().setValue(defaultRatioInt);
		timeRatioSpinner.valueProperty().addListener((o, old_val, new_val) -> {

			if (old_val != new_val) {
				// newTimeRatio = value;
				int value = (int) new_val;
				// boolean previous = startPause();
				masterClock.setTimeRatio(value);
				// endPause(previous);

				StringBuilder s3 = new StringBuilder();
				s3.append(ClockUtils.getTimeTruncated(value));
				realTimeLabel.setText(s3.toString());

			}
		});

		Label tpsLabel0 = createLabelLeft(TPS, "The frequency of simulation ticks being sent.");
		tpsLabel = createLabelRight("e.g. 10 Hz means 10 ticks per second.");
		tpsLabel.setText(masterClock.getPulsesPerSecond() + HZ);

		Label upTimeLabel0 = createLabelLeft(UPTIME, "The time the simulation has been up running.");
		upTimeLabel = createLabelRight("e.g. 03m 05s means 3 minutes and 5 seconds.");
		if (uptimer != null)
			upTimeLabel.setText(uptimer.getUptime());

		Label refreshLabel0 = createLabelLeft(REFRESH, "The frequency the graphic is refreshed");
		refreshLabel = createLabelRight("e.g. 1 Hz means once per second.");


		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("jfx-popup-container; -fx-background-color:transparent;");
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5, 5, 5, 5));
		gridPane.setHgap(1.0);
		gridPane.setVgap(1.0);

		ColumnConstraints right = new ColumnConstraints();
		right.setPrefWidth(120);// earthTimeButton.getPrefWidth() * .6);
		ColumnConstraints left = new ColumnConstraints();
		left.setPrefWidth(80);// earthTimeButton.getPrefWidth() * .4);

		GridPane.setConstraints(timeRatioSpinner, 1, 0);
		GridPane.setConstraints(defaultRatioLabel, 1, 1);
		GridPane.setConstraints(realTimeLabel, 1, 2);
		GridPane.setConstraints(tpsLabel, 1, 3);
		GridPane.setConstraints(upTimeLabel, 1, 4);
		GridPane.setConstraints(refreshLabel, 1, 5);

		GridPane.setConstraints(spinnerLabel0, 0, 0);
		GridPane.setConstraints(defaultRatioLabel0, 0, 1);
		GridPane.setConstraints(realTimeLabel0, 0, 2);
		GridPane.setConstraints(tpsLabel0, 0, 3);
		GridPane.setConstraints(upTimeLabel0, 0, 4);
		GridPane.setConstraints(refreshLabel0, 0, 5);

		GridPane.setHalignment(timeRatioSpinner, HPos.CENTER);
		GridPane.setHalignment(defaultRatioLabel, HPos.CENTER);
		GridPane.setHalignment(realTimeLabel, HPos.CENTER);
		GridPane.setHalignment(tpsLabel, HPos.CENTER);
		GridPane.setHalignment(upTimeLabel, HPos.CENTER);
		GridPane.setHalignment(refreshLabel, HPos.CENTER);

		GridPane.setHalignment(spinnerLabel0, HPos.RIGHT);
		GridPane.setHalignment(defaultRatioLabel0, HPos.RIGHT);
		GridPane.setHalignment(realTimeLabel0, HPos.RIGHT);
		GridPane.setHalignment(tpsLabel0, HPos.RIGHT);
		GridPane.setHalignment(upTimeLabel0, HPos.RIGHT);
		GridPane.setHalignment(refreshLabel0, HPos.RIGHT);

		gridPane.getColumnConstraints().addAll(left, right);
		gridPane.getChildren().addAll(spinnerLabel0, timeRatioSpinner, defaultRatioLabel0, defaultRatioLabel,
				realTimeLabel0, realTimeLabel, tpsLabel0, tpsLabel, upTimeLabel0, upTimeLabel, refreshLabel0,
				refreshLabel);

		speedVBox = new VBox();
		speedVBox.getStyleClass().add(PANE_CSS);
		speedVBox.setPadding(new Insets(6, 2, 6, 2));
		speedVBox.setAlignment(Pos.CENTER);
		speedVBox.getChildren().addAll(header_label, gridPane); // timeSliderBox
		speedPane.getChildren().addAll(speedVBox);

	}

	public Label createLabelLeft(String name, String tip) {
		Label l = new Label(name);
		// upTimeLabel0.setEffect(blend);
		l.setAlignment(Pos.CENTER_RIGHT);
		l.setTextAlignment(TextAlignment.RIGHT);
		l.setStyle(LABEL_CSS_STYLE);
		l.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(l, tip);
		return l;
	}

	public Label createLabelRight(String tip) {
		Label l = new Label();
		l.setAlignment(Pos.CENTER);
		// upTimeLabel.setEffect(blend);
		l.setStyle(LABEL_CSS_STYLE);
		l.setPadding(new Insets(1, 1, 1, 2));
		if (uptimer != null)
			l.setText(uptimer.getUptime());
		setQuickToolTip(l, tip);
		return l;
	}

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
		soundPane.setBackground(
				new Background(new BackgroundFill(Color.rgb(54, 54, 54), CornerRadii.EMPTY, Insets.EMPTY)));
		// soundPane.setEffect(blend);
		soundPane.getStyleClass().add("jfx-popup-container; -fx-text-fill: white;");// -fx-background-radius: 10;");
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

		musicSlider.valueProperty().bindBidirectional(musicProperty);
		// detect dragging
		musicSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				if (old_val != new_val) {
					double newValue = new_val.doubleValue();
					double oldValue = old_val.doubleValue();				
					if (oldValue - newValue > 1 || newValue - oldValue > 1) {
						// Set to the new music volume in the sound player
						soundPlayer.setMusicVolume(convertSlider2Volume(newValue));
					}

					if (newValue <= 0) {
						// check the music mute box
						musicMuteBox.setSelected(true);
					} else if (oldValue == 0){
						// uncheck the music mute box
						musicMuteBox.setSelected(false);
					}
				}
			}
		});
		// Background sound track
		trackLabel = createBlendLabel("Music");
		// trackLabel.setPadding(new Insets(0, 0, 0, 0));

		musicMuteBox = new JFXCheckBox("mute");
		// musicMuteBox.setStyle("-fx-background-color: linear-gradient(to bottom,
		// -fx-base, derive(-fx-base,30%));"
		// + "-fx-font: bold 9pt 'Corbel';" + "-fx-text-fill: #654b00;");
		// cb.setPadding(new Insets(0,0,0,5));
		musicMuteBox.setAlignment(Pos.CENTER);

		musicMuteBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//				System.out.println("musicMuteBox oldValue : " + oldValue + "  newValue : " + newValue);
				if (oldValue != newValue) {					
					if (musicMuteBox.isSelected()) {
						muteControls(false, true);
					}
					else {
						unmuteControls(false, true);
					}
				}
			}
		});

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

		soundEffectSlider.valueProperty().bindBidirectional(soundEffectProperty);

		// detect dragging
		soundEffectSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {

				if (old_val != new_val) {
					double newValue = new_val.doubleValue();
					double oldValue = old_val.doubleValue();				
					if (oldValue - newValue > 1 || newValue - oldValue > 1) {
						// Set to the new music volume in the sound player
						soundPlayer.setSoundVolume(convertSlider2Volume(newValue));
					}
					if (newValue <= 0) {
						// check the sound effect mute box
						soundEffectMuteBox.setSelected(true);
					} else if (oldValue == 0) {
						// uncheck the sound effect mute box
						soundEffectMuteBox.setSelected(false);
					}
				}
			}
		});

		// Sound effect
		effectLabel = createBlendLabel("Sound Effect");
		// effectLabel.setPadding(new Insets(0, 0, 0, 1));

		soundEffectMuteBox = new JFXCheckBox("mute");
		// soundEffectMuteBox.setStyle("-fx-background-color: linear-gradient(to bottom,
		// -fx-base, derive(-fx-base,30%));"
		// + "-fx-font: bold 9pt 'Corbel';" + "-fx-text-fill: #654b00;");
		// cb.setPadding(new Insets(0,0,0,5));
		soundEffectMuteBox.setAlignment(Pos.CENTER);

		soundEffectMuteBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue) {
//					System.out.println("soundEffectMuteBox oldValue : " + oldValue + "  newValue : " + newValue);
					if (soundEffectMuteBox.isSelected()) {
						muteControls(true, false);
					}
					else {
						unmuteControls(true, false);
					}
				}
			}
		});

		GridPane gridPane0 = new GridPane();
		gridPane0.getStyleClass().add("jfx-popup-container ; -fx-background-color:transparent;");
		gridPane0.setAlignment(Pos.CENTER);
		gridPane0.setPadding(new Insets(1, 1, 1, 1));
		gridPane0.setHgap(1.0);
		gridPane0.setVgap(1.0);

		GridPane gridPane1 = new GridPane();
		gridPane1.getStyleClass().add("jfx-popup-container; -fx-background-color:transparent;");
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
		soundVBox.getStyleClass().add(PANE_CSS);
		soundVBox.setPadding(new Insets(5, 5, 5, 5));
		soundVBox.setAlignment(Pos.CENTER);
		soundVBox.getChildren().addAll(header_label, gridPane0, musicSlider, gridPane1, soundEffectSlider);
		soundPane.getChildren().addAll(soundVBox);

	}


//	 public void createFarmPopup() {
//		 //logger.config("MainScene's createFarmPopup() is on " +
//		 Thread.currentThread().getName());
//		  
//		 farmBtn = new JFXButton();
//		 //farmBtn.getStyleClass().add("menu-button");//"button-raised"); farmIcon =
//		 new IconNode(FontAwesome.LEAF); farmIcon.setIconSize(20);
//		 //soundIcon.setFill(Color.YELLOW); //soundIcon.setStroke(Color.WHITE);
//		 
//		 farmBtn.setMaxSize(20, 20); farmBtn.setGraphic(farmIcon);
//		 setQuickToolTip(farmBtn, "Click to open Farming Panel");
//		 
//		 farmBtn.setOnAction(e -> { 
//			 if (farmPopup.isShowing()) {
//			 farmPopup.hide();//close(); } else { farmPopup.show(farmBtn,
//			 PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35); 
//		 } });
//		 
//		 Accordion acc = new Accordion(); 
//		 ObservableList<Settlement> towns =
//		 sim.getUnitManager().getSettlementOList(); 
//		 int num = towns.size();
//		 List<TitledPane> titledPanes = new ArrayList<>(); List<Pane> panes = new
//		 ArrayList<>();
//		 
//		 for (Settlement s : towns) {
//		 
//		 DragDrop dd = new DragDrop(); StackPane p = dd.createDragDropBox();
//		 panes.add(p); TitledPane tp = new TitledPane(s.getName(), p);
//		 titledPanes.add(tp);
//		 
//		 p.getStyleClass().add("jfx-popup-container"); p.setAlignment(Pos.CENTER);
//		 p.setPrefHeight(75); p.setPrefWidth(250);
//		 
//		 acc.getPanes().add(tp); }
//		 
//		 farmPane = new StackPane(acc);
//		 
//		 farmPopup = new JFXPopup(farmPane);
//		 
//	 }


	public void createMarsTimeBar() {

		marsTimeLabel = new Label();
		marsTimeLabel.setId("mars-time-label");
		marsTimeLabel.setAlignment(Pos.CENTER);

		setQuickToolTip(marsTimeLabel, "Martian date/time stamp in Universal Martian Standard Time (UMST) at 0 W 0 N");

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
			marsTimeBox.setPrefSize(LINUX_WIDTH + 25, 29);
		} else if (OS.contains("mac")) {
			marsTimeLabel.setPrefSize(MACOS_WIDTH, 30);
			marsTimeBox.setPrefSize(MACOS_WIDTH + 20, 30);
		} else if (OS.contains("win")) {
			marsTimeLabel.setPrefSize(WIN_WIDTH, 35);
			marsTimeBox.setPrefSize(WIN_WIDTH + 20, 35);
		}

		setQuickToolTip(marsTimeButton, "Click to open Martian calendar");

		if (masterClock == null)
			masterClock = sim.getMasterClock();

		if (marsClock == null)
			marsClock = masterClock.getMarsClock();

		marsTimeButton.setOnAction(e -> {
			if (marsCalendarPopup.isShowing()) {
				marsCalendarPopup.hide();// close();
			} else {
				marsCalendarPopup.show(marsTimeButton, PopupVPosition.TOP, PopupHPosition.LEFT, 0, 38);
			}
			e.consume();
		});

		calendarDisplay = new MarsCalendarDisplay(marsClock, desktop);

		SwingNode calNode = new SwingNode();
		calNode.getStyleClass().add("jfx-popup-container; -fx-background-radius: 10;");
		calNode.setContent(calendarDisplay);

		StackPane calPane = new StackPane(calNode);
		calPane.getStyleClass().add("jfx-popup-container; -fx-background-radius: 10;");
		calPane.setAlignment(Pos.CENTER);
		calPane.setPrefHeight(100);
		calPane.setPrefWidth(180);

		VBox calBox = new VBox();
		calBox.setPadding(new Insets(0, 3, 3, 3));
		calBox.setAlignment(Pos.BOTTOM_CENTER);
		calBox.getChildren().addAll(calPane);
		setQuickToolTip(calBox, "The Martian Calendar. Each month contains four weeks with 27 or 28 sols");

		Text header_label = createBlackTextHeader("MARS CALENDAR PANEL");
		//header_label.setFill(Color.DARKSLATEGREY);

		monthText = createBlendText(MONTH + marsClock.getMonthName());
		yearText = createBlendText(ORBIT + marsClock.getOrbitString());

		setQuickToolTip(monthText, "the current Martian month. Each orbit has 24 months with either 27 or 28 sols");
		setQuickToolTip(yearText,
				"the current Martian orbit (or year). " + "Orbit 0000 coincides with Earth year 2028 CE");
		// The Martian year is referred to as an "orbit". Each orbit has 668.59 Martian
		// sols and
		// is 668.5921 Martian days long.
		HBox hBox = new HBox();
		hBox.setPadding(new Insets(10, 15, 2, 15));
		hBox.setAlignment(Pos.BOTTOM_CENTER);
		hBox.getChildren().addAll(yearText, monthText);

		Label LsLabel = new Label(SOLAR_LONGITUDE);
		setQuickToolTip(LsLabel, "The Areocentric Solar Longitude (Ls) is the Mars-Sun angle for determining the Martian season");

		orbitInfo = sim.getMars().getOrbitInfo();
		double L_s = orbitInfo.getL_s();
		LSText = createBlendText(df.format(Math.round(L_s * 1_000D) / 1_000D) + Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		setQuickToolTip(LSText,
				"Ls in degree. For Northern Hemisphere, " 
						+ "Spring Equinox -> Ls = 0 ; "
						+ "Summer Solstice -> Ls = 90 ; "
						+ "Autumn Equinox -> Ls = 180 ; "
						+ "Winter Solstice -> Ls = 270");

		Label radiusLabel = new Label("Mars-to-Sun : ");
		setQuickToolTip(radiusLabel, "The distance between Mars and Sun in A.U.");
		double radius = orbitInfo.getDistanceToSun();
		radiusText = createBlendText(Math.round(radius * 10_000D) / 10_000D + " A.U.");

		Label northLabel = new Label(NORTH);
		Label southLabel = new Label(SOUTH);
		setQuickToolTip(northLabel, "The season in the Northern hemisphere of Mars");
		setQuickToolTip(southLabel, "The season in the Southern hemisphere of Mars");
		
		northText = createBlendText(marsClock.getSeason(MarsClock.NORTHERN_HEMISPHERE));
		southText = createBlendText(marsClock.getSeason(MarsClock.SOUTHERN_HEMISPHERE));
		setQuickToolTip(northText, "Can be Early/Mid/Late Spring/Summer/Autumn/Winter.");
		setQuickToolTip(southText, "Can be Early/Mid/Late Spring/Summer/Autumn/Winter.");

		
//		HBox northBox = new HBox();
//		northBox.setPadding(new Insets(2, 2, 2, 2));
//		northBox.setAlignment(Pos.BOTTOM_CENTER);
//		northBox.getChildren().addAll(northLabel, northText);
//
//		HBox southBox = new HBox();
//		southBox.setPadding(new Insets(2, 2, 2, 2));
//		southBox.setAlignment(Pos.BOTTOM_CENTER);
//		southBox.getChildren().addAll(southLabel, southText);

		noteLabel = new Label();
		// noteText = new Text();
		// noteLabel.setEffect(blend);
		noteLabel.setPadding(new Insets(2, 5, 2, 0));
		noteLabel.setStyle("-fx-background-color: linear-gradient(to bottom, -fx-base, derive(-fx-base,30%));"
				+ "-fx-font-size: 12px;" + "-fx-text-fill: #654b00;");

		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("jfx-popup-container; -fx-background-color:transparent;");
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5, 5, 5, 5));
		gridPane.setHgap(1.0);
		gridPane.setVgap(1.0);

		ColumnConstraints right = new ColumnConstraints();
		right.setPrefWidth(80);// earthTimeButton.getPrefWidth() * .6);
		ColumnConstraints left = new ColumnConstraints();
		left.setPrefWidth(110);// earthTimeButton.getPrefWidth() * .4);

		GridPane.setConstraints(LSText, 1, 0);
		GridPane.setConstraints(radiusText, 1, 1);
		GridPane.setConstraints(northText, 1, 2);
		GridPane.setConstraints(southText, 1, 3);

		GridPane.setConstraints(LsLabel, 0, 0);
		GridPane.setConstraints(radiusLabel, 0, 1);
		GridPane.setConstraints(northLabel, 0, 2);
		GridPane.setConstraints(southLabel, 0, 3);

		GridPane.setHalignment(LSText, HPos.CENTER);
		GridPane.setHalignment(radiusText, HPos.CENTER);
		GridPane.setHalignment(northText, HPos.CENTER);
		GridPane.setHalignment(southText, HPos.CENTER);

		GridPane.setHalignment(LsLabel, HPos.RIGHT);
		GridPane.setHalignment(radiusLabel, HPos.RIGHT);
		GridPane.setHalignment(northLabel, HPos.RIGHT);
		GridPane.setHalignment(southLabel, HPos.RIGHT);

		gridPane.getColumnConstraints().addAll(left, right);
		gridPane.getChildren().addAll(LsLabel, LSText, radiusLabel, radiusText, northLabel, northText, southLabel, southText);
		
		VBox vBox = new VBox();
		vBox.setPadding(new Insets(1, 5, 1, 5));
		vBox.setAlignment(Pos.CENTER);
		vBox.getChildren().addAll(header_label, hBox, calBox, gridPane, noteLabel);

		calendarPane = new StackPane(vBox);
		calendarPane.getStyleClass().add("jfx-popup-container");
		calendarPane.setAlignment(Pos.CENTER);
		calendarPane.setPrefHeight(170);
		calendarPane.setPrefWidth(180);
		calendarPane.setPadding(new Insets(5, 5, 5, 5));

		marsCalendarPopup = new JFXPopup(calendarPane);
		setGlow(calendarPane);
	}

	public void createMapToolBox() {		
		// Set up the map tool anchor pane
		mapToolAnchorPane = new AnchorPane();
		mapToolAnchorPane.setId("map-tool-anchor-pane");
		//mapToolAnchorPane.setStyle("-fx-background-color: lightgrey; ");
		// see https://introjava.wordpress.com/2012/03/23/java-fx-2-linear-gradients/
//		mapToolAnchorPane.setStyle(
////				"-fx-background-radius: 15px;"
////				+ "-fx-background-color:  linear-gradient(lightgrey, darkgrey);"
//				//+ "-fx-background-color: darkgrey;"
//					"-fx-background-color:"
//				+ "linear-gradient(#686868 0%, #232723 25%, #373837 75%, #757575 100%),"
//				+ "    linear-gradient(#020b02, #3a3a3a),"
//			    + "    linear-gradient(#9d9e9d 0%, #6b6a6b 20%, #343534 80%, #242424 100%),"
//			    + "    linear-gradient(#8a8a8a 0%, #6b6a6b 20%, #343534 80%, #262626 100%),"
//			    + "    linear-gradient(#777777 0%, #606060 50%, #505250 51%, #2a2b2a 100%);"
//			    + "-fx-background-insets: 0,1,4,5,6;"
//			    + "-fx-background-radius: 9,8,5,4,3;"
//			    + "-fx-padding: 1 1 1 1;"
////			    + "-fx-font-family: 'Helvetica';"
//			    + "-fx-font-size: 12px;"
////			    + "-fx-font-weight: bold;"
//			    + "-fx-opacity: .75;"
//			    + "-fx-text-fill: white;"
//			    + "-fx-effect: dropshadow( three-pass-box , rgba(255,255,255,0.2),1 ,0.0 ,0 ,1);"
//				);
//		mapToolAnchorPane.setOpacity(.75);
		mapToolAnchorPane.setPrefHeight(450);
		mapToolAnchorPane.setPrefWidth(140);
		
		sMapStackPane.getChildren().add(mapToolAnchorPane);
		
		AnchorPane.setTopAnchor(mapToolAnchorPane, 110.0);
		AnchorPane.setRightAnchor(mapToolAnchorPane, 20.0);
		
		createMapButtons();
		createMapCacheToggles();
		createFXSettlementComboBox();
		createFXZoomSlider();
		createFXMapLabelBox();

		anchorAllMapWidgets();
		
		// detect mouse wheel scrolling
		sMapStackPane.setOnScroll(new EventHandler<ScrollEvent>() {
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
	}

	public void createMapCacheToggles() {
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

		sMapToggle = new JFXToggleButton();
		sMapToggle.setText("Settlement Map Off");
		sMapToggle.setSelected(false);
		setQuickToolTip(sMapToggle, "Pin Settlement Map");
		sMapToggle.setOnAction(e -> {
			if (sMapToggle.isSelected()) {
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
					minimapGroup.toFront();

				minimapToggle.toFront();
				sMapToggle.toFront();
			} else
				cacheToggle.setText("Map Cache Off");

			e.consume();
		});

	}

	public void createMapButtons() {
		rotateCWBtn = new JFXButton();
		rotateCWBtn.setOpacity(1);
		rotateCWBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.cw"))))); //$NON-NLS-1$
		rotateCWBtn.setStyle("-fx-background-color: transparent; ");
		setQuickToolTip(rotateCWBtn, Msg.getString("SettlementTransparentPanel.tooltip.clockwise"));
		rotateCWBtn.setOnAction(e -> {
			mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE);
			e.consume();
		});

		rotateCCWBtn = new JFXButton();
		rotateCCWBtn.setOpacity(1);
		rotateCCWBtn
				.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.ccw"))))); //$NON-NLS-1$
		rotateCCWBtn.setStyle("-fx-background-color: transparent; ");
		setQuickToolTip(rotateCCWBtn, Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise"));
		rotateCCWBtn.setOnAction(e -> {
			mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE);
			e.consume();
		});

		recenterBtn = new JFXButton();
		recenterBtn.setOpacity(1);
		recenterBtn.setGraphic(
				new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.recenter"))))); //$NON-NLS-1$
		recenterBtn.setStyle("-fx-background-color: transparent; ");
		setQuickToolTip(recenterBtn, Msg.getString("SettlementTransparentPanel.tooltip.recenter"));
		recenterBtn.setOnAction(e -> {
			mapPanel.reCenter();
			zoomSlider.setValue(DEFAULT_ZOOM);
			e.consume();
		});

	}

	public void createFXZoomSlider() {
		// Set up a settlement view zoom bar
		zoomSlider = new JFXSlider();
		zoomSlider.setOpacity(1);
		zoomSlider.getStyleClass().add("jfx-slider");
		zoomSlider.setMin(0);
		zoomSlider.setMax(40);
		zoomSlider.setValue(DEFAULT_ZOOM);
		zoomSlider.setMajorTickUnit(10);
		//zoomSlider.setMinorTickCount(5);
		zoomSlider.setShowTickLabels(true);
		zoomSlider.setShowTickMarks(true);
		zoomSlider.setSnapToTicks(false);
		zoomSlider.setBlockIncrement(5);
		zoomSlider.setOrientation(Orientation.VERTICAL);
		zoomSlider.setIndicatorPosition(IndicatorPosition.RIGHT);

		setQuickToolTip(zoomSlider, Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		// detect dragging on zoom scroll bar
		zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				if (old_val != new_val) {
					// Change scale of map based on slider position.
					double sliderValue = new_val.doubleValue();
					// double d = SettlementMapPanel.DEFAULT_SCALE;
					double newScale = 0;
					if (sliderValue > 0) {
						newScale = sliderValue;// * SettlementTransparentPanel.ZOOM_CHANGE;
						mapPanel.setScale(newScale);
					}
					else {
						zoomSlider.setValue(1);
					}
				}
			}
		});
	}

	public void createFXSettlementComboBox() {
		sBox = new JFXComboBox<>();
		sBox.getStyleClass().add("jfx-combo-box");
		setQuickToolTip(sBox, Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		sBox.itemsProperty().setValue(FXCollections.observableArrayList(sim.getUnitManager().getSettlements()));
		sBox.setPromptText("Select a settlement to view");
		sBox.getSelectionModel().selectFirst();

		sBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != newValue) {
				SwingUtilities.invokeLater(() -> mapPanel.setSettlement((Settlement) newValue));
			}
		});

		settlementBox = new StackPane(sBox);
		settlementBox.setOpacity(1);
		settlementBox.setMaxSize(180, 30);
		settlementBox.setPrefSize(180, 30);
		settlementBox.setAlignment(Pos.CENTER_RIGHT);

	}

	public void changeSBox() {
		sBox.itemsProperty().setValue(FXCollections.observableArrayList(sim.getUnitManager().getSettlements()));
	}

	public void createFXMapLabelBox() {

		mapLabelBox = new VBox();
//		mapLabelBox.setOpacity(1);
		mapLabelBox.setSpacing(5);
		mapLabelBox.setMaxSize(180, 150);
		mapLabelBox.setPrefSize(180, 150);

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

	public void setGlow(Node node) {
		int depth = 70; //Setting the uniform variable for the glow width and height	 
		borderGlow = new DropShadow();
		borderGlow.setOffsetY(0f);
		borderGlow.setOffsetX(0f);
		if (theme == 7)
			borderGlow.setColor(Color.ORANGE);
		else
			borderGlow.setColor(Color.BLUE);		
		borderGlow.setWidth(depth);
		borderGlow.setHeight(depth); 
		node.setEffect(borderGlow);
	}
	
	
	/**
	 * Creates the tab pane for housing a bunch of tabs
	 */
	public void createJFXTabs() {
		tabPane = new JFXTabPane();

		// Set up the "Dashboard" Tab
		dashboardStackPane = new StackPane();

		dashboardTab = new Tab();
		dashboardTab.setText("Dashboard");
		dashboardTab.setContent(dashboardStackPane);

		Parent parent = null;
		DashboardController controller = null;
		FXMLLoader fxmlLoader = null;

		try {
			fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("/fxui/fxml/dashboard/dashboard.fxml")); // //
			parent = (Parent) fxmlLoader.load();
			controller = (DashboardController) fxmlLoader.getController();
		} catch (IOException e) {
			e.printStackTrace();
		}

		controller.setSize(screen_width-120, screen_height);// - TAB_PANEL_HEIGHT);

		dashboardStackPane.getChildren().add(parent);

		// Set up the "Main" Tab
		createJFXToolbar();

		toolbarBox = new VBox(toolbar, desktopNode);
		toolbarBox.setPadding(new Insets(5, 5, 5, 5));
		mainStackPane = new StackPane();
		// mainStackPane.setStyle("-fx-background-color: transparent; ");
		mainStackPane.getChildren().add(toolbarBox);

		mainTab = new Tab();
		mainTab.setText("Main");
		mainTab.setContent(mainStackPane);

		// Set up the map anchor pane
		mapsAnchorPane = new AnchorPane();
		// mapsAnchorPane.setStyle("-fx-background-color: transparent; ");
		mapsAnchorPane.setStyle("-fx-background-color: black; ");
			
		
		Tab mapTab = new Tab();
		mapTab.setText("Map");
		// mapTab.setStyle("-fx-background-color: black; ");
		mapTab.setContent(mapsAnchorPane);

		// Set up Navigator Window (Minimap)
		navWin = (NavigatorWindow) desktop.getToolWindow(NavigatorWindow.NAME);

		minimapNode = new SwingNode();
		minimapNode.setStyle("-fx-background-color: transparent; -fx-border-color: black; ");	
		minimapGroup = new Group(minimapNode);
		minimapNode.setContent(navWin);
				
		// Set up Settlement Window (Settlement Map)
		settlementWindow = (SettlementWindow) desktop.getToolWindow(SettlementWindow.NAME);
		mapPanel = settlementWindow.getMapPanel();
	
		mapNode = new SwingNode();
		mapNode.setStyle("-fx-background-color: transparent; -fx-border-color: black;");	
		mapNode.setContent(settlementWindow);
		
		sMapStackPane = new StackPane(mapNode);
		sMapStackPane.setStyle("-fx-background-color: transparent; -fx-border-color: black;");
//		setGlow(mapNode);

		// Set up the "Help" Tab
		BrowserJFX helpBrowser = desktop.getBrowserJFX();
		StackPane guidePane = new StackPane(helpBrowser.getBorderPane());
		Tab guideTab = new Tab();
		guideTab.setText("Help");
		guideTab.setContent(guidePane);

		// Set up the tabPane
		tabPane.getTabs().addAll(dashboardTab, mainTab, mapTab, guideTab);

		final DashboardController c = controller;

		tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {

			if (newTab == dashboardTab) {
				c.checkSettlements();
				dashboardStackPane.requestFocus();
				closeMaps();
			}

			else if (newTab == mainTab) {
				mainStackPane.requestFocus();
				closeMaps();
			}

			else if (newTab == mapTab) {
				anchorToggles();
				desktop.closeToolWindow(GuideWindow.NAME);
			}

			else if (newTab == guideTab) {
				// if (!desktop.isToolWindowOpen(GuideWindow.NAME))
				desktop.openToolWindow(GuideWindow.NAME);
				closeMaps();
			}

			else {
				mapsAnchorPane.getChildren().removeAll(cacheToggle, minimapToggle, sMapToggle);
			}

		});

		// NOTE: if a tab is NOT selected, should close that tool as well to save cpu
		// utilization
		// this is done in ToolWindow's update(). It allows for up to 1 second of delay,
		// in case user open and close the same repeated.

	}

	/**
	 * Anchors all the map widgets
	 */
	public void anchorAllMapWidgets() {

		AnchorPane.setRightAnchor(zoomSlider, 70.0);
		AnchorPane.setTopAnchor(zoomSlider, 270.0);//350.0);

		AnchorPane.setRightAnchor(rotateCWBtn, 110.0);
		AnchorPane.setTopAnchor(rotateCWBtn, 220.0);//300.0);

		AnchorPane.setRightAnchor(rotateCCWBtn, 30.0);
		AnchorPane.setTopAnchor(rotateCCWBtn, 220.0);//300.0);

		AnchorPane.setRightAnchor(recenterBtn, 70.0);
		AnchorPane.setTopAnchor(recenterBtn, 220.0);//300.0);

		AnchorPane.setRightAnchor(mapLabelBox, -10.0);
		AnchorPane.setTopAnchor(mapLabelBox, 60.0);//140.0);
		
		AnchorPane.setRightAnchor(settlementBox, 15.0);
		AnchorPane.setTopAnchor(settlementBox, 20.0);//100.0);

		mapToolAnchorPane.getChildren().addAll(settlementBox, mapLabelBox, recenterBtn, rotateCCWBtn, rotateCWBtn, zoomSlider);

	}

	public void anchorToggles() {

		AnchorPane.setRightAnchor(cacheToggle, 25.0);
		AnchorPane.setTopAnchor(cacheToggle, 55.0); // 55.0

		AnchorPane.setLeftAnchor(minimapToggle, 10.0);
		AnchorPane.setTopAnchor(minimapToggle, 17.0); // 55.0

		AnchorPane.setRightAnchor(sMapToggle, 15.0);
		AnchorPane.setTopAnchor(sMapToggle, 17.0); // 55.0

		mapsAnchorPane.getChildren().addAll(cacheToggle, minimapToggle, sMapToggle);

	}

	/**
	 * Opens the Minimap / Mars Navigator
	 */
	public void openMinimap() {
		desktop.openToolWindow(NavigatorWindow.NAME);

		navWin.getGlobeDisplay().drawSphere();// updateDisplay();

		AnchorPane.setLeftAnchor(minimapGroup, 3.0);
		AnchorPane.setTopAnchor(minimapGroup, 0.0); // 45.0
		boolean flag = false;
		for (Node node : mapsAnchorPane.getChildrenUnmodifiable()) {
			if (node == minimapGroup) {
				flag = true;
				break;
			}
		}

		if (!flag)
			mapsAnchorPane.getChildren().addAll(minimapGroup);

		minimapGroup.toFront();
		
		navWin.showSurfaceMap();
		
		navWin.toFront();
		navWin.requestFocus();	

		minimapToggle.setSelected(true);
		minimapToggle.setText("Minimap On");
		minimapToggle.toFront();

	}

	/**
	 * Opens the settlement map
	 */
	public void openSettlementMap() {

		sMapStackPane.prefWidthProperty().unbind();
		sMapStackPane.prefWidthProperty().bind(rootStackPane.widthProperty().subtract(1));

		desktop.openToolWindow(SettlementWindow.NAME);

		AnchorPane.setRightAnchor(sMapStackPane, 0.0);
		AnchorPane.setTopAnchor(sMapStackPane, 0.0);

		ObservableList<Node> nodes = mapsAnchorPane.getChildrenUnmodifiable();
			
		boolean hasTool = false;
		boolean hasMap = false;
		
		for (Node node : nodes) {

			if (node == mapToolAnchorPane) {
				hasTool = true;
			} else if (node == sMapStackPane) {
				hasMap = true;
			}

		}
		
		if (!hasTool) {
			mapsAnchorPane.getChildren().addAll(mapToolAnchorPane);
		}
		if (!hasMap) {
			mapsAnchorPane.getChildren().addAll(sMapStackPane);	
		}
		
		mapToolAnchorPane.toFront();

		sMapToggle.toFront();
		cacheToggle.toFront();
		minimapToggle.toFront();

		sMapToggle.setText("Settlement Map On");
		sMapToggle.setSelected(true);

	}

	/**
	 * Checks if the settlement map is on
	 * 
	 * @return
	 */
	public boolean isSettlementMapOn() {
		return sMapToggle.isSelected();
	}

	/**
	 * Checks if minimap is on
	 * 
	 * @return
	 */
	public boolean isMinimapOn() {
		return minimapToggle.isSelected();
	}

	/**
	 * Closes the minimap
	 */
	public void closeMinimap() {
		desktop.closeToolWindow(NavigatorWindow.NAME);
		Platform.runLater(() -> {
			mapsAnchorPane.getChildren().remove(minimapGroup);
			minimapToggle.setSelected(false);
			minimapToggle.setText("Minimap Off");
			tabPane.requestFocus();
		});
	}

	public void closeSettlementMap() {
		desktop.closeToolWindow(SettlementWindow.NAME);
		Platform.runLater(() -> {
			mapsAnchorPane.getChildren().remove(sMapStackPane);
			sMapToggle.setSelected(false);
			sMapToggle.setText("Settlement Map Off");
			tabPane.requestFocus();
		});
	}

	public void closeMaps() {
		mapsAnchorPane.getChildren().removeAll(cacheToggle, minimapToggle, sMapToggle);
		if (!isCacheButtonOn()) {
			desktop.closeToolWindow(SettlementWindow.NAME);
			desktop.closeToolWindow(NavigatorWindow.NAME);
			Platform.runLater(() -> {
				mapsAnchorPane.getChildren().removeAll(sMapStackPane, minimapGroup);
				minimapToggle.setSelected(false);
				minimapToggle.setText("Minimap Off");
				sMapToggle.setSelected(false);
				sMapToggle.setText("Settlement Map Off");
			});
		}
		tabPane.requestFocus();
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
		// logger.config("MainScene's initializeTheme()");
		setTheme(defaultThemeColor);
	}

	/*
	 * Sets the theme skin of the desktop
	 */
	public void setTheme(int theme) {

		String cssFile = THEME_PATH;

		if (menuBar != null && menuBar.getStylesheets() != null)
			menuBar.getStylesheets().clear();

		if (this.theme != theme) {
			this.theme = theme;

			if (theme == 0) { // snow blue
				// for numbus theme
				cssFile += "snowBlue.css";
				updateThemeColor(0, Color.rgb(0, 107, 184), Color.rgb(0, 107, 184), cssFile); // CADETBLUE // //
																								// Color.rgb(23,138,255)
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

			SwingUtilities.invokeLater(() -> setLookAndFeel(defaultThemeType));

		}

	}

	/**
	 * Sets the look and feel of the UI
	 * 
	 * @param choice
	 */
	public void setLookAndFeel(ThemeType choice) {
		// logger.config("MainScene's setLookAndFeel() is on " +
		// Thread.currentThread().getName() + " Thread");
		boolean changed = false;
		if (choice == ThemeType.Weblaf) {
			try {
				// use the weblaf skin
//				WebLookAndFeel.setForceSingleEventsThread ( true );
				WebLookAndFeel.install();
				UIManagers.initialize();
				changed = true;

			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		}

		else if (choice == ThemeType.System) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		}

		else if (choice == ThemeType.Nimrod) {
			try {
				NimRODTheme nt = new NimRODTheme(
						getClass().getClassLoader().getResource("theme/" + themeSkin + ".theme")); //
				NimRODLookAndFeel.setCurrentTheme(nt); // must be declared non-static or not
				// working if switching to a brand new .theme file
				NimRODLookAndFeel nf = new NimRODLookAndFeel();
				nf.setCurrentTheme(nt); // must be declared non-static or not working if switching to a brand new .theme
										// // file
				UIManager.setLookAndFeel(nf);
				changed = true; //

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

					changed = true;
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
	public void updateThemeColor(int theme, Color txtColor, Color txtColor2, String cssFile) {
		mainStackPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		if (!OS.contains("mac"))
			menuBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

		mapToolAnchorPane.getStyleClass().clear();
		mapToolAnchorPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		mapToolAnchorPane.setId("map-tool-anchor-pane");
		
		// Note : menu bar color
		// orange theme : F4BA00
		// blue theme : 3291D2

		// String color = txtColor.replace("0x", "");

		tabPane.getStylesheets().clear();
//		setStylesheet(tabPane, cssFile);

		marsTimeIcon.getStyleClass().clear();
		marsTimeIcon.getStyleClass().add(getClass().getResource(cssFile).toExternalForm());

		setStylesheet(lastSaveLabel, cssFile);
		setStylesheet(cacheToggle, cssFile);
		setStylesheet(minimapToggle, cssFile);
		setStylesheet(sMapToggle, cssFile);

		setStylesheet(sBox, cssFile);
		//setStylesheet(mapLabelBox, cssFile);

		for (Node node : mapLabelBox.getChildren()) {
			if (node instanceof JFXCheckBox) {
				((JFXCheckBox)node).getStylesheets().clear();
				((JFXCheckBox)node).getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
				((JFXCheckBox)node).getStyleClass().add("jfx-check-box");
			}
		}
				
		setStylesheet(speedPane, cssFile);
		setStylesheet(speedVBox, cssFile);
		setStylesheet(calendarPane, cssFile);
		setStylesheet(soundPane, cssFile);
		setStylesheet(soundVBox, cssFile);

		// setStylesheet(timeSlider, cssFile);

		datePickerFX.getStylesheets().clear();
		datePickerFX.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

//		if (cssFile.toLowerCase().contains("blue"))
//			datePickerFX.setDefaultColor(Color.valueOf("#065185"));
//		else
//			datePickerFX.setDefaultColor(Color.valueOf("#654b00"));

		timePickerFX.getStylesheets().clear();
		timePickerFX.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

		// setStylesheet(marsTimeBox, cssFile);
		setStylesheet(toolbarBox, cssFile);
		setStylesheet(marsTimeButton, cssFile);
		setStylesheet(marsTimeLabel, cssFile);
	
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
			speedIcon.setFill(Color.WHITE);
			speedBtn.setGraphic(speedIcon);
			marsNetIcon.setFill(Color.WHITE);
			marsNetBtn.setGraphic(marsNetIcon);
			soundIcon.setFill(Color.WHITE);
			soundBtn.setGraphic(soundIcon);
			tabPane.getStylesheets().add(getClass().getResource(JFX_ORANGE_CSS).toExternalForm());
			tabPane.getStyleClass().add("jfx-tab-pane");
			toolbar.getStylesheets().add(getClass().getResource(JFX_ORANGE_CSS).toExternalForm());
			toolbar.getStyleClass().add("jfx-tool-bar");
			
			setStylesheet(zoomSlider, JFX_ORANGE_CSS);
			setStylesheet(musicSlider, JFX_ORANGE_CSS);
			setStylesheet(soundEffectSlider, JFX_ORANGE_CSS);
			setStylesheet(toolbar, JFX_ORANGE_CSS);
		
			borderGlow.setColor(Color.ORANGE);
			setGlow(calendarPane);
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
			tabPane.getStylesheets().add(getClass().getResource(JFX_BLUE_CSS).toExternalForm());
			tabPane.getStyleClass().add("jfx-tab-pane");
			toolbar.getStylesheets().add(getClass().getResource(JFX_BLUE_CSS).toExternalForm());
			toolbar.getStyleClass().add("jfx-tool-bar");
			
			setStylesheet(zoomSlider, JFX_BLUE_CSS);
			setStylesheet(musicSlider, JFX_BLUE_CSS);
			setStylesheet(soundEffectSlider, JFX_BLUE_CSS);
			setStylesheet(toolbar, JFX_BLUE_CSS);
			
			borderGlow.setColor(Color.BLUE);
			setGlow(calendarPane);
		}

		chatBox.update();

	}

	public void setStylesheet(JFXComboBox<Settlement> c, String cssFile) {
		c.getStylesheets().clear();
		c.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		c.getStyleClass().add("jfx-combo-box");
	}
	
	public void setStylesheet(JFXTabPane t, String cssFile) {
		t.getStylesheets().clear();
		t.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		t.getStyleClass().add("jfx-tab-pane");
	}

	public void setStylesheet(Text t, String cssFile) {
		// t.getStylesheets().clear();
		t.setStyle(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(JFXToolbar t, String cssFile) {
		t.getStylesheets().clear();
		t.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		t.getStyleClass().add("jfx-toolbar");
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

	public void setStylesheet(HBox b, String cssFile) {
		b.getStylesheets().clear();
		b.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
	}

	public void setStylesheet(VBox vb, String cssFile) {
		vb.getStylesheets().clear();
		vb.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		vb.getStyleClass().add("vbox");
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
		timeLabeltimer = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), ae -> updateTimeLabels()));
		// Note: Infinite Timeline might result in a memory leak if not stopped
		// properly.
		// All the objects with animated properties would not be garbage collected.
		timeLabeltimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeLabeltimer.play();

	}

//	public void startCountdownTimer() {
//		countdownTimer = new Timeline(
//				new KeyFrame(Duration.seconds(60 * autosave_minute),
//						ae -> masterClock.setAutosave(true)));
//	}
	
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

	/**
	 * Open the chat box
	 */
	public void openChatBox() {
		try {
			TimeUnit.MILLISECONDS.sleep(200L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		chatBox.getAutoFillTextBox().getTextbox().clear();
		chatBox.getAutoFillTextBox().getTextbox().requestFocus();
		marsNetBox.show(marsNetBtn, PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
		
		chatBox.checkConnection();
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

		// Check if the new has been on display for over one sol or not
		checkBillboardTimer();
		
//		double tr = masterClock.getTimeRatio();
		// if (msol % 10 == 0) {
		// Check to see if a background sound track is being played.
		if (!soundPlayer.isSoundDisabled() && !soundPlayer.isMusicMute() 
				&& !MainMenu.isSoundDisabled() && musicSlider.getValue() > 0) {
				soundPlayer.resumeMusic();//playRandomMusicTrack();
		}

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

			double refresh = 1.0/masterClock.getPulseTime();
			if (refresh > 0 && refresh < 30) {
				double rate = Math.round(50*(refreshCache + refresh))/100.0;
				refreshLabel.setText(rate + HZ);
				refreshCache = rate;
			}
//			else {
//				rate = refreshCache;
//				refreshLabel.setText(rate + HZ);
//			}
			
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
			LSText.setText(df.format(Math.round(L_s * 1_000D) / 1_000D) + Msg.getString("direction.degreeSign")); //$NON-NLS-1$

			double radius = orbitInfo.getDistanceToSun();
			radiusText.setText(Math.round(radius * 10_000D) / 10_000D + " A.U.");
			
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
		m.append(MARS_DATE_TIME).append(marsClock.getDateString()).append(COLON)
				.append(marsClock.getTrucatedTimeStringUMST());
		marsTimeLabel.setText(m.toString());

		// StringBuilder e = new StringBuilder();
		// e.append(EARTH_DATE_TIME).append(earthClock.getLT());//getTimeStampF0());
		// earthTime.setText(e.toString());

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
	 * Performs the process of saving a simulation.
	 */
	private void saveSimulationProcess(int type) {
		// logger.config("MainScene's saveSimulationProcess() is on " +
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

	/**
	 * Checks if a news on the billboard timer has been on display for over one sol 
	 */
	public void checkBillboardTimer() {
		if (billboardTimer != null
			&& lastNewsClock != null
			&& marsClock.getMissionSol() > lastNewsClock.getMissionSol()
			&& marsClock.getMillisol() > lastNewsClock.getMillisol()
		) {
			matrix.clear();
			billboardTimer.stop();
			billboardTimer = null;
			lastNewsClock = null;
		}
	}
	
	/**
	 * Opens the pause popup
	 */
	public void startPausePopup() {
		if (gameScene == null) {
			// if (messagePopup.numPopups() < 1) {
			// Note: (NOT WORKING) popups.size() is always zero no matter what.
			Platform.runLater(() -> {
				if (billboardTimer != null)
					billboardTimer.stop();
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
					pausePane.setLayoutX((rootStackPane.getWidth() - pausePane.getPrefWidth()) / 2D);
					pausePane.setLayoutY((rootStackPane.getHeight() - pausePane.getPrefHeight()) / 2D);
					// root.getChildrenUnmodifiable().add(pausePane);
					rootStackPane.getChildren().add(pausePane);
				}
			});
		}
	}

	/**
	 * Closes the pause popup
	 */
	public void stopPausePopup() {
		if (gameScene == null) {
			Platform.runLater(() -> {
				// messagePopup.stop()
				if (billboardTimer != null)
					billboardTimer.start();
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
			masterClock.setPaused(true, showPane);
		}
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
				masterClock.setPaused(false, true);
			}
		} else {
			if (!now) {
				masterClock.setPaused(false, true);
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
		Simulation.instance().getSimExecutor().shutdown();//.shutdownNow();
//		mainSceneExecutor.shutdownNow();
		getDesktop().clearDesktop();
		timeLabeltimer.stop();
		if (billboardTimer != null)
			billboardTimer.stop();
		stage.close();
	}

	/**
	 * Exits the current simulation and the main menu.
	 */
	public void exitSimulation() {
		logger.config("Exiting the simulation. Bye!");
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

	/**
	 * Create the desktop swing node
	 */
	private void createDesktopNode() {
		// Create group to hold swingNode which in turns holds the Swing desktop
		desktopNode = new SwingNode();
		// desktopNode.setStyle("-fx-background-color: black; ");
		desktop = new MainDesktopPane(this);
		// SwingUtilities.invokeLater(() -> desktopNode.setContent(desktop));

		// Add main pane
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setSize(screen_width, screen_height);
		mainPane.add(desktop, BorderLayout.CENTER);		
		SwingUtilities.invokeLater(() -> desktopNode.setContent(mainPane));
//		SwingUtilities.invokeLater(() -> desktopNode.setContent(desktop));
//		desktopNode.requestFocus();
	}

	public void desktopFocus() {
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

				JFXButton b1 = new JFXButton("Save & Exit");
				JFXButton b2 = new JFXButton("Exit");	
				JFXButton b3 = new JFXButton("Back");
				StackPane sp = MainMenu.getExitDialogPane(b1, b2, b3);		
				
//				Label l = createBlendLabel(Msg.getString("MainScene.exit.header"));
//				l.setPadding(new Insets(10, 10, 10, 10));
//				l.setFont(Font.font(null, FontWeight.BOLD, 14));
//				l.setStyle("-fx-text-fill: white;");
//
//				HBox hb = new HBox();
//				hb.setAlignment(Pos.CENTER);
//				JFXButton b0 = new JFXButton("Save & Exit");
//				b0.setStyle("-fx-background-color: grey;-fx-text-fill: white;");
//				JFXButton b1 = new JFXButton("Exit");
//				b1.setStyle("-fx-background-color: grey;-fx-text-fill: white;");
//				JFXButton b2 = new JFXButton("Back");
//				b2.setStyle("-fx-background-color: grey;-fx-text-fill: white;");
//				// b0.setPadding(new Insets(2, 2, 2, 2));
//
//				hb.getChildren().addAll(b0, b1, b2);
//				HBox.setMargin(b0, new Insets(3, 3, 3, 3));
//				HBox.setMargin(b1, new Insets(3, 3, 3, 3));
//				HBox.setMargin(b2, new Insets(3, 3, 3, 3));
//
//				VBox vb = new VBox();
//				vb.setPadding(new Insets(5, 5, 5, 5));
//				vb.getChildren().addAll(l, hb);
//				vb.setAlignment(Pos.CENTER);
//
//				StackPane sp = new StackPane(vb);
//				sp.setStyle("-fx-background-color: black;");
//				StackPane.setMargin(vb, new Insets(10, 10, 10, 10));

				exitDialog = new JFXDialog();
				exitDialog.setTransitionType(DialogTransition.TOP);
				exitDialog.setDialogContainer(rootStackPane);
				exitDialog.setContent(sp);
				exitDialog.show();

				b1.setOnAction(e -> {
					exitDialog.close();
					saveOnExit();
					e.consume();
				});

				b2.setOnAction(e -> {
					exitDialog.close();
					endSim();
					exitSimulation();
					Platform.exit();
					System.exit(0);
					e.consume();
				});

				b3.setOnAction(e -> {
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

		if (isFXGL) {
			Input input = FXGL.getInput();
			input.addInputMapping(new InputMapping("Open", KeyCode.O));
		}
	}

	@OnUserAction(name = "Open", type = ActionType.ON_ACTION_BEGIN)
	public void openWindow() {
		// 1. create in-game window
		InGameWindow window = new InGameWindow("FXGL Window");

		// 2. set properties
		window.setPrefSize(300, 200);
		window.setPosition(400, 300);
		window.setBackgroundColor(Color.BLACK);

		gameScene.addUINode(window);
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
		return stageAnchorPane;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	private MenuItem registerAction(MenuItem menuItem) {
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

		if (type == AUTOSAVING) {
			// ProgressIndicator indicator = new ProgressIndicator();
			MaskerPane asMPane = new MaskerPane();
			asMPane.setText("Autosaving");
			asMPane.setSkin(null);
			// indicator.setOpacity(.5);
			asMPane.setStyle("-fx-background-color: transparent; ");
			// indicator.setScaleX(1.5);
			// indicator.setScaleY(1.5);
			asPane = new StackPane();
			// stackPane.setOpacity(0.5);
			asPane.getChildren().add(asMPane);
			StackPane.setAlignment(asMPane, Pos.CENTER);
			asPane.setBackground(Background.EMPTY);
			asPane.setStyle(// "-fx-border-style: none; "
					"-fx-background-color: transparent; "
			// + "-fx-background-radius: 3px;"
			);
		}

		else if (type == SAVING) {
			// ProgressIndicator indicator = new ProgressIndicator();
			MaskerPane sMPane= new MaskerPane();
			sMPane.setText("Saving");
			sMPane.setSkin(null);
			// indicator.setOpacity(.5);
			sMPane.setStyle("-fx-background-color: transparent; ");
			// indicator.setScaleX(1.5);
			// indicator.setScaleY(1.5);
			sPane = new StackPane();
			// stackPane.setOpacity(0.5);
			sPane.getChildren().add(sMPane);
			StackPane.setAlignment(sMPane, Pos.CENTER);
			sPane.setBackground(Background.EMPTY);
			sPane.setStyle(// "-fx-border-style: none; "
					"-fx-background-color: transparent; "
			// + "-fx-background-radius: 3px;"
			);

		}

		if (savingStage == null) {
			savingStage = new Stage();
			savingStage.initOwner(stage);
			savingStage.initModality(Modality.WINDOW_MODAL); // Modality.NONE is by default if initModality() is NOT
																// specified.
			savingStage.getIcons()
					.add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
			savingStage.initStyle(StageStyle.TRANSPARENT);
	
			savingScene = new Scene(asPane);// , 150, 150);
			savingScene.setFill(Color.TRANSPARENT);
			savingStage.setScene(savingScene);
			savingStage.hide();
		}
		
	}

	/**
	 * Starts the wait stage in an executor thread
	 * 
	 * @param type
	 */
	public void showWaitStage(int type) {
		if (type == AUTOSAVING) {
			savingScene.setRoot(asPane);
		} else if (type == SAVING) {
			savingScene.setRoot(sPane);
		}

		mainSceneExecutor.execute(new LoadWaitStageTask(type));
	}

	/*
	 * Set up a wait stage
	 * 
	 * @param type
	 */
	class LoadWaitStageTask implements Runnable {
		int type;

		LoadWaitStageTask(int type) {
			this.type = type;
		}

		public void run() {
			// logger.config("LoadWaitStageTask is on " + Thread.currentThread().getName());
			Platform.runLater(() -> {
				// FXUtilities.runAndWait(() -> {}) does NOT work
				if (type == AUTOSAVING || type == SAVING) {
					stopPausePopup();
					setMonitor(savingStage);
					savingStage.setX((int) (stage.getX() + rootStackPane.getWidth() / 2 - 50));
					savingStage.setY((int) (stage.getY() + rootStackPane.getHeight() / 2 - 50));
					savingStage.show();
//				} else if (type == LOADING) {
//					setMonitor(loadingStage);
//					loadingStage.show();
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
//		} else if (type == LOADING) {
//			loadingStage.hide();
//			loadingStage.close();
		} else if (type == PAUSED) {
			stopPausePopup();
		} else
			System.out.println("MainScene's hideWaitStage() : type is invalid");

	}

	/**
	 * Sets the stage properly on the monitor where the mouse pointer is
	 * 
	 * @param stage
	 */
	public void setMonitor(Stage stage) {
		// TODO: how to run on the "Active monitor" as chosen by user ?
		// Note : "Active monitor is defined by whichever computer screen the mouse
		// pointer is or where the command console that starts mars-sim.
		// By default, it runs on the primary monitor (aka monitor 0 as reported by
		// windows os only.
		// see
		// http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762
		StartUpLocation startUpLoc = null;

		if (rootStackPane == null) {
			StackPane pane = new StackPane();
			pane.setPrefHeight(sceneWidth.get());
			pane.setPrefWidth(sceneHeight.get());
			// pane.prefHeightProperty().bind(scene.heightProperty());
			// pane.prefWidthProperty().bind(scene.widthProperty());
			startUpLoc = new StartUpLocation(pane.getPrefWidth(), pane.getPrefHeight());
		} else {
			startUpLoc = new StartUpLocation(rootStackPane.getWidth(), rootStackPane.getHeight());
		}

		double xPos = startUpLoc.getXPos();
		double yPos = startUpLoc.getYPos();
		// Set Only if X and Y are not zero and were computed correctly
		// if (xPos != 0 && yPos != 0) {
		stage.setX(xPos+1);
		stage.setY(yPos+1);
		// }

		// stage.centerOnScreen(); // this will cause the stage to be pinned slight
		// upward.
	}

	/**
	 * Starts the main scene executor thread
	 */
	private void startMainSceneExecutor() {
		// INFO: Simulation's startSimExecutor() is on JavaFX-Launcher Thread
		mainSceneExecutor = Executors.newSingleThreadExecutor();
	}

	public JFXTabPane getJFXTabPane() {
		return tabPane;
	}

	public boolean isMapTabOpen() {
		if (tabPane != null)
			return tabPane.getSelectionModel().isSelected(MainScene.MAP_TAB);
		else
			return false;
	}

	public boolean isMainTabOpen() {
		if (tabPane != null)
			return tabPane.getSelectionModel().isSelected(MainScene.MAIN_TAB);
		else
			return false;
	}

	public Parent getRoot() {
		return root;
	}

	public Parent getRootStackPane() {
		return rootStackPane;
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
	 * 
	 * @param n Node
	 * @param s tooltip's hint text
	 */
	public void setQuickToolTip(Node n, String s) {
		Tooltip tt = new Tooltip(s);
		tt.getStyleClass().add("ttip");
		Tooltip.install(n, tt);
		tt.setOnShowing(ev -> tt.setText(s));
	}

	private double convertSlider2Volume(double y) {
		return y / 100.0;
	}

	public JFXButton getMarsNetBtn() {
		return marsNetBtn;
	}

	public static void disableSound() {
//		logger.log(Level.SEVERE, "Disabling the sound UI in MainScene.");	
		BoxBlur blur = new BoxBlur(1.0, 1.0, 1);
		soundVBox.setEffect(blur);

		if (musicSlider != null) {
			musicSlider.setDisable(true);// .setValue(0);
			musicSlider.setEffect(blur);
		}
		if (soundEffectSlider != null) {
			soundEffectSlider.setDisable(true);// .setValue(0);
			soundEffectSlider.setEffect(blur);
		}

		if (musicMuteBox != null) {
			musicMuteBox.setDisable(true);
			musicMuteBox.setEffect(blur);
		}

		if (soundEffectMuteBox != null) {
			soundEffectMuteBox.setEffect(blur);
			soundEffectMuteBox.setDisable(true);
		}

		// if (effectLabel != null)
		effectLabel.setEffect(blur);

		// if (trackLabel != null)
		trackLabel.setEffect(blur);

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


	public float getMusic() {
		return (float) musicProperty.get();
	}

	public float getSoundEffect() {
		return (float) soundEffectProperty.get();
	}

	public boolean isMinimized() {
		return minimized;
	}

	
	/**
	 * Creates the new ticker billboard
	 */
	public void createBillboard() {

		matrix = DotMatrixBuilder.create().prefSize(925, 54).colsAndRows(196, 11).dotOnColor(Color.rgb(255, 55, 0))
				.dotOffColor(Color.rgb(64, 64, 64)).dotShape(DotShape.ROUND).matrixFont(MatrixFont8x8.INSTANCE).build();

		billboard = new StackPane(matrix);
		// billboard.setPadding(new Insets(1));
		billboard.setBackground(
				new Background(new BackgroundFill(Color.rgb(20, 20, 20), CornerRadii.EMPTY, Insets.EMPTY)));
		// billboard.setBorder(new Border(new BorderStroke(Color.DARKCYAN,
		// BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, BorderWidths.FULL)));
		dragNode = new DraggableNode(billboard, stage, 925, 54);

	}

	
	/**
	 * Sends a message to the new ticker billboard
	 * 
	 * @param str the message string
	 */
	public void sendMsg(String str) {

		if (!str.equals(messageCache)) {

			if (billboardTimer != null) {
				matrix.clear();
				billboardTimer.stop();
				billboardTimer = null;
				lastNewsClock = null;
			}

			messageCache = str;

			newsHeader = null;
			if (str.contains("died"))
				newsHeader = BREAKING_NEWS;
			else if (str.contains("suffered") || str.contains("cured"))
				newsHeader = HEALTH_NEWS;
			else if (str.toLowerCase().contains("mission") || str.toLowerCase().contains("beacon"))
				newsHeader = MISSION_REPORTS;
			else if (str.toLowerCase().contains("radiation"))
				newsHeader = HAZARD;
			else
				newsHeader = BREAKING_NEWS;

			String text = newsHeader + str.replaceAll("\\u00B0 ", "").replace("ć", "c").replace("æ", "a");

			textLength = text.length();
			textLengthInPixel = textLength * 8;
			offset = 3;
			x = matrix.getCols() + 7;
			lastTimerCall = System.nanoTime();

			billboardTimer = new AnimationTimer() {
				@Override
				public void handle(final long now) {
					if (now > lastTimerCall + 10_000_000l) {
						if (x < -textLengthInPixel) {
							x = matrix.getCols() + 7;
						}
						int color = RED;
						if (newsHeader.equals(MISSION_REPORTS))
							color = LIME;
						else if (newsHeader.equals(HEALTH_NEWS))
							color = YELLOW;
						else if (newsHeader.equals(HAZARD))
							color = ORANGE;
						for (int i = 0; i < textLength; i++) {
							matrix.setCharAt(text.charAt(i), x + i * 8, offset, color);// i % 2 == 0 ? LIME : RED);
						}
						x--;
						lastTimerCall = now;
					}
				}
			};

			billboardTimer.start();
			
			lastNewsClock = (MarsClock) marsClock.clone();
		}
	}

	/**
	 * Create the tool windows tool bar
	 */
	public void createJFXToolbar() {
//		JFXButton b0 = new JFXButton("Help");
		JFXButton b1 = new JFXButton("Search");
		JFXButton b2 = new JFXButton("Time");
		JFXButton b3 = new JFXButton("Monitor");
		JFXButton b4 = new JFXButton("Mission");
		JFXButton b5 = new JFXButton("Science");
		JFXButton b6 = new JFXButton("Resupply");

//		setQuickToolTip(b0, "Open Help Browser");
		setQuickToolTip(b1, "Search Box");
		setQuickToolTip(b2, "Time Tool");
		setQuickToolTip(b3, "Monitor Tool");
		setQuickToolTip(b4, "Mission Wizard");
		setQuickToolTip(b5, "Science Tool");
		setQuickToolTip(b6, "Resupply Tool");

		toolbar = new JFXToolbar();
		toolbar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		toolbar.getLeftItems().addAll(b1, b2, b3, b4, b5, b6);

//		b0.setOnAction(e -> desktop.openToolWindow(GuideWindow.NAME));
		b1.setOnAction(e -> desktop.openToolWindow(SearchWindow.NAME));
		b2.setOnAction(e -> desktop.openToolWindow(TimeWindow.NAME));
		b3.setOnAction(e -> desktop.openToolWindow(MonitorWindow.NAME));
		b4.setOnAction(e -> desktop.openToolWindow(MissionWindow.NAME));
		b5.setOnAction(e -> desktop.openToolWindow(ScienceWindow.NAME));
		b6.setOnAction(e -> desktop.openToolWindow(ResupplyWindow.NAME));

//		b0.setOnAction(toolbarHandler);		
//		EventHandler<ActionEvent> toolbarHandler = new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent ae) {
//				String s = ((JFXButton)ae.getTarget()).getText();		
//			}
//		}
	}

	public void unpause() {
		// Remove Dialog
		isShowingDialog = false;
		// Revert the sound setting
//		musicMuteBox.setSelected(lastMusicMuteBoxSetting);
//		soundEffectMuteBox.setSelected(lastSoundEffectMuteBoxSetting);
		// Note : sound player doesn't necessarily know the current music/sound volume in main scene.
//		if (!musicMuteBox.isSelected())
//			soundPlayer.setSoundVolume(convertSlider2Volume(musicSlider.getValue()));
//		if (!soundEffectMuteBox.isSelected())
//			soundPlayer.setMusicVolume(convertSlider2Volume(soundEffectSlider.getValue()));
//		// Play music track
//		if (!soundPlayer.isSoundDisabled() && !musicMuteBox.isSelected()
//			&& musicSlider.getValue() > 0)
//				soundPlayer.resumeMusic();//playRandomMusicTrack();
		// Play time label timer
		timeLabeltimer.play();
		// Play billboard timer
		if (billboardTimer != null)
			billboardTimer.start();
		// Stop pause popup
		stopPausePopup();

	}
	
	public void pause(boolean music, boolean sound) {
		// Show Dialog
		isShowingDialog = true;
		// Save the mute boxes setting
//		if (music)
//			lastMusicMuteBoxSetting = musicMuteBox.isSelected();
//		if (sound)
//			lastSoundEffectMuteBoxSetting = soundEffectMuteBox.isSelected();
//		// Check the mute boxes
//		if (music)		
//			musicMuteBox.setSelected(true);
//		if (sound)
//			soundEffectMuteBox.setSelected(true);
		// Pause time label timer
		timeLabeltimer.pause();
		// Stop billboard timer
		if (billboardTimer != null)
			billboardTimer.stop();
		// Start pause popup
		startPausePopup();
	}
	

	@Override
	public void clockPulse(double time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uiPulse(double time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
//		System.out.println("calling MainScene's pauseChange(). isPaused : " + isPaused);
		if (isPaused) {
			if (!masterClock.isSavingSimulation()) {
				if (exitDialog == null || !exitDialog.isVisible()) {
					pause(true, true);
//					if (!soundPlayer.isMusicMute())
//						pause(true, false);
//					if (!soundPlayer.isSoundMute())
//						pause(false, true);
				}
			}

		} else {
			unpause();
		}
	}
	
	public void destroy() {
		quote = null;
		// messagePopup = null;
		// topFlapBar = null;
		masterClock.removeClockListener(this);
		marsNetBox = null;
		marsNetBtn = null;
		chatBox = null;
		sMapStackPane = null;
		mainStackPane = null;
		dashboardStackPane = null;
		root = null;
		rootStackPane = null;
		minimapGroup = null;
		speedPane = null;
		soundPane = null;
		calendarPane = null;
		settlementBox = null;
		chatBoxPane = null;
		pausePane = null;
		asPane = null;
		sPane = null;
		billboard = null;
		stageAnchorPane = null;
		// newSimThread = null;
		stage = null;
//		loadingStage = null;
		savingStage = null;
		timeLabeltimer = null;
		// notificationPane = null;
		desktop.destroy();
		desktop = null;
		menuBar = null;
		marsNode = null;
		transportWizard = null;
		constructionWizard = null;
	}

}
