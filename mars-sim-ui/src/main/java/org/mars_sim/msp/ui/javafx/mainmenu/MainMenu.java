/**
 * Mars Simulation Project
 * MainMenu.java
 * @version 3.08 2015-04-09
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.bridge.SLF4JBridgeHandler;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import javafx.concurrent.Service;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;

import org.controlsfx.control.MaskerPane;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.javafx.config.ScenarioConfigEditorFX;
import org.mars_sim.msp.ui.javafx.config.ScenarioConfigEditorFX.SimulationTask;
import org.mars_sim.msp.ui.javafx.config.StarfieldFX;
import org.mars_sim.msp.ui.javafx.config.controller.MainMenuController;
import org.mars_sim.msp.ui.javafx.networking.MultiplayerMode;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.tool.StartUpLocation;


/*
 * The MainMenu class creates the Main Menu and the spinning Mars Globe for MSP
 */
public class MainMenu {

	// ------------------------------ FIELDS ------------------------------

	/** default logger. */
	private static Logger logger = Logger.getLogger(MainMenu.class.getName());

	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
	
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

    private StackPane root;
	private Stage stage, mainSceneStage, circleStage, loadingCircleStage;//, waitStage;
	public Scene mainMenuScene, mainSceneScene;

	public MainMenu mainMenu;
	public MainScene mainScene;
	public ScreensSwitcher screen;

	//private MarsProjectFX marsProjectFX;
	private transient ThreadPoolExecutor executor;
	private MultiplayerMode multiplayerMode;
	public MainMenuController mainMenuController;

	public SpinningGlobe spinningGlobe;
	
    public MainMenu() {//MarsProjectFX marsProjectFX) {
       	//logger.info("MainMenu's constructor is on " + Thread.currentThread().getName());
    	//this.marsProjectFX = marsProjectFX;
    	mainMenu = this;
 	}

    /*
     * Sets up and shows the MainMenu and prepare the stage for MainScene
     */
	@SuppressWarnings("restriction")
	public
	void initMainMenu(Stage stage) {
	   //logger.info("MainMenu's initAndShowGUI() is on " + Thread.currentThread().getName());
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
       			"-fx-background-color: transparent; "
       			+ "-fx-background-radius: 1px;"
    		   );

       spinningGlobe = new SpinningGlobe(this);
       Parent globe = spinningGlobe.createGlobe();

       screen.setCache(true);
       starfield.setCache(true);
       screen.setCacheHint(CacheHint.SCALE_AND_ROTATE);
       starfield.setCacheHint(CacheHint.SCALE_AND_ROTATE);

       root.getChildren().addAll(rect, starfield, globe, screen);

       mainMenuScene = new Scene(root, WIDTH+20, HEIGHT+20, true, SceneAntialiasing.BALANCED); // Color.DARKGOLDENROD, Color.TAN);//MAROON); //TRANSPARENT);//DARKGOLDENROD);
       mainMenuScene.setFill(Color.DARKGOLDENROD);//Color.BLACK);
       mainMenuScene.getStylesheets().add(this.getClass().getResource("/fxui/css/mainmenu.css").toExternalForm() );
       //mainMenuScene.setFill(Color.BLACK); // if using Group, a black border will remain
       //mainMenuScene.setFill(Color.TRANSPARENT); // if using Group, a white border will remain
       mainMenuScene.setCursor(Cursor.HAND);

       spinningGlobe.getGlobe().handleMouse(mainMenuScene);
       spinningGlobe.getGlobe().handleKeyboard(mainMenuScene);

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

       stage.centerOnScreen();
       stage.setResizable(false);
	   stage.setTitle(Simulation.title);
       stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
       //NOTE: OR use svg file with stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       stage.setScene(mainMenuScene);     
       //2016-02-07 Added calling setMonitor()
       setMonitor(stage);   
       stage.show();

   }

	
	public void setupMainSceneStage() {
	       mainSceneStage = new Stage();
	       mainSceneStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));

	}

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
	   //logger.info("MainMenu's runOne() is on " + Thread.currentThread().getName());
	   stage.setIconified(true);
	   stage.hide(); 
	   // creates a mainScene instance
	   mainScene = new MainScene(mainSceneStage);
	   // goes to scenario config editor
	   
	   //marsProjectFX.handleNewSimulation();
		logger.info("Creating a new sim in " + OS);	
        try {
            //SimulationConfig.loadConfig(); // located to prepare()
           	Simulation.instance().getSimExecutor().execute(new ConfigEditorTask());
           	
        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Error : could not create a new simulation ", e);
        }
   }

	public class ConfigEditorTask implements Runnable { 	
		  public void run() {
			  //logger.info("MarsProjectFX's ConfigEditorTask's run() is on " + Thread.currentThread().getName() );
			  new ScenarioConfigEditorFX(mainMenu); //marsProjectFX, 
		  }
	}
	
   public void runTwo() {
	   //logger.info("MainMenu's runTwo() is on " + Thread.currentThread().getName());
	   mainMenuScene.setCursor(Cursor.WAIT);
		
	   stage.setIconified(true);
	   stage.hide();
		
	   loadSim(null);
	   
       mainMenuScene.setCursor(Cursor.DEFAULT); //Change cursor to default style
	   
   }


   /*
    * Loads the simulation file via the terminal or FileChooser.
    *  
    * @param selectedFile the saved sim
    */
   public void loadSim(File selectedFile) {
	   //logger.info("MainMenu's loadSim() is on " + Thread.currentThread().getName());
	   Platform.runLater(() -> mainScene = new MainScene(mainSceneStage));
	
	   String dir = Simulation.DEFAULT_DIR;
	   String title = null;

	   try {
		   
		   if (selectedFile == null) {
			   
			   FileChooser chooser = new FileChooser();
				// chooser.setInitialFileName(dir);
				// Set to user directory or go to default if cannot access
				// String userDirectoryString = System.getProperty("user.home");
			   File userDirectory = new File(dir);
			   chooser.setInitialDirectory(userDirectory);
			   chooser.setTitle(title); // $NON-NLS-1$
	
				// Set extension filter
			   FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter(
						"Simulation files (*.sim)", "*.sim");
			   FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter(
						"all files (*.*)", "*.*");
	
			   chooser.getExtensionFilters().addAll(simFilter, allFilter);
	
			   // Show open file dialog
			   selectedFile = chooser.showOpenDialog(stage); 
		   }
		   
		   else {
	   			// if user wants to load the default saved sim
			   
		   }
  
	   } catch (NullPointerException e) {
	       System.err.println("NullPointerException in loading sim. " + e.getMessage());
	       Platform.exit();
	       System.exit(1); 	  
	       
	   } catch (Exception e) {
	       System.err.println("Exception in loading sim. " + e.getMessage());
	       Platform.exit();
	       System.exit(1); 
	   }	   
	   
	   if (selectedFile != null) {
			
			final File fileLocn = selectedFile;
		
			Platform.runLater(() -> {
				mainScene.createIndicator();
				mainScene.showWaitStage(MainScene.LOADING);
			});
			
			CompletableFuture.supplyAsync(() -> submitTask(fileLocn, false));
	        
		}
	   
		else {			
			logger.info("No file was selected. Loading is cancelled");
	        Platform.exit();
	        System.exit(1);
			//return;		
		}

   }

	public int submitTask(File fileLocn, boolean autosaveDefaultName) {
		Simulation.instance().getSimExecutor().execute(new LoadSimulationTask(fileLocn, autosaveDefaultName));
		return 1;
	}
   
	/*
	 * Loads the rest of the methods in MainScene. 
	*/
   public void finalizeMainScene() {
	   
		Platform.runLater(() -> {
	   
			prepareScene();
			
			mainScene.initializeTheme();						
			mainScene.prepareOthers();
			//2016-02-07 Added calling setMonitor() for screen detection
			// Note: setMonitor is needed for placing quotation pop at top right corner
			setMonitor(mainSceneStage);
			//mainSceneStage.setResizable(false);
			mainSceneStage.centerOnScreen();
			mainSceneStage.setTitle(Simulation.title);		
			mainSceneStage.show();
			mainSceneStage.requestFocus();

			mainScene.openInitialWindows();		   
			   
			mainScene.hideWaitStage(MainScene.LOADING);
		});
	   
   }

   /*
    * Loads settlement data from a saved sim
    * @param fileLocn user's sim filename
    */
	public class LoadSimulationTask implements Runnable {
		File fileLocn;
		boolean autosaveDefaultName;
		
		LoadSimulationTask(File fileLocn, boolean autosaveDefaultName){
			this.fileLocn = fileLocn;
			this.autosaveDefaultName = autosaveDefaultName;
		}
		
		public void run() {
			//logger.info("LoadSimulationTask is on " + Thread.currentThread().getName() + " Thread");
			
   			// Initialize the simulation.
			Simulation.instance().createNewSimulation(); 
			
			try {
				// Loading settlement data from the default saved simulation
				Simulation.instance().loadSimulation(fileLocn); // null means loading "default.sim"
			
	        } catch (Exception e) {
	            //e.printStackTrace();
	            exitWithError("Error : could not create a new simulation ", e);
	        }
			
			Simulation.instance().start(autosaveDefaultName);
			
			while (Simulation.instance().getMasterClock() == null)
				try {
					TimeUnit.MILLISECONDS.sleep(200L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			finalizeMainScene();
		}
	}

	/*
	 * Prepares the scene in the main scene
	 */
	public void prepareScene() {
		//logger.info("MainMenu's prepareScene() is on " + Thread.currentThread().getName());
		// prepare main scene
		mainScene.prepareMainScene();
		// creates and initialize scene
		mainSceneScene = mainScene.initializeScene();	
		// switch from the main menu's scene to the main scene's scene
		mainSceneStage.setScene(mainSceneScene);

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
    * Sets the main menu controller at the start of the Simulation.instance().
    */
   // 2015-09-27 Added setController()
   public void setController(ControlledScreen myScreenController) {
	   if (mainMenuController == null)
		   if (myScreenController instanceof MainMenuController )
			   mainMenuController = (MainMenuController) myScreenController;
   }
   
	public void chooseScreen(int num) {
		
		ObservableList<Screen> screens = Screen.getScreens();//.getScreensForRectangle(xPos, yPos, 1, 1); 
    	Screen currentScreen = screens.get(num);
		Rectangle2D rect = currentScreen.getVisualBounds();
		
		Screen primaryScreen = Screen.getPrimary();
	}
	
	public void setMonitor(Stage stage) {
		// Issue: how do we tweak mars-sim to run on the "active" monitor as chosen by user ?
		// "active monitor is defined by whichever computer screen the mouse pointer is or where the command console that starts mars-sim.
		// by default MSP runs on the primary monitor (aka monitor 0 as reported by windows os) only.
		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762 

		if (root == null) {
	       root = new StackPane();//starfield);
	       root.setPrefHeight(WIDTH);
	       root.setPrefWidth(HEIGHT);
		}
		
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
            //System.out.println("Monitor 2:    x : " + xPos + "   y : " + yPos);
        } else {
            stage.centerOnScreen();
            //System.out.println("calling centerOnScreen()");
            //System.out.println("Monitor 1:    x : " + xPos + "   y : " + yPos);
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

    /**
     * Exit the simulation with an error message.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void exitWithError(String message, Exception e) {
        showError(message, e);
        Platform.exit();
        System.exit(1);
    }

    /**
     * Show a modal error message dialog.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void showError(String message, Exception e) {
        if (e != null) {
            logger.log(Level.SEVERE, message, e);
        }
        else {
            logger.log(Level.SEVERE, message);
        }

        //if (!headless) {
        //	if (!OS.contains("mac"))
        //		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        	// Warning: cannot load the editor in macosx if it was a JDialog
        //}
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
		//marsProjectFX = null;
		executor = null;
		multiplayerMode = null;
		mainMenuController = null;
	}

}
