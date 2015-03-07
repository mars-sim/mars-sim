/**
 * Mars Simulation Project 
 * MainMenu.java
 * @version 3.08 2015-02-05
 * @author Manny Kung
 */

package org.mars_sim.msp.javafx;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.MainScene;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainMenu {

	// Data members
	private Stage primaryStage;
	
	private static Stage stage;
	
	public static Scene menuScene;
	public static Scene mainScene;
	public static Scene modtoolScene;
	
    public static String screen1ID = "main";
    public static String screen1File = "/fxui/fxml/Main.fxml";
    public static String screen2ID = "configuration";
    public static String screen2File = "/fxui/fxml/Configuration.fxml";
    public static String screen3ID = "credits";
    public static String screen3File = "/fxui/fxml/Credits.fxml";
    
    public String[] args;
    
    private Sphere mars;
    private PhongMaterial material;
    private PointLight sun;
    private final DoubleProperty sunDistance = new SimpleDoubleProperty(100);
    private final BooleanProperty sunLight = new SimpleBooleanProperty(true);
    private final BooleanProperty diffuseMap = new SimpleBooleanProperty(true);
    private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
    private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
    
    //private boolean cleanUI = true;
    
	/** The main desktop. */
	//private MainDesktopPane desktop;
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
       //switchScreen.getChildren().addAll(switcher);
       //Scene scene = new Scene(root);
       //AnchorPane root = new AnchorPane(); 
       root.getChildren().addAll(createMarsGlobe(), switcher);
       //root.getChildren().add(create3DMars());
       Scene scene = new Scene(root);
       
       //scene.setFill(Color.rgb(10, 10, 40));
       scene.setFill(Color.BLACK);
    		   
       //primaryStage.setFullScreen(true);
       primaryStage.setResizable(false);            
	   primaryStage.setTitle(Simulation.WINDOW_TITLE);
       primaryStage.setScene(scene);
       primaryStage.show();
                  
	   stage = new Stage();
	   stage.setTitle(Simulation.WINDOW_TITLE);
   }    


   public void runOne() {    
	   
	   primaryStage.setIconified(true);
	   mpFX.handleNewSimulation();
	   mpFX.startSimulation();
	   
	   mainScene = new MainScene(stage).createMainScene();
       stage.getIcons().add(new javafx.scene.image.Image(this.getClass().getResource("/icons/LanderHab.png").toString()));
	   stage.setFullScreen(true);
	   stage.setResizable(true);
	   stage.setScene(mainScene);
	   stage.show();
	   
   }
   
   public void runTwo() {
       menuScene = new MenuScene().createMenuScene();
	   stage.setScene(menuScene);
       stage.show();
   }
   
   public void runThree() {
   		modtoolScene = new SettlementScene().createSettlementScene();
	    stage.setScene(modtoolScene);
	    stage.show();
   }
   
   public static void changeScene(int toscene) {	
	   switch(toscene) {	   
		   	case 1: {stage.setScene(menuScene);}
	   		case 2: {stage.setScene(mainScene);}
	   		case 3: {stage.setScene(modtoolScene);}
	   }
   }   
   
   //public Scene getMainScene() {
   //	return mainScene;
   //}
   

   public Parent createMarsGlobe() {
       //Image dImage = new Image(this.getClass().getResource("/maps/mars_mola_hypsometric_8192x4096.jpg").toExternalForm());
       //Image nImage = new Image(this.getClass().getResource("/maps/mars_mola_bumpmap_sealevel_8192x4096.jpg").toExternalForm());
       //Image sImage = new Image(this.getClass().getResource("/maps/mars_mola_specularmap_8192x4096.jpg").toExternalForm());
       Image dImage = new Image(this.getClass().getResource("/maps/mars_mola_hypsometric_8192x4096.jpg").toString());
       Image nImage = new Image(this.getClass().getResource("/maps/mars_mola_bumpmap_sealevel_8192x4096.jpg").toString());
       Image sImage = new Image(this.getClass().getResource("/maps/mars_mola_specularmap_8192x4096.jpg").toString());
         
       material = new PhongMaterial();
       material.setDiffuseColor(Color.WHITE);
       material.diffuseMapProperty().bind(
               Bindings.when(diffuseMap).then(dImage).otherwise((Image) null));
       material.setSpecularColor(Color.TRANSPARENT);
       material.specularMapProperty().bind(
               Bindings.when(specularMap).then(sImage).otherwise((Image) null));
       material.bumpMapProperty().bind(
               Bindings.when(bumpMap).then(nImage).otherwise((Image) null));
       //material.selfIlluminationMapProperty().bind(
        //       Bindings.when(selfIlluminationMap).then(siImage).otherwise((Image) null));
       
       mars = new Sphere(4);
       mars.setMaterial(material);
       mars.setRotationAxis(Rotate.Y_AXIS);
       
       
       // Create and position camera
       PerspectiveCamera camera = new PerspectiveCamera(true);
       camera.getTransforms().addAll(
               new Rotate(-20, Rotate.Y_AXIS),
               new Rotate(-20, Rotate.X_AXIS),
               new Translate(0, 0, -20));
       
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
       //root.getChildren().add(switchScreen);
       
       RotateTransition rt = new RotateTransition(Duration.seconds(24), mars);
       rt.setByAngle(360);
       rt.setInterpolator(Interpolator.LINEAR);
       rt.setCycleCount(Animation.INDEFINITE);
       rt.play();
       
       // Use a SubScene       
       SubScene subScene = new SubScene(root, 480, 480, true, SceneAntialiasing.BALANCED);
       subScene.setFill(Color.TRANSPARENT);
       
       subScene.setCamera(camera);
       
       return new Group(subScene);
   }

   
}
