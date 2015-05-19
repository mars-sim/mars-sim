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
import java.util.logging.Logger;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
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

	public MainScene mainScene;
	//public ModtoolScene modtoolScene;
	private MarsProjectFX mpFX;

	private transient ThreadPoolExecutor executor;

	private MultiplayerMode multiplayerMode;

    public MainMenu(MarsProjectFX mpFX) { //, String[] args, boolean cleanUI) {
    	//this.args = args;
    	this.mpFX = mpFX;
    	//System.out.println("MainMenu's constructor is on " + Thread.currentThread().getName() + " Thread");

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


    //public void start(Stage arg0) {
     // 	System.out.println("MainMenu's start() is on " + Thread.currentThread().getName() + " Thread");
//
    //	initAndShowGUI(primaryStage);
    //}

   void initAndShowGUI(Stage primaryStage) {
		//System.out.println("MainMenu's initAndShowGUI() is on " + Thread.currentThread().getName() + " Thread");

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

       //Parent globe = createMarsGlobe();
       //VBox vbox = ((VBox) switcher.lookup("#globe"));
       //vbox.getChildren().add(globe);
       //Scene scene = new Scene(switcher);

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

   public void runOne() {
	   //System.out.println("MainMenu's runOne() is on " + Thread.currentThread().getName() + " Thread");
	   primaryStage.setIconified(true);
	   mainScene = new MainScene(stage);
	   //primaryStage.hide();
	   //primaryStage.close();
	   mpFX.handleNewSimulation();
   }

   public void runMainScene() {
	   mainScene.prepareMainScene();
	   Platform.runLater(() -> {
		   final Scene scene = mainScene.initializeScene();
		   mainScene.prepareTransportWizard();
	       stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
	       //stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab32.png").toString()));
		   stage.setResizable(true);
		   //stage.setFullScreen(true);
		   stage.setScene(scene);
		   stage.show();
	   });
	   //System.out.println("done running runMainScene()");
   }

   public void runTwo() {

	   primaryStage.setIconified(true);

	   try {
		   mainScene = new MainScene(stage);
		   Simulation.instance().loadSimulation(null);
		   Simulation.instance().start();
		   runMainScene();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

/*      menuScene = new MenuScene().createMenuScene();
	    stage.setScene(menuScene);
        stage.show();
*/
   }

   public void runThree() {
	   //primaryStage.setIconified(true);
		try {
			multiplayerMode = new MultiplayerMode(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
		executor.execute(multiplayerMode.getModeTask());
	   //pool.shutdown();
	   //primaryStage.toFront();
   		//modtoolScene = new SettlementScene().createSettlementScene();
	    //stage.setScene(modtoolScene);
	    //stage.show();
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
