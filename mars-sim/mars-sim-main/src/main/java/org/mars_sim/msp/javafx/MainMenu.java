/**
 * Mars Simulation Project 
 * MainMenu.java
 * @version 3.08 2015-03-15
 * @author Manny Kung
 */

package org.mars_sim.msp.javafx;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.MainScene;

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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainMenu {

    /** default logger. */
    private static Logger logger = Logger.getLogger(MainMenu.class.getName());
    
	// Data members
	
    public static String screen1ID = "main";
    public static String screen1File = "/fxui/fxml/Main.fxml";
    public static String screen2ID = "configuration";
    public static String screen2File = "/fxui/fxml/Configuration.fxml";
    public static String screen3ID = "credits";
    public static String screen3File = "/fxui/fxml/Credits.fxml";
    
    public String[] args;
    
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
	//public MenuScene menuScene;
	public MainScene mainScene;
	//public ModtoolScene modtoolScene;
	private MarsProjectFX mpFX;

    public MainMenu (MarsProjectFX mpFX, String[] args, Stage primaryStage, boolean cleanUI) {
		 //this.cleanUI =  cleanUI;
		 this.primaryStage = primaryStage;
		 this.args = args;
		 this.mpFX = mpFX;
		 initAndShowGUI();
	}

   private void initAndShowGUI() {        
       
       ScreensSwitcher switcher = new ScreensSwitcher(this);
       switcher.loadScreen(MainMenu.screen1ID, MainMenu.screen1File);
       switcher.loadScreen(MainMenu.screen2ID, MainMenu.screen2File);
       switcher.loadScreen(MainMenu.screen3ID, MainMenu.screen3File);      
       switcher.setScreen(MainMenu.screen1ID);
       
       Group root = new Group();
       Parent parent = createMarsGlobe();
       root.getChildren().addAll(parent, switcher);
       Scene scene = new Scene(root);
       
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
       
       primaryStage.centerOnScreen();
       primaryStage.setResizable(false);            
	   primaryStage.setTitle(Simulation.WINDOW_TITLE);
       primaryStage.setScene(scene);
       //primaryStage.getIcons().add(new javafx.scene.image.Image(this.getClass().getResource("/icons/LanderHab.png").toString()));
       primaryStage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       primaryStage.show();
                         
	   stage = new Stage();
	   stage.setTitle(Simulation.WINDOW_TITLE);
	   
	   mainScene = new MainScene(stage);
	   //menuScene = new MenuScene(stage);
	   //modtoolScene = new ModtoolScene(stage);
   }    


   public void runOne() {
	   
	   primaryStage.hide();
	   //primaryStage.setIconified(true);	 
	   //primaryStage.close();
	   mpFX.handleNewSimulation();

   }
   
   public void runMainScene() {
	   
	   Scene scene = mainScene.createMainScene();
       stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       //stage.getIcons().add(new Image(this.getClass().getResource("/icons/LanderHab.png").toString()));
	   stage.setResizable(true);
	   stage.setFullScreen(true);
	   stage.setScene(scene);
	   stage.show();
	   
   }
   
   public void runTwo() {
/*       menuScene = new MenuScene().createMenuScene();
	   stage.setScene(menuScene);
       stage.show();
*/
   }
   
   public void runThree() {
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
	   			Scene scene = mainScene.createMainScene();
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
	   
	   logger.info("3D supported? " + Platform.isSupported(ConditionalFeature.SCENE3D));

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
       Group root = new Group();       
       root.getChildren().add(camera);
       root.getChildren().add(mars);
       root.getChildren().add(sun);
       root.getChildren().add(ambient);
   
       RotateTransition rt = new RotateTransition(Duration.seconds(24), mars);
       //rt.setByAngle(360);
       rt.setInterpolator(Interpolator.LINEAR);
       rt.setCycleCount(Animation.INDEFINITE);
       rt.setAxis(Rotate.Y_AXIS);
       rt.setFromAngle(360);
       rt.setToAngle(0);
       //rt.setCycleCount(RotateTransition.INDEFINITE);
       rt.play();
       
       // Use a SubScene       
       SubScene subScene = new SubScene(root, 512, 512, true, SceneAntialiasing.BALANCED);
       subScene.setFill(Color.TRANSPARENT);      
       subScene.setCamera(camera);
       
       return new Group(subScene);
   }

   
}
