/**
 * Mars Simulation Project
 * SpinningGlobe.java
 * @version 3.1.0 2017-05-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.ui.javafx.config.controller.MainMenuController;

/*
 * The SpinningGlobe class creates a spinning Mars Globe for MainMenu
 */
@SuppressWarnings("restriction")
public class SpinningGlobe extends Application {

	// ------------------------------ FIELDS ------------------------------

	/** default logger. */
	private static Logger logger = Logger.getLogger(SpinningGlobe.class.getName());

    public static final int WIDTH = 768;
    public static final int HEIGHT = 768;

    private RotateTransition rt;
    private SubScene subScene;
    private MainMenuController mainMenuController;

    private Globe globe;
	
	/*
	 * Constructor for SpinningGlobe
	 */
    public SpinningGlobe(MainMenu mainMenu) {
    	//this.mainMenu = mainMenu;
    	this.mainMenuController = mainMenu.getMainMenuController();
 	}

    public SpinningGlobe() {
    }

	/*
	 * Creates a spinning globe
	 */
   public Parent createDraggingGlobe() {
	   boolean support = Platform.isSupported(ConditionalFeature.SCENE3D);
	   if (support)
		   logger.config("JavaFX 3D features supported");
	   else
		   logger.config("JavaFX 3D features NOT supported");

       globe = new Globe();
       rotateGlobe();

	   // Use a SubScene
       subScene = new SubScene(globe.getRoot(), WIDTH, HEIGHT);//, true, SceneAntialiasing.BALANCED);
       subScene.setId("sub");
       subScene.setCamera(globe.getCamera());//subScene));

       //return new Group(subScene);
       return new HBox(subScene);
   }

	/*
	 * Creates a spinning globe
	 */
  public Parent createFixedGlobe() {
	   boolean support = Platform.isSupported(ConditionalFeature.SCENE3D);
	   if (support)
		   logger.config("JavaFX 3D features supported");
	   else
		   logger.config("JavaFX 3D features NOT supported");

      globe = new Globe();
      rotateGlobe();

      //return new Group(globe.getRoot());
      return new HBox(globe.getRoot());
  }

   public void rotateGlobe() {
	   rt = new RotateTransition(Duration.seconds(OrbitInfo.SOLAR_DAY/500D), globe.getWorld());
       //rt.setByAngle(360);
       rt.setInterpolator(Interpolator.LINEAR);
       rt.setCycleCount(Animation.INDEFINITE);
       rt.setAxis(Rotate.Y_AXIS);
       rt.setFromAngle(360);
       rt.setToAngle(0);
       rt.play();
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

   public Globe getGlobe() {
	   return globe;
   }


	public static void main(String[] args) {
		launch(args);
	}

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        Parent globe = null;
        Scene scene = null;

        //if (OS.contains("window")) {
        //	globe = createFixedGlobe();
        //}
        //else {
            globe = createDraggingGlobe();
        //}

    	StackPane root = new StackPane();
        root.setPrefHeight(WIDTH);
        root.setPrefWidth(HEIGHT);
        root.setStyle(//"-fx-border-style: none; "
        			"-fx-background-color: transparent; "
        			+ "-fx-background-radius: 1px;");

        root.getChildren().add(globe);
        scene = new Scene(root, 640, 640, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);

        //if (OS.contains("window")) {

        //}
        //else {
            // Add mouse and keyboard control
            getGlobe().handleMouse(root);
            getGlobe().handleKeyboard(root);
        //}

        primaryStage.setScene(scene);
        primaryStage.show();

    }


 	public void destroy() {
 		rt = null;
 	    subScene = null;
 	    mainMenuController = null;
 	    globe = null;
 		mainMenuController = null;
 	}
}
