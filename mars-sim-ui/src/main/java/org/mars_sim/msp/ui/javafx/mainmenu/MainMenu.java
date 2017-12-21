/**
 * Mars Simulation Project
 * MainMenu.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.input.KeyCode;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.AnchorPane;
import javafx.animation.FadeTransition;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
//import javafx.scene.control.Separator;
//import javafx.scene.control.Toggle;
//import javafx.scene.control.ToggleGroup;
//import javafx.geometry.HPos;
//import javafx.scene.control.Button;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.ColumnConstraints;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.Priority;
//import javafx.scene.SceneAntialiasing;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Alert.AlertType;
//import javafx.scene.control.ButtonType;
//import javafx.stage.Window;
//import javafx.geometry.Point2D;
//import javafx.stage.Modality;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.stage.FileChooser;
import javafx.stage.Screen;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.config.ScenarioConfigEditorFX;
import org.mars_sim.msp.ui.javafx.config.controller.MainMenuController;
import org.mars_sim.msp.ui.javafx.networking.MultiplayerMode;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.tool.StartUpLocation;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.scene.GameScene;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.Tile.SkinType;

/*
 * The MainMenu class creates the Main Menu and the spinning Mars Globe for MSP
 */
@SuppressWarnings({ "restriction", "deprecation"})
public class MainMenu {

	// ------------------------------ FIELDS ------------------------------

	/** default logger. */
	private static Logger logger = Logger.getLogger(MainMenu.class.getName());

	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;
  
    //public static final int MUSIC_VOLUME = 0;
    //public static final int SOUND_EFFECT_VOLUME = 1;
    
	// Data members
    public static String screen1ID = "main";
    public static String screen1File = "/fxui/fxml/MainMenu.fxml";
    public static String screen2ID = "configuration";
    public static String screen2File = "/fxui/fxml/Configuration.fxml";
    public static String screen3ID = "credits";
    public static String screen3File = "/fxui/fxml/Credits.fxml";

	private boolean isExit = false;
	
	public int mainscene_width = 1366; //1920;//
	public int mainscene_height = 768; //1080;//

	public int native_width = 1366; //1920;//
	public int native_height = 768; //1080;//
	
	private double x = 0;
    private double y= 0;
    
	public float music_v = 50f;
	public float sound_effect_v = 50f;

	public boolean isFXGL = false;
    //private Point2D anchorPt;
    //private Point2D previousLocation;
    
	private Group root;
	
    private AnchorPane anchorPane;
    
    private StackPane stackPane;
    
    private StackPane rootPane;

	private Stage primaryStage;
	
	private GameScene gameScene;
	
	private MainMenu mainMenu;
	
	private MainScene mainScene;
	//private ScreensSwitcher screen;
	private MultiplayerMode multiplayerMode;
	
	private MainMenuController mainMenuController;

	private MenuApp menuApp;
	
	private JFXDialog settingDialog;
	
	private JFXDialog exitDialog;

	private List<Resolution> resList;
	
	private transient ThreadPoolExecutor executor;
	
    public MainMenu() {
       	//logger.info("MainMenu's constructor is on " + Thread.currentThread().getName());
    	mainMenu = this;
    	
		// See DPI Scaling at
		// http://news.kynosarges.org/2015/06/29/javafx-dpi-scaling-fixed/
		// "I guess we'll have to wait until Java 9 for more flexible DPI support.
		// In the meantime I managed to get JavaFX DPI scale factor,
		// but it is a hack (uses both AWT and JavaFX methods)"
/*
		// Number of actual horizontal lines (768p)
		double trueHorizontalLines = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		// Number of scaled horizontal lines. (384p for 200%)
		double scaledHorizontalLines = Screen.getPrimary().getBounds().getHeight();
		// DPI scale factor.
		double dpiScaleFactor = trueHorizontalLines / scaledHorizontalLines;
		
		logger.info("horizontal lines : " + trueHorizontalLines);
		logger.info("DPI Scale Factor is " + dpiScaleFactor);
*/
		
		Screen screen = Screen.getPrimary(); 
		Rectangle2D bounds = screen.getVisualBounds(); // getBounds();//
		
		native_width = (int) bounds.getWidth();
		native_height = (int) bounds.getHeight();

		mainscene_height = native_height;//currentRes.getHeight();
		mainscene_width = native_width;//currentRes.getWidth();

		setupResolutions();
		
		logger.info("Current Screen Resolution is " + native_width + " x " + native_height);
		
		// Test
		//logger.info("Earth's surface gravity : " + Math.round(PlanetType.EARTH.getSurfaceGravity()*100.0)/100.0 + " m/s^2");
		//logger.info("Mars's surface gravity : " + Math.round(PlanetType.MARS.getSurfaceGravity()*100.0)/100.0 + " m/s^2");		
 	}

    /*
     * Sets up and shows the MainMenu and prepare the stage for MainScene
     */
	@SuppressWarnings("restriction")
	public void initMainMenu(GameScene gameScene) {
		System.setProperty("sampler.mode", "true");   
		isFXGL = true;
		this.gameScene = gameScene;
		
    	Platform.setImplicitExit(false);

    	menuApp = new MenuApp(mainMenu, true);
    	
    	anchorPane = menuApp.createContent();
    	
    	stackPane = new StackPane(anchorPane);

    	stackPane.setPrefSize(WIDTH, HEIGHT);
    	//stackPane.setLayoutX(10);
    	//stackPane.setLayoutY(10);
       
    	double sceneWidth = stackPane.getPrefWidth();// + 30;
    	double sceneHeight = stackPane.getPrefHeight();// + 30;

    	// Create application area
    	@SuppressWarnings("restriction")
    	Rectangle applicationArea = RectangleBuilder.create()
                .width(sceneWidth)// - 10)
                .height(sceneHeight)// - 10)
                //.arcWidth(20)
                //.arcHeight(20)
                .fill(Color.rgb(0, 0, 0, 1))
                .x(0)
                .y(0)
                .strokeWidth(2)
                .stroke(Color.rgb(255, 255, 255, .70))
                .build();
       
	
    	Node closeRect = RectangleBuilder.create()
               .width(25)
               .height(25)
               .arcWidth(15)
               .arcHeight(15)
               .fill(Color.rgb(0, 0, 0, 1))
               .stroke(Color.WHITE)
               .build();
       
    	Text closeXmark = new Text(9, 16.5, "X");
    	closeXmark.setStroke(Color.WHITE);
    	closeXmark.setFill(Color.WHITE);
    	closeXmark.setStrokeWidth(2);
    	
    	// Create close button
    	final Group closeApp = new Group();
        //closeApp.translateXProperty().bind(gameScene.getWidth()-40);//.widthProperty().subtract(40));
    	closeApp.setTranslateX(WIDTH-30);//40);
    	closeApp.setTranslateY(5);
    	closeApp.getChildren().addAll(closeRect, closeXmark);
    	closeApp.setOnMouseClicked(new EventHandler<MouseEvent>() {
	       	@Override
	       	public void handle(MouseEvent event) {
		        Input input = FXGL.getInput();
				input.mockKeyPress(KeyCode.ESCAPE);
		        input.mockKeyRelease(KeyCode.ESCAPE);
	       	}
       });

       rootPane = new StackPane();
       rootPane.setPrefSize(stackPane.getPrefWidth()-300, stackPane.getPrefHeight());//stackPane.getPrefWidth()-50, stackPane.getPrefHeight()-50);
 
       root = new Group();
       root.getChildren().add(applicationArea);
       root.getChildren().add(stackPane);
       root.getChildren().add(closeApp);
       root.getChildren().add(rootPane);
    
       gameScene.addUINode(root);   

       //Scene scene = gameScene.getContentRoot().getScene(); // scene is null
       // Create an yellowish atmosphere around the globe
       //scene.getStylesheets().setAll(this.getClass().getResource("/fxui/css/demo/main.css").toExternalForm());
       // Add CSS styles
       //scene.getStylesheets().add(this.getClass().getResource("/fxui/css/mainmenu/mainmenu.css").toExternalForm());
       
       
       // Add keyboard control
       menuApp.getSpinningGlobe().getGlobe().handleKeyboard(gameScene.getContentRoot());
       // Add mouse control
       menuApp.getSpinningGlobe().getGlobe().handleMouse(gameScene.getContentRoot());

       // Makes the menu option box fades in
       root.setOnMouseEntered(new EventHandler<MouseEvent>(){
           public void handle(MouseEvent mouseEvent){
        	   menuApp.startAnimation();
               FadeTransition fadeTransition
                       = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
               fadeTransition.setFromValue(0.0);
               fadeTransition.setToValue(1.0);
               fadeTransition.play();
           }
        });

       // Makes the menu option box fades out
       root.setOnMouseExited(new EventHandler<MouseEvent>(){
           public void handle(MouseEvent mouseEvent){
        	   menuApp.endAnimation();
               FadeTransition fadeTransition
                       = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
               fadeTransition.setFromValue(1.0);
               fadeTransition.setToValue(0.0);
               fadeTransition.play();
        	   fadeTransition.setOnFinished(e -> menuApp.clearMenuItems());
           }
       });

   }
	
    /*
     * Sets up and shows the MainMenu and prepare the stage for MainScene
     */
	@SuppressWarnings("restriction")
	public void initMainMenu(Stage stage) {
		System.setProperty("sampler.mode", "true");   
	   //logger.info("MainMenu's initAndShowGUI() is on " + Thread.currentThread().getName());
		this.primaryStage = stage;

    	Platform.setImplicitExit(false);
		// Note: If this attribute is true, the JavaFX runtime will implicitly shutdown when the last window is closed;
		// the JavaFX launcher will call the Application.stop method and terminate the JavaFX application thread.
		// If this attribute is false, the application will continue to run normally even after the last window is closed,
		// until the application calls exit. The default value is true.

    	menuApp = new MenuApp(mainMenu, false);
    	anchorPane = menuApp.createContent();

    	stackPane = new StackPane(anchorPane);
    	stackPane.setPrefSize(WIDTH, HEIGHT);
 
    	double sceneWidth = stackPane.getPrefWidth();// + 30;
    	double sceneHeight = stackPane.getPrefHeight();// + 30;

    	// Create application area
    	@SuppressWarnings("restriction")
    	Rectangle applicationArea = RectangleBuilder.create()
                .width(sceneWidth)// - 10)
                .height(sceneHeight)// - 10)
                .arcWidth(10)
                .arcHeight(10)
                .fill(Color.rgb(0, 0, 0, 1))//.80))
                .x(0)
                .y(0)
                .strokeWidth(2)
                .stroke(Color.rgb(255, 255, 255, .70))
                .build();
       
    	// Create close button
    	final Group closeApp = new Group();
    	Node closeRect = RectangleBuilder.create()
               .width(25)
               .height(25)
               .arcWidth(15)
               .arcHeight(15)
               .fill(Color.rgb(0, 0, 0, .80))
               .stroke(Color.WHITE)
               .build();
       
    	Text closeXmark = new Text(9, 16.5, "X");
    	closeXmark.setStroke( Color.WHITE);
    	closeXmark.setFill(Color.WHITE);
    	closeXmark.setStrokeWidth(2);
    	closeApp.setTranslateY(5);
    	closeApp.getChildren().addAll(closeRect, closeXmark);
    	closeApp.setOnMouseClicked(new EventHandler<MouseEvent>() {
	       	@Override
	       	public void handle(MouseEvent event) {	        
	       		dialogOnExit(rootPane);
	
	       		if (!isExit) {
	       			event.consume();
	       		}
	       		else {
	       			Platform.exit();
	           		System.exit(0);
	       		}      		
	       	}      	
       });
       

       rootPane = new StackPane();
       rootPane.setPrefSize(stackPane.getPrefWidth()-300, stackPane.getPrefHeight());
       
       Group root = new Group();
       root.getChildren().add(applicationArea);
       root.getChildren().add(stackPane);
       root.getChildren().add(closeApp);
       root.getChildren().add(rootPane);
     
       Scene scene = new Scene(root, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);//Color.rgb(0, 0, 0, 0));
       // Create a sliver of sun-lit bright orange atmosphere around the Mars globe when being dragged around
       scene.getStylesheets().setAll(this.getClass().getResource("/fxui/css/mainmenu/globe.css").toExternalForm());
       // Add CSS styles
       scene.getStylesheets().add(this.getClass().getResource("/fxui/css/mainmenu/mainmenu.css").toExternalForm());
       
       scene.setFill(Color.BLACK); // if using Group, a black border will remain
       //scene.setFill(Color.TRANSPARENT); // if using Group, a white border will remain
       scene.setCursor(Cursor.HAND);

       // Makes the menu option box fades in
       scene.setOnMouseEntered(new EventHandler<MouseEvent>(){
           public void handle(MouseEvent mouseEvent){
        	   menuApp.startAnimation();
               FadeTransition fadeTransition
                       = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
               fadeTransition.setFromValue(0.0);
               fadeTransition.setToValue(1.0);
               fadeTransition.play();
           }
       });

       // Makes the menu option box fades out
       scene.setOnMouseExited(new EventHandler<MouseEvent>(){
           public void handle(MouseEvent mouseEvent){
        	   menuApp.endAnimation();
               FadeTransition fadeTransition
                       = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
               fadeTransition.setFromValue(1.0);
               fadeTransition.setToValue(0.0);
               fadeTransition.play();
        	   fadeTransition.setOnFinished(e -> menuApp.clearMenuItems());
           }
        });
       
       closeApp.translateXProperty().bind(scene.widthProperty().subtract(30));
 
       menuApp.getTitleStackPane().setOnMousePressed(new EventHandler<MouseEvent>() {
           @Override
           public void handle(MouseEvent event) {
               x = event.getSceneX();
               y = event.getSceneY();
           }
       });
       
       menuApp.getTitleStackPane().setOnMouseDragged(new EventHandler<MouseEvent>() {
           @Override
           public void handle(MouseEvent event) {
               stage.setX(event.getScreenX() - x);
               stage.setY(event.getScreenY() - y);
           }
       });
       
/*       
       // starting initial anchor point
       scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event){
                anchorPt = new Point2D(event.getScreenX(), event.getScreenY());
            }
        });
        
        // dragging the entire stage
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event){
                if (anchorPt != null && previousLocation != null) {
                    primaryStage.setX(previousLocation.getX() + event.getScreenX() - anchorPt.getX());
                    primaryStage.setY(previousLocation.getY() + event.getScreenY() - anchorPt.getY());                    
                }
            }
        });
        
        // set the current location
        scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event){
                previousLocation = new Point2D(primaryStage.getX(), primaryStage.getY());
            }
        });

       
        previousLocation = new Point2D(primaryStage.getX(), primaryStage.getY()); 
  */
       
       // Add keyboard control
       menuApp.getSpinningGlobe().getGlobe().handleKeyboard(scene.getRoot());
       // Add mouse control
       menuApp.getSpinningGlobe().getGlobe().handleMouse(scene.getRoot());
     
       stage.setOnCloseRequest(e -> {    
    		dialogOnExit(rootPane);

    		if (!isExit) {
    			e.consume();
    		}
    		else {
    			Platform.exit();
        		System.exit(0);
    		}
    		
        });


       stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
       //NOTE: OR use svg file with stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       stage.setScene(scene);
       
       setMonitor(stage);
       
       stage.centerOnScreen();
       stage.initStyle(StageStyle.TRANSPARENT);
       stage.show();


   }
	
	
	/** 
	 * Removes unsupported resolutions 
	 * @param size the highest width index supported
	 */
	public void removeRes(int size) {
		List<Resolution> newRes = new ArrayList<>();
		for (int i = 0; i < size + 1; i++)
			newRes.add(resList.get(i));
		resList = newRes;
	}
	
	/**
	 * Setups the resolution list
	 */
	public void setupResolutions() {
		//resList = new ArrayList<>();
		resList = Arrays.asList(
				new Resolution(1024, 768),
				new Resolution(1280, 720),
				new Resolution(1280, 800),
				new Resolution(1366, 768),
				new Resolution(1440, 900), 
				new Resolution(1600, 900), 
				new Resolution(1920, 1080),
				new Resolution(2560, 1440), 
				new Resolution(2560, 1600)); 

		if (native_width > 2560) {
			; // resList to stay the same
		}
		
		else if (native_width == 2560) {
	    	if (native_height == 1600) {
			    ; // resList to stay the same
	    	}
	    	else if (native_height == 1440) {
	    		removeRes(7);
	    	}
	    }
	    else if (native_width == 1920)
	    	removeRes(6);
	    else if (native_width == 1600)
	    	removeRes(5);	 
	    else if (native_width == 1440)
	    	removeRes(4);	    
	    else if (native_width == 1366)
	    	removeRes(3);	    
	    else if (native_width == 1280) {	 
	    	if (native_height == 800) {
	    		removeRes(2);
	    	}
	    	else if (native_height == 720) {
	    		removeRes(1);
	    	}
	    }
	    else if (native_width == 1024)
	    	removeRes(1);
	}

	public Stage getStage() {
		return primaryStage;
	}

   public MainScene getMainScene() {
	   return mainScene;
   }

   public MultiplayerMode getMultiplayerMode() {
	   return multiplayerMode;
	}
   
   public void runNew(boolean isFXGL) {
	   closeStage(isFXGL);
	   createMainScene();
   }
   
   public void createMainScene() {
	   // creates a mainScene instance
	   mainScene = new MainScene(mainscene_width, mainscene_height, gameScene);

       try {
    	   // Loads Scenario Config Editor
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

   /**
    * Opens the file chooser to select a saved sim to load 
    */
   public void runLoad(boolean isFXGL) {
	   closeStage(isFXGL);
	   loadSim(null);
   }
   
   public void closeStage(boolean isFXGL) {
	   if (isFXGL) {
		   ((Stage) gameScene.getRoot().getScene().getWindow()).hide();
		   gameScene.removeUINode(root);
		   //((Stage) gameScene.getRoot().getScene().getWindow()).close();   
	   }
	   else {
		   primaryStage.setIconified(true);
		   primaryStage.hide();
		   primaryStage.close();
	   }
   }

   /**
    * Loads the setting dialog
    */
   public void runSettings() {   
	   selectResolutionDialog(rootPane);
   }

   /*
    * Loads the simulation file via the terminal or FileChooser.
    * @param selectedFile the saved sim
    */
   public void loadSim(File selectedFile) {

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
			   if (isFXGL)
				   selectedFile = chooser.showOpenDialog(gameScene.getRoot().getScene().getWindow());
			   else
				   selectedFile = chooser.showOpenDialog(primaryStage);
		   }

		   //else {
	   			// if user wants to load the default saved sim
		   //}

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
				mainScene = new MainScene(mainscene_width, mainscene_height, gameScene);
				
				mainScene.createLoadingIndicator();
				mainScene.showWaitStage(MainScene.LOADING);
			});

			CompletableFuture.supplyAsync(() -> submitTask(fileLocn, false));

		}

		else {
			logger.info("No file was selected. Loading is cancelled");
	        Platform.exit();
	        System.exit(1);
		}

   }

	public int submitTask(File fileLocn, boolean autosaveDefaultName) {
		Simulation.instance().getSimExecutor().execute(new LoadSimulationTask(fileLocn, autosaveDefaultName));
		return 1;
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

			Simulation sim = Simulation.instance();
   			// Initialize the simulation.
			Simulation.createNewSimulation();

			try {
				// Loading settlement data from the default saved simulation
				sim.loadSimulation(fileLocn); // null means loading "default.sim"
				

	        } catch (Exception e) {
	            //e.printStackTrace();
	            exitWithError("Error : could not create a new simulation ", e);
	        }

			sim.start(autosaveDefaultName);

			while (sim.getMasterClock() == null)
				try {
					TimeUnit.MILLISECONDS.sleep(200L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			mainScene.finalizeMainScene();
		}
	}

   public void runMultiplayer() {
	   //logger.info("MainMenu's runThree() is on " + Thread.currentThread().getName() + " Thread");
	   Simulation.instance().getSimExecutor().submit(new MultiplayerTask());
	   primaryStage.setIconified(true);//hide();
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
		   if (myScreenController instanceof MainMenuController ) {
			   mainMenuController = (MainMenuController) myScreenController;
			   mainMenuController.setBuild();
		   }
	}

/*	
	public void chooseScreen(int num) {
		ObservableList<Screen> screens = Screen.getScreens();//.getScreensForRectangle(xPos, yPos, 1, 1);
    	Screen currentScreen = screens.get(num);
		Rectangle2D rect = currentScreen.getVisualBounds();
		Screen primaryScreen = Screen.getPrimary();
	}
*/
	
	public void setMonitor(Stage stage) {
		// Issue: how do we tweak mars-sim to run on the "active" monitor as chosen by user ?
		// "active monitor is defined by whichever computer screen the mouse pointer is or where the command console that starts mars-sim.
		// by default MSP runs on the primary monitor (aka monitor 0 as reported by windows os) only.
		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762

/*
		AnchorPane anchor = null;
		if (root == null) {
	       anchor = new AnchorPane();
	       anchor.setPrefWidth(WIDTH);
	       anchor.setPrefHeight(HEIGHT);
		}
*/

		StartUpLocation startUpLoc = new StartUpLocation(WIDTH, HEIGHT);
        double xPos = startUpLoc.getXPos();
        double yPos = startUpLoc.getYPos();
        // Set Only if X and Y are not zero and were computed correctly
     	//ObservableList<Screen> screens = Screen.getScreensForRectangle(xPos, yPos, 1, 1);
     	//ObservableList<Screen> screens = Screen.getScreens();
    	//System.out.println("# of monitors : " + screens.size());

        if (xPos != 0 && yPos != 0) {
            stage.setX(xPos);
            stage.setY(yPos);
            //System.out.println("Monitor 2:    x : " + xPos + "   y : " + yPos);
        } else {
            //System.out.println("calling centerOnScreen()");
            //System.out.println("Monitor 1:    x : " + xPos + "   y : " + yPos);
        }
        stage.centerOnScreen();
	}

	//public ScreensSwitcher getScreensSwitcher() {
	//	return screen;
	//}

	public MainMenuController getMainMenuController() {
		return mainMenuController;
	}

	public SpinningGlobe getSpinningGlobe() {
		return menuApp.getSpinningGlobe();
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
    }


    public StackPane getPane() {
    	return rootPane;
    }
    
    
	/**
	 * Open the exit dialog box
	 * @param pane
	 */
	public void dialogOnExit(StackPane pane) {
		
		if (exitDialog == null && (settingDialog == null || (settingDialog != null && !settingDialog.isVisible()))) {
     
			Label l = new Label("Do you really want to exit ?");//mainScene.createBlendLabel(Msg.getString("MainScene.exit.header"));
			l.setPadding(new Insets(10, 10, 10, 10));
			l.setFont(Font.font(null, FontWeight.BOLD, 14));
			HBox hb = new HBox();
			JFXButton b1 = new JFXButton("Exit");
			b1.setStyle("-fx-background-color: white;");
			JFXButton b2 = new JFXButton("Back");
			b2.setStyle("-fx-background-color: white;");
			hb.getChildren().addAll(b1, b2);
			hb.setAlignment(Pos.CENTER);
			HBox.setMargin(b1, new Insets(3,3,3,3));
			HBox.setMargin(b2, new Insets(3,3,3,3));
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			vb.setPadding(new Insets(5, 5, 5, 5));
			vb.getChildren().addAll(l, hb);
			StackPane sp = new StackPane(vb);
			sp.setStyle("-fx-background-color:rgba(0,0,0,0.1);");
			StackPane.setMargin(vb, new Insets(10,10,10,10));
			exitDialog = new JFXDialog();
			//dialog.setDialogContainer(pane);
			exitDialog.setTransitionType(DialogTransition.BOTTOM);
			exitDialog.setContent(sp);
			exitDialog.show(pane);
			
			b1.setOnAction(e -> {
				isExit = true;
				exitDialog.close();
				Platform.exit();
				System.exit(0);
			});
			
			b2.setOnAction(e -> {
				exitDialog.close();
				e.consume();
			});

		}
		else if (exitDialog != null 
				&& !exitDialog.isVisible()
			 	&& (settingDialog == null || (settingDialog != null && !settingDialog.isVisible()))) {
			exitDialog.show(pane);
		}
	}	

	/**
	 * Obtains the resolution of the current screen
	 */
	public Resolution obtainResolution() {
		Resolution r = null;
	    if (native_width == 2560) {
	    	if (native_height == 1600) {
			    r = resList.get(8);
	    	}
	    	else if (native_height == 1440) {
	    		r = resList.get(7);
	    	}
	    }
	    else if (native_width == 1920)
	    	r = resList.get(6);
	    else if (native_width == 1600)
	    	r = resList.get(5);	 
	    else if (native_width == 1440)
	    	r = resList.get(4);	    
	    else if (native_width == 1366)
	    	r = resList.get(3);	    
	    else if (native_width == 1280) {	 
	    	if (native_height == 800) {
	    		r = resList.get(2);
	    	}
	    	else if (native_height == 720) {
	    		r = resList.get(1);
	    	}
	    }
	    else if (native_width == 1024)
	    	r = resList.get(0);
	    else 
	    	// by default, set to 1024 x 768
	    	r = resList.get(0);
	    
	    return r;
	}
	
	/**
	 * Selects the game screen resolution in this dialog box
	 * @param pane
	 */
	public void selectResolutionDialog(StackPane pane) {
		if (settingDialog == null && (exitDialog == null || (exitDialog != null && !exitDialog.isVisible()))) {

			Label titleLabel = new Label("S E T T I N G S");
			titleLabel.setAlignment(Pos.TOP_CENTER);
			titleLabel.setTextAlignment(TextAlignment.CENTER);
			titleLabel.setContentDisplay(ContentDisplay.TOP);
			titleLabel.setPadding(new Insets(10, 10, 10, 10));
			titleLabel.setStyle("-fx-text-fill: lightgoldenrodyellow;");
			titleLabel.setFont(Font.loadFont(MenuApp.class.getResource("/fonts/Penumbra-HalfSerif-Std_35114.ttf").toExternalForm(), 20));
			//titleLabel.setFont(Font.font(null, FontWeight.BOLD, 20));
							
			JFXButton done_btn = new JFXButton("Done");
			done_btn.setStyle("-fx-background-color: lightgoldenrodyellow;");//lightgrey;");
			
			HBox return_hb = new HBox();
			return_hb.getChildren().addAll(done_btn);
			return_hb.setAlignment(Pos.CENTER);
			
			HBox.setMargin(done_btn, new Insets(10, 10, 10, 10));
	
			Label reslabel = new Label("Select Resolution  :  ");
			reslabel.setAlignment(Pos.TOP_LEFT);
			reslabel.setPadding(new Insets(10, 10, 10, 10));
			reslabel.setStyle("-fx-text-fill: lightgoldenrodyellow;");
			reslabel.setFont(Font.font(null, FontWeight.NORMAL, 16));
			
			JFXComboBox<Resolution> resCombo = new JFXComboBox<>();
			resCombo.setStyle("-fx-text-fill: lightgoldenrodyellow;");
			resCombo.getStyleClass().add("jfx-combo-box");	
			resCombo.getItems().addAll(resList);
			 
			Resolution currentRes = obtainResolution();
			
			resCombo.setValue(currentRes);
						
			//System.out.println(mainscene_width + " x " + mainscene_height);
					
			resCombo.setPromptText("Select desired screen resolution");
			
			resCombo.valueProperty().addListener(new ChangeListener<Resolution>() {
		        @Override public void changed(ObservableValue ov, Resolution t, Resolution t1) {
		        	if (!t.equals(t1)) {
		        		mainscene_width = t1.getWidth();
		        		mainscene_height = t1.getHeight();
		        	}
		        }    
		    });
			
			// Set up the slider for background music and sound effect
			Tile soundTile0 = musicSetting();
			HBox sound0 = new HBox();
			sound0.setPadding(new Insets(5, 5, 5, 5));
			sound0.getChildren().addAll(soundTile0);
			sound0.setAlignment(Pos.CENTER);
	
			Tile soundTile1 = soundEffectSetting();
			HBox sound1 = new HBox();
			sound1.setPadding(new Insets(5, 5, 5, 5));
			sound1.getChildren().addAll(soundTile1);
			sound1.setAlignment(Pos.CENTER);
			
			//Horizontal separator
			//Separator separator = new Separator();
			//separator.setMaxWidth(160);
			//separator.setHalignment(HPos.LEFT);
			
			VBox emptyVB = new VBox();
			//vb.setAlignment(Pos.CENTER);
			emptyVB.setPadding(new Insets(25, 25, 25, 25));
						
			HBox comboBox = new HBox();
			comboBox.getChildren().addAll(reslabel, resCombo);
			comboBox.setAlignment(Pos.CENTER);
			
			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			vb.setPadding(new Insets(15, 15, 15, 15));
			vb.getChildren().addAll(titleLabel, emptyVB, comboBox, sound0, sound1, return_hb); 
					
			StackPane sp = new StackPane(vb);
			sp.setStyle("-fx-background-color: black;-fx-background: grey;-fx-text-fill: lightgoldenrodyellow;");
			StackPane.setMargin(vb, new Insets(10,10,10,10));
			settingDialog = new JFXDialog();
			//dialog.setDialogContainer(pane);
			settingDialog.setContent(sp);
			settingDialog.setTransitionType(DialogTransition.BOTTOM);
			settingDialog.show(pane);
			
			done_btn.setOnAction(e -> {
				//pane.setPrefWidth(stackPane.getPrefWidth()-300);
				settingDialog.close();
				e.consume();
			});

		}
		else if (settingDialog != null 
			&& !settingDialog.isVisible()
			&& (exitDialog == null 
				|| (exitDialog != null && !exitDialog.isVisible()))) {
				settingDialog.show(pane);
			}
	}	

	
    public Tile musicSetting() {
        Tile switchSliderTile = TileBuilder.create()
                .skinType(SkinType.SWITCH_SLIDER)
                .prefSize(450, 150)
                .backgroundColor(Color.BLACK)//.rgb(105,105,105))//.DARKGRAY)//.rgb(255, 255, 255, 0.1))
                //.title(title)
                .description("Background Music Volume")
                .descriptionAlignment(Pos.TOP_CENTER)
                .textVisible(true)
                .decimals(0)
                .minValue(0)
                .maxValue(100)
                .value(music_v)
                .build();
        
        switchSliderTile.setActive(true);

        switchSliderTile.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {

				if (old_val != new_val) {
					float vol = new_val.floatValue();
					
					if (vol <= 5) { 
						music_v = 0;
			            //System.out.println("Background Music is mute.");    
			            switchSliderTile.setValue(0);
			            switchSliderTile.setActive(false);
					} else {
						music_v = ((int)(vol*5f))/5;
						switchSliderTile.setActive(true);
					}
					
					if (!switchSliderTile.isActive()) {
						switchSliderTile.setValue(0);
						//switchSliderTile.setActive(false);
					}
				}

			}
		});

		switchSliderTile.setOnSwitchPressed(e -> {
			if (switchSliderTile.getCurrentValue() < 5 || !switchSliderTile.isActive()) {
				switchSliderTile.setValue(0);
				switchSliderTile.setActive(false);
			}
			else {
				switchSliderTile.setActive(true);
				if (switchSliderTile.getCurrentValue() < 5)
					switchSliderTile.setValue(80);
			}
				
		});
		
		switchSliderTile.setOnSwitchReleased(e -> {
			if (switchSliderTile.getCurrentValue() < 5 || !switchSliderTile.isActive()) {
				switchSliderTile.setValue(0);
				switchSliderTile.setActive(false);
			}
			else {
				switchSliderTile.setActive(true);
				if (switchSliderTile.getCurrentValue() < 5)
					switchSliderTile.setValue(80);
			}
		});

		return switchSliderTile;
    }	
 
    
    public Tile soundEffectSetting() {

        Tile switchSliderTile = TileBuilder.create()
                .skinType(SkinType.SWITCH_SLIDER)
                .prefSize(450, 150)
                .backgroundColor(Color.BLACK)//.rgb(105,105,105))//.DARKGRAY)//.rgb(255, 255, 255, 0.1))
                //.title(title)
                .description("Sound Effect Volume")
                .descriptionAlignment(Pos.TOP_CENTER)
                .textVisible(true)
                .decimals(0)
                .minValue(0)
                .maxValue(100)
                .value(sound_effect_v)
                .build();

        switchSliderTile.setActive(true);
        
        switchSliderTile.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {

				if (old_val != new_val) {
					float vol = new_val.floatValue();

					if (vol <= 5) { 
						sound_effect_v = 0f;
						switchSliderTile.setValue(0);
			            //System.out.println("Sound Effect is mute.");    
			            switchSliderTile.setActive(false);
					} else {
						sound_effect_v = ((int)(vol*5f))/5;
						switchSliderTile.setActive(true);

					}
					
					if (!switchSliderTile.isActive()) {
						switchSliderTile.setValue(0);
						//switchSliderTile.setActive(false);
					}
						
				}
			}
		});

		switchSliderTile.setOnSwitchPressed(e -> {
			
			if (switchSliderTile.getCurrentValue() < 5 || !switchSliderTile.isActive()) {
				switchSliderTile.setValue(0);
				switchSliderTile.setActive(false);
			}
			else {
				switchSliderTile.setActive(true);
				if (switchSliderTile.getCurrentValue() < 5)
					switchSliderTile.setValue(80);
			}
				
		});
		
		switchSliderTile.setOnSwitchReleased(e -> {
			if (switchSliderTile.getCurrentValue() < 5 || !switchSliderTile.isActive()) {
				switchSliderTile.setValue(0);
				switchSliderTile.setActive(false);
			}
			else {
				switchSliderTile.setActive(true);
				if (switchSliderTile.getCurrentValue() < 5)
					switchSliderTile.setValue(80);
			}
		});

		return switchSliderTile;
    }	
    
	//public void setScreenSize(int w, int h) {
	//	mainscene_width = w;
	//	mainscene_height = h;
	//}
	
	public void destroy() {
		anchorPane = null;
		primaryStage = null;
		mainMenu = null;
		mainScene = null;
		executor = null;
		multiplayerMode = null;
		mainMenuController = null;
		menuApp = null;
		resList = null;
		
	}

	private class Resolution {
		
		int width;
		int height;
		
		Resolution(int width, int height) {
			this.width = width;
			this.height	= height;
			
		}
				
		public int getHeight() {
			return height;
		}

		public int getWidth() {
			return width;
		}

		@Override
		public String toString() {
			return width + " x " + height;
		}
	}
}
