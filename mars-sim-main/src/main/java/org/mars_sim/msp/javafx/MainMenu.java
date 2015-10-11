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
import java.util.logging.Logger;

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
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.ProgressIndicator;
import org.pdfsam.ui.FillProgressIndicator;
import org.pdfsam.ui.RingProgressIndicator;


import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.javafx.controller.MainMenuController;
import org.mars_sim.msp.networking.MultiplayerMode;
import org.mars_sim.msp.ui.javafx.MainScene;

/*
 * The MainMenu class creates the Main Menu and the spinning Mars Globe for MSP
 */
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

    private double anchorX;
    private double rate;

    private boolean isDone;

    //private boolean cleanUI = true;

    private final DoubleProperty sunDistance = new SimpleDoubleProperty(100);
    private final BooleanProperty sunLight = new SimpleBooleanProperty(true);
    private final BooleanProperty diffuseMap = new SimpleBooleanProperty(true);
    private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
    private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
    //private final BooleanProperty selfIlluminationMap = new SimpleBooleanProperty(true);


    private RotateTransition rt;
    private Sphere mars;
    private PhongMaterial material;
    private PointLight sun;

    private Group root;
	private Stage mainSceneStage, mainMenuStage, circleStage;
	public Scene mainMenuScene;

	public MainMenu mainMenu;
	public MainScene mainScene;


	private MarsProjectFX marsProjectFX;
	private transient ThreadPoolExecutor executor;
	private MultiplayerMode multiplayerMode;

	public MainMenuController mainMenuController;

    public MainMenu(MarsProjectFX marsProjectFX) {
    	this.marsProjectFX = marsProjectFX;
    	mainMenu = this;
    	//logger.info("MainMenu's constructor is on " + Thread.currentThread().getName() + " Thread");
	}

    /*
     * Sets up and shows the MainMenu and prepare the stage for MainScene
     */
	void initAndShowGUI(Stage mainMenuStage) {
	   //Logger.info("MainMenu's initAndShowGUI() is on " + Thread.currentThread().getName() + " Thread");

	   this.mainMenuStage = mainMenuStage;

    	Platform.setImplicitExit(false);

    	mainMenuStage.setOnCloseRequest(e -> {
			boolean isExit = exitDialog(mainMenuStage);
			if (!isExit) {
				e.consume();
			}
			else {
				Platform.exit();
			}
		});

       ScreensSwitcher screen = new ScreensSwitcher(this);
       screen.loadScreen(MainMenu.screen1ID, MainMenu.screen1File);
       screen.loadScreen(MainMenu.screen2ID, MainMenu.screen2File);
       screen.loadScreen(MainMenu.screen3ID, MainMenu.screen3File);
       screen.setScreen(MainMenu.screen1ID);

       root = new Group();
       Parent parent = createMarsGlobe();
       root.getChildren().addAll(parent, screen);
       mainMenuScene = new Scene(root);
       mainMenuScene.getStylesheets().add(this.getClass().getResource("/fxui/css/mainmenu.css").toExternalForm() );


       // 2015-09-26 Added adjustRotation()
       adjustRotation(mainMenuScene, parent);

       //scene.setFill(Color.rgb(10, 10, 40));
       mainMenuScene.setFill(Color.BLACK);
       mainMenuScene.setCursor(Cursor.HAND);
       mainMenuScene.setFill(Color.TRANSPARENT); // needed to eliminate the white border

       mainMenuStage.initStyle (StageStyle.UTILITY); //or  (StageStyle.TRANSPARENT);;
       mainMenuStage.centerOnScreen();
       mainMenuStage.setResizable(false);
	   mainMenuStage.setTitle(Simulation.WINDOW_TITLE);
       mainMenuStage.setScene(mainMenuScene);
       mainMenuStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));//toString()));
       //mainSceneStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       mainMenuStage.show();

       createProgressCircle();
       circleStage.hide();

       // Starts a new stage for MainScene
	   mainSceneStage = new Stage();
	   //mainSceneStage.setMinWidth(1024);
	   //mainSceneStage.setMinHeight(400);
	   mainSceneStage.setTitle(Simulation.WINDOW_TITLE);

   }

   public Stage getStage() {
	   return mainMenuStage;
   }

   public MultiplayerMode getMultiplayerMode() {
	   return multiplayerMode;
	}

   public void runOne() {
	   //logger.info("MainMenu's runOne() is on " + Thread.currentThread().getName() + " Thread");
	   mainMenuStage.setIconified(true); //hide();
	   mainScene = new MainScene(mainSceneStage);
	   marsProjectFX.handleNewSimulation();
   }

   public void runTwo() {
	   //logger.info("MainMenu's runTwo() is on " + Thread.currentThread().getName() + " Thread");
		Platform.runLater(() -> {
			// TODO: the ring won't turn until a couple of seconds later
			circleStage.show();
			circleStage.requestFocus();

		});

	   mainMenuStage.setIconified(true);//hide();

	   ExecutorService setUpSimTasks = Simulation.instance().getSimExecutor();

	   try {
		   mainScene = new MainScene(mainSceneStage);
		   //isDone = true;
		   Future future = setUpSimTasks.submit(new LoadSimulationTask());
		   // TODO:
		   //mainMenuScene.setCursor(Cursor.WAIT);
		   //TimeUnit.SECONDS.sleep(4L);
		   // Note: java8u60 requires a longer time (than 8u45) such as 4 secs instead of 2 secs on some machines.
		   // or else it will proceed to prepareStage() below prematurely and results in NullPointerException
		   // as it tries to read people data before it was properly populated in UnitManager.
		   //service.shutdownNow();
		   //try {
		   //   service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		   //} catch (InterruptedException e) {}

		   // Note 1: The delay time for launching the JavaFX UI should also be based on the file size of the default.sim
		   //long delay_time = (long) (fileSize * 4000L);
		   //TimeUnit.MILLISECONDS.sleep(delay_time);

		   // 2015-10-10 use future.get to check if setUpSimTasks are finished before moving onto setting up the main scene
		   while(future.get() == null && isDone) {
			   prepareStage();
			   isDone = false;
		   }
		} catch (Exception e) {
			e.printStackTrace();
		}

		Platform.runLater(() -> {
			circleStage.close();
		});
   }

	public class LoadSimulationTask implements Runnable {
		public void run() {
			logger.info("Loading settlement data from the default saved simulation...");
			Simulation.instance().loadSimulation(null); // null means loading "default.sim"
			logger.info("Restarting " + Simulation.WINDOW_TITLE);
			Simulation.instance().start();
			fileSize = Simulation.instance().getFileSize();
			isDone = true;
			//System.out.println("filesize is "+ fileSize);
		}
	}

	public boolean prepareStage() {
	   //logger.info("MainMenu's prepareStage() is on " + Thread.currentThread().getName() + " Thread");

	   // prepare main scene
	   mainScene.prepareMainScene();
	   Scene scene = mainScene.initializeScene();
	   mainScene.prepareOthers();

	   // prepare stage
	   //stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       mainSceneStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));//.toString()));
	   mainSceneStage.setResizable(true);
	   //stage.setFullScreen(true);
	   mainSceneStage.setScene(scene);
	   mainSceneStage.show();

	   //mainScene.getMarsNode().createSettlementWindow();
	   //mainScene.getMarsNode().createJMEWindow(stage);
	   mainScene.initializeTheme();

	   return true;
	   //logger.info("done with stage.show() in MainMenu's prepareStage()");
	}

   public void runThree() {
	   //logger.info("MainMenu's runThree() is on " + Thread.currentThread().getName() + " Thread");
	   Simulation.instance().getSimExecutor().submit(new MultiplayerTask());
	   mainMenuStage.setIconified(true);//hide();
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


   public Parent createMarsGlobe() {
	   logger.info("Is ConditionalFeature.SCENE3D supported on this platform? " + Platform.isSupported(ConditionalFeature.SCENE3D));

	   Image sImage = new Image(this.getClass().getResource("/maps/rgbmars-spec1k.jpg").toExternalForm());
       Image dImage = new Image(this.getClass().getResource("/maps/MarsV3Shaded1k.jpg").toExternalForm());
       Image nImage = new Image(this.getClass().getResource("/maps/MarsNormal1k.png").toExternalForm()); //.toString());
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

       rt = new RotateTransition(Duration.seconds(OrbitInfo.SOLAR_DAY/500D), mars);
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

   /*
    * Adjusts Mars Globe's rotation rate according to how much the user's drags his mouse across the globe
    */
   // 2015-09-26 Added adjustRotation()
   private void adjustRotation(Scene scene, Parent parent) {

	   // 2015-09-26 Changes Mars Globe's rotation rate if a user drags his mouse across the globe
	   scene.setOnMousePressed((event) -> {
		   anchorX = event.getSceneX();
	   });

	   scene.setOnMouseDragged((event) -> {

		   double a = anchorX - event.getSceneX();
		   double rotationMultipler;

		   if (a < 0) { // left to right
			   rate = Math.abs(a/100D);
		   }
		   else { // right to left
			   rate = Math.abs(100D/a) ;
		   }

		   if (rate < 100) { // This prevents unrealistic and excessive rotation rate that creates spurious result (such as causing the direction of rotation to "flip"
			   rt.setRate(rate);
			   anchorX = 0;
			   rotationMultipler = rate * 500D;

			   //System.out.println("rate is " + rate + "   rotationMultipler is "+ rotationMultipler);

			   mainMenuController.setRotation((int)rotationMultipler);
		   }
	   });
   }

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
    * Resets the rotational rate back to 500X
    */
   // 2015-09-27 Added setDefaultRotation()
   public void setDefaultRotation() {
	   rt.setRate(1.0);
   }

	public boolean exitDialog(Stage stage) {
	   Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("Exiting MSP");
		//alert.initModality(Modality.APPLICATION_MODAL);
		alert.initOwner(stage);
		alert.setContentText("Do you really want to do this?");
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

	public void createProgressCircle() {

		StackPane stackPane = new StackPane();

		//BorderPane controlsPane = new BorderPane();
		//controlsPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		//stackPane.getChildren().add(controlsPane);
		//controlsPane.setCenter(new TableView<Void>());

		ProgressIndicator indicator = new ProgressIndicator();
		indicator.setMaxSize(120, 120);
		//indicator.setProgress(50);
		javafx.scene.effect.ColorAdjust adjust = new javafx.scene.effect.ColorAdjust();
		//Green HUE value -0.4
		adjust.setHue(17);
		indicator.setEffect(adjust);
		stackPane.getChildren().add(indicator);
		StackPane.setAlignment(indicator, Pos.CENTER);
		StackPane.setMargin(indicator, new Insets(20));
		stackPane.setBackground(javafx.scene.layout.Background.EMPTY);

		circleStage = new Stage();
		Scene scene = new Scene(stackPane, 120, 120);
		scene.setFill(Color.TRANSPARENT);
		circleStage.initStyle (StageStyle.TRANSPARENT);
		circleStage.setScene(scene);
        circleStage.show();
/*

		final Float[] values = new Float[] {-1.0f, 0f, 0.6f, 1.0f};
		final Label [] labels = new Label[values.length];
		final ProgressBar[] pbs = new ProgressBar[values.length];
		final ProgressIndicator[] pins = new ProgressIndicator[values.length];
		final HBox hbs [] = new HBox [values.length];


		circleStage = new Stage();
		Group root = new Group();
		Scene scene = new Scene(root, 300, 250);
		circleStage.setScene(scene);
		circleStage.setTitle("Progress Controls");

        for (int i = 0; i < values.length; i++) {
            final Label label = labels[i] = new Label();
            label.setText("progress:" + values[i]);

            final ProgressBar pb = pbs[i] = new ProgressBar();
            pb.setProgress(values[i]);

            final ProgressIndicator pin = pins[i] = new ProgressIndicator();
            pin.setProgress(values[i]);
            final HBox hb = hbs[i] = new HBox();
            hb.setSpacing(5);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().addAll(label, pb, pin);
        }

        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.getChildren().addAll(hbs);
        scene.setRoot(vb);
        circleStage.show();


	    circleStage = new Stage();

		RingProgressIndicator indicator = new RingProgressIndicator();
		//Slider slider = new Slider(0, 100, 50);

		final ProgressIndicator ring = new ProgressIndicator();

		//slider.valueProperty().addListener((o, oldVal, newVal) -> indicator.setProgress(newVal.intValue()));
		//VBox main = new VBox(1, indicator, slider);
		VBox main = new VBox(ring); //2,inindicator, ring);
		//indicator.setProgress(Double.valueOf(slider.getValue()).intValue());
		Group root = new Group();
		//root.getChildren().add(main);
        Scene scene = new Scene(root);//, 200, 150);
		//circleStage.initStyle(StageStyle.TRANSPARENT);
        //scene.setFill(Color.BLACK);
        //scene.setCursor(Cursor.WAIT);
        //scene.setFill(Color.TRANSPARENT); // needed to eliminate the white border
        //circleStage.initStyle (StageStyle.TRANSPARENT); //(StageStyle.UTILITY); //or
        circleStage.initStyle (StageStyle.UTILITY);
		circleStage.setScene(scene);
		scene.setRoot(main);
		circleStage.setTitle("Processing");
		circleStage.show();
*/

	}

	public Stage getCircleStage() {
		return circleStage;
	}

	public void destroy() {

		rt = null;
		mars = null;
		material = null;
		sun = null;
		root = null;
		mainSceneStage = null;
		mainMenuStage = null;
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
