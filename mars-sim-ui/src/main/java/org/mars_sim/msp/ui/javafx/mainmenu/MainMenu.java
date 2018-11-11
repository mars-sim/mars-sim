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
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.input.KeyCode;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Alert;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.AnchorPane;
import javafx.animation.AnimationTimer;

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
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.stage.FileChooser;
import javafx.stage.Screen;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.terminal.Commander;
import org.mars_sim.msp.core.tool.Conversion;
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
import com.jfoenix.controls.JFXTextField;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.Tile.SkinType;

/*
 * The MainMenu class creates the Main Menu and the spinning Mars Globe for MSP
 */
public class MainMenu {

	// ------------------------------ FIELDS ------------------------------

	/** default logger. */
	private static Logger logger = Logger.getLogger(MainMenu.class.getName());

	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'

	private static boolean isExit = false;
	private static boolean isSoundDisabled = false;
	
	private static boolean[] goodToGo = {false, false, false, false, false, false};
	
	public static final int WIDTH = 1366;
	public static final int HEIGHT = 768;
	
	private static final int STAR_COUNT = 100;// 00;
	private static final int TIME = 20_000_000;// 2_000_000_000;

	// public static final int MUSIC_VOLUME = 0;
	// public static final int SOUND_EFFECT_VOLUME = 1;


	public static String screen1ID = "main";
	public static String screen1File = "/fxui/fxml/MainMenu.fxml";
	public static String screen2ID = "configuration";
	public static String screen2File = "/fxui/fxml/Configuration.fxml";
	public static String screen3ID = "credits";
	public static String screen3File = "/fxui/fxml/Credits.fxml";

	// Data members
	public boolean isFXGL = false;
	
	public int mainscene_width = 1366; // 1920;//
	public int mainscene_height = 768; // 1080;//

	public int native_width = 1366; // 1920;//
	public int native_height = 768; // 1080;//

	private double x = 0;
	private double y = 0;

	public float music_v = 50f;
	public float sound_effect_v = 50f;

	// private Point2D anchorPt;
	// private Point2D previousLocation;

	private Rectangle[] nodes = new Rectangle[STAR_COUNT];
	
	private final double[] angles = new double[STAR_COUNT];
	private final long[] start = new long[STAR_COUNT];

	private final Random random = new Random();
	
	private Group root;
	
	private Scene scene;
	
	private AnchorPane menuAPane;

	private StackPane mainMenuSPane;

	private StackPane globeSPane;

	private Stage primaryStage;

	private GameScene gameScene;

	private MainMenu mainMenu;

	private MainScene mainScene;

	private MultiplayerMode multiplayerMode;

	private MainMenuController mainMenuController;

	private MenuApp menuApp;

	private List<Resolution> resList;

	private transient ThreadPoolExecutor executor;

	private AnimationTimer starsTimer;
	
	private EventHandler<MouseEvent> enteredHandler;

	private EventHandler<MouseEvent> exitedHandler;

	private static JFXDialog settingDialog;
	private static JFXDialog exitDialog;
	private static Simulation sim = Simulation.instance();
	
	public MainMenu() {
		// logger.config("MainMenu's constructor is on " +
		// Thread.currentThread().getName());
		mainMenu = this;

		// See DPI Scaling at
		// http://news.kynosarges.org/2015/06/29/javafx-dpi-scaling-fixed/
		// "I guess we'll have to wait until Java 9 for more flexible DPI support.
		// In the meantime I managed to get JavaFX DPI scale factor,
		// but it is a hack (uses both AWT and JavaFX methods)"

//		// Number of actual horizontal lines (768p)
//		double trueHorizontalLines = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
//		// Number of scaled horizontal lines. (384p for 200%)
//		double scaledHorizontalLines = Screen.getPrimary().getBounds().getHeight();
//		// DPI scale factor.
//		double dpiScaleFactor = trueHorizontalLines / scaledHorizontalLines;
//		
//		logger.config("horizontal lines : " + trueHorizontalLines);
//		logger.config("DPI Scale Factor is " + dpiScaleFactor);

		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds(); // getBounds();//

		native_width = (int) bounds.getWidth();
		native_height = (int) bounds.getHeight();

		// Detect the current native screen resolution
//		mainscene_height = native_height;
//		mainscene_width = native_width;

		// Assume the typical laptop screen resolution. Will change it later all
		// modifications are done
		mainscene_height = HEIGHT;
		mainscene_width = WIDTH;

		setupResolutions();

		logger.config("Current Screen Resolution is " + native_width + " x " + native_height);

		// Note : Testing only
//		 logger.config("Earth's surface gravity : " +
//		 Math.round(PlanetType.EARTH.getSurfaceGravity()*100.0)/100.0 + " m/s^2");
//		 logger.config("Mars's surface gravity : " +
//		 Math.round(PlanetType.MARS.getSurfaceGravity()*100.0)/100.0 + " m/s^2");
	}

	/*
	 * Sets up and shows the MainMenu and prepare the stage for MainScene
	 */
	public void initMainMenu(GameScene gameScene) {
		System.setProperty("sampler.mode", "true");
		isFXGL = true;
		this.gameScene = gameScene;

		Platform.setImplicitExit(false);

		menuApp = new MenuApp(mainMenu, true);

		menuAPane = menuApp.createContent();

		mainMenuSPane = new StackPane(menuAPane);

//		primaryStage = ((Stage) gameScene.getRoot().getScene().getWindow()); //new Stage();
//		primaryStage.setWidth(WIDTH);
//		primaryStage.setHeight(HEIGHT);

		mainMenuSPane.setPrefSize(WIDTH, HEIGHT);
		mainMenuSPane.setMaxSize(WIDTH, HEIGHT);
		// stackPane.setLayoutX(10);
		// stackPane.setLayoutY(10);

		double sceneWidth = mainMenuSPane.getPrefWidth();// + 30;
		double sceneHeight = mainMenuSPane.getPrefHeight();// + 30;

//		 Create application area
//	    	@SuppressWarnings("restriction")
//	    	Rectangle applicationArea = RectangleBuilder.create()
//	                .width(sceneWidth)// - 10)
//	                .height(sceneHeight)// - 10)
//	                //.arcWidth(20)
//	                //.arcHeight(20)
//	                .fill(Color.rgb(0, 0, 0, 1))
//	                .x(0)
//	                .y(0)
//	                .strokeWidth(2)
//	                .stroke(Color.rgb(255, 255, 255, .70))
//	                .build();

		Rectangle applicationArea = new Rectangle();
		applicationArea.setWidth(sceneWidth);
		applicationArea.setHeight(sceneHeight);
		applicationArea.setFill(Color.rgb(0, 0, 0, 1));
		applicationArea.setX(0);
		applicationArea.setY(0);
		applicationArea.setStrokeWidth(2);
		applicationArea.setStroke(Color.rgb(255, 255, 255, .70));

//	    	Node closeRect = RectangleBuilder.create()
//	               .width(25)
//	               .height(25)
//	               .arcWidth(15)
//	               .arcHeight(15)
//	               .fill(Color.rgb(0, 0, 0, 1))
//	               .stroke(Color.WHITE)
//	               .build();

		Rectangle closeRect = new Rectangle();
		closeRect.setWidth(25);
		closeRect.setHeight(25);
		closeRect.setFill(Color.rgb(0, 0, 0, 1));
		closeRect.setArcWidth(15);
		closeRect.setArcHeight(15);
		closeRect.setStroke(Color.WHITE);

		Text closeXmark = new Text(9, 16.5, "X");
		closeXmark.setStroke(Color.WHITE);
		closeXmark.setFill(Color.WHITE);
		closeXmark.setStrokeWidth(2);

		// Create close button
		final Group closeApp = new Group();
		// closeApp.translateXProperty().bind(gameScene.getWidth()-40);//.widthProperty().subtract(40));
		closeApp.setTranslateX(WIDTH - 30);// 40);
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

		globeSPane = new StackPane();
		globeSPane.setPrefSize(mainMenuSPane.getPrefWidth() - 300, mainMenuSPane.getPrefHeight());// stackPane.getPrefWidth()-50,
																									// stackPane.getPrefHeight()-50);

		// Add a moving starfield behind the Mars Globe
		createMovingStarfield();
		final Group starfield = new Group(nodes);

		root = new Group();
		root.getChildren().add(applicationArea);
		root.getChildren().add(starfield);
		root.getChildren().add(mainMenuSPane);
		root.getChildren().add(closeApp);
		root.getChildren().add(globeSPane);

		gameScene.addUINode(root);

		// Scene scene = gameScene.getContentRoot().getScene(); // scene is null
		// Create an yellowish atmosphere around the globe
		// scene.getStylesheets().setAll(this.getClass().getResource("/fxui/css/demo/main.css").toExternalForm());
		// Add CSS styles
		// scene.getStylesheets().add(this.getClass().getResource("/fxui/css/mainmenu/mainmenu.css").toExternalForm());

		// Add keyboard control
		menuApp.getSpinningGlobe().getGlobe().handleKeyboard(gameScene.getContentRoot());
		// Add mouse control
		menuApp.getSpinningGlobe().getGlobe().handleMouse(gameScene.getContentRoot());

		// Makes the menu option box fades in
		enteredHandler = new EventHandler<MouseEvent>(){
	        public void handle(MouseEvent event){
	        	menuApp.addLineMenuBox();
				menuApp.startAnimation(menuApp.getMenuBox());
				menuApp.startBoxAnimation();
	        }
		};

		root.setOnMouseEntered(enteredHandler);

		// Makes the menu option box fades out
		exitedHandler = new EventHandler<MouseEvent>(){
	        public void handle(MouseEvent event){
				menuApp.endLineAnimation();
				menuApp.endMenuBoxAnimation();
	        }
		};

		root.setOnMouseExited(exitedHandler);

		
//		// Makes the menu option box fades in
//		root.setOnMouseEntered(new EventHandler<MouseEvent>() {
//			public void handle(MouseEvent mouseEvent) {
//				menuApp.startAnimation();
//				FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
//				fadeTransition.setFromValue(0.0);
//				fadeTransition.setToValue(1.0);
//				fadeTransition.play();
//			}
//		});
//
//		// Makes the menu option box fades out
//		root.setOnMouseExited(new EventHandler<MouseEvent>() {
//			public void handle(MouseEvent mouseEvent) {
//				menuApp.endLineAnimation();
//				FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
//				fadeTransition.setFromValue(1.0);
//				fadeTransition.setToValue(0.0);
//				fadeTransition.play();
//				fadeTransition.setOnFinished(e -> menuApp.clearMenuBoxItems());
//			}
//		});

	}

	public void createMovingStarfield() {
		for (int i = 0; i < STAR_COUNT; i++) {
			nodes[i] = new Rectangle(1, 1, Color.DARKGOLDENROD);// .WHITE);
			angles[i] = 2.0 * Math.PI * random.nextDouble();
			start[i] = random.nextInt(TIME);
		}

		starsTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				final double width = 0.25 * WIDTH;// primaryStage.getWidth();
				final double height = 0.5 * HEIGHT;// primaryStage.getHeight();
				final double radius = Math.sqrt(2) * Math.max(width, height);
				for (int i = 0; i < STAR_COUNT; i++) {
					final Node node = nodes[i];
					final double angle = angles[i];
					final long t = (now - start[i]) % TIME;
					final double d = t * radius / TIME;
					node.setTranslateX(Math.cos(angle) * d + width);
					node.setTranslateY(Math.sin(angle) * d + height);
				}
			}
		};

		starsTimer.start();

	}

	/*
	 * Sets up and shows the MainMenu and prepare the stage for MainScene
	 */
	public void initMainMenu(Stage stage) {
		System.setProperty("sampler.mode", "true");
		// logger.config("MainMenu's initAndShowGUI() is on " +
		// Thread.currentThread().getName());
		this.primaryStage = stage;

		Platform.setImplicitExit(false);
		// Note: If this attribute is true, the JavaFX runtime will implicitly shutdown
		// when the last window is closed;
		// the JavaFX launcher will call the Application.stop method and terminate the
		// JavaFX application thread.
		// If this attribute is false, the application will continue to run normally
		// even after the last window is closed,
		// until the application calls exit. The default value is true.

		menuApp = new MenuApp(mainMenu, false);
		menuAPane = menuApp.createContent();

		mainMenuSPane = new StackPane(menuAPane);
		mainMenuSPane.setPrefSize(WIDTH, HEIGHT);
		mainMenuSPane.setMaxSize(WIDTH, HEIGHT);

		double sceneWidth = mainMenuSPane.getPrefWidth();// + 30;
		double sceneHeight = mainMenuSPane.getPrefHeight();// + 30;

		// Create application area
//    	@SuppressWarnings("restriction")
//    	Rectangle applicationArea = RectangleBuilder.create()
//                .width(sceneWidth)// - 10)
//                .height(sceneHeight)// - 10)
//                .arcWidth(10)
//                .arcHeight(10)
//                .fill(Color.rgb(0, 0, 0, 1))//.80))
//                .x(0)
//                .y(0)
//                .strokeWidth(2)
//                .stroke(Color.rgb(255, 255, 255, .70))
//                .build();

		// Add a moving starfield behind the Mars Globe
		createMovingStarfield();
		final Group starfield = new Group(nodes);

		Rectangle applicationArea = new Rectangle();
		applicationArea.setWidth(sceneWidth);
		applicationArea.setHeight(sceneHeight);
		applicationArea.setFill(Color.rgb(0, 0, 0, 1));
		applicationArea.setArcWidth(10);
		applicationArea.setArcHeight(10);
		applicationArea.setX(0);
		applicationArea.setY(0);
		applicationArea.setStrokeWidth(2);
		applicationArea.setStroke(Color.rgb(255, 255, 255, .70));

		// Create close button
		final Group closeApp = new Group();

		Rectangle closeRect = new Rectangle();
		closeRect.setWidth(25);
		closeRect.setHeight(25);
		closeRect.setFill(Color.rgb(0, 0, 0, .8));
		closeRect.setArcWidth(15);
		closeRect.setArcHeight(15);

		Text closeXmark = new Text(9, 16.5, "X");
		closeXmark.setStroke(Color.WHITE);
		closeXmark.setFill(Color.WHITE);
		closeXmark.setStrokeWidth(2);
		closeApp.setTranslateY(5);
		closeApp.getChildren().addAll(closeRect, closeXmark);
		closeApp.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				dialogOnExit(globeSPane);

				if (!isExit) {
					event.consume();
				} else {
					Platform.exit();
					System.exit(0);
				}
			}
		});

		globeSPane = new StackPane();
		globeSPane.setPrefSize(mainMenuSPane.getPrefWidth() - 300, mainMenuSPane.getPrefHeight());

		Group root = new Group();
		root.getChildren().add(applicationArea);
		root.getChildren().add(starfield);
		root.getChildren().add(mainMenuSPane);
		root.getChildren().add(closeApp);
		root.getChildren().add(globeSPane);
		
		scene = new Scene(root, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);// Color.rgb(0, 0, 0,
																									// 0));
		// Create a sliver of sun-lit bright orange atmosphere around the Mars globe
		// when being dragged around
		scene.getStylesheets().setAll(this.getClass().getResource("/fxui/css/mainmenu/globe.css").toExternalForm());
		// Add CSS styles
		scene.getStylesheets().add(this.getClass().getResource("/fxui/css/mainmenu/mainmenu.css").toExternalForm());

		scene.setFill(Color.BLACK); // if using Group, a black border will remain
		// scene.setFill(Color.TRANSPARENT); // if using Group, a white border will
		// remain
		scene.setCursor(Cursor.HAND);

		// Makes the menu option box fades in
		enteredHandler = new EventHandler<MouseEvent>(){
	        public void handle(MouseEvent event){
	        	menuApp.addLineMenuBox();
				menuApp.startAnimation(menuApp.getMenuBox());
				menuApp.startBoxAnimation();
	        }
		};

		scene.setOnMouseEntered(enteredHandler);

		// Makes the menu option box fades out
		exitedHandler = new EventHandler<MouseEvent>(){
	        public void handle(MouseEvent event){
				menuApp.endLineAnimation();
				menuApp.endMenuBoxAnimation();
	        }
		};

		scene.setOnMouseExited(exitedHandler);

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

//       // starting initial anchor point
//       scene.setOnMousePressed(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event){
//                anchorPt = new Point2D(event.getScreenX(), event.getScreenY());
//            }
//        });
//        
//        // dragging the entire stage
//        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event){
//                if (anchorPt != null && previousLocation != null) {
//                    primaryStage.setX(previousLocation.getX() + event.getScreenX() - anchorPt.getX());
//                    primaryStage.setY(previousLocation.getY() + event.getScreenY() - anchorPt.getY());                    
//                }
//            }
//        });
//        
//        // set the current location
//        scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event){
//                previousLocation = new Point2D(primaryStage.getX(), primaryStage.getY());
//            }
//        });
//
//       
//        previousLocation = new Point2D(primaryStage.getX(), primaryStage.getY()); 

		// Add keyboard control
		menuApp.getSpinningGlobe().getGlobe().handleKeyboard(scene.getRoot());
		// Add mouse control
		menuApp.getSpinningGlobe().getGlobe().handleMouse(scene.getRoot());

		stage.setOnCloseRequest(e -> {
			dialogOnExit(globeSPane);

			if (!isExit) {
				e.consume();
			} else {
				Platform.exit();
				System.exit(0);
			}

		});

		stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
		// NOTE: OR use svg file with stage.getIcons().add(new
		// Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
		stage.setScene(scene);

		setMonitor(stage);

		stage.centerOnScreen();
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.show();

	}


	public void removeEvenHandler() {
		if (isFXGL ) {
			root.removeEventHandler(MouseEvent.MOUSE_ENTERED, enteredHandler);
			root.removeEventHandler(MouseEvent.MOUSE_EXITED, exitedHandler);		
			root.setOnMouseEntered(null); 
			root.setOnMouseExited(null); 
		}
		else {
			scene.removeEventHandler(MouseEvent.MOUSE_ENTERED, enteredHandler);
			scene.removeEventHandler(MouseEvent.MOUSE_EXITED, exitedHandler);		
			scene.setOnMouseEntered(null); 
			scene.setOnMouseExited(null);	
		}

//		// Makes the menu option box fades in
//		enteredHandler = new EventHandler<MouseEvent>(){
//	        public void handle(MouseEvent event){
//				menuApp.startAnimation();
//				FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
//				fadeTransition.setFromValue(0.0);
//				fadeTransition.setToValue(1.0);
//				fadeTransition.play();
//	        }
//		};
//
//		scene.setOnMouseEntered(enteredHandler);
//
//		// Makes the menu option box fades out
//		exitedHandler = new EventHandler<MouseEvent>(){
//	        public void handle(MouseEvent event){
//				menuApp.endAnimation();
//				FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
//				fadeTransition.setFromValue(1.0);
//				fadeTransition.setToValue(0.0);
//				fadeTransition.play();
//				fadeTransition.setOnFinished(e -> menuApp.clearMenuBoxItems());
//	        }
//		};
//		
//		scene.setOnMouseExited(exitedHandler);
	}

	/**
	 * Removes unsupported resolutions
	 * 
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
		// resList = new ArrayList<>();
		resList = Arrays.asList(new Resolution(1024, 768), new Resolution(1280, 720), new Resolution(1280, 800),
				new Resolution(1366, 768), new Resolution(1440, 900), new Resolution(1600, 900),
				new Resolution(1920, 1080), new Resolution(2560, 1440), new Resolution(2560, 1600));

		if (native_width > 2560) {
			; // resList to stay the same
		}

		else if (native_width == 2560) {
			if (native_height == 1600) {
				; // resList to stay the same
			} else if (native_height == 1440) {
				removeRes(7);
			}
		} else if (native_width == 1920)
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
			} else if (native_height == 720) {
				removeRes(1);
			}
		} else if (native_width == 1024)
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

	public void runNew(boolean isFXGL, boolean isCommanderMode) {
		closeStage(isFXGL);
		createMainScene(isCommanderMode);
	}

	public void createMainScene(boolean isCommanderMode) {
		// creates a mainScene instance
		mainScene = new MainScene(native_width, native_height, gameScene);

		try {
			// Loads Scenario Config Editor
			Simulation.instance().getSimExecutor().execute(new ConfigEditorTask(isCommanderMode));

		} catch (Exception e) {
			e.printStackTrace();
			exitWithError("Error : could not create a new simulation ", e);
		}

	}

	public class ConfigEditorTask implements Runnable {
		private boolean isCommanderMode;
		
		ConfigEditorTask(boolean isCommanderMode) {
			this.isCommanderMode = isCommanderMode;
		}
		
		public void run() {
			// logger.config("MarsProjectFX's ConfigEditorTask's run() is on " +
			// Thread.currentThread().getName() );
			new ScenarioConfigEditorFX(mainMenu, isCommanderMode); // marsProjectFX,
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
			// ((Stage) gameScene.getRoot().getScene().getWindow()).close();
		} else {
			primaryStage.setIconified(true);
			primaryStage.hide();
			primaryStage.close();
		}
	}

	/**
	 * Loads the setting dialog
	 */
	public void runSettings() {
		selectResolutionDialog(globeSPane);
	}

	/*
	 * Loads the simulation file via the terminal or FileChooser.
	 * 
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
				FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter("Simulation files (*.sim)",
						"*.sim");
				FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("all files (*.*)", "*.*");

				chooser.getExtensionFilters().addAll(simFilter, allFilter);

				// Show open file dialog
				if (isFXGL)
					selectedFile = chooser.showOpenDialog(gameScene.getRoot().getScene().getWindow());
				else
					selectedFile = chooser.showOpenDialog(primaryStage);
			}

			// else {
			// if user wants to load the default saved sim
			// }

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

				// if (isSoundDisabled)
				// MainScene.setSoundDisabled();

				mainScene.createLoadingIndicator();
//				mainScene.showWaitStage(MainScene.LOADING);
			});

			CompletableFuture.supplyAsync(() -> submitTask(fileLocn, false));

		}

		else {
			logger.config("No file was selected. Loading is cancelled");
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
	 * 
	 * @param fileLocn user's sim filename
	 */
	public class LoadSimulationTask implements Runnable {
		File fileLocn;
		boolean autosaveDefaultName;

		LoadSimulationTask(File fileLocn, boolean autosaveDefaultName) {
			this.fileLocn = fileLocn;
			this.autosaveDefaultName = autosaveDefaultName;
		}

		public void run() {
			// logger.config("LoadSimulationTask is on " + Thread.currentThread().getName() +
			// " Thread");

			// Initialize the simulation.
			Simulation.createNewSimulation(-1);

			try {
				// Loading settlement data from the default saved simulation
				sim.loadSimulation(fileLocn); // null means loading "default.sim"

			} catch (Exception e) {
				// e.printStackTrace();
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

			// Initialize interactive terminal and load menu
			sim.getTerm().initializeTerminal();
			sim.getTerm().loadTerminalMenu();
		}
	}

	public void runMultiplayer() {
		// logger.config("MainMenu's runThree() is on " + Thread.currentThread().getName()
		// + " Thread");
		Simulation.instance().getSimExecutor().submit(new MultiplayerTask());
		primaryStage.setIconified(true);// hide();
		/*
		 * try { multiplayerMode = new MultiplayerMode(this); } catch (IOException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); } executor =
		 * (ThreadPoolExecutor) Executors.newFixedThreadPool(1); //
		 * newCachedThreadPool(); executor.execute(multiplayerMode.getModeTask());
		 */
		// pool.shutdown();
		// mainSceneStage.toFront();
		// modtoolScene = new SettlementScene().createSettlementScene();
		// stage.setScene(modtoolScene);
		// stage.show();
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

	// public void changeScene(int toscene) {
	// switch (toscene) {
	// case 1:
	// scene = new MenuScene().createScene();
	// break;
	// case 2:
	// Scene scene = mainScene.createMainScene();
	// break;
	// case 3:
	// scene = modtoolScene.createScene(stage);
	// break;
	// stage.setScene(scene);
	// }
	// }

	/*
	 * Sets the main menu controller at the start of the Simulation.instance().
	 */
	public void setController(ControlledScreen myScreenController) {
		if (mainMenuController == null)
			if (myScreenController instanceof MainMenuController) {
				mainMenuController = (MainMenuController) myScreenController;
				mainMenuController.setBuild();
			}
	}

//	public void chooseScreen(int num) {
//		ObservableList<Screen> screens = Screen.getScreens();//.getScreensForRectangle(xPos, yPos, 1, 1);
//    	Screen currentScreen = screens.get(num);
//		Rectangle2D rect = currentScreen.getVisualBounds();
//		Screen primaryScreen = Screen.getPrimary();
//	}

	public void setMonitor(Stage stage) {
		// Issue: how do we tweak mars-sim to run on the "active" monitor as chosen by
		// user ?
		// "active monitor is defined by whichever computer screen the mouse pointer is
		// or where the command console that starts mars-sim.
		// by default MSP runs on the primary monitor (aka monitor 0 as reported by
		// windows os) only.
		// see
		// http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762

//		AnchorPane anchor = null;
//		if (root == null) {
//	       anchor = new AnchorPane();
//	       anchor.setPrefWidth(WIDTH);
//	       anchor.setPrefHeight(HEIGHT);
//		}

		StartUpLocation startUpLoc = new StartUpLocation(WIDTH, HEIGHT);
		double xPos = startUpLoc.getXPos();
		double yPos = startUpLoc.getYPos();
		// Set Only if X and Y are not zero and were computed correctly
		// ObservableList<Screen> screens = Screen.getScreensForRectangle(xPos, yPos, 1,
		// 1);
		// ObservableList<Screen> screens = Screen.getScreens();
		// System.out.println("# of monitors : " + screens.size());

		if (xPos != 0 && yPos != 0) {
			stage.setX(xPos);
			stage.setY(yPos);
			// System.out.println("Monitor 2: x : " + xPos + " y : " + yPos);
		} else {
			// System.out.println("calling centerOnScreen()");
			// System.out.println("Monitor 1: x : " + xPos + " y : " + yPos);
		}
		stage.centerOnScreen();
	}

	// public ScreensSwitcher getScreensSwitcher() {
	// return screen;
	// }

	public MainMenuController getMainMenuController() {
		return mainMenuController;
	}

	public SpinningGlobe getSpinningGlobe() {
		return menuApp.getSpinningGlobe();
	}

	/**
	 * Exit the simulation with an error message.
	 * 
	 * @param message the error message.
	 * @param e       the thrown exception or null if none.
	 */
	private void exitWithError(String message, Exception e) {
		showError(message, e);
		Platform.exit();
		System.exit(1);
	}

	/**
	 * Show a modal error message dialog.
	 * 
	 * @param message the error message.
	 * @param e       the thrown exception or null if none.
	 */
	private void showError(String message, Exception e) {
		if (e != null) {
			logger.log(Level.SEVERE, message, e);
		} else {
			logger.log(Level.SEVERE, message);
		}
	}

	public StackPane getPane() {
		return globeSPane;
	}

	public static StackPane getExitDialogPane(JFXButton b1, JFXButton b2, JFXButton b3) {
		Label l = new Label("Do you really want to exit ?");// mainScene.createBlendLabel(Msg.getString("MainScene.exit.header")); // 
		l.setPadding(new Insets(10, 10, 10, 10));
		l.setFont(Font.font(null, FontWeight.BOLD, 14));
		l.setStyle("-fx-text-fill: white;");
		
		b1.setStyle("-fx-background-color: grey;-fx-text-fill: white;");
		b2.setStyle("-fx-background-color: grey;-fx-text-fill: white;");
		if (b3 != null)
			b3.setStyle("-fx-background-color: grey;-fx-text-fill: white;");
		
		HBox hb = new HBox();
		hb.setAlignment(Pos.CENTER);
		if (b3 != null)
			hb.getChildren().addAll(b1, b2, b3);
		else
			hb.getChildren().addAll(b1, b2);
		
		HBox.setMargin(b1, new Insets(3, 3, 3, 3));
		HBox.setMargin(b2, new Insets(3, 3, 3, 3));
		if (b3 != null)
			HBox.setMargin(b3, new Insets(3, 3, 3, 3));
		
		VBox vb = new VBox();
		vb.setAlignment(Pos.CENTER);
		vb.setPadding(new Insets(5, 5, 5, 5));
		vb.getChildren().addAll(l, hb);

		StackPane sp = new StackPane(vb);
		sp.setStyle("-fx-background-color: black;");
		StackPane.setMargin(vb, new Insets(10, 10, 10, 10));
		return sp;
	}

	
	/**
	 * Open the exit dialog box
	 * 
	 * @param pane
	 */
	public static void dialogOnExit(StackPane pane) {

		if (exitDialog == null && (settingDialog == null || (settingDialog != null && !settingDialog.isVisible()))) {

			JFXButton b1 = new JFXButton("Exit");
			JFXButton b2 = new JFXButton("Back");	
			
			StackPane sp = getExitDialogPane(b1, b2, null);			

			exitDialog = new JFXDialog();
			// exitDialog.setDialogContainer(pane);
			exitDialog.setTransitionType(DialogTransition.TOP);
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

		} else if (exitDialog != null && !exitDialog.isVisible()
				&& (settingDialog == null || (settingDialog != null && !settingDialog.isVisible()))) {
			exitDialog.show(pane);
		}
	}

	
	/**
	 * Obtains the resolution of the current screen
	 */
	public Resolution obtainResolution() {
		Resolution r = null;
		if (mainscene_width == 2560) {
			if (mainscene_height == 1600) {
				r = resList.get(8);
			} else if (native_height == 1440) {
				r = resList.get(7);
			}
		} else if (mainscene_width == 1920)
			r = resList.get(6);
		else if (mainscene_width == 1600)
			r = resList.get(5);
		else if (mainscene_width == 1440)
			r = resList.get(4);
		else if (mainscene_width == 1366)
			r = resList.get(3);
		else if (mainscene_width == 1280) {
			if (mainscene_height == 800) {
				r = resList.get(2);
			} else if (mainscene_height == 720) {
				r = resList.get(1);
			}
		} else if (mainscene_width == 1024)
			r = resList.get(0);
		else
			// by default, set to 1024 x 768
			r = resList.get(0);

		return r;
	}

	/**
	 * Swaps the mouse cursor type between DEFAULT and HAND
	 *
	 * @param node
	 */
	public void setMouseCursor(Node node) {
		node.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
			node.setCursor(Cursor.DEFAULT);
		});

		node.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
			node.setCursor(Cursor.HAND);
		});
	}
	
	/**
	 * Checks if all of the commander's info are good to go
	 * 
	 * @return true or false
	 */
	public boolean areAllGoodToGo() {
		for (boolean ans : goodToGo) {
			if (!ans) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Selects the game screen resolution in this dialog box
	 * 
	 * @param pane
	 */
	public StackPane createCommanderPane() {
		if (exitDialog == null || (exitDialog != null && !exitDialog.isVisible())) {
			
			JFXButton doneBtn = new JFXButton("Commit");
			setMouseCursor(doneBtn);
			doneBtn.setGraphic(
					new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/round_play_32.png"))));
			doneBtn.getStyleClass().add("button-mid");
			doneBtn.setId("commitButton");
			doneBtn.setAlignment(Pos.CENTER);
			doneBtn.setDisable(true);
			doneBtn.setStyle("-fx-background-color: lightgoldenrodyellow;");// lightgrey;");
			doneBtn.setOnAction(e -> {
				if (areAllGoodToGo()) {
					UnitManager.setCommander(true);
					runNew(isFXGL, true);
				}
				e.consume();
			});

			HBox doneHB = new HBox();
			doneHB.setPadding(new Insets(20, 15, 15, 25));
			doneHB.getChildren().add(doneBtn);
			doneHB.setAlignment(Pos.CENTER);

//			HBox.setMargin(done_btn, new Insets(10, 10, 10, 10));
			
			Label titleLabel = new Label("Commander Mode");
			titleLabel.setAlignment(Pos.TOP_CENTER);
			titleLabel.setTextAlignment(TextAlignment.CENTER);
			titleLabel.setContentDisplay(ContentDisplay.TOP);
			titleLabel.setPadding(new Insets(10, 10, 10, 10));
			titleLabel.setStyle("-fx-text-fill: lightgoldenrodyellow;");
			titleLabel.setFont(Font.loadFont(
					MenuApp.class.getResource("/fonts/Penumbra-HalfSerif-Std_35114.ttf").toExternalForm(), 15));
			// titleLabel.setFont(Font.font(null, FontWeight.BOLD, 20));

			Commander commander = SimulationConfig.instance().getPersonConfiguration().getCommander();

			// First Name
			Label fnameLabel = new Label("First Name :   ");
			fnameLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");
			JFXTextField fnameTF = new JFXTextField();
			fnameTF.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");			
 			
			fnameTF.textProperty().addListener((observable, oldValue, newValue) -> {
			    String first = newValue;
				if (first != null 
						&& !first.trim().isEmpty()
						&& !Conversion.isBlank(first)) {
					commander.setFirstName(first);
					goodToGo[0] = true;
					if (areAllGoodToGo()) 
						doneBtn.setDisable(false);
				} 
				
				else {
					Alert alert = new Alert(AlertType.ERROR, "The commander's first name cannot be blank.");
					alert.initOwner(primaryStage);
					alert.setTitle("Invalid Input");
					alert.showAndWait();
					goodToGo[0] = false;
					doneBtn.setDisable(true);
					fnameTF.requestFocus();
				}
			});
					
			HBox fnameBox = new HBox();
			fnameBox.setPadding(new Insets(10, 10, 10, 10));
			fnameBox.getChildren().addAll(fnameLabel, fnameTF);
			fnameBox.setAlignment(Pos.CENTER);

			// Last Name
			Label lnameLabel = new Label("Last Name :   ");
			JFXTextField lnameTF = new JFXTextField();
			lnameTF.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");
		
			lnameTF.textProperty().addListener((observable, oldValue, newValue) -> {
			    String last = newValue;
				if (last != null 
						&& !last.trim().isEmpty()
						&& !Conversion.isBlank(last)) {
					commander.setLastName(last);
					goodToGo[1] = true;
					if (areAllGoodToGo()) 
						doneBtn.setDisable(false);
				} 
				
				else {
					Alert alert = new Alert(AlertType.ERROR, "The commander's last name cannot be blank.");
					alert.initOwner(primaryStage);
					alert.setTitle("Invalid Input");
					alert.showAndWait();
					goodToGo[1] = false;
					doneBtn.setDisable(true);
					lnameTF.requestFocus();
				}
			});

			HBox lnameBox = new HBox();
			lnameBox.setPadding(new Insets(10, 10, 10, 10));
			lnameLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");
			lnameBox.getChildren().addAll(lnameLabel, lnameTF);
			lnameBox.setAlignment(Pos.CENTER);
		
			// Gender
			Label genderLabel = new Label("Gender :   ");
			genderLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");

        	List<String> genders = Arrays.asList("Male", "Female");

			JFXComboBox<String> genderCombo = new JFXComboBox<>();
			genderCombo.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");
			genderCombo.getStyleClass().add("jfx-combo-box");
			genderCombo.getItems().addAll(genders);	
			genderCombo.setPromptText("Select here");
			genderCombo.valueProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue ov, String t, String t1) {
					if (t1.equals("") || t1 == null) {
						doneBtn.setDisable(true);
						goodToGo[2] = false;
						genderCombo.requestFocus();
					}
					else {
//					if (!t.equals(t1)) {
						genderCombo.setValue(t1);
						commander.setGender(t1);
						goodToGo[2] = true;
						if (areAllGoodToGo()) 
							doneBtn.setDisable(false);
					}
				}
			});

			HBox genderBox = new HBox();
			genderBox.setPadding(new Insets(10, 10, 10, 10));
			genderBox.getChildren().addAll(genderLabel, genderCombo);
			genderBox.setAlignment(Pos.CENTER);

			// Age
			Label ageLabel = new Label("Age :   ");
			JFXTextField ageTF = new JFXTextField();
			ageTF.setPrefColumnCount(5);
			ageTF.setText("30");
			commander.setAge(30);
			goodToGo[3] = true;
			ageTF.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");

			ageTF.textProperty().addListener((observable, oldValue, newValue) -> {
				String ageString = newValue;
				if (ageString != null 
						&& !ageString.trim().isEmpty()
						&& !Conversion.isBlank(ageString) 
						&& Conversion.isInteger(ageString, 10)) {
					int age = Integer.parseInt(ageString);
					if (age >= 18 && age <= 80) {
						commander.setAge(age);
						goodToGo[3] = true;
						if (areAllGoodToGo()) 
							doneBtn.setDisable(false);
					}
					else {
						Alert alert = new Alert(AlertType.ERROR, "The commander's age must be between 18 and 80.");
						alert.initOwner(primaryStage);
						alert.setTitle("Invalid Input");
						alert.showAndWait();
						goodToGo[3] = false;
						doneBtn.setDisable(true);
						ageTF.requestFocus();
					}
				} 
				
				else {
					Alert alert = new Alert(AlertType.ERROR, "The commander's age cannot be blank.");
					alert.initOwner(primaryStage);
					alert.setTitle("Invalid Input");
					alert.showAndWait();
					goodToGo[3] = false;
					doneBtn.setDisable(true);
					ageTF.requestFocus();
				}
			});

			HBox ageBox = new HBox();
			ageBox.setPadding(new Insets(10, 10, 10, 10));
			ageLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");
			ageBox.getChildren().addAll(ageLabel, ageTF);
			ageBox.setAlignment(Pos.CENTER);

			// Job
			Label jobLabel = new Label("Job :   ");
			jobLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");

        	List<String> jobs = JobType.getEditedList();

			JFXComboBox<String> jobCombo = new JFXComboBox<>();
			jobCombo.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");
			jobCombo.getStyleClass().add("jfx-combo-box");
			jobCombo.getItems().addAll(jobs);	
			jobCombo.setPromptText("Select here");
			jobCombo.valueProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue ov, String t, String t1) {
					if (t1.equals("") || t1 == null) {
						doneBtn.setDisable(true);
						goodToGo[4] = false;
						jobCombo.requestFocus();
					}
					else {
//					if (!t.equals(t1)) {
						jobCombo.setValue(t1);
						int id = jobs.indexOf(t1);
						commander.setJob(id + 1);
						goodToGo[4] = true;
						if (areAllGoodToGo()) 
							doneBtn.setDisable(false);
					}
				}
			});

			HBox jobBox = new HBox();
			jobBox.setPadding(new Insets(10, 10, 10, 10));
			jobBox.getChildren().addAll(jobLabel, jobCombo);
			jobBox.setAlignment(Pos.CENTER);

			// Country
			Label countryLabel = new Label("Country :   ");
			countryLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");

        	List<String> countries = UnitManager.getCountryList();

			JFXComboBox<String> countryCombo = new JFXComboBox<>();
			countryCombo.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");
			countryCombo.getStyleClass().add("jfx-combo-box");
			countryCombo.getItems().addAll(countries);	
			countryCombo.setPromptText("Select here");
			countryCombo.valueProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue ov, String t, String t1) {
					if (t1.equals("") || t1 == null) {
						doneBtn.setDisable(true);
						goodToGo[5] = false;
						countryCombo.requestFocus();
					}
					else {
//					if (!t.equals(t1)) {
						countryCombo.setValue(t1);
						int id = countries.indexOf(t1);
						commander.setCountry(id + 1);
						goodToGo[5] = true;
						if (areAllGoodToGo()) 
							doneBtn.setDisable(false);
					}
				}
			});

			HBox countryBox = new HBox();
			countryBox.setPadding(new Insets(10, 10, 10, 10));
			countryBox.getChildren().addAll(countryLabel, countryCombo);
			countryBox.setAlignment(Pos.CENTER);

			VBox vb = new VBox();
			vb.setAlignment(Pos.CENTER);
			vb.setPadding(new Insets(10, 10, 10, 10));
			vb.getChildren().addAll(titleLabel, fnameBox, lnameBox, genderBox, ageBox, jobBox, countryBox, doneHB);
			
			StackPane sp = new StackPane(vb);
			//sp.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgoldenrodyellow;");
			StackPane.setMargin(vb, new Insets(10, 10, 10, 10));

			return sp;
		}
		
		return null;	
	}
	
	public int getListID(List<String> list, String s) {
    	int id = -1;
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i).equals(s)) {
    			id = i;
    			break;
    		}
    	}
    	return id;
	}
	
	/**
	 * Selects the game screen resolution in this dialog box
	 * 
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
			titleLabel.setFont(Font.loadFont(
					MenuApp.class.getResource("/fonts/Penumbra-HalfSerif-Std_35114.ttf").toExternalForm(), 20));
			// titleLabel.setFont(Font.font(null, FontWeight.BOLD, 20));

			JFXButton done_btn = new JFXButton("Done");
			done_btn.setStyle("-fx-background-color: lightgoldenrodyellow;");// lightgrey;");

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

			// System.out.println(mainscene_width + " x " + mainscene_height);

			resCombo.setPromptText("Select desired screen resolution");

			resCombo.valueProperty().addListener(new ChangeListener<Resolution>() {
				@Override
				public void changed(ObservableValue ov, Resolution t, Resolution t1) {
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

			// Horizontal separator
			// Separator separator = new Separator();
			// separator.setMaxWidth(160);
			// separator.setHalignment(HPos.LEFT);

			VBox emptyVB = new VBox();
			// vb.setAlignment(Pos.CENTER);
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
			StackPane.setMargin(vb, new Insets(10, 10, 10, 10));
			settingDialog = new JFXDialog();
			// dialog.setDialogContainer(pane);
			settingDialog.setContent(sp);
			settingDialog.setTransitionType(DialogTransition.BOTTOM);
			settingDialog.show(pane);

			done_btn.setOnAction(e -> {
				// pane.setPrefWidth(stackPane.getPrefWidth()-300);
				settingDialog.close();
				e.consume();
			});

		} else if (settingDialog != null && !settingDialog.isVisible()
				&& (exitDialog == null || (exitDialog != null && !exitDialog.isVisible()))) {
			settingDialog.show(pane);
		}
	}

	public Tile musicSetting() {
		Tile switchSliderTile = TileBuilder.create().skinType(SkinType.SWITCH_SLIDER).prefSize(450, 150)
				.backgroundColor(Color.BLACK)// .rgb(105,105,105))//.DARKGRAY)//.rgb(255, 255, 255, 0.1))
				// .title(title)
				.description("Background Music Volume").descriptionAlignment(Pos.TOP_CENTER).textVisible(true)
				.decimals(0).minValue(0).maxValue(100).value(music_v).build();

		switchSliderTile.setActive(true);

		switchSliderTile.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {

				if (old_val != new_val) {
					float vol = new_val.floatValue();

					if (vol <= 5) {
						music_v = 0;
						// System.out.println("Background Music is mute.");
						switchSliderTile.setValue(0);
						switchSliderTile.setActive(false);
					} else {
						music_v = ((int) (vol * 5f)) / 5;
						switchSliderTile.setActive(true);
					}

					if (!switchSliderTile.isActive()) {
						switchSliderTile.setValue(0);
						// switchSliderTile.setActive(false);
					}
				}

			}
		});

		switchSliderTile.setOnSwitchPressed(e -> {
			if (switchSliderTile.getCurrentValue() < 5 || !switchSliderTile.isActive()) {
				switchSliderTile.setValue(0);
				switchSliderTile.setActive(false);
			} else {
				switchSliderTile.setActive(true);
				if (switchSliderTile.getCurrentValue() < 5)
					switchSliderTile.setValue(80);
			}

		});

		switchSliderTile.setOnSwitchReleased(e -> {
			if (switchSliderTile.getCurrentValue() < 5 || !switchSliderTile.isActive()) {
				switchSliderTile.setValue(0);
				switchSliderTile.setActive(false);
			} else {
				switchSliderTile.setActive(true);
				if (switchSliderTile.getCurrentValue() < 5)
					switchSliderTile.setValue(80);
			}
		});

		return switchSliderTile;
	}

	public Tile soundEffectSetting() {

		Tile switchSliderTile = TileBuilder.create().skinType(SkinType.SWITCH_SLIDER).prefSize(450, 150)
				.backgroundColor(Color.BLACK)// .rgb(105,105,105))//.DARKGRAY)//.rgb(255, 255, 255, 0.1))
				// .title(title)
				.description("Sound Effect Volume").descriptionAlignment(Pos.TOP_CENTER).textVisible(true).decimals(0)
				.minValue(0).maxValue(100).value(sound_effect_v).build();

		switchSliderTile.setActive(true);

		switchSliderTile.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {

				if (old_val != new_val) {
					float vol = new_val.floatValue();

					if (vol <= 5) {
						sound_effect_v = 0f;
						switchSliderTile.setValue(0);
						// System.out.println("Sound Effect is mute.");
						switchSliderTile.setActive(false);
					} else {
						sound_effect_v = ((int) (vol * 5f)) / 5;
						switchSliderTile.setActive(true);

					}

					if (!switchSliderTile.isActive()) {
						switchSliderTile.setValue(0);
						// switchSliderTile.setActive(false);
					}

				}
			}
		});

		switchSliderTile.setOnSwitchPressed(e -> {

			if (switchSliderTile.getCurrentValue() < 5 || !switchSliderTile.isActive()) {
				switchSliderTile.setValue(0);
				switchSliderTile.setActive(false);
			} else {
				switchSliderTile.setActive(true);
				if (switchSliderTile.getCurrentValue() < 5)
					switchSliderTile.setValue(80);
			}

		});

		switchSliderTile.setOnSwitchReleased(e -> {
			if (switchSliderTile.getCurrentValue() < 5 || !switchSliderTile.isActive()) {
				switchSliderTile.setValue(0);
				switchSliderTile.setActive(false);
			} else {
				switchSliderTile.setActive(true);
				if (switchSliderTile.getCurrentValue() < 5)
					switchSliderTile.setValue(80);
			}
		});

		return switchSliderTile;
	}

	// public void setScreenSize(int w, int h) {
	// mainscene_width = w;
	// mainscene_height = h;
	// }

	public static boolean isSoundDisabled() {
		return isSoundDisabled;
	}

	public static void disableSound() {
		isSoundDisabled = true;
	}

	public void destroy() {
		menuAPane = null;
		primaryStage = null;
		mainMenu = null;
		mainScene = null;
		executor = null;
		multiplayerMode = null;
		mainMenuController = null;
		menuApp = null;
		resList = null;
		nodes = null;
		root = null;
		mainMenuSPane = null;
		globeSPane = null;
		gameScene = null;
		settingDialog = null;
		exitDialog = null;
		starsTimer.stop();
		starsTimer = null;
	}

	private class Resolution {

		int width;
		int height;

		Resolution(int width, int height) {
			this.width = width;
			this.height = height;
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
