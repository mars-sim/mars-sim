/**
 * Mars Simulation Project
 * MainMenu.java
 * @version 3.08 2015-04-09
 * @author Manny Kung
 */

package org.mars_sim.msp.javafx;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.bridge.SLF4JBridgeHandler;
import java.util.logging.Logger;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.CacheHint;
import javafx.scene.layout.Region;
import javafx.scene.layout.Background;
import javafx.animation.FadeTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.shape.Rectangle;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.AmbientLight;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
import javafx.stage.Screen;

import org.controlsfx.control.MaskerPane;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.javafx.configEditor.ScenarioConfigEditorFX.SimulationTask;
import org.mars_sim.msp.javafx.controller.MainMenuController;
import org.mars_sim.msp.networking.MultiplayerMode;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.tool.StartUpLocation;


/*
 * The MainMenu class creates the Main Menu and the spinning Mars Globe for MSP
 */
public class MainMenu {

	// ------------------------------ FIELDS ------------------------------

	/** default logger. */
	private static Logger logger = Logger.getLogger(MainMenu.class.getName());

	/** Logger for the class */
	//private static final Logger logger;


    // -------------------------- STATIC METHODS --------------------------
    //static {
    //    SLF4JBridgeHandler.removeHandlersForRootLogger();
   //     SLF4JBridgeHandler.install();
    //    logger = LoggerFactory.getLogger(MainMenu.class);
    //}

    private static final int WIDTH = 1024;//768-20;
    private static final int HEIGHT = 768-20;


	// Data members
    private double fileSize;

    public static String screen1ID = "main";
    public static String screen1File = "/fxui/fxml/MainMenu.fxml";
    public static String screen2ID = "configuration";
    public static String screen2File = "/fxui/fxml/Configuration.fxml";
    public static String screen3ID = "credits";
    public static String screen3File = "/fxui/fxml/Credits.fxml";

    private double anchorX;
    private double rate;

    private boolean isDone;

    //private boolean cleanUI = true;

    //private Group root;
    private StackPane root;
	private Stage stage, mainSceneStage, circleStage;
	public Scene mainMenuScene, mainSceneScene;

	public MainMenu mainMenu;
	public MainScene mainScene;
	public ScreensSwitcher screen;

	private MarsProjectFX marsProjectFX;
	private transient ThreadPoolExecutor executor;
	private MultiplayerMode multiplayerMode;
	public MainMenuController mainMenuController;

	public SpinningGlobe spinningGlobe;

    public MainMenu(MarsProjectFX marsProjectFX) {
    	this.marsProjectFX = marsProjectFX;
    	mainMenu = this;
    	//logger.info("MainMenu's constructor is on " + Thread.currentThread().getName() + " Thread");
	}

    /*
     * Sets up and shows the MainMenu and prepare the stage for MainScene
     */
	@SuppressWarnings("restriction")
	void initAndShowGUI(Stage stage) {
	   //logger.info("MainMenu's initAndShowGUI() is on " + Thread.currentThread().getName() + " Thread");

		this.stage = stage;

    	Platform.setImplicitExit(false);

    	stage.setOnCloseRequest(e -> {
			boolean isExit = screen.exitDialog(stage);
			if (!isExit) {
				e.consume();
			}
			else {
				Platform.exit();
	    		System.exit(0);
			}
		});

       screen = new ScreensSwitcher(this);
       screen.loadScreen(MainMenu.screen1ID, MainMenu.screen1File);
       screen.loadScreen(MainMenu.screen2ID, MainMenu.screen2File);
       screen.loadScreen(MainMenu.screen3ID, MainMenu.screen3File);
       screen.setScreen(MainMenu.screen1ID);

       if ( screen.lookup("#menuOptionBox") == null)
			System.out.println("Warning: menu option box is not found");

       VBox menuOptionBox = ((VBox) screen.lookup("#menuOptionBox"));
/*
       if (mouseEvent.getSource().equals(menuOptionBox)){
    	   System.out.println("hovering over menu option box");
    	   mainMenuScene.setCursor(Cursor.HAND);
       }
*/
       Rectangle rect = new Rectangle(WIDTH, HEIGHT);//, Color.rgb(179,53,0));//rgb(69, 56, 35));//rgb(86,70,44));//SADDLEBROWN);
       rect.setArcWidth(40);
       rect.setArcHeight(40);
       rect.setEffect(new DropShadow(40,5,5, Color.BLACK));//TAN)); // rgb(27,8,0)));// for bottom right edge

       // 2015-11-23 Added StarfieldFX
       StarfieldFX sf = new StarfieldFX();
       Parent starfield = sf.createStars(WIDTH-20, HEIGHT-20);

       root = new StackPane();//starfield);

       root.setPrefHeight(WIDTH);
       root.setPrefWidth(HEIGHT);
       root.setStyle(
    		   //"-fx-border-style: none; "
    		   //"-fx-background-color: #231d12; "
       			"-fx-background-color: transparent; "
       			+ "-fx-background-radius: 1px;"
    		   );

       spinningGlobe = new SpinningGlobe(this);
       Parent globe = spinningGlobe.createMarsGlobe();

       screen.setCache(true);
       starfield.setCache(true);
       screen.setCacheHint(CacheHint.SCALE_AND_ROTATE);
       starfield.setCacheHint(CacheHint.SCALE_AND_ROTATE);

       root.getChildren().addAll(rect, starfield, globe, screen);

       mainMenuScene = new Scene(root, WIDTH+20, HEIGHT+20);//, true, SceneAntialiasing.BALANCED); // Color.DARKGOLDENROD, Color.TAN);//MAROON); //TRANSPARENT);//DARKGOLDENROD);
       mainMenuScene.setFill(Color.DARKGOLDENROD);//Color.BLACK);
       mainMenuScene.getStylesheets().add(this.getClass().getResource("/fxui/css/mainmenu.css").toExternalForm() );
       //mainMenuScene.setFill(Color.BLACK); // if using Group, a black border will remain
       //mainMenuScene.setFill(Color.TRANSPARENT); // if using Group, a white border will remain
       mainMenuScene.setCursor(Cursor.HAND);

       //mainMenuScene.setCamera(spinningGlobe.getMarsGlobe().getCamera(root));
       spinningGlobe.getMarsGlobe().handleMouse(mainMenuScene);
       spinningGlobe.getMarsGlobe().handleKeyboard(mainMenuScene);

       // Makes the menu option box fades in
       mainMenuScene.setOnMouseEntered(new EventHandler<MouseEvent>(){
           public void handle(MouseEvent mouseEvent){
               FadeTransition fadeTransition
                       = new FadeTransition(Duration.millis(1000), menuOptionBox);
               fadeTransition.setFromValue(0.0);
               fadeTransition.setToValue(1.0);
               fadeTransition.play();
           }
       });

       // Makes the menu option box fades out
       mainMenuScene.setOnMouseExited(new EventHandler<MouseEvent>(){
           public void handle(MouseEvent mouseEvent){
               FadeTransition fadeTransition
                       = new FadeTransition(Duration.millis(1000), menuOptionBox);
               fadeTransition.setFromValue(1.0);
               fadeTransition.setToValue(0.0);
               fadeTransition.play();
           }
       });

       // Enable dragging on the undecorated stage
       //EffectUtilities.makeDraggable(mainMenuStage, screen);

       //mstage.initStyle(StageStyle.UTILITY);
       //stage.initStyle(StageStyle.TRANSPARENT);
       //stage.initStyle(StageStyle.UNDECORATED);
       stage.centerOnScreen();
       stage.setResizable(false);
	   stage.setTitle(Simulation.WINDOW_TITLE);
       stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
       //NOTE: OR use svg file with stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       stage.setScene(mainMenuScene);
       
       //2016-02-07 Added calling setMonitor()
       mainMenu.setMonitor(stage);
	   
       stage.show();

       createProgressCircle();
       circleStage.hide();
   }

/*
	public void switchScene(Scene scene) {
		// Starts a new stage for MainScene
		//mainSceneStage = new Stage();
		stage.setIconified(true);
		stage.hide();
		stage.setScene(scene);
	}

	public void switchStage(Scene scene) {
		stage.setScene(scene);
		stage.show();
	}
*/
	public Stage getStage() {
		return stage;
	}

   public MainScene getMainScene() {
	   return mainScene;
   }

   public MultiplayerMode getMultiplayerMode() {
	   return multiplayerMode;
	}

   public void runOne() {
	   //logger.info("MainMenu's runOne() is on " + Thread.currentThread().getName() + " Thread");
	   stage.setIconified(true);
	   stage.hide();

	   // creates a mainScene instance
	   mainScene = new MainScene(stage);
	   // goes to scenario config editor
	   marsProjectFX.handleNewSimulation();
   }

   public void runTwo() {
		Future future = Simulation.instance().getSimExecutor().submit(new LoadSimulationTask());
		//System.out.println("desktop is " + mainMenu.getMainScene().getDesktop());

	   //logger.info("MainMenu's runTwo() is on " + Thread.currentThread().getName() + " Thread");
		Platform.runLater(() -> {
		//	mainMenuScene.setCursor(Cursor.WAIT);
			// TODO: the ring won't turn after a couple of seconds

			//2016-02-07 Added calling setMonitor()
			setMonitor(circleStage);
		       
			circleStage.show();
			circleStage.requestFocus();
		});

		stage.setIconified(true);//hide();
		mainScene = new MainScene(mainSceneStage);

		// 2015-10-13 Set up a Task Thread
		Task task = new Task<Void>() {
            @Override
            protected Void call() {
				try {

					   TimeUnit.MILLISECONDS.sleep(2000L);
					   // Note 1: The delay time for launching the JavaFX UI should also be based on the file size of the default.sim
					   long delay_time = (long) (fileSize * 4000L);
					   TimeUnit.MILLISECONDS.sleep(delay_time);

					   //System.out.println("desktop is " + mainMenu.getMainScene().getDesktop());
					   //System.out.println("isMainSceneDone is " + mainScene.isMainSceneDone());

					   //while (!future.isDone() && !mainScene.isMainSceneDone()) {
					   while ( !future.isDone() && mainMenu.getMainScene().getDesktop() == null) {
			        		long delay = (long) (fileSize * 1000L);
							//System.out.println("Wait for " + delay/1000D + " secs inside the while loop");
							TimeUnit.MILLISECONDS.sleep(delay);
			        		//System.out.println("desktop is " + mainMenu.getMainScene().getDesktop());
			        		//System.out.println("desktop is " + mainScene.isMainSceneDone());
			     		   // Note: java8u60 requires a longer time (than 8u45) such as 4 secs instead of 2 secs on some machines.
			 			   // or else it will proceed to prepareStage() below prematurely and results in NullPointerException
			 			   // as it tries to read people data before it was properly populated in UnitManager.
			 			   //service.shutdownNow();
			 			   //try {
			 			   //   service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			 			   //} catch (InterruptedException e) {}
					   }
					   //System.out.println("future.get() is " + future.get());
					   //System.out.println("future.isDone() is " + future.isDone());

					   TimeUnit.MILLISECONDS.sleep(2000L);
					   Platform.runLater(() -> {
						   prepareStage();
					   });

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
            }
        };

        //ProgressIndicator ind = createProgressCircle();
        //ind.progressProperty().bind(task.progressProperty());

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

        mainMenuScene.setCursor(Cursor.DEFAULT); //Change cursor to default style

   }

   /*
    * Loads settlement data from a default saved sim
    */
	public class LoadSimulationTask implements Runnable {
		public void run() {
			logger.info("MainMenu'sLoadSimulationTask is on " + Thread.currentThread().getName() + " Thread");
			fileSize = 1;
			logger.info("Loading settlement data from the default saved simulation...");
			Simulation.instance().loadSimulation(null); // null means loading "default.sim"
			//System.out.println("done with loadSimulation(null)");
			logger.info("Restarting " + Simulation.WINDOW_TITLE);
			Simulation.instance().start();
			Simulation.instance().stop();
			Simulation.instance().start();
			Platform.runLater(() -> {
				prepareScene();
				//System.out.println("done with prepareScene()");
			});
		}
	}

	/*
	 * Prepares the scene in the main scene
	 */
	public void prepareScene() {
	   //logger.info("MainMenu's prepareStage() is on " + Thread.currentThread().getName() + " Thread");
	   // prepare main scene
	   mainScene.prepareMainScene();
	   // creates and initialize scene
	   mainSceneScene = mainScene.initializeScene();
	   // switch from the main menu's scene to the main scene's scene
	   stage.setScene(mainSceneScene);

	}

	/*
	 * Sets up the stage for the main scene
	 */
	public void prepareStage() {

	   mainScene.prepareOthers();

	   //mainScene.getMarsNode().createSettlementWindow();
	   //mainScene.getMarsNode().createJMEWindow(stage);

	   mainScene.initializeTheme();

	   while (!mainScene.isMainSceneDone())
	   {
		   try {
			TimeUnit.MILLISECONDS.sleep(200L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	   }

	   mainScene.openInitialWindows();

	   stage.setIconified(false);
	   
       //2016-02-07 Added calling setMonitor()
       mainMenu.setMonitor(stage);
       
	   stage.show();

	   Platform.runLater(() -> {
		   circleStage.close();
		});

	   //stage.requestFocus();

	   //logger.info("done with stage.show() in MainMenu's prepareStage()");
	}

   public void runThree() {
	   //logger.info("MainMenu's runThree() is on " + Thread.currentThread().getName() + " Thread");
	   Simulation.instance().getSimExecutor().submit(new MultiplayerTask());
	   stage.setIconified(true);//hide();
/*
	   try {
			multiplayerMode = new MultiplayerMode(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
		executor.execute(multiplayerMode.getModeTask());
*/
	    //pool.shutdown();
	     //mainSceneStage.toFront();
   		//modtoolScene = new SettlementScene().createSettlementScene();
	    //stage.setScene(modtoolScene);
	    //stage.show();
   }

	public class MultiplayerTask implements Runnable {
		public void run() {
			try {
				multiplayerMode = new MultiplayerMode(mainMenu);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
			executor.execute(multiplayerMode.getModeTask());
		}
	}

   //public void changeScene(int toscene) {
	   //switch (toscene) {
		   	//case 1:
		   		//scene = new MenuScene().createScene();
		   	//	break;
	   		//case 2:
	   			//Scene scene = mainScene.createMainScene();
	   			//break;
	   		//case 3:
	   			//scene = modtoolScene.createScene(stage);
	   		//	break;
	   		//stage.setScene(scene);
	   //}
   //}

   /*
    * Sets the main menu controller at the start of the sim.
    */
   // 2015-09-27 Added setController()
   public void setController(ControlledScreen myScreenController) {
	   if (mainMenuController == null)
		   if (myScreenController instanceof MainMenuController )
			   mainMenuController = (MainMenuController) myScreenController;
   }

   /*
    * Create the progress circle animation while waiting for loading the main scene
    */
	public void createProgressCircle() {

		StackPane stackPane = new StackPane();

		//BorderPane controlsPane = new BorderPane();
		//controlsPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		//stackPane.getChildren().add(controlsPane);
		//controlsPane.setCenter(new TableView<Void>());

		MaskerPane indicator = new MaskerPane();
		indicator.setScaleX(1.2);
		indicator.setScaleY(1.2);

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
		//stackPane.setScaleX(1.2);
		//stackPane.setScaleY(1.2);

		circleStage = new Stage();
		Scene scene = new Scene(stackPane);//, 200, 200);
		scene.setFill(Color.TRANSPARENT);
		circleStage.initStyle (StageStyle.TRANSPARENT);
		circleStage.setScene(scene);
		
		//2016-02-07 Added calling setMonitor()
		setMonitor(circleStage);
        circleStage.show();

        //return indicator;
	}

	public void chooseScreen(int num) {
		
		ObservableList<Screen> screens = Screen.getScreens();//.getScreensForRectangle(xPos, yPos, 1, 1); 
    	Screen currentScreen = screens.get(num);
		Rectangle2D rect = currentScreen.getVisualBounds();
		
		Screen primaryScreen = Screen.getPrimary();
	}
	
	public void setMonitor(Stage stage) {
		// Issue: can't run the MSP on the "active" monitor.
		// by default MSP runs on the primary monitor (aka monitor 1 as reported by windows os) only.
		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762 

		StartUpLocation startUpLoc = new StartUpLocation(root.getPrefWidth(), root.getPrefHeight());
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
            //System.out.println(" x : " + xPos + "   y : " + yPos);
        } else {
            stage.centerOnScreen();
            //System.out.println("calling centerOnScreen()");
            //System.out.println(" x : " + xPos + "   y : " + yPos);
        }
        
		
	}
	
	public Stage getCircleStage() {
		return circleStage;
	}

	public ScreensSwitcher getScreensSwitcher() {
		return screen;
	}

	public MainMenuController getMainMenuController() {
		return mainMenuController;
	}

	public SpinningGlobe getSpinningGlobe() {
		return spinningGlobe;
	}

	public void destroy() {

		root = null;
		screen = null;
		mainSceneStage = null;
		stage = null;
		circleStage = null;
		mainMenuScene = null;
		mainMenu = null;
		mainScene = null;
		marsProjectFX = null;
		executor = null;
		multiplayerMode = null;
		mainMenuController = null;
	}

}
