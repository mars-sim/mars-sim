/**
 * Mars Simulation Project
 * MainMenu.java
 * @version 3.08 2015-04-09
 * @author Manny Kung
 */

package org.mars_sim.msp.javafx;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

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
import javafx.util.Duration;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.networking.MultiplayerMode;
import org.mars_sim.msp.ui.javafx.MainScene;

public class MainMenu {

    /** default logger. */
    private static Logger logger = Logger.getLogger(MainMenu.class.getName());

	// Data members
    private double fileSize;

    public static String screen1ID = "main";
    public static String screen1File = "/fxui/fxml/MainMenu.fxml";
    public static String screen2ID = "configuration";
    public static String screen2File = "/fxui/fxml/Configuration.fxml";
    public static String screen3ID = "credits";
    public static String screen3File = "/fxui/fxml/Credits.fxml";

    //public String[] args;

    //private double anchorX;
    //private double anchorY;
    //private double anchorAngle;
    //private boolean cleanUI = true;

    private final DoubleProperty sunDistance = new SimpleDoubleProperty(100);
    private final BooleanProperty sunLight = new SimpleBooleanProperty(true);
    private final BooleanProperty diffuseMap = new SimpleBooleanProperty(true);
    private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
    private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
    //private final BooleanProperty selfIlluminationMap = new SimpleBooleanProperty(true);

    private Sphere mars;
    private PhongMaterial material;
    private PointLight sun;

	private Stage primaryStage;
	private Stage stage;
	public Scene scene;
	public MainMenu mainMenu;
	public MainScene mainScene;
	//public ModtoolScene modtoolScene;
	private MarsProjectFX marsProjectFX;
	private transient ThreadPoolExecutor executor;
	private MultiplayerMode multiplayerMode;

    public MainMenu(MarsProjectFX marsProjectFX) {
    	this.marsProjectFX = marsProjectFX;
    	mainMenu = this;
    	logger.info("MainMenu's constructor is on " + Thread.currentThread().getName() + " Thread");
	}

    public boolean exitDialog(Stage stage) {
    	Alert alert = new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("Mars Simulation Project");
    	alert.setHeaderText("Confirmation Dialog");
    	//alert.initModality(Modality.APPLICATION_MODAL);
		alert.initOwner(stage);
    	alert.setContentText("Do you really want to quit MSP?");
    	ButtonType buttonTypeYes = new ButtonType("Yes");
    	ButtonType buttonTypeNo = new ButtonType("No");
    	alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
    	Optional<ButtonType> result = alert.showAndWait();
    	if (result.get() == buttonTypeYes){
    		if (multiplayerMode != null)
    			if (multiplayerMode.getChoiceDialog() != null)
    				multiplayerMode.getChoiceDialog().close();
    		alert.close();
    		return true;
    	} else {
    		alert.close();
    	    return false;
    	}
    }

   void initAndShowGUI(Stage primaryStage) {
		logger.info("MainMenu's initAndShowGUI() is on " + Thread.currentThread().getName() + " Thread");

	   this.primaryStage = primaryStage;

    	Platform.setImplicitExit(false);

    	primaryStage.setOnCloseRequest(e -> {
			boolean isExit = exitDialog(primaryStage);
			if (!isExit) {
				e.consume();
			}
			else {
				Platform.exit();
			}
		});

       ScreensSwitcher switcher = new ScreensSwitcher(this);
       switcher.loadScreen(MainMenu.screen1ID, MainMenu.screen1File);
       switcher.loadScreen(MainMenu.screen2ID, MainMenu.screen2File);
       switcher.loadScreen(MainMenu.screen3ID, MainMenu.screen3File);
       switcher.setScreen(MainMenu.screen1ID);

       Group root = new Group();
       Parent parent = createMarsGlobe();
       root.getChildren().addAll(parent, switcher);
       Scene scene = new Scene(root);
       scene.getStylesheets().add( this.getClass().getResource("/fxui/css/mainmenu.css").toExternalForm() );

/*
       scene.setOnMousePressed((event) -> {
	        anchorX = event.getSceneX();
	        //anchorY = event.getSceneY();
            anchorAngle = parent.getRotate();
       });
       scene.setOnMouseDragged((event) -> {
	   parent.setRotate(anchorAngle + anchorX -  event.getSceneX());
	      	parent.setRotate(event.getSceneX());
       });
*/
       //scene.setFill(Color.rgb(10, 10, 40));
       scene.setFill(Color.BLACK);
       scene.setCursor(Cursor.HAND);
       //primaryStage.initStyle(StageStyle.UNDECORATED);
       //primaryStage.initStyle (StageStyle.UTILITY);
       //primaryStage.getStyleClass().add("rootPane");
       primaryStage.centerOnScreen();
       primaryStage.setResizable(false);
	   primaryStage.setTitle(Simulation.WINDOW_TITLE);
       primaryStage.setScene(scene);
       //primaryStage.getIcons().add(new javafx.scene.image.Image(this.getClass().getResource("/icons/lander_hab32.png").toString()));
       primaryStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       primaryStage.show();

       // Starts a new stage for MainScene
	   stage = new Stage();
	   stage.setTitle(Simulation.WINDOW_TITLE);
	   //menuScene = new MenuScene(stage);
	   //modtoolScene = new ModtoolScene(stage);
   }

   public Stage getStage() {
	   return primaryStage;
   }

   public MultiplayerMode getMultiplayerMode() {
	   return multiplayerMode;
	}

   public void prepareStage() {
	   logger.info("MainMenu's prepareStage() is on " + Thread.currentThread().getName() + " Thread");

	   // prepare main scene
	   mainScene.prepareMainScene();
	   Scene scene = mainScene.initializeScene();
	   mainScene.prepareOthers();

	   // prepare stage
	   stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       //stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab32.png").toString()));
	   stage.setResizable(true);
	   //stage.setFullScreen(true);
	   stage.setScene(scene);
	   stage.show();

	   mainScene.getMarsNode().createSettlementWindow();
	   mainScene.getMarsNode().createJMEWindow(stage);

  }

   public void runOne() {
	   logger.info("MainMenu's runOne() is on " + Thread.currentThread().getName() + " Thread");

	   primaryStage.setIconified(true);

	   mainScene = new MainScene(stage);

	   marsProjectFX.handleNewSimulation();
   }

   public void runTwo() {
	   logger.info("MainMenu's runTwo() is on " + Thread.currentThread().getName() + " Thread");
	   primaryStage.setIconified(true);

	   try {
		   mainScene = new MainScene(stage);
		   Simulation.instance().getSimExecutor().submit(new LoadSimulationTask());
		   TimeUnit.SECONDS.sleep(1L);
		   // The delay time for launching the JavaFX UI is based on the size of the default.sim
		   long delay_time = (long) (fileSize * 4000L);
		   TimeUnit.MILLISECONDS.sleep(delay_time);
		   prepareStage();
		} catch (Exception e) {
			e.printStackTrace();
		}

   }

	public class LoadSimulationTask implements Runnable {
		public void run() {
			Simulation.instance().loadSimulation(null); // null means loading "default.sim"
			Simulation.instance().start();
			fileSize = Simulation.instance().getFileSize();
			//System.out.println("filesize is "+ fileSize);
		}
	}

   public void runThree() {
	   logger.info("MainMenu's runThree() is on " + Thread.currentThread().getName() + " Thread");
	   Simulation.instance().getSimExecutor().submit(new MultiplayerTask());
	   //primaryStage.setIconified(true);
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
	     //primaryStage.toFront();
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

   public void changeScene(int toscene) {
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
	   		stage.setScene(scene);
	   //}
   }

   //public Scene getMainScene() {
   //	return mainScene;
   //}

   public Parent createMarsGlobe() {

	   logger.info("Is ConditionalFeature.SCENE3D supported on this platform? " + Platform.isSupported(ConditionalFeature.SCENE3D));

	   Image sImage = new Image(this.getClass().getResource("/maps/rgbmars-spec-2k.jpg").toExternalForm());
       Image dImage = new Image(this.getClass().getResource("/maps/MarsV3-Shaded-2k.jpg").toExternalForm());
       Image nImage = new Image(this.getClass().getResource("/maps/MarsNormal2048x1024.png").toExternalForm()); //.toString());
       //Image siImage = new Image(this.getClass().getResource("/maps/rgbmars-names-2k.jpg").toExternalForm()); //.toString());

       material = new PhongMaterial();
       material.setDiffuseColor(Color.WHITE);
       material.diffuseMapProperty().bind(Bindings.when(diffuseMap).then(dImage).otherwise((Image) null));
       material.setSpecularColor(Color.TRANSPARENT);
       material.specularMapProperty().bind(Bindings.when(specularMap).then(sImage).otherwise((Image) null));
       material.bumpMapProperty().bind(Bindings.when(bumpMap).then(nImage).otherwise((Image) null));
       //material.selfIlluminationMapProperty().bind(Bindings.when(selfIlluminationMap).then(siImage).otherwise((Image) null));

       mars = new Sphere(4);
       mars.setMaterial(material);
       mars.setRotationAxis(Rotate.Y_AXIS);

       // Create and position camera
       PerspectiveCamera camera = new PerspectiveCamera(true);
       camera.getTransforms().addAll(
               new Rotate(-10, Rotate.Y_AXIS),
               new Rotate(-10, Rotate.X_AXIS),
               new Translate(0, 0, -10));

       sun = new PointLight(Color.rgb(255, 243, 234));
       sun.translateXProperty().bind(sunDistance.multiply(-0.82));
       sun.translateYProperty().bind(sunDistance.multiply(-0.41));
       sun.translateZProperty().bind(sunDistance.multiply(-0.41));
       sun.lightOnProperty().bind(sunLight);

       AmbientLight ambient = new AmbientLight(Color.rgb(1, 1, 1));

       // Build the Scene Graph
       Group globeComponents = new Group();
       globeComponents.getChildren().add(camera);
       globeComponents.getChildren().add(mars);
       globeComponents.getChildren().add(sun);
       globeComponents.getChildren().add(ambient);

       RotateTransition rt = new RotateTransition(Duration.seconds(60), mars);
       //rt.setByAngle(360);
       rt.setInterpolator(Interpolator.LINEAR);
       rt.setCycleCount(Animation.INDEFINITE);
       rt.setAxis(Rotate.Y_AXIS);
       rt.setFromAngle(360);
       rt.setToAngle(0);
       //rt.setCycleCount(RotateTransition.INDEFINITE);
       rt.play();

       // Use a SubScene
       SubScene subScene = new SubScene(globeComponents, 640, 640, true, SceneAntialiasing.BALANCED);
       subScene.setFill(Color.TRANSPARENT);
       subScene.setCamera(camera);

       return new Group(subScene);
   }


}
