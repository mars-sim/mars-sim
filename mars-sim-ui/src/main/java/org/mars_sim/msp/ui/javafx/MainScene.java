/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.08 2016-06-15
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import static javafx.geometry.Orientation.VERTICAL;

//import com.jidesoft.swing.MarqueePane;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;
//import com.sibvisions.rad.ui.javafx.ext.mdi.FXDesktopPane;
//import com.sibvisions.rad.ui.javafx.ext.mdi.FXInternalWindow;

import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.eclipse.fx.ui.controls.tabpane.DndTabPane;
//import org.eclipse.fx.ui.controls.tabpane.DndTabPaneFactory;
//import org.eclipse.fx.ui.controls.tabpane.DndTabPaneFactory.FeedbackType;
//import org.eclipse.fx.ui.controls.tabpane.skin.DnDTabPaneSkin;

//import jfxtras.scene.menu.CornerMenu;

import com.sun.management.OperatingSystemMXBean;

import de.codecentric.centerdevice.MenuToolkit;
//import eu.hansolo.enzo.notification.Notification;
//import eu.hansolo.enzo.notification.Notification.Notifier;
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

	private static final int EDGE_DETECTION_PIXELS_Y = 35;
	private static final int EDGE_DETECTION_PIXELS_X = 200;
	
	// Categories of loading and saving simulation
	public static final int DEFAULT = 1;
	public static final int AUTOSAVE = 2;
	public static final int OTHER = 3; // load other file
	public static final int SAVE_AS = 3; // save as other file

	private static int theme = 7; // 7 is the standard nimrod theme

    private MenuItem navMenuItem = registerAction(new MenuItem("Navigator", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.globe.wire.png")))));
    private MenuItem mapMenuItem = registerAction(new MenuItem("Map", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.map.folds.png")))));
    private MenuItem missionMenuItem = registerAction(new MenuItem("Mission", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.flag.wavy.png")))));
    private MenuItem monitorMenuItem = registerAction(new MenuItem("Monitor", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.eye.png")))));
    private MenuItem searchMenuItem = registerAction(new MenuItem("Search", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.magnify.png")))));
    private MenuItem eventsMenuItem = registerAction(new MenuItem("Events", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.page.new.png")))));

	private int memMax;
	private int memTotal;
	private int memUsed, memUsedCache;
	private int memFree;
	private int processCpuLoad;
	//private int systemCpuLoad;

	private boolean isMainSceneDoneLoading = false;
	public static boolean menuBarVisible = false;
	private boolean isMarsNetOpen = false;
	private boolean onMenuBarCache = false;
	
	private double width = 1286;//1366-80;
	private double height = 688; //768-80;

	private StringProperty timeStamp;
    private final BooleanProperty hideProperty = new SimpleBooleanProperty();

	private String lookAndFeelTheme = "nimrod";

	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;

	private Label timeText; //Text timeText	
	private Text memUsedText;

	private Button memBtn, clkBtn;//, cpuBtn;
	private ToggleButton marsNetButton, menubarButton;
	//private MaterialDesignToggleButton marsNetButton;

	private Stage stage, loadingCircleStage, savingCircleStage, pausedCircleStage;
	private AnchorPane anchorPane;
	private SwingNode swingNode;
	private StatusBar statusBar;
	private Flyout flyout;

	private ChatBox chatBox;
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
	@SuppressWarnings("restriction")
	//private OperatingSystemMXBean osBean;
	
	private static MainDesktopPane desktop;
	private MainSceneMenu menuBar;
	//private MenuBar menuBar;
	
	private MarsNode marsNode;
	private TransportWizard transportWizard;
	private ConstructionWizard constructionWizard;

	private QuotationPopup quote;
	private MessagePopup messagePopup;
	
	private BorderSlideBar topFlapBar;

	/**
	 * Constructor for MainScene
	 *
	 * @param stage
	 */
	public MainScene(Stage stage) {
		//logger.info("MainScene's constructor() is on " + Thread.currentThread().getName() + " Thread");
		this.stage = stage;
		this.isMainSceneDoneLoading = false;
		
		//stage.setResizable(true);
		stage.setMinWidth(width);//1024);
		stage.setMinHeight(height);//480);
		//stage.setHeight(600);
		//stage.setMaxWidth(1920);
		//stage.setMaxHeight(1200);
		stage.setFullScreenExitHint("Use Ctrl+F (or Meta+C in Mac) to toggle full screen mode");
		stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        //stage.setFullScreen(false);
        //stage.setFullScreen(true);
        
		// Detect if a user hits the top-right close button
		stage.setOnCloseRequest(e -> {
			boolean result = alertOnExit();
			if (!result)
				e.consume();
			//else {
			//	Platform.exit();
			//	System.exit(0);
			//}
		} );

		
		// Detect if a user hits ESC
		esc = new ESCHandler();
		setEscapeEventHandler(true);
		
		//createProgressCircle("Loading");
		
	}

	// 2015-12-28 Added setEscapeEventHandler()
	public void setEscapeEventHandler(boolean value) {
		if (value) {
			//esc = new ESCHandler();
			stage.addEventHandler(KeyEvent.KEY_PRESSED, esc);
			//System.out.println("addEventHandler()");
		}
		else {
			stage.removeEventHandler(KeyEvent.KEY_PRESSED, esc);
			//System.out.println("removeEventHandler()");
		}
	}

	class ESCHandler implements EventHandler<KeyEvent> {

		public void handle(KeyEvent t) {
			if (t.getCode() == KeyCode.ESCAPE) {
				boolean isOnPauseMode = Simulation.instance().getMasterClock().isPaused();
				if (isOnPauseMode) {
					//System.out.println("ESCHandler : calling unpauseSimulation()");
					unpauseSimulation();
					//desktop.getTimeWindow().enablePauseButton(true);
				}
				else {
					//System.out.println("ESCHandler : calling pauseSimulation()");
					pauseSimulation();
					//desktop.getTimeWindow().enablePauseButton(false);
				}
				// Toggle the full screen mode to OFF in the pull-down menu
				// under setting
				//menuBar.exitFullScreen();
				// close the MarsNet side panel
				//openSwingTab();
			}
		}


	}


	/**
	 * Calls an thread executor to submit MainSceneTask
	 */
	public void prepareMainScene() {
		//createProgressCircle();
		//showLoadingStage();
		//logger.info("MainScene's prepareMainScene() is in " + Thread.currentThread().getName() + " Thread");
		Simulation.instance().getSimExecutor().submit(new MainSceneTask());
	}

	/**
	 * Sets up the UI theme and the two timers as a thread pool task
	 */
	public class MainSceneTask implements Runnable {
		public void run() {
			//logger.info("MainScene's MainSceneTask is in " + Thread.currentThread().getName() + " Thread");
			// Set look and feel of UI.
			UIConfig.INSTANCE.useUIDefault();
			//SwingUtilities.invokeLater(() -> {	
				// 2016-06-17 Added checking for OS. 
				// Note: NIMROD theme lib doesn't work on linux 
			//	if (OS.equals("linux"))
			//		setLookAndFeel(2);			
			//	else 
			//		setLookAndFeel(1);			
			//});
			// System.out.println("done running createMainScene()");
		}
	}

	/**
	 * Prepares the transport wizard, construction wizard, autosave timer and earth timer
	 */
	public void prepareOthers() {
		//logger.info("MainScene's prepareOthers() is on " + Thread.currentThread().getName() + " Thread");
		//startAutosaveTimer();
		startEarthTimer();
		transportWizard = new TransportWizard(this, desktop);
		constructionWizard = new ConstructionWizard(this, desktop);
		//logger.info("done with MainScene's prepareOthers()");
	}

	/**
	 * Pauses sim and opens the transport wizard
	 * @param buildingManager
	 */
	public synchronized void openTransportWizard(BuildingManager buildingManager) {
		logger.info("MainScene's openTransportWizard() is in " + Thread.currentThread().getName() + " Thread");
		// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
		// 2015-12-16 Track the current pause state
		boolean previous = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			pauseSimulation();
	    	//System.out.println("previous is false. Paused sim");
		}
		desktop.getTimeWindow().enablePauseButton(false);

		Platform.runLater(() -> {
			//System.out.println("calling transportWizard.deliverBuildings() ");
			transportWizard.deliverBuildings(buildingManager);
			//System.out.println("ended transportWizard.deliverBuildings() ");
		});

		boolean now = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			if (now) {
				unpauseSimulation();
   	    		//System.out.println("previous is false. now is true. Unpaused sim");
			}
		} else {
			if (!now) {
				unpauseSimulation();
   	    		//System.out.println("previous is true. now is false. Unpaused sim");
			}
		}
		desktop.getTimeWindow().enablePauseButton(true);

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
		boolean previous = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			pauseSimulation();
	    	//System.out.println("previous is false. Paused sim");
		}
		desktop.getTimeWindow().enablePauseButton(false);

		Platform.runLater(() -> {
				constructionWizard.selectSite(mission);
			});

		boolean now = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			if (now) {
				unpauseSimulation();
   	    		//System.out.println("previous is false. now is true. Unpaused sim");
			}
		} else {
			if (!now) {
				unpauseSimulation();
   	    		//System.out.println("previous is true. now is false. Unpaused sim");
			}
		}
		desktop.getTimeWindow().enablePauseButton(true);

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
		//marsNode = new MarsNode(this, stage);
	
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
		logger.info("DPI Scale Factor is " + dpiScaleFactor);
		
		
		// Create group to hold swingNode1 which holds the swing desktop
		swingPane = new StackPane();
		swingNode = new SwingNode();
		
		createSwingNode();
		swingPane.getChildren().add(swingNode);
		swingPane.setPrefWidth(width);
		swingPane.setPrefHeight(height);

	    //2015-11-11 Added createFlyout()
		flyout = createFlyout();
        EffectUtilities.makeDraggable(flyout.getStage(), chatBox);
					
		// Create ControlFX's StatusBar
		statusBar = createStatusBar();
	    //System.out.println("height : " + statusBar.getHeight());

        Button memoryButton = new Button();
        memoryButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/memory_chip_48.png"))));
        memoryButton.setOnAction(e -> {
        	memoryButton.setTooltip(new Tooltip ("Memory Usage : " + memUsed + " MB"));
            memoryButton.getTooltip().show(stage);
            Timer timer = FxTimer.runLater(
    				java.time.Duration.ofMillis(2000),
    		        () -> memoryButton.getTooltip().hide());
        });
        
        //memoryButton.setTooltip(new Tooltip ("Memory Usage : " + memUsed + " MB"));
        
        //Button dateTimeButton = new Button("", new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/memory_chip_48.png"))));
/*
		ToolBar miscToolBar = new ToolBar(
				marsNetButton
				//new Separator(),
				//memoryButton
				//new Separator()
				);
		
		miscToolBar.setOrientation(javafx.geometry.Orientation.VERTICAL);
		miscToolBar.setStyle(
	    		   //"-fx-border-style: none; "
	    		   //"-fx-background-color: #231d12; "
	       			"-fx-background-color: transparent; "
	       			//+ "-fx-background-radius: 1px;"
	    		   );
		//.setOpacity(0); 
*/		
		//VBox bottomBox = new VBox();
		//bottomBox.getChildren().addAll(statusBar);

		// Create menuBar
		menuBar = new MainSceneMenu(this, desktop);
		((MenuBar)menuBar).useSystemMenuBarProperty().set(true);
       
        //createMenuBar();
		
		// Create BorderPane
		borderPane = new BorderPane();
		
		//VBox topBox = new VBox();
		//topBox.getChildren().addAll(menuBar, toolBar);
		
		//borderPane.setTop(topBox);
		//borderPane.setBottom(bottomBox);

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

		
		borderPane.setCenter(swingPane);
		borderPane.setMinWidth(width);//1024);
		//borderPane.setMinHeight(height-30);//480);
          
		//rootStackPane = new StackPane(borderPane);

/*		
		hideProperty.setValue(true);
		
		// binding to hideProperty
        // card back is visible if hide property is true
		menuBar.visibleProperty().bind(hideProperty.not());
        
		Pane pane = new Pane();
		pane.setStyle("-fx-background-color: transparent;");
		// card front is visible if property is false, see "not()" call
        pane.visibleProperty().bind(hideProperty);
*/
        //setOnMouseClicked((e)-> {
            // click on card to flip it
        //    hideProperty.setValue(!hideProperty.getValue());
        //});
		
	
    	//System.out.println("w : " + scene.getWidth() + "   h : " + scene.getHeight());	
		
		anchorPane = new AnchorPane();
		anchorPane.setMinWidth(width);//1024);
		anchorPane.setMinHeight(height);//480);
        //anchorPane.setPrefHeight(431);
        //anchorPane.setPrefWidth(600);       

        //BorderPane borderPane = new BorderPane();
		borderPane.prefHeightProperty().bind(anchorPane.heightProperty());
		borderPane.prefWidthProperty().bind(anchorPane.widthProperty());

        AnchorPane.setBottomAnchor(borderPane, 0.0);
        AnchorPane.setLeftAnchor(borderPane, 0.0);
        AnchorPane.setRightAnchor(borderPane, 0.0);
        AnchorPane.setTopAnchor(borderPane, 0.0);//31.0);
        
        
        
/*
        ToolBar toolbar = new ToolBar();
        toolbar.setPrefWidth(width);
		toolbar.setStyle(
	    		   //"-fx-border-style: none; "
	    		   //"-fx-background-color: #231d12; "
	       			"-fx-background-color: transparent; "
	       			//+ "-fx-background-radius: 1px;"
	    		   );
		
        //AnchorPane.setTopAnchor(toolbar, 0.0);
        AnchorPane.setLeftAnchor(toolbar, 0.0);
        AnchorPane.setRightAnchor(toolbar, 0.0);
        AnchorPane.setBottomAnchor(toolbar, 30.0);
*/        
        

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
        //AnchorPane.setRightAnchor(marsNetButton, 0.0);
        AnchorPane.setBottomAnchor(marsNetButton, 35.0);
        
        AnchorPane.setLeftAnchor(menubarButton, 5.0);
        //AnchorPane.setRightAnchor(menubarButton, 5.0);
        //AnchorPane.setTopAnchor(menubarButton, 35.0);
        AnchorPane.setBottomAnchor(menubarButton, 35.0);
        
        //Button buttonTop = new Button("Top");
        //Label labelTop = new Label("Top");
        
        //Button buttonLeft = new Button("Left");
        //Label labelLeft = new Label("Left");
        
        //Button buttonBottom = new Button("Bottom");
        //Label labelBottom = new Label("Bottom");
        //labelBottom.setTextAlignment(TextAlignment.CENTER);
        
        //Button buttonRight = new Button("Right");
        //Label labelRight = new Label("Right");

		//final String os = System.getProperty ("os.name");
		if (OS.equals("mac os x")) {
		//if (os != null && os.startsWith ("Mac"))
		  //menuBar.useSystemMenuBarProperty().set(true);		
		}
		else {
	        /**
	         * Instantiate a BorderSlideBar for each child layouts
	         */
	        topFlapBar = new BorderSlideBar(30, menubarButton, Pos.TOP_LEFT, menuBar);
	        borderPane.setTop(topFlapBar);
		}
		
        borderPane.setBottom(statusBar);
        
        
        //BorderSlideBar leftFlapBar = new BorderSlideBar(70, buttonLeft, Pos.BASELINE_LEFT, miscToolBar);
        //leftFlapBar.setOpacity(0.5);
        //borderPane.setLeft(leftFlapBar);

        //BorderSlideBar bottomFlapBar = new BorderSlideBar(30, buttonBottom, Pos.BOTTOM_LEFT, statusBar);
        //borderPane.setBottom(bottomFlapBar);
        
        //BorderSlideBar rightFlapBar = new BorderSlideBar(100, buttonRight, Pos.BASELINE_RIGHT, labelRight);
        //borderPane.setRight(rightFlapBar);
        
        //toolbar.getItems().addAll(menubarButton, marsNetButton);//, buttonBottom);//, buttonLeft, buttonRight);

        anchorPane.getChildren().addAll(borderPane, marsNetButton, menubarButton);//toolbar);

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
		//marsNode.getFXDesktopPane().getStylesheets().clear();	
		//marsNetButton.getStylesheets().clear();
		String cssColor;
	
		//logger.info("MainScene's changeTheme()");
		if (theme == 1) { // olive green
			cssColor = "/fxui/css/oliveskin.css";
			updateThemeColor(1, Color.GREEN, Color.PALEGREEN, cssColor); //DARKOLIVEGREEN
			//notificationPane.getStyleClass().remove(NotificationPane.STYLE_CLASS_DARK);
			//notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/oliveskin.css").toExternalForm());
			lookAndFeelTheme = "LightTabaco";
			

		} else if (theme == 2) { // burgundy red
			cssColor = "/fxui/css/burgundyskin.css";
			updateThemeColor(2, Color.rgb(140,0,26), Color.YELLOW, cssColor); // ORANGERED
			//notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
			//notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/burgundyskin.css").toExternalForm());
			lookAndFeelTheme = "Burdeos";

		} else if (theme == 3) { // dark chocolate
			cssColor = "/fxui/css/darkTabaco.css";
			updateThemeColor(3, Color.DARKGOLDENROD, Color.BROWN, cssColor);
			//notificationPane.getStyleClass().add(getClass().getResource("/fxui/css/mainskin.css").toExternalForm());
			lookAndFeelTheme = "DarkTabaco";

		} else if (theme == 4) { // grey
			cssColor = "/fxui/css/darkGrey.css";
			updateThemeColor(4, Color.DARKSLATEGREY, Color.DARKGREY, cssColor);
			lookAndFeelTheme = "DarkGrey";

		} else if (theme == 5) { // + purple
			cssColor = "/fxui/css/nightViolet.css";
			updateThemeColor(5, Color.rgb(73,55,125), Color.rgb(73,55,125), cssColor); // DARKMAGENTA, SLATEBLUE
			lookAndFeelTheme = "Night";

		} else if (theme == 6) { // + skyblue
			cssColor = "/fxui/css/snowBlue.css";
			updateThemeColor(6, Color.rgb(0,107,184), Color.rgb(0,107,184), cssColor); // CADETBLUE // Color.rgb(23,138,255)
			lookAndFeelTheme = "Snow";

		} else if (theme == 7) { // standard
			cssColor = "/fxui/css/nimrodskin.css";
			updateThemeColor(7, Color.rgb(156,77,0), Color.rgb(156,77,0), cssColor); //DARKORANGE, CORAL
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
		menuBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		
		// Note : menu bar color
		//orange theme : F4BA00
		//blue theme : 3291D2
		
		String color = txtColor.toString().replace("0x", "");
		//System.out.println("txtColor is " + txtColor.toString());
		//System.out.println("btnTxtColor is " + btnTxtColor.toString());
		//System.out.println("cssFile is " + cssFile);
		//System.out.println("color is " + color);

		
		//marsNode.getFXDesktopPane().getStylesheets().add(getClass().getResource(cssColor).toExternalForm());
		
		//memUsedText.setTextFill(txtColor);
		//memMaxText.setFill(txtColor);
		timeText.setTextFill(txtColor);
		
		//systemCpuLoadText.setFill(txtColor);
		//processCpuLoadText.setFill(txtColor);

		statusBar.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

		//memBtn.setTextFill(btnTxtColor);
		//clkBtn.setTextFill(btnTxtColor);
		//cpuBtn.setTextFill(btnTxtColor);
		//marsNetButton.setTextFill(btnTxtColor);

		//memBtn.setStyle("-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
		//clkBtn.setStyle("-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
		//cpuBtn.setStyle("-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
		//marsNetButton.setStyle("-fx-effect: innershadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );");
		
		chatBox.getTitleLabel().setStyle("-fx-text-fill: white;" +
				" -fx-font: bold 12pt 'Corbel';" + 
        		//+ " -fx-effect: dropshadow( one-pass-box , " + cssColor + " , 8 , 0.0 , 2 , 0 );"
        		"-fx-background-color: transparent;" //+
				//"-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.8), 10, 0, 0, 0);"
        		);

		if (theme == 6) {
			menubarButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/blue_menubar_36.png"))));
			marsNetButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/blue_chat_36.png"))));
		}
		else if (theme == 7) {
			menubarButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/orange_menubar_36.png"))));
			marsNetButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/icons/statusbar/orange_chat_36.png"))));
		}
		
		
/*		
        menubarButton.setStyle(
        		"-fx-background-color: transparent;" +
      		   "-fx-shadow-highlight-color : transparent;" +  // if you don't want a 3d effect highlight.
      		   "-fx-outer-border : transparent;" +  // if you don't want a button border.
      		   "-fx-inner-border : transparent;" +  // if you don't want a button border.
      		   "-fx-focus-color: transparent;" +  // if you don't want any focus ring.
      		   "-fx-faint-focus-color : transparent;" +  // if you don't want any focus ring.
      		   //"-fx-base : orange;" + // if you want a gradient shaded button that lightens on hover and darkens on arming.
     		   "-fx-base : derive(#" + color + ", 80%);" + // if you want a gradient shaded button that lightens on hover and darkens on arming.
      		   // "-fx-body-color: palegreen;" + // instead of -fx-base, if you want a flat shaded button that does not lighten on hover and darken on arming.
      		   //"-fx-font-size: 80px;"
            	"-fx-background-radius: 2px;"
 //           	"-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.8), 10, 0, 0, 0);"
      		   );
        
        marsNetButton.setStyle(
        		"-fx-background-color: transparent;" +
       		   "-fx-shadow-highlight-color : transparent;" +  // if you don't want a 3d effect highlight.
       		   "-fx-outer-border : transparent;" +  // if you don't want a button border.
       		   "-fx-inner-border : transparent;" +  // if you don't want a button border.
       		   "-fx-focus-color: transparent;" +  // if you don't want any focus ring.
       		   "-fx-faint-focus-color : transparent;" +  // if you don't want any focus ring.
       		   //"-fx-base : orange;" + // if you want a gradient shaded button that lightens on hover and darkens on arming.
    		   "-fx-base : derive(#" + color + ", 80%);" + // if you want a gradient shaded button that lightens on hover and darkens on arming.
    		   // "-fx-body-color: palegreen;" + // instead of -fx-base, if you want a flat shaded button that does not lighten on hover and darken on arming.
       		   //"-fx-font-size: 80px;"
             	"-fx-background-radius: 2px;"
            	//"-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.7) , 6, 0.0 , 0 , 2 );"
  //          	"-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.8), 10, 0, 0, 0);"           	
       		   );
*/        
        
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
        //marsNetButton = new ToggleButton(" MarsNet ");
        //marsNetButton = new MaterialDesignToggleButton("", new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/appbar.globe.wire.png")))); //" MarsNet ");
    	marsNetButton = new ToggleButton();
    	//marsNetButton = new Button();
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
        marsNetButton.setOnAction(e -> {
            if (marsNetButton.isSelected()) {
                flyout.flyout();
                // 2016-06-17 Added update() to show the initial system greeting
                chatBox.update();
                chatBox.getAutoFillTextBox().getTextbox().clear();
                chatBox.getAutoFillTextBox().getTextbox().requestFocus();
            } else {
            	// 2016-06-17 Added closeChatBox() to display a disconnection msg 
            	chatBox.closeChatBox();
                flyout.dismiss();
                ToggleMarsNetButton(false);
            }
            //if (!isMarsNetOpen) {
            //    flyout.flyout();
            //    isMarsNetOpen = true;
            //} else {
            //    flyout.dismiss();
            //}
        });
       
        return flyout;
    }
    
    public void ToggleMarsNetButton(boolean value) {
    	marsNetButton.setSelected(value);
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
  		StackPane pane = new StackPane(chatBox);
  		pane.setPadding(new Insets(0, 0, 0, 0));
        //pane.setHgap(0);
  		return pane;
  	}

	/*
	 * Creates the status bar for MainScene
	 */
	public StatusBar createStatusBar() {
		if (statusBar == null) {
			statusBar = new StatusBar();
			statusBar.setText("");
		}
		
		statusBar.getRightItems().add(new Separator(VERTICAL));
		statusBar.getRightItems().add(new Separator(VERTICAL));

		memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
		memUsed = memTotal - memFree;
		
		statusBar.getRightItems().add(new Separator(VERTICAL));

		MasterClock master = Simulation.instance().getMasterClock();
		if (master == null) {
			throw new IllegalStateException("master clock is null");
		}
		EarthClock earthclock = master.getEarthClock();
		if (earthclock == null) {
			throw new IllegalStateException("earthclock is null");
		}

		timeText = new Label();
		timeText.setText("  " + timeStamp + "  ");
		timeText.setStyle("-fx-text-inner-color: orange;");
		timeText.setTooltip(new Tooltip ("Earth Time"));

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
		// notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
		 notificationPane.setText("Breaking news for mars-simmers !!");
		// notificationPane.hide();
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
 
		String t = Simulation.instance().getMasterClock().getEarthClock().getTimeStamp();
		// Check if new simulation is being created or loaded from file.
		if (Simulation.isUpdating() || Simulation.instance().getMasterClock().isPaused()) {
			timeText.setText(" [ PAUSE ]  ESC to resume  " + t + "  ");		
		}
		
		else { 
/*
			MasterClock master = Simulation.instance().getMasterClock();
			if (master == null) {
				throw new IllegalStateException("master clock is null");
			}
			EarthClock earthclock = master.getEarthClock();
			if (earthclock == null) {
				throw new IllegalStateException("earthclock is null");
			}
			t = earthclock.getTimeStamp();
*/				
			timeText.setText("  " + t + "  ");
		}

	/*
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
		memUsedCache = memTotal - memFree;

		memUsed = (int)((memUsed + memUsedCache)/2D);
*/
				
				
	}

	/**
	 * Gets the main desktop panel.
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		// return mainWindow.getDesktop();
		return desktop;
	}

	public boolean isMainSceneDone() {
		return isMainSceneDoneLoading;
	}
	
	/**
	 * Load a previously saved simulation.
	 * @param type
	
	// 2015-01-25 Added autosave
	public void loadSimulation(int type) {
		//logger.info("MainScene's loadSimulation() is on " + Thread.currentThread().getName() + " Thread");
	
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
 */
	
	/**
	 * Performs the process of loading a simulation.
	 * @param reportingAuthorityType
	 
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

		showLoadingStage();
		//desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$
		desktop.clearDesktop();
		
		//Simulation.instance().loadSimulation(fileLocn);
		logger.info("");
		logger.info("Restarting " + Simulation.WINDOW_TITLE);

		Simulation.instance().loadSimulation(fileLocn);
		//imulation.instance().getSimExecutor().submit(new LoadSimulationTask(fileLocn));

		try {
			TimeUnit.MILLISECONDS.sleep(2000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while (Simulation.instance().getMasterClock() == null) {// || Simulation.instance().getMasterClock().isLoadingSimulation()) {
			System.out.println("MainScene : the master clock instance is not ready yet. Wait for another 1/2 secs");
			try {
				TimeUnit.MILLISECONDS.sleep(500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		
		try {
			SwingUtilities.invokeLater(() -> {
				desktop.resetDesktop();
			});		
			logger.info("LoadSimulationProcess() : desktop.resetDesktop() is done");
		} catch (Exception e) {
			// New simulation process should continue even if there's an
			// exception in the UI.
			logger.severe(e.getMessage());
			e.printStackTrace(System.err);
		}
		
		// load UI config
		//UIConfig.INSTANCE.parseFile();

		//desktop.disposeAnnouncementWindow();
		
		// 2016-03-22 uncheck all tool windows in the menu bar
        Collection<ToolWindow> toolWindows = desktop.getToolWindowsList();   
        Iterator<ToolWindow> i = toolWindows.iterator();
		while (i.hasNext()) {
			menuBar.uncheckToolWindow(i.next().getToolName());
		}	

  		// Note: it should save and load up the previous desktop setting instead of the Guide Tool
		// Open Guide tool after loading.
        //desktop.openToolWindow(GuideWindow.NAME);
        //GuideWindow ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
    	//int Xloc = (int)((stage.getScene().getWidth() - ourGuide.getWidth()) * .5D);
		//int Yloc = (int)((stage.getScene().getHeight() - ourGuide.getHeight()) * .5D);
		//ourGuide.setLocation(Xloc, Yloc);
        //ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$	
	
		unpauseSimulation();
	}
*/

   /*
    * Loads settlement data from a default saved sim
    */
	public class LoadSimulationTask implements Runnable {
		File fileLocn;
		
		LoadSimulationTask(File fileLocn){
			this.fileLocn = fileLocn;
		}
		
		public void run() {
			logger.info("LoadSimulationTask is on " + Thread.currentThread().getName() + " Thread");
			logger.info("Loading settlement data from the default saved simulation...");
			
			//MasterClock clock = Simulation.instance().getMasterClock();
			//clock.loadSimulation(fileLocn);
			
			Simulation.instance().loadSimulation(fileLocn); // null means loading "default.sim"
			Simulation.instance().stop();
			//Simulation.instance().getMasterClock().removeClockListener(oldListener);
			Simulation.instance().start(false);
			
			//Simulation.instance().stop();
			//Simulation.instance().start();
		}
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
		ButtonType buttonTypeOne = new ButtonType("Save & Exit");
		//ButtonType buttonTypeTwo = new ButtonType("End Sim");
		ButtonType buttonTypeCancel = new ButtonType("Back to Sim");//, ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(buttonTypeOne, 
				//buttonTypeTwo, 
				buttonTypeCancel);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == buttonTypeOne) {
			saveOnExit();
			//desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			endSim();
			exitSimulation();
			Platform.exit();
		//} else if (result.get() == buttonTypeTwo) {
		//	desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
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

		showSavingStage();
		
		// 2015-12-18 Check if it was previously on pause
		boolean previous = Simulation.instance().getMasterClock().isPaused();
		// Pause simulation.
		if (!previous) {
			pauseSave();
			//System.out.println("previous2 is false. Paused sim");
		}
		desktop.getTimeWindow().enablePauseButton(false);

		
		//2016-06-20 Added Service Worker for calling up the saving indicator

		Service<Void> service = new Service<Void>() {
	        @Override
	        protected Task<Void> createTask() {
	            return new Task<Void>() {           
	                @Override
	                protected Void call() throws Exception {
	                    //Background work               
						saveSimulationProcess(type);
						
	                    final CountDownLatch latch = new CountDownLatch(1);
	                    Platform.runLater(() -> {	                        
                 			try {
                 				//while (Simulation.instance().getMasterClock().isSavingSimulation()) {
                    				//FX Stuff done here
                    				//System.out.println("sleep");
	                        		showSavingStage();
                    				TimeUnit.MILLISECONDS.sleep(1000L);
                 				//}
                			} catch (InterruptedException e) {
                				e.printStackTrace();
                			} finally{
                				//System.out.println("finally");
                                latch.countDown();
                                hideSavingStage();
                            }
	                    });
	                    latch.await();                      
	                    //Keep with the background work

	                    return null;
	                }
	            };
	        }
	    };
	    service.start();
/*		
		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
		
				@Override
				public void run() {
					Platform.runLater(() -> {
						//showSavingStage();
						saveSimulationProcess(type);
						//hideSavingStage();
					});
				}
			};
			saveSimThread.start();
		} else {
			saveSimThread.interrupt();
		}
*/

		// 2015-12-18 Check if it was previously on pause
		boolean now = Simulation.instance().getMasterClock().isPaused();
		if (!previous) {
			if (now) {
				unpauseSave();
	    		//System.out.println("previous is false. now is true. Unpaused sim");
			}
		} else {
			if (!now) {
				unpauseSave();
	    		//System.out.println("previous is true. now is false. Unpaused sim");
			}
		}
		desktop.getTimeWindow().enablePauseButton(true);

		//desktop.disposeAnnouncementWindow();
		//hideSavingStage();
	}

	/**
	 * Performs the process of saving a simulation.
	 */
	// 2015-01-08 Added autosave
	private void saveSimulationProcess(int type) {
		//logger.info("MainScene's saveSimulationProcess() is on " + Thread.currentThread().getName() + " Thread");

		//showSavingStage();
		
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

		//showSavingStage();
		
		MasterClock clock = Simulation.instance().getMasterClock();

		if (type == AUTOSAVE) {
			//desktop.disposeAnnouncementWindow();			
			//desktop.openAnnouncementWindow(Msg.getString("MainScene.autosavingSim")); //$NON-NLS-1$
			clock.autosaveSimulation();
		} else if (type == SAVE_AS || type == DEFAULT) {
			//desktop.disposeAnnouncementWindow();
			//desktop.openAnnouncementWindow(Msg.getString("MainScene.savingSim")); //$NON-NLS-1$
			clock.saveSimulation(fileLocn);
		}

/*
  		// Note: the following Thread.sleep() causes system to hang in MacOSX, but not in Windows
		while (clock.isSavingSimulation() || clock.isAutosavingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.sleepInterrupt"), e); //$NON-NLS-1$
			}
		}

*/

		//desktop.disposeAnnouncementWindow();
		
		//hideSavingStage();
	}

	
	public void startPausePopup() {
		//System.out.println("calling startPausePopup()");   
		//messagePopup.popAMessage("PAUSED", " ESC to resume", null, stage, Pos.CENTER, PNotification.PAUSE_ICON);  		    	
		if (!messagePopup.isOn()) {
			Platform.runLater(() -> 
				messagePopup.popAMessage(" PAUSED", " ", " ", stage, Pos.CENTER, PNotification.PAUSE_ICON)
			);  		    	
			//System.out.println("popping up pause"); 
		}
	}

	public void stopPausePopup() {
		Platform.runLater(() -> 
			messagePopup.stop()
		);
		    	
	}
	
	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {	
		//System.out.println("pauseSimulation() ");    	
		//desktop.openAnnouncementWindow(Msg.getString("MainScene.pausingSim")); //$NON-NLS-1$
		//autosaveTimeline.pause();
		desktop.getMarqueeTicker().pauseMarqueeTimer(true);
		Simulation.instance().getMasterClock().setPaused(true);
		//startPausePopup();
		//timeText.setText(" [Paused] " + timeStamp + "  ");
		//desktop.getTimeWindow().enablePauseButton(false);
	}

	//public void pauseTimeText() {
	//	Platform.runLater(() -> timeText.setText(" [Paused] " + timeStamp + "  "));
	//}
	
	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {	
		//System.out.println("unpauseSimulation() "); 
		stopPausePopup();
		//desktop.disposeAnnouncementWindow();
		desktop.getMarqueeTicker().pauseMarqueeTimer(false);
		Simulation.instance().getMasterClock().setPaused(false);
		//autosaveTimeline.play();	
		//desktop.getTimeWindow().enablePauseButton(true);
	}


	/**
	 * Pauses the simulation.
	 */
	public void pauseSave() {
		//showSavingStage();
		desktop.getMarqueeTicker().pauseMarqueeTimer(true);
		Simulation.instance().getMasterClock().setPaused(true);
	}

	/**
	 * Unpauses the simulation.
	 */
	public void unpauseSave() {
		Simulation.instance().getMasterClock().setPaused(false);
		desktop.getMarqueeTicker().pauseMarqueeTimer(false);	
		//hideSavingStage();
	}

	/**
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves the main menu running
	 */
	private void endSim() {
		//logger.info("MainScene's endSim() is on " + Thread.currentThread().getName() + " Thread");
		Simulation.instance().endSimulation();
		Simulation.instance().getSimExecutor().shutdownNow();

		// Simulation.instance().destroyOldSimulation();
		getDesktop().clearDesktop();
		// getDesktop().resetDesktop();
		// Simulation.instance().getMasterClock().exitProgram();

		statusBar = null;
		stage.close();
		// Simulation.instance().endMasterClock();
		//Simulation.instance().startSimExecutor();
	}

	/**
	 * Exits the current simulation and the main menu.
	 */
	public void exitSimulation() {
		//logger.info("MainScene's exitSimulation() is on " + Thread.currentThread().getName() + " Thread");
		//desktop.openAnnouncementWindow(Msg.getString("MainScene.exitSim"));

		logger.info("Exiting simulation");

		//Simulation sim = Simulation.instance();
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

		Simulation.instance().getMasterClock().exitProgram();
		//Platform.exit();
	}

	/**
	 * Sets the look and feel of the UI
	 *
	 * @param nativeLookAndFeel
	 *            true if native look and feel should be used.
	 */
	// 2015-05-02 Edited setLookAndFeel()
	public void setLookAndFeel(int choice) {
		//logger.info("MainScene's setLookAndFeel() is on " + Thread.currentThread().getName() + " Thread");
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

	//public MenuBar getMainSceneMenu() {
	//	return menuBar;
	//}
	
	public Stage getStage() {
		return stage;
	}

	private void createSwingNode() {
		//setLookAndFeel(1);
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
		
		// desktop.openInitialWindows();
	}

	public SwingNode getSwingNode() {
		return swingNode;
	}

/*	
	public void openSwingTab() {
		// splitPane.setDividerPositions(1.0f);
		dndTabPane.getSelectionModel().select(swingTab);
		//rootStackPane.getStylesheets().add("/fxui/css/mainskin.css");
	}

	public void openMarsNet() {
		// splitPane.setDividerPositions(0.8f);
		dndTabPane.getSelectionModel().select(nodeTab);
	}
*/
	
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
			//desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
			saveOnExit();
			endSim();
			exitSimulation();
			Platform.exit();
			System.exit(0);
			return true;
		//} else if (result.get() == buttonTypeTwo) {
		//	desktop.openAnnouncementWindow(Msg.getString("MainScene.endSim"));
		//	endSim();			
		//	return true;
		} else if (result.get() == buttonTypeThree) {
			endSim();
			exitSimulation();
			Platform.exit();
			System.exit(0);
			return true;
		} else { //if (result.get() == buttonTypeCancel) {
			return false;
		}
	}

	/**
	 * Initiates the process of saving a simulation.
	 */
	public void saveOnExit() {
		//logger.info("MainScene's saveOnExit() is on " + Thread.currentThread().getName() + " Thread");
		//desktop.disposeAnnouncementWindow();
		//desktop.openAnnouncementWindow(Msg.getString("MainScene.defaultSaveSim"));
		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);

		// Save the simulation.
		//Simulation sim = Simulation.instance();
		MasterClock clock = Simulation.instance().getMasterClock();
		try {
			clock.saveSimulation(null);
						
			while (clock.isSavingSimulation())
				TimeUnit.MILLISECONDS.sleep(500L);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
			e.printStackTrace(System.err);
		}
	}

	public void openInitialWindows() {
		//logger.info("MainScene's openInitialWindows() is on " + Thread.currentThread().getName() + " Thread");
		//String OS = System.getProperty("os.name").toLowerCase();
		//System.out.println("OS is " + OS);
		if (OS.equals("mac os x")) {
		// SwingUtilities needed below for MacOSX
			SwingUtilities.invokeLater(() -> {
				desktop.openInitialWindows();
			});
		}
		else {
/*
			GuideWindow ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
			System.out.println("ourGuide.getWidth() is " + ourGuide.getWidth());
			System.out.println("ourGuide.getHeight() is " + ourGuide.getHeight());
			System.out.println("swingPane.getWidth() is " + swingPane.getWidth());
			System.out.println("swingPane.getHeight() is " + swingPane.getHeight());
			System.out.println("stage.getScene().getWidth() is " + stage.getScene().getWidth());
			System.out.println("stage.getScene().getHeight() is " + stage.getScene().getHeight());

			int Xloc = (int)((stage.getScene().getWidth() - ourGuide.getWidth()) * .5D);
			int Yloc = (int)((stage.getScene().getHeight() - ourGuide.getHeight()) * .5D);
			//System.out.println("Xloc is " + Xloc + "  Yloc is " + Yloc);
			ourGuide.setLocation(Xloc, Yloc);
			ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$
*/
			desktop.openInitialWindows();
		}

		//2015-09-27 for testing the use of fxml
		//marsNode.createMaterialDesignWindow();
		//marsNode.createSettlementWindow();
		
		// 2016-02-25 Disabled marsNode
		//marsNode.createStory();
		//marsNode.createDragDrop();
		
		//marsNode.createEarthMap();
		//marsNode.createMarsMap();
		//marsNode.createChatBox();

		messagePopup = new MessagePopup();
		quote = new QuotationPopup(this);

		popAQuote();	

/*		
		// 2016-06-15 Added top edge mouse cursor detection for sliding down the menu bar
		anchorPane.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
		
			boolean onMenuBar = (e.getSceneX() <= EDGE_DETECTION_PIXELS_X) && (e.getSceneY() <= EDGE_DETECTION_PIXELS_Y);
				
			if (!menubarButton.isSelected()) {
				
				if (onMenuBar && !onMenuBarCache && !menuBarVisible) {// || menubarButton.isSelected()) 
					//System.out.println("slide open");
					topFlapBar.slide();
					//menuBarVisible = true;
					
					// add a timer countdown
					
					//FxTimer.runLater(
		    		//		java.time.Duration.ofMillis(2000),
		    		 //       () -> menuBarVisible = true);
					
					
					
				} else if (!onMenuBar && onMenuBarCache && menuBarVisible) {// || menubarButton.isSelected()) 
					//System.out.println("slide close");
					topFlapBar.slide();
					//menuBarVisible = false;
				}
					
				// 2016-06-17 Added self-correcting if-then clause to correct problems arising from hovering over the menu bar multiple times successively
				//if (onMenuBar) {
				//	menuBarVisible = true;
				//}
				//else {
				//	menuBarVisible = false;
				//}

			}
			

			
			onMenuBarCache = onMenuBar;
		
        });	
*/

		
		//hideLoadingStage();
		
		createProgressCircle("Saving");
		//createProgressCircle("Paused");
	
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

	//public Scene getScene() {
	//	return scene;
	//}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	//public StackPane getRootStackPane() {
	//	return rootStackPane;
	//}

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
 	public void createProgressCircle(String title) {

 		StackPane stackPane = new StackPane();
 		//BorderPane controlsPane = new BorderPane();
 		//controlsPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
 		//stackPane.getChildren().add(controlsPane);
 		//controlsPane.setCenter(new TableView<Void>());
 		MaskerPane indicator = new MaskerPane();
 		indicator.setText(title);
 		indicator.setScaleX(1.5);
 		indicator.setScaleY(1.5);
 		//indicator.setMaxSize(200, 200);
 /*
 		ProgressIndicator indicator = new ProgressIndicator();
 		indicator.setMaxSize(120, 120);
 		//indicator.setProgress(50);
 		ColorAdjust adjust = new javafx.scene.effect.ColorAdjust();
 		adjust.setHue(-0.07); // -.07, -0.1 cyan; 3, 17 = red orange; -0.4 = bright green
 		indicator.setEffect(adjust);
 */
 		stackPane.getChildren().add(indicator);
 		StackPane.setAlignment(indicator, Pos.CENTER);
 		//StackPane.setMargin(indicator, new Insets(5));
 		stackPane.setBackground(Background.EMPTY);
 		//stackPane.setScaleX(1.5);
 		//stackPane.setScaleY(1.5);

 		stackPane.setStyle(
     		   //"-fx-border-style: none; "
     		   //"-fx-background-color: #231d12; "
        		"-fx-background-color: transparent; "
        		//+ "-fx-background-radius: 1px;"
        		);     
		
		
 		Scene scene = new Scene(stackPane, 150, 150);
 		scene.setFill(Color.TRANSPARENT);

 		if (title.contains("Saving")) {// || title.contains("Autosaving")) {
 			savingCircleStage = new Stage();
 			savingCircleStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
	 
 			savingCircleStage.initStyle (StageStyle.TRANSPARENT);
 			savingCircleStage.setScene(scene);
 			savingCircleStage.hide();
	 	}
 
 	}


 	public void showSavingStage() {
		Platform.runLater(() -> {
			setMonitor(savingCircleStage);
			savingCircleStage.show();
			//System.out.println("showSavingStage()");//savingCircleStage.requestFocus();		
		});		
 	}
 	
 	public void hideSavingStage() {
 		Platform.runLater(() -> {
 			savingCircleStage.hide();	
			//System.out.println("hideSavingStage()");
 		});
 	}

 	// 2016-06-27 Added setMonitor()
	public void setMonitor(Stage stage) {
		// Issue: how do we tweak mars-sim to run on the "active" monitor as chosen by user ?
		// "active monitor is defined by whichever computer screen the mouse pointer is or where the command console that starts mars-sim.
		// by default MSP runs on the primary monitor (aka monitor 0 as reported by windows os) only.
		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762 

		StartUpLocation startUpLoc = new StartUpLocation(anchorPane.getPrefWidth(), anchorPane.getPrefHeight());
        double xPos = startUpLoc.getXPos();
        double yPos = startUpLoc.getYPos();
        // Set Only if X and Y are not zero and were computed correctly
     	//ObservableList<Screen> screens = Screen.getScreensForRectangle(xPos, yPos, 1, 1); 
     	//ObservableList<Screen> screens = Screen.getScreens();	
    	//System.out.println("# of monitors : " + screens.size());

        if (xPos != 0 && yPos != 0) {
            stage.setX(xPos);
            stage.setY(yPos);
            stage.centerOnScreen();
            System.out.println("Monitor 2:    x : " + xPos + "   y : " + yPos);
        } else {
            stage.centerOnScreen();
            //System.out.println("calling centerOnScreen()");
            System.out.println("Monitor 1:    x : " + xPos + "   y : " + yPos);
        }  
		
	}
	
/*	
 	public void createMenuBar() {
		menuBar = new MenuBar();
		
		menuBar.useSystemMenuBarProperty().set(true);
		
		Platform.runLater(() -> {

			MenuToolkit tk = MenuToolkit.toolkit(Locale.getDefault());
			tk.setApplicationMenu(tk.createDefaultApplicationMenu("mars-sim"));

			// Create the default Application menu
			//Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("mars-sim");

			// Update the existing Application menu
			//tk.setApplicationMenu(defaultApplicationMenu);

			//defaultApplicationMenu.setText("mars-sim");
	
			//MenuBar menuBar = (MenuBar)this
			//menuBar.useSystemMenuBarProperty().set(true);

			Menu java = new Menu("Jave");
			Menu file = new Menu("File");
			Menu help = new Menu("Help");
			
			MenuItem newI = new MenuItem("New");
			MenuItem quit = tk.createQuitMenuItem("mars-sim");

			java.getItems().addAll(quit);

			//menuBar.getMenus().addAll(new Menu(), java, file, help);
			menuBar.getMenus().addAll(java, file, help);					
				
			tk.setMenuBar(stage, menuBar);
			
		});
 	}
 */	
 	
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
		//processCpuLoadText = null;
		memBtn = null;
		clkBtn = null;
		//cpuBtn = null;	

		statusBar = null;
		flyout = null;
		marsNetButton = null;
		chatBox = null;
		//cornerMenu = null;
		swingPane = null;
		anchorPane = null;
		borderPane = null;
		//fxDesktopPane = null;
		//autosaveTimeline = null;

		newSimThread = null;
		loadSimThread = null;
		saveSimThread = null;

		stage = null;
		loadingCircleStage = null;

		swingTab = null;
		nodeTab = null;
		dndTabPane = null;
		timeline = null;
		//notify_timeline = null;
		notificationPane = null;

		desktop.destroy();
		desktop = null;
		menuBar = null;
		marsNode = null;
		transportWizard = null;
		constructionWizard = null;

	}

}
