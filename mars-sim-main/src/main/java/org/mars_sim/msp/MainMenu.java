/**
 * Mars Simulation Project 
 * MainMenu.java
 * @version 3.08 2015-02-05
 * @author Manny Kung
 */

package org.mars_sim.msp;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenu {

	public final static String WINDOW_TITLE = Msg.getString(
			"MainWindow.title", //$NON-NLS-1$
			Simulation.VERSION + " build " + Simulation.BUILD + 
			"  Java VM " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion()
		);
	
	// Data members
	private Stage primaryStage;
	
	private static Stage stage;
	
	public static Scene menuScene;
	public static Scene mainScene;
	public static Scene settlementScene;
	
    public static String screen1ID = "main";
    public static String screen1File = "/fxui/fxml/Main.fxml";
    public static String screen2ID = "configuration";
    public static String screen2File = "/fxui/fxml/Configuration.fxml";
    public static String screen3ID = "credits";
    public static String screen3File = "/fxui/fxml/Credits.fxml";
    
    public String[] args;
    
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

   @SuppressWarnings("restriction")
private void initAndShowGUI() {        
       
       ScreensSwitcher switcher = new ScreensSwitcher(this);
       switcher.loadScreen(MainMenu.screen1ID, MainMenu.screen1File);
       switcher.loadScreen(MainMenu.screen2ID, MainMenu.screen2File);
       switcher.loadScreen(MainMenu.screen3ID, MainMenu.screen3File);
       
       switcher.setScreen(MainMenu.screen1ID);
       
       Group root = new Group();
       root.getChildren().addAll(switcher);
       Scene scene = new Scene(root);
       
	   primaryStage.setTitle(WINDOW_TITLE);
       primaryStage.setScene(scene);
       primaryStage.show();
                  
	   stage = new Stage();

	   stage.setTitle(WINDOW_TITLE);
   }    


   public void runOne() {    
	   
	   primaryStage.setIconified(true);
	   mpFX.handleNewSimulation();
	   mpFX.startSimulation();

	   mainScene = new MainScene(stage).createMainScene();
	   stage.setFullScreen(true);
	   stage.setScene(mainScene);
	   stage.show();
	   
   }
   
   public void runTwo() {
       menuScene = new MenuScene().createMenuScene();
	   stage.setScene(menuScene);
       stage.show();
   }
   
   public void runThree() {
   		settlementScene = new SettlementScene().createSettlementScene();
	    stage.setScene(settlementScene);
	    stage.show();
   }
   
   public static void changeScene(int toscene) {
   	
	   switch(toscene) {
   
	   	case 1: {stage.setScene(menuScene);
		}
   		case 2: {stage.setScene(mainScene);
		}
   		case 3: {stage.setScene(settlementScene);
		}

	   }
   }   
   
   //public Scene getMainScene() {
   //	return mainScene;
   //}
   
}
