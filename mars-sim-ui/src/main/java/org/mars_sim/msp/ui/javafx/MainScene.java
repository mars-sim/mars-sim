/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.1.0 2016-10-01
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import static javafx.geometry.Orientation.VERTICAL;

//import com.jidesoft.swing.MarqueePane;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.eclipse.fx.ui.controls.tabpane.DndTabPane;

import com.sun.management.OperatingSystemMXBean;

import de.codecentric.centerdevice.MenuToolkit;

import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.awt.Toolkit;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
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
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;
import org.mars_sim.msp.ui.swing.tool.StartUpLocation;
import org.mars_sim.msp.ui.swing.tool.construction.ConstructionWizard;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.TransportWizard;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.PlannerWindow;
import org.reactfx.inhibeans.property.SimpleBooleanProperty;
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

	public static final int LOADING = 0;
	public static final int SAVING = 1;
	public static final int PAUSED = 2; 
	
	private static final String PAUSE_MSG = " [ PAUSE ]  ESC to resume  ";
	private static final String LAST_SAVED = "Last Saved : ";
	
	private static int theme = 7; // 7 is the standard nimrod theme

	public static int chatBoxHeight = 256;

	private int memMax;
	private int memTotal;
	private int memUsed, memUsedCache;
	private int memFree;
	private int processCpuLoad;

	private boolean flag = true;
	private boolean isMainSceneDoneLoading = false;
	public static boolean menuBarVisible = false;
	private boolean isMarsNetOpen = false;
	private boolean onMenuBarCache = false;
	
	private double width = 1286;//1366-80;
	private double height = 688; //768-80;

	private volatile transient ExecutorService mainSceneExecutor; 
    	
	private String lookAndFeelTheme = "nimrod";
	private String title = null;
	private String dir = null;
	private String oldLastSaveStamp = null;

	private StringProperty timeStamp;
    private final BooleanProperty hideProperty = new SimpleBooleanProperty();

	private File fileLocn = null;
	
	private Thread newSimThread;

    private MenuItem navMenuItem = registerAction(new MenuItem("Navigator", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.globe.wire.png")))));
    private MenuItem mapMenuItem = registerAction(new MenuItem("Map", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.map.folds.png")))));
    private MenuItem missionMenuItem = registerAction(new MenuItem("Mission", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.flag.wavy.png")))));
    private MenuItem monitorMenuItem = registerAction(new MenuItem("Monitor", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.eye.png")))));
    private MenuItem searchMenuItem = registerAction(new MenuItem("Search", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.magnify.png")))));
    private MenuItem eventsMenuItem = registerAction(new MenuItem("Events", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.page.new.png")))));

	private Label timeText, lastSaveText; //Text timeText	
	private Text memUsedText;

	private Button memBtn, clkBtn;//, cpuBtn;
	private ToggleButton marsNetButton, menubarButton;

	private Stage stage, loadingCircleStage, savingCircleStage, pausingCircleStage;
	private AnchorPane anchorPane;
	private SwingNode swingNode;
	private StatusBar statusBar;
	private Flyout flyout;

	private ChatBox chatBox;
	private StackPane chatBoxPane;
	private StackPane swingPane;
	private Tab swingTab;
	private Tab nodeTab;
	private BorderPane borderPane;
	private DndTabPane dndTabPane;
	private ESCHandler esc = null;

	private Timeline timeline;
	private static NotificationPane notificationPane;

	private ObservableList<Screen> screens;
	private DecimalFormat twoDigitFormat = new DecimalFormat(Msg.getString("twoDigitFormat")); //$NON-NLS-1$

	private static MainDesktopPane desktop;
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
	
	private SettlementWindow settlementWindow; 
	
	/**
	 * Constructor for MainScene
	 *
	 * @param stage
	 */
	public MainScene(Stage stage) {
		//logger.info("MainScene's constructor() is on " + Thread.currentThread().getName() + " Thread");
		this.stage = stage;
		this.isMainSceneDoneLoading = false;

		//logger.info("OS is " + OS);
		stage.setMinWidth(width);//1024);
		stage.setMinHeight(height);//480);
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
	 * Calls an thread executor to submit MainSceneTask
	 */
	public void prepareMainScene() {
			UIConfig.INSTANCE.useUIDefault();
	}

	/**
	 * Sets up the UI theme and the two timers as a thread pool task
	 */
	public class MainSceneTask implements Runnable {
		public void run() {
			//logger.info("MainScene's MainSceneTask is in " + Thread.currentThread().getName() + " Thread");
			// Set look and feel of UI.
			UIConfig.INSTANCE.useUIDefault();
		}
	}

	/**
	 * Prepares the transport wizard, construction wizard, autosave timer and earth timer
	 */
	public void prepareOthers() {
		//logger.info("MainScene's prepareOthers() is on " + Thread.currentThread().getName() + " Thread");
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
	 * initializes the scene
	 *
	 * @return Scene
	 */
	@SuppressWarnings("unchecked")
	public Scene initializeScene() {
		//logger.info("MainScene's initializeScene() is on " + Thread.currentThread().getName() + " Thread");

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
			
		// Create group to hold swingNode1 which holds the swing desktop
		swingPane = new StackPane();
		swingNode = new SwingNode();
		
		createSwingNode();
		swingPane.getChildren().add(swingNode);
		swingPane.setPrefWidth(width);
		swingPane.setPrefHeight(height);

	    //2015-11-11 Added createFlyout()
		flyout = createFlyout();
        flag = false;
        
        EffectUtilities.makeDraggable(flyout.getStage(), chatBox);
					
		// Create ControlFX's StatusBar
		statusBar = createStatusBar();

		// Create menuBar
		menuBar = new MainSceneMenu(this, desktop);
		((MenuBar)menuBar).useSystemMenuBarProperty().set(true);
  
		// Create BorderPane
		borderPane = new BorderPane();

		borderPane.setCenter(swingPane);
		borderPane.setMinWidth(width);//1024);

		anchorPane = new AnchorPane();
		anchorPane.setMinWidth(width);//1024);
		anchorPane.setMinHeight(height);//480);

		borderPane.prefHeightProperty().bind(anchorPane.heightProperty());
		borderPane.prefWidthProperty().bind(anchorPane.widthProperty());

        AnchorPane.setBottomAnchor(borderPane, 0.0);
        AnchorPane.setLeftAnchor(borderPane, 0.0);
        AnchorPane.setRightAnchor(borderPane, 0.0);
        AnchorPane.setTopAnchor(borderPane, 0.0);//31.0);
        
        borderPane.setBottom(statusBar);

 
		if (OS.contains("mac")) {
	        //menubarButton = new ToggleButton();
	        
	        AnchorPane.setLeftAnchor(marsNetButton, 5.0);
	        AnchorPane.setBottomAnchor(marsNetButton, 35.0);    
	        
	        anchorPane.getChildren().addAll(borderPane, marsNetButton);

	        //borderPane.setTop(topFlapBar);  
		}
		else {
			
			menubarButton = new ToggleButton();
	        menubarButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/menubar_36.png"))));
	        menubarButton.setStyle(
	        		"-fx-background-color: transparent;" +     		    		
	     		   "-fx-shadow-highlight-color : transparent;" +  // if you don't want a 3d effect highlight.
	     		   "-fx-outer-border : transparent;" +  // if you don't want a button border.
	     		   "-fx-inner-border : transparent;" +  // if you don't want a button border.
	     		   "-fx-focus-color: transparent;" +  // if you don't want any focus ring.
	     		   "-fx-faint-focus-color : transparent;" +  // if you don't want any focus ring.
	     		   "-fx-base : orange;" + // if you want a gradient shaded button that lightens on hover and darkens on arming.
	     		  // "-fx-body-color: palegreen;" + // instead of -fx-base, if you want a flat shaded button that does not lighten on hover and darken on arming.
	     		   //"-fx-font-size: 80px;"
	           		"-fx-background-radius: 2px;"
	     		   );
	    
	        AnchorPane.setLeftAnchor(marsNetButton, 45.0);
	        AnchorPane.setBottomAnchor(marsNetButton, 35.0);       
	        AnchorPane.setLeftAnchor(menubarButton, 5.0);
	        AnchorPane.setBottomAnchor(menubarButton, 35.0);
	        /**
	         * Instantiate a BorderSlideBar for each child layouts
	         */
	        topFlapBar = new BorderSlideBar(30, menubarButton, Pos.TOP_LEFT, menuBar);
	        borderPane.setTop(topFlapBar);        
	        
	        anchorPane.getChildren().addAll(borderPane, marsNetButton, menubarButton);//toolbar);

		}
		
    	Scene scene = new Scene(anchorPane, width, height);//, Color.BROWN);
		anchorPane.prefHeightProperty().bind(scene.heightProperty());
		anchorPane.prefWidthProperty().bind(scene.widthProperty());
 

		return scene;
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

		// SwingUtilities is needed for MacOSX compatibility
		SwingUtilities.invokeLater(() -> {	
			// 2016-06-17 Added checking for OS. 
			// Note: NIMROD theme lib doesn't work on linux 
			if (OS.equals("linux"))
				setLookAndFeel(2);			
			else 
				setLookAndFeel(1);			
		});
		
		//logger.info("done with MainScene's initializeTheme()");
	}

	/*
	 * Changes the theme skin of desktop
	 */
	public void changeTheme(int theme) {
		this.theme = theme;
		swingPane.getStylesheets().clear();
		if (menuBar.getStylesheets() != null) menuBar.getStylesheets().clear();
		statusBar.getStylesheets().clear();	

		String cssFile;
	
		//logger.info("MainScene's changeTheme()");
		if (theme == 1) { // olive green
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
			updateThemeColor(6, Color.rgb(0,107,184), Color.rgb(0,107,184), cssFile); // CADETBLUE // Color.rgb(23,138,255)
			lookAndFeelTheme = "Snow";

		} else if (theme == 7) { // standard
			cssFile = "/fxui/css/nimrodskin.css";
			updateThemeColor(7, Color.rgb(156,77,0), Color.rgb(156,77,0), cssFile); //DARKORANGE, CORAL
			lookAndFeelTheme = "nimrod";

		}

		//logger.info("done with MainScene's changeTheme()");
	}

	/*
	 * Updates the theme colors of statusBar, swingPane and menuBar
	 */
	// 2015-08-29 Added updateThemeColor()
	public void updateThemeColor(int theme, Color txtColor, Color btnTxtColor, String cssFile) {
		swingPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		if (!OS.contains("mac"))
			menuBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		
		// Note : menu bar color
		// orange theme : F4BA00
		// blue theme : 3291D2
		
		String color = txtColor.toString().replace("0x", "");
		timeText.setTextFill(txtColor);
		lastSaveText.setTextFill(txtColor);
		statusBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		
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
			if (!OS.contains("mac"))
				menubarButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/blue_menubar_36.png"))));
			marsNetButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/blue_chat_36.png"))));
		}
		else if (theme == 7) {
			if (!OS.contains("mac"))
				menubarButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/orange_menubar_36.png"))));
			marsNetButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/orange_chat_36.png"))));
		}
		
		chatBox.update();    
		
	}
	
	/**
	 * Creates and starts the earth timer
	 */
	public void startEarthTimer() {
		// Set up earth time text update
		timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), ae -> updateStatusBarText()));
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
    public Flyout createFlyout() {
     	marsNetButton = new ToggleButton();
        marsNetButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/gray_chat_36.png")))); //" MarsNet ");       
        marsNetButton.setStyle(
        			"-fx-background-color: transparent;" +   
        		   "-fx-shadow-highlight-color : transparent;" +  // if you don't want a 3d effect highlight.
        		   "-fx-outer-border : transparent;" +  // if you don't want a button border.
        		   "-fx-inner-border : transparent;" +  // if you don't want a button border.
        		   "-fx-focus-color: transparent;" +  // if you don't want any focus ring.
        		   "-fx-faint-focus-color : transparent;" +  // if you don't want any focus ring.
        		   "-fx-base : orange;" + // if you want a gradient shaded button that lightens on hover and darkens on arming.
        		  // "-fx-body-color: palegreen;" + // instead of -fx-base, if you want a flat shaded button that does not lighten on hover and darken on arming.
        		   //"-fx-font-size: 80px;"
              		"-fx-background-radius: 2px;"
        		   );
        
        
        flyout = new Flyout(marsNetButton, createChatBox());
        marsNetButton.setId("marsNetButton");
        marsNetButton.setTooltip(new Tooltip ("Toggle on and off MarsNet"));
        //marsNetButton.setPadding(new Insets(0, 0, 0, 0)); // Warning : this significantly reduce the size of the button image
    	chatBox.update();
        marsNetButton.setOnAction(e -> {
            if (!flag) 
            	chatBox.update();
            if (marsNetButton.isSelected()) {
            	//System.out.println("flyingout : " + marsNetButton.isSelected());
                flyout.flyout();              
                // 2016-06-17 Added update() to show the initial system greeting
                chatBox.getAutoFillTextBox().getTextbox().clear();
                chatBox.getAutoFillTextBox().getTextbox().requestFocus();
                ToggleMarsNetButton(true);
            } else {
            	//System.out.println("dismissing : " + marsNetButton.isSelected());
            	// 2016-06-17 Added closeChatBox() to display a disconnection msg 
                flyout.dismiss();
                ToggleMarsNetButton(false);
            }

        });
       
        return flyout;
    }
    
    public void ToggleMarsNetButton(boolean value) {
    	marsNetButton.setSelected(value);
    }
 
    public boolean isToggleMarsNetButtonSelected() {
    	return marsNetButton.isSelected();
    }
    
    public void fireMarsNetButton() {
    	marsNetButton.fire();
    }
    
    public Flyout getFlyout() {
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
  	
  	public void setChatBoxPaneHeight(double value) {
  		chatBoxPane.setMinHeight(value);
 		chatBoxPane.setPrefHeight(value);
 		chatBoxPane.setMaxHeight(value);
  	}
  	
	/*
	 * Creates the status bar for MainScene
	 */
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
		notificationPane = new NotificationPane(dndTabPane);

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
	 * Updates the cpu loads, memory usage and time text in the status bar
	 */
	public void updateStatusBarText() {
 
		String t = earthClock.getTimeStamp2();
		// Check if new simulation is being created or loaded from file.
		if (sim.isUpdating() || masterClock.isPaused()) {
			timeText.setText(PAUSE_MSG + t + "  ");		
		}	
		else { 	
			timeText.setText("  " + t + "  ");
			
			//2016-09-15 Added oldLastSaveStamp and newLastSaveStamp
			String newLastSaveStamp = sim.instance().getLastSave();
			if (!oldLastSaveStamp.equals(newLastSaveStamp)) {
				oldLastSaveStamp = newLastSaveStamp.replace("_", " ");
				lastSaveText.setText(LAST_SAVED + oldLastSaveStamp + " ");
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
					} );
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
				messagePopup.popAMessage(" PAUSED", " ", " ", stage, Pos.TOP_CENTER, PNotification.PAUSE_ICON)
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
		desktop.getMarqueeTicker().pauseMarqueeTimer(true);
		masterClock.setPaused(true);
	}

	/**
	 * Unpauses the marquee timer and unpauses the simulation.
	 */
	public void unpauseSimulation() {	
		desktop.getMarqueeTicker().pauseMarqueeTimer(false);
		masterClock.setPaused(false);
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
		statusBar = null;
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
						changed = true;
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
		desktop = new MainDesktopPane(this);
		SwingUtilities.invokeLater(() -> {	
			// 2016-06-17 Added checking for OS. 
			// Note: NIMROD theme lib doesn't work on linux 
			if (OS.equals("linux"))
				setLookAndFeel(2);			
			else 
				setLookAndFeel(1);				
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
		return width;
	}

	public double getHeight() {
		return height;
	}

	public BorderPane getBorderPane() {
		return borderPane;
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
/*
 		ProgressIndicator indicator = new ProgressIndicator();
 		//MaskerPane indicator = new MaskerPane();
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
 		
 		Scene scene = new Scene(stackPane, 150, 150);
 		scene.setFill(Color.TRANSPARENT);
 		//scene.setStyle("-fx-background-color: transparent; ");  
*/
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
		if (anchorPane == null) {
			StackPane root = new StackPane();//starfield);
			root.setPrefHeight(width);
			root.setPrefWidth(height);
			StartUpLocation startUpLoc = new StartUpLocation(root.getPrefWidth(), root.getPrefHeight());		 
		}
		else {
			StartUpLocation startUpLoc = new StartUpLocation(anchorPane.getPrefWidth(), anchorPane.getPrefHeight());
	        double xPos = startUpLoc.getXPos();
	        double yPos = startUpLoc.getYPos();
	        // Set Only if X and Y are not zero and were computed correctly
	        if (xPos != 0 && yPos != 0) {
	            stage.setX(xPos);
	            stage.setY(yPos);
	            stage.centerOnScreen();
	        } else {
	            stage.centerOnScreen();
	        }  
		}
	}
	
 	
	// 2016-10-01 Added mainSceneExecutor for executing wait stages		
    public void startMainSceneExecutor() {
        //logger.info("Simulation's startSimExecutor() is on " + Thread.currentThread().getName() + " Thread");
    	// INFO: Simulation's startSimExecutor() is on JavaFX-Launcher Thread
    	mainSceneExecutor = Executors.newSingleThreadExecutor();
    }

    public ExecutorService getSimExecutor() {
    	return mainSceneExecutor;
    }
    
	public void destroy() {
		quote = null;
		messagePopup = null;		
		topFlapBar = null;	
	    navMenuItem = null;
	    mapMenuItem = null;
	    missionMenuItem = null;
	    monitorMenuItem = null;
	    searchMenuItem = null;
	    eventsMenuItem = null;
	    timeStamp = null;
	    memUsedText = null;
		memBtn = null;
		clkBtn = null;	
		statusBar = null;
		flyout = null;
		marsNetButton = null;
		chatBox = null;
		swingPane = null;
		anchorPane = null;
		borderPane = null;
		newSimThread = null;
		stage = null;
		loadingCircleStage = null;
		savingCircleStage = null;
		pausingCircleStage = null;
		swingTab = null;
		nodeTab = null;
		dndTabPane = null;
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
