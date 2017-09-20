/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.1.0 2017-01-24
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXSlider.IndicatorPosition;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXToggleButton;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

import org.controlsfx.control.MaskerPane;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
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
import javafx.scene.layout.Background;
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
import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;

import java.awt.Toolkit;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementTransparentPanel;
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

	public static String OS = Simulation.OS.toLowerCase();//System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'

	private static final int TIME_DELAY = SettlementWindow.TIME_DELAY;


	private static final int SYSTEM_THEME = -1;
	private static final int NIMROD_THEME = 7;
	private static final int NIMBUS_THEME = 0;

	private static int choice_theme = NIMBUS_THEME;
	
	public static final int MAIN_TAB = 0;
	public static final int MAP_TAB = 1;
	public static final int HELP_TAB = 2;

	public static final int LOADING = 0;
	public static final int SAVING = 1;
	public static final int AUTOSAVING = 2;
	public static final int PAUSED = 3;

	public static final int DEFAULT_WIDTH = 1280;//1366;
	public static final int DEFAULT_HEIGHT = 768;

	private static final double ROTATION_CHANGE = Math.PI / 20D;

	private static final String ROUND_BUTTONS_DIR = "/icons/round_buttons/";

	private static final String PAUSE = "PAUSE";
	private static final String ESC_TO_RESUME = "ESC to resume";
	private static final String PAUSE_MSG = " [PAUSE]";// : ESC to resume]";
	private static final String LAST_SAVED = "Last Saved : ";
	private static final String EARTH_DATE_TIME = "EARTH  :  ";
	private static final String MARS_DATE_TIME = "MARS  :  ";
	private static final String UMST = " (UMST)";
	private static final String ONE_SPACE = " ";
	private static final String MONTH = "    Month : ";
	private static final String ORBIT = "Orbit : ";
	private static final String ADIR = "Adir";

	private static final String BENCHMARK = "Benchmark :";
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

	private static int theme = -1; // 6 is snow blue; 7 is the mud orange with nimrod
	public static int chatBoxHeight = 256;
	public static int LINUX_WIDTH = 270;
	public static int MACOS_WIDTH = 230;
	public static int WIN_WIDTH = 230;

	public static boolean menuBarVisible = false;
	static boolean isShowingDialog = false;

	private int solElapsedCache = 0;

	private double newTimeRatio = 0;
	private double initial_time_ratio = 0;
	private double sliderCache = 0;

	private boolean isMuteCache;
	private boolean flag = true;
	private boolean isMainSceneDoneLoading = false;
	private boolean isFullScreenCache = false;

	private DoubleProperty sceneWidth;// = new SimpleDoubleProperty(DEFAULt_WIDTH);//1366-40;
	private DoubleProperty sceneHeight;// = new SimpleDoubleProperty(DEFAULt_HEIGHT); //768-40;

	private volatile transient ExecutorService mainSceneExecutor;

	private String themeSkin = "nimrod";
	private String title = null;
	private String dir = null;
	private String oldLastSaveStamp = null;

	private Pane root;
	private StackPane mainAnchorPane, //monPane,
					mapStackPane, minimapStackPane,
					speedPane, soundPane, calendarPane,// farmPane,
					settlementBox, chatBoxPane, pausePane,
					asPane, sPane;

	//private FlowPane flowPane;
	private AnchorPane anchorPane, mapAnchorPane;
	private SwingNode swingNode, mapNode, minimapNode, guideNode;//monNode, missionNode, resupplyNode, sciNode, guideNode ;
	private Stage stage, loadingStage, savingStage;
	private Scene scene, savingScene;

	private File fileLocn = null;
	private Thread newSimThread;

	private IconNode soundIcon, marsNetIcon, speedIcon;//, farmIcon;
	private Button earthTimeButton, marsTimeButton;//, northHemi, southHemi;
	private Label lastSaveLabel, TPSLabel, upTimeLabel, noteLabel, benchmarkLabel; //monthLabel, yearLabel, LSLabel
	private Text LSText, monthText, yearText, northText, southText;
	private Blend blend;
	private VBox mapLabelBox, speedVBox, soundVBox;
	private Tab mainTab;

    private Spinner spinner;

	private JFXComboBox<Settlement> sBox;
	//private JFXBadge badgeIcon;
	//private JFXSnackbar snackbar;
	private JFXToggleButton cacheToggle, minimapToggle, mapToggle; //calendarButton,
	private static JFXSlider zoomSlider, soundSlider; //timeSlider,
	private JFXButton soundBtn, marsNetBtn, rotateCWBtn, rotateCCWBtn, recenterBtn, speedBtn;//, farmBtn; // miniMapBtn, mapBtn,
	private JFXPopup soundPopup, marsNetBox, marsCalendarPopup, simSpeedPopup;//, farmPopup;// marsTimePopup;
	private JFXTabPane jfxTabPane;

	private CheckBox muteBox;
	//private DndTabPane dndTabPane;
	private ESCHandler esc = null;

	private Timeline timeline;
	//private NotificationPane notificationPane;

	private DecimalFormat twoDigitFormat = new DecimalFormat(Msg.getString("twoDigitFormat")); //$NON-NLS-1$
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("TimeWindow.decimalFormat")); //$NON-NLS-1$

	private ChatBox chatBox;
	private MainDesktopPane desktop;
	private MainSceneMenu menuBar;

	private MarsNode marsNode;
	private TransportWizard transportWizard;
	private ConstructionWizard constructionWizard;

	private QuotationPopup quote;
	//private MessagePopup messagePopup;
	//private BorderSlideBar topFlapBar;

    private Simulation sim = Simulation.instance();
    private MasterClock masterClock = sim.getMasterClock();
	private EarthClock earthClock;
	private MarsClock marsClock;

	private SettlementWindow settlementWindow;
	private NavigatorWindow navWin;
	private SettlementMapPanel mapPanel;

	private static AudioPlayer soundPlayer;
	private MarsCalendarDisplay calendarDisplay;
	private UpTimer uptimer;

	private OrbitInfo orbitInfo;

	//private List<DesktopPane> desktops;
	//private ObservableList<Screen> screens;

	/**
	 * Constructor for MainScene
	 */
	public MainScene() {
		//logger.info("MainScene's constructor() is on " + Thread.currentThread().getName() + " Thread");
		stage = new Stage();
		stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
		this.isMainSceneDoneLoading = false;

		sceneWidth = new SimpleDoubleProperty(DEFAULT_WIDTH);
		sceneHeight = new SimpleDoubleProperty(DEFAULT_HEIGHT);

		//logger.info("OS is " + OS);
		stage.setResizable(true);
		stage.setMinWidth(sceneWidth.get());//1024);
		stage.setMinHeight(sceneHeight.get());//480);
		stage.setFullScreenExitHint("Use Ctrl+F (or Meta+C in macOS) to toggle between either the Full Screen mode and the Window mode");
		stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));

		// Detect if a user hits the top-right close button
		stage.setOnCloseRequest(e -> {
			if (!isShowingDialog) {
				dialogOnExit();
			}
			e.consume();
		});

		// Detect if a user hits ESC
		esc = new ESCHandler();
		setEscapeEventHandler(true, stage);

	}

	/*
	 * Loads the rest of the methods in MainScene.
	*/
   public void finalizeMainScene() {

	   Platform.runLater(() -> {
		   prepareScene();
		   initializeTheme();
		   prepareOthers();
		   //2016-02-07 Added calling setMonitor() for screen detection and placing quotation pop at top right corner
		   setMonitor(stage);
		   stage.centerOnScreen();
		   stage.setTitle(Simulation.title);
		   stage.show();
		   stage.requestFocus();

		   openInitialWindows();
		   hideWaitStage(MainScene.LOADING);
		});
   }

	/*
	 * Prepares the scene in the main scene
	 */
	public void prepareScene() {
		//logger.info("MainMenu's prepareScene() is on " + Thread.currentThread().getName());
		UIConfig.INSTANCE.useUIDefault();
		// creates and initialize scene
		scene = initializeScene();
		// switch from the main menu's scene to the main scene's scene
		stage.setScene(scene);

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
		}
		else {
			stage.removeEventHandler(KeyEvent.KEY_PRESSED, esc);
		}
	}

	class ESCHandler implements EventHandler<KeyEvent> {

		public void handle(KeyEvent t) {
			if (t.getCode() == KeyCode.ESCAPE) {
				boolean isOnPauseMode = masterClock.isPaused();
				if (isOnPauseMode) {
					unpauseSimulation();
				}
				else {
					pauseSimulation();
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
	 * Prepares the transport wizard, construction wizard, autosave timer and earth timer
	 */
	public void prepareOthers() {
		//logger.info("MainScene's prepareOthers() is on " + Thread.currentThread().getName() + " Thread");
		uptimer = masterClock.getUpTimer();
		startEarthTimer();
		transportWizard = new TransportWizard(this, desktop);
		constructionWizard = new ConstructionWizard(this, desktop);

	}


	/**
	 * Pauses sim and opens the transport wizard
	 * @param buildingManager
	 */
	public synchronized void openTransportWizard(BuildingManager buildingManager) {
		//logger.info("MainScene's openTransportWizard() is on " + Thread.currentThread().getName() + " Thread");
		// normally on pool-4-thread-3 Thread
		// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
		// 2015-12-16 Track the current pause state
		Platform.runLater(() -> {
			boolean previous = startPause();
			transportWizard.deliverBuildings(buildingManager);
			endPause(previous);
		});
	}


	public TransportWizard getTransportWizard() {
		return transportWizard;
	}

	/**
 	 * Pauses sim and opens the construction wizard
	 * @param mission
	 */
	// 2015-12-16 Added openConstructionWizard()
	public void openConstructionWizard(BuildingConstructionMission mission) { // ConstructionManager constructionManager,
		//logger.info("MainScene's openConstructionWizard() is in " + Thread.currentThread().getName() + " Thread");
		// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
		// 2015-12-16 Track the current pause state
		Platform.runLater(() -> {
			boolean previous = startPause();
			constructionWizard.selectSite(mission);
			endPause(previous);
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
 			if (desktop.isToolWindowOpen(GuideWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(GuideWindow.NAME));
			else {
				//getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(GuideWindow.NAME));
			}
*/
		});
	    Nodes.addInputMap(root, f1);

		InputMap<KeyEvent> f2 = consume(keyPressed(F2), e -> {
			if (desktop.isToolWindowOpen(SearchWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(SearchWindow.NAME));
			else {
				//getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(SearchWindow.NAME));
			}
		});
	    Nodes.addInputMap(root, f2);

		InputMap<KeyEvent> f3 = consume(keyPressed(F3), e -> {
			if (desktop.isToolWindowOpen(TimeWindow.NAME))
				SwingUtilities.invokeLater(() ->desktop.closeToolWindow(TimeWindow.NAME));
			else {
				//getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() ->desktop.openToolWindow(TimeWindow.NAME));
			}
		});
	    Nodes.addInputMap(root, f3);

		InputMap<KeyEvent> f4 = consume(keyPressed(F4), e -> {
			if (desktop.isToolWindowOpen(MonitorWindow.NAME)) {
				SwingUtilities.invokeLater(() ->desktop.closeToolWindow(MonitorWindow.NAME));
				//rootAnchorPane.getChildren().remove(monPane);
			}
			else {

				//getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				//rootAnchorPane.getChildren().add(monPane);
		        //AnchorPane.setRightAnchor(monPane, 0.0);
		        //AnchorPane.setBottomAnchor(monPane, 0.0);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(MonitorWindow.NAME));
			}
		});
	    Nodes.addInputMap(root, f4);

		InputMap<KeyEvent> f5 = consume(keyPressed(F5), e -> {
			if (desktop.isToolWindowOpen(MissionWindow.NAME))
				SwingUtilities.invokeLater(() ->desktop.closeToolWindow(MissionWindow.NAME));
			else {
				//getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() ->desktop.openToolWindow(MissionWindow.NAME));
			}
		});
	    Nodes.addInputMap(root, f5);

		InputMap<KeyEvent> f6 = consume(keyPressed(F6), e -> {
			if (desktop.isToolWindowOpen(ScienceWindow.NAME))
				SwingUtilities.invokeLater(() ->desktop.closeToolWindow(ScienceWindow.NAME));
			else {
				//getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() ->desktop.openToolWindow(ScienceWindow.NAME));
			}
		});
	    Nodes.addInputMap(root, f6);

		InputMap<KeyEvent> f7 = consume(keyPressed(F7), e -> {
			if (desktop.isToolWindowOpen(ResupplyWindow.NAME))
				SwingUtilities.invokeLater(() ->desktop.closeToolWindow(ResupplyWindow.NAME));
			else {
				//getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() ->desktop.openToolWindow(ResupplyWindow.NAME));
			}
		});
	    Nodes.addInputMap(root, f7);

		InputMap<KeyEvent> ctrlQ = consume(keyPressed(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)), e -> {
        	popAQuote();
        	mainAnchorPane.requestFocus();
		});
	    Nodes.addInputMap(root, ctrlQ);

		InputMap<KeyEvent> ctrlN = consume(keyPressed(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)), e -> {
        	boolean isNotificationOn = !desktop.getEventTableModel().isNoFiring();
       		if (isNotificationOn) {
        		menuBar.getNotificationItem().setSelected(false);
                desktop.getEventTableModel().setNoFiring(true);
        	}
        	else {
        		menuBar.getNotificationItem().setSelected(true);
                desktop.getEventTableModel().setNoFiring(false);
        	}
		});
	    Nodes.addInputMap(root, ctrlN);

		InputMap<KeyEvent> ctrlF = consume(keyPressed(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)), e -> {
           	boolean isFullScreen = stage.isFullScreen();
        	if (!isFullScreen) {
        		menuBar.getShowFullScreenItem().setSelected(true);
        		if (!isFullScreenCache)
        			stage.setFullScreen(true);
        	}
        	else {
        		menuBar.getShowFullScreenItem().setSelected(false);
        		if (isFullScreenCache)
        			stage.setFullScreen(false);
        	}
        	isFullScreenCache = stage.isFullScreen();
		});
	    Nodes.addInputMap(root, ctrlF);

		InputMap<KeyEvent> ctrlUp = consume(keyPressed(new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN)), e -> {
			//soundPlayer.volumeUp();
			soundSlider.setValue(soundSlider.getValue() + .5);
			//soundSlider.setValue(convertVolume2Slider(soundPlayer.getVolume() +.05));
		});
	    Nodes.addInputMap(root, ctrlUp);

		InputMap<KeyEvent> ctrlDown = consume(keyPressed(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN)), e -> {
			//soundPlayer.volumeDown();
			soundSlider.setValue(soundSlider.getValue() - .5);
			//soundSlider.setValue(convertVolume2Slider(soundPlayer.getVolume() -.05));
		});
	    Nodes.addInputMap(root, ctrlDown);

		InputMap<KeyEvent> ctrlM = consume(keyPressed(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN)), e -> {
			boolean isMute = menuBar.getMuteItem().isSelected();
			if (isMute) {
        		menuBar.getMuteItem().setSelected(false);
        		soundPlayer.setMute(true);
        		soundSlider.setValue(0);
		        muteBox.setSelected(true);
        	}
        	else {
        		menuBar.getMuteItem().setSelected(true);
        		soundPlayer.setMute(false);
        		soundSlider.setValue(convertVolume2Slider(soundPlayer.getVolume()));
		        muteBox.setSelected(false);
        	}

		});
	    Nodes.addInputMap(root, ctrlM);

		//InputMap<KeyEvent> ctrlN = consume(keyPressed(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)), e -> {
		//	newSimulation();
		//});
	    //Nodes.addInputMap(root, ctrlN);

		InputMap<KeyEvent> ctrlS = consume(keyPressed(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)), e -> {
			saveSimulation(Simulation.SAVE_DEFAULT);
		});
	    Nodes.addInputMap(root, ctrlS);

		InputMap<KeyEvent> ctrlE = consume(keyPressed(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN)), e -> {
			saveSimulation(Simulation.SAVE_AS);
		});
	    Nodes.addInputMap(root, ctrlE);

		InputMap<KeyEvent> ctrlX = consume(keyPressed(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN)), e -> {
			if (!isShowingDialog)
				dialogOnExit();
		});
	    Nodes.addInputMap(root, ctrlX);

		InputMap<KeyEvent> ctrlT = consume(keyPressed(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)), e -> {
			if (OS.contains("linux")) {
				//if (theme == 6)
				setTheme(0);
				//else if (theme == 0)
				//	setTheme(6);
			}
			else {
				if (theme == 0 || theme == 6) {
					setTheme(7);
				}
				else if (theme == 7) {
					setTheme(0);
				}
			}
		});
	    Nodes.addInputMap(root, ctrlT);

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
		ds1.setColor(Color.web("#d68268")); //#d68268 is pinkish orange//f13a00"));
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
        //header_label.setEffect(blend);
        header_label.setStyle("-fx-text-fill: black;"
        			+ "-fx-font-size: 13px;"
        		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
        			+ "-fx-font-weight: normal;");
        header_label.setPadding(new Insets(3, 0, 1, 10));
        return header_label;
	}

	public Text createBlendText(String s) {
		Text text = new Text(s);
        //text.setEffect(blend);
        text.setStyle("-fx-text-fill: black;"
        			+ "-fx-font-size: 11px;"
        		    //+ "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
        			+ "-fx-font-weight: normal;");
        return text;
	}

	/**
	 * initializes the scene
	 *
	 * @return Scene
	 */
	@SuppressWarnings("unchecked")
	public Scene initializeScene() {
		//logger.info("MainScene's initializeScene() is on " + Thread.currentThread().getName() + " Thread");

		//see dpi scaling at http://news.kynosarges.org/2015/06/29/javafx-dpi-scaling-fixed/
		//"I guess we'll have to wait until Java 9 for more flexible DPI support.
		//In the meantime I managed to get JavaFX DPI scale factor,
		//but it is a hack (uses both AWT and JavaFX methods)"

		// Number of actual horizontal lines (768p)
		double trueHorizontalLines = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		// Number of scaled horizontal lines. (384p for 200%)
		double scaledHorizontalLines = Screen.getPrimary().getBounds().getHeight();
		// DPI scale factor.
		double dpiScaleFactor = trueHorizontalLines / scaledHorizontalLines;
		logger.info("DPI Scale Factor is " + dpiScaleFactor);


		// Create group to hold swingNode which in turns holds the Swing desktop
		swingNode = new SwingNode();
		createSwingNode();

		mainAnchorPane = new StackPane();
		mainAnchorPane.getChildren().add(swingNode);
		mainAnchorPane.setMinHeight(sceneHeight.get());
		mainAnchorPane.setMinWidth(sceneWidth.get());
		// 2016-11-25 Setup root for embedding key events
		root = new Pane();//Group();

        soundPlayer = desktop.getSoundPlayer();

		// 2016-11-14 Setup key events using wellbehavedfx
		setupKeyEvents();

		IconFontFX.register(FontAwesome.getIconFont());

	    //2015-11-11 Added createFlyout()
		marsNetBox = createFlyout();
        flag = false;
        //EffectUtilities.makeDraggable(flyout.getScene().getRoot().getStage(), chatBox);
		// Create ControlFX's StatusBar
		//statusBar = createStatusBar();

        createBlend();

        createLastSaveBar();
		createMarsTimeBar();
        createEarthTimeBar();

        createSpeedPanel();
        createSoundPopup();
        
        //createFarmPopup();

        // Create menuBar
		menuBar = new MainSceneMenu(this, desktop);
		// Create Snackbar
		//createJFXSnackbar();
		// Create jfxTabPane
		createJFXTabs();

		anchorPane = new AnchorPane();

		pausePane = new StackPane();
		pausePane.setStyle("-fx-background-color:rgba(0,0,0,0.5);");
		pausePane.getChildren().add(createPausePaneContent());
		pausePane.setPrefSize(150, 150);

		if (OS.contains("mac")) {
			((MenuBar)menuBar).useSystemMenuBarProperty().set(true);
		}

		//AnchorPane.setBottomAnchor(jfxTabPane, 0.0);
        AnchorPane.setLeftAnchor(jfxTabPane, 0.0);
        AnchorPane.setRightAnchor(jfxTabPane, 0.0);
        AnchorPane.setTopAnchor(jfxTabPane, 0.0);

        //AnchorPane.setRightAnchor(badgeIcon, 5.0);
        //AnchorPane.setTopAnchor(badgeIcon, 0.0);

		if (OS.contains("win")) {
	        AnchorPane.setTopAnchor(speedBtn, 3.0);
	        AnchorPane.setTopAnchor(marsNetBtn, 3.0);
	        AnchorPane.setTopAnchor(lastSaveLabel, 1.0);
	        AnchorPane.setTopAnchor(soundBtn, 3.0);
	        //AnchorPane.setTopAnchor(farmBtn, 3.0);
	        AnchorPane.setTopAnchor(earthTimeButton, 1.0);
        	AnchorPane.setTopAnchor(marsTimeButton, 1.0);
		}
		else if (OS.contains("linux")) {
	        AnchorPane.setTopAnchor(speedBtn, 0.0);
	        AnchorPane.setTopAnchor(marsNetBtn, 0.0);
	        AnchorPane.setTopAnchor(lastSaveLabel, 1.0);
	        AnchorPane.setTopAnchor(soundBtn, 0.0);
	        //AnchorPane.setTopAnchor(farmBtn, 0.0);
        	AnchorPane.setTopAnchor(earthTimeButton, 1.0);
        	AnchorPane.setTopAnchor(marsTimeButton, 1.0);
		}
		else if (OS.contains("mac")) {
	        AnchorPane.setTopAnchor(speedBtn, 0.0);
	        AnchorPane.setTopAnchor(marsNetBtn, 0.0);
	        AnchorPane.setTopAnchor(lastSaveLabel, 0.0);
	        AnchorPane.setTopAnchor(soundBtn, 0.0);
	        //AnchorPane.setTopAnchor(farmBtn, 0.0);
        	AnchorPane.setTopAnchor(earthTimeButton, 1.0);
        	AnchorPane.setTopAnchor(marsTimeButton, 1.0);
		}

        AnchorPane.setRightAnchor(speedBtn, 5.0);
        AnchorPane.setRightAnchor(marsNetBtn, 45.0);
        AnchorPane.setRightAnchor(soundBtn, 85.0);
        //AnchorPane.setRightAnchor(farmBtn, 125.0);
        AnchorPane.setRightAnchor(marsTimeButton, 165.0);
        AnchorPane.setRightAnchor(earthTimeButton, marsTimeButton.getMinWidth() + 165);
        AnchorPane.setRightAnchor(lastSaveLabel,  marsTimeButton.getMinWidth() +  marsTimeButton.getMinWidth() + 165);

        anchorPane.getChildren().addAll(
        		jfxTabPane,
        		marsNetBtn, speedBtn,
        		lastSaveLabel,
        		earthTimeButton, marsTimeButton, soundBtn);//, farmBtn);//badgeIcon,borderPane, timeBar, snackbar


		root.getChildren().addAll(anchorPane);


    	scene = new Scene(root, sceneWidth.get(), sceneHeight.get());//, Color.BROWN);

		//pausePane.prefWidthProperty().bind(scene.widthProperty());
		//pausePane.prefHeightProperty().bind(scene.heightProperty());
    	pausePane.setLayoutX((sceneWidth.get()-pausePane.getPrefWidth())/2D);
		pausePane.setLayoutY((sceneHeight.get()-pausePane.getPrefHeight())/2D);

		jfxTabPane.prefHeightProperty().bind(scene.heightProperty());//.subtract(35));//73));
		jfxTabPane.prefWidthProperty().bind(scene.widthProperty());

		mainAnchorPane.prefHeightProperty().bind(scene.heightProperty().subtract(35));
		mainAnchorPane.prefWidthProperty().bind(scene.widthProperty());

		// anchorTabPane is within jfxTabPane
		mapAnchorPane.prefHeightProperty().bind(scene.heightProperty().subtract(35));//73));
		mapAnchorPane.prefWidthProperty().bind(scene.widthProperty());

		mapStackPane.prefHeightProperty().bind(scene.heightProperty().subtract(35));//73));

		return scene;
	}

/*
	public void createJFXSnackbar() {
		snackbar = new JFXSnackbar();
		//snackbar.getStylesheets().clear();
		//snackbar.getStylesheets().add(getClass().getResource("/css/jfoenix-design.css").toExternalForm());
		//snackbar.getStylesheets().add(getClass().getResource("/css/jfoenix-components.css").toExternalForm());
		snackbar.setPrefSize(300, 40);
		snackbar.getStyleClass().add("jfx-snackbar");
		snackbar.registerSnackbarContainer(root);

		Icon icon = new Icon("INBOX");
		icon.setPadding(new Insets(10));
		badgeIcon = new JFXBadge(icon);
		badgeIcon.getStylesheets().clear();
		//badge1.getStylesheets().add(getClass().getResource("/css/jfoenix-design.css").toExternalForm());
		//badge1.getStylesheets().add(getClass().getResource("/css/jfoenix-components.css").toExternalForm());
		//badge1.getStyleClass().add("icons-badge");
		//badge1.setStyle("icons-badge");
		badgeIcon.setText("0");

		badgeIcon.setOnMouseClicked((e) -> {
			int value = Integer.parseInt(badgeIcon.getText());
			if (e.getButton() == MouseButton.PRIMARY) {
				value++;
			} else if (e.getButton() == MouseButton.SECONDARY) {
				if (value > 0)
					value--;
			}

			if (value == 0) {
				badgeIcon.setEnabled(false);
			} else {
				badgeIcon.setEnabled(true);
			}
			badgeIcon.setText(String.valueOf(value));

			// trigger snackbar
			if (count++%2==0){
				snackbar.fireEvent(new SnackbarEvent("Toast Message " + count));
			} else {
				snackbar.fireEvent(new SnackbarEvent("Snackbar Message "+ count,"UNDO",3000,(b)->{}));
			}
		});
	}
*/

	public void createEarthTimeBar() {
		earthTimeButton = new Button();
		earthTimeButton.setMaxWidth(Double.MAX_VALUE);

		if (OS.contains("linux")) {
			earthTimeButton.setMinWidth(LINUX_WIDTH-5);
			earthTimeButton.setPrefSize(LINUX_WIDTH-5, 29);
		}
		else if (OS.contains("mac")) {
			earthTimeButton.setMinWidth(MACOS_WIDTH-10);
			earthTimeButton.setPrefSize(MACOS_WIDTH-10, 28);
		}
		else {
			earthTimeButton.setMinWidth(WIN_WIDTH-15);
			earthTimeButton.setPrefSize(WIN_WIDTH-15, 33);
		}

		if (masterClock == null) {
			masterClock = sim.getMasterClock();
		}

		if (earthClock == null) {
			earthClock = masterClock.getEarthClock();
		}


		earthTimeButton.setId("rich-blue");
		earthTimeButton.setMaxWidth(Double.MAX_VALUE);
		earthTimeButton.setAlignment(Pos.CENTER_LEFT);
	}


	
	
    /**
     * Creates and returns the panel for simulation speed and time info
     */
	// 2017-01-12 Added createSpeedPanel
	@SuppressWarnings("unchecked")
	public void createSpeedPanel() {
		//logger.info("MainScene's createEarthTimeBox() is on " + Thread.currentThread().getName());

		speedBtn = new JFXButton();
		//speedBtn.setStyle(value);
		//speedBtn.getStyleClass().add("menu-button");//"button-raised");
		speedIcon = new IconNode(FontAwesome.CLOCK_O);
		speedIcon.setIconSize(20);
		//speedIcon.setFill(Color.YELLOW);
		//speedIcon.setStroke(Color.WHITE);

		speedBtn.setMaxSize(20, 20);
		speedBtn.setGraphic(speedIcon);
		setQuickToolTip(speedBtn, "Click to open Speed Panel");
		speedBtn.setOnAction(e -> {
            if (simSpeedPopup.isShowing()) {
            	simSpeedPopup.hide();//close();
            }
            else {
            	simSpeedPopup.show(speedBtn, PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
            }
		});

		speedPane = new StackPane();
		//speedPane.setEffect(blend);
		speedPane.getStyleClass().add("jfx-popup-container");
		speedPane.setAlignment(Pos.CENTER);
		speedPane.setPrefHeight(100);
		speedPane.setPrefWidth(earthTimeButton.getPrefWidth());
		simSpeedPopup = new JFXPopup(speedPane);

		initial_time_ratio = Simulation.instance().getMasterClock().getDefaultTimeRatio();
/*
		// Set up a settlement view zoom bar
		timeSlider = new JFXSlider();
		//timeSlider.setEffect(blend);
		timeSlider.getStyleClass().add("jfx-slider");
		timeSlider.setPrefHeight(25);
		timeSlider.setPadding(new Insets(2, 2, 2, 2));
		//timeSlider.prefHeightProperty().bind(mapNodePane.heightProperty().multiply(.3d));
		timeSlider.setMin(0); // need to be zero
		timeSlider.setMax(12);//initial_time_ratio*32D);//8D);
		timeSlider.setValue(7);//initial_time_ratio);
		timeSlider.setMajorTickUnit(1);//initial_time_ratio*4);
		timeSlider.setMinorTickCount(1);
		//timeSlider.setShowTickLabels(true);
		timeSlider.setShowTickMarks(true);
		timeSlider.setSnapToTicks(true);
		timeSlider.setBlockIncrement(1);//initial_time_ratio/32D);//4D);
		timeSlider.setOrientation(Orientation.HORIZONTAL);
		timeSlider.setIndicatorPosition(IndicatorPosition.RIGHT);

        VBox timeSliderBox = new VBox();
        timeSliderBox.setPadding(new Insets(2, 2, 2, 2));
        timeSliderBox.getChildren().add(timeSlider);

		setQuickToolTip(timeSlider, "adjust the time ratio (how fast the simulation runs)"); //$NON-NLS-1$
*/
		//Label header_label = createHeader("SPEED PANEL");
		Text header_label = createTextHeader("SPEED PANEL");

		String DEFAULT = " (Default : ";
		String CLOSE_PAR = ")";
        int default_ratio = (int)masterClock.getDefaultTimeRatio();
        StringBuilder s0 = new StringBuilder();

        Label default_ratio_label0 = new Label(DTR);
        //time_ratio_label0.setEffect(blend);
        default_ratio_label0.setAlignment(Pos.CENTER_RIGHT);
        default_ratio_label0.setStyle("-fx-text-fill: #206982;"
        			+ "-fx-font-size: 12px;"
        		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
        			+ "-fx-font-weight: normal;");
        default_ratio_label0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(default_ratio_label0, "The default time-ratio is the ratio of simulation time to real time"); //$NON-NLS-1$

        Label spinner_label0 = new Label(TR);
        //time_ratio_label0.setEffect(blend);
        spinner_label0.setAlignment(Pos.CENTER_RIGHT);
        spinner_label0.setStyle("-fx-text-fill: #206982;"
        			+ "-fx-font-size: 12px;"
        		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
        			+ "-fx-font-weight: normal;");
		spinner_label0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(spinner_label0, "The current time-ratio is the ratio of simulation time to real time"); //$NON-NLS-1$

        Label default_ratio_label = new Label();
        //time_ratio_label.setEffect(blend);
        default_ratio_label.setStyle("-fx-text-fill: #206982;"
        			+ "-fx-font-size: 12px;"
        		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
        			+ "-fx-font-weight: normal;");
        //default_ratio_label.setPadding(new Insets(1, 1, 1, 5));
        default_ratio_label.setAlignment(Pos.CENTER);
		//s0.append((int)initial_time_ratio).append(DEFAULT).append(default_ratio).append(CLOSE_PAR);
		s0.append(default_ratio);
		default_ratio_label.setText(s0.toString());
		setQuickToolTip(default_ratio_label, "e.g. if 128, then 1 real second equals 128 sim seconds"); //$NON-NLS-1$


        Label real_time_label0 = new Label(SEC);
        //real_time_label0.setEffect(blend);
        real_time_label0.setAlignment(Pos.CENTER_RIGHT);
        real_time_label0.setStyle("-fx-text-fill: #065185;"
        			+ "-fx-font-size: 12px;"
        		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
        			+ "-fx-font-weight: italic;");
        real_time_label0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(real_time_label0, "the amount of simulation time per real second"); //$NON-NLS-1$


        Label real_time_label = new Label();
        //real_time_label.setEffect(blend);
        real_time_label.setStyle("-fx-text-fill: #065185;"
        			+ "-fx-font-size: 12px;"
        		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
        			+ "-fx-font-weight: italic;");
        //real_time_label.setPadding(new Insets(1, 1, 1, 5));
        real_time_label.setAlignment(Pos.CENTER);
		setQuickToolTip(real_time_label, "e.g. 02m.08s means that 1 real second equals 2 real minutes & 8 real seconds"); //$NON-NLS-1$


		StringBuilder s1 = new StringBuilder();
		double ratio = masterClock.getTimeRatio();
		//String factor = String.format(Msg.getString("TimeWindow.timeFormat"), ratio); //$NON-NLS-1$
		s1.append(masterClock.getTimeTruncated(ratio));
		real_time_label.setText(s1.toString());
/*
		// detect dragging
        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {

            	if (old_val != new_val) {

	            	double sliderValue = new_val.doubleValue();

	            	if (default_ratio <= 64)
	            		newTimeRatio = Math.pow(2, (int)sliderValue - 1);
	            	else if (default_ratio <= 128)
	            		newTimeRatio = Math.pow(2, (int)sliderValue);
	            	else if (default_ratio <= 256)
	            		newTimeRatio = Math.pow(2, (int)sliderValue + 1);
	            	else if (default_ratio <= 512)
	            		newTimeRatio = Math.pow(2, (int)sliderValue + 2);

	            	//System.out.println("sliderValue : " + sliderValue + "  newTimeRatio : " + newTimeRatio);

					masterClock.setTimeRatio(newTimeRatio);

	            	//StringBuilder s0 = new StringBuilder();
					//s0.append((int)newTimeRatio).append(DEFAULT).append(default_ratio).append(CLOSE_PAR);
					//time_ratio_label.setText(s0.toString());

					StringBuilder s1 = new StringBuilder();
					s1.append(masterClock.getTimeTruncated(newTimeRatio));
					real_time_label.setText(s1.toString());

            	}
            }
        });
*/
		// TODO: add pause radio box

		spinner = new Spinner();
		spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        //spinner.setValueFactory(new SpinnerValueFactory.IntSpinnerValueFactory(0, 10));

		List<Integer> items = null;
		if (default_ratio == 16)
			items = FXCollections.observableArrayList(1,2,4,8,16,
																32,64,128,256,512);
		else if (default_ratio == 32)
			items = FXCollections.observableArrayList(1,2,4,8,16,
																32,64,128,256,512);//,1024);
		else if (default_ratio == 64)
			items = FXCollections.observableArrayList(1,2,4,8,16,
																32,64,128,256,512,
																1024);//, 2048);
		else if (default_ratio == 128)
			items = FXCollections.observableArrayList(1,2,4,8,16,
																32,64,128,256,512,
																1024,2048);//,4096,8192);
		else //if (default_ratio == 256)
			items = FXCollections.observableArrayList(1,2,4,8,16,
																32,64,128,256,512,
																1024,2048,4096);//,8192);

        spinner.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(items));
        spinner.setMaxSize(95, 15);
        spinner.setStyle("-fx-text-fill: #065185;"
    			+ "-fx-font-size: 11px;"
    		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
    			+ "-fx-font-weight:bold;");
        //spinner.setAlignment(Pos.CENTER);
        spinner.getValueFactory().setValue(default_ratio);

        spinner.valueProperty().addListener((o, old_val, new_val) -> {

		    	if (old_val != new_val) {

		        	int value = (int)new_val;
/*
		        	if (default_ratio <= 64)
		        		newTimeRatio = Math.pow(2, (int)value - 1);
		        	else if (default_ratio <= 128)
		        		newTimeRatio = Math.pow(2, (int)value);
		        	else if (default_ratio <= 256)
		        		newTimeRatio = Math.pow(2, (int)value + 1);
		        	else if (default_ratio <= 512)
		        		newTimeRatio = Math.pow(2, (int)value + 2);
*/
		        	newTimeRatio = value;

		        	boolean flag = startPause();
					masterClock.setTimeRatio(newTimeRatio);
					endPause(flag);

		        	//StringBuilder s2 = new StringBuilder();
					//s2.append((int)newTimeRatio);//.append(DEFAULT).append(default_ratio).append(CLOSE_PAR);
					//default_ratio_label.setText(s2.toString());

					StringBuilder s3 = new StringBuilder();
					s3.append(masterClock.getTimeTruncated(newTimeRatio));
					real_time_label.setText(s3.toString());

		    	}
	        }
        );



        Label TPSLabel0 = new Label(TPS);
        //TPSLabel0.setEffect(blend);
        TPSLabel0.setAlignment(Pos.CENTER_RIGHT);
        TPSLabel0.setStyle("-fx-text-fill: #065185;"
    			+ "-fx-font-size: 12px;"
    		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
    			+ "-fx-font-weight: italic;");
        TPSLabel0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(TPSLabel0, "how often the simulation updates the changes"); //$NON-NLS-1$

        TPSLabel = new Label();
        //TPSLabel.setEffect(blend);
        TPSLabel.setStyle("-fx-text-fill: #065185;"
    			+ "-fx-font-size: 12px;"
    		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
    			+ "-fx-font-weight: italic;");
        //TPSLabel.setPadding(new Insets(1, 1, 1, 5));
        TPSLabel.setAlignment(Pos.CENTER);
		TPSLabel.setText(formatter.format(masterClock.getPulsesPerSecond()) + HZ);
		setQuickToolTip(TPSLabel, "e.g. 6.22 Hz means for each second, the simulation is updated 6.22 times"); //$NON-NLS-1$


        Label upTimeLabel0 = new Label(UPTIME);
        //upTimeLabel0.setEffect(blend);
        upTimeLabel0.setAlignment(Pos.CENTER_RIGHT);
        upTimeLabel0.setTextAlignment(TextAlignment.RIGHT);
        upTimeLabel0.setStyle("-fx-text-fill: #065185;"
    			+ "-fx-font-size: 12px;"
    		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
    			+ "-fx-font-weight: italic;");
        upTimeLabel0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(upTimeLabel0, "how long the simulation has been up running"); //$NON-NLS-1$

        upTimeLabel = new Label();
        upTimeLabel.setAlignment(Pos.CENTER);
        //upTimeLabel.setEffect(blend);
        upTimeLabel.setStyle("-fx-text-fill: #065185;"
    			+ "-fx-font-size: 12px;"
    		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
    			+ "-fx-font-weight: italic;");
        upTimeLabel.setPadding(new Insets(1, 1, 1, 2));
        if (uptimer != null)
        	upTimeLabel.setText (uptimer.getUptime());
		setQuickToolTip(upTimeLabel, "e.g. 03m 05s means 3 minutes and 5 seconds"); //$NON-NLS-1$

        Label benchmarkLabel0 = new Label(BENCHMARK);
        //upTimeLabel0.setEffect(blend);
        benchmarkLabel0.setAlignment(Pos.CENTER_RIGHT);
        benchmarkLabel0.setTextAlignment(TextAlignment.RIGHT);
        benchmarkLabel0.setStyle("-fx-text-fill: #065185;"
    			+ "-fx-font-size: 12px;"
    		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
    			+ "-fx-font-weight: italic;");
        benchmarkLabel0.setPadding(new Insets(1, 1, 1, 2));
		setQuickToolTip(benchmarkLabel0, "how well this machine perform in mars-sim \n (the lower the number the better the performance)"); //$NON-NLS-1$

		benchmarkLabel = new Label();
		benchmarkLabel.setAlignment(Pos.CENTER);
        //upTimeLabel.setEffect(blend);
		benchmarkLabel.setStyle("-fx-text-fill: #065185;"
    			+ "-fx-font-size: 12px;"
    		    + "-fx-text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;"
    			+ "-fx-font-weight: italic;");
		benchmarkLabel.setPadding(new Insets(1, 1, 1, 2));
		benchmarkLabel.setText (masterClock.getDiffCache() + "");
		setQuickToolTip(benchmarkLabel, "a real time metric of performance"); //$NON-NLS-1$

        GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("jfx-popup-container");
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(1, 1, 1, 1));
		gridPane.setHgap(1.0);
		gridPane.setVgap(1.0);

		ColumnConstraints right = new ColumnConstraints();
	    right.setPrefWidth(earthTimeButton.getPrefWidth()*.6);
	    ColumnConstraints left = new ColumnConstraints();
	    left.setPrefWidth(earthTimeButton.getPrefWidth()*.4);

		GridPane.setConstraints(spinner, 1, 0);
		GridPane.setConstraints(default_ratio_label, 1, 1);
		GridPane.setConstraints(real_time_label, 1, 2);
		GridPane.setConstraints(TPSLabel, 1, 3);
		GridPane.setConstraints(upTimeLabel, 1, 4);
		GridPane.setConstraints(benchmarkLabel, 1, 5);

		GridPane.setConstraints(spinner_label0, 0, 0);
		GridPane.setConstraints(default_ratio_label0, 0, 1);
		GridPane.setConstraints(real_time_label0, 0, 2);
		GridPane.setConstraints(TPSLabel0, 0, 3);
		GridPane.setConstraints(upTimeLabel0, 0, 4);
		GridPane.setConstraints(benchmarkLabel0, 0, 5);

		GridPane.setHalignment(spinner, HPos.CENTER);
		GridPane.setHalignment(default_ratio_label, HPos.CENTER);
		GridPane.setHalignment(real_time_label, HPos.CENTER);
		GridPane.setHalignment(TPSLabel, HPos.CENTER);
		GridPane.setHalignment(upTimeLabel, HPos.CENTER);
		GridPane.setHalignment(benchmarkLabel, HPos.CENTER);

		GridPane.setHalignment(spinner_label0, HPos.RIGHT);
		GridPane.setHalignment(default_ratio_label0, HPos.RIGHT);
		GridPane.setHalignment(real_time_label0, HPos.RIGHT);
		GridPane.setHalignment(TPSLabel0, HPos.RIGHT);
		GridPane.setHalignment(upTimeLabel0, HPos.RIGHT);
		GridPane.setHalignment(benchmarkLabel0, HPos.RIGHT);

		gridPane.getColumnConstraints().addAll(left, right);
		gridPane.getChildren().addAll(spinner_label0, spinner,
				default_ratio_label0, default_ratio_label,
				real_time_label0, real_time_label,
				TPSLabel0, TPSLabel,
				upTimeLabel0, upTimeLabel,
				benchmarkLabel0, benchmarkLabel);

        speedVBox = new VBox();
		speedVBox.getStyleClass().add("jfx-popup-container");
		speedVBox.setPadding(new Insets(2, 2, 2, 2));
		speedVBox.setAlignment(Pos.CENTER);
        speedVBox.getChildren().addAll(header_label, gridPane); //timeSliderBox
        speedPane.getChildren().addAll(speedVBox);

	}
/*
	public String timeRatioString(int t) {
		String s = null;
		if (t < 10)
			s = "   " + t;
		else if (t < 100)
			s = "  " + t;
		else if (t < 1000)
			s = " " + t;
		else
			s = "" + t;
		return s;
	}
*/
/*	
    public static Label createIconLabel(String iconName, int iconSize){
        return LabelBuilder.create()
                .text(iconName)
                .styleClass("icons")
                .style("-fx-font-size: " + iconSize + "px;")
                .build();
    }
*/
    
    /**
     * Creates and returns the sound popup box
     */
	// 2017-01-25 Added createSoundPopup()
	public void createSoundPopup() {
		//logger.info("MainScene's createSoundPopup() is on " + Thread.currentThread().getName());

		soundBtn = new JFXButton();
		//soundBtn.getStyleClass().add("menu-button");//"button-raised");
		//Icon icon = new Icon("MUSIC");
		//icon.setCursor(Cursor.HAND);
		//icon.setStyle("-fx-background-color: orange;");
		//value.setPadding(new Insets(1));
		//Label bell = createIconLabel("\uf0a2", 15);
		//IconFontFX.register(FontAwesome.getIconFont());
		soundIcon = new IconNode(FontAwesome.BELL_O);
		soundIcon.setIconSize(20);
		//soundIcon.setFill(Color.YELLOW);
		//soundIcon.setStroke(Color.WHITE);

		soundBtn.setMaxSize(20, 20);
		soundBtn.setGraphic(soundIcon);
		setQuickToolTip(soundBtn, "Click to open Sound Panel");

		soundBtn.setOnAction(e -> {
            if (soundPopup.isShowing()) {
            	soundPopup.hide();//close();
            }
            else {
            	soundPopup.show(soundBtn, PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
            }
		});

		soundPane = new StackPane();
		//soundPane.setEffect(blend);
		soundPane.getStyleClass().add("jfx-popup-container");
		soundPane.setAlignment(Pos.CENTER);
		soundPane.setPrefHeight(75);
		soundPane.setPrefWidth(250);

		soundPopup = new JFXPopup(soundPane);

		// Set up a settlement view zoom bar
		soundSlider = new JFXSlider();
		//soundSlider.setEffect(blend);
		soundSlider.getStyleClass().add("jfx-slider");
		//soundSlider.setEffect(blend);
		soundSlider.setPrefWidth(220);
		soundSlider.setPrefHeight(20);
		soundSlider.setPadding(new Insets(0, 15, 0, 15));

		soundSlider.setMin(0);
		soundSlider.setMax(10);
		soundSlider.setValue(convertVolume2Slider(soundPlayer.getVolume()));
		soundSlider.setMajorTickUnit(1);
		//soundSlider.setMinorTickCount();
		soundSlider.setShowTickLabels(true);
		soundSlider.setShowTickMarks(true);
		soundSlider.setSnapToTicks(true);
		soundSlider.setBlockIncrement(.5);
		soundSlider.setOrientation(Orientation.HORIZONTAL);
		soundSlider.setIndicatorPosition(IndicatorPosition.RIGHT);

		setQuickToolTip(soundSlider, "adjust the sound volume"); //$NON-NLS-1$

		Text header_label = createTextHeader("SOUND PANEL");

		Label volumelabel = createBlendLabel("Volume");
		volumelabel.setPadding(new Insets(0,0,5,0));

        muteBox = new CheckBox("mute");
        muteBox.setStyle("-fx-background-color: linear-gradient(to bottom, -fx-base, derive(-fx-base,30%));"
    			+ "-fx-font: bold 9pt 'Corbel';"
				+ "-fx-text-fill: #654b00;");
        //cb.setPadding(new Insets(0,0,0,5));
        muteBox.setAlignment(Pos.CENTER_RIGHT);
        muteBox.setOnAction(s -> {
        	if (muteBox.isSelected()) {
        		// mute it
		        sliderCache = soundSlider.getValue();
		        soundSlider.setValue(0);
        		menuBar.getMuteItem().setSelected(false);
			}
			else {
				// unmute it
				soundSlider.setValue(sliderCache);
        		menuBar.getMuteItem().setSelected(true);
			}
        });

		// detect dragging
        soundSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {

            	if (old_val != new_val) {

	            	float sliderValue = new_val.floatValue();

	            	if (sliderValue <= 0) {
				        soundPlayer.setMute(true);
				        muteBox.setSelected(true);
		        		menuBar.getMuteItem().setSelected(false);
					}
					else {
						soundPlayer.setMute(false);
						soundPlayer.setVolume((float) convertSlider2Volume(sliderValue));
				        muteBox.setSelected(false);
		        		menuBar.getMuteItem().setSelected(true);
					}
            	}
            }
        });

        Label empty = new Label();

        GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("jfx-popup-container");
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(1, 1, 1, 1));
		gridPane.setHgap(1.0);
		gridPane.setVgap(1.0);

		ColumnConstraints c = new ColumnConstraints();
	    c.setPrefWidth(90);
	    //ColumnConstraints mid = new ColumnConstraints();
	    //mid.setPrefWidth(70);
	    //ColumnConstraints left = new ColumnConstraints();
	    //left.setPrefWidth(70);

		GridPane.setConstraints(empty, 0, 0);
		GridPane.setConstraints(volumelabel, 1, 0);
		GridPane.setConstraints(muteBox, 2, 0);

		GridPane.setHalignment(empty, HPos.CENTER);
		GridPane.setHalignment(volumelabel, HPos.CENTER);
		GridPane.setHalignment(muteBox, HPos.RIGHT);

		gridPane.getColumnConstraints().addAll(c, c, c);
		gridPane.getChildren().addAll(volumelabel, empty, muteBox);


        soundVBox = new VBox();
		soundVBox.getStyleClass().add("jfx-popup-container");
		soundVBox.setPadding(new Insets(5, 5, 5, 5));
		soundVBox.setAlignment(Pos.CENTER);
        soundVBox.getChildren().addAll(header_label, gridPane, soundSlider);
        soundPane.getChildren().addAll(soundVBox);

	}

/*	
    public void createFarmPopup() {
		//logger.info("MainScene's createFarmPopup() is on " + Thread.currentThread().getName());

		farmBtn = new JFXButton();
		//farmBtn.getStyleClass().add("menu-button");//"button-raised");
		farmIcon = new IconNode(FontAwesome.LEAF);
		farmIcon.setIconSize(20);
		//soundIcon.setFill(Color.YELLOW);
		//soundIcon.setStroke(Color.WHITE);

		farmBtn.setMaxSize(20, 20);
		farmBtn.setGraphic(farmIcon);
		setQuickToolTip(farmBtn, "Click to open Farming Panel");

		farmBtn.setOnAction(e -> {
            if (farmPopup.isShowing()) {
            	farmPopup.hide();//close();
            }
            else {
            	farmPopup.show(farmBtn, PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
            }
		});

		Accordion acc = new Accordion();
		ObservableList<Settlement> towns = sim.getUnitManager().getSettlementOList();
		int num = towns.size();
		List<TitledPane> titledPanes = new ArrayList<>();
		List<Pane> panes = new ArrayList<>();
		
		for (Settlement s : towns) {
		
			DragDrop dd = new DragDrop();
			StackPane p = dd.createDragDropBox();
			panes.add(p);
			TitledPane tp = new TitledPane(s.getName(), p);
			titledPanes.add(tp);
			
			p.getStyleClass().add("jfx-popup-container");
			p.setAlignment(Pos.CENTER);
			p.setPrefHeight(75);
			p.setPrefWidth(250);

			acc.getPanes().add(tp);
		}
		
		farmPane = new StackPane(acc);

		farmPopup = new JFXPopup(farmPane);
		
    }
*/

	public void createMarsTimeBar() {
		marsTimeButton = new Button();

		marsTimeButton.setMaxWidth(Double.MAX_VALUE);
		if (OS.contains("linux")) {
			marsTimeButton.setMinWidth(LINUX_WIDTH);
			marsTimeButton.setPrefSize(LINUX_WIDTH, 29);
		}
		else if (OS.contains("mac")) {
			marsTimeButton.setMinWidth(MACOS_WIDTH);
			marsTimeButton.setPrefSize(MACOS_WIDTH, 28);
		}
		else {
			marsTimeButton.setMinWidth(WIN_WIDTH);
			marsTimeButton.setPrefSize(WIN_WIDTH, 33);
		}


		if (masterClock == null) {
			masterClock = sim.getMasterClock();
		}

		if (marsClock == null) {
			marsClock = masterClock.getMarsClock();
		}


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

		//Label header_label = createHeader("MARS CALENDAR");
		Text header_label = createTextHeader("MARS CALENDAR PANEL");

		monthText = createBlendText(MONTH + marsClock.getMonthName());
		yearText = createBlendText(ORBIT + marsClock.getOrbitString());

		setQuickToolTip(monthText, "the current Martian month. Each orbit has 24 months with either 27 or 28 Sols");
		setQuickToolTip(yearText, "the current Martian orbit (or year). Note : Martian Orbit 0015 coincides with Earth year 2043 CE"); // The Martian year is referred to as an "orbit". Each orbit has 668.59 Martian sols. It is 668.5921 Martian days ("Sols") long.

		HBox hBox = new HBox();
		hBox.setPadding(new Insets(2, 15, 2, 15));
		hBox.setAlignment(Pos.BOTTOM_CENTER);
		hBox.getChildren().addAll(yearText, monthText);

		Label LsLabel = new Label(SOLAR_LONGITUDE);
		setQuickToolTip(LsLabel, "Solar Longitude (L_s) is the Mars-Sun angle for determining the Martian season");

		orbitInfo = sim.getMars().getOrbitInfo();
		double L_s = orbitInfo.getL_s();
		LSText = createBlendText(Math.round(L_s*100D)/100D + Msg.getString("direction.degreeSign"));	 //$NON-NLS-1$
		setQuickToolTip(LSText, "L_s [in degrees] e.g. For Northern hemisphere, Spring equinox at L_s=0; Winter solstice at L_s=270.");// Summer solstice at L_s = 90. Autumn equinox at L_s = 180.");

		HBox LsBox = new HBox();
		LsBox.setPadding(new Insets(2, 2, 2, 2));
		LsBox.setAlignment(Pos.BOTTOM_CENTER);
		LsBox.getChildren().addAll(LsLabel, LSText);

		Label northLabel = new Label(NORTH);
		Label southLabel = new Label(SOUTH);
		northText = createBlendText( marsClock.getSeason(MarsClock.NORTHERN_HEMISPHERE));
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
		//noteText = new Text();
		//noteLabel.setEffect(blend);
		noteLabel.setPadding(new Insets(2, 5, 2, 0));
		noteLabel.setStyle("-fx-background-color: linear-gradient(to bottom, -fx-base, derive(-fx-base,30%));"
    			+ "-fx-font-size: 12px;"
				+ "-fx-text-fill: #654b00;");


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
		//marsTimeButton = new Button();//Label();
		//marsTimeButton.setMaxWidth(Double.MAX_VALUE);
		setQuickToolTip(marsTimeButton, "Click to open Martian calendar");
		marsTimeButton.setOnAction(e -> {
            if (marsCalendarPopup.isShowing()) {
            	marsCalendarPopup.hide();//close();
            }
            else {
            	marsCalendarPopup.show(marsTimeButton, PopupVPosition.TOP, PopupHPosition.RIGHT, -20, 25);
            }
		});

		marsTimeButton.setId("rich-orange");
		//marsTimeButton.setTextAlignment(TextAlignment.LEFT);
		marsTimeButton.setAlignment(Pos.CENTER_LEFT);
		//setQuickToolTip(marsTime, "Click to see Quick Info on Mars");

	}

	public void createFXButtons() {

		minimapToggle = new JFXToggleButton();
		//pinButton.setTextFill(Paint.OPAQUE);
		minimapToggle.setText("Minimap Off");
		minimapToggle.setSelected(false);
		setQuickToolTip(minimapToggle, "Pin Minimap");
		minimapToggle.setOnAction(e -> {
			if (minimapToggle.isSelected()) {
				openMinimap();
			}
			else {
				closeMinimap();
				//minimapToggle.setText("Minimap Off");
				//desktop.closeToolWindow(NavigatorWindow.NAME);
				//mapAnchorPane.getChildren().remove(minimapStackPane);
	    	    //minimapButton.setSelected(false);
			}

			//minimapButton.toFront();

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
					//pinButton.setSelected(true);
					//closeMinimap();
					openMinimap();
				}
			}
			else {
				closeSettlementMap();
			}

			//sMapButton.toFront();

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
					//pinButton.setSelected(true);
					openMinimap();
				}
				else
		    	    minimapStackPane.toFront();

				minimapToggle.toFront();
				mapToggle.toFront();
			}
			else
				cacheToggle.setText("Map Cache Off");
		});

		rotateCWBtn = new JFXButton();
		rotateCWBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.cw")))));	 //$NON-NLS-1$
		//IconNode rotateCWIcon = new IconNode(FontAwesome.ar.ARROW_CIRCLE_O_RIGHT);
		//rotateCWIcon.setIconSize(30);
		//rotateCWBtn.setGraphic(rotateCWIcon);
		//Tooltip t0 = new Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.clockwise")); //$NON-NLS-1$
		//rotateCWBtn.setTooltip(t0);
		setQuickToolTip(rotateCWBtn, Msg.getString("SettlementTransparentPanel.tooltip.clockwise"));
		rotateCWBtn.setOnAction(e -> {
			mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE);
		});

		rotateCCWBtn = new JFXButton();
		rotateCCWBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.ccw")))));	//$NON-NLS-1$
		//IconNode rotateCCWIcon = new IconNode(FontAwesome.ARROW_CIRCLE_O_LEFT);
		//rotateCCWIcon.setIconSize(30);
		//rotateCCWBtn.setGraphic(rotateCCWIcon);
		//Tooltip t1 = new Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise")); //$NON-NLS-1$
		//rotateCCWBtn.setTooltip(t1);
		setQuickToolTip(rotateCCWBtn, Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise"));
		rotateCCWBtn.setOnAction(e -> {
			mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE);
		});

		recenterBtn = new JFXButton();
		recenterBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.recenter"))))); //$NON-NLS-1$
		//IconNode recenterIcon = new IconNode(FontAwesome.ALIGN_CENTER);
		//recenterIcon.setIconSize(30);
		//recenterBtn.setGraphic(recenterIcon);
		//Tooltip t2 = new Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.recenter")); //$NON-NLS-1$
		//recenterBtn.setTooltip(t2);
		setQuickToolTip(recenterBtn, Msg.getString("SettlementTransparentPanel.tooltip.recenter"));
		recenterBtn.setOnAction(e -> {
			mapPanel.reCenter();
			zoomSlider.setValue(0);
		});

	}

	public void createFXZoomSlider() {
		//logger.info("MainScene's createFXZoomSlider() is on " + Thread.currentThread().getName() + " Thread");

		// Set up a settlement view zoom bar
		zoomSlider = new JFXSlider();
		zoomSlider.getStyleClass().add("jfx-slider");
		//zoom.setMinHeight(100);
		//zoom.setMaxHeight(200);
		zoomSlider.prefHeightProperty().bind(mapStackPane.heightProperty().multiply(.3d));
		zoomSlider.setMin(-10);
		zoomSlider.setMax(10);
		zoomSlider.setValue(0);
		zoomSlider.setMajorTickUnit(5);
		zoomSlider.setShowTickLabels(true);
		zoomSlider.setShowTickMarks(true);
		zoomSlider.setBlockIncrement(1);
		zoomSlider.setOrientation(Orientation.VERTICAL);
		zoomSlider.setIndicatorPosition(IndicatorPosition.RIGHT);

		setQuickToolTip(zoomSlider, Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$

		// detect dragging on zoom scroll bar
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
            	// Change scale of map based on slider position.
				int sliderValue = (int) new_val.doubleValue();
				double defaultScale = SettlementMapPanel.DEFAULT_SCALE;
				double newScale = defaultScale;
				if (sliderValue > 0) {
					newScale += defaultScale * (double) sliderValue * SettlementTransparentPanel.ZOOM_CHANGE;
				}
				else if (sliderValue < 0) {
					newScale = defaultScale / (1D + ((double) sliderValue * -1D * SettlementTransparentPanel.ZOOM_CHANGE));
				}
				mapPanel.setScale(newScale);
            }
        });
	}


	public void createFXSettlementComboBox() {
		sBox = new JFXComboBox<>();
		//sBox.setAlignment(Pos.CENTER_RIGHT);
		//JFXListView<Settlement> list = new JFXListView<Settlement>();
		sBox.getStyleClass().add("jfx-combo-box");
		setQuickToolTip(sBox, Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		//ObservableList<Settlement> names = sim.getUnitManager().getSettlementOList();
		sBox.itemsProperty().setValue(sim.getUnitManager().getSettlementOList());
		sBox.setPromptText("Select a settlement to view");
		sBox.getSelectionModel().selectFirst();

		sBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != newValue) {
				SwingUtilities.invokeLater(() -> mapPanel.setSettlement((Settlement)newValue));
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
		//mapLabelBox.setAlignment(Pos.CENTER_RIGHT);

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
		//logger.info("MainScene's createJFXTabs() is on " + Thread.currentThread().getName() + " Thread");

		jfxTabPane = new JFXTabPane();
		jfxTabPane.setPrefSize(sceneHeight.get(),sceneWidth.get());

		String cssFile = null;

        if (theme == 0 || theme == 6)
        	cssFile = "/fxui/css/jfx_blue.css";
        else
        	cssFile = "/fxui/css/jfx_orange.css";

		jfxTabPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		jfxTabPane.getStyleClass().add("jfx-tab-pane");

		mainTab = new Tab();
		mainTab.setText("Main");
		mainTab.setContent(mainAnchorPane);

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
/*
		miniMapBtn = new JFXButton();
		setQuickToolTip(miniMapBtn, "Open Mars Navigator Minimap below");
		miniMapBtn.setOnAction(e -> {

			if (desktop.isToolWindowOpen(NavigatorWindow.NAME)) {
				desktop.closeToolWindow(NavigatorWindow.NAME);
				mapAnchorPane.getChildren().remove(minimapStackPane);
				minimapButton.setSelected(false);
				minimapButton.setText("Minimap Off");
			}

			else {
				openMinimap();
			}
		});
*/
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
						zoomSlider.setValue( (zoomSlider.getValue() - 1));
				}
				else if (direction < 0) {
					// Move zoom slider up.
					if (zoomSlider.getValue() < zoomSlider.getMax())
						zoomSlider.setValue( (zoomSlider.getValue() + 1));
				}
                //event.consume();
            }
        });

		//desktop.openToolWindow(MonitorWindow.NAME);
		//desktop.openToolWindow(MissionWindow.NAME);
		//desktop.openToolWindow(ResupplyWindow.NAME);
		//desktop.openToolWindow(ScienceWindow.NAME);


		// set up monitor tab
		//MonitorWindow monWin = (MonitorWindow) desktop.getToolWindow(MonitorWindow.NAME);
		//monNode = new SwingNode();
		//monNode.setContent(monWin);
		//monPane = new StackPane(monNode);

		//desktop.openToolWindow(MonitorWindow.NAME);

/*
 *
		// set up mission tab
		MissionWindow missionWin = (MissionWindow) desktop.getToolWindow(MissionWindow.NAME);
		missionNode = new SwingNode();
	    JDesktopPane d1 = desktops.get(1);
	    d1.add(missionWin);
		missionNode.setContent(d1);
		StackPane missionPane = new StackPane(missionNode);
		Tab missionTab = new Tab();
		missionTab.setText("Mission");
		missionTab.setContent(missionPane);

		//desktop.openToolWindow(MissionWindow.NAME);


		// set up resupply tab
		ResupplyWindow resupplyWin = (ResupplyWindow) desktop.getToolWindow(ResupplyWindow.NAME);
		resupplyNode = new SwingNode();
	    JDesktopPane d2 = desktops.get(2);
	    d2.add(resupplyWin);
		resupplyNode.setContent(d2);
		StackPane resupplyPane = new StackPane(resupplyNode);
		Tab resupplyTab = new Tab();
		resupplyTab.setText("Resupply");
		resupplyTab.setContent(resupplyPane);

		//desktop.openToolWindow(ResupplyWindow.NAME);

		// set up science tab
		ScienceWindow sciWin = (ScienceWindow) desktop.getToolWindow(ScienceWindow.NAME);
		sciNode = new SwingNode();
		// Note: don't need to create a DesktopPane for scienceWin
	    //JDesktopPane d4 = desktops.get(4);
	    //d4.add(scienceWin);
		//scienceNode.setContent(d4);
		sciNode.setContent(sciWin);
		StackPane sciencePane = new StackPane(sciNode);
		Tab scienceTab = new Tab();
		scienceTab.setText("Science");
		scienceTab.setContent(sciencePane);

		//desktop.openToolWindow(ScienceWindow.NAME);
*/

        
		// set up help tab
		//GuideWindow guideWin = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
		//guideNode = new SwingNode();
		//guideNode.setContent(guideWin);
		
        BrowserJFX helpBrowser = desktop.getBrowserJFX();
        //StackPane guidePane = new StackPane(guideNode);
        StackPane guidePane = new StackPane(helpBrowser.getBorderPane());
        Tab guideTab = new Tab();
		guideTab.setText("Help");
		guideTab.setContent(guidePane);

		//desktop.openToolWindow(GuideWindow.NAME);

		jfxTabPane.getTabs().addAll(
				mainTab,
				mapTab,
				//monTab,
				//missionTab,
				//resupplyTab,
				//scienceTab,
				guideTab);


		jfxTabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {

			if (newTab == mainTab) {
				mainAnchorPane.requestFocus();
				closeMaps();
				desktop.closeToolWindow(GuideWindow.NAME);
				//anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
				//anchorMapTabPane.getChildren().removeAll(cacheButton);
			}
/*
			else if (newTab == monTab) {
				if (!desktop.isToolWindowOpen(MonitorWindow.NAME)) {
					desktop.openToolWindow(MonitorWindow.NAME);
					//monNode.setContent(monWin);
				}

				anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
				anchorMapTabPane.getChildren().removeAll(cacheButton);

			}
*/
			else if (newTab == mapTab) {

/*
				if (!desktop.isToolWindowOpen(SettlementWindow.NAME)) {
					System.out.println("settlement map was closed");
					if (isCacheButtonOn())
						mapBtn.fire();
				}

				if (!desktop.isToolWindowOpen(NavigatorWindow.NAME)) {
					System.out.println("minimap was closed");
					if (isCacheButtonOn()) {
						miniMapBtn.fire();
						if (nw == null) nw = (NavigatorWindow) desktop.getToolWindow(NavigatorWindow.NAME);
						nw.getGlobeDisplay().drawSphere();//updateDisplay();
					}
				}
*/

/*
				AnchorPane.setRightAnchor(mapBtn, 125.0);
				if (OS.contains("win"))
					AnchorPane.setTopAnchor(mapBtn, 0.0);
				else
					AnchorPane.setTopAnchor(mapBtn, -3.0);
				rootAnchorPane.getChildren().addAll(mapBtn);

		        AnchorPane.setRightAnchor(miniMapBtn, 165.0);
				if (OS.contains("win"))
					AnchorPane.setTopAnchor(miniMapBtn, 0.0);
				else
					AnchorPane.setTopAnchor(miniMapBtn, -3.0);
				rootAnchorPane.getChildren().addAll(miniMapBtn);
*/
		        AnchorPane.setRightAnchor(cacheToggle, 25.0);
		        AnchorPane.setTopAnchor(cacheToggle, 45.0);  // 55.0

		        AnchorPane.setLeftAnchor(minimapToggle, 10.0);
		        AnchorPane.setTopAnchor(minimapToggle, 10.0);  // 55.0

		        AnchorPane.setRightAnchor(mapToggle, 15.0);
		        AnchorPane.setTopAnchor(mapToggle, 10.0);  // 55.0

				mapAnchorPane.getChildren().addAll(cacheToggle, minimapToggle, mapToggle);

				desktop.closeToolWindow(GuideWindow.NAME);

				//rootAnchorPane.getChildren().remove(monPane);
				//desktop.closeToolWindow(MonitorWindow.NAME);
			}

			else if (newTab == guideTab) {

				if (!desktop.isToolWindowOpen(GuideWindow.NAME))
					desktop.openToolWindow(GuideWindow.NAME);

				closeMaps();
				//anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
			    //anchorMapTabPane.getChildren().removeAll(cacheButton);
			}
/*
			else if (newTab == missionTab) {
				if (!desktop.isToolWindowOpen(MissionWindow.NAME)) {
					desktop.openToolWindow(MissionWindow.NAME);
					//missionNode.setContent(missionWin);
				}

				anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
			    anchorMapTabPane.getChildren().removeAll(cacheButton);
			}

			else if (newTab == resupplyTab) {
				if (!desktop.isToolWindowOpen(ResupplyWindow.NAME)) {
					desktop.openToolWindow(ResupplyWindow.NAME);
					//resupplyNode.setContent(resupplyWin);
				}

				anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
			    anchorMapTabPane.getChildren().removeAll(cacheButton);
			}

			else if (newTab == scienceTab) {
				if (!desktop.isToolWindowOpen(ScienceWindow.NAME)) {
					desktop.openToolWindow(ScienceWindow.NAME);
					//sciNode.setContent(scienceWin);
				}

				anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
			    anchorMapTabPane.getChildren().removeAll(cacheButton);
			}
*/
			else {
				//rootAnchorPane.getChildren().removeAll(miniMapBtn, mapBtn);
			    mapAnchorPane.getChildren().removeAll(cacheToggle, minimapToggle, mapToggle);
			}

		});

		//jfxTabPane.getSelectionModel().select(guideTab);

		// NOTE: if a tab is NOT selected, should close that tool as well to save cpu utilization
		// this is done in ToolWindow's update(). It allows for up to 1 second of delay, in case user open and close the same repeated.

	}

	public void openMinimap() {
		//System.out.println("openMinimap()");
		//if (!desktop.isToolWindowOpen(NavigatorWindow.NAME)) {
			desktop.openToolWindow(NavigatorWindow.NAME);
			//System.out.println("navWin : " + navWin.hashCode());

			//navWin = (NavigatorWindow) desktop.getToolWindow(NavigatorWindow.NAME);
			//System.out.println("navWin : " + navWin.hashCode());
			//minimapNode.setContent(navWin);
			//minimapNode.setStyle("-fx-background-color: black; ");
/*
			minimapNode = new SwingNode();
			minimapStackPane = new StackPane(minimapNode);
			minimapNode.setContent(navWin);
			minimapStackPane.setStyle("-fx-background-color: black; ");
			minimapNode.setStyle("-fx-background-color: black; ");
*/

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
			navWin.getGlobeDisplay().drawSphere();//updateDisplay();
    	    navWin.toFront();
    	    navWin.requestFocus();
    	    minimapStackPane.toFront();
    	    minimapToggle.setSelected(true);
			minimapToggle.setText("Minimap On");
    	    minimapToggle.toFront();
		//}
	}

	public void openSettlementMap() {

		mapStackPane.prefWidthProperty().unbind();
		mapStackPane.prefWidthProperty().bind(scene.widthProperty().subtract(1));

		desktop.openToolWindow(SettlementWindow.NAME);
		//mapNode.setContent(settlementWindow);

        AnchorPane.setRightAnchor(mapStackPane, 0.0);
        AnchorPane.setTopAnchor(mapStackPane, 0.0);

        AnchorPane.setRightAnchor(zoomSlider, 65.0);
        AnchorPane.setTopAnchor(zoomSlider, 350.0);//(mapNodePane.heightProperty().get() - zoomSlider.heightProperty().get())*.4d);

        AnchorPane.setRightAnchor(rotateCWBtn, 110.0);
        AnchorPane.setTopAnchor(rotateCWBtn, 300.0);

        AnchorPane.setRightAnchor(rotateCCWBtn, 30.0);
        AnchorPane.setTopAnchor(rotateCCWBtn, 300.0);

        AnchorPane.setRightAnchor(recenterBtn, 70.0);
        AnchorPane.setTopAnchor(recenterBtn, 300.0);

        AnchorPane.setRightAnchor(settlementBox, 15.0);//anchorMapTabPane.widthProperty().get()/2D - 110.0);//settlementBox.getWidth());
        AnchorPane.setTopAnchor(settlementBox, 100.0);

        AnchorPane.setRightAnchor(mapLabelBox, -10.0);
        AnchorPane.setTopAnchor(mapLabelBox, 140.0);


        boolean hasMap = false, hasZoom = false, hasButtons = false,
        		hasSettlements = false, hasMapLabel = false;

        ObservableList<Node> nodes = mapAnchorPane.getChildrenUnmodifiable();

        for (Node node : nodes) {

	        if (node == settlementBox) {
	        	hasSettlements = true;
	        }
	        else if (node == mapStackPane) {
	        	hasMap = true;
	        }
	        else if (node == zoomSlider) {
	        	hasZoom = true;
	        }
	        else if (node == recenterBtn || node == rotateCWBtn || node == rotateCCWBtn) {
	        	hasButtons = true;
	        }
	        else if (node == mapLabelBox)
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
			for (Node node : mapAnchorPane.getChildrenUnmodifiable()) {
		        if (node == cacheButton) {
		        	node.toFront();
		        }
		        else if (node == minimapButton) {
		        	node.toFront();
		        }
		        else if (node == sMapButton) {
		        	node.toFront();
		        }
		        else if (node == settlementBox) {
		        	node.toFront();
		        }
		        else if (node == mapLabelBox) {
		        	node.toFront();
		        }
		    }
*/
		mapLabelBox.toFront();
		settlementBox.toFront();
		mapToggle.toFront();
		cacheToggle.toFront();
		minimapToggle.toFront();

		mapToggle.setText("Settlement Map On");
		mapToggle.setSelected(true);

	}

	public void closeMinimap() {
		//System.out.println("closeMinimap()");
		desktop.closeToolWindow(NavigatorWindow.NAME);
		Platform.runLater(() -> {
			//addNavWin();
			mapAnchorPane.getChildren().remove(minimapStackPane);
			minimapToggle.setSelected(false);
			minimapToggle.setText("Minimap Off");
		    jfxTabPane.requestFocus();
		});
		//System.out.println("closing minimap...");
	}

	public void closeSettlementMap() {
		desktop.closeToolWindow(SettlementWindow.NAME);
		Platform.runLater(() -> {
			mapAnchorPane.getChildren().removeAll(mapStackPane,
					settlementBox, mapLabelBox,
					zoomSlider,
					rotateCWBtn, rotateCCWBtn, recenterBtn);
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
				mapAnchorPane.getChildren().removeAll(minimapStackPane, mapStackPane, zoomSlider, rotateCWBtn, rotateCCWBtn, recenterBtn, settlementBox, mapLabelBox);
				//System.out.println("closing both maps...");
				minimapToggle.setSelected(false);
				minimapToggle.setText("Minimap Off");
				mapToggle.setSelected(false);
				mapToggle.setText("Settlement Map Off");
			});
		}
	    jfxTabPane.requestFocus(); // rootAnchorPane //jfxTabPane
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
		//logger.info("MainScene's initializeTheme()");
		// NOTE: it is mandatory to change the theme from 1 to 2 below at the start of the sim
		// This avoids two display issues:
		// (1). the crash of Mars Navigator Tool when it was first loaded
		// (2). the inability of loading the tab icons of the Monitor Tool at the beginning
		// Also, when clicking a tab at the first time, a NullPointerException results)
		// TODO: find out if it has to do with nimrodlf and/or JIDE-related
/*
		if (OS.contains("linux")) {
			setTheme(0);
		}
		else {
			setTheme(7);
		}
*/
		setTheme(choice_theme); // 0 = nimbus
		
		//logger.info("done with MainScene's initializeTheme()");
	}

	/*
	 * Sets the theme skin of the desktop
	 */
	public void setTheme(int theme) {

		if (menuBar.getStylesheets() != null)
			menuBar.getStylesheets().clear();
			
		String cssFile;

		if (this.theme != theme) {
			this.theme = theme;

			if (theme == 0) { //  snow blue
				// for numbus theme
				cssFile = "/fxui/css/snowBlue.css";
				updateThemeColor(0, Color.rgb(0,107,184), Color.rgb(0,107,184), cssFile); // CADETBLUE // Color.rgb(23,138,255)
				themeSkin = "numbus";
				
				// see https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/color.html

			} else if (theme == 1) { // olive green
				cssFile = "/fxui/css/oliveskin.css";
				updateThemeColor(1, Color.GREEN, Color.PALEGREEN, cssFile); //DARKOLIVEGREEN
				themeSkin = "LightTabaco";

			} else if (theme == 2) { // burgundy red
				cssFile = "/fxui/css/burgundyskin.css";
				updateThemeColor(2, Color.rgb(140,0,26), Color.YELLOW, cssFile); // ORANGERED
				themeSkin = "Burdeos";

			} else if (theme == 3) { // dark chocolate
				cssFile = "/fxui/css/darkTabaco.css";
				updateThemeColor(3, Color.DARKGOLDENROD, Color.BROWN, cssFile);
				themeSkin = "DarkTabaco";

			} else if (theme == 4) { // grey
				cssFile = "/fxui/css/darkGrey.css";
				updateThemeColor(4, Color.DARKSLATEGREY, Color.DARKGREY, cssFile);
				themeSkin = "DarkGrey";

			} else if (theme == 5) { // + purple
				cssFile = "/fxui/css/nightViolet.css";
				updateThemeColor(5, Color.rgb(73,55,125), Color.rgb(73,55,125), cssFile); // DARKMAGENTA, SLATEBLUE
				themeSkin = "Night";

			} else if (theme == 6) { // + skyblue

				cssFile = "/fxui/css/snowBlue.css";
				updateThemeColor(6, Color.rgb(0,107,184), Color.rgb(255,255,255), cssFile); //(144, 208, 229) light blue // CADETBLUE (0,107,184)// Color.rgb(23,138,255)
				themeSkin = "snowBlue";

			} else if (theme == 7) { // mud orange/standard

				cssFile = "/fxui/css/nimrodskin.css";
				updateThemeColor(7, Color.rgb(156,77,0), Color.rgb(255,255,255), cssFile); //DARKORANGE, CORAL
				themeSkin = "nimrod";

			}
			
			//SwingUtilities.invokeLater(() -> setLookAndFeel(NIMBUS_THEME));		

			SwingUtilities.invokeLater(() -> {
				// 2016-06-17 Added checking for OS.
				if (OS.contains("linux")) {
					// Note: NIMROD theme lib doesn't work on linux
					setLookAndFeel(NIMBUS_THEME);
				}
				else {
					if (theme == 0 || theme == 6)
						setLookAndFeel(NIMBUS_THEME);
					else if (theme == 7)
						setLookAndFeel(NIMROD_THEME);
				}
			});

		}

	
	}

	/**
	 * Sets the look and feel of the UI
	 * @param choice
	 */
	// 2015-05-02 Edited setLookAndFeel()
	public void setLookAndFeel(int choice) {
		//logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
		boolean changed = false;
		if (choice == SYSTEM_THEME) { // theme == "nativeLookAndFeel"
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
				//themeSkin = "system";
				//System.out.println("found system");
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} 
		
		else if (choice == NIMROD_THEME) { // theme == "nimRODLookAndFeel"
			try {
				NimRODTheme nt = new NimRODTheme(getClass().getClassLoader().getResource("theme/" + themeSkin + ".theme"));
				//NimRODLookAndFeel.setCurrentTheme(nt); // must be declared non-static or not working if switching to a brand new .theme file
				NimRODLookAndFeel nf = new NimRODLookAndFeel();
				nf.setCurrentTheme(nt); //must be declared non-static or not working if switching to a brand new .theme file
				UIManager.setLookAndFeel(nf);
				changed = true;
				//System.out.println("found Nimrod");

			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		}

		else if (choice == NIMBUS_THEME) {
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
						//themeSkin = "nimbus";
						changed = true;
						//break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					//themeSkin = "metal";
					changed = true;
					//System.out.println("found metal");
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
			}
		}

		if (changed) {
			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateUnitWindowLF();
				//SwingUtilities.updateComponentTreeUI(desktop);
				//desktop.updateAnnouncementWindowLF();
				// desktop.updateTransportWizardLF();
				//System.out.println("just updated UI");
			}
		}
		//logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
	}

	/*
	 * Updates the theme colors of statusBar, swingPane and menuBar
	 */
	// 2015-08-29 Added updateThemeColor()
	public void updateThemeColor(int theme, Color txtColor, Color txtColor2, String cssFile) {
		mainAnchorPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		if (!OS.contains("mac"))
			menuBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

		// Note : menu bar color
		// orange theme : F4BA00
		// blue theme : 3291D2

		//String color = txtColor.replace("0x", "");

		jfxTabPane.getStylesheets().clear();

		setStylesheet(marsTimeButton, cssFile);
		setStylesheet(earthTimeButton, cssFile);

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

		//setStylesheet(timeSlider, cssFile);
		setStylesheet(zoomSlider, cssFile);
		setStylesheet(soundSlider, cssFile);


		if (settlementWindow == null) {
			settlementWindow = (SettlementWindow)(desktop.getToolWindow(SettlementWindow.NAME));
			if (settlementWindow != null) {
				settlementWindow.setTheme(txtColor);
				settlementWindow.setStatusBarTheme(cssFile);
			}
		}

		else {
			settlementWindow.setTheme(txtColor);
			settlementWindow.setStatusBarTheme(cssFile);
		}

		if (theme == 7) {
			speedIcon.setFill(Color.YELLOW);
			speedBtn.setGraphic(speedIcon);
			marsNetIcon.setFill(Color.YELLOW);
			marsNetBtn.setGraphic(marsNetIcon);
			soundIcon.setFill(Color.YELLOW);
			soundBtn.setGraphic(soundIcon);
			//farmIcon.setFill(Color.YELLOW);
			//farmBtn.setGraphic(farmIcon);
			jfxTabPane.getStylesheets().add(getClass().getResource("/fxui/css/jfx_orange.css").toExternalForm());
		}

		else {
			speedIcon.setFill(Color.LAVENDER);
			speedBtn.setGraphic(speedIcon);
			marsNetIcon.setFill(Color.LAVENDER);
			marsNetBtn.setGraphic(marsNetIcon);
			soundIcon.setFill(Color.LAVENDER);
			soundBtn.setGraphic(soundIcon);
			//farmIcon.setFill(Color.LAVENDER);
			//farmBtn.setGraphic(farmIcon);
			jfxTabPane.getStylesheets().add(getClass().getResource("/fxui/css/jfx_blue.css").toExternalForm());
		}

		chatBox.update();

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
		// Note: Infinite Timeline might result in a memory leak if not stopped properly.
		// All the objects with animated properties would not be garbage collected.
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();

	}


    /**
     * Creates and returns a {@link Flyout}
     * @return  a new {@link Flyout}
     */
    //2015-11-11 Added createFlyout()
    public JFXPopup createFlyout() {
     	marsNetBtn = new JFXButton();
     	//marsNetBtn.getStyleClass().add("menu-button");//"button-raised");
		marsNetIcon = new IconNode(FontAwesome.COMMENTING_O);
		marsNetIcon.setIconSize(20);
		//marsNetIcon.setStroke(Color.WHITE);

        //marsNetButton.setId("marsNetButton");
        //marsNetButton.setPadding(new Insets(0, 0, 0, 0)); // Warning : this significantly reduce the size of the button image
		setQuickToolTip(marsNetBtn, "Click to open MarsNet Chat Box");

		marsNetBox = new JFXPopup(createChatBox());
		//rootAnchorPane.getChildren().add(marsNetBox);
		marsNetBox.setOpacity(.9);
		//marsNetBox.setPopupContainer(rootAnchorPane);
		//marsNetBox.setSource(marsNetBtn);

		//chatBox.update();
        marsNetBtn.setOnAction(e -> {
            if (!flag)
            	chatBox.update();

            if (marsNetBox.isShowing()) {//.isVisible()) {
                marsNetBox.hide();//.close();
            }
            else {
            	openChatBox();
            }

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


    //public void ToggleMarsNetButton(boolean value) {
    //	marsNetButton.setSelected(value);
    //}

    //public boolean isToggleMarsNetButtonSelected() {
    //	return marsNetButton.isSelected();
    //}

    //public void fireMarsNetButton() {
    //	marsNetButton.fire();
    //}

    public JFXPopup getFlyout() {
    	return marsNetBox;
    }

    /*
     * Creates a chat box
     * @return StackPane
     */
    //2015-11-11 Added createChatBox()
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
		//lastSaveBar = new HBox();
		//lastSaveBar.setPadding(new Insets(5,5,5,5));

		//2016-09-15 Added oldLastSaveStamp
		oldLastSaveStamp = sim.getLastSave();
		oldLastSaveStamp = oldLastSaveStamp.replace("_", " ");

		lastSaveLabel = new Label();
		lastSaveLabel.setId("save-label");
		lastSaveLabel.setMaxWidth(Double.MAX_VALUE);
		//lastSaveLabel.setMinWidth(220);
        if (OS.contains("linux")) {
            lastSaveLabel.setMinWidth(LINUX_WIDTH);
            lastSaveLabel.setPrefSize(LINUX_WIDTH, 29);
        }
        else if (OS.contains("mac")) {
            lastSaveLabel.setMinWidth(MACOS_WIDTH);
            lastSaveLabel.setPrefSize(MACOS_WIDTH, 28);
        }
        else {
            lastSaveLabel.setMinWidth(WIN_WIDTH);
            lastSaveLabel.setPrefSize(WIN_WIDTH, 33);
        }
		//lastSaveLabel.setPrefSize(220, 20);
        lastSaveLabel.setAlignment(Pos.CENTER_LEFT);
		lastSaveLabel.setTextAlignment(TextAlignment.LEFT);
		lastSaveLabel.setText(LAST_SAVED + oldLastSaveStamp);

		setQuickToolTip(lastSaveLabel, "Last time when the sim was (auto)saved");

		//lastSaveBar.getChildren().add(lastSaveLabel);
/*
		memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
		memUsed = memTotal - memFree;
*/

	}


	/*
	 * Creates the status bar for MainScene

	public StatusBar createStatusBar() {
		if (statusBar == null) {
			statusBar = new StatusBar();
			statusBar.setText("");
		}

		//2016-09-15 Added oldLastSaveStamp
		oldLastSaveStamp = sim.instance().getLastSave();
		oldLastSaveStamp = oldLastSaveStamp.replace("_", " ");

		lastSaveText = new Label();
		lastSaveText.setText("Last Saved : " + oldLastSaveStamp + " ");
		lastSaveText.setStyle("-fx-text-inner-color: orange;");
		lastSaveText.setTooltip(new Tooltip ("Time last saved/autosaved on your machine"));

		statusBar.getLeftItems().add(new Separator(VERTICAL));
		statusBar.getLeftItems().add(lastSaveText);
		statusBar.getLeftItems().add(new Separator(VERTICAL));

		memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
		memUsed = memTotal - memFree;

		statusBar.getRightItems().add(new Separator(VERTICAL));

		if (masterClock == null) {
			masterClock = Simulation.instance().getMasterClock();
		}

		if (earthClock == null) {
			earthClock = masterClock.getEarthClock();
		}

		timeText = new Label();
		timeText.setText("  " + timeStamp + "  ");
		timeText.setStyle("-fx-text-inner-color: orange;");
		timeText.setTooltip(new Tooltip ("Earth Date/Time"));

		statusBar.getRightItems().add(timeText);
		statusBar.getRightItems().add(new Separator(VERTICAL));

		return statusBar;
	}


	public NotificationPane getNotificationPane() {
		return notificationPane;
	}

	public Node createNotificationPane() {
		// wrap the dndTabPane inside notificationNode
		notificationPane = new NotificationPane(mainAnchorPane);

		String imagePath = getClass().getResource("/notification/notification-pane-warning.png").toExternalForm();
		ImageView image = new ImageView(imagePath);
		notificationPane.setGraphic(image);
		notificationPane.getActions().addAll(new Action("Close", ae -> {
			// do sync, then hide...
			notificationPane.hide();
		} ));

		notificationPane.setShowFromTop(false);
		notificationPane.setText("Breaking news for mars-simmers !!");
		return notificationPane;
	}


	public String getSampleName() {
		return "Notification Pane";
	}

	public String getControlStylesheetURL() {
		return "/org/controlsfx/control/notificationpane.css";
	}
*/

	/*
	 * Updates Earth and Mars time label in the earthTimeBar and marsTimeBar
	 */
	public void updateTimeLabels() {

		calendarDisplay.update();

		TPSLabel.setText(formatter.format(masterClock.getPulsesPerSecond()) + HZ);

		upTimeLabel.setText(uptimer.getUptime());

		benchmarkLabel.setText (masterClock.getDiffCache() + "");

		int solElapsed = marsClock.getMissionSol();
		if (solElapsed != solElapsedCache) {

			if (solElapsed == 1) {
				String mn = marsClock.getMonthName();
				if (mn != null) {
					monthText.setText(MONTH + mn);
					if (mn.equals(ADIR)) {
						yearText.setText(ORBIT + marsClock.getOrbitString());
					}
				}
			}

			solElapsedCache = solElapsed;
		}

		double L_s = orbitInfo.getL_s();
		LSText.setText(Math.round(L_s*100D)/100D + Msg.getString("direction.degreeSign"));

		if (L_s > 68 && L_s < 72) {
			noteLabel.setText(NOTE_MARS + APHELION);
			//noteLabel.setEffect(blend);
		}
		else if (L_s > 248 && L_s < 252) {
			noteLabel.setText(NOTE_MARS + PERIHELION);
			//noteLabel.setEffect(blend);
		}
		else
			noteLabel.setEffect(null);

		northText.setText(marsClock.getSeason(MarsClock.NORTHERN_HEMISPHERE));
		southText.setText(marsClock.getSeason(MarsClock.SOUTHERN_HEMISPHERE));

		StringBuilder m = new StringBuilder();
        m.append(MARS_DATE_TIME).append(marsClock.getDateString()).append(ONE_SPACE)
        	.append(marsClock.getTrucatedTimeStringUMST());
		marsTimeButton.setText(m.toString());

		StringBuilder e = new StringBuilder();
        e.append(EARTH_DATE_TIME).append(earthClock.getTimeStampF1());
		earthTimeButton.setText(e.toString());

		//2017-05-03 Add triggering the display of pause pane when autosaving
		if (masterClock.getAutosave()) {
			saveSimulation(Simulation.AUTOSAVE);
			masterClock.setAutosave(false);
		}

		//2016-09-15 Added oldLastSaveStamp and newLastSaveStamp
		if (sim.getJustSaved()) {
			String newLastSaveStamp = sim.getLastSave();
			if (!oldLastSaveStamp.equals(newLastSaveStamp)) {
				sim.setJustSaved(false);
				oldLastSaveStamp = newLastSaveStamp.replace("_", " ");
				lastSaveLabel.setText(LAST_SAVED + oldLastSaveStamp);
				//System.out.print("updated last save time stamp");
			}
		}
	}

	/**
	 * Gets the main desktop panel.
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
	 */
	public void newSimulation() {
		//logger.info("MainScene's newSimulation() is on " + Thread.currentThread().getName() + " Thread");

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

	/**
	 * Performs the process of creating a new simulation.
	 */
	private void newSimulationProcess() {
		//logger.info("MainScene's newSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Starting new sim");
		alert.setHeaderText(Msg.getString("MainScene.new.header"));
		alert.setContentText(Msg.getString("MainScene.new.content"));
		ButtonType buttonTypeOne = new ButtonType("Save on Exit");
		//ButtonType buttonTypeTwo = new ButtonType("End Sim");
		ButtonType buttonTypeCancel = new ButtonType("Back to Sim");//, ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne,
				//buttonTypeTwo,
				buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne) {
			saveOnExit();
		//} else if (result.get() == buttonTypeTwo) {
		//	endSim();
		} else if (result.get() == buttonTypeCancel) {//!result.isPresent())
			return;
		}
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 *
	 * @param type
	 */
	public void saveSimulation(int type) {
		//logger.info("MainScene's saveSimulation() is on " + Thread.currentThread().getName() + " Thread");
		//boolean previous = startPause();
		if (!masterClock.isPaused()) {
			//hideWaitStage(PAUSED);
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
	            protected void succeeded(){
	                super.succeeded();
	                hideWaitStage(SAVING);
	            }
	        };
	        new Thread(task).start();

		}
		//endPause(previous);
	}

	/**
	 * Performs the process of saving a simulation.
	 */
	// 2015-01-08 Added autosave
	private void saveSimulationProcess(int type) {
		//logger.info("MainScene's saveSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");
		fileLocn = null;
		dir = null;
		title = null;

		hideWaitStage(PAUSED);

		// 2015-01-25 Added autosave
		if (type == Simulation.AUTOSAVE) {
			dir = Simulation.AUTOSAVE_DIR;
			masterClock.setSaveSim(Simulation.AUTOSAVE, null);

		} else if (type == Simulation.SAVE_DEFAULT) {
			dir = Simulation.DEFAULT_DIR;
			masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);

		} else if (type == Simulation.SAVE_AS) {
			//masterClock.setPaused(true);
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
					fileLocn = selectedFile;// + Simulation.DEFAULT_EXTENSION;
				else {
					hideWaitStage(PAUSED);
					return;
				}

				//hideWaitStage(PAUSED);
				showWaitStage(SAVING);

		        Task<Void> task = new Task<Void>() {
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
		            protected void succeeded(){
		                super.succeeded();
		                hideWaitStage(SAVING);
		            }
		        };
		        new Thread(task).start();
			});
		}

	}


	public void startPausePopup() {
		//if (messagePopup.numPopups() < 1) {
            // Note: (NOT WORKING) popups.size() is always zero no matter what.
		Platform.runLater(() -> {
		//		messagePopup.popAMessage(PAUSE, ESC_TO_RESUME, " ", stage, Pos.TOP_CENTER, PNotification.PAUSE_ICON)
			boolean hasIt = false;
		    for (Node node : root.getChildrenUnmodifiable()) {
		    	if (node == pausePane) {
		    		hasIt = true;
		    		break;
		    	}
		    }
		    if (!hasIt) {
		    	pausePane.setLayoutX((scene.getWidth()-pausePane.getPrefWidth())/2D);
				pausePane.setLayoutY((scene.getHeight()-pausePane.getPrefHeight())/2D);
		    	root.getChildren().add(pausePane);
		    }
		});

	}

	public void stopPausePopup() {
		Platform.runLater(() -> {
		//	messagePopup.stop()
			boolean hasIt = false;
		    for (Node node : root.getChildrenUnmodifiable()) {
		    	if (node == pausePane) {
		    		hasIt = true;
		    		break;
		    	}
		    }
		    if (hasIt)
		    	root.getChildren().remove(pausePane);
		});

	}

    /**
     * Creates the pause box to be displayed on the root pane.
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
        //label.setMaxWidth(250);
        label.setWrapText(true);

        Label label1 = new Label("ESC to resume");
        label1.setAlignment(Pos.CENTER);
        label1.setPadding(new Insets(2));
        label1.setStyle(" -fx-font: bold 11pt 'Corbel'; -fx-text-fill: cyan;");
        //label.setMaxWidth(250);
        //label1.setWrapText(true);

        vbox.getChildren().addAll(label, label1);
        vbox.setAlignment(Pos.CENTER);

        return vbox;
    }

	/**
	 * Pauses the marquee timer and pauses the simulation.
	 */
	public void pauseSimulation() {
		isMuteCache = desktop.getSoundPlayer().isMute(false);
		if (!isMuteCache)
			desktop.getSoundPlayer().setMute(true);
		desktop.getMarqueeTicker().pauseMarqueeTimer(true);
		masterClock.setPaused(true);
	}

	/**
	 * Unpauses the marquee timer and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		masterClock.setPaused(false);
		desktop.getMarqueeTicker().pauseMarqueeTimer(false);
		if (!isMuteCache)
			desktop.getSoundPlayer().setMute(false);
	}

	public boolean startPause() {
		boolean previous = masterClock.isPaused();
		if (!previous) {
			pauseSimulation();
		}
		//desktop.getTimeWindow().enablePauseButton(false);
		masterClock.setPaused(true);
		return previous;
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
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves the main menu running
	 */
	private void endSim() {
		//logger.info("MainScene's endSim() is on " + Thread.currentThread().getName() + " Thread");
		Simulation.instance().endSimulation();
		Simulation.instance().getSimExecutor().shutdownNow();
		mainSceneExecutor.shutdownNow();
		getDesktop().clearDesktop();
		stage.close();
	}

	/**
	 * Exits the current simulation and the main menu.
	 */
	public void exitSimulation() {
		//logger.info("MainScene's exitSimulation() is on " + Thread.currentThread().getName() + " Thread");
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

	private void createSwingNode() {
		//createDesktops();
		desktop = new MainDesktopPane(this);
		SwingUtilities.invokeLater(() -> {
			swingNode.setContent(desktop);
		});
	}

	public SwingNode getSwingNode() {
		return swingNode;
	}

	/**
	 * Creates an Alert Dialog to confirm ending or exiting the simulation or
	 * MSP

	public boolean alertOnExit() {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Leaving the sim");
		alert.initOwner(stage);
		alert.setHeaderText(Msg.getString("MainScene.exit.header"));
		alert.setContentText(Msg.getString("MainScene.exit.content"));
		ButtonType buttonTypeOne = new ButtonType("Save & Exit");
		//ButtonType buttonTypeTwo = new ButtonType("End Sim");
		ButtonType buttonTypeThree = new ButtonType("Exit Sim");
		ButtonType buttonTypeCancel = new ButtonType("Back to Sim", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne,
				//buttonTypeTwo,
				buttonTypeThree, buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne) {
			saveOnExit();
			return true;

		} else if (result.get() == buttonTypeThree) {
			endSim();
			exitSimulation();
			Platform.exit();
			System.exit(0);
			return true;

		} else {
			return false;
		}
	}
	 */

	/**
	 * Open the exit dialog box
	 */
	public void dialogOnExit() {//StackPane pane) {
		jfxTabPane.getSelectionModel().select(mainTab);
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
		//b0.setPadding(new Insets(2, 2, 2, 2));
		hb.getChildren().addAll(b0, b1, b2);
		HBox.setMargin(b0, new Insets(3,3,3,3));
		HBox.setMargin(b1, new Insets(3,3,3,3));
		HBox.setMargin(b2, new Insets(3,3,3,3));
		VBox vb = new VBox();
		vb.setPadding(new Insets(5, 5, 5, 5));
		vb.getChildren().addAll(l, hb);
		StackPane sp = new StackPane(vb);
		sp.setStyle("-fx-background-color:rgba(0,0,0,0.1);");
		StackPane.setMargin(vb, new Insets(10,10,10,10));
		JFXDialog dialog = new JFXDialog();
		dialog.setDialogContainer(mainAnchorPane);
		dialog.setContent(sp);
		dialog.show();

		b0.setOnAction(e -> {
			dialog.close();
			saveOnExit();
		});

		b1.setOnAction(e -> {
			dialog.close();
			endSim();
			exitSimulation();
			Platform.exit();
			System.exit(0);
		});
		
		b2.setOnAction(e -> {
			dialog.close();
			isShowingDialog = false;
			e.consume();
		});

	}
	
	/**
	 * Initiates the process of saving a simulation.
	 */
	public void saveOnExit() {
		//logger.info("MainScene's saveOnExit() is on " + Thread.currentThread().getName() + " Thread");
		showWaitStage(SAVING);
		desktop.getTimeWindow().enablePauseButton(false);
		// Save the simulation as default.sim
		masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);

        Task task = new Task<Void>() {
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
            protected void succeeded(){
                super.succeeded();
                hideWaitStage(SAVING);
                endSim();
                exitSimulation();
    			Platform.exit();
    			System.exit(0);
            }
        };
        new Thread(task).start();

	}

	public void openInitialWindows() {
		//logger.info("MainScene's openInitialWindows() is on " + Thread.currentThread().getName() + " Thread");
		//String OS = System.getProperty("os.name").toLowerCase();
		//System.out.println("OS is " + OS);
		if (OS.contains("mac")) {
		// SwingUtilities needed below for MacOSX
			SwingUtilities.invokeLater(() -> {
				desktop.openInitialWindows();
			});
		}
		else {
			desktop.openInitialWindows();
		}

		quote = new QuotationPopup(this);
		//popAQuote();

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

	public double getWidth() {
		return sceneWidth.get();
	}

	public double getHeight() {
		return sceneHeight.get();
	}

	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

    private MenuItem registerAction(MenuItem menuItem) {
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                //showPopup(borderPane, "You clicked the " + menuItem.getText() + " icon");
            	System.out.println("You clicked the " + menuItem.getText() + " icon");
            }
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
 	 		//indicator.setOpacity(.5);
 	 		indicator.setStyle("-fx-background-color: transparent; ");
 	 		StackPane stackPane = new StackPane();
 	 		//stackPane.setOpacity(0.5);
 	 		stackPane.getChildren().add(indicator);
 	 		StackPane.setAlignment(indicator, Pos.CENTER);
 	 		stackPane.setBackground(Background.EMPTY);
 	 		stackPane.setStyle(
 	 	    		   //"-fx-border-style: none; "
 	 	       			"-fx-background-color: transparent; "
 	 	       			//+ "-fx-background-radius: 3px;"
 	 	    		   );
 	 		Scene scene = new Scene(stackPane, 100, 100);
 	 		scene.setFill(Color.TRANSPARENT);
 			loadingStage = new Stage();
 			//loadingCircleStage.setOpacity(1);
 			setEscapeEventHandler(true, loadingStage);
 			loadingStage.initOwner(stage);
 			loadingStage.initModality(Modality.WINDOW_MODAL); // Modality.NONE is by default if initModality() is NOT specified.
 			loadingStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
 			loadingStage.initStyle (StageStyle.TRANSPARENT);
 			loadingStage.setScene(scene);
 			loadingStage.hide();
	 	}

 		else if (type == AUTOSAVING) {
 	 		//ProgressIndicator indicator = new ProgressIndicator();
 			MaskerPane mPane = new MaskerPane();
 	 		mPane.setText("Autosaving");
 	 		mPane.setSkin(null);
 	 		//indicator.setOpacity(.5);
 	 		mPane.setStyle("-fx-background-color: transparent; ");
 	 		//indicator.setScaleX(1.5);
 	 		//indicator.setScaleY(1.5);
 	 		asPane = new StackPane();
 	 		//stackPane.setOpacity(0.5);
 	 		asPane.getChildren().add(mPane);
 	 		StackPane.setAlignment(mPane, Pos.CENTER);
 	 		asPane.setBackground(Background.EMPTY);
 	 		asPane.setStyle(//"-fx-border-style: none; "
 	 	       			"-fx-background-color: transparent; "
 	 	       			//+ "-fx-background-radius: 3px;"
 	 	    		   );

 			savingStage = new Stage();
 			savingStage.initOwner(stage);
 			savingStage.initModality(Modality.WINDOW_MODAL); // Modality.NONE is by default if initModality() is NOT specified.
 			savingStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
 			savingStage.initStyle (StageStyle.TRANSPARENT);

 	 		savingScene = new Scene(asPane);//, 150, 150);
 	 		savingScene.setFill(Color.TRANSPARENT);
 			savingStage.setScene(savingScene);

	    	//savingStage.xProperty().addListener((observable, oldValue, newValue) -> savingStage.setX(scene.getX()));
	    	//savingStage.yProperty().addListener((observable, oldValue, newValue) -> savingStage.setY(scene.getY()));
	    	//savingStage.setY(scene.getY() + .5 * (scene.getHeight() - savingStage.getHeight()));
 			savingStage.hide();

	 	}

 		else if (type == SAVING) {
 	 		//ProgressIndicator indicator = new ProgressIndicator();
 			MaskerPane mPane = new MaskerPane();
 	 		mPane.setText("Saving");
 	 		mPane.setSkin(null);
 	 		//indicator.setOpacity(.5);
 	 		mPane.setStyle("-fx-background-color: transparent; ");
 	 		//indicator.setScaleX(1.5);
 	 		//indicator.setScaleY(1.5);
 	 		sPane = new StackPane();
 	 		//stackPane.setOpacity(0.5);
 	 		sPane.getChildren().add(mPane);
 	 		StackPane.setAlignment(mPane, Pos.CENTER);
 	 		sPane.setBackground(Background.EMPTY);
 	 		sPane.setStyle(//"-fx-border-style: none; "
 	 	       			"-fx-background-color: transparent; "
 	 	       			//+ "-fx-background-radius: 3px;"
 	 	    		   );

	 	}

 		else if (type == PAUSED) {
 			//messagePopup = new MessagePopup();
	 	}

		else
			System.out.println("MainScene's createProgressCircle() : type is invalid");

 	}


 	/**
 	 * Starts the wait stage in an executor thread
 	 * @param type
 	 */
 	public void showWaitStage(int type) {
		if (type == AUTOSAVING) {
			savingScene.setRoot(asPane);
		}
		else if (type == SAVING) {
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

 		LoadWaitStageTask(int type){
 			this.type = type;
 		}

 		public void run() {
 			//logger.info("LoadWaitStageTask is on " + Thread.currentThread().getName());
			Platform.runLater(() -> {
				//FXUtilities.runAndWait(() -> {}) does NOT work
				if (type == AUTOSAVING || type == SAVING) {
					stopPausePopup();
					setMonitor(savingStage);
					//System.out.println("sPane.getWidth() / 2 : " + sPane.getWidth() / 2); // equals 0.0
					//System.out.println("savingStage.getWidth() / 2 : " + savingStage.getWidth() / 2); // equals : NaN
					savingStage.setX((int) (stage.getX() + scene.getWidth() / 2 - 50));
					savingStage.setY((int) (stage.getY() + scene.getHeight() / 2 - 50));
					savingStage.show();
					//System.out.println("sPane.getWidth() / 2 : " + sPane.getWidth() / 2); // equals 50.0
					//System.out.println("savingStage.getWidth() / 2 : " + savingStage.getWidth() / 2); // equals 50.0
				}
				else if (type == LOADING) {
					setMonitor(loadingStage);
					loadingStage.show();
				}
				else if (type == PAUSED) {
					stopPausePopup();
					startPausePopup();
				}
			});
 		}
 	}

 	public void hideWaitStage(int type) {
		//FXUtilities.runAndWait(() -> { // not working for loading sim
 		if (type == AUTOSAVING || type == SAVING) {
			savingStage.hide();
		}
		else if (type == LOADING) {
			loadingStage.hide();
			loadingStage.close();
		}
		else if (type == PAUSED) {
			stopPausePopup();
		}
		else
			System.out.println("MainScene's hideWaitStage() : type is invalid");

 	}

 	// 2016-06-27 Added setMonitor()
	public void setMonitor(Stage stage) {
		// Issue: how do we tweak mars-sim to run on the "active" monitor as chosen by user ?
		// "active monitor is defined by whichever computer screen the mouse pointer is or where the command console that starts mars-sim.
		// by default MSP runs on the primary monitor (aka monitor 0 as reported by windows os) only.
		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762
		StartUpLocation startUpLoc = null;

		if (anchorPane == null) {
			StackPane pane = new StackPane();//starfield);
			pane.setPrefHeight(sceneWidth.get());
			pane.setPrefWidth(sceneHeight.get());

			startUpLoc = new StartUpLocation(pane.getPrefWidth(), pane.getPrefHeight());
		}
		else {
			startUpLoc = new StartUpLocation(scene.getWidth(), scene.getHeight());
		}

        double xPos = startUpLoc.getXPos();
        double yPos = startUpLoc.getYPos();
        // Set Only if X and Y are not zero and were computed correctly
        if (xPos != 0 && yPos != 0) {
            stage.setX(xPos);
            stage.setY(yPos);
        }

        stage.centerOnScreen();
	}



	// 2016-10-01 Added mainSceneExecutor for executing wait stages
    private void startMainSceneExecutor() {
        //logger.info("Simulation's startSimExecutor() is on " + Thread.currentThread().getName() + " Thread");
    	// INFO: Simulation's startSimExecutor() is on JavaFX-Launcher Thread
    	mainSceneExecutor = Executors.newSingleThreadExecutor();
    }


    public JFXTabPane getJFXTabPane() {
    	return jfxTabPane;
    }

    public Pane getRoot() {
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
			//if (!desktop.isToolWindowOpen(SettlementWindow.NAME))
			openSettlementMap();
			sBox.getSelectionModel().select(s);
		});
	}

	//public CheckComboBox<String> getMapLabelBox() {
	//	return mapLabelBox;
	//}
/*
	public void sendSnackBar(String msg) {
		snackbar.fireEvent(new SnackbarEvent(msg, "UNDO",3000,(b)->{}));
	}
*/
	/**
	 * Sets up the JavaFX's tooltip
	 * @param n Node
	 * @param s tooltip's hint text
	 */
	public void setQuickToolTip(Node n, String s) {
		Tooltip tt = new Tooltip(s);
		tt.getStyleClass().add("ttip");

		n.setOnMouseEntered(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent event) {
		        Point2D p = n.localToScreen(n.getLayoutBounds().getMaxX(), n.getLayoutBounds().getMaxY()); //I position the tooltip at bottom right of the node (see below for explanation)
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

	private double convertSlider2Volume(double y) {
		return .05*y + .5;
	}

	private double convertVolume2Slider(double x) {
		return 20D*(x - .5);
	}

	public double getInitialTimeRatio() {
		return initial_time_ratio;
	}

	public JFXButton getMarsNetBtn() {
		return marsNetBtn;
	}

	public static void disableSound() {
		soundPlayer.enableMasterGain(false);
		if (soundSlider != null) soundSlider.setDisable(true);//.setValue(0);
	}
    
	public void destroy() {
		quote = null;
		//messagePopup = null;
		//topFlapBar = null;
		marsNetBox = null;
		marsNetBtn = null;
		chatBox = null;
		mainAnchorPane = null;
		anchorPane = null;
		newSimThread = null;
		stage = null;
		loadingStage = null;
		savingStage = null;
		timeline = null;
		//notificationPane = null;
		desktop.destroy();
		desktop = null;
		menuBar = null;
		marsNode = null;
		transportWizard = null;
		constructionWizard = null;
	}
}
