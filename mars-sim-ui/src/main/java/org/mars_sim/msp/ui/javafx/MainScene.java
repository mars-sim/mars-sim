/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.1.0 2016-10-26
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import com.jfoenix.controls.JFXBadge;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXRippler.RipplerMask;
import com.jfoenix.controls.JFXRippler.RipplerPos;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXSlider.IndicatorPosition;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXToggleButton;
//import com.jidesoft.swing.MarqueePane;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;


import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.eclipse.fx.ui.controls.tabpane.DndTabPane;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import com.sun.management.OperatingSystemMXBean;

import de.codecentric.centerdevice.MenuToolkit;
import de.jensd.fx.fontawesome.Icon;
import javafx.event.*;
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
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import javafx.util.Duration;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import static javafx.geometry.Orientation.VERTICAL;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.swing.AbstractAction;
import javax.swing.DesktopManager;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.javafx.BorderSlideBar;
import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;
import org.mars_sim.msp.ui.javafx.notification.MessagePopup;
import org.mars_sim.msp.ui.javafx.notification.PNotification;
import org.mars_sim.msp.ui.javafx.quotation.QuotationPopup;
//import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.swing.DesktopPane;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;
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
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.PlannerWindow;


import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;
import static org.fxmisc.wellbehaved.event.EventPattern.*;
import static org.fxmisc.wellbehaved.event.InputHandler.Result.*;
import static org.fxmisc.wellbehaved.event.InputMap.*;

import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;


/**
 * The MainScene class is the primary Stage for MSP. It is the container for
 * housing desktop swing node, javaFX UI, pull-down menu and icons for tools.
 */
public class MainScene {
	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
	
	private static final int TIME_DELAY = SettlementWindow.TIME_DELAY;
	private static final double ROTATION_CHANGE = Math.PI / 20D;
	
	public static final int NIMROD_THEME = 1;
	public static final int NIMBUS_THEME = 2;
	
	public static final int MAIN_TAB = 0;
	public static final int MAP_TAB = 1;
	public static final int MONITOR_TAB = 2;
	public static final int MISSION_TAB = 3;
	public static final int RESUPPLY_TAB = 4;
	public static final int SCIENCE_TAB = 5;
	public static final int HELP_TAB = 6;
	
	public static final int LOADING = 0;
	public static final int SAVING = 1;
	public static final int PAUSED = 2; 
	
	public static final int DEFAULT_WIDTH = 1366;
	public static final int DEFAULT_HEIGHT = 768;
	
	private static final String PAUSE_MSG = " [PAUSE]";// : ESC to resume]";
	private static final String LAST_SAVED = "Last Saved : ";
	private static final String EARTH_DATE_TIME = "EARTH  :  ";
	private static final String MARS_DATE_TIME = "MARS  :  ";
	//public static final String MILLISOLS_UMST = " millisols (UMST) ";
	public static final String UMST = " (UMST)";
	
	private static int theme = 7; // 6 is snow blue; 7 is the mud orange with nimrod

	public static int chatBoxHeight = 256;

	private int count = 0;
	private int memMax;
	private int memTotal;
	private int memUsed, memUsedCache;
	private int memFree;
	private int processCpuLoad;

	private boolean isMuteCache;
	private boolean flag = true;
	private boolean isMainSceneDoneLoading = false;
	public static boolean menuBarVisible = false;
	private boolean isMarsNetOpen = false;
	private boolean onMenuBarCache = false;
	private boolean isFullScreenCache = false;
	
	private DoubleProperty sceneWidth;// = new SimpleDoubleProperty(DEFAULt_WIDTH);//1366-40;
	private DoubleProperty sceneHeight;// = new SimpleDoubleProperty(DEFAULt_HEIGHT); //768-40;

	private volatile transient ExecutorService mainSceneExecutor; 
    	
	private String lookAndFeelTheme = "nimrod";
	private String title = null;
	private String dir = null;
	private String oldLastSaveStamp = null;

	//private StringProperty timeStamp;
    //private final BooleanProperty hideProperty = new SimpleBooleanProperty();

	private Pane root;
	private StackPane settlementBox, chatBoxPane, desktopPane, mapNodePane, minimapNodePane;
	//private BorderPane borderPane;
	private FlowPane flowPane;
	private AnchorPane anchorDesktopPane, anchorMapTabPane ;
	private SwingNode swingNode, mapNode, minimapNode, monNode, missionNode, resupplyNode, sciNode, guideNode ;
	private Stage stage, loadingCircleStage, savingCircleStage, pausingCircleStage;
	private Scene scene;

	private File fileLocn = null;
	private Thread newSimThread;

	private Label earthTimeLabel, marsTimeLabel, lastSaveLabel;
	private Text memUsedText;
	
	private JFXComboBox<Settlement> sBox;
	private JFXBadge badgeIcon;
	private JFXSnackbar snackbar;
	private JFXToggleButton cacheButton, calendarButton;
	private JFXSlider zoomSlider;
	private JFXButton miniMapBtn, mapBtn, marsNetButton, rotateCWBtn, rotateCCWBtn, recenterBtn; //menubarButton,
	private JFXPopup flyout;
	private JFXTabPane jfxTabPane;
	
	//private Button memBtn, clkBtn;
	//private StatusBar statusBar;
	//private Flyout flyout;
	//private CheckComboBox<String> mapLabelBox;
	private HBox lastSaveBar, earthTimeBar, marsTimeBar;
	private VBox mapLabelBox;
	private ChatBox chatBox;
	//private DndTabPane dndTabPane;
	private ESCHandler esc = null;

	private Tab mainTab;
	
	private Timeline timeline;
	private NotificationPane notificationPane;

	private DecimalFormat twoDigitFormat = new DecimalFormat(Msg.getString("twoDigitFormat")); //$NON-NLS-1$

	private MainDesktopPane desktop;
	private MainSceneMenu menuBar;

	private MarsNode marsNode;
	private TransportWizard transportWizard;
	private ConstructionWizard constructionWizard;

	private QuotationPopup quote;
	private MessagePopup messagePopup;
	
	private BorderSlideBar topFlapBar;
	
    private Simulation sim = Simulation.instance();
    private MasterClock masterClock = sim.getMasterClock();
	private EarthClock earthClock;
	private MarsClock marsClock;
	
	private SettlementWindow settlementWindow; 
	private SettlementMapPanel mapPanel;
	
	private List<DesktopPane> desktops;
	private ObservableList<Screen> screens;
	
	/**
	 * Constructor for MainScene
	 *
	 * @param stage
	 */
	public MainScene(Stage stage) {
		//logger.info("MainScene's constructor() is on " + Thread.currentThread().getName() + " Thread");
		this.stage = stage;
		this.isMainSceneDoneLoading = false;

		sceneWidth = new SimpleDoubleProperty(DEFAULT_WIDTH);
		sceneHeight = new SimpleDoubleProperty(DEFAULT_HEIGHT);
		
		//logger.info("OS is " + OS);
		stage.setResizable(true);
		stage.setMinWidth(sceneWidth.get());//1024);
		stage.setMinHeight(sceneHeight.get());//480);
		stage.setFullScreenExitHint("Use Ctrl+F (or Meta+C in macOS) to toggle full screen mode");
		stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
         
		// Detect if a user hits the top-right close button
		stage.setOnCloseRequest(e -> {
			boolean result = alertOnExit();
			if (!result)
				e.consume();
		});
	
		// Detect if a user hits ESC
		esc = new ESCHandler();
		setEscapeEventHandler(true, stage);
		
	}

	public void createIndicator() {
		// 2016-10-01 Added mainSceneExecutor for executing wait stages
		startMainSceneExecutor();
		createProgressCircle(LOADING);
		createProgressCircle(SAVING);
		createProgressCircle(PAUSED);
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
		logger.info("MainScene's prepareOthers() is on " + Thread.currentThread().getName() + " Thread");
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
		boolean previous = startPause();

		Platform.runLater(() -> {
			transportWizard.deliverBuildings(buildingManager);
		});

		endPause(previous);
	}


	public TransportWizard getTransportWizard() {
		return transportWizard;
	}

	/**
 	 * Pauses sim and opens the construction wizard
	 * @param constructionManager
	 */
	// 2015-12-16 Added openConstructionWizard()
	public void openConstructionWizard(BuildingConstructionMission mission) { // ConstructionManager constructionManager,
		//logger.info("MainScene's openConstructionWizard() is in " + Thread.currentThread().getName() + " Thread");
		// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
		// 2015-12-16 Track the current pause state
		boolean previous = startPause();

		Platform.runLater(() -> {
				constructionWizard.selectSite(mission);
			});

		endPause(previous);
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
			if (desktop.isToolWindowOpen(GuideWindow.NAME))
				SwingUtilities.invokeLater(() -> desktop.closeToolWindow(GuideWindow.NAME));
			else {
				//getJFXTabPane().getSelectionModel().select(MainScene.MAIN_TAB);
				SwingUtilities.invokeLater(() -> desktop.openToolWindow(GuideWindow.NAME));
			}
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
	    
		InputMap<KeyEvent> ctrlM = consume(keyPressed(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN)), e -> {
			boolean isMute = menuBar.getMuteItem().isSelected();
			if (isMute) {	
        		menuBar.getMuteItem().setSelected(false);
        		desktop.getSoundPlayer().setMute(true);
        	}
        	else {
        		menuBar.getMuteItem().setSelected(true);
        		desktop.getSoundPlayer().setMute(false);
        	}
			
		});
	    Nodes.addInputMap(root, ctrlM);

		InputMap<KeyEvent> ctrlQ = consume(keyPressed(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)), e -> {
        	popAQuote();
        	desktopPane.requestFocus();	
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
			desktop.getSoundPlayer().volumeUp();
		});
	    Nodes.addInputMap(root, ctrlUp);

		InputMap<KeyEvent> ctrlDown = consume(keyPressed(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN)), e -> {
			desktop.getSoundPlayer().volumeDown();
		});
	    Nodes.addInputMap(root, ctrlDown);

		//InputMap<KeyEvent> ctrlN = consume(keyPressed(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)), e -> {
		//	newSimulation();
		//});
	    //Nodes.addInputMap(root, ctrlN);
    
		InputMap<KeyEvent> ctrlS = consume(keyPressed(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)), e -> {
			saveSimulation(Simulation.SAVE_DEFAULT);
		});
	    Nodes.addInputMap(root, ctrlS);

		InputMap<KeyEvent> ctrlA = consume(keyPressed(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN)), e -> {
			saveSimulation(Simulation.SAVE_AS);
		});
	    Nodes.addInputMap(root, ctrlA);

		InputMap<KeyEvent> ctrlX = consume(keyPressed(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN)), e -> {
			alertOnExit();
		});
	    Nodes.addInputMap(root, ctrlX);
	    
		InputMap<KeyEvent> ctrlT = consume(keyPressed(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)), e -> {
			if (theme == 6)
				changeTheme(7);
			else if (theme == 7)
				changeTheme(6);
		});
	    Nodes.addInputMap(root, ctrlT);
	    
	}
    
	// Toggle the full screen mode off
	public void updateFullScreenMode() {
		menuBar.getShowFullScreenItem().setSelected(false);
	}
	
	/**
	 * initializes the scene
	 *
	 * @return Scene
	 */
	@SuppressWarnings("unchecked")
	public Scene initializeScene() {
		logger.info("MainScene's initializeScene() is on " + Thread.currentThread().getName() + " Thread");

		//see dpi scaling at http://news.kynosarges.org/2015/06/29/javafx-dpi-scaling-fixed/
		//"I guess we�ll have to wait until Java 9 for more flexible DPI support. 
		//In the meantime I managed to get JavaFX DPI scale factor, 
		//but it is a hack (uses both AWT and JavaFX methods):"
		
		// Number of actual horizontal lines (768p)
		double trueHorizontalLines = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		// Number of scaled horizontal lines. (384p for 200%)
		double scaledHorizontalLines = Screen.getPrimary().getBounds().getHeight();
		// DPI scale factor.
		double dpiScaleFactor = trueHorizontalLines / scaledHorizontalLines;
		//logger.info("DPI Scale Factor is " + dpiScaleFactor);
		

		// Create group to hold swingNode which in turns holds the Swing desktop
		swingNode = new SwingNode();
		createSwingNode();

		desktopPane = new StackPane();
		desktopPane.getChildren().add(swingNode);
		desktopPane.setMinHeight(sceneHeight.get());
		desktopPane.setMinWidth(sceneWidth.get());
		// 2016-11-25 Setup root for embedding key events
		root = new Pane();//Group();
		// 2016-11-14 Setup key events using wellbehavedfx
		setupKeyEvents();		
	    //2015-11-11 Added createFlyout()
		flyout = createFlyout();
        flag = false;
        //EffectUtilities.makeDraggable(flyout.getScene().getRoot().getStage(), chatBox);		
		// Create ControlFX's StatusBar
		//statusBar = createStatusBar();
        createLastSaveBar();
		createMarsTimeBar(); 
        createEarthTimeBar();
        // Create menuBar
		menuBar = new MainSceneMenu(this, desktop);
		// Create Snackbar
		//createJFXSnackbar();
		// Create jfxTabPane
		createJFXTabs();	

		anchorDesktopPane = new AnchorPane();

		if (OS.contains("mac")) {   
			((MenuBar)menuBar).useSystemMenuBarProperty().set(true);  
		}

		//AnchorPane.setBottomAnchor(jfxTabPane, 0.0);
        AnchorPane.setLeftAnchor(jfxTabPane, 0.0);
        AnchorPane.setRightAnchor(jfxTabPane, 0.0);
        AnchorPane.setTopAnchor(jfxTabPane, 0.0);

        //AnchorPane.setRightAnchor(badgeIcon, 5.0);
        //AnchorPane.setTopAnchor(badgeIcon, 0.0);

        AnchorPane.setRightAnchor(marsNetButton, 45.0);
        AnchorPane.setTopAnchor(marsNetButton, -3.0);       
         
        AnchorPane.setRightAnchor(mapBtn, 85.0);
        AnchorPane.setTopAnchor(mapBtn, -3.0);     

        AnchorPane.setRightAnchor(miniMapBtn, 125.0);
        AnchorPane.setTopAnchor(miniMapBtn, -3.0); 

        AnchorPane.setRightAnchor(lastSaveBar, 140.0);
        AnchorPane.setTopAnchor(lastSaveBar, 0.0);

        AnchorPane.setLeftAnchor(earthTimeBar, sceneWidth.get()/2D);// - earthTimeBar.getPrefWidth());
		if (OS.contains("linux")) AnchorPane.setTopAnchor(earthTimeBar, 30.0);
		else AnchorPane.setTopAnchor(earthTimeBar, 35.0);
		
        AnchorPane.setLeftAnchor(marsTimeBar, sceneWidth.get()/2D - marsTimeBar.getPrefWidth());
        if (OS.contains("linux")) AnchorPane.setTopAnchor(marsTimeBar, 30.0);
        else AnchorPane.setTopAnchor(marsTimeBar, 35.0);
        
        anchorDesktopPane.getChildren().addAll(jfxTabPane, miniMapBtn, mapBtn, marsNetButton, lastSaveBar, earthTimeBar, marsTimeBar);//badgeIcon,borderPane, timeBar, snackbar
        
		root.getChildren().addAll(anchorDesktopPane);
		
    	scene = new Scene(root, sceneWidth.get(), sceneHeight.get());//, Color.BROWN);

    	//scene.heightProperty().addListener((observable, oldValue, newValue) -> {
    	//    System.out.println("scene height : " + newValue);    	    
    	//});
    	//scene.widthProperty().addListener((observable, oldValue, newValue) -> {
    	//    System.out.println("scene width : " + newValue);
    	//});
    	   	

		jfxTabPane.prefHeightProperty().bind(scene.heightProperty());//.subtract(35));//73));
		jfxTabPane.prefWidthProperty().bind(scene.widthProperty());

		desktopPane.prefHeightProperty().bind(scene.heightProperty().subtract(35));
		desktopPane.prefWidthProperty().bind(scene.widthProperty());

		// anchorTabPane is within jfxTabPane
		anchorMapTabPane.prefHeightProperty().bind(scene.heightProperty().subtract(35));//73));
		anchorMapTabPane.prefWidthProperty().bind(scene.widthProperty());
		
		mapNodePane.prefHeightProperty().bind(scene.heightProperty().subtract(35));//73));
		//mapNodePane.prefWidthProperty().bind(scene.widthProperty());
		
		//mapNodePane.heightProperty().addListener((observable, oldValue, newValue) -> {
    	//    System.out.println("mapNodePane height : " + newValue);    	    
    	//});

		return scene;
	}

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
	
	public void createEarthTimeBar() {
		earthTimeBar = new HBox();
		earthTimeBar.setMaxWidth(Double.MAX_VALUE);
		if (OS.contains("linux")) {
			earthTimeBar.setMinWidth(250);
			earthTimeBar.setPrefSize(250, 32);			
		}
		else {
			earthTimeBar.setMinWidth(220);
			earthTimeBar.setPrefSize(220, 32);			
		}	
		//calendarButton = new JFXToggleButton();
		
		if (masterClock == null) {
			masterClock = sim.getMasterClock();
		}
		
		if (earthClock == null) {
			earthClock = masterClock.getEarthClock();
		}

		earthTimeLabel = new Label();
		earthTimeLabel.setId("rich-blue");
		//earthTimeLabel.setMaxWidth(Double.MAX_VALUE);
		//earthTimeLabel.setMinWidth(180);
		//earthTimeLabel.setPrefSize(180, 30);
		earthTimeLabel.setTextAlignment(TextAlignment.CENTER);
		Tooltip t = new Tooltip("Earth Date/Time");
		earthTimeLabel.setTooltip(t);
		setQuickToolTip(earthTimeLabel, t);

		earthTimeBar.getChildren().add(earthTimeLabel);
	}
	
	
	public void createMarsTimeBar() {
		marsTimeBar = new HBox();
		marsTimeBar.setMaxWidth(Double.MAX_VALUE);
		if (OS.contains("linux")) {
			marsTimeBar.setMinWidth(260);
			marsTimeBar.setPrefSize(260, 24);			
		}
		else {
			marsTimeBar.setMinWidth(230);
			marsTimeBar.setPrefSize(230, 24);			
		}

	
		if (masterClock == null) {
			masterClock = sim.getMasterClock();
		}
		
		if (marsClock == null) {
			marsClock = masterClock.getMarsClock();
		}

		marsTimeLabel = new Label();
		marsTimeLabel.setId("rich-orange");
		Tooltip t = new Tooltip("Mars Date/Time");
		marsTimeLabel.setTooltip(t);
		setQuickToolTip(marsTimeLabel, t);

		marsTimeLabel.setTextAlignment(TextAlignment.CENTER);
		
		marsTimeBar.getChildren().add(marsTimeLabel);
	}
	
	public void createFXButtons() {
		cacheButton = new JFXToggleButton();
		cacheButton.setText("Cache Off");
		cacheButton.setSelected(false);
		Tooltip t = new Tooltip("Caching Settlement Map even after switching to another tab");
		cacheButton.setTooltip(t);
		setQuickToolTip(cacheButton, t);
		cacheButton.setOnAction(e -> {
			if (cacheButton.isSelected()) {
				cacheButton.setText("Cache On");
				openMap();
				//cacheButton.setTextFill(Paint.OPAQUE);
			}
			else
				cacheButton.setText("Cache Off");
		});
		
		rotateCWBtn = new JFXButton();
		rotateCWBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.cw")))));	 //$NON-NLS-1$
		Tooltip t0 = new Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.clockwise")); //$NON-NLS-1$
		rotateCWBtn.setTooltip(t0);
		setQuickToolTip(rotateCWBtn, t0);
		rotateCWBtn.setOnAction(e -> {
			mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE);	
		});
		
		rotateCCWBtn = new JFXButton();
		rotateCCWBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.ccw")))));	//$NON-NLS-1$ 
		Tooltip t1 = new Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise")); //$NON-NLS-1$
		rotateCCWBtn.setTooltip(t1);
		setQuickToolTip(rotateCCWBtn, t1);
		rotateCCWBtn.setOnAction(e -> {
			mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE);	
		});
		
		recenterBtn = new JFXButton();
		recenterBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(Msg.getString("img.recenter"))))); //$NON-NLS-1$	
		Tooltip t2 = new Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.recenter")); //$NON-NLS-1$
		recenterBtn.setTooltip(t2);
		setQuickToolTip(recenterBtn, t2);
		recenterBtn.setOnAction(e -> {
			mapPanel.reCenter();
			zoomSlider.setValue(0);
		});

	}
	
	public void createFXZoomSlider() {
		logger.info("MainScene's createFXZoomSlider() is on " + Thread.currentThread().getName() + " Thread");
		
		// Set up a settlement view zoom bar
		zoomSlider = new JFXSlider();
		zoomSlider.getStyleClass().add("jfx-slider");
		//zoom.setMinHeight(100);
		//zoom.setMaxHeight(200);
		zoomSlider.prefHeightProperty().bind(mapNodePane.heightProperty().multiply(.3d));
		zoomSlider.setMin(-10);
		zoomSlider.setMax(10);
		zoomSlider.setValue(0);
		zoomSlider.setMajorTickUnit(5);
		zoomSlider.setShowTickLabels(true);
		zoomSlider.setShowTickMarks(true);
		zoomSlider.setBlockIncrement(1);
		zoomSlider.setOrientation(Orientation.VERTICAL);
		zoomSlider.setIndicatorPosition(IndicatorPosition.RIGHT);
		Tooltip t2 = new Tooltip(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		zoomSlider.setTooltip(t2);
		setQuickToolTip(zoomSlider, t2);
		
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
		Tooltip t2 = new Tooltip(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		sBox.setTooltip(t2);
		setQuickToolTip(sBox, t2);
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
		logger.info("MainScene's createJFXTabs() is on " + Thread.currentThread().getName() + " Thread");
		
		jfxTabPane = new JFXTabPane();
		
		String cssFile = null;
        
        if (desktop.getMainScene().getTheme() == 6)
        	cssFile = "/css/jfx_blue.css";
        else
        	cssFile = "/css/jfx_orange.css";
		
		jfxTabPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		jfxTabPane.getStyleClass().add("jfx-tab-pane");
					
		mainTab = new Tab();
		mainTab.setText("Main");
		mainTab.setContent(desktopPane);
		
		
		// set up mapTab
		Tab mapTab = new Tab();		
		anchorMapTabPane = new AnchorPane();
		anchorMapTabPane.setStyle("-fx-background-color: black; ");

		NavigatorWindow navWin = (NavigatorWindow) desktop.getToolWindow(NavigatorWindow.NAME);
		minimapNode = new SwingNode();
		minimapNodePane = new StackPane(minimapNode);
		minimapNodePane.setStyle("-fx-background-color: black; ");
		minimapNode.setStyle("-fx-background-color: black; ");
		miniMapBtn = new JFXButton();
		miniMapBtn.setTooltip(new Tooltip("Open mini-map below"));
		miniMapBtn.setOnAction(e -> {
			
			if (desktop.isToolWindowOpen(NavigatorWindow.NAME)) {
				desktop.closeToolWindow(NavigatorWindow.NAME);
				anchorMapTabPane.getChildren().removeAll(minimapNodePane);
			}
			
			else {
				desktop.openToolWindow(NavigatorWindow.NAME);
				minimapNode.setContent(navWin); 
		        AnchorPane.setLeftAnchor(minimapNodePane, 3.0);
		        AnchorPane.setTopAnchor(minimapNodePane, 0.0); // 45.0  
		        anchorMapTabPane.getChildren().addAll(minimapNodePane);
		        minimapNode.toFront();

			}
  
		});
		
		settlementWindow = (SettlementWindow) desktop.getToolWindow(SettlementWindow.NAME);
		mapPanel = settlementWindow.getMapPanel();
		
		mapNode = new SwingNode();
		mapNodePane = new StackPane(mapNode);
		mapNodePane.setStyle("-fx-background-color: black; ");
		mapNode.setStyle("-fx-background-color: black; ");
		
		createFXButtons();
		
		createFXSettlementComboBox();
		
		createFXZoomSlider();

		createFXMapLabelBox();
		
        // detect mouse wheel scrolling
        mapNodePane.setOnScroll(new EventHandler<ScrollEvent>() {
            public void handle(ScrollEvent event) {
		                       
                if (event.getDeltaY() == 0) {
                    return;
                }
  
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
		
		mapBtn = new JFXButton();
		mapBtn.setTooltip(new Tooltip("Open settlement map below"));
		mapBtn.setOnAction(e -> {			
			if (desktop.isToolWindowOpen(SettlementWindow.NAME)) {
				//System.out.println("closing map tool.");
				desktop.closeToolWindow(SettlementWindow.NAME);
				anchorMapTabPane.getChildren().removeAll(settlementBox, mapLabelBox, mapNodePane, zoomSlider, rotateCWBtn, rotateCCWBtn, recenterBtn);//, minimapNodePane);
			}
			
			else {					
				openMap();
			}

		});
		 
        
		mapTab.setText("Map");
		mapTab.setContent(anchorMapTabPane);
		
 
		// set up monitor tab
		MonitorWindow monWin = (MonitorWindow) desktop.getToolWindow(MonitorWindow.NAME);
		monNode = new SwingNode();		
	    JDesktopPane d0 = desktops.get(0);
	    d0.add(monWin);
		monNode.setContent(d0); 
		StackPane monPane = new StackPane(monNode);
		Tab monTab = new Tab();
		monTab.setText("Monitor");
		monTab.setContent(monPane);
		
		//desktop.openToolWindow(MonitorWindow.NAME);
		
		
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
		
		
		// set up help tab
		GuideWindow guideWin = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
		guideNode = new SwingNode();
		guideNode.setContent(guideWin); 
		StackPane guidePane = new StackPane(guideNode);
		Tab guideTab = new Tab();
		guideTab.setText("Help");
		guideTab.setContent(guidePane);

		//desktop.openToolWindow(GuideWindow.NAME);
		
		jfxTabPane.getTabs().addAll(mainTab, mapTab, monTab, missionTab, resupplyTab, scienceTab, guideTab);	
		
		jfxTabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
			if (newTab == mainTab) {	
				anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
				anchorMapTabPane.getChildren().removeAll(cacheButton);
				desktopPane.requestFocus();
			}
			
			else if (newTab == monTab) {	
				if (!desktop.isToolWindowOpen(MonitorWindow.NAME)) {
					desktop.openToolWindow(MonitorWindow.NAME);
					//monNode.setContent(monWin); 
				}
				
				anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
				anchorMapTabPane.getChildren().removeAll(cacheButton);

			}
			
			else if (newTab == mapTab) {
				
				if (!desktop.isToolWindowOpen(SettlementWindow.NAME)) {			       
					if (isCacheButtonOn())
						mapBtn.fire();
				}

				if (!desktop.isToolWindowOpen(NavigatorWindow.NAME)) {				       
					if (isCacheButtonOn())
						miniMapBtn.fire();
				}

				AnchorPane.setRightAnchor(mapBtn, 85.0);
				AnchorPane.setTopAnchor(mapBtn, -3.0);   
				anchorDesktopPane.getChildren().addAll(mapBtn);

		        AnchorPane.setRightAnchor(miniMapBtn, 125.0);
		        AnchorPane.setTopAnchor(miniMapBtn, -3.0);  
				anchorDesktopPane.getChildren().addAll(miniMapBtn);

		        AnchorPane.setRightAnchor(cacheButton, 20.0);
		        AnchorPane.setTopAnchor(cacheButton, 55.0);  // 45.0 		        
				anchorMapTabPane.getChildren().addAll(cacheButton);

			}
			
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
			
			else if (newTab == guideTab) {	
				if (!desktop.isToolWindowOpen(GuideWindow.NAME)) {
					desktop.openToolWindow(GuideWindow.NAME);
					//guideNode.setContent(guideWin); 
				}
			       
				anchorDesktopPane.getChildren().removeAll(miniMapBtn, mapBtn);
			    anchorMapTabPane.getChildren().removeAll(cacheButton);
			}
		});
		
		//jfxTabPane.getSelectionModel().select(mainTab);
		
		// NOTE: if a tab is NOT selected, should close that tool as well to save cpu utilization
		// this is done in ToolWindow's update(). It allows for up to 1 second of delay, in case user open and close the same repeated.

	}
	
	public void openMap() {
		
		if (desktop.isToolWindowOpen(NavigatorWindow.NAME)) {
			mapNodePane.prefWidthProperty().unbind();
			mapNodePane.prefWidthProperty().bind(scene.widthProperty().subtract(minimapNodePane.widthProperty()).subtract(2));							
		}
		else {
			mapNodePane.prefWidthProperty().unbind();
			mapNodePane.prefWidthProperty().bind(scene.widthProperty().subtract(1));			
		}
		
		
		desktop.openToolWindow(SettlementWindow.NAME);
		mapNode.setContent(settlementWindow); 

        AnchorPane.setRightAnchor(mapNodePane, 0.0);
        AnchorPane.setTopAnchor(mapNodePane, 0.0);   

        AnchorPane.setRightAnchor(zoomSlider, 55.0);
        AnchorPane.setTopAnchor(zoomSlider, 350.0);//(mapNodePane.heightProperty().get() - zoomSlider.heightProperty().get())*.4d);    

        AnchorPane.setRightAnchor(rotateCWBtn, 100.0);
        AnchorPane.setTopAnchor(rotateCWBtn, 300.0);    

        AnchorPane.setRightAnchor(rotateCCWBtn, 20.0);
        AnchorPane.setTopAnchor(rotateCCWBtn, 300.0);    

        AnchorPane.setRightAnchor(recenterBtn, 60.0);
        AnchorPane.setTopAnchor(recenterBtn, 300.0);    

        AnchorPane.setRightAnchor(settlementBox, 2.0);//anchorMapTabPane.widthProperty().get()/2D - 110.0);//settlementBox.getWidth());
        AnchorPane.setTopAnchor(settlementBox, 30.0);
   
        AnchorPane.setRightAnchor(mapLabelBox, -10.0);
        AnchorPane.setTopAnchor(mapLabelBox, 120.0); 
   
        
        boolean hasMap = false, hasZoom = false, hasButtons = false, 
        		hasSettlements = false, hasMapLabel = false;
        
        for (Node node : anchorMapTabPane.getChildrenUnmodifiable()) {

	        if (node == settlementBox) {
	        	hasSettlements = true;
	        }
	        else if (node == mapNodePane) {
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
			anchorMapTabPane.getChildren().addAll(mapNodePane);

		if (!hasSettlements) {
			anchorMapTabPane.getChildren().addAll(settlementBox);
		}
		
		if (!hasMapLabel)
			anchorMapTabPane.getChildren().addAll(mapLabelBox);
			        				
		if (!hasZoom)
			anchorMapTabPane.getChildren().addAll(zoomSlider);
        
		if (!hasButtons)
			anchorMapTabPane.getChildren().addAll(rotateCWBtn, rotateCCWBtn, recenterBtn);
		
		for (Node node : anchorMapTabPane.getChildrenUnmodifiable()) {
	        if (node == cacheButton) {
	        	node.toFront();
	        }
	        if (node == settlementBox) {
	        	node.toFront();
	        }
	        if (node == mapLabelBox) {
	        	node.toFront();
	        }			        
	    }
	}

	public void closeMaps() {
		if (!cacheButton.isSelected()) {
			//System.out.println("closing both maps...");
			desktop.closeToolWindow(SettlementWindow.NAME);
			desktop.closeToolWindow(NavigatorWindow.NAME);
			Platform.runLater(() -> {
				anchorMapTabPane.getChildren().removeAll(mapNodePane, zoomSlider, rotateCWBtn, rotateCCWBtn, recenterBtn, minimapNodePane, settlementBox, mapLabelBox);
			});
		}
	}
	
	
	public boolean isCacheButtonOn() {
		if (cacheButton.isSelected())
			return true;
		else
			return false;
	}
	
	public void createDesktops() {
		desktops = new ArrayList<DesktopPane>();		
		int size = 3;	
		for (int i= 0; i < size; i++ ){
		   DesktopPane d = new DesktopPane(this);
		   desktops.add(d); 
		}	    
	}
	
	public List<DesktopPane> getDesktops() {
		return desktops;
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
		changeTheme(theme);
		//logger.info("done with MainScene's initializeTheme()");
	}

	/*
	 * Changes the theme skin of desktop
	 */
	public void changeTheme(int theme) {
		this.theme = theme;
		
		SwingUtilities.invokeLater(() -> {	
			// 2016-06-17 Added checking for OS. 
			// Note: NIMROD theme lib doesn't work on linux 
			//if (OS.equals("linux")) { 
			//	this.theme = 0;
			//	setLookAndFeel(NIMBUS_THEME);	
			//}
			//else 
				setLookAndFeel(NIMROD_THEME);			
		});
		
		//desktopPane.getStylesheets().clear();
		
		if (menuBar.getStylesheets() != null) 
			menuBar.getStylesheets().clear();
		//statusBar.getStylesheets().clear();	

		String cssFile;
	
		//logger.info("MainScene's changeTheme()");
		if (theme == 0) { //  snow blue 
			// for numbus theme
			cssFile = "/fxui/css/snowBlue.css";
			updateThemeColor(6, Color.rgb(0,107,184), Color.rgb(0,107,184), cssFile); // CADETBLUE // Color.rgb(23,138,255)
			lookAndFeelTheme = "snowBlue";

		} else if (theme == 1) { // olive green
			cssFile = "/fxui/css/oliveskin.css";
			updateThemeColor(1, Color.GREEN, Color.PALEGREEN, cssFile); //DARKOLIVEGREEN
			lookAndFeelTheme = "LightTabaco";
			
		} else if (theme == 2) { // burgundy red
			cssFile = "/fxui/css/burgundyskin.css";
			updateThemeColor(2, Color.rgb(140,0,26), Color.YELLOW, cssFile); // ORANGERED
			lookAndFeelTheme = "Burdeos";

		} else if (theme == 3) { // dark chocolate
			cssFile = "/fxui/css/darkTabaco.css";
			updateThemeColor(3, Color.DARKGOLDENROD, Color.BROWN, cssFile);
			lookAndFeelTheme = "DarkTabaco";

		} else if (theme == 4) { // grey
			cssFile = "/fxui/css/darkGrey.css";
			updateThemeColor(4, Color.DARKSLATEGREY, Color.DARKGREY, cssFile);
			lookAndFeelTheme = "DarkGrey";

		} else if (theme == 5) { // + purple
			cssFile = "/fxui/css/nightViolet.css";
			updateThemeColor(5, Color.rgb(73,55,125), Color.rgb(73,55,125), cssFile); // DARKMAGENTA, SLATEBLUE
			lookAndFeelTheme = "Night";

		} else if (theme == 6) { // + skyblue
	
			cssFile = "/fxui/css/snowBlue.css";
			updateThemeColor(6, Color.rgb(0,107,184), Color.rgb(255,255,255), cssFile); //(144, 208, 229) light blue // CADETBLUE (0,107,184)// Color.rgb(23,138,255)
			lookAndFeelTheme = "snowBlue";

		} else if (theme == 7) { // mud orange/standard

			cssFile = "/fxui/css/nimrodskin.css";
			updateThemeColor(7, Color.rgb(156,77,0), Color.rgb(255,255,255), cssFile); //DARKORANGE, CORAL
			lookAndFeelTheme = "nimrod";

		}

		//logger.info("done with MainScene's changeTheme()");
	}

	/*
	 * Updates the theme colors of statusBar, swingPane and menuBar
	 */
	// 2015-08-29 Added updateThemeColor()
	public void updateThemeColor(int theme, Color txtColor, Color txtColor2, String cssFile) {
		desktopPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		if (!OS.contains("mac"))
			menuBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		
		// Note : menu bar color
		// orange theme : F4BA00
		// blue theme : 3291D2
		
		//String color = txtColor.toString().replace("0x", "");
		//timeLabel.setTextFill(txtColor2);
		//lastSaveLabel.setTextFill(txtColor2);

		jfxTabPane.getStylesheets().clear();
		
		earthTimeLabel.getStylesheets().clear();
		earthTimeLabel.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());	

		marsTimeLabel.getStylesheets().clear();
		marsTimeLabel.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());	
	
		lastSaveLabel.getStylesheets().clear();
		lastSaveLabel.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		
		miniMapBtn.getStylesheets().clear();
		miniMapBtn.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		mapBtn.getStylesheets().clear();
		mapBtn.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		cacheButton.getStylesheets().clear();
		cacheButton.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		settlementBox.getStylesheets().clear();
		settlementBox.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		mapLabelBox.getStylesheets().clear();
		mapLabelBox.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
			
		
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
		
		if (theme == 6) {
			//if (!OS.contains("mac"))
			//	menubarButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/blue_menu_32.png"))));
			
			miniMapBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/blue_globe_32.png"))));
			mapBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/blue_map_32.png"))));
			marsNetButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/blue_chat_32.png"))));
			jfxTabPane.getStylesheets().add(getClass().getResource("/css/jfx_blue.css").toExternalForm());
		}
		else if (theme == 7) {
			//if (!OS.contains("mac"))
			//	menubarButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/orange_menu_32.png"))));

			miniMapBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/orange_globe_32.png"))));
			mapBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/orange_map_32.png"))));
			marsNetButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/orange_chat_32.png"))));
			jfxTabPane.getStylesheets().add(getClass().getResource("/css/jfx_orange.css").toExternalForm());
		}
		
		chatBox.update();    
		
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
     	marsNetButton = new JFXButton();       
        //marsNetButton.setId("marsNetButton");
        marsNetButton.setTooltip(new Tooltip ("Open MarsNet chat box"));
        //marsNetButton.setPadding(new Insets(0, 0, 0, 0)); // Warning : this significantly reduce the size of the button image
 
		flyout = new JFXPopup();
		flyout.setOpacity(.9);
		flyout.setContent(createChatBox());
		flyout.setPopupContainer(anchorDesktopPane);
		flyout.setSource(marsNetButton);		
   	
		//chatBox.update();
        marsNetButton.setOnAction(e -> {
            if (!flag) 
            	chatBox.update();
  
            if (flyout.isVisible()) {
                flyout.close();
            }
            else {
            	openChatBox();
            }
            
        });
       
        return flyout;
    }
    
    public void openChatBox() {
		try {
			TimeUnit.MILLISECONDS.sleep(200L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        chatBox.getAutoFillTextBox().getTextbox().clear();
        chatBox.getAutoFillTextBox().getTextbox().requestFocus();
    	flyout.show(PopupVPosition.TOP, PopupHPosition.RIGHT, -50, 20); 
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
    	return flyout;
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
		lastSaveBar = new HBox();
		lastSaveBar.setPadding(new Insets(5,20,5,20));
		
		//2016-09-15 Added oldLastSaveStamp
		oldLastSaveStamp = sim.getLastSave();
		oldLastSaveStamp = oldLastSaveStamp.replace("_", " ");
		
		lastSaveLabel = new Label();
		lastSaveLabel.setMaxWidth(Double.MAX_VALUE);
		lastSaveLabel.setMinWidth(250);
		lastSaveLabel.setPrefSize(250, 25);
		lastSaveLabel.setTextAlignment(TextAlignment.LEFT);
		lastSaveLabel.setText(LAST_SAVED + oldLastSaveStamp);
		Tooltip t = new Tooltip ("The very last time the sim was saved/autosaved on your machine");
		lastSaveLabel.setTooltip(t);
		setQuickToolTip(lastSaveLabel, t);
		
		lastSaveBar.getChildren().add(lastSaveLabel);		
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
	 */

	public NotificationPane getNotificationPane() {
		return notificationPane;
	}

	public Node createNotificationPane() {
		// wrap the dndTabPane inside notificationNode
		notificationPane = new NotificationPane(desktopPane);

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


	/*
	 * Updates Earth and Mars time label in the earthTimeBar and marsTimeBar
	 */
	public void updateTimeLabels() {
		//String t = earthClock.getTimeStamp2();
		// Check if new simulation is being created or loaded from file.
		//if (sim.isUpdating() || masterClock.isPaused()) {
		//	earthTimeLabel.setText(" " + t + " " + PAUSE_MSG);		
		marsTimeLabel.setText(MARS_DATE_TIME + marsClock.getDateString() + " " + marsClock.getTrucatedTimeString() + UMST);		
		earthTimeLabel.setText(EARTH_DATE_TIME + earthClock.getTimeStamp2());
		
		//2016-09-15 Added oldLastSaveStamp and newLastSaveStamp
		String newLastSaveStamp = sim.getLastSave();
		if (!oldLastSaveStamp.equals(newLastSaveStamp)) {
			oldLastSaveStamp = newLastSaveStamp.replace("_", " ");
			lastSaveLabel.setText(LAST_SAVED + oldLastSaveStamp);
			//System.out.print("updated last save time stamp");
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
	 * @param useDefault
	 *            Should the user be allowed to override location?
	 */
	public void saveSimulation(int type) {
		//logger.info("MainScene's saveSimulation() is on " + Thread.currentThread().getName() + " Thread");
		boolean previous = startPause();

		hideWaitStage(PAUSED);
		showWaitStage(SAVING);
	
        Task task = new Task<Void>() {
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

		endPause(previous);
		
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
			masterClock.saveSimulation(Simulation.AUTOSAVE, null);
		
		} else if (type == Simulation.SAVE_DEFAULT) {
			dir = Simulation.DEFAULT_DIR;
			masterClock.saveSimulation(Simulation.SAVE_DEFAULT, null);
			
		} else if (type == Simulation.SAVE_AS) {			
			masterClock.setPaused(true);	
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
				else
					return;
				
				hideWaitStage(PAUSED);
				showWaitStage(SAVING);

		        Task task = new Task<Void>() {
		            @Override
		            protected Void call() throws Exception {
		        		try {
		    				masterClock.saveSimulation(Simulation.SAVE_AS, fileLocn);
		    				
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
		//System.out.println("calling startPausePopup(): messagePopup.numPopups() is " + messagePopup.numPopups());   
		if (messagePopup.numPopups() == 0) {	
            // Note: (NOT WORKING) popups.size() is always zero no matter what.
			Platform.runLater(() -> 
				messagePopup.popAMessage("PAUSE", "ESC to resume", " ", stage, Pos.TOP_CENTER, PNotification.PAUSE_ICON)
			);  		    	
		}
	}

	public void stopPausePopup() {
		Platform.runLater(() -> 
			messagePopup.stop()
		);
		    	
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
		desktop.getTimeWindow().enablePauseButton(false);
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

	/**
	 * Sets the look and feel of the UI
	 * @param nativeLookAndFeel true if native look and feel should be used.
	 */
	// 2015-05-02 Edited setLookAndFeel()
	public void setLookAndFeel(int choice) {
		//logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
		boolean changed = false;
		if (choice == NIMBUS_THEME) { // theme == "nativeLookAndFeel"
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
				lookAndFeelTheme = "system";
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else if (choice == NIMROD_THEME) { // theme == "nimRODLookAndFeel"
			try {

				//if (lookAndFeelTheme.equals("nimrod")) // at the start of the sim
					// Use default theme
				//	try {
				//		UIManager.setLookAndFeel(new NimRODLookAndFeel());
				//		changed = true;
				//	} catch (Exception e) {
				//		e.printStackTrace();
				//	}

				//else { // at the start of the sim
					/*
					 * //TODO: let user customize theme in future NimRODTheme nt
					 * = new NimRODTheme(); nt.setPrimary1(new
					 * java.awt.Color(10,10,10)); nt.setPrimary2(new
					 * java.awt.Color(20,20,20)); nt.setPrimary3(new
					 * java.awt.Color(30,30,30)); NimRODLookAndFeel NimRODLF =
					 * new NimRODLookAndFeel(); NimRODLF.setCurrentTheme( nt);
					 */
					if (theme == 0 || theme == 6) {
						lookAndFeelTheme = "snowBlue";
					}
					else if (theme == 7)
						lookAndFeelTheme = "nimrod"; // note that nimrod.theme uses all default parameter except overriding the opacity with 220.
					
					NimRODTheme nt = new NimRODTheme(getClass().getClassLoader().getResource("theme/" + lookAndFeelTheme + ".theme"));					
					NimRODLookAndFeel nf = new NimRODLookAndFeel();				
					nf.setCurrentTheme(nt);
					UIManager.setLookAndFeel(nf);
					changed = true;
				//}

			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else if (choice == 2) {
			try {
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {				
					if (info.getName().equals("Nimbus")) { //$NON-NLS-1$
						// Set Nimbus look & feel if found in JVM.
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						lookAndFeelTheme = "nimbus";
						changed = true;
						//break;
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
			if (desktop != null) {
				SwingUtilities.updateComponentTreeUI(desktop);
				desktop.updateToolWindowLF();
				desktop.updateUnitWindowLF();
				desktop.updateAnnouncementWindowLF();
				// desktop.updateTransportWizardLF();
			}
		}
		//logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
	}

	public MainSceneMenu getMainSceneMenu() {
		return menuBar;
	}

	
	public Stage getStage() {
		return stage;
	}

	private void createSwingNode() {
		createDesktops();
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
	 */
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

	/**
	 * Initiates the process of saving a simulation.
	 */
	public void saveOnExit() {
		//logger.info("MainScene's saveOnExit() is on " + Thread.currentThread().getName() + " Thread");
		showWaitStage(SAVING);	
		desktop.getTimeWindow().enablePauseButton(false);
		// Save the simulation as default.sim
		masterClock.saveSimulation(Simulation.SAVE_DEFAULT, null);

        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
        		try {
        			masterClock.saveSimulation(Simulation.SAVE_DEFAULT, null);
        						
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
		popAQuote();

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
		return anchorDesktopPane;
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
 			loadingCircleStage = new Stage();
 			//loadingCircleStage.setOpacity(1);
 			setEscapeEventHandler(true, loadingCircleStage);
 			loadingCircleStage.initOwner(stage);
 			loadingCircleStage.initModality(Modality.WINDOW_MODAL); // Modality.NONE is by default if initModality() is NOT specified.
 			loadingCircleStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
 			loadingCircleStage.initStyle (StageStyle.TRANSPARENT);
 			loadingCircleStage.setScene(scene);
 			loadingCircleStage.hide();
	 	}
 		
 		else if (type == SAVING) {
 	 		//ProgressIndicator indicator = new ProgressIndicator();
 	 		MaskerPane indicator = new MaskerPane();
 	 		indicator.setSkin(null);
 	 		//indicator.setOpacity(.5);
 	 		indicator.setStyle("-fx-background-color: transparent; ");  
 	 		//indicator.setScaleX(1.5);
 	 		//indicator.setScaleY(1.5);
 	 		StackPane stackPane = new StackPane();
 	 		//stackPane.setOpacity(0.5);
 	 		stackPane.getChildren().add(indicator);
 	 		StackPane.setAlignment(indicator, Pos.CENTER);
 	 		stackPane.setBackground(Background.EMPTY);
 	 		//stackPane.setStyle("-fx-background-color: transparent; ");     		
 	 		stackPane.setStyle(
 	 	    		   //"-fx-border-style: none; "
 	 	       			"-fx-background-color: transparent; "
 	 	       			//+ "-fx-background-radius: 3px;"
 	 	    		   );
 	 		
 	 		Scene scene = new Scene(stackPane);//, 150, 150);
 	 		scene.setFill(Color.TRANSPARENT);
 	 		indicator.setText("Saving");
 			savingCircleStage = new Stage();
 			savingCircleStage.initOwner(stage);
 			savingCircleStage.initModality(Modality.WINDOW_MODAL); // Modality.NONE is by default if initModality() is NOT specified.
 			savingCircleStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
 			savingCircleStage.initStyle (StageStyle.TRANSPARENT);
 			savingCircleStage.setScene(scene);
 			savingCircleStage.hide();
	 	}
 		
 		else if (type == PAUSED) {
 			messagePopup = new MessagePopup();
	 	}
 		
		else
			System.out.println("MainScene's createProgressCircle() : type is invalid");
 		
 	}


 	public void showWaitStage(int type) {
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
				if (type == LOADING) {
					setMonitor(loadingCircleStage);
					loadingCircleStage.show();
				}
				else if (type == SAVING) {
					stopPausePopup();
					setMonitor(savingCircleStage);
					savingCircleStage.show();
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
		if (type == LOADING)
			loadingCircleStage.hide();
		else if (type == SAVING)
			savingCircleStage.hide();
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
		if (anchorDesktopPane == null) {
			StackPane pane = new StackPane();//starfield);
			pane.setPrefHeight(sceneWidth.get());
			pane.setPrefWidth(sceneHeight.get());

			StartUpLocation startUpLoc = new StartUpLocation(pane.getPrefWidth(), pane.getPrefHeight());		 
		}
		else {
			StartUpLocation startUpLoc = new StartUpLocation(scene.getWidth(), scene.getHeight());
	        double xPos = startUpLoc.getXPos();
	        double yPos = startUpLoc.getYPos();
	        // Set Only if X and Y are not zero and were computed correctly
	        if (xPos != 0 && yPos != 0) {
	            stage.setX(xPos);
	            stage.setY(yPos);
	        } 
	        
            stage.centerOnScreen();	        
		}
	}
	
 	
	// 2016-10-01 Added mainSceneExecutor for executing wait stages		
    public void startMainSceneExecutor() {
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
	
	public void setSettlement(Settlement s) {
		Platform.runLater(() -> {
			//if (!desktop.isToolWindowOpen(SettlementWindow.NAME))
			openMap();
			sBox.getSelectionModel().select(s);
		});
	}
	
	//public CheckComboBox<String> getMapLabelBox() {
	//	return mapLabelBox;
	//}
	
	public void sendSnackBar(String msg) {
		snackbar.fireEvent(new SnackbarEvent(msg, "UNDO",3000,(b)->{}));
	}
	

	/**
	 * Speeds up the time it takes to display JavaFX's tooltip 
	 * @param node
	 * @param tooltip
	 */
	@SuppressWarnings("restriction")
	public void setQuickToolTip(Node n, Tooltip tt) {
		
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
	
	public void destroy() {
		quote = null;
		messagePopup = null;		
		topFlapBar = null;	
	    //timeStamp = null;
	    memUsedText = null;
		//memBtn = null;
		//clkBtn = null;	
		lastSaveBar = null;
		//statusBar = null;
		flyout = null;
		marsNetButton = null;
		chatBox = null;
		desktopPane = null;
		anchorDesktopPane = null;
		//borderPane = null;
		newSimThread = null;
		stage = null;
		loadingCircleStage = null;
		savingCircleStage = null;
		pausingCircleStage = null;
		mainTab = null;
		//nodeTab = null;
		//dndTabPane = null;
		timeline = null;
		notificationPane = null;
		desktop.destroy();
		desktop = null;
		menuBar = null;
		marsNode = null;
		transportWizard = null;
		constructionWizard = null;

	}

}
