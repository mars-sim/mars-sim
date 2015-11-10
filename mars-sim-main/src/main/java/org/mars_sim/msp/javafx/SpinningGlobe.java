/**
 * Mars Simulation Project
 * SpinningGlobe.java
 * @version 3.08 2015-11-09
 * @author Manny Kung
 */

package org.mars_sim.msp.javafx;

import java.util.concurrent.ThreadPoolExecutor;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.bridge.SLF4JBridgeHandler;
import java.util.logging.Logger;

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

import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.javafx.controller.MainMenuController;

/*
 * The SpinningGlobe class creates a spinning Mars Globe for MainMenu
 */
public class SpinningGlobe {

	// ------------------------------ FIELDS ------------------------------

	/** default logger. */
	private static Logger logger = Logger.getLogger(SpinningGlobe.class.getName());

	/** Logger for the class */
	//private static final Logger logger;


    // -------------------------- STATIC METHODS --------------------------
    //static {
    //    SLF4JBridgeHandler.removeHandlersForRootLogger();
   //     SLF4JBridgeHandler.install();
    //    logger = LoggerFactory.getLogger(MainMenu.class);
    //}

    private static final int WIDTH = 768-20;
    private static final int HEIGHT = 768-20;

	// Data members

    private double anchorX;
    private double rate;

    //private boolean cleanUI = true;

    @SuppressWarnings("restriction")
	private final DoubleProperty sunDistance = new SimpleDoubleProperty(100);
    private final BooleanProperty sunLight = new SimpleBooleanProperty(true);
    private final BooleanProperty diffuseMap = new SimpleBooleanProperty(true);
    private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
    private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
    //private final BooleanProperty selfIlluminationMap = new SimpleBooleanProperty(true);

    private RotateTransition rt;
    private Sphere marsSphere;
    private PhongMaterial material;
    private PointLight sun;

	public MainMenuController mainMenuController;
	public MainMenu mainMenu;

	/*
	 * Constructor for SpinningGlobe
	 */
    public SpinningGlobe(MainMenu mainMenu) {
    	this.mainMenu = mainMenu;
    	this.mainMenuController = mainMenu.getMainMenuController();
 	}

	/*
	 * Creates a spinning Mars Globe
	 */
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

       marsSphere = new Sphere(4);
       marsSphere.setMaterial(material);
       marsSphere.setRotationAxis(Rotate.Y_AXIS);

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
       //globeComponents.setStyle("-fx-border-color: rgba(0, 0, 0, 0);");
       //globeComponents.setScaleX(.75);
       //globeComponents.setScaleY(.75);
       globeComponents.getChildren().add(camera);
       globeComponents.getChildren().add(marsSphere);
       globeComponents.getChildren().add(sun);
       globeComponents.getChildren().add(ambient);

       // Increased the speed of rotation 500 times as dictated by the value of time-ratio in simulation.xml
       rt = new RotateTransition(Duration.seconds(OrbitInfo.SOLAR_DAY/500D), marsSphere);
       //rt.setByAngle(360);
       rt.setInterpolator(Interpolator.LINEAR);
       rt.setCycleCount(Animation.INDEFINITE);
       rt.setAxis(Rotate.Y_AXIS);
       rt.setFromAngle(360);
       rt.setToAngle(0);
       //rt.setCycleCount(RotateTransition.INDEFINITE);
       rt.play();

       // Use a SubScene
       SubScene subScene = new SubScene(globeComponents, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
       subScene.setId("sub");
       subScene.setCamera(camera);

       return new Group(subScene);
   }

   /*
    * Adjusts Mars Globe's rotation rate according to how much the user's drags his mouse across the globe
    */
   // 2015-09-26 Added adjustRotation()
   void adjustRotation(Scene scene, Parent parent) {

	   // 2015-09-26 Changes Mars Globe's rotation rate if a user drags his mouse across the globe
	   scene.setOnMousePressed((event) -> {
		   // 2015-10-13 Detected right mouse button pressed
           if (event.isSecondaryButtonDown())
        	   anchorX = event.getSceneX();
	   });

	   scene.setOnMouseDragged((event) -> {
		   // 2015-10-13 Detected right mouse button drag
		   if (event.isSecondaryButtonDown()) {

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

/*
	public boolean exitDialog(Stage stage) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(stage);
		alert.setTitle("Exiting MSP");//("Confirmation Dialog");
		alert.setHeaderText("Do you really want to exit MPS?");
		//alert.initModality(Modality.APPLICATION_MODAL);
		alert.setContentText("Warning: exiting the main menu will terminate any running simultion without saving it.");
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
*/

	public void destroy() {

		rt = null;
		marsSphere = null;
		material = null;
		sun = null;
		mainMenu = null;
		mainMenuController = null;
	}

}
